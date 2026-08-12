package cnuphys.ced.cedview;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;

import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.util.Fonts;
import edu.cnu.mdi.ui.colors.X11Colors;

@SuppressWarnings("serial")
public abstract class HexView extends CedView {

	private static final Font labelFont = Fonts.commonFont(Font.PLAIN, 11);
	private static final Color TRANS = new Color(192, 192, 192, 128);

	// font for sector number label text
	protected static final Color TRANSTEXT = new Color(64, 64, 192, 40);
	protected static final Font _font = Fonts.commonFont(Font.BOLD, 48);
	protected static final Color TRANSTEXT2 = new Color(0, 0, 0, 30);

	protected static final Color _lineColor = X11Colors.getX11Color("dark red");


	/**
	 * Create a hex view that lays items out in six sectors NOTE: In Hex views, the
	 * world system should be the same as the 2D (xy) lab system
	 *
	 * @param title the title of the view
	 */
	public HexView(Object... keyVals) {
		super(keyVals);
		addControls();
		addItems();
		pack();
	}

	// add items to the view
	protected abstract void addItems();

	// add the control panel
	protected abstract void addControls();

	/**
	 * Get the 1-based sector.
	 *
	 * @return the 1-based sector
	 */
	@Override
	public int getSector(IContainer container, Point screenPoint, Point2D.Double worldPoint) {
		return getSector(worldPoint);
	}

	/**
	 * Get the 1-based sector.
	 *
	 * @return the 1-based sector
	 */
	public int getSector(Point2D.Double worldPoint) {
		double phi = getPhi(worldPoint);

		if ((phi > 30.0) && (phi <= 90.0)) {
			return 2;
		} else if ((phi > 90.0) && (phi <= 150.0)) {
			return 3;
		} else if ((phi > 150.0) && (phi <= 210.0)) {
			return 4;
		} else if ((phi > 210.0) && (phi <= 270.0)) {
			return 5;
		} else if ((phi > 270.0) && (phi <= 330.0)) {
			return 6;
		} else {
			return 1;
		}
	}

	/**
	 * Get the azimuthal angle
	 *
	 * @param worldPoint the world point
	 * @return the value of phi in degrees.
	 */
	public double getPhi(Point2D.Double worldPoint) {
		double phi = Math.toDegrees(Math.atan2(worldPoint.y, worldPoint.x));
		if (phi < 0) {
			phi += 360.0;
		}
		return phi;
	}

	/**
	 * From detector xyz get the projected world point.
	 *
	 * @param x  the detector x coordinate
	 * @param y  the detector y coordinate
	 * @param z  the detector z coordinate
	 * @param wp the projected 2D world point.
	 */
	@Override
	public void projectClasToWorld(double x, double y, double z, Plane3D projectionPlane, Point2D.Double wp) {

		projectedPoint(x, y, z, projectionPlane, wp);
	}

	/**
	 * Project a space point. Projected by finding the closest point on the plane.
	 *
	 * @param x  the x coordinate
	 * @param y  the y coordinate
	 * @param z  the z coordinate
	 * @param wp will hold the projected 2D world point
	 * @return the projected 3D space point
	 */
	@Override
	public Point3D projectedPoint(double x, double y, double z, Plane3D projectionPlane, Point2D.Double wp) {
		Point3D p1 = new Point3D(x, y, z);
		Vector3D normal = projectionPlane.normal();
		Point3D p2 = new Point3D(p1.x() + normal.x(), p1.y() + normal.y(), p1.z() + normal.z());
		Line3D perp = new Line3D(p1, p2);
		Point3D pisect = new Point3D();
		projectionPlane.intersection(perp, pisect);

		wp.x = pisect.x();
		wp.y = pisect.y();
		return pisect;
	}

	/**
	 * Draw an xy coordinate system  the graphics context
	 * @param container the container
	 */
	protected void drawCoordinateSystem(Graphics g, IContainer container, Color lColor) {
		// draw coordinate system
		Component component = container.getComponent();
		Rectangle sr = component.getBounds();

		int left = 25;
		int right = left + 50;
		int bottom = sr.height - 20;
		int top = bottom - 50;
		g.setFont(labelFont);
		FontMetrics fm = getFontMetrics(labelFont);

		Rectangle r = new Rectangle(left - fm.stringWidth("x") - 4, top - fm.getHeight() / 2 + 1,
				(right - left + fm.stringWidth("x") + fm.stringWidth("y") + 9), (bottom - top) + fm.getHeight() + 2);

		g.setColor(TRANS);
		g.fillRect(r.x, r.y, r.width, r.height);

		g.setColor((lColor != null) ? lColor : _lineColor);
		g.drawLine(left, bottom, right, bottom);
		g.drawLine(right, bottom, right, top);

		g.drawString("y", right + 3, top + fm.getHeight() / 2 - 1);
		g.drawString("x", left - fm.stringWidth("x") - 2, bottom + fm.getHeight() / 2);

	}

	// draw the sector numbers
	protected void drawSectorNumbers(Graphics g, IContainer container, Color color, double x) {
		double r3over2 = Math.sqrt(3) / 2;

		double y = 0;
		FontMetrics fm = getFontMetrics(_font);
		g.setFont(_font);
		g.setColor((color != null) ? color :  TRANSTEXT);
		Point pp = new Point();

		for (int sect = 1; sect <= 6; sect++) {
			container.worldToLocal(pp, x, y);

			String s = "" + sect;
			int sw = fm.stringWidth(s);

			g.drawString(s, pp.x - sw / 2, pp.y + fm.getHeight() / 2);

			if (sect != 6) {
				double tx = x;
				double ty = y;
				x = 0.5 * tx - r3over2 * ty;
				y = r3over2 * tx + 0.5 * ty;
			}
		}
	}



}
