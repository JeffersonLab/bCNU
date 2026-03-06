package cnuphys.ced.ced3d.util;

import java.awt.Color;
import java.util.List;

import org.jlab.geom.prim.Line3D;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2ES3;

import bCNU3D.Support3D;

public class DrawSupport {
	public static void drawPlaneAndHull(GL2 gl, Plane plane, List<Point> hullPoints, float scale, Color color, int volumeAlpha) {

	    gl.glEnable(GL2.GL_BLEND);
	    gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);



	    // fill the hull, not the plane quad
	    if (hullPoints != null && hullPoints.size() >= 3) {
	        Color fillColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), volumeAlpha);
	        Support3D.setColor(gl, fillColor);

	        gl.glBegin(GL2.GL_POLYGON);
	        for (Point p : hullPoints) {
	            gl.glVertex3d(p.x, p.y, p.z);
	        }
	        gl.glEnd();

	        Support3D.setColor(gl, color);
	        gl.glLineWidth(2.5f);
	        gl.glBegin(GL2.GL_LINE_LOOP);
	        for (Point p : hullPoints) {
	            gl.glVertex3d(p.x, p.y, p.z);
	        }
	        gl.glEnd();
	    }

	    gl.glDisable(GL2.GL_BLEND);
	}
	
	/**
	 * Determines if a set of Line3D objects are coplanar.
	 * @param lines An array of Line3D objects.
	 * @param tolerance The distance tolerance for planarity.
	 * @return A Plane object if coplanar, otherwise null.
	 */
	public static Plane findCommonPlane(Line3D[] lines, double tolerance) {
	    if (lines == null || lines.length == 0) return null;

	    // 1. Get the first two points from the first line
	    // Using the requested line.origin().x() style access
	    Point p0 = new Point(lines[0].origin().x(), lines[0].origin().y(), lines[0].origin().z());
	    Point p1 = new Point(lines[0].end().x(), lines[0].end().y(), lines[0].end().z());
	    
	    Point p2 = null;
	    
	    // 2. Find a third point in the array that is not collinear with p0 and p1
	    for (int i = 1; i < lines.length; i++) {
	        // Check both origin and end of subsequent lines to find a non-collinear point
	        Point[] candidates = {
	            new Point(lines[i].origin().x(), lines[i].origin().y(), lines[i].origin().z()),
	            new Point(lines[i].end().x(), lines[i].end().y(), lines[i].end().z())
	        };

	        for (Point pCheck : candidates) {
	            // Vector v1 = p1 - p0
	            double dx1 = p1.x - p0.x; double dy1 = p1.y - p0.y; double dz1 = p1.z - p0.z;
	            // Vector v2 = pCheck - p0
	            double dx2 = pCheck.x - p0.x; double dy2 = pCheck.y - p0.y; double dz2 = pCheck.z - p0.z;
	            
	            // Cross product magnitude checks for collinearity
	            double cpX = dy1 * dz2 - dz1 * dy2;
	            double cpY = dz1 * dx2 - dx1 * dz2;
	            double cpZ = dx1 * dy2 - dy1 * dx2;
	            
	            double crossMag = Math.sqrt(cpX * cpX + cpY * cpY + cpZ * cpZ);
	            
	            // If the cross product is significant, these three points define a unique plane
	            if (crossMag > tolerance) {
	                p2 = pCheck;
	                break;
	            }
	        }
	        if (p2 != null) break;
	    }

	    // If no third point is found, all points are collinear (a line, not a unique plane)
	    if (p2 == null) return null;

	    // 3. Create the Plane
	    // We use the cross product of (p1-p0) and (p2-p0) as the normal vector
	    double dx1 = p1.x - p0.x; double dy1 = p1.y - p0.y; double dz1 = p1.z - p0.z;
	    double dx2 = p2.x - p0.x; double dy2 = p2.y - p0.y; double dz2 = p2.z - p0.z;
	    
	    double nx = dy1 * dz2 - dz1 * dy2;
	    double ny = dz1 * dx2 - dx1 * dz2;
	    double nz = dx1 * dy2 - dy1 * dx2;
	    
	    // Use your Plane constructor: Plane(double nx, double ny, double nz, double px, double py, double pz)
	    Plane plane = new Plane(nx, ny, nz, p0.x, p0.y, p0.z);

	    // 4. Verification: Check if every endpoint of every line lies on this plane
	    for (Line3D line : lines) {
	        if (plane.distance(line.origin().x(), line.origin().y(), line.origin().z()) > tolerance ||
	            plane.distance(line.end().x(), line.end().y(), line.end().z()) > tolerance) {
	            return null;
	        }
	    }

	    return plane;
	}
	
	/**
     * Given a list of Line3D arrays, returns a single 3D bounding box 
     * encompassing all points.
     * * @param allLines A list where each element is an array of Line3D objects.
     * @return A BoundingBox3D object containing the min/max extents.
     */
    public static BoundingBox3D getBoundingBox(List<Line3D[]> allLines) {
        BoundingBox3D box = new BoundingBox3D();

        if (allLines == null || allLines.isEmpty()) {
            return box;
        }

        for (Line3D[] lineArray : allLines) {
            if (lineArray == null) continue;
            
            for (Line3D line : lineArray) {
                if (line == null) continue;

                // Check the origin of the line
                updateBox(box, line.origin().x(), line.origin().y(), line.origin().z());

                // Check the end of the line
                updateBox(box, line.end().x(), line.end().y(), line.end().z());
            }
        }

        return box;
    }

    // Helper method to update the bounding box with a new point
    private static void updateBox(BoundingBox3D box, double x, double y, double z) {
        if (x < box.minX) box.minX = x;
        if (x > box.maxX) box.maxX = x;
        
        if (y < box.minY) box.minY = y;
        if (y > box.maxY) box.maxY = y;
        
        if (z < box.minZ) box.minZ = z;
        if (z > box.maxZ) box.maxZ = z;
    }
}
