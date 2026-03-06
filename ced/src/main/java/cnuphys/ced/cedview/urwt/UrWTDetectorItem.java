package cnuphys.ced.cedview.urwt;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.item.ItemList;
import cnuphys.bCNU.item.PolygonItem;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.geometry.urwt.UrWTGeometry;

public class UrWTDetectorItem extends PolygonItem {

	//1-based sector
	private int _sector;

	//1-based layer
	private int _layer;

	//chamber colors
	private static Color _fillColors[] = {
			X11Colors.getX11Color("Dark Blue", 10),
			X11Colors.getX11Color("Dark Green", 10),
			X11Colors.getX11Color("Dark Red", 10),
			X11Colors.getX11Color("coral", 10),

	};

	/**
	 * Create a chamber outline
	 *
	 * @param itemList the item list
	 * @param points   the points of the chamber
	 * @param sector   the sector
	 * @param chamber  the chamber
	 */
	public UrWTDetectorItem(ItemList itemList, Point2D.Double points[], int sector, int layer) {
		super(itemList, points);
		_sector = sector;
		_layer = layer;
	}

	/**
	 * Create a chamber outline
	 * @param itemList the item list
	 * @param sector the sector [1..6]
	 * @param layer the layer [1..4]
	 * @return the layer outline item
	 */
	public static  UrWTDetectorItem createUrWELLChamberItem(ItemList itemList, int sector, int layer) {
		Point2D.Double points[] = new Point2D.Double[4];

		int layerm1 = layer-1;
		points[0] = new Point2D.Double(UrWTGeometry.minX[layerm1], UrWTGeometry.maxY[layerm1]);
		points[1] = new Point2D.Double(UrWTGeometry.maxX[layerm1], UrWTGeometry.maxY[layerm1]);
		points[2] = new Point2D.Double(UrWTGeometry.maxX[layerm1], UrWTGeometry.minY[layerm1]);
		points[3] = new Point2D.Double(UrWTGeometry.minX[layerm1], UrWTGeometry.minY[layerm1]);

		//rotate if not sector 1

		if (sector > 1) {
			double midPhi = (Math.PI * (sector - 1)) / 3;
			for (int i = 0; i < 4; i++) {
				rotatePoint(points[i], midPhi);
			}
		}


		UrWTDetectorItem item = new UrWTDetectorItem(itemList, points, sector, layer);
		item.getStyle().setFillColor(_fillColors[layerm1]);
		return item;
	}

	/**
	 * Rotate a point around the z axis
	 *
	 * @param wp  the point being rotated
	 * @param phi rotation angle in radians
	 */
	private static void rotatePoint(Point2D.Double wp, double phi) {
		double cosPhi = Math.cos(phi);
		double sinPhi = Math.sin(phi);
		double x = cosPhi * wp.x + -sinPhi * wp.y;
		double y = sinPhi * wp.x + cosPhi * wp.y;
		wp.setLocation(x, y);
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
			String sectorStr = "$yellow$sector " + _sector;
			String layerStr = "$yellow$layer " + _layer;
			feedbackStrings.add(sectorStr);
			feedbackStrings.add(layerStr);
		}
	}

}
