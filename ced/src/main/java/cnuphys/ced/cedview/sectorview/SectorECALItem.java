package cnuphys.ced.cedview.sectorview;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.List;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.style.LineStyle;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.item.ItemList;
import cnuphys.bCNU.item.PolygonItem;
import cnuphys.ced.alldata.ECalAdc;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.geometry.ECGeometry;

public class SectorECALItem extends PolygonItem {

	// 1-based sector
	private int _sector;

	// the container sector view
	private SectorView _view;

	// should be ECGeometry.EC_INNER or ECGeometry.EC_OUTER
	private int _plane;

	// should be ECGeometry.EC_U, EC_V, or EC_W
	private int _viewType;

	private static final String _ecPlanes[] = { "EC (inner)", "EC (outer)" };
	private static final String _ecViews[] = { "U", "V", "W" };
	private static final Color _ecFill[] = { new Color(225, 215, 215), new Color(215, 215, 225) };
	private static final Color _ecLine[] = { Color.gray, Color.gray };

	//data containers
	ECalAdc ecData = ECalAdc.getInstance();


	/**
	 * Create a world polygon item
	 *
	 * @param itemList   the item list this item is on.
	 * @param planeIndex should be EC_INNER or EC_OUTER
	 * @param stripIndex should be EC_U, EC_V, or EC_W
	 * @param sector the 1-based sector
	 */
	public SectorECALItem(ItemList itemList, int planeIndex, int stripIndex, int sector) {
		super(itemList, getShell((SectorView) itemList.getContainer().getView(), planeIndex, stripIndex, sector));

		setRightClickable(false);
		_sector = sector;
		_plane = planeIndex;
		_viewType = stripIndex;

		_name = _ecPlanes[_plane] + " " + _ecViews[_viewType] + " sector " + _sector;

		_style.setFillColor(_ecFill[planeIndex]);
		_style.setLineColor(_ecLine[planeIndex]);
		_style.setLineWidth(0);
		_view = (SectorView) getItemList().getContainer().getView();

	}

	@Override
	public boolean shouldDraw(Graphics g, IContainer container) {
		return true;
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

		Point2D.Double path[] = getShell(_view, _plane, _viewType, _sector);

		if (path == null) {
			System.err.println("StripType: " + _viewType + " path is null");
			return;
		}

		setPath(path);
		// super.drawItem(g, container);

		for (int stripIndex = 0; stripIndex < ECGeometry.EC_NUMSTRIP; stripIndex++) {
			Point2D.Double wp[] = getStrip(stripIndex);

			if (wp != null) {
				Path2D.Double path2d = WorldGraphicsUtilities.worldPolygonToPath(wp);
				WorldGraphicsUtilities.drawPath2D(g, container, path2d, _style.getFillColor(), _style.getLineColor(), 0,
						LineStyle.SOLID, true);
			}
		}

		// draw ADC data
		drawADCData(g, container);

	}

	/**
	 * Get a strip outline
	 *
	 * @param stripIndex the 0-based index
	 * @return the strip outline
	 */
	private Point2D.Double[] getStrip(int stripId) {

		if (!ECGeometry.doesProjectedPolyFullyIntersect(_plane, _viewType, stripId, _view.getProjectionPlane())) {
			return null;
		}

		Point2D.Double wp[] = ECGeometry.getIntersections(_plane, _viewType, stripId, _view.getProjectionPlane(),
				true);

		if (wp == null) {
			return null;
		}

		// lower sectors (4, 5, 6) (need sign flip
		if (_sector > 3) {
			for (Point2D.Double twp : wp) {
				twp.y = -twp.y;
			}
		}

		return wp;
	}

	// draw any hits
	private void drawADCData(Graphics g, IContainer container) {

		if (_view.isSingleEventMode()) {
			drawSingleEventADC(g, container);
		} else {
			drawAccumulatedHits(g, container);
		}
	}

	// single event drawer
	private void drawSingleEventADC(Graphics g, IContainer container) {

		for (int i = 0; i < ecData.count(); i++) {
			byte sector = ecData.sector(i);
			byte plane = ecData.plane(i);
			byte view = ecData.view(i);

			if (ecData.isECal(i) && (sector == _sector) && (plane == _plane) && (view == _viewType)) {
				int strip0 = ecData.strip(i) - 1;
				Point2D.Double wp[] = getStrip(strip0);

				if (wp != null) {
					Path2D.Double path = WorldGraphicsUtilities.worldPolygonToPath(wp);
					Color fc = ecData.adcColor(i);
					WorldGraphicsUtilities.drawPath2D(g, container, path, fc, fc, 0, LineStyle.SOLID, true);
				}
			}
		}

	}

	// accumulated drawer
	private void drawAccumulatedHits(Graphics g, IContainer container) {
		int maxHit = AccumulationManager.getInstance().getMaxECALCount(_plane);

		int hits[][][][] = AccumulationManager.getInstance().getAccumulatedECALData();
		for (int strip0 = 0; strip0 < 36; strip0++) {
			int hitCount = hits[_sector - 1][_plane][_viewType][strip0];
			double fract = (maxHit == 0) ? 0 : (((double) hitCount) / maxHit);

			Point2D.Double wp[] = getStrip(strip0);

			if (wp != null) {
				Color color = AccumulationManager.getInstance().getColor(_view.getColorScaleModel(), fract);
				Path2D.Double path = WorldGraphicsUtilities.worldPolygonToPath(wp);
				WorldGraphicsUtilities.drawPath2D(g, container, path, color, _style.getLineColor(), 0, LineStyle.SOLID,
						true);
			}

		}
	}

	/**
	 * Get the shell of the ec.
	 *
	 * @param view       the view being rendered.
	 * @param planeIndex the index (0: inner, 1:outer)
	 * @param stripType  the strip index (0:U, 1:V, 2:W)
	 * @param sector     the 1-based sector 1..6
	 * @return
	 */
	private static Point2D.Double[] getShell(SectorView view, int planeIndex, int stripType, int sector) {

		Point2D.Double wp[] = ECGeometry.getShell(planeIndex, stripType, view.getProjectionPlane());

		if (wp == null) {
			System.err.println("null shell in SectorECItem planeIndex = " + planeIndex + " stripType = "
					+ stripType + "  sector = " + sector);
			return null;
		}

		// lower sectors (4, 5, 6) (need sign flip
		if (sector > 3) {
			for (Point2D.Double twp : wp) {
				twp.y = -twp.y;
			}
		}

		return wp;
	}

	/**
	 * Add any appropriate feedback strings panel. Default implementation returns
	 * the item's name.
	 *
	 * @param container       the Base container.
	 * @param screenPoint     the mouse location.
	 * @param worldPoint      the corresponding world point.
	 * @param feedbackStrings the List of feedback strings to add to.
	 */
	@Override
	public void getFeedbackStrings(IContainer container, Point screenPoint, Point2D.Double worldPoint,
			List<String> feedbackStrings) {

		if (contains(container, screenPoint)) {
			feedbackStrings.add(getName());
		}
		else {
			return;
		}

		// which strip?
		for (int strip0 = 0; strip0 < ECGeometry.EC_NUMSTRIP; strip0++) {
			Point2D.Double wp[] = getStrip(strip0);
			if (wp != null) {
				Path2D.Double path = WorldGraphicsUtilities.worldPolygonToPath(wp);

				if (path.contains(worldPoint)) {
					feedbackStrings.add("$white$plane " + _ecPlanes[_plane] + " view " + _ecViews[_viewType]
							+ " strip " + (strip0 + 1));

					for (int i = 0; i < ecData.count(); i++) {
						byte sector = ecData.sector(i);
						byte plane = ecData.plane(i);
						byte view = ecData.view(i);
						int strip = ecData.strip(i);

						if (ecData.isECal(i) && (sector == _sector) && (plane == _plane)
								&& (view == _viewType) && (strip == (strip0+1))) {
							String str = String.format("%s %s strip %d adc %d time %-7.3f",
									ECGeometry.PLANE_NAMES[plane], ECGeometry.VIEW_NAMES[view], strip,
									ecData.adc(i), ecData.time(i));

							feedbackStrings.add("$coral$" + str);

							break;
						}
					}
					return;
				}
			} // wp != null
		} // strip loop
	} // getFeedbackStrings
}
