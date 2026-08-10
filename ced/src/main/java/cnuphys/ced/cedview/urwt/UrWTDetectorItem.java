package cnuphys.ced.cedview.urwt;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;

import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.item.ItemList;
import cnuphys.bCNU.item.PolygonItem;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.ced.alldata.DataDrawSupport;
import cnuphys.ced.alldata.URWTHits;
import cnuphys.ced.alldata.URWTClusters;
import cnuphys.ced.alldata.URWTCrosses;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.geometry.urwt.UrWTGeometry;

public class UrWTDetectorItem extends PolygonItem {
	
	// tolerance for hit testing in pixels
	private static final double HIT_TEST_TOLERANCE = 4;

	//1-based sector
	public final int sector;

	//1-based layer
	public final int layer;
	
	//work space
	private Point _pp1 = new Point();
	private Point _pp2 = new Point();
	
	
	//layer colors
	public static Color layerColors[] = {
			X11Colors.getX11Color("Dark Blue"),
			X11Colors.getX11Color("Web Green"),
			X11Colors.getX11Color("Dark Red"),
			X11Colors.getX11Color("coral"),

	};
	
	//layer colors
	public static Color layerAlphaColors[] = {
			X11Colors.getX11Color("Dark Blue", 30),
			X11Colors.getX11Color("Web Green", 30),
			X11Colors.getX11Color("Dark Red", 30),
			X11Colors.getX11Color("coral", 30),

	};

	//cluster colors 
	private final Color clusterColor1;
	private final Color clusterColor2;



	/**
	 * Create a chamber outline
	 *
	 * @param itemList the item list
	 * @param sector   the sector [1..6]
	 * @param layer    the layer [1..4]
	 */
	public UrWTDetectorItem(ItemList itemList, int sector, int layer) {
		super(itemList, getPoints(sector, layer));
		this.sector = sector;
		this.layer = layer;
		getStyle().setFillColor(layerAlphaColors[layer-1]);
		clusterColor1 = layerColors[layer-1].darker();
		clusterColor2 = layerColors[layer-1].brighter();
		
	}
	
	// to get the polygon points for the super constructor
	private static Point2D.Double[] getPoints(int sector, int layer) {
		return UrWTGeometry.getDetectorData(sector, layer).getXYPoints();
	}
	
	// helper to get the view
	private UrWTXYView getView() {
		return (UrWTXYView) getContainer().getView();
	}
	

	/**
	 * Custom drawer for the item.
	 *
	 * @param g         the graphics context.
	 * @param container the graphical container being rendered.
	 */
	@Override
	public void drawItem(Graphics g, IContainer container) {
		if (!showLayer() || ClasIoEventManager.getInstance().isAccumulating()) {
			return;
		}
		super.drawItem(g, container);
	}
	
	/**
	 * Draw the hits for this layer. This is called by the view after all items have
	 * been drawn, so we can draw on top of the detector outlines.
	 * 
	 * @param g         the graphics context
	 * @param container the container being drawn
	 */
	protected void drawData(Graphics g, IContainer container) {
		if (!showLayer() || ClasIoEventManager.getInstance().isAccumulating()) {
			return;
		}

		if (ClasIoEventManager.getInstance().hasCurrentEvent()) {

			drawHits(g, container); // hits
			drawClusters(g, container); // clusters
			drawCrosses(g, container); // crosses
		}
	}
	
	
	// helper to draw the crosses
	private void drawCrosses(Graphics g, IContainer container) {
		UrWTXYView view = getView();
		if (!view.showCrosses()) {
			return;
		}
		

		URWTCrosses crosses = URWTCrosses.getInstance();
		for (int i = 0; i < crosses.count(); i++) {
			if (crosses.hasValidSector(i) && crosses.sector(i) == sector) {
				view.projectPoint(container, crosses.x(i), crosses.y(i), crosses.z(i), _pp1);
				DataDrawSupport.drawCross(g, _pp1.x, _pp1.y, 4);
			}
		}

	}
	
	// helper to draw the clusters
	private void drawClusters(Graphics g, IContainer container) {
		UrWTXYView view = getView();
		if (!view.showClusters()) {
			return;
		}

		URWTClusters clusters = URWTClusters.getInstance();
		for (int i = 0; i < clusters.count(); i++) {
			if (clusters.hasValidGeometry(i) && clusters.sector(i) == sector && clusters.layer(i) == layer) {
				view.projectLine(container, clusters.xo(i), clusters.yo(i), clusters.zo(i),
						clusters.xe(i), clusters.ye(i), clusters.ze(i), _pp1, _pp2);
				GraphicsUtilities.drawHighlightedLine(g, _pp1.x, _pp1.y, _pp2.x, _pp2.y, clusterColor1, clusterColor2);
			}
		}
		
	}
	

	
	// helper to draw the hits
	private void drawHits(Graphics g, IContainer container) {
		URWTHits hits = URWTHits.getInstance();
		
		g.setColor(layerColors[layer-1]);
		
		for (int i = 0; i < hits.count(); i++) {
			if (hits.hasValidGeometry(i) && hits.sector(i) == sector && hits.layer(i) == layer) {
				projectStrip(container, hits.strip(i));
				g.drawLine(_pp1.x, _pp1.y, _pp2.x, _pp2.y);
			}
		}
	}
	
	/**
	 * Draw the accumulated hits for this layer. This is called by the view after all items have been drawn, so we can draw on top of the detector outlines.
	 * @param g the graphics context
	 * @param container the container being drawn
	 */
	protected void drawAccumulatedData(Graphics g, IContainer container) {
		if (!showLayer()) {
			return;
		}
		
		int[][][] accumulatedHits = AccumulationManager.getInstance().getAccumulatedUrWTData();
		if (accumulatedHits == null) {
			return;
		}
		
		//the number of strips in the layer
		int stripCount = (layer < 3) ? 1465 : 1485;
		
		int maxHit = AccumulationManager.getInstance().getMaxUrWTCount();
		if (maxHit == 0) {
			return;
		}
		
		for (int strip = 1; strip <= stripCount; strip++) {
			int hitCount = accumulatedHits[sector-1][layer-1][strip-1];
			double fract = (double) hitCount / maxHit;
			Color color = AccumulationManager.getInstance().getAlphaColor(getView().getColorScaleModel(), fract, 28);
			g.setColor(color);
			projectStrip(container, strip);
			g.drawLine(_pp1.x, _pp1.y, _pp2.x, _pp2.y);
		}
		
		
	}
	
	public void  frame(Graphics g, IContainer container) {
		if (!showLayer() || ClasIoEventManager.getInstance().isAccumulating()) {
			return;
		}
		if (_lastDrawnPolygon != null) {
			g.setColor(Color.black);
			g.drawPolygon(_lastDrawnPolygon);
		}
	}
	
	// helper to check if we should show this layer
	private boolean showLayer() {
		
		UrWTXYView view = getView();
		
		if (layer == 1) {
			return view.showLayer1();
		} else if (layer == 2) {
			return view.showLayer2();
		} else if (layer == 3) {
			return view.showLayer3();
		} else if (layer == 4) {
			return view.showLayer4();
		}
		
		return false;
	}
	
	// helper to project a strip
	// the strip number is 1-based
	private void projectStrip(IContainer container, int strip) {
		UrWTXYView view = getView();
        view.projectStrip(container, sector, layer, strip, _pp1, _pp2);
	}

	@Override
	public void getFeedbackStrings(IContainer container, Point pp, Point2D.Double wp, List<String> feedbackStrings) {

		if (contains(container, pp)) {
			UrWTXYView view = getView();
    		if (view.isSingleEventMode() && view.showLayer(layer)) {
    			hitFeedback(container, pp, feedbackStrings);
    			clusterFeedback(container, pp, feedbackStrings);
			}
		}
	}

	private void clusterFeedback(IContainer container, Point pp, List<String> feedbackStrings) {
		UrWTXYView view = getView();
		if (view.isSingleEventMode() && view.showClusters()) {
			URWTClusters clusters = URWTClusters.getInstance();
			for (int i = 0; i < clusters.count(); i++) {
				if (clusters.hasValidGeometry(i) && clusters.sector(i) == sector && clusters.layer(i) == layer) {
					view.projectLine(container, clusters.xo(i), clusters.yo(i), clusters.zo(i),
							clusters.xe(i), clusters.ye(i), clusters.ze(i), _pp1, _pp2);
					boolean hit = GraphicsUtilities.isPointOnLine(_pp1, _pp2, pp, HIT_TEST_TOLERANCE);
					if (hit) {
						clusters.addFeedback(i, feedbackStrings);
					}
				}
			}
		}
	}

	// helper to add hit feedback strings
	private void hitFeedback(IContainer container, Point pp, List<String> feedbackStrings) {
		UrWTXYView view = getView();
		if (view.isSingleEventMode() && view.showLayer(layer)) {
			URWTHits hits = URWTHits.getInstance();
			
			for (int i = 0; i < hits.count(); i++) {
				if (hits.hasValidGeometry(i) && hits.sector(i) == sector && hits.layer(i) == layer) {
					projectStrip(container, hits.strip(i));
					boolean hit = GraphicsUtilities.isPointOnLine(_pp1, _pp2, pp, HIT_TEST_TOLERANCE);
					if (hit) {
						hits.addFeedback(i, feedbackStrings);
					}
				}
			}
		}
	}

}
