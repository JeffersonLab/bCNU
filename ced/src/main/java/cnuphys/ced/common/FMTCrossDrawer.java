package cnuphys.ced.common;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.List;

import org.jlab.io.base.DataBank;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.ced.alldata.DataDrawSupport;
import cnuphys.ced.alldata.DataWarehouse;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.cedview.dcxy.DCXYView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.geometry.GeometryManager;

public class FMTCrossDrawer extends CedViewDrawer {

	private static final int ARROWLEN = 30; // pixels
	private static final Stroke THICKLINE = new BasicStroke(1.5f);
	private static final String FMT_CROSSES_BANK = "FMTRec::Crosses";

	public FMTCrossDrawer(CedView view) {
		super(view);
	}

	private DataBank _drawnCrosses;
	private Point[] _drawnLocations;

	@Override
	public void draw(Graphics g, IContainer container) {

		if (!_view.showFMTCrosses() || ClasIoEventManager.getInstance().isAccumulating() || !_view.isSingleEventMode()) {
			return;
		}

		Graphics2D g2 = (Graphics2D) g;
		Shape oldClip = g2.getClip();
		// clip the active area
		Rectangle sr = container.getInsetRectangle();
		g2.clipRect(sr.x, sr.y, sr.width, sr.height);

		Stroke oldStroke = g2.getStroke();
		g2.setStroke(THICKLINE);

		drawFMTCrosses(g, container);

		g2.setStroke(oldStroke);
		g2.setClip(oldClip);
	}

	/**
	 * Draw FMT crosses
	 *
	 * @param g         the graphics context
	 * @param container the drawing container
	 */
	public void drawFMTCrosses(Graphics g, IContainer container) {
		_drawnCrosses = DataWarehouse.getInstance().getBank(FMT_CROSSES_BANK);
		int count = crossCount();
		_drawnLocations = (count == 0) ? null : new Point[count];

		// treat DCXY view separately
		if (_view instanceof DCXYView) {
			drawFMTCrossesXY(g, container);
			return;
		}

		if (count > 0) {
			Point2D.Double wp = new Point2D.Double();
			Point pp = new Point();
			Point2D.Double wp2 = new Point2D.Double();
			Point pp2 = new Point();
			double result[] = new double[3];

			for (int i = 0; i < count; i++) {

				result[0] = _drawnCrosses.getFloat("x", i);
				result[1] = _drawnCrosses.getFloat("y", i);
				result[2] = _drawnCrosses.getFloat("z", i);

				int crossSector = GeometryManager.labXYZToSectorNumber(result);
				_view.projectClasToWorld(result[0], result[1], result[2], _view.getProjectionPlane(), wp);
				int mySector = _view.getSector(container, null, wp);
				if (mySector == crossSector) {
					container.worldToLocal(pp, wp);
					_drawnLocations[i] = new Point(pp);

					// arrows

					int pixlen = ARROWLEN;
					double r = pixlen / WorldGraphicsUtilities.getMeanPixelDensity(container);

					// lab coordinates of end of arrow
					result[0] = _drawnCrosses.getFloat("x", i) + r * _drawnCrosses.getFloat("ux", i);
					result[1] = _drawnCrosses.getFloat("y", i) + r * _drawnCrosses.getFloat("uy", i);
					result[2] = _drawnCrosses.getFloat("z", i) + r * _drawnCrosses.getFloat("uz", i);
					_view.projectClasToWorld(result[0], result[1], result[2], _view.getProjectionPlane(), wp2);
					container.worldToLocal(pp2, wp2);

					g.setColor(Color.orange);
					g.drawLine(pp.x + 1, pp.y, pp2.x + 1, pp2.y);
					g.drawLine(pp.x, pp.y + 1, pp2.x, pp2.y + 1);
					g.setColor(Color.darkGray);
					g.drawLine(pp.x, pp.y, pp2.x, pp2.y);

					// the circles and crosses
					DataDrawSupport.drawCross(g, pp.x, pp.y, DataDrawSupport.FMT_CROSS);
				}
			}
		}
	}

	/**
	 * Draw FMT crosses
	 *
	 * @param g         the graphics context
	 * @param container the drawing container
	 */
	public void drawFMTCrossesXY(Graphics g, IContainer container) {

		int count = crossCount();
		if (count > 0) {
			Point pp = new Point();
			Point2D.Double wp2 = new Point2D.Double();
			Point pp2 = new Point();

			for (int i = 0; i < count; i++) {
				float x = _drawnCrosses.getFloat("x", i);
				float y = _drawnCrosses.getFloat("y", i);
				container.worldToLocal(pp, x, y);
				_drawnLocations[i] = new Point(pp);

				// arrows

				int pixlen = ARROWLEN;
				double r = pixlen / WorldGraphicsUtilities.getMeanPixelDensity(container);

				// lab coordinates of end of arrow
				wp2.setLocation(x + r * _drawnCrosses.getFloat("ux", i),
						y + r * _drawnCrosses.getFloat("uy", i));
				container.worldToLocal(pp2, wp2);

				g.setColor(Color.orange);
				g.drawLine(pp.x + 1, pp.y, pp2.x + 1, pp2.y);
				g.drawLine(pp.x, pp.y + 1, pp2.x, pp2.y + 1);
				g.setColor(Color.darkGray);
				g.drawLine(pp.x, pp.y, pp2.x, pp2.y);

				// the circles and crosses
				DataDrawSupport.drawCross(g, pp.x, pp.y, DataDrawSupport.FMT_CROSS);
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
	private void feedback(IContainer container, Point screenPoint, Point2D.Double worldPoint,
			List<String> feedbackStrings) {

		// fmt crosses?

		int count = crossCount();
		if (count > 0) {
			for (int i = 0; i < count; i++) {
				if (contains(i, screenPoint)) {
					addFeedback(i, feedbackStrings);
					break;
				}
			}
		}

	}

	private int crossCount() {
		return (_drawnCrosses == null) ? 0 : _drawnCrosses.rows();
	}

	private boolean contains(int index, Point screenPoint) {
		Point location = (_drawnLocations == null || index >= _drawnLocations.length) ? null : _drawnLocations[index];
		return location != null && Math.abs(location.x - screenPoint.x) <= DataDrawSupport.HITHALF
				&& Math.abs(location.y - screenPoint.y) <= DataDrawSupport.HITHALF;
	}

	private void addFeedback(int index, List<String> feedbackStrings) {
		feedbackStrings.add(String.format("$Forest Green$FMTRec cross ID %d", _drawnCrosses.getShort("ID", index)));
		feedbackStrings.add(String.format("$Forest Green$FMTRec sector %d region %d",
				_drawnCrosses.getByte("sector", index), _drawnCrosses.getByte("region", index)));
		feedbackStrings.add(String.format("$Forest Green$FMTRec cross xyz (%-6.3f, %-6.3f, %-6.3f) cm",
				_drawnCrosses.getFloat("x", index), _drawnCrosses.getFloat("y", index),
				_drawnCrosses.getFloat("z", index)));
		feedbackStrings.add(String.format("$Forest Green$FMTRec cross error (%-6.3f, %-6.3f, %-6.3f) cm",
				_drawnCrosses.getFloat("err_x", index), _drawnCrosses.getFloat("err_y", index),
				_drawnCrosses.getFloat("err_z", index)));
		feedbackStrings.add(String.format("$Forest Green$FMTRec cross direction (%-6.3f, %-6.3f, %-6.3f)",
				_drawnCrosses.getFloat("ux", index), _drawnCrosses.getFloat("uy", index),
				_drawnCrosses.getFloat("uz", index)));
	}

	@Override
	public void vdrawFeedback(IContainer container, Point screenPoint, Double worldPoint, List<String> feedbackStrings,
			int option) {
		feedback(container, screenPoint, worldPoint, feedbackStrings);

	}

}
