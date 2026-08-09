package cnuphys.ced.common;

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
import cnuphys.ced.alldata.DCCrosses;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.clasio.ClasIoEventManager;

public class CrossDrawer extends CedViewDrawer {

	public static final int HB = DataDrawSupport.HB_CROSS;
	public static final int TB = DataDrawSupport.TB_CROSS;
	public static final int AIHB = DataDrawSupport.AIHB_CROSS;
	public static final int AITB = DataDrawSupport.AITB_CROSS;

	public static final int ARROWLEN = 30; // pixels
	public static final Stroke THICKLINE = new BasicStroke(1.5f);


	//data containers
	private DCCrosses _hbCrossData = DCCrosses.hitBased();
	private DCCrosses _tbCrossData = DCCrosses.timeBased();
	private DCCrosses _hbAICrossData = DCCrosses.aiHitBased();
	private DCCrosses _tbAICrossData = DCCrosses.aiTimeBased();


	// feedback string color
	public static String fbcolors[] = { "$wheat$", "$misty rose$", "$light pink$", "$khaki$" };

	protected int _mode = HB;

	protected double tiltedx[];
	protected double tiltedy[];
	protected double tiltedz[];
	protected int sector[];
	protected double unitx[];
	protected double unity[];
	protected double unitz[];

	public CrossDrawer(CedView view) {
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
		Point2D.Double wp = new Point2D.Double();

		for (int i = 0; i < crossData.count(); i++) {
			result[0] = crossData.x(i);
			result[1] = crossData.y(i);
			result[2] = crossData.z(i);
			_view.tiltedToSector(result, result);
			_view.sectorToWorld(_view.getProjectionPlane(), wp, result, crossData.sector(i));

			// right sector?
			int mySector = _view.getSector(container, null, wp);
			if (mySector == crossData.sector(i)) {
				container.worldToLocal(pp, wp);
				crossData.setLocation(i, pp);

				// arrows
				Point2D.Double wp2 = new Point2D.Double();
				Point pp2 = new Point();

				int pixlen = ARROWLEN;
				double r = pixlen / WorldGraphicsUtilities.getMeanPixelDensity(container);

				result[0] = crossData.x(i) + r * crossData.directionX(i);
				result[1] = crossData.y(i) + r * crossData.directionY(i);
				result[2] = crossData.z(i) + r * crossData.directionZ(i);
				_view.tiltedToSector(result, result);

				_view.sectorToWorld(_view.getProjectionPlane(), wp2, result, crossData.sector(i));
				container.worldToLocal(pp2, wp2);

				g.setColor(Color.orange);
				g.drawLine(pp.x + 1, pp.y, pp2.x + 1, pp2.y);
				g.drawLine(pp.x, pp.y + 1, pp2.x, pp2.y + 1);
				g.setColor(Color.darkGray);
				g.drawLine(pp.x, pp.y, pp2.x, pp2.y);

				// the circles and crosses
				DataDrawSupport.drawCross(g2, pp.x, pp.y, _mode);
			} // sector match;
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
	public void vdrawFeedback(IContainer container, Point screenPoint, Point2D.Double worldPoint,
			List<String> feedbackStrings, int option) {

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

}
