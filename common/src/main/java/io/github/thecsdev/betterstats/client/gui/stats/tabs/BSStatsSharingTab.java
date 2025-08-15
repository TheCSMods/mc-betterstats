package io.github.thecsdev.betterstats.client.gui.stats.tabs;

import static io.github.thecsdev.betterstats.api.client.gui.panel.BSComponentPanel.BS_WIDGETS_TEXTURE;
import static io.github.thecsdev.betterstats.client.BetterStatsClient.MC_CLIENT;
import static io.github.thecsdev.tcdcommons.TCDCommonsConfig.RESTRICTED_MODE;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.awt.Rectangle;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.StringUtils;

import io.github.thecsdev.betterstats.BetterStatsProperties;
import io.github.thecsdev.betterstats.api.client.gui.screen.BetterStatsScreen;
import io.github.thecsdev.betterstats.api.client.gui.util.StatsTabUtils;
import io.github.thecsdev.betterstats.api.client.registry.StatsTab;
import io.github.thecsdev.betterstats.client.gui.screen.QuickShareDownloadScreen;
import io.github.thecsdev.betterstats.client.gui.screen.QuickShareUploadScreen;
import io.github.thecsdev.betterstats.client.network.BetterStatsClientPlayNetworkHandler;
import io.github.thecsdev.betterstats.util.BST;
import io.github.thecsdev.tcdcommons.api.client.gui.layout.UILayout;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TBlankElement;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TTextureElement;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.util.GuiUtils;
import io.github.thecsdev.tcdcommons.api.client.gui.util.TDrawContext;
import io.github.thecsdev.tcdcommons.api.client.gui.util.UITexture;
import io.github.thecsdev.tcdcommons.api.client.gui.widget.TButtonWidget;
import io.github.thecsdev.tcdcommons.api.client.gui.widget.TCheckboxWidget;
import io.github.thecsdev.tcdcommons.api.client.gui.widget.TTextFieldWidget;
import io.github.thecsdev.tcdcommons.api.util.TextUtils;

public class BSStatsSharingTab extends StatsTab
{
	// ==================================================
	private static final UITexture TEX_UPLOAD   = new UITexture(BS_WIDGETS_TEXTURE, new Rectangle(0,  120, 20, 20));
	private static final UITexture TEX_DOWNLOAD = new UITexture(BS_WIDGETS_TEXTURE, new Rectangle(20, 120, 20, 20));
	// --------------------------------------------------
	private static boolean LEGAL_QS_CONSENT = false;
	// ==================================================
	public final @Override Component getName() { return BST.menu_statsSharing(); }
	public final boolean isAvailable() { return false; }
	// ==================================================
	public final @Override void initFilters(FiltersInitContext initContext)
	{
		StatsTabUtils.initDefaultFilters(initContext);
		if(!RESTRICTED_MODE)
		{
			final var panel = initContext.getFiltersPanel();
			
			initSeparator(panel, 5);
			StatsTabUtils.initGroupLabel(panel, TextUtils.translatable("mco.warning"));
			initMultilineLabel(panel, BST.gui_tpsbs_qs_tosnotice(), 0xccffddff);
			initSeparator(panel, 5);
			
			final var n1 = UILayout.nextChildVerticalRect(panel);
			final var btn_openTos = new TButtonWidget(n1.x, n1.y, n1.width, 20);
			btn_openTos.setText(TextUtils.translatable("symlink_warning.more_info"));
			btn_openTos.setOnClick(__ -> GuiUtils.showUrlPrompt(BetterStatsProperties.URL_QS_LEGAL, true));
			panel.addChild(btn_openTos, false);
			
			final var n2 = UILayout.nextChildVerticalRect(panel);
			final var check_consent = new TCheckboxWidget(
					n2.x, n2.y + 3, n2.width, n2.height,
					TextUtils.translatable("mco.terms.buttons.agree"),
					LEGAL_QS_CONSENT);
			check_consent.setOnClick(__ ->
			{
				LEGAL_QS_CONSENT = check_consent.getChecked();
				initContext.refreshStatsTab();
			});
			panel.addChild(check_consent, false);
		}
	}
	// --------------------------------------------------
	public final @Override void initStats(StatsInitContext initContext)
	{
		init_ssps(initContext);
		if(!RESTRICTED_MODE) init_quickShare(initContext);
	}
	
	private final void init_ssps(StatsInitContext initContext) 
	{
		//prepare
		final var cpnh  = Optional.ofNullable(BetterStatsClientPlayNetworkHandler.getInstance());
		final var panel = initContext.getStatsPanel();
		
		//init the group label and its description
		StatsTabUtils.initGroupLabel(panel, BST.gui_tpsbs_ssps());
		initMultilineLabel(panel, BST.gui_tpsbs_ssps_description(), 0xffffffff);
		
		//the input and submit widgets
		final var n1 = UILayout.nextChildVerticalRect(panel); n1.y += 3;
		final var a  = cpnh.isPresent() && cpnh.get().comms();
		
		final var in_name = new TTextFieldWidget(n1.x, n1.y, n1.width - 25, 20);
		in_name.setPlaceholderText(translatable("gui.abuseReport.type.name"));
		in_name.setTooltip(Tooltip.create(BST.gui_tpsbs_ssps_requirements()));
		in_name.setEnabled(a);
		panel.addChild(in_name, false);
		
		final var btn_submit = new TButtonWidget(in_name.getEndX() + 5, in_name.getY(), 20, 20);
		btn_submit.setIcon(new UITexture(BS_WIDGETS_TEXTURE, new Rectangle(20, 60, 20, 20)));
		btn_submit.setTooltip(Tooltip.create(BST.gui_tpsbs_ssps_requirements()));
		btn_submit.setEnabled(a);
		btn_submit.setOnClick(__ ->
		{
			//do nothing if the input string is blank
			if(StringUtils.isBlank(in_name.getInput())) return;
			
			//open better statistics screen
			cpnh.ifPresent(net ->
			{
				//prepare
				final String input = in_name.getInput().trim();
				
				//handle requests for self, just in case the player for some reason does that
				if(Objects.equals(input, net.getPlayer().getName().getString()))
				{
					MC_CLIENT.setScreen(new BetterStatsScreen(GuiUtils.getCurrentScreenParent()).getAsScreen());
					return;
				}
				
				//handle requests for other players
				final var stats = net.getSessionPlayerStats(input);
				final var bss = new BetterStatsScreen(GuiUtils.getCurrentScreenParent(), stats);
				MC_CLIENT.setScreen(bss.getAsScreen());
			});
		});
		panel.addChild(btn_submit, false);
	}
	
	private final void init_quickShare(StatsInitContext initContext)
	{
		final var panel = initContext.getStatsPanel();
		StatsTabUtils.initGroupLabel(panel, BST.gui_tpsbs_qs());
		initMultilineLabel(panel, BST.gui_tpsbs_qs_description(), 0xffffffff);
		
		initSeparator(panel, 10);
		init_quickShare_upload(initContext);
		initSeparator(panel, 10);
		init_quickShare_download(initContext);
		initSeparator(panel, 10);
		initMultilineLabel(panel, BST.gui_tpsbs_qs_abusenotice(), 0xbbeeddee);
	}
	
	private final void init_quickShare_upload(StatsInitContext initContext)
	{
		final var panel = initContext.getStatsPanel();
		final var n1 = UILayout.nextChildVerticalRect(panel);
		
		final var img_upload = new TTextureElement(n1.x, n1.y, 20, 20, TEX_UPLOAD);
		panel.addChild(img_upload, false);
		
		final var btn_qs = new TButtonWidget(n1.x + 25, n1.y, n1.width - 25, 20);
		btn_qs.setText(BST.gui_tpsbs_qs());
		btn_qs.setTooltip(Tooltip.create(BST.gui_tpsbs_qs_step1()));
		btn_qs.setEnabled(LEGAL_QS_CONSENT);
		btn_qs.setOnClick(__ ->
			MC_CLIENT.setScreen(
				new QuickShareUploadScreen(
						MC_CLIENT.screen,
						initContext.getStatsProvider())
				.getAsScreen()));
		panel.addChild(btn_qs, false);
	}
	
	private final void init_quickShare_download(StatsInitContext initContext)
	{
		final var panel = initContext.getStatsPanel();
		final var n1 = UILayout.nextChildVerticalRect(panel);
		
		final var img_download = new TTextureElement(n1.x, n1.y, 20, 20, TEX_DOWNLOAD);
		panel.addChild(img_download, false);
		
		final var in_qscode = new TTextFieldWidget(n1.x + 25, n1.y, n1.width - 50, 20);
		in_qscode.setPlaceholderText(BST.gui_tpsbs_qs_step2_entrqscode());
		in_qscode.setTooltip(Tooltip.create(BST.gui_tpsbs_qs_step2()));
		in_qscode.setEnabled(LEGAL_QS_CONSENT);
		panel.addChild(in_qscode, false);
		
		final var btn_submit = new TButtonWidget(n1.x + n1.width - 20, n1.y, 20, 20);
		btn_submit.setIcon(new UITexture(BS_WIDGETS_TEXTURE, new Rectangle(20, 60, 20, 20)));
		btn_submit.setTooltip(Tooltip.create(BST.gui_tpsbs_qs_step2()));
		btn_submit.setEnabled(LEGAL_QS_CONSENT);
		btn_submit.setOnClick(__ ->
		{
			final var input = in_qscode.getInput().trim();
			if(StringUtils.isBlank(input)) return;
			MC_CLIENT.setScreen(new QuickShareDownloadScreen(
					GuiUtils.getCurrentScreenParent(),
					MC_CLIENT.screen,
					input
				).getAsScreen());
		});
		panel.addChild(btn_submit, false);
	}
	// ==================================================
	/*private static final void openFullVersionRequestScreen()
	{
		final var fvrs = new FullVersionRequestScreen(GuiUtils.getCurrentScreenParent());
		MC_CLIENT.setScreen(fvrs.getAsScreen());
	}/*/
	// --------------------------------------------------
	private static final void initSeparator(TPanelElement panel, int height)
	{
		final var n1 = UILayout.nextChildVerticalRect(panel);
		panel.addChild(new TBlankElement(n1.x, n1.y, n1.width, height), false);
	}
	// --------------------------------------------------
	private static final void initMultilineLabel(TPanelElement panel, Component label, int color)
	{
		//prepare
		final var tr        = panel.getTextRenderer();
		final var txt_descr = tr.split(label, panel.getWidth() - (panel.getScrollPadding() * 2));
		
		//add lines to the panel
		for(final var line : txt_descr)
		{
			final var n1 = UILayout.nextChildVerticalRect(panel);
			final var lbl = new TBlankElement(n1.x, n1.y + 2, n1.width, tr.lineHeight)
			{
				public final @Override void render(TDrawContext pencil) {
					pencil.drawString(tr, line, getX(), getY(), color, true);
				}
			};
			panel.addChild(lbl, false);
		}
	}
	// ==================================================
}