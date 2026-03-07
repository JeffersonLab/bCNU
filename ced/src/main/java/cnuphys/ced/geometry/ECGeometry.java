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
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Triangle3D;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import cnuphys.ced.geometry.cache.ACachedGeometry;

/**
 * Holds the EC geometry from the geometry packages
 *
 * @author heddle
 *
 */
public class ECGeometry extends ACachedGeometry {

	/** constant for the inner stack EC */
	public static final int EC_INNER = 0;

	/** constant for the outer stack EC */
	public static final int EC_OUTER = 1;

	/** constant for the u view index */
	public static final int EC_U = 0;

	/** constant for the u view index */
	public static final int EC_V = 1;

	/** constant for the u view index */
	public static final int EC_W = 2;

	// ** stack names names */
	public static final String PLANE_NAMES[] = { "Inner", "Outer" };

	// ** plane or "view" names */
	public static final String VIEW_NAMES[] = { "U", "V", "W" };

	/** there are 36 strips for u, v and w */
	public static final int EC_NUMSTRIP = 36;

	public static final String layerNames[] = { "???", "PCAL_U", "PCAL_V", "PCAL_W", "ECAL_IN_U", "ECAL_IN_V",
			"ECAL_IN_W", "ECAL_OUT_U", "ECAL_OUT_V", "ECAL_OUT_W" };

	// deltaK separating front of inner from front of outer
	private static double[] _deltaK = new double[2];

	// the normal vector in sector xyz (cm) from the nominal target to the
	// front planes of the inner EC. All coordinates are in cm. First index
	// [0,1]
	// is for plane second is for coordinate
	private static Point3D _r0[] = new Point3D[2];

	// The strips. First index is for plane (inner and outer)
	// second index if for strip stype EC_U, EC_V or EC_W,
	// third index is for the strip index [0..35]
	// and the fourth index is the point index [0..3]
	private static Point3D[][][][] _strips = new Point3D[2][3][EC_NUMSTRIP][4];

	// angles related to the _ro
	public static double THETA = Double.NaN; // radians
	public static double COSTHETA = Double.NaN;
	public static double SINTHETA = Double.NaN;
	public static double TANTHETA = Double.NaN;

	// slopes of front planes
	private static double[] _slopes = { Double.NaN, Double.NaN };

	private static Transformations _transformations[];

	// layers in clas and local coordinates
	private static ECLayer[][] ecLayerLocal;
	private static ECLayer[][] ecLayer;

	public ECGeometry() {
		super("ECGeometry");
	}

	/**
	 * Initialize the EC Geometry
	 */
	@Override
	public void initializeUsingCCDB() {

		System.out.println("\n=====================================");
		System.out.println("====  EC Geometry Initialization ====");
		System.out.println("=====================================");

		ConstantProvider ecDataProvider = GeometryFactory.getConstants(org.jlab.detector.base.DetectorType.ECAL);
		ECDetector clas_Cal_Detector = (new ECFactory()).createDetectorCLAS(ecDataProvider);

		// cal sector 0 in clas coordinates
		ECSector clas_Cal_Sector0 = clas_Cal_Detector.getSector(0);

		// in local coordinates
		ECSector local_Cal_Sector0 = (new ECFactory()).createDetectorLocal(ecDataProvider).getSector(0);

		// CLAS system
		ECSuperlayer ecSuperlayer[] = new ECSuperlayer[2];
		ecSuperlayer[EC_INNER] = clas_Cal_Sector0.getSuperlayer(1);
		ecSuperlayer[EC_OUTER] = clas_Cal_Sector0.getSuperlayer(2);
		ecLayer = new ECLayer[2][3];
		for (int plane = 0; plane < 2; plane++) {
			for (int stripType = 0; stripType < 3; stripType++) {
				ecLayer[plane][stripType] = ecSuperlayer[plane].getLayer(stripType);
			}
		}

		// LOCAL SYStem
		ECSuperlayer ecSuperLayerLocal[] = new ECSuperlayer[2];
		ecSuperLayerLocal[EC_INNER] = local_Cal_Sector0.getSuperlayer(1);
		ecSuperLayerLocal[EC_OUTER] = local_Cal_Sector0.getSuperlayer(2);
		ecLayerLocal = new ECLayer[2][3];
		for (int plane = 0; plane < 2; plane++) {
			for (int stripType = 0; stripType < 3; stripType++) {
				ecLayerLocal[plane][stripType] = ecSuperLayerLocal[plane].getLayer(stripType);
			}
		}

		createTransformations();
		getStripsAndTriangles();
	} // initialize

	// create the transformations FOR INNER AND OUTER
	private static void createTransformations() {
		_transformations = new Transformations[2];
		_transformations[EC_INNER] = new Transformations(DetectorType.EC_INNER);
		_transformations[EC_OUTER] = new Transformations(DetectorType.EC_OUTER);

		for (int plane = 0; plane < 2; plane++) {
			_r0[plane] = new Point3D(0, 0, 0);
			_transformations[plane].localToSector(_r0[plane]);
		}

		THETA = Math.atan2(_r0[EC_INNER].x(), _r0[EC_INNER].z());
		COSTHETA = Math.cos(THETA);
		SINTHETA = Math.sin(THETA);
		TANTHETA = Math.tan(THETA);

	}

	private static void getStripsAndTriangles() {
		Point3D zeroP = new Point3D(0, 0, 0);

		double rmag[] = new double[2];

		rmag[0] = _r0[0].distance(zeroP);
		rmag[1] = _r0[1].distance(zeroP);

		_deltaK[EC_INNER] = rmag[1] - rmag[0];
		_deltaK[EC_OUTER] = 1.5 * _deltaK[EC_INNER];

		// get the strips
		// The strips. First index is for plane (inner and outer)
		// second index if for strip stype EC_U, EC_V or EC_W,
		// third index is for the strip index [0..35]
		// and the fourth index is the point index [0..3]

		double minI[] = { Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY };
		double maxI[] = { Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY };
		double minJ[] = { Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY };
		double maxJ[] = { Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY };

		for (int plane = 0; plane < 2; plane++) {
			for (int stripType = 0; stripType < 3; stripType++) {
				for (int stripId = 0; stripId < EC_NUMSTRIP; stripId++) {
					ScintillatorPaddle strip = ecLayerLocal[plane][stripType].getComponent(stripId);
					_strips[plane][stripType][stripId][0] = strip.getVolumePoint(4);
					_strips[plane][stripType][stripId][1] = strip.getVolumePoint(5);
					_strips[plane][stripType][stripId][2] = strip.getVolumePoint(1);
					_strips[plane][stripType][stripId][3] = strip.getVolumePoint(0);

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
		} // plane loop

			for (int plane = 0; plane < 2; plane++) {
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
	 * Get the normal vector in sector xyz (cm) from the nominal target to the front
	 * plane of the inner EC. All coordinates are in cm.
	 *
	 * @param index the plane, EC_INNER or EC_OUTER
	 * @return the normal vector for the given plane
	 */
	public static Point3D getR0(int index) {
		return _r0[index];
	}

	/**
	 * Get the front plane of the PCAL
	 *
	 * @param sector the 1-based sector [1..6]
	 * @param plane  EC_INNER or EC_OUTER
	 * @return the front plane of the PCAL
	 */
	public static Plane3D getFrontPlane(int sector, int plane) {
		Point3D clasr0 = new Point3D();
		GeometryManager.sectorToClas(sector, clasr0, _r0[plane]);
		Plane3D plane3D = new Plane3D(clasr0.x(), clasr0.y(), clasr0.z(), clasr0.x(), clasr0.y(), clasr0.z());
		return plane3D;
	}

	/**
	 * Get the coordinate transformation object
	 *
	 * @param index the plane, EC_INNER or EC_OUTER
	 * @return the coordinate transformations
	 */
	public static Transformations getTransformations(int index) {
		return _transformations[index];
	}

	/**
	 * Get a point from a u, v or w strip
	 *
	 * @param planeIndex either EC_INNER or EC_OUTER [0, 1]
	 * @param stripType  EC_U, EC_V, or EC_W [0..2]
	 * @param stripIndex the strip index [0..(EC_NUMSTRIP-1)]
	 * @param pointIndex the point index [0..3]
	 * @return the point (corner) of a strip
	 */
	public static Point3D getStripPoint(int planeIndex, int stripType, int stripIndex, int pointIndex) {
		return _strips[planeIndex][stripType][stripIndex][pointIndex];
	}

	/**
	 * For the front face of a given plane, compute z from x.
	 * Used by hex view.
	 *
	 * @param planeIndex EC_INNER or EC_OUTER [0,1]
	 * @param x          the x coordinate in cm
	 * @return the z coordinate in cm
	 */
	public static double zFromX(int planeIndex, double x) {
		double x0 = _r0[planeIndex].x();
		double z0 = _r0[planeIndex].z();
		return z0 + (x - x0) / _slopes[planeIndex];
	}

	/**
	 * Obtain the shell (for sector views) for the whole inner or outer EC correct
	 * for the relative phi. Used by sector views.
	 *
	 * @param planeIndex      should be EC_INNER or EC_OUTER
	 * @param stripType       should be EC_U, EC_V, or EC_W
	 * @param projectionPlane the projection plane
	 * @return the shell for the whole panel.
	 */
	public static Point2D.Double[] getShell(int planeIndex, int stripType, Plane3D projectionPlane) {

		Point2D.Double wp[] = GeometryManager.allocate(4);

		// get last visible (intersecting) strip
		int lastIndex = EC_NUMSTRIP - 1;

		Point2D.Double lastPP[] = null;
		lastPP = getIntersections(planeIndex, stripType, lastIndex, projectionPlane, true);

		int firstIndex = 0;

		Point2D.Double firstPP[] = null;
		firstPP = getIntersections(planeIndex, stripType, firstIndex, projectionPlane, true);

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
	 * Converts 1-based uvw triplets to a pixel. NOTE: not all uvw triplets are
	 * "real". For example, there is no (36, 36, 36) triplet; those strips do not
	 * intersect. Proper triplets have u + w + w = (2N+1) or (2N+2), where N = 36.
	 * Possible uvw triplets always yield a positive pixel value from [1..1296].
	 *
	 * @param u the 1.based [1..36] u strip
	 * @param v the 1 based [1..36] v strip
	 * @param w the 1 based [1..36] w strip
	 * @return the pixel. Should be [1..1296]
	 */
	public static int pixelFromUVW(int u, int v, int w) {
		return (u * (u - 1) + v - w + 1);
	}

	/**
	 * Convert ijk coordinates to sector xyz
	 *
	 * @param localP    the ijk coordinates
	 * @param sectorXYZ the sector xyz coordinates
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
	 * Get the triangle for a given view for 3D
	 *
	 * @param sector the sector 1..6
	 * @param stack  (aka the superlayer) 1..2 for inner and outer
	 * @param view   (aka layer) 1..3 for u, v, w
	 * @param coords will hold the corners as [x1, y1, z1, ..., x3, y3, z3]
	 */
	public static void getViewTriangle(int sector, int stack, int view, float coords[]) {
		// argh the geometry pakage superlayers are 1,2 rather than 0,1 because
		// they use 0 for PCAL. So stack does not need the -1, but view still
		// does.

		if (stack < 1 || stack > 2) {
			throw new RuntimeException("EC Geometry [getViewTriangle] stack must be 1 or 2");
		}

		ECLayer ecLay = ecLayer[stack - 1][view - 1];

		// NOTE, each ec layer has one face, a triangle

		Triangle3D t3d = (Triangle3D) ecLay.getBoundary().face(0);

		// translation

		double delK = _deltaK[stack - 1];

		double dist = (view - 1) * (delK / 3);
		double xt = dist * Math.sin(Math.toRadians(25));
		double yt = 0;
		double zt = dist * Math.cos(Math.toRadians(25));

		for (int i = 0; i < 3; i++) {
			int j = 3 * i;
			Point3D corner = new Point3D(t3d.point(i));
			corner.translateXYZ(xt, yt, zt);

			if (sector > 1) {
				corner.rotateZ(Math.toRadians(60 * (sector - 1)));
			}

			coords[j] = (float) corner.x();
			coords[j + 1] = (float) corner.y();
			coords[j + 2] = (float) corner.z();
		}
	}

	/**
	 *
	 * @param superlayer      0, 1 (EC_INNER or EC_OUTER)
	 * @param layer           EC_U, EC_V, EC_W
	 * @param stripid         the 0-based paddle id
	 * @param projectionPlane the projection plane
	 * @return <code>true</code> if the projected polygon fully intersects the plane
	 */
	public static boolean doesProjectedPolyFullyIntersect(int superlayer, int layer, int stripid,
			Plane3D projectionPlane) {

		ECLayer ecLay = ecLayer[superlayer][layer];
		ScintillatorPaddle strip = ecLay.getComponent(stripid);
		return GeometryManager.doesProjectedPolyIntersect(strip, projectionPlane, 6, 4);
	}

	/**
	 * Get the intersections of a with a constant phi plane. If the paddle does not
	 * intersect (happens as phi grows) return null;
	 *
	 * @param superlayer      0, 1 (EC_INNER or EC_OUTER)
	 * @param layer           EC_U, EC_V, EC_W
	 * @param stripid         the 0-based paddle id
	 * @param projectionPlane the projection plane
	 * @return the intersection points (z component will be 0).
	 */
	public static Point2D.Double[] getIntersections(int superlayer, int layer, int stripid, Plane3D projectionPlane,
			boolean offset) {

		ECLayer ecLay = ecLayer[superlayer][layer];
		ScintillatorPaddle strip = ecLay.getComponent(stripid);

		Point2D.Double wp[] = GeometryManager.allocate(4);
		boolean isects = GeometryManager.getProjectedPolygon(strip, projectionPlane, 6, 4, wp, null);
		

		// note reordering
		Point2D.Double p2d[] = new Point2D.Double[4];
		p2d[0] = new Point2D.Double(wp[2].x, wp[2].y);
		p2d[1] = new Point2D.Double(wp[3].x, wp[3].y);
		p2d[2] = new Point2D.Double(wp[0].x, wp[0].y);
		p2d[3] = new Point2D.Double(wp[1].x, wp[1].y);

		if (offset) {
			// move
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

	private static void offsetLine(Point2D.Double start, Point2D.Double end, double len) {
		double delx = len * COSTHETA;
		double dely = len * SINTHETA;
		start.x += delx;
		start.y += dely;
		end.x += delx;
		end.y += dely;
	}

	@Override
	public boolean readGeometry(Kryo kryo, Input input) {
		try {
			// Read ecLayer (2D array)
			int outer = input.readInt();
			ecLayer = new ECLayer[outer][];
			for (int i = 0; i < outer; i++) {
				int inner = input.readInt();
				ecLayer[i] = new ECLayer[inner];
				for (int j = 0; j < inner; j++) {
					ecLayer[i][j] = kryo.readObjectOrNull(input, ECLayer.class);
				}
			}

			// Read ecLayerLocal (2D array)
			outer = input.readInt();
			ecLayerLocal = new ECLayer[outer][];
			for (int i = 0; i < outer; i++) {
				int inner = input.readInt();
				ecLayerLocal[i] = new ECLayer[inner];
				for (int j = 0; j < inner; j++) {
					ecLayerLocal[i][j] = kryo.readObjectOrNull(input, ECLayer.class);
				}
			}

			// Read _strips (4D array)
			outer = input.readInt(); // first dimension (should be 2)
			_strips = new Point3D[outer][][][];
			for (int i = 0; i < outer; i++) {
				int dim2 = input.readInt(); // second dimension (should be 3)
				_strips[i] = new Point3D[dim2][][];
				for (int j = 0; j < dim2; j++) {
					int dim3 = input.readInt(); // third dimension (should be EC_NUMSTRIP, 36)
					_strips[i][j] = new Point3D[dim3][];
					for (int k = 0; k < dim3; k++) {
						int dim4 = input.readInt(); // fourth dimension (should be 4)
						_strips[i][j][k] = new Point3D[dim4];
						for (int l = 0; l < dim4; l++) {
							_strips[i][j][k][l] = kryo.readObjectOrNull(input, Point3D.class);
						}
					}
				}
			}

			// Read _transformations (array of Transformations)
			int tLength = input.readInt();
			_transformations = new Transformations[tLength];
			for (int i = 0; i < tLength; i++) {
				_transformations[i] = kryo.readObjectOrNull(input, Transformations.class);
			}

			// Read _slopes (double array)
			int dLength = input.readInt();
			_slopes = new double[dLength];
			for (int i = 0; i < dLength; i++) {
				_slopes[i] = input.readDouble();
			}

			// Read _r0 (Point3D array)
			dLength = input.readInt();
			_r0 = new Point3D[dLength];
			for (int i = 0; i < dLength; i++) {
				_r0[i] = kryo.readObjectOrNull(input, Point3D.class);
			}

			// Read _deltaK (double array)
			dLength = input.readInt();
			_deltaK = new double[dLength];
			for (int i = 0; i < dLength; i++) {
				_deltaK[i] = input.readDouble();
			}

			// Recompute dependent transformation parameters (if needed)
			THETA = Math.atan2(_r0[EC_INNER].x(), _r0[EC_INNER].z());
			COSTHETA = Math.cos(THETA);
			SINTHETA = Math.sin(THETA);
			TANTHETA = Math.tan(THETA);

			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public boolean writeGeometry(Kryo kryo, Output output) {
		try {
			// Write ecLayer (2D array: [2][3])
			output.writeInt(ecLayer.length); // outer dimension (should be 2)
			for (int i = 0; i < ecLayer.length; i++) {
				output.writeInt(ecLayer[i].length); // inner dimension (should be 3)
				for (int j = 0; j < ecLayer[i].length; j++) {
					kryo.writeObjectOrNull(output, ecLayer[i][j], ECLayer.class);
				}
			}

			// Write ecLayerLocal (2D array: [2][3])
			output.writeInt(ecLayerLocal.length);
			for (int i = 0; i < ecLayerLocal.length; i++) {
				output.writeInt(ecLayerLocal[i].length);
				for (int j = 0; j < ecLayerLocal[i].length; j++) {
					kryo.writeObjectOrNull(output, ecLayerLocal[i][j], ECLayer.class);
				}
			}

			// Write _strips (4D array: [2][3][EC_NUMSTRIP][4])
			output.writeInt(_strips.length); // outer dimension (should be 2)
			for (int i = 0; i < _strips.length; i++) {
				output.writeInt(_strips[i].length); // should be 3
				for (int j = 0; j < _strips[i].length; j++) {
					output.writeInt(_strips[i][j].length); // should be EC_NUMSTRIP (36)
					for (int k = 0; k < _strips[i][j].length; k++) {
						output.writeInt(_strips[i][j][k].length); // should be 4
						for (int l = 0; l < _strips[i][j][k].length; l++) {
							kryo.writeObjectOrNull(output, _strips[i][j][k][l], Point3D.class);
						}
					}
				}
			}

			// Write _transformations (array of Transformations, length 2)
			output.writeInt(_transformations.length);
			for (int i = 0; i < _transformations.length; i++) {
				kryo.writeObjectOrNull(output, _transformations[i], Transformations.class);
			}

			// Write _slopes (double array, length 2)
			output.writeInt(_slopes.length);
			for (int i = 0; i < _slopes.length; i++) {
				output.writeDouble(_slopes[i]);
			}

			// Write _r0 (Point3D array, length 2)
			output.writeInt(_r0.length);
			for (int i = 0; i < _r0.length; i++) {
				kryo.writeObjectOrNull(output, _r0[i], Point3D.class);
			}

			// Write _deltaK (double array, length 2)
			output.writeInt(_deltaK.length);
			for (int i = 0; i < _deltaK.length; i++) {
				output.writeDouble(_deltaK[i]);
			}

			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
