package cnuphys.ced.cedview.ftof;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.ced.alldata.DataDrawSupport;
import cnuphys.ced.alldata.FTOFClusters;

public class FTOFClusterHandler {

	// work space
	private Point _pp = new Point();
	private Point2D.Double _wp = new Point2D.Double();


	// the parent FTOF xy view
	private FTOFView _view;

	// cluster data
	private FTOFClusters _clusterData = FTOFClusters.getInstance();

	public FTOFClusterHandler(FTOFView view) {
		_view = view;
	}

	// draw the clusters
	public void draw(Graphics g, IContainer container) {
		if (_view.isSingleEventMode() && _view.showClusters()) {
			for (int i = 0; i < _clusterData.count(); i++) {
				int panel = _clusterData.layer(i) - 1;
				if (panel == _view.displayPanel()) {
					_wp.setLocation(_clusterData.x(i), _clusterData.y(i));
					container.worldToLocal(_pp, _wp);
					DataDrawSupport.drawCluster(g, _pp);
					_clusterData.setLocation(i, _pp);
				}
			}

		} else {
			// drawAccumulatedHits(g, container);
		}

	}

	/**
	 * Get feedback strings for the clusters
	 * @param container the drawing container
	 * @param sect the sector [1,6]
	 * @param panel the panel [0,2]
	 * @param paddleId the paddle id [1,23]
	 * @param pp the pixel point
	 * @param wp the world point
	 * @param feedbackStrings the list of feedback strings
	 */
	public void getFeedbackStrings(IContainer container, int sect, int panel, int paddleId, Point pp, Point2D.Double wp,
			List<String> feedbackStrings) {

		if (_view.isSingleEventMode() && _view.showClusters()) {

			for (int i = 0; i < _clusterData.count(); i++) {
			if ((sect == _clusterData.sector(i)) && (panel == (_clusterData.layer(i) - 1))) {
				_wp.setLocation(_clusterData.x(i), _clusterData.y(i));

				container.worldToLocal(_pp, _wp);

				if (_clusterData.contains(i, pp)) {
					_clusterData.addFeedback(i, feedbackStrings);
					break;
				}
			}
		}

		} else { // accum mode
		}

	}




}
