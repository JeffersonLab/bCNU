package cnuphys.ced.cedview.ftof;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.util.List;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.ced.alldata.FTOFAdc;

/**
 * Handles drawing and feedback for the adc (and tdc) bank
 * @author heddle
 *
 */
public class FTOFAdcHandler {


	// the parent view
	private FTOFView _view;

	//data containers
	private FTOFAdc _adcData = FTOFAdc.getInstance();

	public FTOFAdcHandler(FTOFView view) {
		_view = view;
	}


	// draw the adc data
	public void draw(Graphics g, IContainer container) {
		if (_view.isSingleEventMode()) {

			int count = _adcData.count();
			if (count == 1) {
				return;
			}

			Polygon poly = new Polygon();
			for (int i = 0; i < count; i++) {
				int panel = _adcData.layer(i) - 1; // nasty -- this is zero based
				if (panel == _view.displayPanel()) {

					_view.getPaddlePolygon(container, _adcData.sector(i), panel, _adcData.component(i), poly);
					Color colorL = _adcData.componentColor(_adcData.sector(i), _adcData.layer(i), _adcData.component(i), (byte) 0);
					Color colorR = _adcData.componentColor(_adcData.sector(i), _adcData.layer(i), _adcData.component(i), (byte) 1);

					if (colorL == null) {
						colorL = Color.white;
					}
					if (colorR == null) {
						colorR = Color.white;
					}
					GradientPaint gpaint = new GradientPaint(poly.xpoints[0], poly.ypoints[0], colorL, poly.xpoints[2],
							poly.ypoints[2], colorR);

					((Graphics2D) g).setPaint(gpaint);
					g.fillPolygon(poly);

					g.fillPolygon(poly);
				}
			}

		} else {
			// drawAccumulatedHits(g, container);
		}

	}

	/**
	 * Get the feedback strings for the adc bank
	 * @param container the drawing container
	 * @param sect the 1-based sector
	 * @param layer the 1-based layer
	 * @param paddleId the 1-based paddle
	 * @param pp the pixel point
	 * @param wp the world point
	 * @param feedbackStrings the list to add feedback strings to
	 */
	public void getFeedbackStrings(IContainer container, int sect, int layer, int paddleId, Point pp, Point2D.Double wp,
			List<String> feedbackStrings) {

		for (int i = 0; i < _adcData.count(); i++) {
			if ((_adcData.sector(i) == sect) && (_adcData.layer(i) == layer) && (_adcData.component(i) == paddleId)) {
				_adcData.addFeedback(i, feedbackStrings);
			}
		}
	}


}
