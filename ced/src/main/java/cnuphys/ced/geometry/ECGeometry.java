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
 * Holds the EC geometry used by the event display.
 * <p>
 * The runtime public API is preserved, but the cache no longer serializes
 * {@link ECLayer}, {@link Transformations}, or {@link Point3D} object graphs.
 * Instead it stores explicit primitive geometry and reconstructs the
 * deterministic transformation helpers after a cache read.
 */
public class ECGeometry extends ACachedGeometry {

	/** Inner EC stack index. */
	public static final int EC_INNER = 0;

	/** Outer EC stack index. */
	public static final int EC_OUTER = 1;

	/** U view index. */
	public static final int EC_U = 0;

	/** V view index. */
	public static final int EC_V = 1;

	/** W view index. */
	public static final int EC_W = 2;

	/** Stack names. */
	public static final String PLANE_NAMES[] = { "Inner", "Outer" };

	/** View names. */
	public static final String VIEW_NAMES[] = { "U", "V", "W" };

	/** Number of EC strips for U, V, and W. */
	public static final int EC_NUMSTRIP = 36;

	/** Layer names. */
	public static final String layerNames[] = { "???", "PCAL_U", "PCAL_V", "PCAL_W", "ECAL_IN_U", "ECAL_IN_V",
			"ECAL_IN_W", "ECAL_OUT_U", "ECAL_OUT_V", "ECAL_OUT_W" };

	/** Number of EC planes: inner and outer. */
	private static final int PLANE_COUNT = 2;

	/** Number of EC views: U, V, W. */
	private static final int VIEW_COUNT = 3;

	/** Number of sectors. */
	private static final int SECTOR_COUNT = 6;

	/** Number of local strip polygon points. */
	private static final int LOCAL_STRIP_POINT_COUNT = 4;

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

	/** Delta K values for inner and outer EC. */
	private static double[] _deltaK = new double[PLANE_COUNT];

	/** Normal vectors in sector xyz, cm, from nominal target to EC front planes. */
	private static Point3D _r0[] = new Point3D[PLANE_COUNT];

	/** Local strip points shaped [plane][view][strip][point]. */
	private static Point3D[][][][] _strips = new Point3D[PLANE_COUNT][VIEW_COUNT][EC_NUMSTRIP][LOCAL_STRIP_POINT_COUNT];

	/** Primitive 3D view triangles shaped [sector][stack][view][point][xyz]. */
	private static double _viewTriangles[][][][][] = new double[SECTOR_COUNT][PLANE_COUNT][VIEW_COUNT][TRIANGLE_POINT_COUNT][COORD_COUNT];

	/** Primitive projection edge lines shaped [plane][view][strip][edge][endpoint][xyz]. */
	private static double _projectionEdges[][][][][][] = new double[PLANE_COUNT][VIEW_COUNT][EC_NUMSTRIP][PROJECTION_EDGE_COUNT][EDGE_ENDPOINT_COUNT][COORD_COUNT];

	/** Angles related to the inner r0. */
	public static double THETA = Double.NaN;

	/** Cosine of THETA. */
	public static double COSTHETA = Double.NaN;

	/** Sine of THETA. */
	public static double SINTHETA = Double.NaN;

	/** Tangent of THETA. */
	public static double TANTHETA = Double.NaN;

	/** Slopes of front planes. */
	private static double[] _slopes = { Double.NaN, Double.NaN };

	/** Deterministic coordinate transformations, reconstructed after cache reads. */
	private static Transformations _transformations[];

	/** CCDB-time local layers. Not retained as cache state. */
	private static ECLayer[][] ecLayerLocal;

	/** CCDB-time CLAS layers. Not retained as cache state. */
	private static ECLayer[][] ecLayer;

	/**
	 * Constructor.
	 */
	public ECGeometry() {
		super("ECGeometry");
	}

	/**
	 * Initialize the EC geometry.
	 */
	@Override
	public void initializeUsingCCDB() {
		System.out.println("\n=====================================");
		System.out.println("====  EC Geometry Initialization ====");
		System.out.println("=====================================");

		ConstantProvider ecDataProvider = GeometryFactory.getConstants(org.jlab.detector.base.DetectorType.ECAL);
		ECDetector clas_Cal_Detector = (new ECFactory()).createDetectorCLAS(ecDataProvider);

		ECSector clas_Cal_Sector0 = clas_Cal_Detector.getSector(0);
		ECSector local_Cal_Sector0 = (new ECFactory()).createDetectorLocal(ecDataProvider).getSector(0);

		ECSuperlayer ecSuperlayer[] = new ECSuperlayer[PLANE_COUNT];
		ecSuperlayer[EC_INNER] = clas_Cal_Sector0.getSuperlayer(1);
		ecSuperlayer[EC_OUTER] = clas_Cal_Sector0.getSuperlayer(2);

		ecLayer = new ECLayer[PLANE_COUNT][VIEW_COUNT];
		for (int plane = 0; plane < PLANE_COUNT; plane++) {
			for (int stripType = 0; stripType < VIEW_COUNT; stripType++) {
				ecLayer[plane][stripType] = ecSuperlayer[plane].getLayer(stripType);
			}
		}

		ECSuperlayer ecSuperLayerLocal[] = new ECSuperlayer[PLANE_COUNT];
		ecSuperLayerLocal[EC_INNER] = local_Cal_Sector0.getSuperlayer(1);
		ecSuperLayerLocal[EC_OUTER] = local_Cal_Sector0.getSuperlayer(2);

		ecLayerLocal = new ECLayer[PLANE_COUNT][VIEW_COUNT];
		for (int plane = 0; plane < PLANE_COUNT; plane++) {
			for (int stripType = 0; stripType < VIEW_COUNT; stripType++) {
				ecLayerLocal[plane][stripType] = ecSuperLayerLocal[plane].getLayer(stripType);
			}
		}

		createTransformations();
		getStripsAndTriangles();
		cache3DViewTriangles();
		cacheProjectionEdges();
	}

	/**
	 * Create deterministic transformations for inner and outer EC.
	 */
	private static void createTransformations() {
		_transformations = new Transformations[PLANE_COUNT];
		_transformations[EC_INNER] = new Transformations(DetectorType.EC_INNER);
		_transformations[EC_OUTER] = new Transformations(DetectorType.EC_OUTER);

		_r0 = new Point3D[PLANE_COUNT];

		for (int plane = 0; plane < PLANE_COUNT; plane++) {
			_r0[plane] = new Point3D(0, 0, 0);
			_transformations[plane].localToSector(_r0[plane]);
		}

		recomputeAngles();
	}

	/**
	 * Recompute angle values derived from the inner EC r0.
	 */
	private static void recomputeAngles() {
		THETA = Math.atan2(_r0[EC_INNER].x(), _r0[EC_INNER].z());
		COSTHETA = Math.cos(THETA);
		SINTHETA = Math.sin(THETA);
		TANTHETA = Math.tan(THETA);
	}

	/**
	 * Cache local strip points and compute derived slopes/deltaK values.
	 */
	private static void getStripsAndTriangles() {
		Point3D zeroP = new Point3D(0, 0, 0);

		double rmag[] = new double[PLANE_COUNT];

		rmag[EC_INNER] = _r0[EC_INNER].distance(zeroP);
		rmag[EC_OUTER] = _r0[EC_OUTER].distance(zeroP);

		_deltaK[EC_INNER] = rmag[EC_OUTER] - rmag[EC_INNER];
		_deltaK[EC_OUTER] = 1.5 * _deltaK[EC_INNER];

		_strips = new Point3D[PLANE_COUNT][VIEW_COUNT][EC_NUMSTRIP][LOCAL_STRIP_POINT_COUNT];

		double minI[] = { Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY };
		double maxI[] = { Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY };
		double minJ[] = { Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY };
		double maxJ[] = { Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY };

		for (int plane = 0; plane < PLANE_COUNT; plane++) {
			for (int stripType = 0; stripType < VIEW_COUNT; stripType++) {
				for (int stripId = 0; stripId < EC_NUMSTRIP; stripId++) {
					ScintillatorPaddle strip = ecLayerLocal[plane][stripType].getComponent(stripId);

					_strips[plane][stripType][stripId][0] = new Point3D(strip.getVolumePoint(4));
					_strips[plane][stripType][stripId][1] = new Point3D(strip.getVolumePoint(5));
					_strips[plane][stripType][stripId][2] = new Point3D(strip.getVolumePoint(1));
					_strips[plane][stripType][stripId][3] = new Point3D(strip.getVolumePoint(0));

					// Preserve the old behavior exactly. The old code tested point 0 four times.
					Point3D p0 = _strips[plane][stripType][stripId][0];
					Point3D p1 = _strips[plane][stripType][stripId][0];
					Point3D p2 = _strips[plane][stripType][stripId][0];
					Point3D p3 = _strips[plane][stripType][stripId][0];

					minI[plane] = Math.min(minI[plane], p0.x());
					maxI[plane] = Math.max(maxI[plane], p0.x());
					minJ[plane] = Math.min(minJ[plane], p0.y());
					maxJ[plane] = Math.max(maxJ[plane], p0.y());

					minI[plane] = Math.min(minI[plane], p1.x());
					maxI[plane] = Math.max(maxI[plane], p1.x());
					minJ[plane] = Math.min(minJ[plane], p1.y());
					maxJ[plane] = Math.max(maxJ[plane], p1.y());

					minI[plane] = Math.min(minI[plane], p2.x());
					maxI[plane] = Math.max(maxI[plane], p2.x());
					minJ[plane] = Math.min(minJ[plane], p2.y());
					maxJ[plane] = Math.max(maxJ[plane], p2.y());

					minI[plane] = Math.min(minI[plane], p3.x());
					maxI[plane] = Math.max(maxI[plane], p3.x());
					minJ[plane] = Math.min(minJ[plane], p3.y());
					maxJ[plane] = Math.max(maxJ[plane], p3.y());
				}
			}
		}

		_slopes = new double[PLANE_COUNT];

		for (int plane = 0; plane < PLANE_COUNT; plane++) {
			Point3D rP0 = new Point3D(minI[plane], 0, 0);
			Point3D rP1 = new Point3D(minI[plane], 0, _deltaK[plane]);
			Point3D rP2 = new Point3D(maxI[plane], 0, _deltaK[plane]);
			Point3D rP3 = new Point3D(maxI[plane], 0, 0);

			_transformations[plane].localToSector(rP0);
			_transformations[plane].localToSector(rP1);
			_transformations[plane].localToSector(rP2);
			_transformations[plane].localToSector(rP3);

			double dely = rP0.x() - rP3.x();
			double delx = rP0.z() - rP3.z();
			_slopes[plane] = dely / delx;
		}
	}

	/**
	 * Cache the 3D view triangles for all sectors, stacks, and views.
	 */
	private static void cache3DViewTriangles() {
		_viewTriangles = new double[SECTOR_COUNT][PLANE_COUNT][VIEW_COUNT][TRIANGLE_POINT_COUNT][COORD_COUNT];

		for (int sector = 1; sector <= SECTOR_COUNT; sector++) {
			for (int stack = 1; stack <= PLANE_COUNT; stack++) {
				for (int view = 1; view <= VIEW_COUNT; view++) {
					ECLayer ecLay = ecLayer[stack - 1][view - 1];
					Triangle3D t3d = (Triangle3D) ecLay.getBoundary().face(0);

					double delK = _deltaK[stack - 1];
					double dist = (view - 1) * (delK / 3);
					double xt = dist * Math.sin(Math.toRadians(25));
					double yt = 0;
					double zt = dist * Math.cos(Math.toRadians(25));

					for (int i = 0; i < TRIANGLE_POINT_COUNT; i++) {
						Point3D corner = new Point3D(t3d.point(i));
						corner.translateXYZ(xt, yt, zt);

						if (sector > 1) {
							corner.rotateZ(Math.toRadians(60 * (sector - 1)));
						}

						storePoint(_viewTriangles[sector - 1][stack - 1][view - 1][i], corner);
					}
				}
			}
		}
	}

	/**
	 * Cache projection edge lines used by sector views.
	 */
	private static void cacheProjectionEdges() {
		_projectionEdges = new double[PLANE_COUNT][VIEW_COUNT][EC_NUMSTRIP][PROJECTION_EDGE_COUNT][EDGE_ENDPOINT_COUNT][COORD_COUNT];

		for (int plane = 0; plane < PLANE_COUNT; plane++) {
			for (int view = 0; view < VIEW_COUNT; view++) {
				ECLayer ecLay = ecLayer[plane][view];

				for (int stripId = 0; stripId < EC_NUMSTRIP; stripId++) {
					ScintillatorPaddle strip = ecLay.getComponent(stripId);

					for (int edge = 0; edge < PROJECTION_EDGE_COUNT; edge++) {
						Line3D line = strip.getVolumeEdge(PROJECTION_EDGE_START + edge);
						storePoint(_projectionEdges[plane][view][stripId][edge][0], line.origin());
						storePoint(_projectionEdges[plane][view][stripId][edge][1], line.end());
					}
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
	 * plane.
	 *
	 * @param index the plane, EC_INNER or EC_OUTER
	 * @return normal vector
	 */
	public static Point3D getR0(int index) {
		return _r0[index];
	}

	/**
	 * Get the front plane of EC.
	 *
	 * @param sector the 1-based sector [1..6]
	 * @param plane  EC_INNER or EC_OUTER
	 * @return front plane
	 */
	public static Plane3D getFrontPlane(int sector, int plane) {
		Point3D clasr0 = new Point3D();
		GeometryManager.sectorToClas(sector, clasr0, _r0[plane]);
		Plane3D plane3D = new Plane3D(clasr0.x(), clasr0.y(), clasr0.z(), clasr0.x(), clasr0.y(), clasr0.z());
		return plane3D;
	}

	/**
	 * Get the coordinate transformation object.
	 *
	 * @param index the plane, EC_INNER or EC_OUTER
	 * @return transformations
	 */
	public static Transformations getTransformations(int index) {
		return _transformations[index];
	}

	/**
	 * Get a point from a U, V, or W strip.
	 *
	 * @param planeIndex EC_INNER or EC_OUTER
	 * @param stripType  EC_U, EC_V, or EC_W
	 * @param stripIndex strip index
	 * @param pointIndex point index
	 * @return strip point
	 */
	public static Point3D getStripPoint(int planeIndex, int stripType, int stripIndex, int pointIndex) {
		return _strips[planeIndex][stripType][stripIndex][pointIndex];
	}

	/**
	 * For the front face of a given plane, compute z from x.
	 *
	 * @param planeIndex EC_INNER or EC_OUTER
	 * @param x          x coordinate in cm
	 * @return z coordinate in cm
	 */
	public static double zFromX(int planeIndex, double x) {
		double x0 = _r0[planeIndex].x();
		double z0 = _r0[planeIndex].z();
		return z0 + (x - x0) / _slopes[planeIndex];
	}

	/**
	 * Obtain the shell for sector views.
	 *
	 * @param planeIndex      EC_INNER or EC_OUTER
	 * @param stripType       EC_U, EC_V, or EC_W
	 * @param projectionPlane projection plane
	 * @return shell points
	 */
	public static Point2D.Double[] getShell(int planeIndex, int stripType, Plane3D projectionPlane) {
		Point2D.Double wp[] = GeometryManager.allocate(4);

		int lastIndex = EC_NUMSTRIP - 1;
		Point2D.Double lastPP[] = getIntersections(planeIndex, stripType, lastIndex, projectionPlane, true);

		int firstIndex = 0;
		Point2D.Double firstPP[] = getIntersections(planeIndex, stripType, firstIndex, projectionPlane, true);

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
	 * Converts 1-based uvw triplets to a pixel.
	 *
	 * @param u 1-based U strip
	 * @param v 1-based V strip
	 * @param w 1-based W strip
	 * @return pixel index
	 */
	public static int pixelFromUVW(int u, int v, int w) {
		return (u * (u - 1) + v - w + 1);
	}

	/**
	 * Convert local ijk coordinates to sector xyz.
	 *
	 * @param plane     EC_INNER or EC_OUTER
	 * @param localP    local coordinates
	 * @param sectorXYZ sector coordinates
	 */
	public static void ijkToSectorXYZ(int plane, Point3D localP, double[] sectorXYZ) {
		if (plane < 0 || plane > 1) {
			throw new RuntimeException("EC Geometry [ijkToSectorXYZ] plane must be 0 or 1");
		}

		Point3D sectorP = new Point3D();
		_transformations[plane].localToSector(localP, sectorP);

		sectorXYZ[0] = sectorP.x();
		sectorXYZ[1] = sectorP.y();
		sectorXYZ[2] = sectorP.z();
	}

	/**
	 * Get the triangle for a given 3D view.
	 *
	 * @param sector sector 1..6
	 * @param stack  stack/superlayer 1..2
	 * @param view   view/layer 1..3
	 * @param coords receives [x1,y1,z1,...,x3,y3,z3]
	 */
	public static void getViewTriangle(int sector, int stack, int view, float coords[]) {
		if ((coords == null) || (coords.length < 9)) {
			return;
		}

		if ((sector < 1) || (sector > SECTOR_COUNT) || (stack < 1) || (stack > PLANE_COUNT) || (view < 1)
				|| (view > VIEW_COUNT)) {
			return;
		}

		double triangle[][] = _viewTriangles[sector - 1][stack - 1][view - 1];

		for (int i = 0; i < TRIANGLE_POINT_COUNT; i++) {
			int j = 3 * i;
			coords[j] = (float) triangle[i][0];
			coords[j + 1] = (float) triangle[i][1];
			coords[j + 2] = (float) triangle[i][2];
		}
	}

	/**
	 * Check whether a projected strip polygon intersects the plane.
	 *
	 * @param superlayer      EC_INNER or EC_OUTER
	 * @param layer           EC_U, EC_V, or EC_W
	 * @param stripid         0-based strip id
	 * @param projectionPlane projection plane
	 * @return true if intersecting
	 */
	public static boolean doesProjectedPolyFullyIntersect(int superlayer, int layer, int stripid,
			Plane3D projectionPlane) {
		if (!validStrip(superlayer, layer, stripid)) {
			return false;
		}

		return GeometryManager.doesProjectedPolyIntersect(_projectionEdges[superlayer][layer][stripid],
				projectionPlane);
	}

	/**
	 * Get projected strip intersections.
	 *
	 * @param superlayer      EC_INNER or EC_OUTER
	 * @param layer           EC_U, EC_V, or EC_W
	 * @param stripid         0-based strip id
	 * @param projectionPlane projection plane
	 * @param offset          true to apply display offsets
	 * @return projected points
	 */
	public static Point2D.Double[] getIntersections(int superlayer, int layer, int stripid, Plane3D projectionPlane,
			boolean offset) {
		if (!validStrip(superlayer, layer, stripid)) {
			return null;
		}

		Point2D.Double wp[] = GeometryManager.allocate(4);
		GeometryManager.getProjectedPolygon(_projectionEdges[superlayer][layer][stripid], projectionPlane,  wp, null);

		Point2D.Double p2d[] = new Point2D.Double[4];

		p2d[0] = new Point2D.Double(wp[2].x, wp[2].y);
		p2d[1] = new Point2D.Double(wp[3].x, wp[3].y);
		p2d[2] = new Point2D.Double(wp[0].x, wp[0].y);
		p2d[3] = new Point2D.Double(wp[1].x, wp[1].y);

		if (offset) {
			if (layer == EC_V) {
				double del = _deltaK[superlayer] / 3;
				offsetLine(p2d[0], p2d[1], del - 1);
				offsetLine(p2d[2], p2d[3], del - 1);
			} else if (layer == EC_W) {
				double del = 2 * _deltaK[superlayer] / 3;
				offsetLine(p2d[0], p2d[1], del - 2);
				offsetLine(p2d[2], p2d[3], del - 2);
			}

			offsetLine(p2d[2], p2d[3], (0.9 * (_deltaK[superlayer] / 3)) - 1);
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
	 * @param superlayer EC_INNER or EC_OUTER
	 * @param layer      EC_U, EC_V, or EC_W
	 * @param stripid    strip id
	 * @return true if valid
	 */
	private static boolean validStrip(int superlayer, int layer, int stripid) {
		return (superlayer >= 0) && (superlayer < PLANE_COUNT) && (layer >= 0) && (layer < VIEW_COUNT)
				&& (stripid >= 0) && (stripid < EC_NUMSTRIP);
	}

	/**
	 * Read EC geometry from explicit primitive cache data.
	 *
	 * @param kryo  retained for interface compatibility
	 * @param input cache input
	 * @return true if successful
	 */
	@Override
	public boolean readGeometry(Kryo kryo, Input input) {
		try {
			readR0SlopesAndDeltaK(input);

			_strips = readLocalStrips(input);
			_viewTriangles = readViewTriangles(input);
			_projectionEdges = readProjectionEdges(input);

			_transformations = new Transformations[PLANE_COUNT];
			_transformations[EC_INNER] = new Transformations(DetectorType.EC_INNER);
			_transformations[EC_OUTER] = new Transformations(DetectorType.EC_OUTER);

			ecLayer = null;
			ecLayerLocal = null;

			recomputeAngles();

			return true;
		} catch (Exception e) {
			System.err.println("ECGeometry: Error reading geometry cache: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Write EC geometry as explicit primitive cache data.
	 *
	 * @param kryo   retained for interface compatibility
	 * @param output cache output
	 * @return true if successful
	 */
	@Override
	public boolean writeGeometry(Kryo kryo, Output output) {
		try {
			writeR0SlopesAndDeltaK(output);
			writeLocalStrips(output);
			writeViewTriangles(output);
			writeProjectionEdges(output);

			return true;
		} catch (Exception e) {
			System.err.println("ECGeometry: Error writing geometry cache: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Write r0, slopes, and deltaK.
	 *
	 * @param output cache output
	 */
	private static void writeR0SlopesAndDeltaK(Output output) {
		output.writeInt(PLANE_COUNT);

		for (int plane = 0; plane < PLANE_COUNT; plane++) {
			writePoint(output, _r0[plane]);
			output.writeDouble(_slopes[plane]);
			output.writeDouble(_deltaK[plane]);
		}
	}

	/**
	 * Read r0, slopes, and deltaK.
	 *
	 * @param input cache input
	 */
	private static void readR0SlopesAndDeltaK(Input input) {
		int planeCount = input.readInt();
		require(planeCount == PLANE_COUNT, "ECGeometry: plane count mismatch.");

		_r0 = new Point3D[PLANE_COUNT];
		_slopes = new double[PLANE_COUNT];
		_deltaK = new double[PLANE_COUNT];

		for (int plane = 0; plane < PLANE_COUNT; plane++) {
			_r0[plane] = readPoint(input, "EC r0 plane " + plane);
			_slopes[plane] = input.readDouble();
			_deltaK[plane] = input.readDouble();
		}
	}

	/**
	 * Write local strip points.
	 *
	 * @param output cache output
	 */
	private static void writeLocalStrips(Output output) {
		output.writeInt(PLANE_COUNT);
		output.writeInt(VIEW_COUNT);

		for (int plane = 0; plane < PLANE_COUNT; plane++) {
			for (int view = 0; view < VIEW_COUNT; view++) {
				output.writeInt(EC_NUMSTRIP);

				for (int strip = 0; strip < EC_NUMSTRIP; strip++) {
					writePointBlock(output, _strips[plane][view][strip], LOCAL_STRIP_POINT_COUNT,
							"EC local strip plane " + plane + " view " + view + " strip " + strip);
				}
			}
		}
	}

	/**
	 * Read local strip points.
	 *
	 * @param input cache input
	 * @return local strips
	 */
	private static Point3D[][][][] readLocalStrips(Input input) {
		int planeCount = input.readInt();
		int viewCount = input.readInt();

		require(planeCount == PLANE_COUNT, "EC local strip plane count mismatch.");
		require(viewCount == VIEW_COUNT, "EC local strip view count mismatch.");

		Point3D strips[][][][] = new Point3D[PLANE_COUNT][VIEW_COUNT][EC_NUMSTRIP][LOCAL_STRIP_POINT_COUNT];

		for (int plane = 0; plane < PLANE_COUNT; plane++) {
			for (int view = 0; view < VIEW_COUNT; view++) {
				int stripCount = input.readInt();
				require(stripCount == EC_NUMSTRIP, "EC local strip count mismatch.");

				for (int strip = 0; strip < stripCount; strip++) {
					strips[plane][view][strip] = readPointBlock(input, LOCAL_STRIP_POINT_COUNT,
							"EC local strip plane " + plane + " view " + view + " strip " + strip);
				}
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
		output.writeInt(PLANE_COUNT);
		output.writeInt(VIEW_COUNT);

		for (int sector = 0; sector < SECTOR_COUNT; sector++) {
			for (int stack = 0; stack < PLANE_COUNT; stack++) {
				for (int view = 0; view < VIEW_COUNT; view++) {
					GeometryPrimitiveIO.writeCorners(output, _viewTriangles[sector][stack][view],
							TRIANGLE_POINT_COUNT, COORD_COUNT);
				}
			}
		}
	}

	/**
	 * Read 3D view triangles.
	 *
	 * @param input cache input
	 * @return view triangles
	 */
	private static double[][][][][] readViewTriangles(Input input) {
		int sectorCount = input.readInt();
		int stackCount = input.readInt();
		int viewCount = input.readInt();

		require(sectorCount == SECTOR_COUNT, "EC view triangle sector count mismatch.");
		require(stackCount == PLANE_COUNT, "EC view triangle stack count mismatch.");
		require(viewCount == VIEW_COUNT, "EC view triangle view count mismatch.");

		double data[][][][][] = new double[SECTOR_COUNT][PLANE_COUNT][VIEW_COUNT][][];

		for (int sector = 0; sector < SECTOR_COUNT; sector++) {
			for (int stack = 0; stack < PLANE_COUNT; stack++) {
				for (int view = 0; view < VIEW_COUNT; view++) {
					data[sector][stack][view] = GeometryPrimitiveIO.readCorners(input, TRIANGLE_POINT_COUNT,
							COORD_COUNT, "EC view triangle sector " + sector + " stack " + stack + " view " + view);
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
		output.writeInt(PLANE_COUNT);
		output.writeInt(VIEW_COUNT);

		for (int plane = 0; plane < PLANE_COUNT; plane++) {
			for (int view = 0; view < VIEW_COUNT; view++) {
				output.writeInt(EC_NUMSTRIP);

				for (int strip = 0; strip < EC_NUMSTRIP; strip++) {
					output.writeInt(PROJECTION_EDGE_COUNT);

					for (int edge = 0; edge < PROJECTION_EDGE_COUNT; edge++) {
						GeometryPrimitiveIO.writeCorners(output, _projectionEdges[plane][view][strip][edge],
								EDGE_ENDPOINT_COUNT, COORD_COUNT);
					}
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
	private static double[][][][][][] readProjectionEdges(Input input) {
		int planeCount = input.readInt();
		int viewCount = input.readInt();

		require(planeCount == PLANE_COUNT, "EC projection edge plane count mismatch.");
		require(viewCount == VIEW_COUNT, "EC projection edge view count mismatch.");

		double data[][][][][][] = new double[PLANE_COUNT][VIEW_COUNT][EC_NUMSTRIP][][][];

		for (int plane = 0; plane < PLANE_COUNT; plane++) {
			for (int view = 0; view < VIEW_COUNT; view++) {
				int stripCount = input.readInt();
				require(stripCount == EC_NUMSTRIP,
						"EC projection edge strip count mismatch for plane " + plane + " view " + view);

				for (int strip = 0; strip < stripCount; strip++) {
					int edgeCount = input.readInt();
					require(edgeCount == PROJECTION_EDGE_COUNT,
							"EC projection edge count mismatch for plane " + plane + " view " + view + " strip "
									+ strip);

					data[plane][view][strip] = new double[PROJECTION_EDGE_COUNT][][];

					for (int edge = 0; edge < PROJECTION_EDGE_COUNT; edge++) {
						data[plane][view][strip][edge] = GeometryPrimitiveIO.readCorners(input, EDGE_ENDPOINT_COUNT,
								COORD_COUNT,
								"EC projection edge plane " + plane + " view " + view + " strip " + strip + " edge "
										+ edge);
					}
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
			throw new IllegalArgumentException("ECGeometry: cannot write null point.");
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
	 * @param output  cache output
	 * @param points  points
	 * @param count   expected count
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