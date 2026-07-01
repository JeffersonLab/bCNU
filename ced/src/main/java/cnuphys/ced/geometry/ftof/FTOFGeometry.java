package cnuphys.ced.geometry.ftof;

import java.awt.geom.Point2D;

import org.jlab.detector.base.GeometryFactory;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.component.ScintillatorPaddle;
import org.jlab.geom.detector.ftof.FTOFDetector;
import org.jlab.geom.detector.ftof.FTOFFactory;
import org.jlab.geom.detector.ftof.FTOFLayer;
import org.jlab.geom.detector.ftof.FTOFSector;
import org.jlab.geom.detector.ftof.FTOFSuperlayer;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import cnuphys.bCNU.geometry.Plane;
import cnuphys.bCNU.geometry.Point;
import cnuphys.bCNU.geometry.Vector;
import cnuphys.ced.geometry.GeometryManager;
import cnuphys.ced.geometry.cache.ACachedGeometry;
import cnuphys.ced.geometry.cache.GeometryPrimitiveIO;

/**
 * Geometry support for the Forward Time-of-Flight detector.
 * <p>
 * Historically, this class cached the JLab {@link FTOFSuperlayer} and
 * {@link FTOFLayer} object graphs directly through Kryo. It now keeps those
 * objects only as CCDB-time legacy objects and caches the runtime geometry as
 * explicit primitive data.
 * <p>
 * The primitive cached geometry is:
 *
 * <pre>
 * _paddleCorners[sector][panel][paddle][corner][xyz]
 * _projectionEdges[sector][panel][paddle][edge][endpoint][xyz]
 * _paddleLengths[sector][panel][paddle]
 * </pre>
 *
 * Public drawing helpers such as {@link #paddleVertices(int, int, int, float[])},
 * {@link #paddlePolygon(int, int, int, Point2D.Double[])}, and
 * {@link #getIntersections(int, int, Plane3D, Point2D.Double[])} use primitive
 * data and therefore work after cache reads without the JLab detector objects.
 */
public class FTOFGeometry extends ACachedGeometry {

	/** Panel 1A. */
	public static final int PANEL_1A = 0;

	/** Panel 1B. */
	public static final int PANEL_1B = 1;

	/** Panel 2. */
	public static final int PANEL_2 = 2;

	/** Number of sectors. */
	private static final int SECTOR_COUNT = 6;

	/** Number of panel types. */
	private static final int PANEL_COUNT = 3;

	/** Number of volume corners per paddle. */
	private static final int CORNER_COUNT = 8;

	/** Number of coordinates per 3D point. */
	private static final int COORD_COUNT = 3;

	/** First JLab volume edge used for the sector-view projection polygon. */
	private static final int PROJECTION_EDGE_START = 6;

	/** Number of JLab volume edges used for the sector-view projection polygon. */
	private static final int PROJECTION_EDGE_COUNT = 4;

	/** Number of endpoints per edge. */
	private static final int EDGE_ENDPOINT_COUNT = 2;

	/** Full panel names. */
	private static final String panelNames[] = { "Panel 1A", "Panel 1B", "Panel 2" };

	/** Brief panel names. */
	private static final String briefPNames[] = { "1A", "1B", "2" };

	/** Panel names used by FTOFPanel creation. */
	private static final String ftofNames[] = { "Panel 1A", "Panel 1B", "Panel 2" };

	/**
	 * Legacy JLab superlayers. Available only after CCDB initialization.
	 */
	private static FTOFSuperlayer[][] ftofSuperlayers = new FTOFSuperlayer[SECTOR_COUNT][PANEL_COUNT];

	/**
	 * Legacy JLab layers. Available only after CCDB initialization.
	 */
	private static FTOFLayer[][] ftofLayers = new FTOFLayer[SECTOR_COUNT][PANEL_COUNT];

	/**
	 * Planes for the face of the panels. Rebuilt from primitive geometry.
	 */
	private static Plane facePlanes[][] = new Plane[SECTOR_COUNT][PANEL_COUNT];

	/**
	 * Number of paddles in each panel type.
	 */
	public static int numPaddles[];

	/**
	 * FTOF panel wrappers. One sector is represented because all sectors are assumed
	 * to have the same panel shape/counts for this purpose.
	 */
	private static FTOFPanel ftofPanel[] = new FTOFPanel[PANEL_COUNT];

	/**
	 * Primitive paddle corners.
	 * <p>
	 * Index order:
	 *
	 * <pre>
	 * [sector][panel][paddle][corner][xyz]
	 * </pre>
	 */
	private static double _paddleCorners[][][][][];

	/**
	 * Primitive projection edges corresponding to the old JLab volume edges
	 * {@code 6..9}.
	 * <p>
	 * Index order:
	 *
	 * <pre>
	 * [sector][panel][paddle][edge][endpoint][xyz]
	 * </pre>
	 */
	private static double _projectionEdges[][][][][][];

	/**
	 * Primitive paddle lengths.
	 * <p>
	 * Index order:
	 *
	 * <pre>
	 * [sector][panel][paddle]
	 * </pre>
	 */
	private static double _paddleLengths[][][];

	/**
	 * Constructor.
	 */
	public FTOFGeometry() {
		super("FTOFGeometry");
	}

	/**
	 * Initialize the FTOF geometry from CCDB/JLab geometry services.
	 */
	@Override
	public void initializeUsingCCDB() {
		System.out.println("\n=====================================");
		System.out.println("===  FTOF Geometry Initialization ===");
		System.out.println("=====================================");

		ConstantProvider tofDataProvider = GeometryFactory.getConstants(org.jlab.detector.base.DetectorType.FTOF);
		FTOFDetector ftofDetector = (new FTOFFactory()).createDetectorCLAS(tofDataProvider);

		ftofSuperlayers = new FTOFSuperlayer[SECTOR_COUNT][PANEL_COUNT];
		ftofLayers = new FTOFLayer[SECTOR_COUNT][PANEL_COUNT];

		for (int sect = 0; sect < SECTOR_COUNT; sect++) {
			FTOFSector ftofSector = ftofDetector.getSector(sect);

			for (int panel = 0; panel < PANEL_COUNT; panel++) {
				ftofSuperlayers[sect][panel] = ftofSector.getSuperlayer(panel);
				ftofLayers[sect][panel] = ftofSuperlayers[sect][panel].getLayer(0);
			}
		}

		cachePrimitiveGeometryFromLayers();
		createPanels();
		createFacePlanes();
	}

	/**
	 * Copy all required FTOF geometry from the JLab object graph into primitive
	 * arrays.
	 */
	private static void cachePrimitiveGeometryFromLayers() {
		if (ftofLayers == null) {
			throw new IllegalStateException("FTOFGeometry: cannot cache primitive geometry without FTOF layers.");
		}

		numPaddles = new int[PANEL_COUNT];

		for (int panel = 0; panel < PANEL_COUNT; panel++) {
			if (ftofLayers[0][panel] == null) {
				throw new IllegalStateException("FTOFGeometry: missing sector 1 layer for panel " + panel);
			}

			numPaddles[panel] = ftofLayers[0][panel].getNumComponents();
		}

		_paddleCorners = new double[SECTOR_COUNT][PANEL_COUNT][][][];
		_projectionEdges = new double[SECTOR_COUNT][PANEL_COUNT][][][][];
		_paddleLengths = new double[SECTOR_COUNT][PANEL_COUNT][];

		for (int sector = 0; sector < SECTOR_COUNT; sector++) {
			for (int panel = 0; panel < PANEL_COUNT; panel++) {
				int paddleCount = numPaddles[panel];

				_paddleCorners[sector][panel] = new double[paddleCount][CORNER_COUNT][COORD_COUNT];
				_projectionEdges[sector][panel] = new double[paddleCount][PROJECTION_EDGE_COUNT][EDGE_ENDPOINT_COUNT][COORD_COUNT];
				_paddleLengths[sector][panel] = new double[paddleCount];

				for (int paddle = 0; paddle < paddleCount; paddle++) {
					ScintillatorPaddle scintillatorPaddle = ftofLayers[sector][panel].getComponent(paddle);
					cachePaddlePrimitiveGeometry(sector, panel, paddle, scintillatorPaddle);
				}
			}
		}
	}

	/**
	 * Copy one paddle from a JLab {@link ScintillatorPaddle} into primitive arrays.
	 *
	 * @param sector the 0-based sector
	 * @param panel  the panel type
	 * @param paddle the 0-based paddle index
	 * @param source the JLab paddle
	 */
	private static void cachePaddlePrimitiveGeometry(int sector, int panel, int paddle, ScintillatorPaddle source) {
		if (source == null) {
			throw new IllegalArgumentException("FTOFGeometry: null source paddle.");
		}

		for (int corner = 0; corner < CORNER_COUNT; corner++) {
			Point3D point = source.getVolumePoint(corner);
			_paddleCorners[sector][panel][paddle][corner][0] = point.x();
			_paddleCorners[sector][panel][paddle][corner][1] = point.y();
			_paddleCorners[sector][panel][paddle][corner][2] = point.z();
		}

		for (int edge = 0; edge < PROJECTION_EDGE_COUNT; edge++) {
			Line3D line = source.getVolumeEdge(PROJECTION_EDGE_START + edge);

			copyPoint(line.origin(), _projectionEdges[sector][panel][paddle][edge][0]);
			copyPoint(line.end(), _projectionEdges[sector][panel][paddle][edge][1]);
		}

		_paddleLengths[sector][panel][paddle] = source.getLength();
	}

	/**
	 * Copy a JLab point into a primitive coordinate array.
	 *
	 * @param point  the source point
	 * @param coords the destination coordinates
	 */
	private static void copyPoint(Point3D point, double coords[]) {
		coords[0] = point.x();
		coords[1] = point.y();
		coords[2] = point.z();
	}

	/**
	 * Create the three FTOF panel wrappers from the primitive paddle counts.
	 */
	private static void createPanels() {
		if ((numPaddles == null) || (numPaddles.length != PANEL_COUNT)) {
			throw new IllegalStateException("FTOFGeometry: cannot create panels without paddle counts.");
		}

		for (int panel = 0; panel < PANEL_COUNT; panel++) {
			ftofPanel[panel] = new FTOFPanel(ftofNames[panel], numPaddles[panel]);
		}
	}

	/**
	 * Create the face planes, one for each panel in each sector.
	 */
	private static void createFacePlanes() {
		facePlanes = new Plane[SECTOR_COUNT][PANEL_COUNT];

		for (int sect = 0; sect < SECTOR_COUNT; sect++) {
			for (int panel = 0; panel < PANEL_COUNT; panel++) {
				facePlanes[sect][panel] = createFacePlane(sect + 1, panel);
			}
		}
	}

	/**
	 * Create the face plane for a sector and panel.
	 *
	 * @param sect  the 1-based sector
	 * @param panel the panel type
	 * @return the face plane
	 */
	private static Plane createFacePlane(int sect, int panel) {
		Point3D corners[] = new Point3D[4];

		int numPaddle = getNumPaddles(sect, panel);

		frontFace(sect, panel, 1, corners);
		Line3D line = new Line3D(corners[0], corners[3]);
		Point3D p0 = new Point3D(line.midpoint());

		frontFace(sect, panel, numPaddle / 2, corners);
		line = new Line3D(corners[0], corners[3]);
		Point3D p1 = new Point3D(line.midpoint());

		frontFace(sect, panel, numPaddle, corners);
		Point3D p2 = corners[1];
		Point3D p3 = corners[2];

		Vector u = new Vector(p2.x() - p0.x(), p2.y() - p0.y(), p2.z() - p0.z());
		Vector v = new Vector(p3.x() - p0.x(), p3.y() - p0.y(), p3.z() - p0.z());
		Vector norm = Vector.cross(u, v);

		return new Plane(norm, p1.x(), p1.y(), p1.z());
	}

	/**
	 * Get the array of three FTOF panels.
	 *
	 * @return the FTOF panel array
	 */
	public static FTOFPanel[] getFtofPanel() {
		return ftofPanel;
	}

	/**
	 * Get the face plane used for drawing.
	 *
	 * @param sector the 1-based sector
	 * @param panel  the panel type
	 * @return the face plane
	 */
	public static Plane getFacePlane(int sector, int panel) {
		if (!validSectorPanel(sector, panel)) {
			return null;
		}

		return facePlanes[sector - 1][panel];
	}

	/**
	 * Get a JLab paddle.
	 * <p>
	 * This is a legacy CCDB-only convenience method. After a cache read, the JLab
	 * layer objects are not retained and this method returns {@code null}.
	 *
	 * @param sect     the 1-based sector
	 * @param panel    the panel type
	 * @param paddleId the 1-based paddle id
	 * @return the JLab paddle, or {@code null}
	 */
	public static ScintillatorPaddle getPaddle(int sect, int panel, int paddleId) {
		if ((ftofLayers == null) || !validPaddleId(sect, panel, paddleId)) {
			return null;
		}

		FTOFLayer layer = ftofLayers[sect - 1][panel];
		return (layer == null) ? null : layer.getComponent(paddleId - 1);
	}

	/**
	 * Used by 3D drawing.
	 *
	 * @param sector   the 1-based sector
	 * @param panel    the panel type
	 * @param paddleId the 1-based paddle id
	 * @param coords   receives 8*3 = 24 values [x1, y1, z1, ..., x8, y8, z8]
	 */
	public static void paddleVertices(int sector, int panel, int paddleId, float[] coords) {
		if (!validPaddleGeometry(sector, panel, paddleId) || (coords == null)
				|| (coords.length < CORNER_COUNT * COORD_COUNT)) {
			return;
		}

		double corners[][] = _paddleCorners[sector - 1][panel][paddleId - 1];

		for (int i = 0; i < CORNER_COUNT; i++) {
			int j = COORD_COUNT * i;
			coords[j] = (float) corners[i][0];
			coords[j + 1] = (float) corners[i][1];
			coords[j + 2] = (float) corners[i][2];
		}
	}

	/**
	 * Get the four corners of the front face.
	 *
	 * @param sector   the 1-based sector
	 * @param panel    the panel type
	 * @param paddleId the 1-based paddle id
	 * @param corners  receives the four front-face corners
	 */
	public static void frontFace(int sector, int panel, int paddleId, Point3D corners[]) {
		if ((corners == null) || (corners.length < 4)) {
			return;
		}

		if (!validPaddleGeometry(sector, panel, paddleId)) {
			for (int i = 0; i < 4; i++) {
				corners[i] = new Point3D(Double.NaN, Double.NaN, Double.NaN);
			}
			return;
		}

		double paddleCorners[][] = _paddleCorners[sector - 1][panel][paddleId - 1];

		corners[0] = pointFromCorner(paddleCorners[0]);
		corners[1] = pointFromCorner(paddleCorners[1]);
		corners[2] = pointFromCorner(paddleCorners[5]);
		corners[3] = pointFromCorner(paddleCorners[4]);
	}

	/**
	 * Convert a primitive corner into a JLab point.
	 *
	 * @param corner the primitive corner
	 * @return the point
	 */
	private static Point3D pointFromCorner(double corner[]) {
		return new Point3D(corner[0], corner[1], corner[2]);
	}

	/**
	 * Get the z that puts the point on the face plane.
	 *
	 * @param sector the 1-based sector
	 * @param panel  the panel type
	 * @param x      the x coordinate
	 * @param y      the y coordinate
	 * @return the z coordinate
	 */
	public static double getZ(int sector, int panel, double x, double y) {
		Plane plane = getFacePlane(sector, panel);
		return (plane == null) ? Double.NaN : plane.getZ(x, y);
	}

	/**
	 * Convert from CLAS 3D coordinates to local panel coordinates.
	 *
	 * @param sector the 1-based sector
	 * @param panel  the panel type
	 * @param x      the CLAS x coordinate
	 * @param y      the CLAS y coordinate
	 * @param z      the CLAS z coordinate
	 * @param wp     receives the world point
	 */
	public static void clasToWorld(int sector, int panel, double x, double y, double z, Point2D.Double wp) {
		Plane plane = getFacePlane(sector, panel);

		if ((plane == null) || (wp == null)) {
			return;
		}

		Point cp = new Point();
		plane.closestPoint(x, y, z, cp);
		wp.setLocation(cp.x, cp.y);
	}

	/**
	 * Get the polygon for a paddle in panel-face coordinates.
	 *
	 * @param sector   the 1-based sector
	 * @param panel    the panel type
	 * @param paddleId the 1-based paddle id
	 * @param wp       receives the four corners in world coordinates
	 */
	public static void paddlePolygon(int sector, int panel, int paddleId, Point2D.Double wp[]) {
		if ((wp == null) || (wp.length < 4)) {
			return;
		}

		Point3D corners[] = new Point3D[4];
		frontFace(sector, panel, paddleId, corners);

		for (int i = 0; i < 4; i++) {
			if (wp[i] == null) {
				wp[i] = new Point2D.Double();
			}

			wp[i].x = corners[i].x();
			wp[i].y = corners[i].y();
		}
	}

	/**
	 * Get the panel name.
	 *
	 * @param panel the panel type
	 * @return the panel name
	 */
	public static String getPanelName(int panel) {
		return validPanel(panel) ? ftofNames[panel] : "???";
	}

	/**
	 * Get the number of paddles.
	 *
	 * @param sector the 1-based sector, retained for API compatibility
	 * @param panel  the panel type
	 * @return the number of paddles
	 */
	public static int getNumPaddles(int sector, int panel) {
		if (!validSectorPanel(sector, panel) || (numPaddles == null)) {
			return 0;
		}

		return numPaddles[panel];
	}

	/**
	 * Check whether the projected primitive polygon fully intersects the plane.
	 *
	 * @param panel           the panel type
	 * @param paddleid        the 0-based paddle id
	 * @param projectionPlane the projection plane
	 * @return {@code true} if the projected polygon intersects
	 */
	public static boolean doesProjectedPolyFullyIntersect(int panel, int paddleid, Plane3D projectionPlane) {
		if (!validZeroBasedPaddle(PANEL_1A + 1, panel, paddleid) || (projectionPlane == null)) {
			return false;
		}

		double edgeLines[][][] = _projectionEdges[0][panel][paddleid];
		return GeometryManager.doesProjectedPolyIntersect(edgeLines, projectionPlane);
	}

	/**
	 * Get the intersections with a constant phi plane. If the paddle does not
	 * intersect, return {@code false}.
	 *
	 * @param panel           the panel type
	 * @param paddleid        the 0-based paddle id
	 * @param projectionPlane the projection plane
	 * @param wp              receives the projected polygon
	 * @return {@code true} if the paddle intersects
	 */
	public static boolean getIntersections(int panel, int paddleid, Plane3D projectionPlane, Point2D.Double wp[]) {
		if (!validZeroBasedPaddle(1, panel, paddleid) || (projectionPlane == null)) {
			return false;
		}

		double edgeLines[][][] = _projectionEdges[0][panel][paddleid];
		return GeometryManager.getProjectedPolygon(edgeLines, projectionPlane, wp, null);
	}

	/**
	 * Get the length of a paddle in cm.
	 *
	 * @param panel    the panel type
	 * @param paddleId the 0-based paddle id
	 * @return the paddle length
	 */
	public static double getLength(int panel, int paddleId) {
		if (!validZeroBasedPaddle(1, panel, paddleId)) {
			return Double.NaN;
		}

		return _paddleLengths[0][panel][paddleId];
	}

	/**
	 * Get all paddle lengths for a panel.
	 *
	 * @param panel the panel type
	 * @return the lengths
	 */
	public static double[] getLengths(int panel) {
		if (!validPanel(panel) || (numPaddles == null)) {
			return new double[0];
		}

		double length[] = new double[numPaddles[panel]];

		for (int i = 0; i < length.length; i++) {
			length[i] = getLength(panel, i);
		}

		return length;
	}

	/**
	 * Get the brief panel name.
	 *
	 * @param layer the 1-based layer, 1..3
	 * @return the brief panel name
	 */
	public static String getBriefPanelName(byte layer) {
		if ((layer < 1) || (layer > PANEL_COUNT)) {
			return "" + layer;
		}

		return briefPNames[layer - 1];
	}

	/**
	 * Get the panel name from the panel type.
	 *
	 * @param panelType one of the panel constants
	 * @return the panel name
	 */
	public static String panelName(int panelType) {
		return validPanel(panelType) ? panelNames[panelType] : "???";
	}

	/**
	 * Read FTOF geometry from the cache.
	 * <p>
	 * This reads primitive paddle geometry rather than Kryo-serialized
	 * {@link FTOFSuperlayer} or {@link FTOFLayer} objects.
	 *
	 * @param kryo  the Kryo instance, retained for interface compatibility
	 * @param input the cache input stream
	 * @return {@code true} if the read succeeded
	 */
	@Override
	public boolean readGeometry(Kryo kryo, Input input) {
		try {
			int sectorCount = input.readInt();
			int panelCount = input.readInt();

			if ((sectorCount != SECTOR_COUNT) || (panelCount != PANEL_COUNT)) {
				System.err.printf("FTOFGeometry: expected %d sectors and %d panels, found %d and %d.%n",
						SECTOR_COUNT, PANEL_COUNT, sectorCount, panelCount);
				return false;
			}

			int paddleCounts[] = new int[PANEL_COUNT];

			for (int panel = 0; panel < PANEL_COUNT; panel++) {
				paddleCounts[panel] = input.readInt();

				if (paddleCounts[panel] <= 0) {
					System.err.println("FTOFGeometry: bad paddle count for panel " + panel + ": "
							+ paddleCounts[panel]);
					return false;
				}
			}

			double paddleCorners[][][][][] = new double[SECTOR_COUNT][PANEL_COUNT][][][];
			double projectionEdges[][][][][][] = new double[SECTOR_COUNT][PANEL_COUNT][][][][];
			double paddleLengths[][][] = new double[SECTOR_COUNT][PANEL_COUNT][];

			for (int sector = 0; sector < SECTOR_COUNT; sector++) {
				for (int panel = 0; panel < PANEL_COUNT; panel++) {
					int paddleCount = input.readInt();

					if (paddleCount != paddleCounts[panel]) {
						System.err.printf(
								"FTOFGeometry: paddle count mismatch for sector %d panel %d: expected %d, found %d.%n",
								sector + 1, panel, paddleCounts[panel], paddleCount);
						return false;
					}

					paddleCorners[sector][panel] = new double[paddleCount][CORNER_COUNT][COORD_COUNT];
					projectionEdges[sector][panel] = new double[paddleCount][PROJECTION_EDGE_COUNT][EDGE_ENDPOINT_COUNT][COORD_COUNT];
					paddleLengths[sector][panel] = new double[paddleCount];

					for (int paddle = 0; paddle < paddleCount; paddle++) {
						boolean hasPaddle = input.readBoolean();

						if (!hasPaddle) {
							System.err.printf("FTOFGeometry: missing paddle data for sector %d panel %d paddle %d.%n",
									sector + 1, panel, paddle + 1);
							return false;
						}

						String context = String.format("FTOF sector %d panel %d paddle %d", sector + 1, panel,
								paddle + 1);

						paddleCorners[sector][panel][paddle] = GeometryPrimitiveIO.readCorners(input, CORNER_COUNT,
								COORD_COUNT, context + " corners");
						projectionEdges[sector][panel][paddle] = readProjectionEdges(input, context + " projection");
						paddleLengths[sector][panel][paddle] = input.readDouble();
					}
				}
			}

			numPaddles = paddleCounts;
			_paddleCorners = paddleCorners;
			_projectionEdges = projectionEdges;
			_paddleLengths = paddleLengths;

			ftofSuperlayers = null;
			ftofLayers = null;

			createPanels();
			createFacePlanes();

			return true;
		} catch (Exception e) {
			System.err.println("FTOFGeometry: Error reading geometry cache: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Write FTOF geometry to the cache.
	 * <p>
	 * This writes primitive paddle geometry rather than Kryo-serialized
	 * {@link FTOFSuperlayer} or {@link FTOFLayer} objects.
	 *
	 * @param kryo   the Kryo instance, retained for interface compatibility
	 * @param output the cache output stream
	 * @return {@code true} if the write succeeded
	 */
	@Override
	public boolean writeGeometry(Kryo kryo, Output output) {
		try {
			if ((_paddleCorners == null) || (_projectionEdges == null) || (_paddleLengths == null)) {
				cachePrimitiveGeometryFromLayers();
			}

			output.writeInt(SECTOR_COUNT);
			output.writeInt(PANEL_COUNT);

			for (int panel = 0; panel < PANEL_COUNT; panel++) {
				output.writeInt(numPaddles[panel]);
			}

			for (int sector = 0; sector < SECTOR_COUNT; sector++) {
				for (int panel = 0; panel < PANEL_COUNT; panel++) {
					int paddleCount = numPaddles[panel];
					output.writeInt(paddleCount);

					for (int paddle = 0; paddle < paddleCount; paddle++) {
						boolean hasPaddle = validPrimitivePaddle(sector, panel, paddle);
						output.writeBoolean(hasPaddle);

						if (!hasPaddle) {
							System.err.printf("FTOFGeometry: missing primitive data for sector %d panel %d paddle %d.%n",
									sector + 1, panel, paddle + 1);
							return false;
						}

						GeometryPrimitiveIO.writeCorners(output, _paddleCorners[sector][panel][paddle], CORNER_COUNT,
								COORD_COUNT);
						writeProjectionEdges(output, _projectionEdges[sector][panel][paddle]);
						output.writeDouble(_paddleLengths[sector][panel][paddle]);
					}
				}
			}

			return true;
		} catch (Exception e) {
			System.err.println("FTOFGeometry: Error writing geometry cache: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Write primitive projection edges.
	 *
	 * @param output the output stream
	 * @param edges  the edges, shaped [edge][endpoint][xyz]
	 */
	private static void writeProjectionEdges(Output output, double edges[][][]) {
		validateProjectionEdges(edges, "write");

		output.writeInt(PROJECTION_EDGE_COUNT);

		for (int edge = 0; edge < PROJECTION_EDGE_COUNT; edge++) {
			GeometryPrimitiveIO.writeCorners(output, edges[edge], EDGE_ENDPOINT_COUNT, COORD_COUNT);
		}
	}

	/**
	 * Read primitive projection edges.
	 *
	 * @param input   the input stream
	 * @param context context for error messages
	 * @return the edges, shaped [edge][endpoint][xyz]
	 */
	private static double[][][] readProjectionEdges(Input input, String context) {
		int edgeCount = input.readInt();

		if (edgeCount != PROJECTION_EDGE_COUNT) {
			throw new IllegalArgumentException(String.format("%s: expected %d projection edges, found %d.", context,
					PROJECTION_EDGE_COUNT, edgeCount));
		}

		double edges[][][] = new double[PROJECTION_EDGE_COUNT][EDGE_ENDPOINT_COUNT][COORD_COUNT];

		for (int edge = 0; edge < PROJECTION_EDGE_COUNT; edge++) {
			edges[edge] = GeometryPrimitiveIO.readCorners(input, EDGE_ENDPOINT_COUNT, COORD_COUNT,
					context + " edge " + edge);
		}

		return edges;
	}

	/**
	 * Validate primitive projection edges.
	 *
	 * @param edges     the edge data
	 * @param operation the operation name
	 */
	private static void validateProjectionEdges(double edges[][][], String operation) {
		if ((edges == null) || (edges.length != PROJECTION_EDGE_COUNT)) {
			throw new IllegalArgumentException("FTOFGeometry: cannot " + operation + " invalid projection edge array.");
		}

		for (int edge = 0; edge < PROJECTION_EDGE_COUNT; edge++) {
			if ((edges[edge] == null) || (edges[edge].length != EDGE_ENDPOINT_COUNT)) {
				throw new IllegalArgumentException(
						"FTOFGeometry: cannot " + operation + " invalid projection edge " + edge + ".");
			}

			for (int endpoint = 0; endpoint < EDGE_ENDPOINT_COUNT; endpoint++) {
				if ((edges[edge][endpoint] == null) || (edges[edge][endpoint].length != COORD_COUNT)) {
					throw new IllegalArgumentException("FTOFGeometry: cannot " + operation
							+ " invalid projection edge coordinate data.");
				}
			}
		}
	}

	/**
	 * Check a sector/panel address.
	 *
	 * @param sector the 1-based sector
	 * @param panel  the panel type
	 * @return {@code true} if valid
	 */
	private static boolean validSectorPanel(int sector, int panel) {
		return (sector >= 1) && (sector <= SECTOR_COUNT) && validPanel(panel);
	}

	/**
	 * Check a panel type.
	 *
	 * @param panel the panel type
	 * @return {@code true} if valid
	 */
	private static boolean validPanel(int panel) {
		return (panel >= 0) && (panel < PANEL_COUNT);
	}

	/**
	 * Check a 1-based paddle id.
	 *
	 * @param sector   the 1-based sector
	 * @param panel    the panel type
	 * @param paddleId the 1-based paddle id
	 * @return {@code true} if valid
	 */
	private static boolean validPaddleId(int sector, int panel, int paddleId) {
		return validSectorPanel(sector, panel) && (numPaddles != null) && (paddleId >= 1)
				&& (paddleId <= numPaddles[panel]);
	}

	/**
	 * Check a 0-based paddle id.
	 *
	 * @param sector the 1-based sector
	 * @param panel  the panel type
	 * @param paddle the 0-based paddle id
	 * @return {@code true} if valid
	 */
	private static boolean validZeroBasedPaddle(int sector, int panel, int paddle) {
		return validSectorPanel(sector, panel) && (numPaddles != null) && (paddle >= 0)
				&& (paddle < numPaddles[panel]);
	}

	/**
	 * Check whether primitive paddle-corner geometry exists for a 1-based paddle id.
	 *
	 * @param sector   the 1-based sector
	 * @param panel    the panel type
	 * @param paddleId the 1-based paddle id
	 * @return {@code true} if geometry exists
	 */
	private static boolean validPaddleGeometry(int sector, int panel, int paddleId) {
		return validPaddleId(sector, panel, paddleId) && validPrimitivePaddle(sector - 1, panel, paddleId - 1);
	}

	/**
	 * Check whether primitive data exists for a 0-based paddle.
	 *
	 * @param sector the 0-based sector
	 * @param panel  the panel type
	 * @param paddle the 0-based paddle id
	 * @return {@code true} if primitive data exists
	 */
	private static boolean validPrimitivePaddle(int sector, int panel, int paddle) {
		return (sector >= 0) && (sector < SECTOR_COUNT) && validPanel(panel) && (numPaddles != null)
				&& (paddle >= 0) && (paddle < numPaddles[panel]) && (_paddleCorners != null)
				&& (_projectionEdges != null) && (_paddleLengths != null)
				&& (_paddleCorners[sector][panel] != null) && (_projectionEdges[sector][panel] != null)
				&& (_paddleLengths[sector][panel] != null) && (_paddleCorners[sector][panel][paddle] != null)
				&& (_projectionEdges[sector][panel][paddle] != null);
	}
}