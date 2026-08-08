package cnuphys.ced.cedview.central;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D.Double;
import java.util.List;

import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.ced.alldata.CNDClusters;
import cnuphys.ced.alldata.DataDrawSupport;
import cnuphys.ced.alldata.DataWarehouse;
import cnuphys.ced.cedview.CedXYView;
import cnuphys.ced.cedview.alert.AlertXYView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.frame.Ced;

public class ClusterDrawerXY extends CentralXYViewDrawer {

	//data warehouse
	private DataWarehouse _dataWarehouse = DataWarehouse.getInstance();


	private static final Stroke THICKLINE = new BasicStroke(1.5f);

	public ClusterDrawerXY(CedXYView view) {
		super(view);
	}

	@Override
	public void draw(Graphics g, IContainer container) {

		if (ClasIoEventManager.getInstance().isAccumulating() || !_view.isSingleEventMode()) {
			return;
		}

		Graphics2D g2 = (Graphics2D) g;
		Shape oldClip = g2.getClip();
		// clip the active area
		Rectangle sr = container.getInsetRectangle();
		g2.clipRect(sr.x, sr.y, sr.width, sr.height);

		Stroke oldStroke = g2.getStroke();
		g2.setStroke(THICKLINE);

		drawCNDClusters(g, container);
		drawBSTClusters(g, container);
		drawBMTClusters(g, container);

		g2.setStroke(oldStroke);

		g2.setClip(oldClip);
	}

	/**
	 * Draw CND clusters
	 *
	 * @param g         the graphics context
	 * @param container the drawing container
	 */
	public void drawCNDClusters(Graphics g, IContainer container) {
		DataEvent event = ClasIoEventManager.getInstance().getCurrentEvent();
		if (event == null) {
			return;
		}

		CNDClusters cndClusterData = CNDClusters.getInstance();
		int count = cndClusterData.count();
		if (count > 0) {
			Point p = new Point();
			for (int i = 0; i < count; i++) {
				float x = cndClusterData.x(i);
				float y = cndClusterData.y(i);
				container.worldToLocal(p, 10 * x, 10 * y);
				DataDrawSupport.drawCluster(g, p);
				cndClusterData.setLocation(i, p);
			}
		}
	}

	/**
	 * Draw BST clusters
	 *
	 * @param g         the graphics context
	 * @param container the drawing container
	 */
	public void drawBSTClusters(Graphics g, IContainer container) {
		
		if (_view instanceof AlertXYView) {
			return;
		}


		DataEvent event = ClasIoEventManager.getInstance().getCurrentEvent();
		if (event == null) {
			return;
		}

		byte sector[] = _dataWarehouse.getByte("BSTRec::Clusters", "sector");

		int count = (sector == null) ? 0 : sector.length;
		if (count == 0) {
			return;
		}

		float x1[] = _dataWarehouse.getFloat("BSTRec::Clusters", "x1");
		if (x1 != null) {
			float y1[] = _dataWarehouse.getFloat("BSTRec::Clusters", "y1");
			float x2[] = _dataWarehouse.getFloat("BSTRec::Clusters", "x2");
			float y2[] = _dataWarehouse.getFloat("BSTRec::Clusters", "y2");

			Point p1 = new Point();
			Point p2 = new Point();

			for (int i = 0; i < count; i++) {
				container.worldToLocal(p1, 10 * x1[i], 10 * y1[i]);
				container.worldToLocal(p2, 10 * x2[i], 10 * y2[i]);

				if (Ced.getCed().isConnectCluster()) {
					g.setColor(Color.black);
					g.drawLine(p1.x, p1.y, p2.x, p2.y);
				}
				DataDrawSupport.drawCluster(g, p1);
				DataDrawSupport.drawCluster(g, p2);
			}
		}

	}

	/**
	 * Draw BMT clusters
	 *
	 * @param g         the graphics context
	 * @param container the drawing container
	 */
	public void drawBMTClusters(Graphics g, IContainer container) {
		
		if (_view instanceof AlertXYView) {
			return;
		}

		
		DataEvent event = ClasIoEventManager.getInstance().getCurrentEvent();
		if (event == null) {
			return;
		}

		byte sector[] = _dataWarehouse.getByte("BMTRec::Clusters", "sector");

		int count = (sector == null) ? 0 : sector.length;
		if (count == 0) {
			return;
		}

		float x1[] = _dataWarehouse.getFloat("BMTRec::Clusters", "x1");

		if (x1 != null) {

            float y1[] = _dataWarehouse.getFloat("BMTRec::Clusters", "y1");
            float x2[] = _dataWarehouse.getFloat("BMTRec::Clusters", "x2");
            float y2[] = _dataWarehouse.getFloat("BMTRec::Clusters", "y2");

			Point p1 = new Point();
			Point p2 = new Point();

			for (int i = 0; i < count; i++) {
				container.worldToLocal(p1, 10 * x1[i], 10 * y1[i]);
				container.worldToLocal(p2, 10 * x2[i], 10 * y2[i]);

				if (Ced.getCed().isConnectCluster()) {
					g.setColor(Color.black);
					g.drawLine(p1.x, p1.y, p2.x, p2.y);
				}
				DataDrawSupport.drawCluster(g, p1);
				DataDrawSupport.drawCluster(g, p2);
			}
		}

	}

	@Override
	public void feedback(IContainer container, Point screenPoint, Double worldPoint, List<String> feedbackStrings) {

		// CND clusters
		CNDClusters cndClusterData = CNDClusters.getInstance();
		for (int i = 0; i < cndClusterData.count(); i++) {
			if (cndClusterData.contains(i, screenPoint)) {
				cndClusterData.addFeedback(i, feedbackStrings);
				break;
			}
		}
	}

}
