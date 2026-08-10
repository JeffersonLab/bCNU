package cnuphys.ced.cedview.dchex;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Point2D;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.ced.alldata.DCClusters;
import cnuphys.ced.alldata.DCHits;
import cnuphys.ced.frame.CedColors;

public class DCHexClusterDrawer {

	//the parent view
	private DCHexView _view;

	public static final Stroke THICKLINE = new BasicStroke(2.0f);

	// data containers
	private DCHits _hbData = DCHits.hitBased();
	private DCHits _tbData = DCHits.timeBased();
	private DCHits _hbAIData = DCHits.aiHitBased();
	private DCHits _tbAIData = DCHits.aiTimeBased();
	private DCClusters _hbClusterData = DCClusters.hitBased();
	private DCClusters _tbClusterData = DCClusters.timeBased();
	private DCClusters _hbAIClusterData = DCClusters.aiHitBased();
	private DCClusters _tbAIClusterData = DCClusters.aiTimeBased();



	/**
	 * A cluster drawer for the all dc view
	 * @param view the all dc parent view
	 */
	public DCHexClusterDrawer(DCHexView view) {
		_view = view;
	}


	/**
	 * Draw the hit based DC clusters
	 */
	public void drawHBDCClusters(Graphics g, IContainer container) {
		drawDCClusterList(g, container, _hbClusterData, _hbData, CedColors.HB_COLOR);
	}

	/**
	 * Draw the time based DC clusters
	 */
	public void drawTBDCClusters(Graphics g, IContainer container) {
		drawDCClusterList(g, container, _tbClusterData, _tbData, CedColors.TB_COLOR);
	}

	/**
	 * Draw the AI hit based DC clusters
	 */
	public void drawAIHBDCClusters(Graphics g, IContainer container) {
		drawDCClusterList(g, container, _hbAIClusterData, _hbAIData, CedColors.AIHB_COLOR);
	}

	/**
	 * Draw the AI time based DC clusters
	 */
	public void drawAITBDCClusters(Graphics g, IContainer container) {
		drawDCClusterList(g, container, _tbAIClusterData, _tbAIData, CedColors.AITB_COLOR);
	}



	//draws the HB or TB clusters
	private void drawDCClusterList(Graphics g, IContainer container, DCClusters clusters, DCHits reconHits, Color color) {

		if ((clusters == null) || (reconHits == null)) {
			return;
		}

		for (int i = 0; i < clusters.count(); i++) {
			short hitIds[] = clusters.hitIds(i);
			if (hitIds != null) {
				//count the non-negative hit ids
				int length = 0;
				for (short element : hitIds) {
					if (element > 0) {
						length++;
					} else {
						break;
					}
				}

				if (length > 0) {
					int sector = clusters.sector(i); // 1..6
					int superlayer = clusters.superlayer(i); // 1..6
					int layer[] = new int[length]; // 1..6
					int wire[] = new int[length]; // 1..112

					int j = 0;
					for (short element : hitIds) {
						if (element > 0) {
							int index = reconHits.indexFromId(element);
							if (index >= 0) {
								layer[j] = reconHits.layer(index);
								wire[j] = reconHits.wire(index);
							}
							j++;
						}
					}


					drawSingleClusterOfWires(g, container, sector, superlayer, layer, wire, color);
				}

			}
		}


	}

	/**
	 *
	 * @param g
	 * @param container
	 * @param sector 1-based sector
	 * @param superlayer 1-based superlayer
	 * @param layer array of 1-based wires
	 * @param wire array of 1-based wires
	 */
	private void drawSingleClusterOfWires(Graphics g, IContainer container,
			int sector, int superlayer, int layer[], int wire[], Color color) {


		Graphics2D g2 = (Graphics2D)g;

		Stroke saveStroke = g2.getStroke();
		g2.setStroke(THICKLINE);


		Area area = new Area();

		Point2D.Double[] wirePoly = new Point2D.Double[4];
		for (int i = 0; i < 4; i++) {
			wirePoly[i] = new Point2D.Double();
		}
		Point pp = new Point();

		for (int i = 0; i < wire.length; i++) {

			if ((wire[i] <= 0) || (layer[i] <= 0)) {
				continue;
			}
			if (!_view.getWirePolygon(sector, superlayer, layer[i], wire[i], wirePoly)) continue;

			Polygon poly = new Polygon();
			for (int j = 0; j < 4; j++) {
				container.worldToLocal(pp, wirePoly[j]);
				poly.addPoint(pp.x, pp.y);
			}
			area.add(new Area(poly));
		}

		if (!area.isEmpty()) {
			g2.setColor(color);
            g2.fill(area);
            g2.setColor(color.darker());
            g2.draw(area);
		}

		g2.setStroke(saveStroke);
	}



}
