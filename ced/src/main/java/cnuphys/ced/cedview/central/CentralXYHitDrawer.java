package cnuphys.ced.cedview.central;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Point2D;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.ced.alldata.DataDrawSupport;
import cnuphys.ced.alldata.BMTAdc;
import cnuphys.ced.alldata.BMTRecHits;
import cnuphys.ced.alldata.datacontainer.bst.BSTADCData;
import cnuphys.ced.alldata.BSTRecHits;
import cnuphys.ced.alldata.CNDAdc;
import cnuphys.ced.alldata.CTOFAdc;
import cnuphys.ced.alldata.CTOFClusters;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.cedview.alert.AlertXYView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.geometry.BMTGeometry;
import cnuphys.ced.geometry.BSTGeometry;
import cnuphys.ced.geometry.BSTxyPanel;
import cnuphys.ced.geometry.bmt.BMTSectorItem;
import cnuphys.splot.plot.X11Colors;

public class CentralXYHitDrawer extends CentralHitDrawer {

	private static Color _baseColor = new Color(255, 0, 0, 60);

	// owner view
	private ICentralXYView _iCentralView;
	
	private CedView _view;

	// data containers
	private CNDAdc adcCNDData = CNDAdc.getInstance();
	private CTOFAdc adcCTOFData = CTOFAdc.getInstance();
	private CTOFClusters clusterCTOFData = CTOFClusters.getInstance();
	private BSTADCData adcBSTData = BSTADCData.getInstance();
	private BMTAdc adcBMTData = BMTAdc.getInstance();
	private BSTRecHits bstRecHitData = BSTRecHits.getInstance();
	private BMTRecHits bmtRecHitData = BMTRecHits.getInstance();

	// accumulation manager
	private AccumulationManager _accumManager = AccumulationManager.getInstance();

	public CentralXYHitDrawer(ICentralXYView iCentralView, CedView view) {
		super(view);
		_iCentralView = iCentralView;
		_view = view;
	}

	@Override
	public String getName() {
		return "CentralXYHitDrawer";
	}

	// draw accumulated BST hits (panels)
	@Override
	protected void drawCNDAccumulatedHits(Graphics g, IContainer container) {
		int maxHit = _accumManager.getMaxCNDCount();

		int cndData[][][] = _accumManager.getAccumulatedCNDData();

		for (int sect0 = 0; sect0 < 24; sect0++) {
			for (int lay0 = 0; lay0 < 3; lay0++) {
				for (int order = 0; order < 2; order++) {

					int hitCount = cndData[sect0][lay0][order];

					CNDXYPolygon poly = _iCentralView.getCNDPolygon(sect0 + 1, lay0 + 1, order + 1);
					double fract = (maxHit == 0) ? 0 : (((double) hitCount) / maxHit);
					Color color = _accumManager.getColor(_view.getColorScaleModel(), fract);

					poly.draw(g, container, color, Color.black);

				}
			}
		}
	}

	// draw accumulated BST hits (panels)
	@Override
	protected void drawBSTAccumulatedHits(Graphics g, IContainer container) {
		
		if (_view instanceof AlertXYView) {
			return;
		}

		// panels

		int maxHit = _accumManager.getMaxBSTCount();

		// first index is layer 0..5, second is sector 0..23
		int bstData[][] = _accumManager.getAccumulatedBSTData();

		for (int lay0 = 0; lay0 < 6; lay0++) {
			for (int sect0 = 0; sect0 < BSTGeometry.sectorsPerLayer[lay0]; sect0++) {
				BSTxyPanel panel = CentralXYView.getPanel(lay0 + 1, sect0 + 1);

				if (panel != null) {
					int hitCount = bstData[lay0][sect0];

					double fract = (maxHit == 0) ? 0 : (((double) hitCount) / maxHit);
					Color color = _accumManager.getColor(_view.getColorScaleModel(), fract);
					_iCentralView.drawBSTPanel((Graphics2D) g, container, panel, color);

				}
			}
		}
	}

	// draw CTOF accumulated hits
	@Override
	protected void drawCTOFAccumulatedHits(Graphics g, IContainer container) {

		// sector and layer always == 1 only worry about component
		int maxHit = _accumManager.getMaxCTOFCount();

		int ctofData[] = _accumManager.getAccumulatedCTOFData();

		for (int index = 0; index < 48; index++) {
			CTOFXYPolygon poly = _iCentralView.getCTOFPolygon(index + 1);
			if (poly != null) {
				int hitCount = ctofData[index];

				double fract = (maxHit == 0) ? 0 : (((double) hitCount) / maxHit);
				Color color = _accumManager.getColor(_view.getColorScaleModel(), fract);

				poly.draw(g, container, index + 1, color);
			}
		}
	}

	// only called in single event mode
	@Override
	protected void drawHitsSingleMode(Graphics g, IContainer container) {
		if (ClasIoEventManager.getInstance().hasCurrentEvent()) {
			drawBSTHitsSingleMode(g, container);
			drawBMTHitsSingleMode(g, container);
			drawCTOFSingleHitsMode(g, container);
			drawCNDSingleHitsMode(g, container);
			drawCVTRecKFTraj(g, container);
			drawCVTP1Traj(g, container);
			drawCVTRecTraj(g, container);
		}
	}

	// draw CTOF hits
	@Override
	protected void drawCNDSingleHitsMode(Graphics g, IContainer container) {

		if (_eventManager.getCurrentEvent() == null) {
			return;
		}

		// draw based on adc data

		for (int i = 0; i < adcCNDData.count(); i++) {
			CNDXYPolygon poly = _iCentralView.getCNDPolygon(adcCNDData.sector(i), adcCNDData.layer(i), adcCNDData.order(i) + 1);
			if (poly != null) {
				Color color = adcCNDData.color(i);
				poly.draw(g, container, color, Color.black);
			}
		}
	}

	// draw CTOF hits
	@Override
	protected void drawCTOFSingleHitsMode(Graphics g, IContainer container) {

		if (_eventManager.getCurrentEvent() == null) {
			return;
		}

		// draw based on adc data
		for (int i = 0; i < adcCTOFData.count(); i++) {
			CTOFXYPolygon poly = _iCentralView.getCTOFPolygon(adcCTOFData.component(i));
			if (poly != null) {
				Color color = adcCTOFData.componentColor(adcCTOFData.sector(i), adcCTOFData.layer(i),
						adcCTOFData.component(i), adcCTOFData.order(i));
				poly.draw(g, container, adcCTOFData.component(i), color);
			}
		}

		if (_view.showClusters()) {
			Point pp = new Point();
			for (int i = 0; i < clusterCTOFData.count(); i++) {
				// convert to mm
				container.worldToLocal(pp, 10 * clusterCTOFData.x(i), 10 * clusterCTOFData.y(i));
				DataDrawSupport.drawCluster(g, pp);
				clusterCTOFData.setLocation(i, pp);
			}
		}

	}

	// draw BMT hits
	@Override
	protected void drawBMTHitsSingleMode(Graphics g, IContainer container) {
		if (_view instanceof AlertXYView) {
			return;
		}

		drawBMTADCData(g, container);
		drawBMTReconHits(g, container);
	}

	// draw BMT adc data
	private void drawBMTADCData(Graphics g, IContainer container) {
		
		if (_view.showADCHits()) {

			int count = adcBMTData.count();
			if (count > 0) {
				Point pp = new Point();
				Point2D.Double wp = new Point2D.Double();

				for (int i = 0; i < count; i++) {
					BMTSectorItem bmtItem = _iCentralView.getBMTSectorItem(adcBMTData.sector(i), adcBMTData.layer(i));
					if (bmtItem != null) {
						if (bmtItem.getLastDrawnPolygon() != null) {
							g.setColor(X11Colors.getX11Color("tan"));
							g.fillPolygon(bmtItem.getLastDrawnPolygon());
							g.setColor(Color.red);
							g.drawPolygon(bmtItem.getLastDrawnPolygon());
						}
						Polygon poly = bmtItem.getStripPolygon(container, adcBMTData.component(i));
						if (poly != null) {
							g.setColor(Color.black);
							g.fillPolygon(poly);
							g.setColor(adcBMTData.color(i));
							g.drawPolygon(poly);
						}

						if (bmtItem.isZLayer()) {

							Color color = adcBMTData.color(i);

							double phi = BMTGeometry.getGeometry().CRZStrip_GetPhi(adcBMTData.sector(i),
									adcBMTData.layer(i), adcBMTData.component(i));

							double rad = bmtItem.getInnerRadius() + BMTSectorItem.FAKEWIDTH / 2.;
							wp.x = rad * Math.cos(phi);
							wp.y = rad * Math.sin(phi);

							container.worldToLocal(pp, wp);
							adcBMTData.setLocation(i, pp);
							DataDrawSupport.drawAdcHit(g, pp, color);

						}
					}
				}

			}
		}
	}

	// draw bmt reconstructed hits

	// draw bst reconstructed hits
	private void drawBMTReconHits(Graphics g, IContainer container) {

		if (_view.showReconHits()) {
			int count = bmtRecHitData.count();
			if (count > 0) {
				Point pp = new Point();
				Point2D.Double wp = new Point2D.Double();

				for (int i = 0; i < count; i++) {
					BMTSectorItem bmtItem = _iCentralView.getBMTSectorItem(bmtRecHitData.sector(i), bmtRecHitData.layer(i));
					if (bmtItem != null && bmtItem.isZLayer()) {

						double phi = BMTGeometry.getGeometry().CRZStrip_GetPhi(bmtRecHitData.sector(i), bmtRecHitData.layer(i),
								bmtRecHitData.strip(i));

						double rad = bmtItem.getInnerRadius() + BMTSectorItem.FAKEWIDTH / 2.;
						wp.x = rad * Math.cos(phi);
						wp.y = rad * Math.sin(phi);
						container.worldToLocal(pp, wp);

						bmtRecHitData.setLocation(i, pp);
						DataDrawSupport.drawReconHit(g, pp);
					}

				}
			}

		}
	}
	// draw BST hits single event mode
	@Override
	protected void drawBSTHitsSingleMode(Graphics g, IContainer container) {
		if (_view instanceof AlertXYView) {
			return;
		}

		drawBSTADCData(g, container);
		drawBSTReconHits(g, container);
	}

	// draw BST adc data
	private void drawBSTADCData(Graphics g, IContainer container) {

		if (_view.showADCHits()) {
			for (int i = 0; i < adcBSTData.count(); i++) {
				BSTxyPanel panel = CentralXYView.getPanel(adcBSTData.layer[i], adcBSTData.sector[i]);
				if (panel != null) {
					_iCentralView.drawBSTPanel((Graphics2D) g, container, panel, _baseColor);
					_iCentralView.drawBSTPanel((Graphics2D) g, container, panel, adcBSTData.getADCColor(i));
				}
			}
		}

	}

	// draw bst reconstructed hits
	private void drawBSTReconHits(Graphics g, IContainer container) {

		if (_view.showReconHits()) {
			int count = bstRecHitData.count();
			if (count > 0) {
				Point pp = new Point();
				Point2D.Double wp = new Point2D.Double();

				for (int i = 0; i < count; i++) {
					if (bstRecHitData.hasValidGeometry(i, BSTGeometry.sectorsPerLayer)) {
						BSTGeometry.getStripMidpointXY(bstRecHitData.sector(i) - 1, bstRecHitData.layer(i) - 1,
								bstRecHitData.strip(i) - 1, wp);
						container.worldToLocal(pp, wp);
						bstRecHitData.setLocation(i, pp);
						DataDrawSupport.drawReconHit(g, pp);
					} else {
						System.err.println("bad data in CentralXYHitDrawer drawBSTReconHits ");
					}

				}
			}

		}
	}
}
