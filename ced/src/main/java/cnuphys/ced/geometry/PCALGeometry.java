package cnuphys.ced.geometry;

import java.awt.geom.Point2D;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

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


import cnuphys.ced.geometry.cache.ACachedGeometry;

/**
 * Holds the EC geometery from the geometry packages
 *
 * @author heddle
 *
 */
public class PCALGeometry extends ACachedGeometry {

	/** index for the geometry package */
	private static final int EC_PCAL = 0;

	/** constant for the u strip index */
	public static final int PCAL_U = 0;

	/** constant for the u strip index */
	public static final int PCAL_V = 1;

	/** constant for the u strip index */
	public static final int PCAL_W = 2;

	// ** plane or "view" names */
	public static final String PLANE_NAMES[] = { "U", "V", "W" };

	/** there are 36 strips for u, v and w */
	public static final int PCAL_NUMSTRIP[] = { 68, 62, 62 };

	// deltaK separating front of inner from front of outer
	private static double _deltaK = 14.94;

	// the normal vector in sector xyz (cm) from the nominal target to the
	// front plNE
	private static Point3D _r0;


	// The strips.
	// First index if for strip type U, V or W,
	// 2nd index is for the strip index [0..(62 or 68)]
	// and the third index is the point index [0..3]
	private static Point3D[][][] _strips = new Point3D[3][68][4];

	// angles related to the _ro
	public static double COSTHETA = Double.NaN;
	public static double SINTHETA = Double.NaN;


	// slopes of front planes
	private static double _slope = Double.NaN;

	// used for coordinate transformations
	private static Transformations _transformations;

	// layers in clas and local coordinates
	private static ECLayer[] ecLayerLocal;
	private static ECLayer[] ecLayer;

	private static double[][][][] viewTriangles;
	private static double[][][][][] stripVertices;
	private static double[][][][][] projectionEdges;

	public PCALGeometry() {
		super("PCALGeometry");
	}

	/**
	 * Initialize the Pcal Geometry
	 */
	@Override
	public void initializeUsingCCDB() {

		System.out.println("\n=====================================");
		System.out.println("===  PCAL Geometry Initialization ===");
		System.out.println("=====================================");

		ConstantProvider ecDataProvider = GeometryFactory.getConstants(org.jlab.detector.base.DetectorType.ECAL);
		ECDetector clas_Cal_Detector = (new ECFactory()).createDetectorCLAS(ecDataProvider);

		// cal sector 0 in clas coordinates
		ECSector clas_Cal_Sector0 = clas_Cal_Detector.getSector(0);

		// in local coordinates
		ECSector local_Cal_Sector0 = (new ECFactory()).createDetectorLocal(ecDataProvider).getSector(0);

		// CLAS system
		ECSuperlayer ecSuperlayer = clas_Cal_Sector0.getSuperlayer(EC_PCAL);

		ecLayer = new ECLayer[3];
		
		for (int stripType = 0; stripType < 3; stripType++) {
			ecLayer[stripType] = ecSuperlayer.getLayer(stripType);
		}

		// local system
		ECSuperlayer ecSuperlayerLocal = local_Cal_Sector0.getSuperlayer(EC_PCAL);

		ecLayerLocal = new ECLayer[3];

		for (int stripType = 0; stripType < 3; stripType++) {
			ecLayerLocal[stripType] = ecSuperlayerLocal.getLayer(stripType);
		}

		createTransformations();
		getStripsAndTriangles();
		captureDrawingGeometry();
	} // initialize

	private static void captureDrawingGeometry() {
		viewTriangles = new double[6][3][3][3];
		stripVertices = new double[6][3][68][8][3];
		projectionEdges = new double[3][68][4][2][3];
		for (int view = 0; view < 3; view++) {
			ECLayer layer = ecLayer[view];
			Triangle3D triangle = (Triangle3D) layer.getBoundary().face(0);
			double dist = view * (_deltaK / 3.0);
			double xt = dist * Math.sin(Math.toRadians(25));
			double zt = dist * Math.cos(Math.toRadians(25));
			for (int sector = 0; sector < 6; sector++) {
				for (int point = 0; point < 3; point++) {
					Point3D corner = new Point3D(triangle.point(point));
					corner.translateXYZ(xt, 0, zt);
					corner.rotateZ(Math.toRadians(60 * sector));
					storePoint(viewTriangles[sector][view][point], corner);
				}
				for (int strip = 0; strip < PCAL_NUMSTRIP[view]; strip++) {
					ScintillatorPaddle paddle = layer.getComponent(strip);
					for (int cornerIndex = 0; cornerIndex < 8; cornerIndex++) {
						Point3D corner = new Point3D(paddle.getVolumePoint(cornerIndex));
						corner.translateXYZ(xt, 0, zt);
						corner.rotateZ(Math.toRadians(60 * sector));
						storePoint(stripVertices[sector][view][strip][cornerIndex], corner);
					}
				}
			}
			for (int strip = 0; strip < PCAL_NUMSTRIP[view]; strip++) {
				ScintillatorPaddle paddle = layer.getComponent(strip);
				for (int edge = 0; edge < 4; edge++) {
					org.jlab.geom.prim.Line3D line = paddle.getVolumeEdge(6 + edge);
					storePoint(projectionEdges[view][strip][edge][0], line.origin());
					storePoint(projectionEdges[view][strip][edge][1], line.end());
				}
			}
		}
	}

	private static void storePoint(double[] destination, Point3D point) {
		destination[0] = point.x();
		destination[1] = point.y();
		destination[2] = point.z();
	}

	// create the transformations
	private static void createTransformations() {

		// obtain the transformations
		_transformations = new Transformations(DetectorType.PCAL);

		_r0 = new Point3D(0, 0, 0);
		_transformations.localToSector(_r0);

		double theta = Math.atan2(_r0.x(), _r0.z());
		COSTHETA = Math.cos(theta);
		SINTHETA = Math.sin(theta);
	}

	private static void getStripsAndTriangles() {
		double minI = Double.POSITIVE_INFINITY;
		double maxI = Double.NEGATIVE_INFINITY;
		double minJ = Double.POSITIVE_INFINITY;
		double maxJ = Double.NEGATIVE_INFINITY;

		for (int stripType = 0; stripType < 3; stripType++) {
			for (int stripId = 0; stripId < PCAL_NUMSTRIP[stripType]; stripId++) {
				ScintillatorPaddle strip = ecLayerLocal[stripType].getComponent(stripId);
				_strips[stripType][stripId][0] = strip.getVolumePoint(4);
				_strips[stripType][stripId][1] = strip.getVolumePoint(5);
				_strips[stripType][stripId][2] = strip.getVolumePoint(1);
				_strips[stripType][stripId][3] = strip.getVolumePoint(0);

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

		// sector view midplane shells

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
	 * Get the normal vector in sector xyz (cm) from the nominal target to the front
	 * plane of the inner EC. All coordinates are in cm.
	 *
	 * @return the normal vector for the given plane
	 */
	public static Point3D getR0() {
		return _r0;
	}

	/**
	 * Get the front plane of the PCAL
	 *
	 * @param sector the 1-based sector [1..6]
	 * @return the front plane of the PCAL
	 */
	public static Plane3D getFrontPlane(int sector) {
		Point3D clasr0 = new Point3D();
		GeometryManager.sectorToClas(sector, clasr0, _r0);
		Plane3D plane = new Plane3D(clasr0.x(), clasr0.y(), clasr0.z(), clasr0.x(), clasr0.y(), clasr0.z());
		return plane;
	}

	/**
	 * Get the coordinate transformation object
	 *
	 * @return the coordinate transformations
	 */
	public static Transformations getTransformations() {
		return _transformations;
	}

	/**
	 * Get a point from a u, v or w strip
	 *
	 * @param stripType  EC_U, EC_V, or EC_W [0..2]
	 * @param stripIndex the strip index [0..(PCAL_NUMSTRIP-1)]
	 * @param pointIndex the point index [0..3]
	 * @return
	 */
	public static Point3D getStripPoint(int stripType, int stripIndex, int pointIndex) {
		return _strips[stripType][stripIndex][pointIndex];
	}


	/**
	 * For the front face of a given plane, compute z from x
	 *
	 * @param x the x coordinate in cm
	 * @return the z coordinate in cm
	 */
	public static double zFromX(double x) {
		double x0 = _r0.x();
		double z0 = _r0.z();
		return z0 + (x - x0) / _slope;
	}

	/**
	 * Get the triangle for a given view for 3D
	 *
	 * @param sector the sector 1..6
	 * @param view   (aka layer) 1..3 for u, v, w
	 * @param coords will hold the corners as [x1, y1, z1, ..., x3, y3, z3]
	 */
	public static void getViewTriangle(int sector, int view, float coords[]) {
		for (int i = 0; i < 3; i++) {
			int j = 3 * i;
			coords[j] = (float) viewTriangles[sector - 1][view - 1][i][0];
			coords[j + 1] = (float) viewTriangles[sector - 1][view - 1][i][1];
			coords[j + 2] = (float) viewTriangles[sector - 1][view - 1][i][2];
		}
	}

	/**
	 * Get the strips for use by 3D view
	 *
	 * @param sector the sector 1..6
	 * @param view   (aka layer) 1..3 for u, v, w
	 * @param strip  1..36
	 * @param coords holds the eight corners as [x1, y1, z1..x8, y8, z8]
	 */
	public static void getStrip(int sector, int view, int strip, float coords[]) {
		for (int i = 0; i < 8; i++) {
			int j = 3 * i;
			coords[j] = (float) stripVertices[sector - 1][view - 1][strip - 1][i][0];
			coords[j + 1] = (float) stripVertices[sector - 1][view - 1][strip - 1][i][1];
			coords[j + 2] = (float) stripVertices[sector - 1][view - 1][strip - 1][i][2];
		}

	}

	/**
	 * Obtain the shell (for sector views) for the whole PCAL correct for the
	 * relative phi.
	 *
	 * @param stripType       should be PCAL_U, PCAL_V, or PCAL_W
	 * @param projectionPlane the projection plane
	 * @return the shell for the whole panel.
	 */
	public static Point2D.Double[] getShell(int stripType, Plane3D projectionPlane) {

		Point2D.Double wp[] = new Point2D.Double[4];
		for (int i = 0; i < 4; i++) {
			wp[i] = new Point2D.Double();
		}

		// get last visible (intersecting) strip
		int lastIndex = PCAL_NUMSTRIP[stripType] - 1;
		while (!doesProjectedPolyFullyIntersect(stripType, lastIndex, projectionPlane)) {
			lastIndex--;
			if (lastIndex < 1) {
				return null;
			}
		}

		Point2D.Double lastPP[] = null;
		lastPP = getIntersections(stripType, lastIndex, projectionPlane, true);

		int firstIndex = 0;

		while (!doesProjectedPolyFullyIntersect(stripType, firstIndex, projectionPlane)) {
			firstIndex++;
		}
		Point2D.Double firstPP[] = null;
		firstPP = getIntersections(stripType, firstIndex, projectionPlane, true);

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
	 * Convert ijk coordinates to sector xyz
	 *
	 * @param pijk      the ijk coordinates
	 * @param sectorXYZ the sector xyz coordinates
	 */
	public static void ijkToSectorXYZ(Point3D localP, double[] sectorXYZ) {

		Point3D sectorP = new Point3D();
		_transformations.localToSector(localP, sectorP);
		sectorXYZ[0] = sectorP.x();
		sectorXYZ[1] = sectorP.y();
		sectorXYZ[2] = sectorP.z();
	}

	/**
	 * @param layer           PCAL_U, PCAL_V, PCAL_W
	 * @param stripid         the 0-based paddle id
	 * @param projectionPlane the projection plane
	 * @return <code>true</code> if the projected polygon fully intersects the plane
	 */
	public static boolean doesProjectedPolyFullyIntersect(int layer, int stripid, Plane3D projectionPlane) {

		return GeometryManager.doesProjectedPolyIntersect(projectionEdges[layer][stripid], projectionPlane);
	}

	/**
	 * Get the intersections of a with a constant phi plane. If the paddle does not
	 * intersect (happens as phi grows) return null;
	 *
	 * @param layer           PCAL_U, PCAL_V, PCAL_W
	 * @param stripid         the 0-based paddle id
	 * @param projectionPlane the projection plane
	 * @return the intersection points (z component will be 0).
	 */
	public static Point2D.Double[] getIntersections(int layer, int stripid, Plane3D projectionPlane, boolean offset) {

		Point2D.Double wp[] = GeometryManager.allocate(4);
		GeometryManager.getProjectedPolygon(projectionEdges[layer][stripid], projectionPlane, wp, null);

		// note reordering
		Point2D.Double p2d[] = new Point2D.Double[4];

		p2d[0] = new Point2D.Double(wp[2].x, wp[2].y);
		p2d[1] = new Point2D.Double(wp[3].x, wp[3].y);
		p2d[2] = new Point2D.Double(wp[0].x, wp[0].y);
		p2d[3] = new Point2D.Double(wp[1].x, wp[1].y);

		if (offset) {
			// move
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

	private static void offsetLine(Point2D.Double start, Point2D.Double end, double len) {
		double delx = len * COSTHETA;
		double dely = len * SINTHETA;
		start.x += delx;
		start.y += dely;
		end.x += delx;
		end.y += dely;
	}

	@Override
	public boolean supportsCache() {
		return true;
	}

	@Override
	public void writeGeometry(DataOutput output) throws IOException {
		writePoint(output, new double[] {_r0.x(), _r0.y(), _r0.z()});
		output.writeDouble(COSTHETA);
		output.writeDouble(SINTHETA);
		output.writeDouble(_slope);
		writeAffine(output, _transformations.getLocalToSectorAffine());
		writeAffine(output, _transformations.getSectorToLocalAffine());
		for (int view = 0; view < 3; view++) {
			output.writeInt(PCAL_NUMSTRIP[view]);
			for (int strip = 0; strip < PCAL_NUMSTRIP[view]; strip++) {
				for (int point = 0; point < 4; point++) {
					Point3D p = _strips[view][strip][point];
					writePoint(output, new double[] {p.x(), p.y(), p.z()});
				}
				for (int edge = 0; edge < 4; edge++) {
					writePoint(output, projectionEdges[view][strip][edge][0]);
					writePoint(output, projectionEdges[view][strip][edge][1]);
				}
			}
		}
		for (int sector = 0; sector < 6; sector++) {
			for (int view = 0; view < 3; view++) {
				for (int point = 0; point < 3; point++) {
					writePoint(output, viewTriangles[sector][view][point]);
				}
				for (int strip = 0; strip < PCAL_NUMSTRIP[view]; strip++) {
					for (int corner = 0; corner < 8; corner++) {
						writePoint(output, stripVertices[sector][view][strip][corner]);
					}
				}
			}
		}
	}

	@Override
	public void readGeometry(DataInput input) throws IOException {
		double[] r0 = readPoint(input);
		_r0 = new Point3D(r0[0], r0[1], r0[2]);
		COSTHETA = input.readDouble();
		SINTHETA = input.readDouble();
		_slope = input.readDouble();
		_transformations = new Transformations(DetectorType.PCAL, readAffine(input), readAffine(input));
		_strips = new Point3D[3][68][4];
		projectionEdges = new double[3][68][4][2][3];
		for (int view = 0; view < 3; view++) {
			int count = input.readInt();
			if (count != PCAL_NUMSTRIP[view]) {
				throw new IOException("Unexpected PCAL strip count for view " + view + ": " + count);
			}
			for (int strip = 0; strip < count; strip++) {
				for (int point = 0; point < 4; point++) {
					double[] p = readPoint(input);
					_strips[view][strip][point] = new Point3D(p[0], p[1], p[2]);
				}
				for (int edge = 0; edge < 4; edge++) {
					projectionEdges[view][strip][edge][0] = readPoint(input);
					projectionEdges[view][strip][edge][1] = readPoint(input);
				}
			}
		}
		viewTriangles = new double[6][3][3][3];
		stripVertices = new double[6][3][68][8][3];
		for (int sector = 0; sector < 6; sector++) {
			for (int view = 0; view < 3; view++) {
				for (int point = 0; point < 3; point++) {
					viewTriangles[sector][view][point] = readPoint(input);
				}
				for (int strip = 0; strip < PCAL_NUMSTRIP[view]; strip++) {
					for (int corner = 0; corner < 8; corner++) {
						stripVertices[sector][view][strip][corner] = readPoint(input);
					}
				}
			}
		}
		ecLayer = null;
		ecLayerLocal = null;
	}

	private static void writeAffine(DataOutput output, double[][] affine) throws IOException {
		for (double[] row : affine) {
			for (double value : row) {
				output.writeDouble(value);
			}
		}
	}

	private static double[][] readAffine(DataInput input) throws IOException {
		double[][] affine = new double[3][4];
		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 4; column++) {
				affine[row][column] = input.readDouble();
			}
		}
		return affine;
	}

	private static void writePoint(DataOutput output, double[] point) throws IOException {
		for (double value : point) {
			output.writeDouble(value);
		}
	}

	private static double[] readPoint(DataInput input) throws IOException {
		return new double[] {input.readDouble(), input.readDouble(), input.readDouble()};
	}

}
