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

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.ced.cedview.alert.AlertLayerDonut;
import cnuphys.ced.geometry.cache.GeometryPrimitiveIO;

/**
 * Runtime geometry and drawing support for one ALERT drift-chamber layer.
 * <p>
 * The geometry address is stored 0-based:
 *
 * <pre>
 * sector:     0..
 * superlayer: 0..
 * layer:      0..
 * wire:       0..
 * </pre>
 *
 * The runtime representation still uses {@link Line3D} for convenience, but the
 * cache representation is explicit primitive wire endpoint data rather than
 * Kryo-serialized object graphs.
 */
public class DCLayer {

	/** Number of endpoints in one wire line. */
	private static final int LINE_ENDPOINT_COUNT = 2;

	/** Number of coordinates per endpoint. */
	private static final int COORD_COUNT = 3;

	/** Layer shell colors. */
	private static Color[] shellColors = { X11Colors.getX11Color("Cornsilk"),
			X11Colors.getX11Color("Blanched Almond") };

	/** Wire fill color. */
	private static Color wireFill = Color.white;

	/** Assumed half radial width of gap, in mm. */
	public static final double LAYERDR = 1.6;

	/** Assumed wire radius, in mm. */
	public static final double WIRERAD = 0.5 * LAYERDR;

	/** The 0-based sector of this layer. */
	public final int sector;

	/** The 0-based superlayer of this layer. */
	public final int superlayer;

	/** The 0-based layer id of this layer. */
	public final int layer;

	/** Number of wires. */
	public final int numWires;

	/** Wire lines. */
	public final Line3D wires[];

	/** Shell outline. */
	private AlertLayerDonut _donut;

	/** Wire hit rectangles for feedback. */
	private Rectangle2D.Double _wrect[];

	/**
	 * Create a DC layer from the geometry service object.
	 *
	 * @param geoAlertDCLayer the geometry-service ALERT DC layer
	 */
	public DCLayer(AlertDCLayer geoAlertDCLayer) {
		sector = geoAlertDCLayer.getSectorId() - 1;
		superlayer = geoAlertDCLayer.getSuperlayerId() - 1;
		layer = geoAlertDCLayer.getLayerId() - 1;

		numWires = geoAlertDCLayer.getNumComponents();
		List<AlertDCWire> wireList = geoAlertDCLayer.getAllComponents();

		wires = new Line3D[numWires];

		int index = 0;
		for (AlertDCWire aw : wireList) {
			wires[index] = aw.getLine();
			index++;
		}

		createWorkRectangles();
	}

	/**
	 * Create a DC layer from primitive wire-line data read from the cache.
	 *
	 * @param sector     the 0-based sector
	 * @param superlayer the 0-based superlayer
	 * @param layer      the 0-based layer
	 * @param wireLines  wire line data shaped [wire][endpoint][xyz]
	 */
	private DCLayer(int sector, int superlayer, int layer, double wireLines[][][]) {
		this.sector = sector;
		this.superlayer = superlayer;
		this.layer = layer;

		numWires = (wireLines == null) ? 0 : wireLines.length;
		wires = new Line3D[numWires];

		for (int wire = 0; wire < numWires; wire++) {
			double line[][] = wireLines[wire];
			validateLine(line, "ALERT DC sector " + sector + " superlayer " + superlayer + " layer " + layer
					+ " wire " + wire);

			Point3D p0 = new Point3D(line[0][0], line[0][1], line[0][2]);
			Point3D p1 = new Point3D(line[1][0], line[1][1], line[1][2]);
			wires[wire] = new Line3D(p0, p1);
		}

		createWorkRectangles();
	}

	/**
	 * Create feedback rectangles.
	 */
	private void createWorkRectangles() {
		_wrect = new Rectangle2D.Double[numWires];
		for (int wire = 0; wire < numWires; wire++) {
			_wrect[wire] = new Rectangle2D.Double();
		}
	}

	/**
	 * Get the 3D coordinates of a wire for 3D drawing.
	 *
	 * @param wire   the 0-based wire id
	 * @param coords receives [x0, y0, z0, x1, y1, z1]
	 */
	public void getWireCoords(int wire, float coords[]) {
		if ((wire < 0) || (wire >= numWires) || (coords == null) || (coords.length < 6)) {
			return;
		}

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
	 * Get a wire as a 3D line.
	 *
	 * @param wire the 0-based wire id
	 * @return the wire line, or {@code null}
	 */
	public Line3D getLine(int wire) {
		if ((wire < 0) || (wire >= numWires)) {
			return null;
		}
		return wires[wire];
	}

	/**
	 * Is a screen point contained in this layer shell?
	 *
	 * @param pp the pixel point
	 * @return {@code true} if contained
	 */
	public boolean containsXY(Point pp) {
		if ((numWires == 0) || (_donut == null) || (_donut.area == null)) {
			return false;
		}

		return _donut.area.contains(pp);
	}

	/**
	 * Is a world point contained by the wire oval?
	 *
	 * @param wire the 0-based wire id
	 * @param wp   the world point
	 * @return {@code true} if contained
	 */
	public boolean wireContainsXY(int wire, Point2D.Double wp) {
		if ((wire < 0) || (wire >= numWires)) {
			System.err.println("Bad wire id: " + wire);
			return false;
		}

		return _wrect[wire].contains(wp);
	}

	/**
	 * Add basic feedback strings for the XY view.
	 *
	 * @param pp              the pixel point
	 * @param wp              the world point
	 * @param feedbackStrings the feedback list
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
	 * Draw the layer shell as a donut.
	 *
	 * @param g         the graphics context
	 * @param container the drawing container
	 */
	private void drawShell(Graphics g, IContainer container) {
		if ((numWires == 0) || (_donut == null) || (_donut.area == null)) {
			return;
		}

		Graphics2D g2d = (Graphics2D) g;

		Color fc = shellColors[layer % shellColors.length];
		g2d.setColor(fc);
		g2d.fill(_donut.area);
		g2d.setColor(Color.gray);
		g2d.draw(_donut.area);
	}

	/**
	 * Get the XY location of a wire at a given z.
	 *
	 * @param wire the 0-based wire id
	 * @param z    the z coordinate
	 * @param xy   receives the XY point
	 * @return interpolation parameter along the wire
	 */
	public double getWireXYatZ(int wire, double z, Point2D.Double xy) {
		Line3D line = wires[wire];
		Point3D p0 = line.origin();
		Point3D p1 = line.end();

		double t = (z - p0.z()) / (p1.z() - p0.z());
		xy.x = p0.x() + t * (p1.x() - p0.x());
		xy.y = p0.y() + t * (p1.y() - p0.y());

		return t;
	}

	/**
	 * Compute the shell polygon.
	 *
	 * @param container the drawing container
	 * @param z         the z coordinate
	 */
	private void shellWorldPoly(IContainer container, double z) {
		_donut = new AlertLayerDonut(container, this, z);
	}

	/**
	 * Draw all wires in the XY view.
	 *
	 * @param g         the graphics context
	 * @param container the drawing container
	 * @param z         the z coordinate
	 */
	public void drawXYWires(Graphics g, IContainer container, double z) {
		shellWorldPoly(container, z);
		drawShell(g, container);

		for (int wire = 0; wire < numWires; wire++) {
			drawXYWire(g, container, wire, wireFill, null, z);
		}
	}

	/**
	 * Draw one wire.
	 *
	 * @param g         the graphics context
	 * @param container the drawing container
	 * @param wire      the 0-based wire id
	 * @param fc        fill color
	 * @param lc        line color
	 * @param z         the z coordinate
	 */
	public void drawXYWire(Graphics g, IContainer container, int wire, Color fc, Color lc, double z) {
		drawXYWire(g, container, wire, fc, lc, z, false);
	}

	/**
	 * Draw one wire.
	 *
	 * @param g         the graphics context
	 * @param container the drawing container
	 * @param wire      the 0-based wire id
	 * @param fc        fill color
	 * @param lc        line color
	 * @param z         the z coordinate
	 * @param isHit     whether this wire is a hit
	 */
	public void drawXYWire(Graphics g, IContainer container, int wire, Color fc, Color lc, double z, boolean isHit) {
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
		}

		double rad = isHit ? 1.2 * WIRERAD : 1.2 * WIRERAD;

		_wrect[wire].setFrame(zp.x - rad, zp.y - rad, 2 * rad, 2 * rad);
		WorldGraphicsUtilities.drawWorldOval(g, container, _wrect[wire], fc, lc);
	}

	/**
	 * Write this layer to the geometry cache using primitive wire endpoint data.
	 *
	 * @param output the cache output stream
	 */
	public void writeToCache(Output output) {
		output.writeInt(sector);
		output.writeInt(superlayer);
		output.writeInt(layer);
		output.writeInt(numWires);

		for (int wire = 0; wire < numWires; wire++) {
			GeometryPrimitiveIO.writeCorners(output, lineToPrimitive(wires[wire]), LINE_ENDPOINT_COUNT, COORD_COUNT);
		}
	}

	/**
	 * Read a DC layer from the geometry cache.
	 *
	 * @param input the cache input stream
	 * @return the reconstructed DC layer
	 */
	public static DCLayer readFromCache(Input input) {
		int sector = input.readInt();
		int superlayer = input.readInt();
		int layer = input.readInt();
		int numWires = input.readInt();

		if (numWires < 0) {
			throw new IllegalArgumentException("ALERT DC: negative wire count in cache.");
		}

		double wireLines[][][] = new double[numWires][][];

		for (int wire = 0; wire < numWires; wire++) {
			wireLines[wire] = GeometryPrimitiveIO.readCorners(input, LINE_ENDPOINT_COUNT, COORD_COUNT,
					"ALERT DC sector " + sector + " superlayer " + superlayer + " layer " + layer + " wire " + wire);
		}

		return new DCLayer(sector, superlayer, layer, wireLines);
	}

	/**
	 * Convert a line to primitive endpoint data.
	 *
	 * @param line the line
	 * @return endpoint data shaped [2][3]
	 */
	private static double[][] lineToPrimitive(Line3D line) {
		if (line == null) {
			throw new IllegalArgumentException("ALERT DC: cannot cache null wire line.");
		}

		double primitive[][] = new double[LINE_ENDPOINT_COUNT][COORD_COUNT];

		primitive[0][0] = line.origin().x();
		primitive[0][1] = line.origin().y();
		primitive[0][2] = line.origin().z();

		primitive[1][0] = line.end().x();
		primitive[1][1] = line.end().y();
		primitive[1][2] = line.end().z();

		return primitive;
	}

	/**
	 * Validate primitive line endpoint data.
	 *
	 * @param line    the primitive line
	 * @param context context for error messages
	 */
	private static void validateLine(double line[][], String context) {
		if ((line == null) || (line.length != LINE_ENDPOINT_COUNT)) {
			throw new IllegalArgumentException(context + ": invalid endpoint count.");
		}

		for (int endpoint = 0; endpoint < LINE_ENDPOINT_COUNT; endpoint++) {
			if ((line[endpoint] == null) || (line[endpoint].length != COORD_COUNT)) {
				throw new IllegalArgumentException(context + ": invalid coordinate count at endpoint " + endpoint);
			}
		}
	}
}