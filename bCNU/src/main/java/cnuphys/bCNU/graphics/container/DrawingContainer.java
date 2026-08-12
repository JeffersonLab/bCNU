package cnuphys.bCNU.graphics.container;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;

import cnuphys.bCNU.item.AItem;
import cnuphys.bCNU.item.ItemList;
import cnuphys.bCNU.item.LineItem;
import cnuphys.bCNU.item.PolygonItem;
import cnuphys.bCNU.item.RectangleItem;
import cnuphys.bCNU.view.BaseView;

public class DrawingContainer extends BaseContainer {

	public DrawingContainer(BaseView view, Double worldSystem) {
		super(view, worldSystem);
	}

	/**
	 * From a given screen rectangle, create a rectangle item.
	 *
	 * @param itemList the list to put the item on
	 * @param b     the screen rectangle, probably from rubber banding.
	 * @return the new item
	 */
	public AItem createRectangleItem(ItemList itemList, Rectangle b) {
		Rectangle2D.Double wr = new Rectangle2D.Double();
		localToWorld(b, wr);
		return new RectangleItem(itemList, wr);
	}

	/**
	 * From two given screen points, create a line item
	 *
	 * @param itemList the list to put the item on
	 * @param p0    one screen point, probably from rubber banding.
	 * @param p1    another screen point, probably from rubber banding.
	 * @return the new item
	 */
	public AItem createLineItem(ItemList itemList, Point p0, Point p1) {
		Point2D.Double wp0 = new Point2D.Double();
		Point2D.Double wp1 = new Point2D.Double();
		localToWorld(p0, wp0);
		localToWorld(p1, wp1);
		return new LineItem(itemList, wp0, wp1);
	}

	/**
	 * From a given screen polygon, create a polygon item.
	 *
	 * @param itemList the list to put the item on
	 * @param pp    the screen polygon, probably from rubber banding.
	 * @return the new item
	 */
	public AItem createPolygonItem(ItemList itemList, Point pp[]) {
		if ((pp == null) || (pp.length < 3)) {
			return null;
		}
		Point2D.Double wp[] = new Point2D.Double[pp.length];
		for (int index = 0; index < pp.length; index++) {
			wp[index] = new Point2D.Double();
			localToWorld(pp[index], wp[index]);
		}

		return new PolygonItem(itemList, wp);
	}

}
