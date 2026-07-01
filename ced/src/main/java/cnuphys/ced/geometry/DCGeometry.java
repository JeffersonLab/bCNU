package cnuphys.ced.geometry;

import java.awt.geom.Point2D;

import org.jlab.detector.base.GeometryFactory;
import org.jlab.detector.geom.dc.DCGeantFactory;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.component.DriftChamberWire;
import org.jlab.geom.detector.dc.DCDetector;
import org.jlab.geom.detector.dc.DCLayer;
import org.jlab.geom.detector.dc.DCSector;
import org.jlab.geom.detector.dc.DCSuperlayer;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Triangle3D;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import cnuphys.ced.frame.Ced;
import cnuphys.ced.geometry.cache.ACachedGeometry;
import cnuphys.ced.geometry.cache.GeometryPrimitiveIO;

/**
 * Holds the drift chamber geometry used by the event display.
 * <p>
 * The old cache stored the full {@link DriftChamberWire} object graph. That is
 * unnecessary after initialization. The display code needs only:
 *
 * <pre>
 * sense wire line endpoints  [superlayer][layer][wire][endpoint][xyz]
 * midpoints                  [superlayer][layer][wire][xyz]
 * hexagon projection edges   [superlayer][layer][wire][edge][endpoint][xyz]
 * min/max wire x
 * </pre>
 *
 * The full {@link DriftChamberWire} objects are therefore used only while
 * building the geometry from CCDB.
 */
public class DCGeometry extends ACachedGeometry {

	/** Number of DC superlayers. */
	private static final int SUPERLAYER_COUNT = 6;

	/** Number of layers per superlayer. */
	private static final int LAYER_COUNT = 6;

	/** Number of wires per layer. */
	private static final int WIRE_COUNT = 112;

	/** Number of endpoints in a line. */
	private static final int LINE_ENDPOINT_COUNT = 2;

	/** Number of coordinates per point. */
	private static final int COORD_COUNT = 3;

	/**
	 * Old DC display code projected six volume edges starting at edge 10. Preserve
	 * that behavior.
	 */
	private static final int HEX_EDGE_START = 10;

	/** Number of projected hexagon edges. */
	private static final int HEX_EDGE_COUNT = 6;

	private static double minWireX;
	private static double maxWireX;

	/**
	 * Primitive sense-wire lines shaped
	 * [superlayer][layer][wire][endpoint][xyz].
	 */
	private static double wireLines[][][][][];

	/**
	 * Primitive wire midpoints shaped [superlayer][layer][wire][xyz].
	 */
	private static double wireMidpoints[][][][];

	/**
	 * Primitive hexagon projection edges shaped
	 * [superlayer][layer][wire][edge][endpoint][xyz].
	 */
	private static double hexEdges[][][][][][];

	/**
	 * Constructor.
	 */
	public DCGeometry() {
		super("DriftChamber");
	}

	/**
	 * Initialize the DC geometry by loading all wires from CCDB/JLab geometry and
	 * converting the needed runtime data to primitive arrays.
	 */
	@Override
	public void initializeUsingCCDB() {
		int run = 4013;
		String variation = Ced.getGeometryVariation();
		ConstantProvider cp = GeometryFactory.getConstants(org.jlab.detector.base.DetectorType.DC, run, variation);

		DCGeantFactory factory = new DCGeantFactory();

		DCDetector dcDetector = factory.createDetectorCLAS(cp);
		DCSector sector0 = dcDetector.getSector(0);

		minWireX = Double.POSITIVE_INFINITY;
		maxWireX = Double.NEGATIVE_INFINITY;

		wireLines = new double[SUPERLAYER_COUNT][LAYER_COUNT][WIRE_COUNT][LINE_ENDPOINT_COUNT][COORD_COUNT];
		wireMidpoints = new double[SUPERLAYER_COUNT][LAYER_COUNT][WIRE_COUNT][COORD_COUNT];
		hexEdges = new double[SUPERLAYER_COUNT][LAYER_COUNT][WIRE_COUNT][HEX_EDGE_COUNT][LINE_ENDPOINT_COUNT][COORD_COUNT];

		for (int suplay = 0; suplay < SUPERLAYER_COUNT; suplay++) {
			DCSuperlayer sl = sector0.getSuperlayer(suplay);

			for (int lay = 0; lay < LAYER_COUNT; lay++) {
				DCLayer dcLayer = sl.getLayer(lay);

				for (int w = 0; w < WIRE_COUNT; w++) {
					DriftChamberWire dcw = dcLayer.getComponent(w);
					cacheWire(suplay, lay, w, dcw);
				}
			}
		}
	}

	/**
	 * Cache one drift chamber wire as primitive data.
	 *
	 * @param suplay superlayer index
	 * @param lay    layer index
	 * @param w      wire index
	 * @param dcw    drift chamber wire
	 */
	private static void cacheWire(int suplay, int lay, int w, DriftChamberWire dcw) {
		Line3D line = dcw.getLine();

		storePoint(wireLines[suplay][lay][w][0], line.origin());
		storePoint(wireLines[suplay][lay][w][1], line.end());

		Point3D midpoint = dcw.getMidpoint();
		storePoint(wireMidpoints[suplay][lay][w], midpoint);

		double xx0 = line.origin().x();
		double xx1 = line.end().x();

		minWireX = Math.min(minWireX, xx0);
		minWireX = Math.min(minWireX, xx1);
		maxWireX = Math.max(maxWireX, xx0);
		maxWireX = Math.max(maxWireX, xx1);

		for (int edge = 0; edge < HEX_EDGE_COUNT; edge++) {
			Line3D edgeLine = dcw.getVolumeEdge(HEX_EDGE_START + edge);
			storePoint(hexEdges[suplay][lay][w][edge][0], edgeLine.origin());
			storePoint(hexEdges[suplay][lay][w][edge][1], edgeLine.end());
		}
	}

	/**
	 * Store a Point3D in a primitive coordinate array.
	 *
	 * @param target destination [x,y,z]
	 * @param point  source point
	 */
	private static void storePoint(double target[], Point3D point) {
		target[0] = point.x();
		target[1] = point.y();
		target[2] = point.z();
	}

	/**
	 * Used by the 3D drawing.
	 *
	 * @param sector     the 1-based sector
	 * @param superlayer 1-based superlayer [1..6]
	 * @param coords     holds 6*3 = 18 values [x1,y1,z1,...]
	 */
	public static void superLayerVertices(int sector, int superlayer, float[] coords) {
		Line3D wire1 = getWire(sector, superlayer, 1, 1);
		Line3D wire2 = getWire(sector, superlayer, 1, 112);

		Line3D wire3 = getWire(sector, superlayer, 6, 1);
		Line3D wire4 = getWire(sector, superlayer, 6, 112);

		Triangle3D triangle1 = new Triangle3D(wire1.midpoint(), wire2.origin(), wire2.end());
		Triangle3D triangle6 = new Triangle3D(wire3.midpoint(), wire4.origin(), wire4.end());

		if (triangle1 != null) {
			for (int i = 0; i < 3; i++) {
				Point3D v1 = new Point3D(triangle1.point(i));
				Point3D v6 = new Point3D(triangle6.point(i));

				int j = 3 * i;
				int k = j + 9;

				coords[j] = (float) v1.x();
				coords[j + 1] = (float) v1.y();
				coords[j + 2] = (float) v1.z();

				coords[k] = (float) v6.x();
				coords[k + 1] = (float) v6.y();
				coords[k + 2] = (float) v6.z();
			}
		}
	}

	/**
	 * Get the absolute value of the largest x coordinate of any wire.
	 *
	 * @return the absolute value of the largest x coordinate of any wire
	 */
	public static double getAbsMaxWireX() {
		return Math.max(Math.abs(minWireX), maxWireX);
	}

	/**
	 * Get the midpoint of the untransformed wire in sector 1.
	 *
	 * @param superlayer superlayer [1..6]
	 * @param layer      layer [1..6]
	 * @param wire       wire [1..112]
	 * @return midpoint
	 */
	public static Point3D getMidPoint(int superlayer, int layer, int wire) {
		if (!validAddress(superlayer, layer, wire)) {
			return null;
		}

		return pointFrom(wireMidpoints[superlayer - 1][layer - 1][wire - 1]);
	}

	/**
	 * Get the sense wire in a given sector.
	 *
	 * @param sector     sector [1..6]
	 * @param superlayer superlayer [1..6]
	 * @param layer      layer [1..6]
	 * @param wire       wire [1..112]
	 * @return transformed sense-wire line
	 */
	public static Line3D getWire(int sector, int superlayer, int layer, int wire) {
		Line3D line = getWire(superlayer, layer, wire);

		if ((line != null) && (sector > 1)) {
			line.rotateZ(Math.toRadians(60 * (sector - 1)));
		}

		return line;
	}

	/**
	 * Get the untransformed sense wire in sector 1.
	 * <p>
	 * Historically this method returned a {@link DriftChamberWire}. No outside
	 * callers use that return type, and after a cache read the full
	 * DriftChamberWire object is intentionally not retained.
	 *
	 * @param superlayer superlayer [1..6]
	 * @param layer      layer [1..6]
	 * @param wire       wire [1..112]
	 * @return untransformed sense-wire line
	 */
	public static Line3D getWire(int superlayer, int layer, int wire) {
		if (!validAddress(superlayer, layer, wire)) {
			return null;
		}

		return lineFrom(wireLines[superlayer - 1][layer - 1][wire - 1]);
	}

	/**
	 * Get the origin of the wire in sector 1.
	 *
	 * @param superlayer superlayer [1..6]
	 * @param layer      layer [1..6]
	 * @param wire       wire [1..112]
	 * @return origin
	 */
	public static Point3D getOrigin(int superlayer, int layer, int wire) {
		if (!validAddress(superlayer, layer, wire)) {
			return null;
		}

		return pointFrom(wireLines[superlayer - 1][layer - 1][wire - 1][0]);
	}

	/**
	 * Get the end of the wire in sector 1.
	 *
	 * @param superlayer superlayer [1..6]
	 * @param layer      layer [1..6]
	 * @param wire       wire [1..112]
	 * @return end point
	 */
	public static Point3D getEnd(int superlayer, int layer, int wire) {
		if (!validAddress(superlayer, layer, wire)) {
			return null;
		}

		return pointFrom(wireLines[superlayer - 1][layer - 1][wire - 1][1]);
	}

	/**
	 * Get the projected hexagon of a wire on a constant-phi plane.
	 *
	 * @param superlayer      superlayer [1..6]
	 * @param layer           layer [1..6]
	 * @param wire            wire [1..112]
	 * @param projectionPlane projection plane
	 * @param wp              projected polygon
	 * @param centroid        optional centroid
	 * @return true if projection succeeded
	 */
	public static boolean getHexagon(int superlayer, int layer, int wire, Plane3D projectionPlane, Point2D.Double wp[],
			Point2D.Double centroid) {
		if (!validAddress(superlayer, layer, wire)) {
			return false;
		}

		return GeometryManager.getProjectedPolygon(hexEdges[superlayer - 1][layer - 1][wire - 1], projectionPlane, wp,
				centroid, false);
	}

	/**
	 * Get the approximate center of the projected hexagon.
	 *
	 * @param superlayer      superlayer [1..6]
	 * @param layer           layer [1..6]
	 * @param wire            wire [1..112]
	 * @param projectionPlane projection plane
	 * @return approximate center
	 */
	public static Point2D.Double getCenter(int superlayer, int layer, int wire, Plane3D projectionPlane) {
		Point2D.Double centroid = new Point2D.Double();

		Line3D l3D = getWire(superlayer, layer, wire);
		Point3D p3 = new Point3D();
		projectionPlane.intersection(l3D, p3);

		centroid.x = p3.z();
		centroid.y = Math.hypot(p3.x(), p3.y());

		return centroid;
	}

	/**
	 * Get one point on either side of a layer.
	 *
	 * @param superLayer      superlayer [1..6]
	 * @param layer           layer [1..6]
	 * @param projectionPlane projection plane
	 * @param wp              returns two extended points
	 */
	public static void getLayerExtendedPoints(int superLayer, int layer, Plane3D projectionPlane, Point2D.Double wp[]) {
		Point2D.Double hexagon[] = GeometryManager.allocate(6);

		getHexagon(superLayer, layer, 1, projectionPlane, hexagon, null);
		Point2D.Double first = new Point2D.Double(hexagon[0].x, hexagon[0].y);

		getHexagon(superLayer, layer, 2, projectionPlane, hexagon, null);
		Point2D.Double second = new Point2D.Double(hexagon[0].x, hexagon[0].y);

		getHexagon(superLayer, layer, 111, projectionPlane, hexagon, null);
		Point2D.Double nexttolast = new Point2D.Double(hexagon[0].x, hexagon[0].y);

		getHexagon(superLayer, layer, 112, projectionPlane, hexagon, null);
		Point2D.Double last = new Point2D.Double(hexagon[0].x, hexagon[0].y);

		extPoint(first, second, wp[0]);
		extPoint(last, nexttolast, wp[1]);
	}

	/**
	 * Get the boundary of a layer.
	 *
	 * @param superLayer superlayer [1..6]
	 * @param layer      layer [1..6]
	 * @param plane      projection plane
	 * @param wp         layer boundary points
	 */
	public static void getLayerPolygon(int superLayer, int layer, Plane3D plane, Point2D.Double wp[]) {
		Point2D.Double hex[] = GeometryManager.allocate(6);

		int firstWire = 1;
		while ((firstWire < 112) && !getHexagon(superLayer, layer, firstWire, plane, hex, null)) {
			firstWire++;
		}

		getHexagon(superLayer, layer, 1, plane, hex, null);

		/*
		 * The mappings of the old geo hex indices to the new is 0 --> 5 1 --> 4
		 * 2 --> 3 3 --> 2 4 --> 1 5 --> 0
		 */

		assignFromHex(wp, 0, hex, 5);
		assignFromHex(wp, 11, hex, 2);
		assignFromHex(wp, 12, hex, 1);
		assignFromHex(wp, 13, hex, 0);

		int sindex = Math.max(13, firstWire + 8);
		getHexagon(superLayer, layer, sindex, plane, hex, null);

		assignFromHex(wp, 1, hex, 5);
		assignFromHex(wp, 10, hex, 2);

		sindex = Math.max(57, sindex + 12);
		getHexagon(superLayer, layer, 57, plane, hex, null);

		assignFromHex(wp, 2, hex, 5);
		assignFromHex(wp, 9, hex, 2);

		sindex = Math.max(99, sindex + 29);
		getHexagon(superLayer, layer, 99, plane, hex, null);

		assignFromHex(wp, 3, hex, 5);
		assignFromHex(wp, 8, hex, 2);

		getHexagon(superLayer, layer, 112, plane, hex, null);

		assignFromHex(wp, 4, hex, 5);
		assignFromHex(wp, 5, hex, 4);
		assignFromHex(wp, 6, hex, 3);
		assignFromHex(wp, 7, hex, 2);
	}

	/**
	 * Get the boundary of a superlayer.
	 *
	 * @param superLayer      superlayer [1..6]
	 * @param projectionPlane projection plane
	 * @param wp              superlayer boundary points
	 */
	public static void getSuperLayerPolygon(int superLayer, Plane3D projectionPlane, Point2D.Double wp[]) {
		Point2D.Double layBoundry[] = GeometryManager.allocate(14);

		getLayerPolygon(superLayer, 1, projectionPlane, layBoundry);
		wp[0].setLocation(layBoundry[12]);
		wp[1].setLocation(layBoundry[13]);
		wp[2].setLocation(layBoundry[0]);
		wp[3].setLocation(layBoundry[1]);
		wp[4].setLocation(layBoundry[2]);
		wp[5].setLocation(layBoundry[3]);
		wp[6].setLocation(layBoundry[4]);
		wp[7].setLocation(layBoundry[5]);
		wp[8].setLocation(layBoundry[6]);

		getLayerPolygon(superLayer, 2, projectionPlane, layBoundry);
		wp[9].setLocation(layBoundry[5]);
		wp[10].setLocation(layBoundry[6]);
		wp[32].setLocation(layBoundry[12]);
		wp[33].setLocation(layBoundry[13]);

		getLayerPolygon(superLayer, 3, projectionPlane, layBoundry);
		wp[11].setLocation(layBoundry[5]);
		wp[12].setLocation(layBoundry[6]);
		wp[30].setLocation(layBoundry[12]);
		wp[31].setLocation(layBoundry[13]);

		getLayerPolygon(superLayer, 4, projectionPlane, layBoundry);
		wp[13].setLocation(layBoundry[5]);
		wp[14].setLocation(layBoundry[6]);
		wp[28].setLocation(layBoundry[12]);
		wp[29].setLocation(layBoundry[13]);

		getLayerPolygon(superLayer, 5, projectionPlane, layBoundry);
		wp[15].setLocation(layBoundry[5]);
		wp[16].setLocation(layBoundry[6]);
		wp[26].setLocation(layBoundry[12]);
		wp[27].setLocation(layBoundry[13]);

		getLayerPolygon(superLayer, 6, projectionPlane, layBoundry);
		wp[17].setLocation(layBoundry[5]);
		wp[18].setLocation(layBoundry[6]);
		wp[19].setLocation(layBoundry[7]);
		wp[20].setLocation(layBoundry[8]);
		wp[21].setLocation(layBoundry[9]);
		wp[22].setLocation(layBoundry[10]);
		wp[23].setLocation(layBoundry[11]);
		wp[24].setLocation(layBoundry[12]);
		wp[25].setLocation(layBoundry[13]);
	}

	private static void assignFromHex(Point2D.Double wp[], int wpIndex, Point2D.Double hex[], int hexIndex) {
		hexIndex = hexIndex % 6;
		Point2D.Double p = new Point2D.Double(hex[hexIndex].x, hex[hexIndex].y);
		wp[wpIndex] = p;
	}

	private static void extPoint(Point2D.Double p0, Point2D.Double p1, Point2D.Double ext) {
		if ((p0 == null) || (p1 == null) || (ext == null)) {
			System.err.println("null point in DCGeometry.extPoint");
			return;
		}

		ext.x = p0.x + (p0.x - p1.x);
		ext.y = p0.y + (p0.y - p1.y);
	}

	/**
	 * Read DC geometry as explicit primitive cache data.
	 *
	 * @param kryo  retained for interface compatibility
	 * @param input cache input
	 * @return true if successful
	 */
	@Override
	public boolean readGeometry(Kryo kryo, Input input) {
		try {
			minWireX = input.readDouble();
			maxWireX = input.readDouble();

			wireLines = readWireLines(input);
			wireMidpoints = readWireMidpoints(input);
			hexEdges = readHexEdges(input);

			return true;
		} catch (Exception e) {
			System.err.println("DCGeometry: Error reading geometry cache: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Write DC geometry as explicit primitive cache data.
	 *
	 * @param kryo   retained for interface compatibility
	 * @param output cache output
	 * @return true if successful
	 */
	@Override
	public boolean writeGeometry(Kryo kryo, Output output) {
		try {
			output.writeDouble(minWireX);
			output.writeDouble(maxWireX);

			writeWireLines(output);
			writeWireMidpoints(output);
			writeHexEdges(output);

			return true;
		} catch (Exception e) {
			System.err.println("DCGeometry: Error writing geometry cache: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Write sense-wire lines.
	 *
	 * @param output cache output
	 */
	private static void writeWireLines(Output output) {
		output.writeInt(SUPERLAYER_COUNT);
		output.writeInt(LAYER_COUNT);
		output.writeInt(WIRE_COUNT);

		for (int suplay = 0; suplay < SUPERLAYER_COUNT; suplay++) {
			for (int lay = 0; lay < LAYER_COUNT; lay++) {
				for (int w = 0; w < WIRE_COUNT; w++) {
					GeometryPrimitiveIO.writeCorners(output, wireLines[suplay][lay][w], LINE_ENDPOINT_COUNT,
							COORD_COUNT);
				}
			}
		}
	}

	/**
	 * Read sense-wire lines.
	 *
	 * @param input cache input
	 * @return wire lines
	 */
	private static double[][][][][] readWireLines(Input input) {
		int superlayerCount = input.readInt();
		int layerCount = input.readInt();
		int wireCount = input.readInt();

		require(superlayerCount == SUPERLAYER_COUNT, "DC wire line superlayer count mismatch.");
		require(layerCount == LAYER_COUNT, "DC wire line layer count mismatch.");
		require(wireCount == WIRE_COUNT, "DC wire line wire count mismatch.");

		double data[][][][][] = new double[SUPERLAYER_COUNT][LAYER_COUNT][WIRE_COUNT][][];

		for (int suplay = 0; suplay < SUPERLAYER_COUNT; suplay++) {
			for (int lay = 0; lay < LAYER_COUNT; lay++) {
				for (int w = 0; w < WIRE_COUNT; w++) {
					data[suplay][lay][w] = GeometryPrimitiveIO.readCorners(input, LINE_ENDPOINT_COUNT, COORD_COUNT,
							"DC wire line sl " + suplay + " layer " + lay + " wire " + w);
				}
			}
		}

		return data;
	}

	/**
	 * Write wire midpoints.
	 *
	 * @param output cache output
	 */
	private static void writeWireMidpoints(Output output) {
		output.writeInt(SUPERLAYER_COUNT);
		output.writeInt(LAYER_COUNT);
		output.writeInt(WIRE_COUNT);

		for (int suplay = 0; suplay < SUPERLAYER_COUNT; suplay++) {
			for (int lay = 0; lay < LAYER_COUNT; lay++) {
				for (int w = 0; w < WIRE_COUNT; w++) {
					writePoint(output, wireMidpoints[suplay][lay][w]);
				}
			}
		}
	}

	/**
	 * Read wire midpoints.
	 *
	 * @param input cache input
	 * @return wire midpoints
	 */
	private static double[][][][] readWireMidpoints(Input input) {
		int superlayerCount = input.readInt();
		int layerCount = input.readInt();
		int wireCount = input.readInt();

		require(superlayerCount == SUPERLAYER_COUNT, "DC midpoint superlayer count mismatch.");
		require(layerCount == LAYER_COUNT, "DC midpoint layer count mismatch.");
		require(wireCount == WIRE_COUNT, "DC midpoint wire count mismatch.");

		double data[][][][] = new double[SUPERLAYER_COUNT][LAYER_COUNT][WIRE_COUNT][COORD_COUNT];

		for (int suplay = 0; suplay < SUPERLAYER_COUNT; suplay++) {
			for (int lay = 0; lay < LAYER_COUNT; lay++) {
				for (int w = 0; w < WIRE_COUNT; w++) {
					readPoint(input, data[suplay][lay][w]);
				}
			}
		}

		return data;
	}

	/**
	 * Write hexagon projection edges.
	 *
	 * @param output cache output
	 */
	private static void writeHexEdges(Output output) {
		output.writeInt(SUPERLAYER_COUNT);
		output.writeInt(LAYER_COUNT);
		output.writeInt(WIRE_COUNT);
		output.writeInt(HEX_EDGE_COUNT);

		for (int suplay = 0; suplay < SUPERLAYER_COUNT; suplay++) {
			for (int lay = 0; lay < LAYER_COUNT; lay++) {
				for (int w = 0; w < WIRE_COUNT; w++) {
					for (int edge = 0; edge < HEX_EDGE_COUNT; edge++) {
						GeometryPrimitiveIO.writeCorners(output, hexEdges[suplay][lay][w][edge],
								LINE_ENDPOINT_COUNT, COORD_COUNT);
					}
				}
			}
		}
	}

	/**
	 * Read hexagon projection edges.
	 *
	 * @param input cache input
	 * @return hexagon edge data
	 */
	private static double[][][][][][] readHexEdges(Input input) {
		int superlayerCount = input.readInt();
		int layerCount = input.readInt();
		int wireCount = input.readInt();
		int edgeCount = input.readInt();

		require(superlayerCount == SUPERLAYER_COUNT, "DC hex edge superlayer count mismatch.");
		require(layerCount == LAYER_COUNT, "DC hex edge layer count mismatch.");
		require(wireCount == WIRE_COUNT, "DC hex edge wire count mismatch.");
		require(edgeCount == HEX_EDGE_COUNT, "DC hex edge count mismatch.");

		double data[][][][][][] = new double[SUPERLAYER_COUNT][LAYER_COUNT][WIRE_COUNT][HEX_EDGE_COUNT][][];

		for (int suplay = 0; suplay < SUPERLAYER_COUNT; suplay++) {
			for (int lay = 0; lay < LAYER_COUNT; lay++) {
				for (int w = 0; w < WIRE_COUNT; w++) {
					for (int edge = 0; edge < HEX_EDGE_COUNT; edge++) {
						data[suplay][lay][w][edge] = GeometryPrimitiveIO.readCorners(input, LINE_ENDPOINT_COUNT,
								COORD_COUNT,
								"DC hex edge sl " + suplay + " layer " + lay + " wire " + w + " edge " + edge);
					}
				}
			}
		}

		return data;
	}

	/**
	 * Write a point.
	 *
	 * @param output cache output
	 * @param point  point data [x,y,z]
	 */
	private static void writePoint(Output output, double point[]) {
		output.writeDouble(point[0]);
		output.writeDouble(point[1]);
		output.writeDouble(point[2]);
	}

	/**
	 * Read a point.
	 *
	 * @param input  cache input
	 * @param target target [x,y,z]
	 */
	private static void readPoint(Input input, double target[]) {
		target[0] = input.readDouble();
		target[1] = input.readDouble();
		target[2] = input.readDouble();
	}

	/**
	 * Make a Point3D from primitive coordinates.
	 *
	 * @param coords [x,y,z]
	 * @return point
	 */
	private static Point3D pointFrom(double coords[]) {
		return new Point3D(coords[0], coords[1], coords[2]);
	}

	/**
	 * Make a Line3D from primitive endpoints.
	 *
	 * @param endpoints [2][xyz]
	 * @return line
	 */
	private static Line3D lineFrom(double endpoints[][]) {
		return new Line3D(pointFrom(endpoints[0]), pointFrom(endpoints[1]));
	}

	/**
	 * Validate a one-based DC address.
	 *
	 * @param superlayer superlayer [1..6]
	 * @param layer      layer [1..6]
	 * @param wire       wire [1..112]
	 * @return true if valid
	 */
	private static boolean validAddress(int superlayer, int layer, int wire) {
		if ((superlayer < 1) || (superlayer > SUPERLAYER_COUNT)) {
			System.err.println("BAD HIPO DATA DCGeometry.getWire superlayer must be [1..6], was " + superlayer);
			return false;
		}

		if ((layer < 1) || (layer > LAYER_COUNT)) {
			System.err.println("BAD HIPO DATA DCGeometry.getWire layer must be [1..6], was " + layer);
			return false;
		}

		if ((wire < 1) || (wire > WIRE_COUNT)) {
			System.err.println("BAD HIPO DATA DCGeometry.getWire wire must be [1..112], was " + wire);
			return false;
		}

		return true;
	}

	/**
	 * Require a condition.
	 *
	 * @param condition condition
	 * @param message   error message
	 */
	private static void require(boolean condition, String message) {
		if (!condition) {
			throw new IllegalArgumentException(message);
		}
	}
}