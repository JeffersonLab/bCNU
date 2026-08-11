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

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.ced.alldata.BMTClusters;
import cnuphys.ced.alldata.BSTClusters;
import cnuphys.ced.alldata.CNDClusters;
import cnuphys.ced.alldata.DataDrawSupport;
import cnuphys.ced.cedview.CedXYView;
import cnuphys.ced.cedview.alert.AlertXYView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.frame.Ced;

public class ClusterDrawerXY extends CentralXYViewDrawer {

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


		BSTClusters clusters = BSTClusters.getInstance();
		int count = clusters.count();
		if (count == 0) {
			return;
		}

		Point p1 = new Point();
		Point p2 = new Point();

		for (int i = 0; i < count; i++) {
			container.worldToLocal(p1, 10 * clusters.x1(i), 10 * clusters.y1(i));
			container.worldToLocal(p2, 10 * clusters.x2(i), 10 * clusters.y2(i));

			if (Ced.getCed().isConnectCluster()) {
				g.setColor(Color.black);
				g.drawLine(p1.x, p1.y, p2.x, p2.y);
			}
			DataDrawSupport.drawCluster(g, p1);
			DataDrawSupport.drawCluster(g, p2);
			clusters.setLocations(i, p1, p2);
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

		
		BMTClusters clusters = BMTClusters.getInstance();
		int count = clusters.count();
		if (count == 0) {
			return;
		}

		Point p1 = new Point();
		Point p2 = new Point();

		for (int i = 0; i < count; i++) {
			container.worldToLocal(p1, 10 * clusters.x1(i), 10 * clusters.y1(i));
			container.worldToLocal(p2, 10 * clusters.x2(i), 10 * clusters.y2(i));

			if (Ced.getCed().isConnectCluster()) {
				g.setColor(Color.black);
				g.drawLine(p1.x, p1.y, p2.x, p2.y);
			}
			DataDrawSupport.drawCluster(g, p1);
			DataDrawSupport.drawCluster(g, p2);
			clusters.setLocations(i, p1, p2);
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

		BSTClusters bstClusters = BSTClusters.getInstance();
		for (int i = 0; i < bstClusters.count(); i++) {
			if (bstClusters.contains(i, screenPoint)) {
				bstClusters.addFeedback(i, feedbackStrings);
				return;
			}
		}

		BMTClusters bmtClusters = BMTClusters.getInstance();
		for (int i = 0; i < bmtClusters.count(); i++) {
			if (bmtClusters.contains(i, screenPoint)) {
				bmtClusters.addFeedback(i, feedbackStrings);
				return;
			}
		}
	}

}
