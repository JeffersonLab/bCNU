package cnuphys.ced.geometry.alert;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.jlab.detector.calib.utils.DatabaseConstantProvider;
import org.jlab.geom.component.ScintillatorPaddle;
import org.jlab.geom.detector.alert.AHDC.AlertDCDetector;
import org.jlab.geom.detector.alert.AHDC.AlertDCFactory;
import org.jlab.geom.detector.alert.ATOF.AlertTOFDetector;
import org.jlab.geom.detector.alert.ATOF.AlertTOFFactory;
import org.jlab.geom.detector.alert.ATOF.AlertTOFLayer;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.util.Fonts;
import cnuphys.ced.frame.Ced;
import cnuphys.ced.geometry.GeometryManager;
import cnuphys.ced.geometry.cache.ACachedGeometry;

/**
 * Geometry manager for the ALERT detector.
 * <p>
 * ALERT keeps CED-owned {@link DCLayer} and {@link TOFLayer} runtime objects so
 * the existing views can continue to use the same public API. The cache format,
 * however, is explicit primitive data written and read by those layer classes,
 * rather than Kryo-serialized layer maps.
 */
public class AlertGeometry extends ACachedGeometry {

	/** Debug flag. */
	private static boolean _debug = false;

	/** Detector name. */
	public static String NAME = "ALERT";

	/** Layer objects used for DC drawing, keyed by sector|superlayer|layer. */
	private static HashMap<String, DCLayer> _dcLayers = new HashMap<>();

	/** Layer objects used for TOF drawing, keyed by sector|superlayer|layer. */
	private static HashMap<String, TOFLayer> _tofLayers = new HashMap<>();

	/** Sector boundaries for XY view. */
	public static Point2D.Double tofSectorXY[][] = new Point2D.Double[15][16];

	/** Constant provider used during CCDB initialization. */
	private static DatabaseConstantProvider constantProvider;

	/**
	 * Constructor.
	 */
	public AlertGeometry() {
		super(NAME);
	}

	/**
	 * Initialize ALERT geometry from CCDB/JLab geometry services.
	 */
	@Override
	public void initializeUsingCCDB() {
		System.out.println("\n=======================================");
		System.out.println("===  " + NAME + " Geometry Initialization ===");
		System.out.println("=======================================");

		String variationName = Ced.getGeometryVariation();
		constantProvider = new DatabaseConstantProvider(11, variationName);

		_dcLayers = new HashMap<>();
		_tofLayers = new HashMap<>();

		initializeDC(constantProvider);
		initializeTOF(constantProvider);
	}

	/**
	 * Print a debug message.
	 *
	 * @param s      message
	 * @param option debug option
	 */
	private static void debugPrint(String s, int option) {
		if (_debug) {
			if (option == 0) {
				System.out.println("ALERT_DC  " + s);
			} else if (option == 1) {
				System.out.println("ALERT_TOF " + s);
			} else {
				System.out.println(s);
			}
		}
	}

	/**
	 * Initialize the drift-chamber geometry.
	 *
	 * @param cp constant provider
	 */
	private static void initializeDC(DatabaseConstantProvider cp) {
		AlertDCFactory dcFactory = new AlertDCFactory();
		AlertDCDetector dcCLASDetector = dcFactory.createDetectorCLAS(cp);

		int numsect = dcCLASDetector.getNumSectors();
		debugPrint(String.format("numsect: %d", numsect), 0);

		for (int sect = 0; sect < numsect; sect++) {
			debugPrint("", 2);
			debugPrint(String.format("  for sect: %d", sect), 0);

			int numsupl = dcFactory.createSector(cp, sect).getNumSuperlayers();
			debugPrint(String.format("  numsuperlayer: %d", numsupl), 0);

			for (int superlayer = 0; superlayer < numsupl; superlayer++) {
				debugPrint(String.format("    for superlayer: %d", superlayer), 0);

				int numlay = dcFactory.createSuperlayer(cp, sect, superlayer).getNumLayers();
				debugPrint(String.format("    numlayer: %d", numlay), 0);

				for (int layer = 0; layer < numlay; layer++) {
					DCLayer dcLayer = new DCLayer(dcFactory.createLayer(cp, sect, superlayer, layer));

					debugPrint(String.format("      for layer: %d  numwires: %d", layer, dcLayer.numWires), 0);
					_dcLayers.put(hash(sect, superlayer, layer), dcLayer);
				}
			}
		}

		debugPrint("", 2);
	}

	/**
	 * Initialize the time-of-flight geometry.
	 *
	 * @param cp constant provider
	 */
	private static void initializeTOF(DatabaseConstantProvider cp) {
		AlertTOFFactory tofFactory = new AlertTOFFactory();
		AlertTOFDetector tofCLASDetector = tofFactory.createDetectorCLAS(cp);

		int numsect = tofCLASDetector.getNumSectors();
		debugPrint(String.format("numsect: %d", numsect), 1);

		for (int sect = 0; sect < numsect; sect++) {
			debugPrint("", 2);
			debugPrint(String.format("  for sect: %d", sect + 1), 1);

			int numsupl = tofFactory.createSector(cp, sect).getNumSuperlayers();
			debugPrint(String.format("  numsuperlayer: %d", numsupl), 1);

			for (int superlayer = 0; superlayer < numsupl; superlayer++) {
				debugPrint(String.format("    for superlayer: %d", superlayer + 1), 1);

				int numlay = tofFactory.createSuperlayer(cp, sect, superlayer).getNumLayers();
				debugPrint(String.format("    numlayer: %d", numlay), 1);

				for (int layer = 0; layer < numlay; layer++) {
					debugPrint(String.format("      for layer: %d", layer + 1), 1);

					AlertTOFLayer alertTOFLayer = tofFactory.createLayer(cp, sect, superlayer, layer);
					TOFLayer tofLayer = new TOFLayer(alertTOFLayer);

					int numpaddle = alertTOFLayer.getNumComponents();
					debugPrint(String.format("      numpaddle: %d", numpaddle), 1);

					if (_debug) {
						List<ScintillatorPaddle> paddles = alertTOFLayer.getAllComponents();

						if ((sect == 0) || (sect == 14)) {
							for (int i = 0; i < numpaddle; i++) {
								ScintillatorPaddle paddle = paddles.get(i);
								Point3D pmp = paddle.getMidpoint();
								double x = pmp.x();
								double y = pmp.y();
								double z = pmp.z();
								double r = Math.sqrt(x * x + y * y + z * z);
								double rho = Math.sqrt(x * x + y * y);
								double phi = Math.toDegrees(Math.atan2(y, x));
								double theta = Math.toDegrees(Math.acos(z / r));
								int id = paddle.getComponentId();

								String sout = String.format(
										"sect: %d supl: %d lay: %d index: %d comp: %d z: %6.3f rho: %6.2f phi: %6.2f theta: %6.2f",
										sect + 1, superlayer + 1, layer + 1, i, id, z, rho, phi, theta);

								System.out.println(sout);
							}
							System.out.println();
						}
					}

					_tofLayers.put(hash(sect, superlayer, layer), tofLayer);
				}
			}
		}

		createTOFSectorXY();
	}

	/**
	 * Create the ALERT TOF sector-boundary points used in the XY view.
	 */
	private static void createTOFSectorXY() {
		tofSectorXY = new Point2D.Double[15][16];

		for (int sect = 0; sect < 15; sect++) {
			tofSectorXY[sect][0] = getCorner(sect, 0, 0, 0, 0);
			tofSectorXY[sect][1] = getCorner(sect, 0, 0, 0, 3);
			tofSectorXY[sect][2] = getCorner(sect, 0, 1, 0, 0);
			tofSectorXY[sect][3] = getCorner(sect, 0, 1, 0, 3);
			tofSectorXY[sect][4] = getCorner(sect, 0, 2, 0, 0);
			tofSectorXY[sect][5] = getCorner(sect, 0, 2, 0, 3);
			tofSectorXY[sect][6] = getCorner(sect, 0, 3, 0, 0);
			tofSectorXY[sect][7] = getCorner(sect, 0, 3, 0, 3);
			tofSectorXY[sect][8] = getCorner(sect, 1, 3, 0, 2);
			tofSectorXY[sect][9] = getCorner(sect, 1, 3, 0, 1);
			tofSectorXY[sect][10] = getCorner(sect, 1, 2, 0, 2);
			tofSectorXY[sect][11] = getCorner(sect, 1, 2, 0, 1);
			tofSectorXY[sect][12] = getCorner(sect, 1, 3, 0, 2);
			tofSectorXY[sect][13] = getCorner(sect, 1, 3, 0, 1);
			tofSectorXY[sect][14] = getCorner(sect, 1, 0, 0, 2);
			tofSectorXY[sect][15] = getCorner(sect, 1, 0, 0, 1);
		}
	}

	/**
	 * Get a scintillator paddle.
	 * <p>
	 * This is a legacy CCDB-only convenience method. After cache reads, the live
	 * paddle objects are not retained and this method returns {@code null}.
	 *
	 * @param sector     0-based sector
	 * @param superlayer 0-based superlayer
	 * @param layer      0-based layer
	 * @param paddle     0-based paddle
	 * @return the live paddle, or {@code null}
	 */
	public static ScintillatorPaddle getPaddle(int sector, int superlayer, int layer, int paddle) {
		TOFLayer tof = getTOFLayer(sector, superlayer, layer);
		return (tof == null) ? null : tof.getPaddle(paddle);
	}

	/**
	 * Check whether a projected paddle polygon intersects the projection plane.
	 *
	 * @param sector          0-based sector
	 * @param superlayer      0-based superlayer
	 * @param layer           0-based layer
	 * @param paddleId        0-based paddle id
	 * @param projectionPlane projection plane
	 * @return {@code true} if the paddle intersects
	 */
	public static boolean doesProjectedPolyFullyIntersect(int sector, int superlayer, int layer, int paddleId,
			Plane3D projectionPlane) {
		TOFLayer tof = getTOFLayer(sector, superlayer, layer);

		if ((tof == null) || (projectionPlane == null)) {
			return false;
		}

		return GeometryManager.doesProjectedPolyIntersect(tof.getProjectionEdges(paddleId), projectionPlane);
	}

	/**
	 * Legacy intersection check using a live paddle.
	 *
	 * @param sector          0-based sector
	 * @param superlayer      0-based superlayer
	 * @param layer           0-based layer
	 * @param paddle          live paddle
	 * @param projectionPlane projection plane
	 * @return {@code true} if the paddle intersects
	 */
	public static boolean doesProjectedPolyFullyIntersect(int sector, int superlayer, int layer,
			ScintillatorPaddle paddle, Plane3D projectionPlane) {
		TOFLayer tof = getTOFLayer(sector, superlayer, layer);

		if ((tof == null) || (paddle == null)) {
			return false;
		}

		return doesProjectedPolyFullyIntersect(sector, superlayer, layer, paddle.getComponentId(), projectionPlane);
	}

	/**
	 * Get projected paddle intersections in ordinary XY world coordinates.
	 *
	 * @param sector          0-based sector
	 * @param superlayer      0-based superlayer
	 * @param layer           0-based layer
	 * @param paddleId        0-based paddle id
	 * @param projectionPlane projection plane
	 * @param offset          retained for API compatibility
	 * @return the four projected points, or {@code null}
	 */
	public static Point2D.Double[] getIntersections(int sector, int superlayer, int layer, int paddleId,
			Plane3D projectionPlane, boolean offset) {
		TOFLayer tof = getTOFLayer(sector, superlayer, layer);

		if ((tof == null) || (projectionPlane == null)) {
			return null;
		}

		double edges[][][] = tof.getProjectionEdges(paddleId);

		if (edges == null) {
			return null;
		}

		Point2D.Double wp[] = GeometryManager.allocate(4);
		getProjectedPolygon(edges, projectionPlane, wp);

		return wp;
	}

	/**
	 * Legacy projected intersection method using a live paddle.
	 *
	 * @param sector          0-based sector
	 * @param superlayer      0-based superlayer
	 * @param layer           0-based layer
	 * @param paddle          live paddle
	 * @param projectionPlane projection plane
	 * @param offset          retained for API compatibility
	 * @return the projected points, or {@code null}
	 */
	public static Point2D.Double[] getIntersections(int sector, int superlayer, int layer, ScintillatorPaddle paddle,
			Plane3D projectionPlane, boolean offset) {
		if (paddle == null) {
			return null;
		}

		return getIntersections(sector, superlayer, layer, paddle.getComponentId(), projectionPlane, offset);
	}

	/**
	 * Project primitive edge intersections into ordinary XY world coordinates.
	 *
	 * @param edgeLines       edge data shaped [edge][endpoint][xyz]
	 * @param projectionPlane projection plane
	 * @param wp              receives the projected polygon
	 */
	public static void getProjectedPolygon(double edgeLines[][][], Plane3D projectionPlane, Point2D.Double wp[]) {
		if ((edgeLines == null) || (projectionPlane == null) || (wp == null)) {
			return;
		}

		for (int i = 0; i < Math.min(edgeLines.length, wp.length); i++) {
			Line3D line = primitiveLine(edgeLines[i]);

			if (line == null) {
				continue;
			}

			Point3D p3d = new Point3D();
			projectionPlane.intersection(line, p3d);

			wp[i].x = p3d.x();
			wp[i].y = p3d.y();
		}
	}

	/**
	 * Convert primitive endpoint data to a line.
	 *
	 * @param endpoints endpoint data shaped [2][3]
	 * @return the line, or {@code null}
	 */
	private static Line3D primitiveLine(double endpoints[][]) {
		if ((endpoints == null) || (endpoints.length < 2) || (endpoints[0] == null) || (endpoints[1] == null)
				|| (endpoints[0].length < 3) || (endpoints[1].length < 3)) {
			return null;
		}

		Point3D origin = new Point3D(endpoints[0][0], endpoints[0][1], endpoints[0][2]);
		Point3D end = new Point3D(endpoints[1][0], endpoints[1][1], endpoints[1][2]);

		return new Line3D(origin, end);
	}

	/**
	 * Draw the ALERT TOF sector numbers.
	 *
	 * @param g         the graphics context
	 * @param container the drawing container
	 */
	public static void drawAlertTOFSectorNumbers(Graphics g, IContainer container) {
		Point[] anchorPP = new Point[15];

		for (int sect = 0; sect < 15; sect++) {
			Point2D.Double anchor = tofSectorXY[sect][11];
			anchorPP[sect] = new Point();
			container.worldToLocal(anchorPP[sect], anchor);
		}

		g.setColor(Color.red);
		for (int sect = 0; sect < 15; sect++) {
			int oppSect = (sect + 7) % 15;
			Point pp0 = anchorPP[sect];
			Point pp1 = anchorPP[oppSect];
			GraphicsUtilities.drawNumberAtEnd(g, sect, pp1, pp0, 16, Fonts.hugeFont, Color.black);
		}
	}

	/**
	 * Get a paddle corner in ordinary XY coordinates.
	 *
	 * @param sector     0-based sector
	 * @param superlayer 0-based superlayer
	 * @param layer      0-based layer
	 * @param paddle     0-based paddle
	 * @param corner     corner index
	 * @return the XY corner
	 */
	public static Point2D.Double getCorner(int sector, int superlayer, int layer, int paddle, int corner) {
		TOFLayer tof = getTOFLayer(sector, superlayer, layer);

		if (tof == null) {
			return new Point2D.Double(Double.NaN, Double.NaN);
		}

		double c[] = tof.getCorner(paddle, corner);

		if (c == null) {
			return new Point2D.Double(Double.NaN, Double.NaN);
		}

		return new Point2D.Double(c[0], c[1]);
	}

	/**
	 * Legacy get-corner method using a live paddle.
	 *
	 * @param paddle live paddle
	 * @param corner corner index
	 * @return the corner
	 */
	public static Point2D.Double getCorner(ScintillatorPaddle paddle, int corner) {
		Point3D p3d = paddle.getVolumePoint(corner);
		return new Point2D.Double(p3d.x(), p3d.y());
	}

	/**
	 * Get all DC layers.
	 *
	 * @return all DC layers
	 */
	public static Collection<DCLayer> getAllDCLayers() {
		return _dcLayers.values();
	}

	/**
	 * Get all TOF layers.
	 *
	 * @return all TOF layers
	 */
	public static Collection<TOFLayer> getAllTOFLayers() {
		return _tofLayers.values();
	}

	/**
	 * Used by 3D drawing.
	 *
	 * @param sector     0-based sector
	 * @param superlayer 0-based superlayer
	 * @param layer      0-based layer
	 * @param paddleId   0-based paddle id
	 * @param coords     receives 8*3 = 24 values
	 */
	public static void paddleVertices(int sector, int superlayer, int layer, int paddleId, float[] coords) {
		TOFLayer tof = getTOFLayer(sector, superlayer, layer);

		if ((tof == null) || (coords == null) || (coords.length < 24)) {
			return;
		}

		for (int i = 0; i < 8; i++) {
			double c[] = tof.getCorner(paddleId, i);

			if (c == null) {
				return;
			}

			int j = 3 * i;
			coords[j] = (float) c[0];
			coords[j + 1] = (float) c[1];
			coords[j + 2] = (float) c[2];
		}
	}

	/**
	 * Build a layer hash key.
	 *
	 * @param sector     0-based sector
	 * @param superlayer 0-based superlayer
	 * @param layer      0-based layer
	 * @return the key
	 */
	private static String hash(int sector, int superlayer, int layer) {
		return String.format("%d|%d|%d", sector, superlayer, layer);
	}

	/**
	 * Get a DC layer.
	 *
	 * @param sector     0-based sector
	 * @param superlayer 0-based superlayer
	 * @param layer      0-based layer
	 * @return the DC layer
	 */
	public static DCLayer getDCLayer(int sector, int superlayer, int layer) {
		return _dcLayers.get(hash(sector, superlayer, layer));
	}

	/**
	 * Get a TOF layer.
	 *
	 * @param sector     0-based sector
	 * @param superlayer 0-based superlayer
	 * @param layer      0-based layer
	 * @return the TOF layer
	 */
	public static TOFLayer getTOFLayer(int sector, int superlayer, int layer) {
		return _tofLayers.get(hash(sector, superlayer, layer));
	}

	/**
	 * Read ALERT geometry from the cache using explicit primitive layer data.
	 *
	 * @param kryo  retained for interface compatibility
	 * @param input the cache input stream
	 * @return {@code true} if successful
	 */
	@Override
	public boolean readGeometry(Kryo kryo, Input input) {
		try {
			HashMap<String, DCLayer> dcLayers = new HashMap<>();
			HashMap<String, TOFLayer> tofLayers = new HashMap<>();

			int dcCount = input.readInt();
			if (dcCount < 0) {
				System.err.println("AlertGeometry: negative DC layer count in cache.");
				return false;
			}

			for (int i = 0; i < dcCount; i++) {
				DCLayer layer = DCLayer.readFromCache(input);
				dcLayers.put(hash(layer.sector, layer.superlayer, layer.layer), layer);
			}

			int tofCount = input.readInt();
			if (tofCount < 0) {
				System.err.println("AlertGeometry: negative TOF layer count in cache.");
				return false;
			}

			for (int i = 0; i < tofCount; i++) {
				TOFLayer layer = TOFLayer.readFromCache(input);
				tofLayers.put(hash(layer.sector, layer.superlayer, layer.layer), layer);
			}

			Point2D.Double xy[][] = readPointArray(input);

			_dcLayers = dcLayers;
			_tofLayers = tofLayers;
			tofSectorXY = xy;

			constantProvider = null;

			return true;
		} catch (Exception e) {
			System.err.println("AlertGeometry: Error reading geometry cache: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Write ALERT geometry to the cache using explicit primitive layer data.
	 *
	 * @param kryo   retained for interface compatibility
	 * @param output the cache output stream
	 * @return {@code true} if successful
	 */
	@Override
	public boolean writeGeometry(Kryo kryo, Output output) {
		try {
			output.writeInt(_dcLayers.size());
			for (DCLayer layer : _dcLayers.values()) {
				layer.writeToCache(output);
			}

			output.writeInt(_tofLayers.size());
			for (TOFLayer layer : _tofLayers.values()) {
				layer.writeToCache(output);
			}

			writePointArray(output, tofSectorXY);

			return true;
		} catch (Exception e) {
			System.err.println("AlertGeometry: Error writing geometry cache: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Write a nullable rectangular-ish 2D point array.
	 *
	 * @param output the cache output stream
	 * @param array  the point array
	 */
	private static void writePointArray(Output output, Point2D.Double array[][]) {
		if (array == null) {
			output.writeInt(0);
			return;
		}

		output.writeInt(array.length);

		for (Point2D.Double row[] : array) {
			if (row == null) {
				output.writeInt(0);
			} else {
				output.writeInt(row.length);

				for (Point2D.Double point : row) {
					boolean hasPoint = point != null;
					output.writeBoolean(hasPoint);

					if (hasPoint) {
						output.writeDouble(point.x);
						output.writeDouble(point.y);
					}
				}
			}
		}
	}

	/**
	 * Read a nullable rectangular-ish 2D point array.
	 *
	 * @param input the cache input stream
	 * @return the point array
	 */
	private static Point2D.Double[][] readPointArray(Input input) {
		int rows = input.readInt();

		if (rows < 0) {
			throw new IllegalArgumentException("AlertGeometry: negative point-array row count.");
		}

		Point2D.Double array[][] = new Point2D.Double[rows][];

		for (int row = 0; row < rows; row++) {
			int cols = input.readInt();

			if (cols < 0) {
				throw new IllegalArgumentException("AlertGeometry: negative point-array column count.");
			}

			array[row] = new Point2D.Double[cols];

			for (int col = 0; col < cols; col++) {
				boolean hasPoint = input.readBoolean();

				if (hasPoint) {
					array[row][col] = new Point2D.Double(input.readDouble(), input.readDouble());
				}
			}
		}

		return array;
	}
}