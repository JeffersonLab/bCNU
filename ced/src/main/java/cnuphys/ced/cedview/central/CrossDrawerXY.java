package cnuphys.ced.cedview.central;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.util.List;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.ced.alldata.DataDrawSupport;
import cnuphys.ced.alldata.CentralCrosses;
import cnuphys.ced.cedview.alert.AlertXYView;
import cnuphys.ced.clasio.ClasIoEventManager;

public class CrossDrawerXY extends CentralXYViewDrawer {

	private static final int ARROWLEN = 30; // pixels
	private static final Stroke THICKLINE = new BasicStroke(1.5f);

	// data containers
	private CentralCrosses bstCrossData = CentralCrosses.bst();
	private CentralCrosses bmtCrossData = CentralCrosses.bmt();

	/**
	 * Create a central cross drawer
	 *
	 * @param view the CedView
	 */
	public CrossDrawerXY(CentralXYView view) {
		super(view);
	}

	@Override
	public void draw(Graphics g, IContainer container) {

		if (ClasIoEventManager.getInstance().isAccumulating() || !_view.isSingleEventMode()) {
			return;
		}

		Graphics2D g2 = (Graphics2D) g;
		Shape oldClip = g2.getClip();
		// clip the active area
		Rectangle sr = container.getInsetRectangle();
		g2.clipRect(sr.x, sr.y, sr.width, sr.height);

		Stroke oldStroke = g2.getStroke();
		g2.setStroke(THICKLINE);

		drawBSTCrosses(g, container);
		drawBMTCrosses(g, container);

		g2.setStroke(oldStroke);

		g2.setClip(oldClip);
	}

	/**
	 * Draw BST crosses
	 *
	 * @param g         the graphics context
	 * @param container the drawing container
	 */
	public void drawBSTCrosses(Graphics g, IContainer container) {
		
		if (_view instanceof AlertXYView) {
			return;
		}

		int count = bstCrossData.count();
		if (count > 0) {
			Point2D.Double wp = new Point2D.Double();
			Point pp = new Point();
			Point2D.Double wp2 = new Point2D.Double();
			Point pp2 = new Point();

			int pixlen = ARROWLEN;
			double r = pixlen / WorldGraphicsUtilities.getMeanPixelDensity(container);

			for (int i = 0; i < count; i++) {
				wp.setLocation(10.0 * bstCrossData.x(i), 10.0 * bstCrossData.y(i));

				if (!bstCrossData.isXYLocationBad(i)) {
					container.worldToLocal(pp, wp);

					if (!bstCrossData.isDirectionBad(i)) {
						wp2.x = wp.x + r * bstCrossData.ux(i);
						wp2.y = wp.y + r * bstCrossData.uy(i);
						container.worldToLocal(pp2, wp2);

						g.setColor(Color.orange);
						g.drawLine(pp.x + 1, pp.y, pp2.x + 1, pp2.y);
						g.drawLine(pp.x, pp.y + 1, pp2.x, pp2.y + 1);
						g.setColor(Color.darkGray);
						g.drawLine(pp.x, pp.y, pp2.x, pp2.y);
					} // bad direction

					// the circles and crosses
					DataDrawSupport.drawCross(g, pp.x, pp.y, DataDrawSupport.BMT_CROSS);
					bstCrossData.setLocation(i, pp);
				} // bad location
			}
		}

	}

	/**
	 * Draw the BMT Crosses
	 *
	 * @param g
	 * @param container
	 */
	public void drawBMTCrosses(Graphics g, IContainer container) {
		if (_view instanceof AlertXYView) {
			return;
		}

		int count = bmtCrossData.count();
		if (count > 0) {
			Point2D.Double wp = new Point2D.Double();
			Point pp = new Point();
			Point2D.Double wp2 = new Point2D.Double();
			Point pp2 = new Point();

			int pixlen = ARROWLEN;
			double r = pixlen / WorldGraphicsUtilities.getMeanPixelDensity(container);

			for (int i = 0; i < count; i++) {
				wp.setLocation(10.0 * bmtCrossData.x(i), 10.0 * bmtCrossData.y(i));

				if (!bmtCrossData.isXYLocationBad(i)) {
					container.worldToLocal(pp, wp);

					if (!bmtCrossData.isDirectionBad(i)) {
						wp2.x = wp.x + r * bmtCrossData.ux(i);
						wp2.y = wp.y + r * bmtCrossData.uy(i);
						container.worldToLocal(pp2, wp2);

						g.setColor(Color.orange);
						g.drawLine(pp.x + 1, pp.y, pp2.x + 1, pp2.y);
						g.drawLine(pp.x, pp.y + 1, pp2.x, pp2.y + 1);
						g.setColor(Color.darkGray);
						g.drawLine(pp.x, pp.y, pp2.x, pp2.y);
					} // bad direction

					// the circles and crosses
					DataDrawSupport.drawCross(g, pp.x, pp.y, DataDrawSupport.BMT_CROSS);
					bmtCrossData.setLocation(i, pp);
				} // bad location
			}
		}
	}


	/**
	 * Use what was drawn to generate feedback strings
	 *
	 * @param container       the drawing container
	 * @param screenPoint     the mouse location
	 * @param worldPoint      the corresponding world location
	 * @param feedbackStrings add strings to this collection
	 */
	@Override
	public void feedback(IContainer container, Point screenPoint, Point2D.Double worldPoint,
			List<String> feedbackStrings) {
		
		if (_view instanceof AlertXYView) {
			return;
		}


		// bst crosses?
		if (_view.showCrosses()) {

			for (int i = 0; i < bstCrossData.count(); i++) {
				if (bstCrossData.contains(i, screenPoint)) {
					bstCrossData.addFeedback(i, feedbackStrings);
					return;
				}
			}
		}


		// bmt crosses?
		if (_view.showCrosses()) {

			for (int i = 0; i < bmtCrossData.count(); i++) {
				if (bmtCrossData.contains(i, screenPoint)) {
					bmtCrossData.addFeedback(i, feedbackStrings);
					return;
				}
			}
		}
	}
}
