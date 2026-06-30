package cnuphys.ced.cedview.central;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.util.List;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.ced.alldata.datacontainer.cnd.CNDADCData;
import cnuphys.ced.alldata.datacontainer.cnd.CNDTDCData;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.cedview.CedXYView;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.geometry.CNDGeometry;

/**
 * A screen polygon representing one CND paddle in the central XY view.
 * <p>
 * The polygon is constructed from the explicit CND corner geometry supplied by
 * {@link CNDGeometry#paddleXYCorners(int, int, Point2D.Double[])}. It
 * deliberately does not hold or use a JLab {@code ScintillatorPaddle}; this
 * allows the view to work whether CND geometry was initialized from CCDB or
 * restored from the lightweight geometry cache.
 */
@SuppressWarnings("serial")
public class CNDXYPolygon extends Polygon {

	/** Number of XY vertices used to draw the paddle footprint. */
	private static final int XY_VERTEX_COUNT = 4;

	// work points in world coordinates
	private final Point2D.Double wp[] = new Point2D.Double[XY_VERTEX_COUNT];

	// reusable screen point
	private final Point pp = new Point();

	// data containers
	private final CNDADCData adcData = CNDADCData.getInstance();
	private final CNDTDCData tdcData = CNDTDCData.getInstance();

	/**
	 * The CND layer, 1..3.
	 */
	public int layer;

	/**
	 * The geometry paddle id, 1..48.
	 */
	public int paddleId;

	private static final Color _navy = X11Colors.getX11Color("navy");
	private static final Color _powder = X11Colors.getX11Color("powder blue");

	private static Font _font = Fonts.hugeFont;

	// "REAL" numbering
	int sector; // 1..24
	int _leftRight; // 1..2

	/**
	 * Create an XY polygon for one CND paddle.
	 *
	 * @param layer    the CND layer, 1..3
	 * @param paddleId the geometry paddle id, 1..48
	 */
	public CNDXYPolygon(int layer, int paddleId) {
		this.layer = layer;
		this.paddleId = paddleId;

		for (int i = 0; i < XY_VERTEX_COUNT; i++) {
			wp[i] = new Point2D.Double();
		}

		int real[] = new int[3];
		int geo[] = { 1, layer, paddleId };
		CNDGeometry.geoTripletToRealTriplet(geo, real);

		sector = real[0];
		_leftRight = real[2];
	}

	/**
	 * Draw the polygon using the default central-view colors.
	 *
	 * @param g         the graphics object
	 * @param container the drawing container
	 */
	public void draw(Graphics g, IContainer container) {
		draw(g, container, CedXYView.LIGHT, Color.black);
	}

	/**
	 * Draw the polygon.
	 *
	 * @param g         the graphics object
	 * @param container the drawing container
	 * @param fillColor the fill color, or {@code null} for no fill
	 * @param lineColor the outline color
	 */
	public void draw(Graphics g, IContainer container, Color fillColor, Color lineColor) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		reset();

		// CNDGeometry returns corners in cm. The central XY view uses mm, so
		// convert the work points before projecting to local screen coordinates.
		CNDGeometry.paddleXYCorners(layer, paddleId, wp);

		for (int i = 0; i < XY_VERTEX_COUNT; i++) {
			wp[i].x *= 10.0;
			wp[i].y *= 10.0;

			container.worldToLocal(pp, wp[i]);
			addPoint(pp.x, pp.y);
		}

		if (fillColor != null) {
			g.setColor(fillColor);
			g.fillPolygon(this);
		}

		g.setColor(lineColor);
		g.drawPolygon(this);

		if ((_leftRight == 1) && (layer == 2)) {
			Point2D.Double centroid = WorldGraphicsUtilities.getCentroid(wp);
			container.worldToLocal(pp, centroid);
			g.setColor(_powder);
			g.setFont(_font);
			g.drawString("" + sector, pp.x - 6, pp.y + 6);
			g.setColor(_navy);
			g.drawString("" + sector, pp.x - 5, pp.y + 7);
		}
	}

	/**
	 * Get feedback strings for this CND polygon.
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

		fbString("cyan", "CND sect " + sector + " layer " + layer + (_leftRight == 1 ? " [left]" : " [right]"),
				feedbackStrings);

		CedView view = (CedView) (container.getView());

		if (view.isSingleEventMode()) {

			for (int i = 0; i < adcData.count(); i++) {
				if ((adcData.sector[i] == sector) && (adcData.layer[i] == layer)
						&& (adcData.order[i] == (_leftRight - 1))) {

					adcData.adcFeedback("CND", i, feedbackStrings);
					break;
				}
			}

			for (int i = 0; i < tdcData.count(); i++) {
				if ((tdcData.sector[i] == sector) && (tdcData.layer[i] == layer)
						&& (tdcData.order[i] == (_leftRight + 1))) {
					feedbackStrings.add(String.format("$cyan$CND tdc %d", tdcData.tdc[i]));
					break;
				}
			}

		} else { // accumulated

			int[][][] cndAccumData = AccumulationManager.getInstance().getAccumulatedCNDData();
			int count = cndAccumData[sector - 1][layer - 1][_leftRight - 1];
			fbString("cyan", "accumulated count " + count, feedbackStrings);
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