package io.github.thecsdev.betterstats.api.client.gui.screen;

import static io.github.thecsdev.betterstats.BetterStats.getModID;
import static io.github.thecsdev.betterstats.client.BetterStatsClient.MC_CLIENT;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.api.client.gui.widget.ScrollBarWidget;
import io.github.thecsdev.betterstats.client.gui.stats.tabs.BSConfigTab;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TFillColorElement;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TLabelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.screen.TScreen;
import io.github.thecsdev.tcdcommons.api.client.gui.screen.TScreenPlus;
import io.github.thecsdev.tcdcommons.api.client.gui.util.TDrawContext;
import io.github.thecsdev.tcdcommons.api.client.gui.widget.TButtonWidget;
import io.github.thecsdev.tcdcommons.api.client.util.interfaces.IParentScreenProvider;
import io.github.thecsdev.tcdcommons.api.util.enumerations.HorizontalAlignment;
import io.github.thecsdev.tcdcommons.client.TCDCommonsClient;
import net.minecraft.client.gui.screens.Screen;

/**
 * A config {@link TScreen} for the {@link BetterStats} mod.
 */
public final class BetterStatsConfigScreen extends TScreenPlus implements IParentScreenProvider
{
	// ==================================================
	private final @Nullable Screen parent;
	// ==================================================
	public BetterStatsConfigScreen(@Nullable Screen parent)
	{
		super(translatable(getModID()).append(" - ").append(translatable("options.title")));
		this.parent = parent;
	}
	// --------------------------------------------------
	public final @Override Screen getParentScreen() { return this.parent; }
	// --------------------------------------------------
	public final @Override void close() { MC_CLIENT.setScreen(this.parent); }
	public final @Override void renderBackground(TDrawContext pencil)
	{
		if(this.parent != null) this.parent.render(pencil, pencil.mouseX, pencil.mouseY, pencil.deltaTime);
		else super.renderBackground(pencil);
	}
	public final @Override boolean shouldRenderInGameHud() { return false; }
	// ==================================================
	protected final @Override void init()
	{
		//the "background" "content pane"
		final var contentPane = new TFillColorElement(0, 0, getWidth(), getHeight());
		contentPane.setColor(0x44FFFFFF);
		contentPane.setZOffset(TCDCommonsClient.MAGIC_ITEM_Z_OFFSET);
		addChild(contentPane, false);
		
		//mathematical calculations
		final int panelW = (int) ((float)getWidth() / 2f);
		final int panelX = (getWidth() / 2) - (panelW / 2);
		final int panelH = getHeight() - 40;
		final int panelY = (getHeight() / 2) - (panelH / 2);
		
		//a bunch of panels
		final var panel = new TPanelElement(panelX, panelY, panelW, panelH);
		panel.setScrollFlags(0);
		panel.setScrollPadding(0);
		panel.setBackgroundColor(0xFF555555);
		panel.setOutlineColor(-16777216);
		contentPane.addChild(panel, false);
		
		final var panel_title = new TPanelElement(0, 0, panelW, 20);
		panel_title.setScrollFlags(0);
		panel_title.setScrollPadding(0);
		panel_title.setOutlineColor(panel.getOutlineColor());
		panel.addChild(panel_title, true);
		
		final var lbl_title = new TLabelElement(0, 0, panel_title.getWidth(), panel_title.getHeight());
		lbl_title.setText(translatable(getModID()));
		lbl_title.setTextHorizontalAlignment(HorizontalAlignment.CENTER);
		panel_title.addChild(lbl_title, true);
		
		final var panel_config = new TPanelElement(0, 20, panelW - 8, panel.getHeight() - (20 + 30));
		panel_config.setScrollFlags(TPanelElement.SCROLL_VERTICAL);
		panel_config.setScrollPadding(10);
		panel_config.setBackgroundColor(0);
		panel_config.setOutlineColor(0);
		panel.addChild(panel_config, true);
		
		final var scroll_config = new ScrollBarWidget(
				panel_config.getEndX(), panel_config.getY(),
				8, panel_config.getHeight(),
				panel_config);
		panel.addChild(scroll_config, false);
		
		final var panel_action = new TPanelElement(panel_config.getX(), panel_config.getEndY(), panelW, 30);
		panel_action.setScrollFlags(0);
		panel_action.setScrollPadding(0);
		panel_action.setBackgroundColor(panel_title.getBackgroundColor());
		panel_action.setOutlineColor(panel.getOutlineColor());
		panel.addChild(panel_action, false);
		
		//the config gui
		final var config_builder = BSConfigTab.initConfigGui(panel_config);
		
		//cancel and done buttons
		final var btn_actionCancel = new TButtonWidget(
				5, 5, (panelW / 2) - 7, 20,
				translatable("gui.cancel"),
				__ -> close());
		final var btn_actionDone = new TButtonWidget(
				panel_action.getEndX() - btn_actionCancel.getWidth() - 5, panel_action.getY() + 5,
				btn_actionCancel.getWidth(), 20,
				translatable("gui.done"),
				__ -> { config_builder.saveChanges(); close(); });
		panel_action.addChild(btn_actionCancel, true);
		panel_action.addChild(btn_actionDone, false);
	}
	// ==================================================
}