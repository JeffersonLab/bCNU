package cnuphys.ced.cedview.ftof;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Hashtable;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.ced.alldata.DataDrawSupport;
import cnuphys.ced.alldata.FTOFHits;
import cnuphys.ced.alldata.datacontainer.tof.FTOFClusterData;

public class FTOFHighlightHandler {

	//parent view
	private  FTOFView _view;

	// work space
	private Point _pp = new Point();
	private Point2D.Double _wp = new Point2D.Double();

	// cluster data
	private FTOFClusterData _clusterData = FTOFClusterData.getInstance();

	//hit data
	private FTOFHits _hitData = FTOFHits.getInstance();


	private Hashtable<String, Integer> highlights = new Hashtable<>();

	/**
	 * Will handle select-from-bank highlight drawing
	 * @param view
	 */
	public FTOFHighlightHandler(FTOFView view) {
		_view = view;
	}

	public void draw(Graphics g, IContainer container) {

		Integer index = highlights.get("FTOF::hits");

		if ((index != null) && index.intValue() >= 0) {
			int row = index.intValue();
			byte layer = _hitData.layer(row);
			if (_view.displayPanel() == (layer-1)) {
				double x = _hitData.x(row);
				double y = _hitData.y(row);
				_wp.setLocation(x, y);
				container.worldToLocal(_pp, _wp);
				DataDrawSupport.drawReconHitHighlight(g, _pp);
			}

		}


		//highlight cluster
		index = highlights.get("FTOF::clusters");
		if ((index != null) && index.intValue() >= 0) {
			int row = index.intValue();

            byte layer = _clusterData.layer[row];
			if (_view.displayPanel() == (layer-1)) {
				double x = _clusterData.x[row];
				double y = _clusterData.y[row];
				_wp.setLocation(x, y);
				container.worldToLocal(_pp, _wp);
				DataDrawSupport.drawClusterHighlight(g, _pp);
			}

		}
	}

	public void set(String bankname, int index) {
		highlights.remove(bankname);
		highlights.put(bankname, index);
	}

	/*
	 * Clear all highlight info
	 */
	public void reset() {
		highlights.clear();
	}


}
