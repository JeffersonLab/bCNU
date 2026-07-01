package cnuphys.ced.geometry;

import java.awt.geom.Point2D;

import org.jlab.detector.base.GeometryFactory;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.component.ScintillatorPaddle;
import org.jlab.geom.detector.ec.ECDetector;
import org.jlab.geom.detector.ec.ECFactory;
import org.jlab.geom.detector.ec.ECLayer;
import org.jlab.geom.detector.ec.ECSector;
import org.jlab.geom.detector.ec.ECSuperlayer;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Triangle3D;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import cnuphys.ced.geometry.cache.ACachedGeometry;
import cnuphys.ced.geometry.cache.GeometryPrimitiveIO;

/**
 * Holds the PCAL geometry used by the event display.
 * <p>
 * The runtime class still exposes the same public helper methods as before, but
 * the geometry cache no longer serializes {@link ECLayer}, {@link Transformations},
 * or {@link Point3D} object graphs. Instead, the cache stores explicit primitive
 * geometry:
 *
 * <pre>
 * local strip points     [stripType][strip][4][xyz]
 * 3D view triangles      [sector][view][3][xyz]
 * 3D strip vertices      [sector][view][strip][8][xyz]
 * projection edge lines  [stripType][strip][4][2][xyz]
 * r0, slope
 * </pre>
 */
public class PCALGeometry extends ACachedGeometry {

	/** Index for the geometry package. */
	private static final int EC_PCAL = 0;

	/** U strip index. */
	public static final int PCAL_U = 0;

	/** V strip index. */
	public static final int PCAL_V = 1;

	/** W strip index. */
	public static final int PCAL_W = 2;

	/** Plane or view names. */
	public static final String PLANE_NAMES[] = { "U", "V", "W" };

	/** Number of strips for U, V, W. */
	public static final int PCAL_NUMSTRIP[] = { 68, 62, 62 };

	/** Number of strip types. */
	private static final int STRIP_TYPE_COUNT = 3;

	/** Number of sectors. */
	private static final int SECTOR_COUNT = 6;

	/** Maximum number of strips in a PCAL strip type. */
	private static final int MAX_STRIP_COUNT = 68;

	/** Number of points used for the local 2D strip polygons. */
	private static final int LOCAL_STRIP_POINT_COUNT = 4;

	/** Number of 3D volume corners for a strip. */
	private static final int VOLUME_CORNER_COUNT = 8;

	/** Number of triangle points. */
	private static final int TRIANGLE_POINT_COUNT = 3;

	/** Number of coordinates per point. */
	private static final int COORD_COUNT = 3;

	/** First volume edge used for sector-view projection. */
	private static final int PROJECTION_EDGE_START = 6;

	/** Number of projection edges. */
	private static final int PROJECTION_EDGE_COUNT = 4;

	/** Number of endpoints in one edge line. */
	private static final int EDGE_ENDPOINT_COUNT = 2;

	/** Delta K separating front of inner from front of outer. */
	private static double _deltaK = 14.94;

	/** Normal vector in sector xyz, cm, from nominal target to front plane. */
	private static Point3D _r0;

	/**
	 * Local strip points. First index is U/V/W, second is strip index, third is
	 * point index 0..3.
	 */
	private static Point3D[][][] _strips = new Point3D[STRIP_TYPE_COUNT][MAX_STRIP_COUNT][LOCAL_STRIP_POINT_COUNT];

	/** Primitive 3D view triangles, shaped [sector][view][point][xyz]. */
	private static double _viewTriangles[][][][] = new double[SECTOR_COUNT][STRIP_TYPE_COUNT][TRIANGLE_POINT_COUNT][COORD_COUNT];

	/** Primitive 3D strip vertices, shaped [sector][view][strip][corner][xyz]. */
	private static double _stripVertices[][][][][] = new double[SECTOR_COUNT][STRIP_TYPE_COUNT][MAX_STRIP_COUNT][VOLUME_CORNER_COUNT][COORD_COUNT];

	/**
	 * Primitive projection edge lines, shaped
	 * [stripType][strip][edge][endpoint][xyz].
	 */
	private static double _projectionEdges[][][][][] = new double[STRIP_TYPE_COUNT][MAX_STRIP_COUNT][PROJECTION_EDGE_COUNT][EDGE_ENDPOINT_COUNT][COORD_COUNT];

	/** Angles related to _r0. */
	public static double COSTHETA = Double.NaN;
	public static double SINTHETA = Double.NaN;

	/** Slope of front plane. */
	private static double _slope = Double.NaN;

	/** Coordinate transformations. Reconstructed rather than cached. */
	private static Transformations _transformations;

	/** CCDB-time layers in local coordinates. Not retained as cache state. */
	private static ECLayer[] ecLayerLocal;

	/** CCDB-time layers in CLAS coordinates. Not retained as cache state. */
	private static ECLayer[] ecLayer;

	/**
	 * Constructor.
	 */
	public PCALGeometry() {
		super("PCALGeometry");
	}

	/**
	 * Initialize the PCAL geometry.
	 */
	@Override
	public void initializeUsingCCDB() {
		System.out.println("\n=====================================");
		System.out.println("===  PCAL Geometry Initialization ===");
		System.out.println("=====================================");

		ConstantProvider ecDataProvider = GeometryFactory.getConstants(org.jlab.detector.base.DetectorType.ECAL);
		ECDetector clas_Cal_Detector = (new ECFactory()).createDetectorCLAS(ecDataProvider);

		ECSector clas_Cal_Sector0 = clas_Cal_Detector.getSector(0);
		ECSector local_Cal_Sector0 = (new ECFactory()).createDetectorLocal(ecDataProvider).getSector(0);

		ECSuperlayer ecSuperlayer = clas_Cal_Sector0.getSuperlayer(EC_PCAL);
		ecLayer = new ECLayer[STRIP_TYPE_COUNT];

		for (int stripType = 0; stripType < STRIP_TYPE_COUNT; stripType++) {
			ecLayer[stripType] = ecSuperlayer.getLayer(stripType);
		}

		ECSuperlayer ecSuperlayerLocal = local_Cal_Sector0.getSuperlayer(EC_PCAL);
		ecLayerLocal = new ECLayer[STRIP_TYPE_COUNT];

		for (int stripType = 0; stripType < STRIP_TYPE_COUNT; stripType++) {
			ecLayerLocal[stripType] = ecSuperlayerLocal.getLayer(stripType);
		}

		createTransformations();
		getStripsAndTriangles();
		cache3DViewTriangles();
		cache3DStripVertices();
		cacheProjectionEdges();
	}

	/**
	 * Create the deterministic PCAL coordinate transformations and dependent
	 * quantities.
	 */
	private static void createTransformations() {
		_transformations = new Transformations(DetectorType.PCAL);

		_r0 = new Point3D(0, 0, 0);
		_transformations.localToSector(_r0);

		recomputeAngles();
	}

	/**
	 * Recompute values derived from _r0.
	 */
	private static void recomputeAngles() {
		double theta = Math.atan2(_r0.x(), _r0.z());
		COSTHETA = Math.cos(theta);
		SINTHETA = Math.sin(theta);
	}

	/**
	 * Cache local strip points and compute the front-plane slope.
	 */
	private static void getStripsAndTriangles() {
		double minI = Double.POSITIVE_INFINITY;
		double maxI = Double.NEGATIVE_INFINITY;
		double minJ = Double.POSITIVE_INFINITY;
		double maxJ = Double.NEGATIVE_INFINITY;

		_strips = new Point3D[STRIP_TYPE_COUNT][MAX_STRIP_COUNT][LOCAL_STRIP_POINT_COUNT];

		for (int stripType = 0; stripType < STRIP_TYPE_COUNT; stripType++) {
			for (int stripId = 0; stripId < PCAL_NUMSTRIP[stripType]; stripId++) {
				ScintillatorPaddle strip = ecLayerLocal[stripType].getComponent(stripId);

				_strips[stripType][stripId][0] = new Point3D(strip.getVolumePoint(4));
				_strips[stripType][stripId][1] = new Point3D(strip.getVolumePoint(5));
				_strips[stripType][stripId][2] = new Point3D(strip.getVolumePoint(1));
				_strips[stripType][stripId][3] = new Point3D(strip.getVolumePoint(0));

				// Preserve the old behavior exactly. The old code tested point 0 four times.
				Point3D p0 = _strips[stripType][stripId][0];
				Point3D p1 = _strips[stripType][stripId][0];
				Point3D p2 = _strips[stripType][stripId][0];
				Point3D p3 = _strips[stripType][stripId][0];

				minI = Math.min(minI, p0.x());
				maxI = Math.max(maxI, p0.x());
				minJ = Math.min(minJ, p0.y());
				maxJ = Math.max(maxJ, p0.y());

				minI = Math.min(minI, p1.x());
				maxI = Math.max(maxI, p1.x());
				minJ = Math.min(minJ, p1.y());
				maxJ = Math.max(maxJ, p1.y());

				minI = Math.min(minI, p2.x());
				maxI = Math.max(maxI, p2.x());
				minJ = Math.min(minJ, p2.y());
				maxJ = Math.max(maxJ, p2.y());

				minI = Math.min(minI, p3.x());
				maxI = Math.max(maxI, p3.x());
				minJ = Math.min(minJ, p3.y());
				maxJ = Math.max(maxJ, p3.y());
			}
		}

		Point3D rP0 = new Point3D(minI, 0, 0);
		Point3D rP1 = new Point3D(minI, 0, _deltaK);
		Point3D rP2 = new Point3D(maxI, 0, _deltaK);
		Point3D rP3 = new Point3D(maxI, 0, 0);

		_transformations.localToSector(rP0);
		_transformations.localToSector(rP1);
		_transformations.localToSector(rP2);
		_transformations.localToSector(rP3);

		double dely = rP0.x() - rP3.x();
		double delx = rP0.z() - rP3.z();
		_slope = dely / delx;
	}

	/**
	 * Cache the 3D view triangles for all sectors and views.
	 */
	private static void cache3DViewTriangles() {
		_viewTriangles = new double[SECTOR_COUNT][STRIP_TYPE_COUNT][TRIANGLE_POINT_COUNT][COORD_COUNT];

		for (int sector = 1; sector <= SECTOR_COUNT; sector++) {
			for (int view = 1; view <= STRIP_TYPE_COUNT; view++) {
				ECLayer ecLay = ecLayer[view - 1];
				Triangle3D t3d = (Triangle3D) ecLay.getBoundary().face(0);

				double dist = (view - 1) * (_deltaK / 3);
				double xt = dist * Math.sin(Math.toRadians(25));
				double yt = 0;
				double zt = dist * Math.cos(Math.toRadians(25));

				for (int i = 0; i < TRIANGLE_POINT_COUNT; i++) {
					Point3D corner = new Point3D(t3d.point(i));
					corner.translateXYZ(xt, yt, zt);

					if (sector > 1) {
						corner.rotateZ(Math.toRadians(60 * (sector - 1)));
					}

					storePoint(_viewTriangles[sector - 1][view - 1][i], corner);
				}
			}
		}
	}

	/**
	 * Cache the 3D strip vertices for all sectors, views, and strips.
	 */
	private static void cache3DStripVertices() {
		_stripVertices = new double[SECTOR_COUNT][STRIP_TYPE_COUNT][MAX_STRIP_COUNT][VOLUME_CORNER_COUNT][COORD_COUNT];

		for (int sector = 1; sector <= SECTOR_COUNT; sector++) {
			for (int view = 1; view <= STRIP_TYPE_COUNT; view++) {
				ECLayer ecLay = ecLayer[view - 1];

				double dist = (view - 1) * (_deltaK / 3);
				double xt = dist * Math.sin(Math.toRadians(25));
				double yt = 0;
				double zt = dist * Math.cos(Math.toRadians(25));

				for (int strip = 1; strip <= PCAL_NUMSTRIP[view - 1]; strip++) {
					ScintillatorPaddle paddle = ecLay.getComponent(strip - 1);

					for (int i = 0; i < VOLUME_CORNER_COUNT; i++) {
						Point3D v = new Point3D(paddle.getVolumePoint(i));
						v.translateXYZ(xt, yt, zt);

						if (sector > 1) {
							v.rotateZ(Math.toRadians(60 * (sector - 1)));
						}

						storePoint(_stripVertices[sector - 1][view - 1][strip - 1][i], v);
					}
				}
			}
		}
	}

	/**
	 * Cache the projection edge lines used by the sector views.
	 */
	private static void cacheProjectionEdges() {
		_projectionEdges = new double[STRIP_TYPE_COUNT][MAX_STRIP_COUNT][PROJECTION_EDGE_COUNT][EDGE_ENDPOINT_COUNT][COORD_COUNT];

		for (int stripType = 0; stripType < STRIP_TYPE_COUNT; stripType++) {
			ECLayer ecLay = ecLayer[stripType];

			for (int stripId = 0; stripId < PCAL_NUMSTRIP[stripType]; stripId++) {
				ScintillatorPaddle strip = ecLay.getComponent(stripId);

				for (int edge = 0; edge < PROJECTION_EDGE_COUNT; edge++) {
					Line3D line = strip.getVolumeEdge(PROJECTION_EDGE_START + edge);
					storePoint(_projectionEdges[stripType][stripId][edge][0], line.origin());
					storePoint(_projectionEdges[stripType][stripId][edge][1], line.end());
				}
			}
		}
	}

	/**
	 * Store a point in primitive coordinates.
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
	 * Get the normal vector in sector xyz, cm, from the nominal target to the front
	 * plane of PCAL.
	 *
	 * @return normal vector
	 */
	public static Point3D getR0() {
		return _r0;
	}

	/**
	 * Get the front plane of PCAL.
	 *
	 * @param sector the 1-based sector [1..6]
	 * @return the front plane
	 */
	public static Plane3D getFrontPlane(int sector) {
		Point3D clasr0 = new Point3D();
		GeometryManager.sectorToClas(sector, clasr0, _r0);
		Plane3D plane = new Plane3D(clasr0.x(), clasr0.y(), clasr0.z(), clasr0.x(), clasr0.y(), clasr0.z());
		return plane;
	}

	/**
	 * Get the coordinate transformation object.
	 *
	 * @return transformations
	 */
	public static Transformations getTransformations() {
		return _transformations;
	}

	/**
	 * Get a point from a U, V, or W strip.
	 *
	 * @param stripType  U, V, or W [0..2]
	 * @param stripIndex strip index
	 * @param pointIndex point index [0..3]
	 * @return strip point
	 */
	public static Point3D getStripPoint(int stripType, int stripIndex, int pointIndex) {
		return _strips[stripType][stripIndex][pointIndex];
	}

	/**
	 * For the front face of a given plane, compute z from x.
	 *
	 * @param x x coordinate in cm
	 * @return z coordinate in cm
	 */
	public static double zFromX(double x) {
		double x0 = _r0.x();
		double z0 = _r0.z();
		return z0 + (x - x0) / _slope;
	}

	/**
	 * Get the triangle for a given 3D view.
	 *
	 * @param sector sector 1..6
	 * @param view   view/layer 1..3
	 * @param coords receives [x1,y1,z1,...,x3,y3,z3]
	 */
	public static void getViewTriangle(int sector, int view, float coords[]) {
		if ((coords == null) || (coords.length < 9)) {
			return;
		}

		if ((sector < 1) || (sector > SECTOR_COUNT) || (view < 1) || (view > STRIP_TYPE_COUNT)) {
			return;
		}

		double triangle[][] = _viewTriangles[sector - 1][view - 1];

		for (int i = 0; i < TRIANGLE_POINT_COUNT; i++) {
			int j = 3 * i;
			coords[j] = (float) triangle[i][0];
			coords[j + 1] = (float) triangle[i][1];
			coords[j + 2] = (float) triangle[i][2];
		}
	}

	/**
	 * Get strip vertices for use by the 3D view.
	 *
	 * @param sector sector 1..6
	 * @param view   view/layer 1..3
	 * @param strip  strip 1..PCAL_NUMSTRIP[view-1]
	 * @param coords receives eight corners as [x1,y1,z1,...]
	 */
	public static void getStrip(int sector, int view, int strip, float coords[]) {
		if ((coords == null) || (coords.length < 24)) {
			return;
		}

		if ((sector < 1) || (sector > SECTOR_COUNT) || (view < 1) || (view > STRIP_TYPE_COUNT)) {
			return;
		}

		if ((strip < 1) || (strip > PCAL_NUMSTRIP[view - 1])) {
			return;
		}

		double vertices[][] = _stripVertices[sector - 1][view - 1][strip - 1];

		for (int i = 0; i < VOLUME_CORNER_COUNT; i++) {
			int j = 3 * i;
			coords[j] = (float) vertices[i][0];
			coords[j + 1] = (float) vertices[i][1];
			coords[j + 2] = (float) vertices[i][2];
		}
	}

	/**
	 * Obtain the shell for sector views.
	 *
	 * @param stripType       PCAL_U, PCAL_V, or PCAL_W
	 * @param projectionPlane projection plane
	 * @return shell points
	 */
	public static Point2D.Double[] getShell(int stripType, Plane3D projectionPlane) {
		Point2D.Double wp[] = new Point2D.Double[4];

		for (int i = 0; i < 4; i++) {
			wp[i] = new Point2D.Double();
		}

		int lastIndex = PCAL_NUMSTRIP[stripType] - 1;
		while (!doesProjectedPolyFullyIntersect(stripType, lastIndex, projectionPlane)) {
			lastIndex--;
			if (lastIndex < 1) {
				return null;
			}
		}

		Point2D.Double lastPP[] = getIntersections(stripType, lastIndex, projectionPlane, true);

		int firstIndex = 0;
		while (!doesProjectedPolyFullyIntersect(stripType, firstIndex, projectionPlane)) {
			firstIndex++;
		}

		Point2D.Double firstPP[] = getIntersections(stripType, firstIndex, projectionPlane, true);

		if (lastPP[0].y > firstPP[0].y) {
			wp[0] = lastPP[0];
			wp[1] = firstPP[1];
			wp[2] = firstPP[2];
			wp[3] = lastPP[3];
		} else {
			wp[0] = firstPP[0];
			wp[1] = lastPP[1];
			wp[2] = lastPP[2];
			wp[3] = firstPP[3];
		}

		return wp;
	}

	/**
	 * Convert ijk/local coordinates to sector xyz.
	 *
	 * @param localP    local coordinates
	 * @param sectorXYZ sector coordinates
	 */
	public static void ijkToSectorXYZ(Point3D localP, double[] sectorXYZ) {
		Point3D sectorP = new Point3D();
		_transformations.localToSector(localP, sectorP);
		sectorXYZ[0] = sectorP.x();
		sectorXYZ[1] = sectorP.y();
		sectorXYZ[2] = sectorP.z();
	}

	/**
	 * Check whether a projected strip polygon intersects the plane.
	 *
	 * @param layer           PCAL_U, PCAL_V, or PCAL_W
	 * @param stripid         0-based strip id
	 * @param projectionPlane projection plane
	 * @return true if intersecting
	 */
	public static boolean doesProjectedPolyFullyIntersect(int layer, int stripid, Plane3D projectionPlane) {
		if (!validStrip(layer, stripid)) {
			return false;
		}

		return GeometryManager.doesProjectedPolyIntersect(_projectionEdges[layer][stripid], projectionPlane);
	}

	/**
	 * Get the projected strip intersections.
	 *
	 * @param layer           PCAL_U, PCAL_V, or PCAL_W
	 * @param stripid         0-based strip id
	 * @param projectionPlane projection plane
	 * @param offset          true to apply PCAL display offsets
	 * @return projected points
	 */
	public static Point2D.Double[] getIntersections(int layer, int stripid, Plane3D projectionPlane, boolean offset) {
		if (!validStrip(layer, stripid)) {
			return null;
		}

		Point2D.Double wp[] = GeometryManager.allocate(4);
		GeometryManager.getProjectedPolygon(_projectionEdges[layer][stripid], projectionPlane, wp, null);

		Point2D.Double p2d[] = new Point2D.Double[4];

		p2d[0] = new Point2D.Double(wp[2].x, wp[2].y);
		p2d[1] = new Point2D.Double(wp[3].x, wp[3].y);
		p2d[2] = new Point2D.Double(wp[0].x, wp[0].y);
		p2d[3] = new Point2D.Double(wp[1].x, wp[1].y);

		if (offset) {
			if (layer == PCAL_V) {
				double del = _deltaK / 3;
				offsetLine(p2d[0], p2d[1], del - 1);
				offsetLine(p2d[2], p2d[3], del - 1);
			} else if (layer == PCAL_W) {
				double del = 2 * _deltaK / 3;
				offsetLine(p2d[0], p2d[1], del - 2);
				offsetLine(p2d[2], p2d[3], del - 2);
			}

			offsetLine(p2d[2], p2d[3], (0.9 * (_deltaK / 3)) - 1);
		}

		return p2d;
	}

	/**
	 * Offset a projected line.
	 *
	 * @param start start point
	 * @param end   end point
	 * @param len   offset length
	 */
	private static void offsetLine(Point2D.Double start, Point2D.Double end, double len) {
		double delx = len * COSTHETA;
		double dely = len * SINTHETA;
		start.x += delx;
		start.y += dely;
		end.x += delx;
		end.y += dely;
	}

	/**
	 * Validate a strip address.
	 *
	 * @param layer   strip type
	 * @param stripid strip id
	 * @return true if valid
	 */
	private static boolean validStrip(int layer, int stripid) {
		return (layer >= 0) && (layer < STRIP_TYPE_COUNT) && (stripid >= 0) && (stripid < PCAL_NUMSTRIP[layer]);
	}

	/**
	 * Read PCAL geometry from explicit primitive cache data.
	 *
	 * @param kryo  retained for interface compatibility
	 * @param input cache input
	 * @return true if successful
	 */
	@Override
	public boolean readGeometry(Kryo kryo, Input input) {
		try {
			readR0AndSlope(input);

			_strips = readLocalStrips(input);
			_viewTriangles = readViewTriangles(input);
			_stripVertices = readStripVertices(input);
			_projectionEdges = readProjectionEdges(input);

			_transformations = new Transformations(DetectorType.PCAL);
			ecLayer = null;
			ecLayerLocal = null;

			recomputeAngles();

			return true;
		} catch (Exception e) {
			System.err.println("PCALGeometry: Error reading geometry cache: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Write PCAL geometry as explicit primitive cache data.
	 *
	 * @param kryo   retained for interface compatibility
	 * @param output cache output
	 * @return true if successful
	 */
	@Override
	public boolean writeGeometry(Kryo kryo, Output output) {
		try {
			writeR0AndSlope(output);

			writeLocalStrips(output);
			writeViewTriangles(output);
			writeStripVertices(output);
			writeProjectionEdges(output);

			return true;
		} catch (Exception e) {
			System.err.println("PCALGeometry: Error writing geometry cache: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Write r0 and slope.
	 *
	 * @param output cache output
	 */
	private static void writeR0AndSlope(Output output) {
		writePoint(output, _r0);
		output.writeDouble(_slope);
	}

	/**
	 * Read r0 and slope.
	 *
	 * @param input cache input
	 */
	private static void readR0AndSlope(Input input) {
		_r0 = readPoint(input, "PCAL r0");
		_slope = input.readDouble();
	}

	/**
	 * Write local strip points.
	 *
	 * @param output cache output
	 */
	private static void writeLocalStrips(Output output) {
		output.writeInt(STRIP_TYPE_COUNT);

		for (int stripType = 0; stripType < STRIP_TYPE_COUNT; stripType++) {
			output.writeInt(PCAL_NUMSTRIP[stripType]);

			for (int strip = 0; strip < PCAL_NUMSTRIP[stripType]; strip++) {
				writePointBlock(output, _strips[stripType][strip], LOCAL_STRIP_POINT_COUNT,
						"PCAL local strip " + stripType + " " + strip);
			}
		}
	}

	/**
	 * Read local strip points.
	 *
	 * @param input cache input
	 * @return local strips
	 */
	private static Point3D[][][] readLocalStrips(Input input) {
		int typeCount = input.readInt();
		require(typeCount == STRIP_TYPE_COUNT, "PCAL local strip type count mismatch.");

		Point3D strips[][][] = new Point3D[STRIP_TYPE_COUNT][MAX_STRIP_COUNT][LOCAL_STRIP_POINT_COUNT];

		for (int stripType = 0; stripType < STRIP_TYPE_COUNT; stripType++) {
			int stripCount = input.readInt();
			require(stripCount == PCAL_NUMSTRIP[stripType], "PCAL local strip count mismatch for type " + stripType);

			for (int strip = 0; strip < stripCount; strip++) {
				strips[stripType][strip] = readPointBlock(input, LOCAL_STRIP_POINT_COUNT,
						"PCAL local strip " + stripType + " " + strip);
			}
		}

		return strips;
	}

	/**
	 * Write 3D view triangles.
	 *
	 * @param output cache output
	 */
	private static void writeViewTriangles(Output output) {
		output.writeInt(SECTOR_COUNT);
		output.writeInt(STRIP_TYPE_COUNT);

		for (int sector = 0; sector < SECTOR_COUNT; sector++) {
			for (int view = 0; view < STRIP_TYPE_COUNT; view++) {
				GeometryPrimitiveIO.writeCorners(output, _viewTriangles[sector][view], TRIANGLE_POINT_COUNT,
						COORD_COUNT);
			}
		}
	}

	/**
	 * Read 3D view triangles.
	 *
	 * @param input cache input
	 * @return view triangles
	 */
	private static double[][][][] readViewTriangles(Input input) {
		int sectorCount = input.readInt();
		int viewCount = input.readInt();

		require(sectorCount == SECTOR_COUNT, "PCAL view triangle sector count mismatch.");
		require(viewCount == STRIP_TYPE_COUNT, "PCAL view triangle type count mismatch.");

		double data[][][][] = new double[SECTOR_COUNT][STRIP_TYPE_COUNT][][];

		for (int sector = 0; sector < SECTOR_COUNT; sector++) {
			for (int view = 0; view < STRIP_TYPE_COUNT; view++) {
				data[sector][view] = GeometryPrimitiveIO.readCorners(input, TRIANGLE_POINT_COUNT, COORD_COUNT,
						"PCAL view triangle sector " + sector + " view " + view);
			}
		}

		return data;
	}

	/**
	 * Write 3D strip vertices.
	 *
	 * @param output cache output
	 */
	private static void writeStripVertices(Output output) {
		output.writeInt(SECTOR_COUNT);
		output.writeInt(STRIP_TYPE_COUNT);

		for (int sector = 0; sector < SECTOR_COUNT; sector++) {
			for (int view = 0; view < STRIP_TYPE_COUNT; view++) {
				output.writeInt(PCAL_NUMSTRIP[view]);

				for (int strip = 0; strip < PCAL_NUMSTRIP[view]; strip++) {
					GeometryPrimitiveIO.writeCorners(output, _stripVertices[sector][view][strip],
							VOLUME_CORNER_COUNT, COORD_COUNT);
				}
			}
		}
	}

	/**
	 * Read 3D strip vertices.
	 *
	 * @param input cache input
	 * @return strip vertices
	 */
	private static double[][][][][] readStripVertices(Input input) {
		int sectorCount = input.readInt();
		int viewCount = input.readInt();

		require(sectorCount == SECTOR_COUNT, "PCAL strip vertex sector count mismatch.");
		require(viewCount == STRIP_TYPE_COUNT, "PCAL strip vertex view count mismatch.");

		double data[][][][][] = new double[SECTOR_COUNT][STRIP_TYPE_COUNT][MAX_STRIP_COUNT][][];

		for (int sector = 0; sector < SECTOR_COUNT; sector++) {
			for (int view = 0; view < STRIP_TYPE_COUNT; view++) {
				int stripCount = input.readInt();
				require(stripCount == PCAL_NUMSTRIP[view],
						"PCAL strip vertex count mismatch for sector " + sector + " view " + view);

				for (int strip = 0; strip < stripCount; strip++) {
					data[sector][view][strip] = GeometryPrimitiveIO.readCorners(input, VOLUME_CORNER_COUNT,
							COORD_COUNT, "PCAL strip vertices sector " + sector + " view " + view + " strip " + strip);
				}
			}
		}

		return data;
	}

	/**
	 * Write projection edges.
	 *
	 * @param output cache output
	 */
	private static void writeProjectionEdges(Output output) {
		output.writeInt(STRIP_TYPE_COUNT);

		for (int stripType = 0; stripType < STRIP_TYPE_COUNT; stripType++) {
			output.writeInt(PCAL_NUMSTRIP[stripType]);

			for (int strip = 0; strip < PCAL_NUMSTRIP[stripType]; strip++) {
				output.writeInt(PROJECTION_EDGE_COUNT);

				for (int edge = 0; edge < PROJECTION_EDGE_COUNT; edge++) {
					GeometryPrimitiveIO.writeCorners(output, _projectionEdges[stripType][strip][edge],
							EDGE_ENDPOINT_COUNT, COORD_COUNT);
				}
			}
		}
	}

	/**
	 * Read projection edges.
	 *
	 * @param input cache input
	 * @return projection edges
	 */
	private static double[][][][][] readProjectionEdges(Input input) {
		int typeCount = input.readInt();
		require(typeCount == STRIP_TYPE_COUNT, "PCAL projection edge type count mismatch.");

		double data[][][][][] = new double[STRIP_TYPE_COUNT][MAX_STRIP_COUNT][][][];

		for (int stripType = 0; stripType < STRIP_TYPE_COUNT; stripType++) {
			int stripCount = input.readInt();
			require(stripCount == PCAL_NUMSTRIP[stripType],
					"PCAL projection edge strip count mismatch for type " + stripType);

			for (int strip = 0; strip < stripCount; strip++) {
				int edgeCount = input.readInt();
				require(edgeCount == PROJECTION_EDGE_COUNT,
						"PCAL projection edge count mismatch for type " + stripType + " strip " + strip);

				data[stripType][strip] = new double[PROJECTION_EDGE_COUNT][][];

				for (int edge = 0; edge < PROJECTION_EDGE_COUNT; edge++) {
					data[stripType][strip][edge] = GeometryPrimitiveIO.readCorners(input, EDGE_ENDPOINT_COUNT,
							COORD_COUNT, "PCAL projection edge type " + stripType + " strip " + strip + " edge " + edge);
				}
			}
		}

		return data;
	}

	/**
	 * Write a Point3D.
	 *
	 * @param output cache output
	 * @param point  point
	 */
	private static void writePoint(Output output, Point3D point) {
		if (point == null) {
			throw new IllegalArgumentException("PCALGeometry: cannot write null point.");
		}

		output.writeDouble(point.x());
		output.writeDouble(point.y());
		output.writeDouble(point.z());
	}

	/**
	 * Read a Point3D.
	 *
	 * @param input   cache input
	 * @param context diagnostic context
	 * @return point
	 */
	private static Point3D readPoint(Input input, String context) {
		return new Point3D(input.readDouble(), input.readDouble(), input.readDouble());
	}

	/**
	 * Write a block of Point3D values.
	 *
	 * @param output cache output
	 * @param points points
	 * @param count  expected count
	 * @param context diagnostic context
	 */
	private static void writePointBlock(Output output, Point3D points[], int count, String context) {
		if ((points == null) || (points.length != count)) {
			throw new IllegalArgumentException(context + ": invalid point count.");
		}

		output.writeInt(count);

		for (int i = 0; i < count; i++) {
			writePoint(output, points[i]);
		}
	}

	/**
	 * Read a block of Point3D values.
	 *
	 * @param input   cache input
	 * @param count   expected count
	 * @param context diagnostic context
	 * @return points
	 */
	private static Point3D[] readPointBlock(Input input, int count, String context) {
		int actualCount = input.readInt();
		require(actualCount == count, context + ": point count mismatch.");

		Point3D points[] = new Point3D[count];

		for (int i = 0; i < count; i++) {
			points[i] = readPoint(input, context + " point " + i);
		}

		return points;
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