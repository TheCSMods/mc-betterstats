package io.github.thecsdev.betterstats.client.gui.screen;

import static io.github.thecsdev.betterstats.BetterStats.LOGGER;
import static io.github.thecsdev.betterstats.BetterStats.getModID;
import static io.github.thecsdev.betterstats.client.BetterStatsClient.MC_CLIENT;
import static io.github.thecsdev.betterstats.util.io.BetterStatsWebApiUtils.GSON;
import static io.github.thecsdev.tcdcommons.api.util.io.HttpUtils.fetchSync;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.thread.BlockableEventLoop;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;

import io.github.thecsdev.betterstats.api.client.gui.screen.BetterStatsScreen;
import io.github.thecsdev.betterstats.api.util.io.RAMStatsProvider;
import io.github.thecsdev.betterstats.util.BST;
import io.github.thecsdev.betterstats.util.io.BetterStatsWebApiUtils;
import io.github.thecsdev.tcdcommons.api.client.gui.layout.UIListLayout;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TLabelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TStackTracePanel;
import io.github.thecsdev.tcdcommons.api.util.enumerations.Axis2D;
import io.github.thecsdev.tcdcommons.api.util.enumerations.HorizontalAlignment;
import io.github.thecsdev.tcdcommons.api.util.enumerations.VerticalAlignment;
import io.github.thecsdev.tcdcommons.api.util.io.HttpUtils.FetchOptions;
import io.github.thecsdev.tcdcommons.api.util.io.cache.CachedResource;
import io.github.thecsdev.tcdcommons.api.util.io.cache.CachedResourceManager;
import io.github.thecsdev.tcdcommons.api.util.io.cache.IResourceFetchTask;
import io.netty.buffer.Unpooled;

public class QuickShareDownloadScreen extends QuickShareScreen
{
	// ==================================================
	private @Nullable Screen bssParent;
	private final String quickShareCode;
	// --------------------------------------------------
	private @Internal volatile boolean   __started = false;
	private @Internal volatile int       __stage   = 0;
	private @Internal volatile Throwable __error   = null;
	// ==================================================
	public QuickShareDownloadScreen(@Nullable Screen bssParent, @Nullable Screen parent, String quickShareCode)
		throws NullPointerException
	{
		super(parent, BST.gui_qsscreen_download_title());
		this.bssParent = bssParent;
		Objects.requireNonNull(quickShareCode);
		quickShareCode = quickShareCode.toLowerCase(Locale.ENGLISH);
		if(!quickShareCode.endsWith(QSC_SUFFIX)) quickShareCode += QSC_SUFFIX;
		this.quickShareCode = quickShareCode;
	}
	// ==================================================
	protected final @Override void init()
	{
		//start the operation
		__start__stage1and2and3();
		
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
				panel_st.setTitle(BST.gui_qsscreen_download_stageN1().getString());
				panel_st.setDescription(this.__error.getMessage());
				addChild(panel_st, false);
				new UIListLayout(Axis2D.Y, VerticalAlignment.CENTER, HorizontalAlignment.CENTER).apply(this);
				break;
			case 0: lbl.setText(BST.gui_qsscreen_download_stage0()); break;
			case 1: lbl.setText(BST.gui_qsscreen_download_stage1()); break;
			case 2: lbl.setText(BST.gui_qsscreen_download_stage2()); break;
			case 3: lbl.setText(BST.gui_qsscreen_download_stage3()); break;
			case 4: lbl.setText(BST.gui_qsscreen_download_stage4()); break;
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
				"[Quick-Share] Failed to download quick-shared statistics using the code; " + this.quickShareCode,
				exception);
	}
	// --------------------------------------------------
	private @Internal void __start__stage1and2and3()
	{
		//prepare
		if(this.__started) return;
		this.__started = true;
		this.__stage = 1;
		if(!isOpen()) return; //break the operation if the user closed the screen
		//note: do not call `refresh()` here
		
		//log
		LOGGER.info("[Quick-Share] Downloading quick-shared statistics using the code; " + this.quickShareCode);
		
		//parse the user-input identifier
		@Nullable ResourceLocation mcbsCachedId = null;
		try { mcbsCachedId = ResourceLocation.fromNamespaceAndPath(getModID(), "quick_share/downloads/" + this.quickShareCode); }
		catch(Exception e) { __start_onError(e); return; }
		
		//fetch
		CachedResourceManager.getResourceAsync(mcbsCachedId, new IResourceFetchTask<byte[]>()
		{
			public final @Override BlockableEventLoop<?> getMinecraftClientOrServer() { return MC_CLIENT; }
			public final @Override Class<byte[]> getResourceType() { return byte[].class; }
			public final @Override CachedResource<byte[]> fetchResourceSync() throws Exception
			{
				//fetch the API URLs
				final AtomicReference<JsonObject> au_ready = new AtomicReference<JsonObject>();
				final AtomicReference<Exception>  au_error = new AtomicReference<Exception>();
				BetterStatsWebApiUtils.fetchBssApiLinks(
						false, MC_CLIENT,
						json -> au_ready.set(json),
						error -> au_error.set(error));
				
				//handle the API URL fetch outcome
				if(au_error.get() != null)
				{
					if(au_error.get() instanceof HttpResponseException hre)
					{
						final var msg = "HTTP " + hre.getStatusCode() + " " + hre.getReasonPhrase();
						final var txt = BST.gui_qsscreen_err_cmmn_fau_httpN200(msg).getString();
						throw new IOException(txt, au_error.get());
					}
					else
					{
						final var txt = BST.gui_qsscreen_err_cmmn_fau_generic().getString();
						throw new IOException(txt, au_error.get());
					}
				}
				else if(au_ready.get() == null) throw new NullPointerException("This shouldn't happen.");
				
				//go from stage 1 into stage 2
				QuickShareDownloadScreen.this.__stage = 2;
				if(!QuickShareDownloadScreen.this.isOpen())
					throw new RuntimeException("Aborted."); //break the operation if the user closed the screen
				QuickShareDownloadScreen.this.refresh();
				
				//fetch the download link
				final AtomicReference<JsonObject> du_ready = new AtomicReference<JsonObject>();
				final AtomicReference<Exception>  du_error = new AtomicReference<Exception>();
				CachedResourceManager.getResourceSync( //NOTE: MUST BE SYNCHRONOUS!
						ResourceLocation.fromNamespaceAndPath(
								getModID(),
								"quick_share/download_urls/" +
								QuickShareDownloadScreen.this.quickShareCode + ".json"),
						new IResourceFetchTask<JsonObject>()
				{
					public final @Override BlockableEventLoop<?> getMinecraftClientOrServer() { return MC_CLIENT; }
					public final @Override Class<JsonObject> getResourceType() { return JsonObject.class; }
					public final @Override CachedResource<JsonObject> fetchResourceSync() throws Exception
					{
						//perform the request
						final     JsonObject            links    = au_ready.get();
						@Nullable CloseableHttpResponse response = null;
						try
						{
							//obtain the endpoint url
							String endpoint = null;
							try { endpoint = links.get("quickshare_gdu").getAsString(); }
							catch(Exception exc)
							{
								var additionalNote = "-";
								if(links.has("quickshare_notice") && links.get("quickshare_notice").isJsonPrimitive())
									additionalNote = links.get("quickshare_notice").getAsString();
								throw new IOException(BST.gui_qsscreen_err_cmmn_fau_mssngUrl(additionalNote).getString(), exc);
							}
							
							
							//send a request to the endpoint url
							response = fetchSync(endpoint, new FetchOptions()
							{
								public final @Override String method() { return "POST"; }
								public final @Override Object body()
								{
									final var bodyJson = new JsonObject();
									addTelemetryData(bodyJson);
									bodyJson.addProperty("file", QuickShareDownloadScreen.this.quickShareCode);
									return bodyJson;
								}
							});
							
							//collect the response message
							String responseMessage = "";
							if(response.getEntity() != null)
								responseMessage = EntityUtils.toString(response.getEntity());
							
							//handle the response status code
							final var statusCode = response.getStatusLine().getStatusCode();
							final var statusMessage = response.getStatusLine().getReasonPhrase();
							if(statusCode != 200)
								throw new IOException(
									BST.gui_qsscreen_err_dwnl_gdu_httpN200(
											"HTTP " + statusCode + " " + statusMessage + "\n" + responseMessage
										).getString(),
									new HttpResponseException(statusCode, statusMessage));
							
							//parse the response
							final var json = GSON.fromJson(responseMessage, JsonObject.class);
							@Nullable Instant expires = null;
							try { expires = Instant.parse(json.get("expires").getAsString()); }
							catch(Exception parseExc) { expires = Instant.now().plusSeconds(30); }
							
							//return the response json
							return new CachedResource<JsonObject>(json, responseMessage.length(), expires);
						}
						finally { if(response != null) IOUtils.closeQuietly(response); }
					}
					public final @Override void onError(Exception error) { du_error.set(error); }
					public final @Override void onReady(JsonObject result) { du_ready.set(result); }
				});
				
				//handle the download link fetch outcome
				if(du_error.get() != null) throw du_error.get();
				else if(du_ready.get() == null) throw new NullPointerException("This shouldn't happen.");
				
				//go from stage 2 into stage 3
				QuickShareDownloadScreen.this.__stage = 3;
				if(!QuickShareDownloadScreen.this.isOpen())
					throw new RuntimeException("Aborted."); //break the operation if the user closed the screen
				QuickShareDownloadScreen.this.refresh();
				
				//prepare to download the MCBS file
				final var downloadUrlData = du_ready.get();
				final var url     = downloadUrlData.get("url").getAsString();
				final var method  = downloadUrlData.get("method").getAsString().toUpperCase(Locale.ENGLISH);
				final var headers = downloadUrlData.get("headers").getAsJsonObject()
						.entrySet().stream()
						.map(entry -> new BasicHeader(entry.getKey(), entry.getValue().getAsString()))
						.toArray(BasicHeader[]::new);
				
				//checks
				if(!Objects.equals(method, "GET"))
					throw new UnsupportedOperationException("BSS API server told me to perform HTTP " + method +
							" to download the quick-share file, but I only support HTTP GET.");
				
				//perform the download
				@Nullable CloseableHttpResponse response = null;
				try
				{
					//fetch
					response = fetchSync(url, new FetchOptions()
					{
						public final @Override String   method() { return method; }
						public final @Override Header[] headers() { return headers; }
					});
					
					//handle non-200 status codes
					final var statusCode = response.getStatusLine().getStatusCode();
					final var statusMessage = response.getStatusLine().getReasonPhrase();
					if(statusCode != 200)
					{
						//read the response body
						@Nullable String responseBody = "";
						if(response.getEntity() != null)
							responseBody = EntityUtils.toString(response.getEntity());
						
						//throw the exception
						throw new IOException(
								BST.gui_qsscreen_err_dwnl_act_httpN200(
										"HTTP " + statusCode + " " + statusMessage + "\n" + responseBody
									).getString(),
								new HttpResponseException(statusCode, statusMessage));
					}
					
					//handle the response body
					final byte[] responseBody = (response.getEntity() != null) ?
							EntityUtils.toByteArray(response.getEntity()) : null;
					if(responseBody == null)
						throw new IOException("Cloud server responded with an empty file with no data inside of it.");
					
					//finally, conclude
					@Nullable Instant expires_file = null;
					try { expires_file = Instant.parse(downloadUrlData.get("expires_file").getAsString()); }
					catch(Exception parseExc) { expires_file = Instant.now().plus(Duration.ofDays(1)); }
					
					return new CachedResource<byte[]>(responseBody, responseBody.length, expires_file);
				}
				finally { if(response != null) IOUtils.closeQuietly(response); }
			}
			public final @Override void onError(Exception error) { __start_onError(error); }
			public final @Override void onReady(byte[] result) { __start__stage4(result); }
		});
	}
	
	private @Internal void __start__stage4(final byte[] mcbs)
	{
		this.__stage = 4;
		if(!isOpen()) return; //break the operation if the user closed the screen
		refresh();
		try
		{
			final var buffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(mcbs));
			final var stats  = new RAMStatsProvider(buffer, true);
			final var bss    = new BetterStatsScreen(this.bssParent, stats);
			MC_CLIENT.setScreen(bss.getAsScreen());
			
			//log
			LOGGER.info("[Quick-Share] Succesfully downloaded quick-shared statistics using the code; " + this.quickShareCode);
		}
		catch(Exception exc) { __start_onError(exc); }
	}
	// ==================================================
}