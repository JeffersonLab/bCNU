package cnuphys.ced.cedview.central;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.ced.alldata.DataDrawSupport;
import cnuphys.ced.alldata.BSTAdc;
import cnuphys.ced.alldata.BSTRecHits;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.geometry.BSTGeometry;
import cnuphys.ced.geometry.BSTxyPanel;
import eu.mihosoft.vrl.v3d.Vector3d;

public class CentralZHitDrawer extends CentralHitDrawer {


	// owner view
	private CentralZView _view;

	//data containers
	private BSTAdc adcBSTData = BSTAdc.getInstance();
	private BSTRecHits bstRecHitData = BSTRecHits.getInstance();


	public CentralZHitDrawer(CentralZView view) {
		super(view);
		_view = view;
	}

	@Override
	public String getName() {
		return "CentralZHitDrawer";
	}

	@Override
	protected void drawBSTAccumulatedHits(Graphics g, IContainer container) {

		int maxHit = AccumulationManager.getInstance().getMaxFullBSTCount();

		// first index is layer 0..7, second is sector 0..23
		int bstFullData[][][] = AccumulationManager.getInstance().getAccumulatedBSTFullData();
		for (int lay0 = 0; lay0 < 6; lay0++) {
			for (int sect0 = 0; sect0 < BSTGeometry.sectorsPerLayer[lay0]; sect0++) {
				for (int strip0 = 0; strip0 < 256; strip0++) {
					int hitCount = bstFullData[lay0][sect0][strip0];

					if (hitCount > 1) {
						double fract = (maxHit == 0) ? 0 : (((double) hitCount) / maxHit);
						Color color = AccumulationManager.getInstance().getColor(_view.getColorScaleModel(), fract);
						_view.drawBSTStrip((Graphics2D) g, container, color, sect0 + 1, lay0 + 1, strip0 + 1);
					}

				}
			}
		}
	}

	// draw gemc simulated hits single event mode
	@Override
	protected void drawBSTHitsSingleMode(Graphics g, IContainer container) {

		drawBSTADCData(g, container);
		drawBSTReconHits(g, container);
	}

	// draw BST adc data
	private void drawBSTADCData(Graphics g, IContainer container) {
		if (_view.showADCHits()) {
			for (int i = 0; i < adcBSTData.count(); i++) {
				int sector = adcBSTData.sector(i);

				int layer = adcBSTData.layer(i);
				BSTxyPanel panel = CentralXYView.getPanel(layer, sector);
				if (panel != null) {

					int strip = adcBSTData.component(i);
					_view.drawBSTStrip((Graphics2D) g, container, adcBSTData.color(i), sector, layer, strip);
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

				for (int i = 0; i < count; i++) {
					int sector = bstRecHitData.sector(i);
					int layer = bstRecHitData.layer(i);
					int strip = bstRecHitData.strip(i);
					if (bstRecHitData.hasValidGeometry(i, BSTGeometry.sectorsPerLayer)) {
						Vector3d v = BSTGeometry.getStripMidpoint(sector - 1, layer - 1, strip - 1);
						double alpha = _view.labToLocalWithAlpha(v.x, v.y, v.z, pp);
						int alp = (int) Math.max(0, Math.min(255, 255 * alpha));
						DataDrawSupport.drawReconHit(g, pp, alp);
						bstRecHitData.setLocation(i, pp);
					} else {
						System.err.println("bad data in CentralZHitDrawer drawBSTReconHits ");
					}
				}
			}

		}
	}


}
