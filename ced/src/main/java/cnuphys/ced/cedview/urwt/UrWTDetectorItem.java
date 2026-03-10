package cnuphys.ced.cedview.urwt;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;

import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.item.ItemList;
import cnuphys.bCNU.item.PolygonItem;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.ced.alldata.DataWarehouse;
import cnuphys.ced.clasio.ClasIoEventManager;
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

	//data warehouse
	private static DataWarehouse _dataWarehouse = DataWarehouse.getInstance();

	
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
	
	public void  frame(Graphics g, IContainer container) {
		if (!showLayer() || ClasIoEventManager.getInstance().isAccumulating()) {
			return;
		}
		if (_lastDrawnPolygon != null) {
			g.setColor(Color.black);
			g.drawPolygon(_lastDrawnPolygon);
		}
	}
	
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
	private void projectStrip(IContainer container, int strip) {
		UrWTXYView view = getView();
        view.projectStrip(container, sector, layer,strip, _pp1, _pp2);
	}

	@Override
	public void getFeedbackStrings(IContainer container, Point pp, Point2D.Double wp, List<String> feedbackStrings) {

		if (contains(container, pp)) {
			UrWTXYView view = getView();
    		if (view.isSingleEventMode() && view.showLayer(layer)) {
				DataEvent event = ClasIoEventManager.getInstance().getCurrentEvent();
				if (event == null) {
					return;
				}

				byte sector[] = _dataWarehouse.getByte("URWT::hits", "sector");

				int count = (sector == null) ? 0 : sector.length;
				if (count == 0) {
					return;
				}

				byte layer[] = _dataWarehouse.getByte("URWT::hits", "layer");
				short strip[] = _dataWarehouse.getShort("URWT::hits","strip");
				
				if (layer == null || strip == null) {
					return;
				}
				
				for (int i = 0; i < count; i++) {
					if (sector[i] == this.sector && layer[i] == this.layer) {
						projectStrip(container, strip[i]);
						boolean hit = GraphicsUtilities.isPointOnLine(_pp1, _pp2, pp, HIT_TEST_TOLERANCE);
						if (hit) {
							feedbackStrings.add(String.format("URWT hit: sector %d, layer %d, strip %d", sector[i],
									layer[i], strip[i]));
						}
					}
				}
			}
		}
	}

}
