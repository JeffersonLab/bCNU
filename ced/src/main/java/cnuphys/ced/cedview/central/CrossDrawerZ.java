package cnuphys.ced.cedview.central;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.util.List;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.ced.alldata.DataDrawSupport;
import cnuphys.ced.alldata.CentralCrosses;
import cnuphys.ced.clasio.ClasIoEventManager;

public class CrossDrawerZ extends CentralZViewDrawer {

	private static final int ARROWLEN = 30; // pixels
	private static final Stroke THICKLINE = new BasicStroke(1.5f);
	private static final Color ERROR = new Color(255, 0, 0, 128);


	// data containers
	private CentralCrosses bstCrossData = CentralCrosses.bst();
	private CentralCrosses bmtCrossData = CentralCrosses.bmt();


	/**
	 * A BST Cross drawer
	 *
	 * @param view the owner vie
	 */
	public CrossDrawerZ(CentralZView view) {
		super(view);
	}

	@Override
	public void draw(Graphics g, IContainer container) {
		if (ClasIoEventManager.getInstance().isAccumulating() || !_view.isSingleEventMode()) {
			return;
		}

		Graphics2D g2 = (Graphics2D) g;

		Stroke oldStroke = g2.getStroke();
		g2.setStroke(THICKLINE);

		drawBSTCrosses(g, container);
		drawBMTCrosses(g, container);
		g2.setStroke(oldStroke);
	}

	/**
	 * Draw the reconstructed crosses on the BST Z view
	 *
	 * @param g         the graphics context
	 * @param container the drawing container
	 */
	public void drawBSTCrosses(Graphics g, IContainer container) {

		int count = bstCrossData.count();
		if (count > 0) {
			Point2D.Double wp = new Point2D.Double();
			Point pp = new Point();
			Point2D.Double wp2 = new Point2D.Double();
			Point pp2 = new Point();
			Point2D.Double wp3 = new Point2D.Double();
			Point2D.Double wp4 = new Point2D.Double();

			// error bars
			for (int i = 0; i < count; i++) {

				// convert to mm
				_view.labToWorld(10*bstCrossData.x(i), 10*bstCrossData.y(i), 10*bstCrossData.z(i), wp);
				wp3.setLocation(wp.x - 10*bstCrossData.errorZ(i), wp.y);
				wp4.setLocation(wp.x + 10*bstCrossData.errorZ(i), wp.y);
				container.worldToLocal(pp, wp3);
				container.worldToLocal(pp2, wp4);
				g.setColor(ERROR);
				g.fillRect(pp.x, pp.y - DataDrawSupport.CROSSHALF, pp2.x - pp.x, 2 * DataDrawSupport.CROSSHALF);
			}

			for (int i = 0; i < count; i++) {
				wp.setLocation(10.0 * bstCrossData.x(i), 10.0 * bstCrossData.y(i));
				if (!bstCrossData.isFullLocationBad(i)) {
					_view.labToWorld(10 * bstCrossData.x(i), 10 * bstCrossData.y(i), 10 * bstCrossData.z(i), wp);

					// arrows
					int pixlen = ARROWLEN;
					double r = pixlen / WorldGraphicsUtilities.getMeanPixelDensity(container);

					double xa = 10 * bstCrossData.x(i) + r * bstCrossData.ux(i);
					double ya = 10 * bstCrossData.y(i) + r * bstCrossData.uy(i);
					double za = 10 * bstCrossData.z(i) + r * bstCrossData.uz(i);
					_view.labToWorld(xa, ya, za, wp2);
					container.worldToLocal(pp, wp);
					container.worldToLocal(pp2, wp2);

					g.setColor(Color.orange);
					g.drawLine(pp.x + 1, pp.y, pp2.x + 1, pp2.y);
					g.drawLine(pp.x, pp.y + 1, pp2.x, pp2.y + 1);
					g.setColor(Color.darkGray);
					g.drawLine(pp.x, pp.y, pp2.x, pp2.y);

					// the circles and crosses
					DataDrawSupport.drawCross(g, pp.x, pp.y, DataDrawSupport.BST_CROSS);
					bstCrossData.setLocation(i, pp);


				}
			}
		}
	}

	public void drawBMTCrosses(Graphics g, IContainer container) {

		int count = bmtCrossData.count();
		if (count > 0) {
			Point2D.Double wp = new Point2D.Double();
			Point pp = new Point();
			Point2D.Double wp2 = new Point2D.Double();
			Point pp2 = new Point();
			Point2D.Double wp3 = new Point2D.Double();
			Point2D.Double wp4 = new Point2D.Double();

			// error bars
			for (int i = 0; i < count; i++) {

				// convert to mm
				_view.labToWorld(10*bmtCrossData.x(i), 10*bmtCrossData.y(i), 10*bmtCrossData.z(i), wp);
				wp3.setLocation(wp.x - 10*bmtCrossData.errorZ(i), wp.y);
				wp4.setLocation(wp.x + 10*bmtCrossData.errorZ(i), wp.y);
				container.worldToLocal(pp, wp3);
				container.worldToLocal(pp2, wp4);
				g.setColor(ERROR);
				g.fillRect(pp.x, pp.y - DataDrawSupport.CROSSHALF, pp2.x - pp.x, 2 * DataDrawSupport.CROSSHALF);
			}

			for (int i = 0; i < count; i++) {
				wp.setLocation(10.0 * bmtCrossData.x(i), 10.0 * bmtCrossData.y(i));
				if (!bmtCrossData.isFullLocationBad(i)) {
					_view.labToWorld(10 * bmtCrossData.x(i), 10 * bmtCrossData.y(i), 10 * bmtCrossData.z(i), wp);

					// arrows
					int pixlen = ARROWLEN;
					double r = pixlen / WorldGraphicsUtilities.getMeanPixelDensity(container);

					double xa = 10 * bmtCrossData.x(i) + r * bmtCrossData.ux(i);
					double ya = 10 * bmtCrossData.y(i) + r * bmtCrossData.uy(i);
					double za = 10 * bmtCrossData.z(i) + r * bmtCrossData.uz(i);
					_view.labToWorld(xa, ya, za, wp2);
					container.worldToLocal(pp, wp);
					container.worldToLocal(pp2, wp2);

					g.setColor(Color.orange);
					g.drawLine(pp.x + 1, pp.y, pp2.x + 1, pp2.y);
					g.drawLine(pp.x, pp.y + 1, pp2.x, pp2.y + 1);
					g.setColor(Color.darkGray);
					g.drawLine(pp.x, pp.y, pp2.x, pp2.y);

					// the circles and crosses
					DataDrawSupport.drawCross(g, pp.x, pp.y, DataDrawSupport.BMT_CROSS);
					bmtCrossData.setLocation(i, pp);


				}
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
