package cnuphys.ced.geometry.urwt;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Objects;

import org.jlab.detector.geant4.v2.MPGD.URWT.URWTStripFactory;
import org.jlab.geom.prim.Line3D;

import cnuphys.ced.ced3d.util.PlaneHullUtility;
import cnuphys.ced.ced3d.util.Point;

public class UrWTDetectorData {

	// sector is stored as 1-based [1..6]
	public final int sector;

	// layer stored as [1-base] [1..4]
	public final int layer;

	// the strip count
	public final int count;

	// the strips
	public final Line3D[] strips;
	
	// the centroid of the detector, used for some calculations
	private Point centroid;
	
	// the convex hull of the strip endpoints, used for some drawing
	private List<Point> convexHull;

	// for 2D drawing, the coordinates of the convex hull points, stored as a 
	// flat array for efficient drawing in 2D with Java2D.
	private Point2D.Double[] xyPoints;
	
	private static final double[] DELTA_Z = {-2.0, -1.0, 0.0, 1.0};

	/**
	 * Some useful chamber data
	 *
	 * @param sector  [1..6]
	 * @param layer   [1..4]
	 */
	public UrWTDetectorData(URWTStripFactory factory, int sector, int layer) {

		if ((sector < 1) || (sector > 6)) {
			throw new IllegalArgumentException("URWT sector must be in [1, 6]: " + sector);
		} else if ((layer < 1) || (layer > 4)) {
			throw new IllegalArgumentException("URWT layer must be in [1, 4]: " + layer);
		}
		Objects.requireNonNull(factory, "URWT strip factory");

		// these are 1-based, just like in the database
		this.sector = sector;
		this.layer = layer;

		// the strip count
		count = factory.getNComponents(sector, layer);
		if (count < 1) {
			throw new IllegalStateException("URWT strip factory returned no strips for sector " + sector
					+ " layer " + layer);
		}

		// the strips
		strips = new Line3D[count];

		for (int strip = 1; strip <= count; strip++) {

			strips[strip - 1] = factory.getStrip(sector, layer, strip);

			if (strips[strip - 1] == null) {
				throw new IllegalStateException("URWT strip factory returned null for sector " + sector
						+ " layer " + layer + " strip " + strip);
			}

		}
		
		// compute the convex hull of the strip endpoints, which is used 
		//for 3D drawing. 
		convexHull = PlaneHullUtility.getHullIfCoplanar(strips, 1.0e-6);
		if (convexHull == null) {
			throw new IllegalStateException(
					"Could not compute URWT convex hull for sector " + sector + " layer " + layer);
		}
		
		// offset the z coords of the strips for 3D drawing, so that the layers
		// don't z fight
		
		for (int i = 0; i < convexHull.size(); i++) {
			Point p = convexHull.get(i);
			p.z += DELTA_Z[layer - 1];
		}		
		
		//get the xy points for 2D drawing
		xyPoints = new Point2D.Double[convexHull.size()];
		for (int i = 0; i < convexHull.size(); i++) {
			Point p = convexHull.get(i);
			xyPoints[i] = new Point2D.Double(p.x, p.y);
		}

	}
	
	/**
	 * Get the convex hull of the strip endpoints, which is used for some drawing
	 * @return the convex hull points
	 */
	public List<Point> getConvexHull() {
		return convexHull;
	}
	
	/**
	 * Get the xy coordinates of the convex hull points, which is used for some 2D drawing
	 * @return the xy points
	 */
	public Point2D.Double[] getXYPoints() {
		return xyPoints;
	}
	
	/**
	 * Get the centroid of the detector, which is used for some calculations
	 * @return the centroid
	 */
	public Point getCentroid() {
		if (centroid == null) {
			double x = 0;
			double y = 0;
			double z = 0;
			for (Line3D strip : strips) {
				x += strip.midpoint().x();
				y += strip.midpoint().y();
				z += strip.midpoint().z();
			}
			int n = strips.length;
			centroid = new Point(x / n, y / n, z / n);
		}
		return centroid;
	}
	
	/**
	 * Get a strip by its 1-based strip number
	 * @param strip the 1-based strip number [1..count]
	 * @return the strip, or null if the strip number is out of range
	 */
	public Line3D getStrip(int strip) {
		if (strip < 1 || strip > count) {
			System.err.println("Bad strip number: " + strip);
			return null;
		}
		return strips[strip - 1];
	}	
}
