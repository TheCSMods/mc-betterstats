package io.github.thecsdev.betterstats.client.gui.screen;

import static io.github.thecsdev.betterstats.BetterStats.LOGGER;
import static io.github.thecsdev.betterstats.BetterStats.getModID;
import static io.github.thecsdev.betterstats.client.BetterStatsClient.MC_CLIENT;
import static io.github.thecsdev.betterstats.util.io.BetterStatsWebApiUtils.GSON;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;
import static io.github.thecsdev.tcdcommons.api.util.io.HttpUtils.fetchSync;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.zip.GZIPOutputStream;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.thread.BlockableEventLoop;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Charsets;
import com.google.gson.JsonObject;

import io.github.thecsdev.betterstats.api.util.io.IStatsProvider;
import io.github.thecsdev.betterstats.api.util.io.StatsProviderIO;
import io.github.thecsdev.betterstats.util.BST;
import io.github.thecsdev.betterstats.util.io.BetterStatsWebApiUtils;
import io.github.thecsdev.tcdcommons.api.client.gui.layout.UIListLayout;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TLabelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TStackTracePanel;
import io.github.thecsdev.tcdcommons.api.client.gui.widget.TButtonWidget;
import io.github.thecsdev.tcdcommons.api.util.enumerations.Axis2D;
import io.github.thecsdev.tcdcommons.api.util.enumerations.HorizontalAlignment;
import io.github.thecsdev.tcdcommons.api.util.enumerations.VerticalAlignment;
import io.github.thecsdev.tcdcommons.api.util.io.HttpUtils.FetchOptions;
import io.github.thecsdev.tcdcommons.api.util.io.cache.CachedResource;
import io.github.thecsdev.tcdcommons.api.util.io.cache.CachedResourceManager;
import io.github.thecsdev.tcdcommons.api.util.io.cache.IResourceFetchTask;
import io.github.thecsdev.tcdcommons.api.util.math.Tuple2;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;

public final class QuickShareUploadScreen extends QuickShareScreen
{
	// ==================================================
	private static final String CRLF = "\r\n";
	// --------------------------------------------------
	private @Internal volatile boolean   __started        = false;
	private @Internal volatile int       __stage          = 0;
	private @Internal volatile Throwable __error          = null;
	private @Internal volatile String    __quickShareCode = null; //is present only after the upload successfully finishes
	// --------------------------------------------------
	private final IStatsProvider stats;
	// ==================================================
	public QuickShareUploadScreen(@Nullable Screen parent, IStatsProvider stats)
			throws NullPointerException
	{
		super(parent, BST.gui_qsscreen_upload_title());
		this.stats = Objects.requireNonNull(stats);
	}
	// ==================================================
	protected final @Override void init()
	{
		//start the operation
		__start__stage1();
		
		//the primary label
		final var lbl = new TLabelElement(0, 0, getWidth(), getHeight());
		lbl.setTextHorizontalAlignment(HorizontalAlignment.CENTER);
		lbl.setTextColor(0xffffff00);
		addChild(lbl, false);
		
		//the primary label text
		switch(this.__stage)
		{
			case -1:
				removeChild(lbl, false);
				int w = (int) ((float) this.getWidth() * 0.6F);
				if (w < 300) w = 300; if (w > this.getWidth()) w = this.getWidth();
				final var panel_st = new TStackTracePanel(0, 0, w, this.getHeight() - 50, this.__error);
				panel_st.setCloseAction(() -> close());
				panel_st.setTitle(BST.gui_qsscreen_upload_stageN1().getString());
				panel_st.setDescription(this.__error.getMessage());
				addChild(panel_st, false);
				new UIListLayout(Axis2D.Y, VerticalAlignment.CENTER, HorizontalAlignment.CENTER).apply(this);
				break;
			case 0: lbl.setText(BST.gui_qsscreen_upload_stage0()); break;
			case 1: lbl.setText(BST.gui_qsscreen_upload_stage1()); break;
			case 2: lbl.setText(BST.gui_qsscreen_upload_stage2()); break;
			case 3: lbl.setText(BST.gui_qsscreen_upload_stage3()); break;
			case 4:
				//set final stage label text
				final var codeStr = StringUtils.removeEnd("" + this.__quickShareCode, QSC_SUFFIX)
					.toUpperCase(Locale.ENGLISH);
				final var codeTxt = literal(codeStr).withStyle(ChatFormatting.WHITE);
				lbl.setText(BST.gui_qsscreen_upload_stage4(codeTxt));
				
				//add a "Done" button
				final var btn_done = new TButtonWidget((getWidth() / 2) - 75, getHeight() - 30, 150, 20);
				btn_done.setText(translatable("gui.done"));
				btn_done.setOnClick(__ -> close());
				addChild(btn_done, false);
				break;
			default: break;
		}
	}
	// ==================================================
	private @Internal void __start_onError(@Nullable Exception exception)
	{
		this.__stage = -1;
		this.__error = exception;
		if(!isOpen()) return; //break the operation if the user closed the screen
		refresh();
		
		//log
		LOGGER.error(
				"[Quick-share] Failed to upload quick-share statistics." +
				(this.__quickShareCode != null ? " The quick-share code is: " + this.__quickShareCode : ""),
				exception);
	}
	// --------------------------------------------------
	private @Internal void __start__stage1()
	{
		//prepare
		if(this.__started) return;
		this.__started = true;
		this.__stage = 1;
		if(!isOpen()) return; //break the operation if the user closed the screen
		//note: do not call `refresh()` here
		
		//log
		LOGGER.info("[Quick-share] Uploading quick-share statistics...");
		
		//fetch the API links
		BetterStatsWebApiUtils.fetchBssApiLinksAsync(MC_CLIENT,
				json -> __start__stage2(json),
				error ->
				{
					if(error instanceof HttpResponseException hre)
					{
						final var msg = "HTTP " + hre.getStatusCode() + " " + hre.getReasonPhrase();
						final var txt = BST.gui_qsscreen_err_cmmn_fau_httpN200(msg).getString();
						__start_onError(new IOException(txt, error));
					}
					else
					{
						final var txt = BST.gui_qsscreen_err_cmmn_fau_generic().getString();
						__start_onError(new IOException(txt, error));
					}
				});
	}
	
	private @Internal void __start__stage2(final JsonObject links)
	{
		//prepare
		this.__stage = 2;
		if(!isOpen()) return; //break the operation if the user closed the screen
		refresh();
		
		//fetch the upload link
		CachedResourceManager.getResourceAsync(
				ResourceLocation.fromNamespaceAndPath(getModID(), "quick_share/latest_upload_url.json"),
				new IResourceFetchTask<JsonObject>()
		{
			public BlockableEventLoop<?> getMinecraftClientOrServer() { return MC_CLIENT; }
			public Class<JsonObject> getResourceType() { return JsonObject.class; }
			public CachedResource<JsonObject> fetchResourceSync() throws Exception
			{
				//obtain the API url
				String endpoint = null;
				try { endpoint = links.get("quickshare_guu").getAsString(); }
				catch(Exception e)
				{
					var additionalNote = "-";
					if(links.has("quickshare_notice") && links.get("quickshare_notice").isJsonPrimitive())
						additionalNote = links.get("quickshare_notice").getAsString();
					throw new IOException(BST.gui_qsscreen_err_cmmn_fau_mssngUrl(additionalNote).getString(), e);
				}
				
				//send an http request to the endpoint
				final var httpResult = fetchSync(endpoint, new FetchOptions()
				{
					public final @Override String method() { return "POST"; }
					public final @Override Object body()
					{
						final var bodyJson = new JsonObject();
						addTelemetryData(bodyJson);
						return bodyJson;
					}
				});
				@Nullable String httpResultStr = null;
				try
				{
					//read the response body as string
					final @Nullable var httpResultEntity = httpResult.getEntity();
					if(httpResultEntity == null) throw new IOException("Missing HTTP response body.");
					httpResultStr = EntityUtils.toString(httpResultEntity);
					
					//throw an exception if the server does not respond with status 200
					final int statusCode = httpResult.getStatusLine().getStatusCode();
					final var statusMessage = httpResult.getStatusLine().getReasonPhrase();
					if(statusCode != 200)
						throw new IOException(
							BST.gui_qsscreen_err_upld_guu_httpN200(
									"HTTP " + statusCode + " " + statusMessage + "\n" + httpResultStr
								).getString(),
							new HttpResponseException(statusCode, httpResult.getStatusLine().getReasonPhrase()));
				}
				finally { IOUtils.closeQuietly(httpResult); }
				
				//handle the result
				final var result = GSON.fromJson(httpResultStr, JsonObject.class);
				@Nullable Instant expires = null;
				
				try { expires = Instant.parse(result.get("expires").getAsString()); }
				catch(Exception insParseErr) { expires = Instant.now().plusSeconds(30); }
				
				return new CachedResource<JsonObject>(result, httpResultStr.length(), expires);
			}
			public void onError(Exception error) { __start_onError(error); }
			public void onReady(JsonObject result) { __start__stage3(result); }
		});
	}
	
	private @Internal void __start__stage3(final JsonObject uploadUrlData)
	{
		//prepare
		this.__stage = 3;
		if(!isOpen()) return; //break the operation if the user closed the screen
		refresh();
		
		//perform the upload
		CachedResourceManager.getResourceAsync(
				ResourceLocation.fromNamespaceAndPath(getModID(), "quick_share/uploads/" + System.currentTimeMillis() + ".txt"),
				new IResourceFetchTask<String>()
		{
			public BlockableEventLoop<?> getMinecraftClientOrServer() { return MC_CLIENT; }
			public Class<String> getResourceType() { return String.class; }
			public CachedResource<String> fetchResourceSync() throws Exception
			{
				//obtain upload request information
				final String        url      = uploadUrlData.get("url").getAsString();
				final String        method   = uploadUrlData.get("method").getAsString().toUpperCase(Locale.ENGLISH);
				final String        filename = uploadUrlData.get("filename").getAsString();
				final var           fields   = uploadUrlData.get("fields").getAsJsonObject();
				final BasicHeader[] headers  = uploadUrlData.get("headers").getAsJsonObject()
					.entrySet().stream()
					.map(entry -> new BasicHeader(entry.getKey(), entry.getValue().getAsString()))
					.toArray(BasicHeader[]::new);
				
				//checks
				if(!Objects.equals(method, "POST"))
					throw new UnsupportedOperationException("BSS API server told me to perform HTTP " + method +
							" to upload the quick-share file, but I only support HTTP POST.");
				
				//construct and upload the HTTP request body
				final var multipartBoundary = "----WebKitFormBoundary@" + System.currentTimeMillis();
				final var httpBody = new FriendlyByteBuf(Unpooled.buffer());
				@Nullable CloseableHttpResponse response = null;
				try
				{
					//add multipart form fields to the request body
					for(final var field : fields.entrySet())
					{
						final var str = "--" + multipartBoundary + CRLF +
							"Content-Disposition: form-data; name=\"" + field.getKey() + "\"" + CRLF +
							"Content-Type: text/plain" + CRLF + CRLF +
							field.getValue().getAsString() + CRLF;
						httpBody.writeBytes(str.getBytes(Charsets.UTF_8));
					}
					{
						final var statsBytes = exportStatsBytes();
						final var str = "--" + multipartBoundary + CRLF +
							"Content-Disposition: form-data; name=\"file\"; filename=\"" + filename + "\"" + CRLF +
							"Content-Type: application/octet-stream" + CRLF +
							"Cache-Control: no-transform" + CRLF +
							(statsBytes.Item2 ? "Content-Encoding: gzip" + CRLF : "") +
							CRLF;
						httpBody.writeBytes(str.getBytes(Charsets.UTF_8));
						httpBody.writeBytes(statsBytes.Item1);
						httpBody.writeBytes(CRLF.getBytes(Charsets.UTF_8));	
					}
					httpBody.writeBytes(("--" + multipartBoundary + "--" + CRLF).getBytes(Charsets.UTF_8));
					
					//perform the request
					response = fetchSync(url, new FetchOptions()
					{
						public final @Override String   method() { return method; }
						public final @Override Header[] headers() { return headers; }
						public final @Override Object   body()
						{
							return new InputStreamEntity(
									new ByteBufInputStream(httpBody),
									ContentType.parse("multipart/form-data; boundary=" + multipartBoundary));
						}
						
					});
					
					//read the response body
					@Nullable String responseBody = "";
					if(response.getEntity() != null)
						responseBody = EntityUtils.toString(response.getEntity());
					
					//handle non-2XX responses
					final var statusCode = response.getStatusLine().getStatusCode();
					final var statusReason = response.getStatusLine().getReasonPhrase();
					if(statusCode < 200 || statusCode > 299)
						throw new IOException(
							BST.gui_qsscreen_err_upld_act_httpN200(
									"HTTP " + statusCode + " " + statusReason + "\n" + responseBody
								).getString(),
							new HttpResponseException(statusCode, statusReason));
					
					//finally, conclude
					QuickShareUploadScreen.this.__quickShareCode = filename;
					final var result = new StringBuilder();
					result.append("HTTP " + method + " " + url + CRLF);
					result.append("Response: " + statusCode + " " + statusReason + CRLF);
					result.append("Response body:" + CRLF + CRLF + responseBody);
					
					return new CachedResource<String>(
							result.toString(),
							result.length(),
							Instant.now().plus(Duration.ofDays(7)));
				}
				finally
				{
					try { httpBody.release(); } catch(Exception e) {}
					if(response != null) IOUtils.closeQuietly(response);
				}
			}
			public void onError(Exception error) { __start_onError(error); }
			public void onReady(String result) { __start__stage4(); }
		});
	}
	
	private @Internal void __start__stage4()
	{
		this.__stage = 4;
		if(!isOpen()) return; //break the operation if the user closed the screen
		refresh();
		
		//log
		LOGGER.info("[Quick-share] Succesfully uploaded quick-share statistics. The code is: " + this.__quickShareCode);
	}
	// ==================================================
	/**
	 * Exports the {@link #stats} bytes, in either GZip compressed or
	 * uncompressed format, whichever takes up less space.
	 * 
	 * @apiNote May result in duplicate data in-RAM as the compression takes place,
	 * but the duplicate data is erased from RAM shortly after.
	 */
	private @Internal Tuple2<byte[], Boolean> exportStatsBytes()
	{
		//prepare
		byte[] raw        = null;
		byte[] compressed = null;
		
		//obtain the raw statistics
		final var rawBuffer = new FriendlyByteBuf(Unpooled.buffer());
		try
		{
			StatsProviderIO.write(rawBuffer, this.stats);
			raw = new byte[rawBuffer.readableBytes()];
			rawBuffer.readBytes(raw);
		}
		finally { rawBuffer.release(); } //Note: Always release to avoid memory leaks.
		
		//obtain the compressed statistics
		final var compressedBuffer = new FriendlyByteBuf(Unpooled.buffer());
		try
		{
			final var outputStream = new ByteBufOutputStream(compressedBuffer);
			try (var gzipOutputStream = new GZIPOutputStream(outputStream)) { gzipOutputStream.write(raw); }
			catch (IOException e) { throw new RuntimeException("Error during GZip compression of MCBS data", e); }
			
			compressed = new byte[compressedBuffer.readableBytes()];
			compressedBuffer.readBytes(compressed);
		}
		finally { compressedBuffer.release(); } //Note: Always release to avoid memory leaks.
		
		//log
		LOGGER.info("[Quick-share] Attempting to compress quick-share MCBS data using GZip. Raw file size is " +
				raw.length + ", and compressed file size is " + compressed.length + ".");
		
		//return the shortest outcome
		if(compressed.length < raw.length) return new Tuple2<>(compressed, true);
		else return new Tuple2<>(raw, false);
	}
	// ==================================================
}