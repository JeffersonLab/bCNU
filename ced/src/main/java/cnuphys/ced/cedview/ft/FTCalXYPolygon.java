package cnuphys.ced.cedview.ft;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.util.List;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.ced.cedview.CedXYView;
import cnuphys.ced.geometry.FTCALGeometry;

/**
 * A screen polygon representing one FTCAL paddle in the FTCal XY view.
 * <p>
 * This class deliberately does not store or use a JLab
 * {@code ScintillatorPaddle}. The polygon is drawn from the explicit FTCAL
 * corner geometry supplied by
 * {@link FTCALGeometry#paddleXYCorners(int, Point2D.Double[])}. That allows the
 * view to work whether FTCAL geometry was initialized directly from CCDB or
 * restored from the lightweight geometry cache.
 */
@SuppressWarnings("serial")
public class FTCalXYPolygon extends Polygon {

	/** Number of XY vertices used to draw an FTCAL paddle footprint. */
	private static final int XY_VERTEX_COUNT = 4;

	/**
	 * The FTCAL component id.
	 */
	public int paddleId;

	/** Reusable world-coordinate work points. */
	private final Point2D.Double wp[] = new Point2D.Double[XY_VERTEX_COUNT];

	/** Reusable screen-coordinate point. */
	private final Point pp = new Point();

	/**
	 * Create an XY polygon for one FTCAL paddle.
	 *
	 * @param paddleId the FTCAL component id
	 */
	public FTCalXYPolygon(int paddleId) {
		this.paddleId = paddleId;

		for (int i = 0; i < XY_VERTEX_COUNT; i++) {
			wp[i] = new Point2D.Double();
		}
	}

	/**
	 * Draw the polygon.
	 *
	 * @param g         the graphics object
	 * @param container the drawing container
	 */
	public void draw(Graphics g, IContainer container) {
		reset();

		FTCALGeometry.paddleXYCorners(paddleId, wp);

		for (int i = 0; i < XY_VERTEX_COUNT; i++) {
			container.worldToLocal(pp, wp[i]);
			addPoint(pp.x, pp.y);
		}

		g.setColor(CedXYView.LIGHT);
		g.fillPolygon(this);
		g.setColor(Color.black);
		g.drawPolygon(this);
	}

	/**
	 * Get feedback strings for this FTCAL polygon.
	 *
	 * @param container       the drawing container
	 * @param screenPoint     the mouse location in screen coordinates
	 * @param worldPoint      the corresponding world point
	 * @param feedbackStrings where feedback strings are added
	 * @return {@code true} if the point is inside this polygon and feedback was
	 *         added
	 */
	public boolean getFeedbackStrings(IContainer container, Point screenPoint, Point2D.Double worldPoint,
			List<String> feedbackStrings) {

		if (!contains(screenPoint)) {
			return false;
		}

		fbString("red", "Id " + paddleId, feedbackStrings);

		Point p = FTCALGeometry.getXYIndices(paddleId);
		if (p != null) {
			fbString("red", "XY Indices [" + p.x + ", " + p.y + "]", feedbackStrings);
		}

		return true;
	}

	/**
	 * Add a color-tagged feedback string.
	 *
	 * @param color the feedback color name
	 * @param str   the feedback text
	 * @param fbstrs the feedback string list
	 */
	private void fbString(String color, String str, List<String> fbstrs) {
		fbstrs.add("$" + color + "$" + str);
	}

}