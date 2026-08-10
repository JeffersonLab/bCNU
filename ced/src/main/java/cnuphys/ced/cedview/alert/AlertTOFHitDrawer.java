package cnuphys.ced.cedview.alert;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;

import org.jlab.geom.component.ScintillatorPaddle;
import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.ced.alldata.ATOFTdc;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.geometry.alert.AlertGeometry;
import cnuphys.ced.geometry.alert.TOFLayer;

public class AlertTOFHitDrawer {

	private final ATOFTdc tdcData = ATOFTdc.getInstance();

	// the alert view
	private AlertXYView _view;

	/**
	 * Create a TOF hit drawer for the alert view.
	 *
	 * @param view the alert view.
	 */
	public AlertTOFHitDrawer(AlertXYView view) {
		_view = view;
	}

	/**
	 * Draw the hits
	 *
	 * @param g
	 * @param container
	 */
	public void drawHits(Graphics g, IContainer container) {

		if (ClasIoEventManager.getInstance().isAccumulating()) {
			return;
		}

		DataEvent dataEvent = ClasIoEventManager.getInstance().getCurrentEvent();
		if (dataEvent == null) {
			return;
		}

		drawTOFHits(g, container, dataEvent);

	}

	// draw the TOF hits
	private void drawTOFHits(Graphics g, IContainer container, DataEvent dataEvent) {
		if (dataEvent.hasBank(ATOFTdc.BANK_NAME) && _view.showADCHits()) {
			AlertTOFGeometryNumbering tdcGeom = new AlertTOFGeometryNumbering();

			for (int i = 0; i < tdcData.count(); i++) {
				if (!tdcGeom.fromHipoNumbering(tdcData.sector(i), tdcData.layer(i), tdcData.component(i),
						tdcData.order(i))) continue;
				TOFLayer tofl = AlertGeometry.getTOFLayer(tdcGeom.sector, tdcGeom.superlayer, tdcGeom.layer);
				if (tofl == null) {
					System.err.println("TOF layer not found for sector " + tdcGeom.sector + ", superlayer "
							+ tdcGeom.superlayer + ", layer " + tdcGeom.layer);
					continue;
				}

				ScintillatorPaddle paddle = tofl.getPaddle(tdcGeom.paddleIndex);
				tofl.drawPaddle(g, container, paddle, Color.red, Color.black);
			}
		}

	} // drawTOFHits


	/**
	 * Draw the highlighted hit from the adc bank
	 *
	 * @param g         the graphics context
	 * @param container the container
	 * @param index     the 0-based index of the hit
	 */
	public void drawHighlightHit(Graphics g, IContainer container, DataEvent dataEvent, int index) {
		if (dataEvent.hasBank(ATOFTdc.BANK_NAME) && _view.showADCHits() && tdcData.hasRow(index)) {
			AlertTOFGeometryNumbering tdcGeom = new AlertTOFGeometryNumbering();
			if (!tdcGeom.fromHipoNumbering(tdcData.sector(index), tdcData.layer(index), tdcData.component(index),
					tdcData.order(index))) return;
			TOFLayer tofl = AlertGeometry.getTOFLayer(tdcGeom.sector, tdcGeom.superlayer, tdcGeom.layer);
			if (tofl != null) {
				ScintillatorPaddle paddle = tofl.getPaddle(tdcGeom.paddleIndex);
				tofl.drawPaddle(g, container, paddle, Color.orange, Color.black);
			}
		}
	}


	public void drawAccumulatedHits(Graphics g, IContainer container) {
		drawAccumulatedHitsSL(g, container, 0);
		drawAccumulatedHitsSL(g, container, 1);

	}


	/**
	 * Draw the accumulated hits
	 * @param g
	 * @param container
	 */
	private void drawAccumulatedHitsSL(Graphics g, IContainer container, int superlayer) {

		int maxHit;
		int counts[][][];

		if (superlayer == 0) {
			maxHit = AccumulationManager.getInstance().getMaxAlertTOFSL0Count();
			counts = AccumulationManager.getInstance().getAccumulatedAlertTOFSL0Data();
		} else {
			maxHit = AccumulationManager.getInstance().getMaxAlertTOFSL1Count();
			counts = AccumulationManager.getInstance().getAccumulatedAlertTOFSL1Data();
		}


		if (counts == null) {
			return;
		}


		for (int sector = 0; sector < counts.length; sector++) {
			for (int layer = 0; layer < counts[sector].length; layer++) {
				TOFLayer tofl = AlertGeometry.getTOFLayer(sector, superlayer, layer);
				if (tofl == null) {
					continue;
				}

				for (int paddle = 0; paddle < counts[sector][layer].length; paddle++) {
					double count = counts[sector][layer][paddle];
					double fract = (maxHit == 0) ? 0 : (count / maxHit);
					Color color = AccumulationManager.getInstance().getColor(_view.getColorScaleModel(), fract);
					ScintillatorPaddle scintPaddle = tofl.getPaddle(paddle);
					tofl.drawPaddle(g, container, scintPaddle, color, Color.black);
				}
			}
		}
	}

	/**
	 * Get feedback strings for a hit
	 *
	 * @param container       the container
	 * @param pp              the pixel point
	 * @param wp              the world point
	 * @param tofl            the TOF layer
	 * @param feedbackStrings the list of feedback strings to add to
	 */
	public void getHitFeedbackStrings(IContainer container, Point pp, Point2D.Double wp, TOFLayer tofl,
			List<String> feedbackStrings) {

		if (ClasIoEventManager.getInstance().isAccumulating() || _view.isAccumulatedMode()) {
			return;
		}

		DataEvent dataEvent = ClasIoEventManager.getInstance().getCurrentEvent();
		if ((dataEvent == null) || !dataEvent.hasBank(ATOFTdc.BANK_NAME)) {
			return;
		}

		AlertTOFGeometryNumbering tdcGeom = new AlertTOFGeometryNumbering();

		for (int i = 0; i < tdcData.count(); i++) {
			if (!tdcGeom.fromHipoNumbering(tdcData.sector(i), tdcData.layer(i), tdcData.component(i),
					tdcData.order(i))) continue;
			if (tdcGeom.match(tofl)) {
				ScintillatorPaddle paddle = tofl.getPaddle(tdcGeom.paddleIndex);
				if (tofl.paddleContains(paddle, pp)) {
					tdcData.addFeedback(i, feedbackStrings);
					return;
				}
			}
		}

	}

}
