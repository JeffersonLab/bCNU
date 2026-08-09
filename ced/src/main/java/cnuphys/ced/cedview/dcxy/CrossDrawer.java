package cnuphys.ced.cedview.dcxy;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.util.List;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.ced.alldata.DataDrawSupport;
import cnuphys.ced.alldata.DCCrosses;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.item.HexSectorItem;

public class CrossDrawer extends DCXYViewDrawer {

	public static final int HB = cnuphys.ced.common.CrossDrawer.HB;
	public static final int TB = cnuphys.ced.common.CrossDrawer.TB;
	public static final int AIHB = cnuphys.ced.common.CrossDrawer.AIHB;
	public static final int AITB = cnuphys.ced.common.CrossDrawer.AITB;


	private static final int ARROWLEN = cnuphys.ced.common.CrossDrawer.ARROWLEN;
	private static final Stroke THICKLINE = cnuphys.ced.common.CrossDrawer.THICKLINE;

	//data containers
	private DCCrosses _hbCrossData = DCCrosses.hitBased();
	private DCCrosses _tbCrossData = DCCrosses.timeBased();
	private DCCrosses _hbAICrossData = DCCrosses.aiHitBased();
	private DCCrosses _tbAICrossData = DCCrosses.aiTimeBased();

	private int _mode = HB;

	/**
	 * A cross drawer for the DCXY view
	 * @param view the view
	 */
	public CrossDrawer(DCXYView view) {
		super(view);
	}

	/**
	 * Set the mode. 0 for hit based, 1 for time based
	 *
	 * @param mode the new mode
	 */
	public void setMode(int mode) {
		_mode = mode;
	}

	@Override
	public void draw(Graphics g, IContainer container) {

		if (ClasIoEventManager.getInstance().isAccumulating() || !_view.isSingleEventMode()) {
			return;
		}

		DCCrosses crossData = null;

		// any crosses?
		if (_mode == HB) {
			crossData = _hbCrossData;
		}
		else if (_mode == TB) {
			crossData = _tbCrossData;
		}
		else if (_mode == AIHB) {
			crossData = _hbAICrossData;
		}
		else if (_mode == AITB) {
			crossData = _tbAICrossData;
		}

		if ((crossData == null) || crossData.count() < 1) {
			return;
		}

		Graphics2D g2 = (Graphics2D) g;

		Stroke oldStroke = g2.getStroke();
		g2.setStroke(THICKLINE);

		double result[] = new double[3];
		Point pp = new Point();

		for (int i = 0; i < crossData.count(); i++) {
			HexSectorItem hsItem = _view.getHexSectorItem(crossData.sector(i));

			if (hsItem == null) {
				System.err.println("null sector item in DCXY Cross Drawer sector: " + crossData.sector(i));
				break;
			}

			result[0] = crossData.x(i);
			result[1] = crossData.y(i);
			result[2] = crossData.z(i);

			_view.tiltedToSector(result, result);

			// only care about xy
			Point2D.Double sp = new Point2D.Double(result[0], result[1]);
			hsItem.sector2DToLocal(container, pp, sp);
			crossData.setLocation(i, pp);

			// arrows
			Point pp2 = new Point();

			int pixlen = ARROWLEN;
			double r = pixlen / WorldGraphicsUtilities.getMeanPixelDensity(container);

			result[0] = crossData.x(i) + r * crossData.directionX(i);
			result[1] = crossData.y(i) + r * crossData.directionY(i);
			result[2] = crossData.z(i) + r * crossData.directionZ(i);

			_view.tiltedToSector(result, result);
	        sp.setLocation(result[0], result[1]);
	        hsItem.sector2DToLocal(container, pp2, sp);

	        g.setColor(Color.orange);
	        g.drawLine(pp.x + 1, pp.y, pp2.x + 1, pp2.y);
	        g.drawLine(pp.x, pp.y + 1, pp2.x, pp2.y + 1);
	        g.setColor(Color.darkGray);
	        g.drawLine(pp.x, pp.y, pp2.x, pp2.y);

	        // the circles and crosses
	        DataDrawSupport.drawCross(g, pp.x, pp.y, _mode);
		}



		g2.setStroke(oldStroke);
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

		DCCrosses crossData = null;

		// any crosses?
		if (_mode == HB) {
			crossData = _hbCrossData;
		}
		else if (_mode == TB) {
			crossData = _tbCrossData;
		}
		else if (_mode == AIHB) {
			crossData = _hbAICrossData;
		}
		else if (_mode == AITB) {
			crossData = _tbAICrossData;
		}

		if ((crossData == null) || crossData.count() < 1) {
			return;
		}

		for (int i = 0; i < crossData.count(); i++) {
			if (crossData.contains(i, screenPoint)) {
				crossData.addFeedback(i, feedbackStrings);
				break;
			}
		}


	}


	class FeedbackRects {
		public Rectangle rects[];
	}
}