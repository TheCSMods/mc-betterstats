package io.github.thecsdev.betterstats.client.gui.panel;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;

import java.util.Objects;

import io.github.thecsdev.betterstats.api.client.gui.panel.BSComponentPanel;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TLabelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TRefreshablePanelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.widget.TButtonWidget;
import io.github.thecsdev.tcdcommons.api.util.enumerations.HorizontalAlignment;

/**
 * A {@link TRefreshablePanelElement} featuring a label that shows the
 * "current page", as well as buttons that allow the user to change "pages".
 */
public final class PageChooserPanel extends BSComponentPanel
{
	// ==================================================
	private final PageChooserPanelProxy proxy;
	// ==================================================
	public PageChooserPanel(int x, int y, int width, PageChooserPanelProxy proxy)
	{
		super(x, y, width, 22);
		this.proxy = Objects.requireNonNull(proxy);
	}
	// --------------------------------------------------
	/**
	 * Returns the {@link PageChooserPanelProxy} associated with this object.
	 */
	public PageChooserPanelProxy getProxy() { return this.proxy; }
	// ==================================================
	protected final @Override void init()
	{
		//get page info
		final int maxPages = Math.max(this.proxy.getPageCount(), 1);
		final int page     = Math.min(Math.max(this.proxy.getPage(), 1), maxPages);
		
		//init label
		final var lbl = new TLabelElement(0, 0, this.width, this.height);
		lbl.setText(literal(page + " / " + maxPages));
		lbl.setTextHorizontalAlignment(HorizontalAlignment.CENTER);
		addChild(lbl, true);
		
		//init left button
		final var btn_left = new TButtonWidget(1, 1, 20, 20, literal("<"));
		btn_left.setOnClick(__ -> nav(-1));
		btn_left.setEnabled(page > 1);
		addChild(btn_left, true);
		
		//init right button
		final var btn_right = new TButtonWidget(this.width - 21, 1, 20, 20, literal(">"));
		btn_right.setOnClick(__ -> nav(1));
		btn_right.setEnabled(page < maxPages);
		addChild(btn_right, true);
	}
	// --------------------------------------------------
	private final void nav(int num)
	{
		//get page info + number
		final int maxPages = Math.max(this.proxy.getPageCount(), 1);
		final int page     = Math.min(Math.max(this.proxy.getPage() + num, 1), maxPages);
		
		//navigate, and refresh if needed
		this.proxy.setPage(page);
		if(getParent() != null) refresh();
	}
	// ==================================================
	/**
	 * A component used by {@link PageChooserPanel} that relays the
	 * {@link PageChooserPanel}'s current state to the {@link PageChooserPanel}.
	 */
	public static interface PageChooserPanelProxy
	{
		public int getPage();
		public int getPageCount();
		public void setPage(int page);
	}
	// ==================================================
}