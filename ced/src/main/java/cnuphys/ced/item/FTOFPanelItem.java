package cnuphys.ced.item;

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
import cnuphys.ced.alldata.DataDrawSupport;
import cnuphys.ced.alldata.FTOFHits;
import cnuphys.ced.alldata.datacontainer.tof.FTOFADCData;
import cnuphys.ced.alldata.datacontainer.tof.FTOFClusterData;
import cnuphys.ced.cedview.sectorview.SectorView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.geometry.GeometryManager;
import cnuphys.ced.geometry.ftof.FTOFGeometry;
import cnuphys.ced.geometry.ftof.FTOFPanel;

public class FTOFPanelItem extends PolygonItem {

	private FTOFPanel _ftofPanel;

	// 1-based sector
	private int _sector;

	// the container sector view
	private SectorView _view;

	//the event manager
	private ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();

	//data containers
	private FTOFADCData _adcData = FTOFADCData.getInstance();
	private FTOFClusterData _clusterData = FTOFClusterData.getInstance();
	private FTOFHits _hitData = FTOFHits.getInstance();

	/**
	 * Create a FTOFPanelItem
	 *
	 * @param itemList the list this item is on.
	 */
	public FTOFPanelItem(ItemList itemList, FTOFPanel panel, int sector) {
		super(itemList, getShell((SectorView) itemList.getContainer().getView(), panel, sector));

		_ftofPanel = panel;
		_sector = sector;

		_name = (panel != null) ? FTOFGeometry.panelName((byte)panel.getPanelType()) : "??";

		// _style.setFillColor(X11Colors.getX11Color("Wheat", 128));
		_style.setFillColor(Color.white);
		_style.setLineWidth(0);
		_view = (SectorView) getItemList().getContainer().getView();
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

		Point2D.Double path[] = getShell(_view, _ftofPanel, _sector);
		if (path == null) {
			return;
		}

		setPath(path);
		super.drawItem(g, container);

		// adc data
		drawFTOFData(g, container);

		Point2D.Double wp[] = GeometryManager.allocate(4);

		// draw a line marking paddle boundary
		for (int i = 0; i < _ftofPanel.getCount(); i++) {
			boolean isects = _ftofPanel.getPaddle(i, _view.getProjectionPlane(), wp);
			if (isects) {
				if (_sector > 3) {
					wp[0].y = -wp[0].y;
					wp[3].y = -wp[3].y;
				}

				WorldGraphicsUtilities.drawWorldLine(g, container, wp[0], wp[3], _style);
			}
		}

	}

	// draw any adc based data and hits
	private void drawFTOFData(Graphics g, IContainer container) {

		if (_view.isSingleEventMode()) {
			drawSingleModeADCBased(g, container);
			drawSingleModeHits(g, container);
			drawSingleModeClusters(g, container);
		} else {
			drawAccumulatedADCBased(g, container);
		}
	}

	//draw based on accumulated data
	private void drawAccumulatedADCBased(Graphics g, IContainer container) {
		int hits[][] = null;

		int maxHit = 0;

		int panelType = _ftofPanel.getPanelType();
		switch (panelType) {
		case FTOFGeometry.PANEL_1A:
			maxHit = AccumulationManager.getInstance().getMaxFTOF1ACount();
			hits = AccumulationManager.getInstance().getAccumulatedFTOF1AData();
			break;
		case FTOFGeometry.PANEL_1B:
			maxHit = AccumulationManager.getInstance().getMaxFTOF1BCount();
			hits = AccumulationManager.getInstance().getAccumulatedFTOF1BData();
			break;
		case FTOFGeometry.PANEL_2:
			maxHit = AccumulationManager.getInstance().getMaxFTOF2Count();
			hits = AccumulationManager.getInstance().getAccumulatedFTOF2Data();
			break;
		}

		if (hits != null) {
			int sect0 = _sector - 1;
			for (int paddle0 = 0; paddle0 < hits[sect0].length; paddle0++) {

				int hitCount = hits[sect0][paddle0];
				double fract = (maxHit == 0) ? 0 : (((double) hitCount) / maxHit);
				Color fc = AccumulationManager.getInstance().getColor(_view.getColorScaleModel(), fract);
				Point2D.Double wp[] = getPaddle(_view, paddle0, _ftofPanel, _sector);

				if (wp != null) {
					Path2D.Double path = WorldGraphicsUtilities.worldPolygonToPath(wp);
					WorldGraphicsUtilities.drawPath2D(g, container, path, fc, _style.getLineColor(), 0, LineStyle.SOLID,
							true);
				}
			}
		}

	}

	//draw based on data in ADC bank
	private void drawSingleModeADCBased(Graphics g, IContainer container) {

		if (_eventManager.getCurrentEvent() == null) {
			return;
		}


		byte sect = (byte) _sector; //1-based
		byte layer = (byte) (_ftofPanel.getPanelType() + 1); //(now) 1-based

		for (int i = 0; i < _adcData.count(); i++) {
			if ((_adcData.sector[i] == sect) && (_adcData.layer[i] == layer)) {
				Point2D.Double wp[] = getPaddle(_view, _adcData.component[i] - 1, _ftofPanel, _sector);

				if (wp != null) {
					Color fc = _adcData.getADCColor(i);
					Path2D.Double path = WorldGraphicsUtilities.worldPolygonToPath(wp);
					WorldGraphicsUtilities.drawPath2D(g, container, path, fc, _style.getLineColor(), 0, LineStyle.SOLID,
							true);
				}
			}
		}

	}

	//draw based on data in hits bank
	private void drawSingleModeClusters(Graphics g, IContainer container) {

		if ((_eventManager.getCurrentEvent() == null) || !_view.showClusters()) {
			return;
		}

		byte layer = (byte) (_ftofPanel.getPanelType() + 1); //(now) 1-based
		Point.Double wp = new Point.Double();
		Point pp = new Point();


		for (int i = 0; i < _clusterData.count(); i++) {
			if ((_clusterData.sector[i] == _sector) && (_clusterData.layer[i] == layer)) {
				_view.projectClasToWorld(_clusterData.x[i], _clusterData.y[i], _clusterData.z[i],
						_view.getProjectionPlane(), wp);
				container.worldToLocal(pp, wp);
				DataDrawSupport.drawCluster(g, pp);
				_clusterData.setLocation(i, pp);
			}
		}


	}

	// draw based on data in hits bank
	private void drawSingleModeHits(Graphics g, IContainer container) {

		if ((_eventManager.getCurrentEvent() == null) || !_view.showReconHits()) {
			return;
		}

		int count = _hitData.count();

		if (count > 0) {
			byte sect = (byte) _sector; // 1-based
			byte layer = (byte) (_ftofPanel.getPanelType() + 1); // (now) 1-based

			Point.Double wp = new Point.Double();
			Point pp = new Point();

			for (int i = 0; i < count; i++) {
				if ((_hitData.sector(i) == sect) && (_hitData.layer(i) == layer)) {
					_view.projectClasToWorld(_hitData.x(i), _hitData.y(i), _hitData.z(i), _view.getProjectionPlane(),
							wp);
					container.worldToLocal(pp, wp);
					DataDrawSupport.drawReconHit(g, pp);
					_hitData.setLocation(i, pp);
				}
			}
		}

	}


	/**
	 * Get the FTOFPanel which contains the geometry
	 *
	 * @return the ftofPanel
	 */
	public FTOFPanel getFtofPanel() {
		return _ftofPanel;
	}

	/**
	 * Get a paddle outline of the tof panel.
	 *
	 * @param view   the view being rendered.
	 * @param index  the zero-based paddle index.
	 * @param panel  the panel holding the geometry data
	 * @param sector the 1-based sector 1..6
	 * @return
	 */
	private static Point2D.Double[] getPaddle(SectorView view, int index, FTOFPanel panel, int sector) {

		// hide if don't fully intersect, which happens as phi moves away from midplane
		if (!doesPaddleFullIntersectPlane(view, index, panel)) {
			return null;
		}

		Point2D.Double wp[] = GeometryManager.allocate(4);

		panel.getPaddle(index, view.getProjectionPlane(), wp);

		// lower sectors (4, 5, 6) (need sign flip
		if (sector > 3) {
			for (Point2D.Double twp : wp) {
				twp.y = -twp.y;
			}
		}

		return wp;
	}

	// does paddle fully intersect projection plane?
	private static boolean doesPaddleFullIntersectPlane(SectorView view, int index, FTOFPanel panel) {

		return panel.paddleFullyIntersects(index, view.getProjectionPlane());
	}

	/**
	 * Get the shell of the tof panel.
	 *
	 * @param view   the view being rendered.
	 * @param panel  the panel holding the geometry data
	 * @param sector the 1-based sector 1..6
	 * @return
	 */
	private static Point2D.Double[] getShell(SectorView view, FTOFPanel panel, int sector) {
		if (panel == null) {
			return null;
		}

		Point2D.Double wp[] = panel.getShell(view.getProjectionPlane());

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

		// which paddle?

		for (int index = 0; index < _ftofPanel.getCount(); index++) {

			Point2D.Double wp[] = getPaddle(_view, index, _ftofPanel, _sector);

			if (wp != null) {
				Path2D.Double path = WorldGraphicsUtilities.worldPolygonToPath(wp);

				if (path.contains(worldPoint)) {

					byte sect = (byte) _sector; // 1-based
					byte layer = (byte) (_ftofPanel.getPanelType() + 1); // 1-based
					short paddle = (short) (index + 1); // 1-based

					feedbackStrings.add("$Orange Red$" + getName() + "  sector " + _sector + " paddle " + (index + 1));
					feedbackStrings.add("$Orange Red$paddle length "
							+ FTOFGeometry.getLength(_ftofPanel.getPanelType(), index) + " cm");

					for (int i = 0; i < _adcData.count(); i++) {
						if ((_adcData.sector[i] == sect) && (_adcData.layer[i] == layer)
								&& (_adcData.component[i] == paddle)) {
							_adcData.adcFeedback("FTOF", i, feedbackStrings);
							break;
						}
					}

					// any hit data?
					break;
				} // path contains wp
			} // end wp != null
		} // end for loop

		//rest of feedback depends on having an event
		if (_eventManager.getCurrentEvent() == null) {
			return;
		}

		// hit feedback
		if (_view.showReconHits()) {

			int count = _hitData.count();
			if (count > 0) {
				byte sect = (byte) _sector; // 1-based
				byte layer = (byte) (_ftofPanel.getPanelType() + 1); // 1-based

				for (int i = 0; i < count; i++) {
					if ((_hitData.sector(i) == sect) && (_hitData.layer(i) == layer)) {
						if (_hitData.contains(i, screenPoint)) {
							_hitData.addFeedback(i, feedbackStrings);
							break;
						}
					}
				}
			}

		} //end recon hits shown

		// cluster feedback
		if (_view.showClusters()) {

			for (int i = 0; i < _clusterData.count(); i++) {
				if ((_clusterData.sector[i] == _sector) && (_clusterData.layer[i] == _ftofPanel.getPanelType() + 1)) {
					if (_clusterData.contains(i, screenPoint)) {
						_clusterData.feedback("FTOF", i, feedbackStrings);
						break;
					}
				}
			}
		}

	}

}
