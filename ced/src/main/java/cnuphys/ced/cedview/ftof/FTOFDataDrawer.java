package cnuphys.ced.cedview.ftof;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.ced.alldata.DataDrawSupport;
import cnuphys.ced.alldata.FTOFHits;
import cnuphys.ced.cedview.SwimTrajectoryDrawerXY;

/**
 * Handles drawing and feedback for data in the FTOF banks
 *
 * @author heddle
 *
 */
public class FTOFDataDrawer {

	// the parent view
	private FTOFView _view;

	// used to draw swum trajectories (if any) in the after drawer
	private SwimTrajectoryDrawerXY _swimTrajectoryDrawer;

	//adc handler
	private FTOFAdcHandler _adcHandler;

	//cluster handler
	private FTOFClusterHandler _clusterHandler;

	//hit based hits handler
	private FTOFHBHandler _hbHandler;

	//accumulating data
	private FTOFAccumulationHandler _accumHandler;


	// hits data
	FTOFHits _hitData = FTOFHits.getInstance();

	/**
	 * This object handles drawing and feedback for FTOF banks for the FTOF view
	 * @param view the FTOF view parent
	 */
	public FTOFDataDrawer(FTOFView view) {
		_view = view;
		_swimTrajectoryDrawer = new SwimTrajectoryDrawerXY(_view);
		_adcHandler = new FTOFAdcHandler(_view);
		_hbHandler = new FTOFHBHandler(_view);
		_clusterHandler = new FTOFClusterHandler(_view);
		_accumHandler = new FTOFAccumulationHandler(_view);
	}

	/**
	 * Draw all the data on the parent view
	 *
	 * @param g         the graphics context
	 * @param container the drawing container
	 */
	public void draw(Graphics g, IContainer container) {

		if (_view.isSingleEventMode()) {
			// drawADCData
			_adcHandler.draw(g, container);

			// draw trajectories
			_swimTrajectoryDrawer.draw(g, container);

			// draw hits
			if (_view.showReconHits()) {
				drawFTOFHits(g, container);
			}

			// draw hit based hits
			_hbHandler.draw(g, container);

			// draw clusters
			_clusterHandler.draw(g, container);
		} else { //accumulated mode
			_accumHandler.draw(g, container);
		}

	}

	//draw the hits
	private void drawFTOFHits(Graphics g, IContainer container) {

		Point pp = new Point();
		Point2D.Double wp = new Point2D.Double();

		for (int i = 0; i < _hitData.count(); i++) {
			int panel = _hitData.layer(i) - 1;
			if (panel == _view.displayPanel()) {
				 wp.setLocation(_hitData.x(i), _hitData.y(i));
				 container.worldToLocal(pp, wp);
				 DataDrawSupport.drawReconHit(g, pp);
				_hitData.setLocation(i, pp);
			}
		}
	}

	/**
	 * Get feedback related the the last data drawn
	 *
	 * @param container
	 * @param sect            the one based sector
	 * @param panel           PANEL_1A, PANEL_1B or PANEL_12 (0, 1, 2)
	 * @param paddleId        1-based paddle id
	 * @param pp              the screen point
	 * @param wp              the the world point
	 * @param feedbackStrings
	 */
	public void getFeedbackStrings(IContainer container, int sect, int panel, int paddleId, Point pp, Point2D.Double wp,
			List<String> feedbackStrings) {

		if (_view.isSingleEventMode()) {

			_hbHandler.getFeedbackStrings(container, sect, panel, paddleId, pp, wp, feedbackStrings);
			_clusterHandler.getFeedbackStrings(container, sect, panel, paddleId, pp, wp, feedbackStrings);

			// convert panel to 1-based layer
			if (paddleId > 0) {
				_adcHandler.getFeedbackStrings(container, sect, panel + 1, paddleId, pp, wp, feedbackStrings);
			}

			if (_view.showReconHits()) {
				for (int i = 0; i < _hitData.count(); i++) {
					if ((sect == _hitData.sector(i)) && (panel == (_hitData.layer(i) - 1))) {
						if (_hitData.contains(i, pp)) {
							_hitData.addFeedback(i, feedbackStrings);
							break;
						}
					}
				}
			}
		}
	}


	class ADCHit {
		public int sector;
		public int panel;
		public int component;
		public int adcL;
		public int adcR;

		public ADCHit(int sector, int panel, int component, int adcL, int adcR) {
			super();
			this.sector = sector;
			this.panel = panel;
			this.component = component;
			this.adcL = adcL;
			this.adcR = adcR;
		}
	}

}
