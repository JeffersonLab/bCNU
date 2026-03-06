package cnuphys.ced.cedview.urwt;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.item.ItemList;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.item.HexSectorItem;

public class UrWELLHexSectorItem extends HexSectorItem {

	private UrWTXYView _view;

	/**
	 * Get a hex sector item
	 *
	 * @param itemList the item list
	 * @param sector   the 1-based sector
	 */
	public UrWELLHexSectorItem(ItemList itemList, UrWTXYView view, int sector) {
		super(itemList, view, sector);
		_view = view;
	}

	/**
	 * Custom drawer for the item.
	 *
	 * @param g         the graphics context.
	 * @param container the graphical container being rendered.
	 */
	@Override
	public void drawItem(Graphics g, IContainer container) {
		if (ClasIoEventManager.getInstance().isAccumulating()) {
			return;
		}

		super.drawItem(g, container);

	}

	@Override
	public void getFeedbackStrings(IContainer container, Point pp, Point2D.Double wp, List<String> feedbackStrings) {

		if (contains(container, pp)) {

			// have no z info, just lab x, y, phy
			double labRho = Math.hypot(wp.x, wp.y);
			double labPhi = Math.atan2(wp.y, wp.x);

			String labXY = String.format("$yellow$lab xy (%-6.2f, %-6.2f) ", wp.x, wp.y);

			String labRhoPhi = String.format("$yellow$lab " + CedView.rhoPhi + " (%-6.2f, %-6.2f)", labRho,
					(Math.toDegrees(labPhi)));

			Point2D.Double sect2D = new Point2D.Double();
			worldToSector2D(sect2D, wp);
			double sectRho = Math.hypot(sect2D.x, sect2D.y);
			double sectPhi = Math.atan2(sect2D.y, sect2D.x);

			String sectXY = String.format("$orange$sector xy (%-6.2f, %-6.2f) ", sect2D.x, sect2D.y);

			String sectRhoPhi = String.format("$orange$sector " + CedView.rhoPhi + " (%-6.2f, %-6.2f)", sectRho,
					(Math.toDegrees(sectPhi)));

			feedbackStrings.add(labXY);
			feedbackStrings.add(labRhoPhi);
			feedbackStrings.add(sectXY);
			feedbackStrings.add(sectRhoPhi);
		} // end contains

	}
}
