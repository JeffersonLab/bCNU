package cnuphys.ced.geometry.alert;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.jlab.geom.detector.alert.AHDC.AlertDCLayer;
import org.jlab.geom.detector.alert.AHDC.AlertDCWire;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.ced.cedview.alert.AlertLayerDonut;
import cnuphys.lund.X11Colors;

public class DCLayer {
	//LENGTH units are mm

	//layer shell
	private static Color[] shellColors = {X11Colors.getX11Color("Cornsilk"), X11Colors.getX11Color("Blanched Almond")};

	//wire fill
	private static Color wireFill = Color.white;

	/** assumed half radial width of gap */
	public static final double LAYERDR = 1.6; // mm;

	/** assumed half radial width of gap */
	public static final double WIRERAD = 0.5*LAYERDR; // mm;

	/** the 0-based sector of this layer */
	public final int sector;

	/** the 0-based superlayer of this layer */
	public final int superlayer;

	/** the 0-based layer ID of this layer */
	public final int layer;

	/** the number of wires */
	public final int numWires;

	/** the array of wires */
	public final Line3D wires[];

	//for shell outline
	private AlertLayerDonut _donut;

	//for wire outline
	private Rectangle2D.Double _wrect[];

	//workspace

/**
 * Create a DC layer from an alert DC layer
 * @param geoAlertDCLayer the geoDatabase object
 */
	public DCLayer(AlertDCLayer geoAlertDCLayer) {
		sector = geoAlertDCLayer.getSectorId()-1;
		superlayer = geoAlertDCLayer.getSuperlayerId()-1;
		layer = geoAlertDCLayer.getLayerId()-1;

		numWires = geoAlertDCLayer.getNumComponents();


		List<AlertDCWire> wireList = geoAlertDCLayer.getAllComponents();

		wires = new Line3D[numWires];

		int index = 0;


		for (AlertDCWire aw : wireList) {
			wires[index] = aw.getLine();
			index++;
		}

		_wrect = new Rectangle2D.Double[numWires];
 		for (int wire = 0; wire < numWires; wire++) {
			_wrect[wire] = new Rectangle2D.Double();
		}
	}



	/**
	 * Get the 3D coords of the wire used in 3D drawing
	 * @param wire the 0-based wire id
	 * @param coords the 3D coords
	 */
	public void getWireCoords(int wire, float coords[]) {

		Point3D p0 = wires[wire].origin();
		Point3D p1 = wires[wire].end();
		coords[0] = (float) p0.x();
		coords[1] = (float) p0.y();
		coords[2] = (float) p0.z();
		coords[3] = (float) p1.x();
		coords[4] = (float) p1.y();
		coords[5] = (float) p1.z();
	}

	/**
	 * Get a wire as a 3D line
	 * @param wire the 0-based wire id
	 * @return the 3D line for the wire, or null
	 */
	public Line3D getLine(int wire) {
		if ((wire < 0) || (wire >= numWires)) {
			return null;
		}
		return wires[wire];
	}



	/**
	 * Contained in the XY view?
	 * @param wp the world point
	 * @return true if contained in this layer
	 */
	public boolean containsXY(Point pp) {
		if ((numWires == 0)|| (_donut == null) || (_donut.area == null)) {
			return false;
		}

		return _donut.area.contains(pp);
	}

	/**
	 * Point contained by the wire oval?
	 * @param wire the 0-based wire id
	 * @param wp the world point
	 * @return true if contained
	 */
	public boolean wireContainsXY(int wire, Point2D.Double wp) {
		if ((wire < 0) || (wire >= numWires)) {
			System.err.println("Bad wire id: " + wire);
			return false;
		}

		return _wrect[wire].contains(wp);
	}

	/**
	 * Basic fb string for XY view
	 * @param feedbackStrings list to add to
	 */
	public void feedbackXYString(Point pp, Point2D.Double wp, List<String> feedbackStrings) {
		feedbackStrings.add(String.format("DC sector: %d (1-based)", sector + 1));

		int supl1 = superlayer + 1;
		int lay1 = layer + 1;
		int compLayer = supl1 * 10 + lay1;

		feedbackStrings.add(String.format("DC superlayer: %d (1-based)", supl1));
		feedbackStrings.add(String.format("DC layer: %d (1-based)", lay1));
		feedbackStrings.add(String.format("DC hipo layer: %d", compLayer));

		if (numWires > 0) {
			for (int wire = 0; wire < numWires; wire++) {
				if (wireContainsXY(wire, wp)) {
					feedbackStrings.add(String.format("AlertDC wire: %d", wire + 1));
					return;
				}
			}
		}
	}

	/**
	 * Draw the layer shell as a donut
	 * @param g the graphics object
	 * @param container the drawing container
	 */
	private void drawShell(Graphics g, IContainer container) {
		if ((numWires == 0)|| (_donut == null) || (_donut.area == null)) {
			return;
		}

		Graphics2D g2d = (Graphics2D) g;

		Color fc = shellColors[layer];
		g2d.setColor(fc);
		g2d.fill(_donut.area);
		g2d.setColor(Color.gray);
		g2d.draw(_donut.area);
	}

	public double getWireXYatZ(int wire, double z, Point2D.Double xy) {
		Line3D line = wires[wire];
		Point3D p0 = line.origin();
		Point3D p1 = line.end();
		double t = (z - p0.z()) / (p1.z() - p0.z());
		xy.x = p0.x() + t * (p1.x() - p0.x());
		xy.y = p0.y() + t * (p1.y() - p0.y());
		return t;
	}


	//get the shell poly
	private void shellWorldPoly(IContainer container, double z) {
		_donut = new AlertLayerDonut(container, this, z);
	}

	/**
	 * Draw the wires
	 * @param g the graphics object
	 * @param container the drawing container
	 */
	public void drawXYWires(Graphics g, IContainer container, double z) {

		shellWorldPoly(container, z);
		//draw the poly
		drawShell(g, container);
		for (int wire = 0; wire < numWires; wire++) {
			drawXYWire(g, container, wire, wireFill, null, z);
		}
	}

	/**
	 * Draw a wire
	 *
	 * @param g         the graphics object
	 * @param container the drawing container
	 * @param wire      the 0-based wire id (data is 1-based!)
	 * @param fc        the fill color
	 * @param lc        the line color
	 * @param z         the z value
	 */
	public void drawXYWire(Graphics g, IContainer container, int wire, Color fc, Color lc,   double z) {
		drawXYWire(g, container, wire, fc, lc, z, false);
	}
	
	/**
	 * Draw a wire
	 *
	 * @param g         the graphics object
	 * @param container the drawing container
	 * @param wire      the 0-based wire id (data is 1-based!)
	 * @param fc        the fill color
	 * @param lc        the line color
	 * @param z         the z value
	 * @param isHit     true if this is a hit
	 */
	public void drawXYWire(Graphics g, IContainer container, int wire, Color fc, Color lc,   double z, boolean isHit) {
		if (numWires < 1) {
			return;
		}

		

		Point2D.Double zp = new Point2D.Double();
		double t = getWireXYatZ(wire, z, zp);


		if ((t < 0) || (t > 1)) {
			return;
		}

		if (wire == 0) {
			lc = X11Colors.getX11Color("Coral");
//			fc = X11Colors.getX11Color("Alice Blue");
		}
		
		double rad = isHit ? 1.2*WIRERAD : 1.2*WIRERAD;
		
		_wrect[wire].setFrame(zp.x-rad, zp.y-rad, 2*rad, 2*rad);
		WorldGraphicsUtilities.drawWorldOval(g, container, _wrect[wire], fc, lc);

	}



}
