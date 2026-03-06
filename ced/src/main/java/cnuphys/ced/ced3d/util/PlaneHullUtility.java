package cnuphys.ced.ced3d.util;

import java.util.*;
import java.util.stream.Collectors;

import org.jlab.geom.prim.Line3D;

public class PlaneHullUtility {

	/**
	 * Given an array of Line3D, determine if they are coplanar within a given tolerance. If so, compute the convex hull of their endpoints projected onto that plane and return the hull points in 3D.
	 * @param lines the array of Line3D to check and compute the hull from
	 * @param tolerance the distance tolerance for coplanarity and point uniqueness
	 * @return a list of Points representing the convex hull in 3D if coplanar, or null if not coplanar or input is invalid
	 */
    public static List<Point> getHullIfCoplanar(Line3D[] lines, double tolerance) {
        if (lines == null || lines.length == 0) return null;

        Plane commonPlane = findCommonPlane(lines, tolerance);
        if (commonPlane == null) return null;

        List<Point> uniquePoints = getUniqueEndpoints(lines, tolerance);
        return computeHullOnPlane(uniquePoints, commonPlane);
    }

    private static Plane findCommonPlane(Line3D[] lines, double tolerance) {
        // Access coordinates via origin() and end()
        Point p0 = new Point(lines[0].origin().x(), lines[0].origin().y(), lines[0].origin().z());
        Point p1 = new Point(lines[0].end().x(), lines[0].end().y(), lines[0].end().z());
        
        Point p2 = null;
        for (int i = 1; i < lines.length; i++) {
            Point pCheck = new Point(lines[i].origin().x(), lines[i].origin().y(), lines[i].origin().z());
            
            double dx1 = p1.x - p0.x; double dy1 = p1.y - p0.y; double dz1 = p1.z - p0.z;
            double dx2 = pCheck.x - p0.x; double dy2 = pCheck.y - p0.y; double dz2 = pCheck.z - p0.z;
            
            double crossX = dy1 * dz2 - dz1 * dy2;
            double crossY = dz1 * dx2 - dx1 * dz2;
            double crossZ = dx1 * dy2 - dy1 * dx2;
            
            if (Math.sqrt(crossX * crossX + crossY * crossY + crossZ * crossZ) > tolerance) {
                p2 = pCheck;
                break;
            }
        }

        if (p2 == null) return null;

        double dx1 = p1.x - p0.x; double dy1 = p1.y - p0.y; double dz1 = p1.z - p0.z;
        double dx2 = p2.x - p0.x; double dy2 = p2.y - p0.y; double dz2 = p2.z - p0.z;
        
        double nx = dy1 * dz2 - dz1 * dy2;
        double ny = dz1 * dx2 - dx1 * dz2;
        double nz = dx1 * dy2 - dy1 * dx2;
        
        Plane plane = new Plane(nx, ny, nz, p0.x, p0.y, p0.z);

        for (Line3D line : lines) {
            if (plane.distance(line.origin().x(), line.origin().y(), line.origin().z()) > tolerance ||
                plane.distance(line.end().x(), line.end().y(), line.end().z()) > tolerance) {
                return null;
            }
        }
        return plane;
    }

    private static List<Point> getUniqueEndpoints(Line3D[] lines, double tol) {
        List<Point> all = new ArrayList<>();
        for (Line3D l : lines) {
            all.add(new Point(l.origin().x(), l.origin().y(), l.origin().z()));
            all.add(new Point(l.end().x(), l.end().y(), l.end().z()));
        }
        List<Point> unique = new ArrayList<>();
        for (Point p : all) {
            if (unique.stream().noneMatch(u -> u.distance(p) < tol)) {
                unique.add(p);
            }
        }
        return unique;
    }

    private static List<Point> computeHullOnPlane(List<Point> pts3d, Plane plane) {
        double mag = Math.sqrt(plane.a * plane.a + plane.b * plane.b + plane.c * plane.c);
        Point n = new Point(plane.a / mag, plane.b / mag, plane.c / mag);

        // Define temp vectors to ensure finality for the lambda
        Point tempU = (Math.abs(n.x) > 0.1) ? new Point(n.y, -n.x, 0) : new Point(0, n.z, -n.y);
        final Point uVec = tempU.scale(1.0 / tempU.distance(0, 0, 0));

        final Point vVec = new Point(
            n.y * uVec.z - n.z * uVec.y, 
            n.z * uVec.x - n.x * uVec.z, 
            n.x * uVec.y - n.y * uVec.x
        );

        Point origin = pts3d.get(0);
        List<Point2D> pts2d = pts3d.stream().map(p -> {
            Point diff = p.subtract(origin);
            // uVec and vVec are now final, solving the Java 17 error
            return new Point2D(diff.dot(uVec), diff.dot(vVec), p);
        }).sorted().collect(Collectors.toList());

        List<Point2D> hull = new ArrayList<>();
        for (Point2D p : pts2d) {
            while (hull.size() >= 2 && crossProduct2D(hull.get(hull.size() - 2), hull.get(hull.size() - 1), p) <= 0) 
                hull.remove(hull.size() - 1);
            hull.add(p);
        }
        int lowerSize = hull.size();
        for (int i = pts2d.size() - 2; i >= 0; i--) {
            Point2D p = pts2d.get(i);
            while (hull.size() > lowerSize && crossProduct2D(hull.get(hull.size() - 2), hull.get(hull.size() - 1), p) <= 0) 
                hull.remove(hull.size() - 1);
            hull.add(p);
        }
        if (hull.size() > 1) hull.remove(hull.size() - 1);

        return hull.stream().map(h -> h.original).collect(Collectors.toList());
    }

    private static double crossProduct2D(Point2D a, Point2D b, Point2D c) {
        return (b.u - a.u) * (c.v - a.v) - (b.v - a.v) * (c.u - a.u);
    }

    private static class Point2D implements Comparable<Point2D> {
        double u, v;
        Point original;
        Point2D(double u, double v, Point p) { this.u = u; this.v = v; this.original = p; }
        @Override
        public int compareTo(Point2D o) {
            return (Math.abs(u - o.u) > 1e-9) ? Double.compare(u, o.u) : Double.compare(v, o.v);
        }
    }
}