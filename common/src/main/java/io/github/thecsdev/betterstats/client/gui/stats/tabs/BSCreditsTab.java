package io.github.thecsdev.betterstats.client.gui.stats.tabs;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.github.thecsdev.betterstats.BetterStatsProperties;
import io.github.thecsdev.betterstats.api.client.gui.util.StatsTabUtils;
import io.github.thecsdev.betterstats.api.client.registry.StatsTab;
import io.github.thecsdev.betterstats.client.gui.widget.CreditsTabPersonWidget;
import io.github.thecsdev.betterstats.util.BST;
import io.github.thecsdev.tcdcommons.api.client.gui.layout.UILayout;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TLabelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.util.TDrawContext;
import io.github.thecsdev.tcdcommons.api.util.enumerations.HorizontalAlignment;
import io.github.thecsdev.tcdcommons.util.TCDCT;
import io.github.thecsdev.tcdcommons.util.io.http.TcdWebApiPerson;
import net.minecraft.network.chat.Component;

public final class BSCreditsTab extends StatsTab
{
	// ==================================================
	private static final Component TEXT_TITLE            = translatable("credits_and_attribution.button.credits");
	private static final Component TEXT_CONTRIBUTORS     = BST.bss_contributors_title();
	//private static final Text TEXT_SPONSORS       = TCDCT.tcdc_term_ghSponsors();
	private static final Component TEXT_SPECIAL_THANKS   = TCDCT.tcdc_term_specialThanks();
	//private static final Text TEXT_FEATURED       = TCDCT.tcdc_term_featured();
	//private static final Text TEXT_FEATURED_NOONE = TCDCT.tcdc_term_featured_noOne();
	//private static final Text TEXT_FETCH_FAIL     = TCDCT.tcdc_term_fetchFail();
	// ==================================================
	public final @Override Component getName() { return TEXT_TITLE; }
	public final @Override boolean isAvailable() { return false; }
	// --------------------------------------------------
	public final @Override void initFilters(FiltersInitContext initContext) { StatsTabUtils.initDefaultFilters(initContext); }
	// ==================================================
	public final @Override void initStats(final StatsInitContext initContext)
	{
		final var panel = initContext.getStatsPanel();
		
		final var lbl_title = StatsTabUtils.initGroupLabel(panel, TEXT_TITLE);
		lbl_title.setSize(lbl_title.getWidth(), lbl_title.getHeight() * 2);
		lbl_title.setTextColor(TDrawContext.DEFAULT_TEXT_COLOR);
		lbl_title.setTextScale(1.8f);
		lbl_title.setTextHorizontalAlignment(HorizontalAlignment.CENTER);
		
		//init contributor stats
		initGroupLabel(panel, TEXT_CONTRIBUTORS);
		initContributorStats(panel, null);
		initGroupLabel(panel, TEXT_SPECIAL_THANKS);
		initContributorStats(panel, "special_thanks");
	}
	// ==================================================
	private static final void initGroupLabel(TPanelElement panel, Component text)
	{
		final var n1 = UILayout.nextChildVerticalRect(panel);
		final var panel_tGroup = new TPanelElement(n1.x, n1.y, n1.width, 20);
		panel_tGroup.setBackgroundColor(0xff353535);
		panel_tGroup.setOutlineColor(0xff000000);
		panel.addChild(panel_tGroup, false);
		//panel_tGroup.addChild(new TFillColorElement(2, 2, 16, 16, panel_tGroup.getOutlineColor()));
		final var lbl = new TLabelElement(
				panel_tGroup.getHeight() + 5, 0,
				panel_tGroup.getWidth(), panel_tGroup.getHeight(),
				text);
		lbl.setTextHorizontalAlignment(HorizontalAlignment.LEFT);
		lbl.setTextColor(0xffffff00);
		lbl.setTextScale(0.9f);
		panel_tGroup.addChild(lbl);
	}
	// --------------------------------------------------
	private static final void initContributorStats(TPanelElement panel, @Nullable String key)
	{
		final var contributors = getContributors(key);
		boolean highlight = true;
		int iteration = 1;
		
		for(final var contributor : contributors)
		{
			//keep track of the loop iterations
			highlight = !highlight;
			iteration++;
			
			//create gui elements
			final var n1 = UILayout.nextChildVerticalRect(panel);
			final var ctpw = new CreditsTabPersonWidget(n1.x, n1.y, n1.width, contributor, iteration <= 15);
			ctpw.setBackgroundColor(highlight ? 0x44000000 : 0x22000000);
			panel.addChild(ctpw, false);
		}
	}
	// ==================================================
	/**
	 * Returns an array of {@link TcdWebApiPerson}s who are credited as contributors of this mod.
	 */
	public static final TcdWebApiPerson[] getContributors(@Nullable String key)
	{
		//argument check
		if(key == null) key = "contributors";
		
		//obtain the contributors array
		@Nullable JsonArray contributors = null;
		try { contributors = BetterStatsProperties.getModProperties().get(key).getAsJsonArray(); }
		catch(Exception e) { return new TcdWebApiPerson[] {}; }
		
		//return
		return contributors.asList().stream()
			.filter(p -> p.isJsonObject())
			.map(p -> p.getAsJsonObject())
			.map(p -> tryParsePerson(p))
			.filter(p -> p != null)
			.toArray(TcdWebApiPerson[]::new);
	}
	// --------------------------------------------------
	/**
	 * Parses a {@link JsonObject} to a {@link TcdWebApiPerson}.
	 */
	private static final @Nullable TcdWebApiPerson tryParsePerson(JsonObject json) throws NullPointerException
	{
		try { return new TcdWebApiPerson(json); } catch(Exception e) { return null; }
	}
	// ==================================================
}