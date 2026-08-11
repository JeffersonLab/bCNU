package cnuphys.ced.geometry;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.jlab.geom.abs.AbstractComponent;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Path3D;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;

import cnuphys.ced.geometry.cache.GeometryCache;
import cnuphys.swim.SwimTrajectory;

public class GeometryManager {

	/**
	 * Singleton
	 */
	private static volatile GeometryManager instance;




	//work points
	private static Point3D[] _workPoints;
	static {
		_workPoints = new Point3D[8];
		for (int i = 0; i < 8; i++) {
			_workPoints[i] = new Point3D();
		}
	}

	// 0.866...
	private static final double ROOT3OVER2 = Math.sqrt(3) / 2;
	private static double cosPhi[] = { Double.NaN, 1, 0.5, -0.5, -1, -0.5, 0.5 };
	private static double sinPhi[] = { Double.NaN, 0, ROOT3OVER2, ROOT3OVER2, 0, -ROOT3OVER2, -ROOT3OVER2 };

	/**
	 * Private constructor for the singleton.
	 */
	private GeometryManager() {
		GeometryCache.initializeAllGeometry();
	}

	/**
	 * Public access to the singleton manager.
	 *
	 * @return the GeometryManager singleton.
	 */
	public static GeometryManager getInstance() {
		if (instance == null) {
			synchronized (GeometryManager.class) {
				if (instance == null) {
					instance = new GeometryManager();
				}
			}
		}
		return instance;
	}



	/**
	 * Get the sector [1..6] from the phi value
	 *
	 * @param phi the value of phi in degrees
	 * @return the sector [1..6]
	 */
	public static int getSector(double phi) {
		// convert phi to [0..360]

		while (phi < 0) {
			phi += 360.0;
		}
		while (phi > 360.0) {
			phi -= 360.0;
		}

		if ((phi > 30.0) && (phi <= 90.0)) {
			return 2;
		}
		if ((phi > 90.0) && (phi <= 150.0)) {
			return 3;
		}
		if ((phi > 150.0) && (phi <= 210.0)) {
			return 4;
		}
		if ((phi > 210.0) && (phi <= 270.0)) {
			return 5;
		}
		if ((phi > 270.0) && (phi <= 330.0)) {
			return 6;
		}
		return 1;
	}

	/**
	 * Cross two vectors and return result as a unit vector. Used to make planes.
	 * @param u one vector
	 * @param v other vector
	 * @return u x v as a unit vector
	 */
	public static Vector3D normal(Vector3D u, Vector3D v) {
		return u.cross(v).asUnit();
	}

	/**
	 * Get the sector [1..6] from the lab x and y coordinates
	 *
	 * @param labX the lab x
	 * @param labY the lab y
	 * @return the sector [1..6]
	 */
	public static int getSector(double labX, double labY) {
		double phi = Math.atan2(labY, labX);
		return getSector(Math.toDegrees(phi));
	}

	/**
	 * Obtains the relative phi (relative to the midplane of the appropriate sector)
	 *
	 * @param absPhi the absolute value of phi
	 * @return the relative phi (i.e., the "slider" value)
	 */
	public static double getRelativePhi(double absPhi) {
		while (absPhi < 360.0) {
			absPhi += 360.0;
		}

		while (absPhi > 30.0) {
			absPhi -= 60.0;
		}

		return absPhi;
	}


	/**
	 * Obtain the 1-based sector from the xyz coordinates
	 *
	 * @param clasP the lab xyz coordinates
	 * @return the sector [1..6]
	 */
	public static int clasToSectorNumber(Point3D clasP) {
		double phi = Math.toDegrees(Math.atan2(clasP.y(), clasP.x()));
		double ang = phi + 30;
		while (ang < 0) {
			ang += 360;
		}
		return 1 + (int) (ang / 60);
	}

	/**
	 * Converts the lab 3D coordinates to sector 3D coordinates
	 *
	 * @param clasP   the lab 3D Cartesian coordinates (not modified)
	 * @param sectorP the sector 3D Cartesian coordinates (modified)
	 */
	public static void clasToSector(Point3D clasP, Point3D sectorP) {
		int sector = clasToSectorNumber(clasP);
		if ((sector < 1) || (sector > 6)) {
			String wstr = "Bad sector: " + sector + " in clasToSector";
			System.err.println(wstr);
			return;
		}

		if (sector == 1) {
			sectorP.setX(clasP.x());
			sectorP.setY(clasP.y());
		} else {
			double x = clasP.x();
			double y = clasP.y();
			double midPlanePhi = Math.toRadians(60 * (sector - 1));
			double cosPhi = Math.cos(midPlanePhi);
			double sinPhi = Math.sin(midPlanePhi);
			sectorP.setX(cosPhi * x + sinPhi * y);
			sectorP.setY(-sinPhi * x + cosPhi * y);
		}

		// z coordinates are the same
		sectorP.setZ(clasP.z());
	}

	/**
	 * Converts the lab 3D coordinates to sector 3D coordinates
	 *
	 * @param x       the lab (clas) x coordinate
	 * @param y       the lab (clas) y coordinate
	 * @param z       the lab (clas) z coordinate
	 * @param sectorP the sector 3D Cartesian coordinates (modified)
	 */
	public static void clasToSector(double x, double y, double z, Point3D sectorP) {
		Point3D clasP = new Point3D(x, y, z);
		clasToSector(clasP, sectorP);
	}

	/**
	 * Converts the sector 3D coordinates to clas (lab) 3D coordinates
	 *
	 * @param sector  the 1-based sector [1..6]
	 * @param clasP   the lab 3D Cartesian coordinates (modified)
	 * @param sectorP the sector 3D Cartesian coordinates (not modified)
	 */

	public static void sectorToClas(int sector, Point3D clasP, Point3D sectorP) {

		if ((sector < 1) || (sector > 6)) {
			String wstr = "Bad sector: " + sector + " in sectorToClas";
			System.err.println(wstr);
			return;
		}

		if (sector == 1) {
			clasP.setX(sectorP.x());
			clasP.setY(sectorP.y());
		} else if (sector == 4) {
			clasP.setX(-sectorP.x());
			clasP.setY(-sectorP.y());
		} else { // sectors 2, 3, 5, 6
			double x = sectorP.x();
			double y = sectorP.y();
			double cosP = cosPhi[sector];
			double sinP = sinPhi[sector];

			clasP.setX(cosP * x - sinP * y);
			clasP.setY(sinP * x + cosP * y);
		}

		// z coordinates are the same
		clasP.setZ(sectorP.z());
	}

	/**
	 * Obtain the 1-based sector from the xyz coordinates
	 *
	 * @param labXYZ the lab xyz coordinates
	 * @return the sector [1..6]
	 */
	public static int labXYZToSectorNumber(double labXYZ[]) {
		double phi = Math.toDegrees(Math.atan2(labXYZ[1], labXYZ[0]));
		double ang = phi + 30;
		while (ang < 0) {
			ang += 360;
		}
		return 1 + (int) (ang / 60);
	}

	/**
	 * Converts the lab 3D coordinates to sector 3D coordinates
	 *
	 * @param labXYZ    the lab 3D Cartesian coordinates
	 * @param sectorXYZ the sector 3D Cartesian coordinates
	 */
	public static void labXYZToSectorXYZ(double labXYZ[], double sectorXYZ[]) {
		int sector = labXYZToSectorNumber(labXYZ);
		if ((sector < 1) || (sector > 6)) {
			String wstr = "Bad sector: " + sector + " in labXYZToSectorXYZ";
			System.err.println(wstr);
			return;
		}

		if (sector == 1) {
			sectorXYZ[0] = labXYZ[0];
			sectorXYZ[1] = labXYZ[1];
		} else {
			double x = labXYZ[0];
			double y = labXYZ[1];
			double midPlanePhi = Math.toRadians(60 * (sector - 1));
			double cosPhi = Math.cos(midPlanePhi);
			double sinPhi = Math.sin(midPlanePhi);
			sectorXYZ[0] = cosPhi * x + sinPhi * y;
			sectorXYZ[1] = -sinPhi * x + cosPhi * y;
		}

		// z coordinates are the same
		sectorXYZ[2] = labXYZ[2];
	}

	/**
	 * Converts the lab 3D coordinates to sector 3D coordinates
	 *
	 * @param sector    the 1-based sector [1..6]
	 * @param labXYZ    the lab 3D Cartesian coordinates
	 * @param sectorXYZ the sector 3D Cartesian coordinates
	 */
	public static void sectorXYZToLabXYZ(int sector, double labXYZ[], double sectorXYZ[]) {

		if ((sector < 1) || (sector > 6)) {
			String wstr = "Bad sector: " + sector + " in sectorXYZToLabXYZ";
			System.err.println(wstr);
			return;
		}

		if (sector == 1) {
			labXYZ[0] = sectorXYZ[0];
			labXYZ[1] = sectorXYZ[1];
		} else {
			double x = sectorXYZ[0];
			double y = sectorXYZ[1];
			double midPlanePhi = Math.toRadians(60 * (sector - 1));
			double cosPhi = Math.cos(midPlanePhi);
			double sinPhi = Math.sin(midPlanePhi);
			labXYZ[0] = cosPhi * x - sinPhi * y;
			labXYZ[1] = sinPhi * x + cosPhi * y;

		}

		// z coordinates are the same
		labXYZ[2] = sectorXYZ[2];
	}

	/**
	 * Get the front plane of a calorimeter detector
	 *
	 * @param sector     the 1-based sector
	 * @param superlayer 0,1,2 for PCAL, EC_INNER, EC_OUTER
	 * @return
	 */
	public static Plane3D getCalFrontPlane(int sector, int superlayer) {
		if (superlayer == 0) {
			return PCALGeometry.getFrontPlane(sector);
		} else {
			return ECGeometry.getFrontPlane(sector, superlayer - 1);
		}
	}

	/**
	 * Lab (CLAS) 3D Cartesian coordinates to world graphical coordinates.
	 *
	 * @param superlayer 0,1,2 for PCAL, EC_INNER, EC_OUTER
	 * @param labXYZ     the lab 3D coordinates
	 * @param wp         will hold the graphical world coordinates
	 */
	public static void cal_labXYZToWorld(int superlayer, double labXYZ[], Point2D.Double wp) {

		Point3D start = new Point3D(0, 0, 0);
		Point3D end = new Point3D(labXYZ[0], labXYZ[1], labXYZ[2]);

		int sector = getSector(labXYZ[0], labXYZ[1]);

		Plane3D plane = getCalFrontPlane(sector, superlayer);
		Line3D line3D = new Line3D(start, end);

		Point3D iPoint = new Point3D();
		plane.intersection(line3D, iPoint);
		wp.x = iPoint.x();
		wp.y = iPoint.y();
	}

	private static boolean lengthTest(double len, Point3D p0, Point3D p1, Point3D pint) {

		double lentest = p0.distance(pint);
		if (lentest > len) {
			return false;
		}

		lentest = p1.distance(pint);
		if (lentest > len) {
			return false;
		}

		return true;
	}

	/** Test intersections using explicit edge endpoints shaped [edge][endpoint][xyz]. */
	public static boolean doesProjectedPolyIntersect(double[][][] edgeLines, Plane3D projectionPlane) {
		if (edgeLines == null || projectionPlane == null) {
			return false;
		}
		int intersectionCount = 0;
		for (double[][] endpoints : edgeLines) {
			Line3D line = primitiveLine(endpoints);
			if (line != null) {
				Point3D intersection = new Point3D();
				projectionPlane.intersection(line, intersection);
				if (lengthTest(line.length(), line.origin(), line.end(), intersection)) {
					intersectionCount++;
				}
			}
		}
		return intersectionCount > 2;
	}

	/** Project explicit edge endpoints using the same convention as detector objects. */
	public static boolean getProjectedPolygon(double[][][] edgeLines, Plane3D projectionPlane,
			Point2D.Double[] wp, Point2D.Double centroid) {
		if (edgeLines == null || projectionPlane == null || wp == null || wp.length < edgeLines.length) {
			return false;
		}
		for (int index = 0; index < edgeLines.length; index++) {
			Line3D line = primitiveLine(edgeLines[index]);
			if (line == null) {
				return false;
			}
			Point3D intersection = new Point3D();
			projectionPlane.intersection(line, intersection);
			wp[index].x = intersection.z();
			wp[index].y = Math.hypot(intersection.x(), intersection.y());
		}
		if (centroid != null) {
			average(wp, centroid);
		}
		return doesProjectedPolyIntersect(edgeLines, projectionPlane);
	}

	private static Line3D primitiveLine(double[][] endpoints) {
		if (endpoints == null || endpoints.length != 2 || endpoints[0] == null || endpoints[1] == null
				|| endpoints[0].length != 3 || endpoints[1].length != 3) {
			return null;
		}
		return new Line3D(new Point3D(endpoints[0][0], endpoints[0][1], endpoints[0][2]),
				new Point3D(endpoints[1][0], endpoints[1][1], endpoints[1][2]));
	}

	/**
	 * See if the projected polygon intersects a plane
	 *
	 * @param geoObj
	 * @param projectionPlane
	 * @param startIndex
	 * @param count
	 * @return <code>true</code> if the projected polygon intersects the plane
	 */
	public static boolean doesProjectedPolyIntersect(AbstractComponent geoObj, Plane3D projectionPlane, int startIndex,
			int count) {

		Point3D pp[] = new Point3D[count];
		for (int i = 0; i < count; i++) {
			pp[i] = new Point3D();
		}

		int isectsCount = 0;
		for (int i = 0; i < count; i++) {
			int index = startIndex + i;
			Line3D l3d = geoObj.getVolumeEdge(index);
			projectionPlane.intersection(l3d, pp[i]);

			if (lengthTest(l3d.length(), l3d.origin(), l3d.end(), pp[i])) {
				isectsCount += 1;
			}
		}

		return isectsCount > 2;
	}
	
	/**
	 * Get a world 2D polygon from a clas geo object like a FTOF slab.
	 *
	 * @param geoObj          the geometric object
	 * @param projectionPlane the projection plane
	 * @param startIndex      the firstIndex of the volume edges corresponding to a
	 *                        "long" edge
	 * @param count           the number of such edges (should be contiguous!)
	 * @param wp              will hold the world 2D polygon
	 * @param centroid        optionally compute the centroid.
	 */
	public static boolean getProjectedPolygon(AbstractComponent geoObj, Plane3D projectionPlane, int startIndex,
			int count, Point2D.Double wp[], Point2D.Double centroid) {
		return getProjectedPolygon(geoObj, projectionPlane, startIndex, count, wp, centroid, true);
	}

	/**
	 * Get a world 2D polygon from a clas geo object like a FTOF slab.
	 *
	 * @param geoObj          the geometric object
	 * @param projectionPlane the projection plane
	 * @param startIndex      the firstIndex of the volume edges corresponding to a
	 *                        "long" edge
	 * @param count           the number of such edges (should be contiguous!)
	 * @param wp              will hold the world 2D polygon
	 * @param centroid        optionally compute the centroid.
	 */
	public static boolean getProjectedPolygon(AbstractComponent geoObj, Plane3D projectionPlane, int startIndex,
			int count, Point2D.Double wp[], Point2D.Double centroid, boolean checkIntersects) {

		if (count > _workPoints.length) {
            _workPoints = new Point3D[count];
            for (int i = 0; i < count; i++) {
                _workPoints[i] = new Point3D();
            }
		}

		for (int i = 0; i < count; i++) {
			_workPoints[i] = new Point3D();
		}

		for (int i = 0; i < count; i++) {
			int index = startIndex + i;
			Line3D l3d = geoObj.getVolumeEdge(index);
			projectionPlane.intersection(l3d, _workPoints[i]);
		}

		ArrayList<Line3D> lines = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			int j = (i + 1) % count;
			lines.add(new Line3D(_workPoints[i], _workPoints[j]));
		}

		for (int i = 0; i < count; i++) {
			wp[i].x = _workPoints[i].z();
			wp[i].y = Math.hypot(_workPoints[i].x(), _workPoints[i].y());
		}

		if (centroid != null) {
			average(wp, centroid);
		}
		
		if (checkIntersects) {
			return doesProjectedPolyIntersect(geoObj, projectionPlane, startIndex, count);	
		}

		return true;
	}

	// faster than centroid, and good enough
	private static void average(Point2D.Double wp[], Point2D.Double centroid) {
		double xsum = 0;
		double ysum = 0;

		int size = wp.length;
		for (int i = 0; i < size; i++) {
			xsum += wp[i].x;
			ysum += wp[i].y;
		}

		centroid.x = xsum / size;
		centroid.y = ysum / size;
	}

	/**
	 * Create an XY plane
	 *
	 * @param z the z location of the plane
	 * @return the new plane
	 */
	public static Plane3D xyPlane(double z) {
		// can use arbitrary wire
		Point3D p1 = new Point3D(0, 0, 0);
		Point3D p2 = new Point3D(0, 0, 100);
		Line3D l3d = new Line3D(p1, p2);
		Point3D origin = new Point3D(0, 0, z);
		return new Plane3D(origin, l3d.direction());
	}

	/**
	 * Get a plane of constant phi
	 *
	 * @param phi the azimuthal angle in degrees
	 * @return a plane of constant phi
	 */
	public static Plane3D constantPhiPlane(double phi) {
		Point3D point = new Point3D(0, 0, 0);
		phi = Math.toRadians(phi);
		double cphi = Math.cos(phi);
		double sphi = Math.sin(phi);
		Vector3D norm = new Vector3D(sphi, -cphi, 0);
		return new Plane3D(point, norm);
	}

	/**
	 * Get a plane of constant z
	 *
	 * @param z the constant z coordinate
	 * @return a plane of constant z
	 */
	public static Plane3D constantZPlane(double z) {
		Point3D point = new Point3D(0, 0, z);
		Vector3D norm = new Vector3D(0, 0, 1);
		return new Plane3D(point, norm);
	}


	/**
	 * allocate an array of allocated points
	 *
	 * @param n the number of points
	 * @return an array of allocated points
	 */
	public static Point2D.Double[] allocate(int n) {
		Point2D.Double[] wp = new Point2D.Double[n];
		for (int i = 0; i < n; i++) {
			wp[i] = new Point2D.Double();
		}
		return wp;
	}

	/**
	 * Create a Geometry package Path3D object from the trajectory
	 *
	 * @param traj the swum trajectory
	 * @return the Path3D object
	 */
	public static Path3D fromSwimTrajectory(SwimTrajectory traj) {
		Path3D path = new Path3D();
		if (traj != null) {
			for (double[] tp : traj) {
				// convert to cm
				double x = 100. * tp[0];
				double y = 100. * tp[1];
				double z = 100. * tp[2];
				path.addPoint(x, y, z);
			}
		}
		return path;
	}

	/**
	 * Get the rotation matrix from an axis specified by unit vector components and
	 * an angle.
	 *
	 * @param ux     the x component of the unit vector giving the axis direction
	 * @param uy     the y component of the unit vector giving the axis direction
	 * @param uz     the z component of the unit vector giving the axis direction
	 * @param theta  the angle in degrees
	 * @param rotMat space for the rotation matrix
	 */
	public void rotationMatrixFromAxisAndAngle(double ux, double uy, double uz, double theta, double[][] rotMat) {
		double ang = Math.toRadians(theta);
		double cos = Math.cos(ang);
		double sin = Math.sin(ang);
		double omc = 1. - cos;
		double uxuy = ux * uy;
		double uxuz = ux * uz;
		double uyuz = uy * uz;
		double uxsq = ux * ux;
		double uysq = uy * uy;
		double uzsq = uz * uz;
		double uxsin = ux * sin;
		double uysin = uy * sin;
		double uzsin = uz * sin;

		rotMat[0][0] = cos + uxsq * omc;
		rotMat[0][1] = uxuy * omc - uzsin;
		rotMat[0][2] = uxuz * omc + uysin;

		rotMat[1][0] = uxuy * omc + uzsin;
		rotMat[1][1] = cos + uysq * omc;
		rotMat[1][2] = uyuz * omc - uxsin;

		rotMat[2][0] = uxuz * omc - uysin;
		rotMat[2][1] = uyuz * omc + uxsin;
		rotMat[2][2] = cos + uzsq * omc;
	}

}
