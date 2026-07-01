package cnuphys.ced.geometry.urwt;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.jlab.detector.geant4.v2.MPGD.URWT.URWTStripFactory;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import cnuphys.ced.ced3d.util.PlaneHullUtility;
import cnuphys.ced.ced3d.util.Point;
import cnuphys.ced.geometry.cache.GeometryPrimitiveIO;

/**
 * Runtime geometry data for one micro-resistive well tracker detector panel.
 * <p>
 * A detector panel is identified by a 1-based sector and layer:
 *
 * <pre>
 * sector: 1..6
 * layer:  1..4
 * </pre>
 *
 * This class keeps the runtime data needed by the UrWT 2D and 3D drawing code:
 * strip lines, convex hull points, XY hull points, and a lazily computed
 * centroid.
 * <p>
 * For cache stability, this class provides explicit primitive cache read/write
 * methods. The cache stores primitive line endpoints and hull coordinates
 * rather than serializing this object or the JLab {@link Line3D} objects
 * directly.
 */
public class UrWTDetectorData {

	/** Number of endpoints in one strip line. */
	private static final int LINE_ENDPOINT_COUNT = 2;

	/** Number of coordinates per 3D point. */
	private static final int COORD_COUNT = 3;

	/** Number of coordinates per 2D point. */
	private static final int XY_COORD_COUNT = 2;

	/**
	 * For 3D drawing, z offsets applied to the convex hull points for the layers.
	 */
	private static final double[] DELTA_Z = { -2.0, -1.0, 0.0, 1.0 };

	/**
	 * Sector stored as 1-based, 1..6.
	 */
	public final int sector;

	/**
	 * Layer stored as 1-based, 1..4.
	 */
	public final int layer;

	/**
	 * Strip count.
	 */
	public final int count;

	/**
	 * Strip line geometry.
	 */
	public Line3D[] strips;

	/**
	 * Centroid of the detector, computed lazily.
	 */
	private Point centroid;

	/**
	 * Convex hull of the strip endpoints, used for drawing.
	 */
	private List<Point> convexHull;

	/**
	 * XY coordinates of the convex hull points, used for 2D drawing.
	 */
	private Point2D.Double[] xyPoints;

	/**
	 * Build detector data from the authoritative UrWT strip factory.
	 *
	 * @param factory the UrWT strip factory
	 * @param sector  the 1-based sector, 1..6
	 * @param layer   the 1-based layer, 1..4
	 */
	public UrWTDetectorData(URWTStripFactory factory, int sector, int layer) {

		checkSectorLayer(sector, layer);

		this.sector = sector;
		this.layer = layer;

		count = factory.getNComponents(sector, layer);
		strips = new Line3D[count];

		for (int strip = 1; strip <= count; strip++) {

			strips[strip - 1] = factory.getStrip(sector, layer, strip);

			if (strips[strip - 1] == null) {
				System.err.println(
						"strip factory returned null for sector " + sector + " layer " + layer + " strip " + strip);
				System.exit(1);
			}
		}

		buildHullFromStrips();
	}

	/**
	 * Build detector data from explicit primitive cache data.
	 *
	 * @param sector       the 1-based sector
	 * @param layer        the 1-based layer
	 * @param stripLines   strip line endpoint data shaped [strip][endpoint][xyz]
	 * @param hullPoints   convex hull data shaped [point][xyz]
	 * @param xyPointArray XY hull data shaped [point][xy]
	 */
	private UrWTDetectorData(int sector, int layer, double[][][] stripLines, double[][] hullPoints,
			double[][] xyPointArray) {

		checkSectorLayer(sector, layer);

		this.sector = sector;
		this.layer = layer;
		this.count = (stripLines == null) ? 0 : stripLines.length;

		strips = new Line3D[count];

		for (int strip = 0; strip < count; strip++) {
			double[][] linePoints = stripLines[strip];
			validateLinePoints(linePoints, "UrWT sector " + sector + " layer " + layer + " strip " + (strip + 1));

			Point3D origin = new Point3D(linePoints[0][0], linePoints[0][1], linePoints[0][2]);
			Point3D end = new Point3D(linePoints[1][0], linePoints[1][1], linePoints[1][2]);
			strips[strip] = new Line3D(origin, end);
		}

		convexHull = new ArrayList<>();

		if (hullPoints != null) {
			for (double[] hp : hullPoints) {
				if ((hp != null) && (hp.length >= COORD_COUNT)) {
					convexHull.add(new Point(hp[0], hp[1], hp[2]));
				}
			}
		}

		if (xyPointArray == null) {
			xyPoints = new Point2D.Double[0];
		} else {
			xyPoints = new Point2D.Double[xyPointArray.length];

			for (int i = 0; i < xyPointArray.length; i++) {
				double[] xy = xyPointArray[i];
				if ((xy != null) && (xy.length >= XY_COORD_COUNT)) {
					xyPoints[i] = new Point2D.Double(xy[0], xy[1]);
				} else {
					xyPoints[i] = new Point2D.Double(Double.NaN, Double.NaN);
				}
			}
		}
	}

	/**
	 * Compute the convex hull and XY hull data from the strip endpoints.
	 */
	private void buildHullFromStrips() {
		convexHull = PlaneHullUtility.getHullIfCoplanar(strips, 1.0e-6);

		if (convexHull == null) {
			System.err.println("UrWTDetectorData: Could not compute convex hull for sector " + sector + " layer "
					+ layer);
			convexHull = new ArrayList<>();
		}

		for (int i = 0; i < convexHull.size(); i++) {
			Point p = convexHull.get(i);
			p.z += DELTA_Z[layer - 1];
		}

		xyPoints = new Point2D.Double[convexHull.size()];
		for (int i = 0; i < convexHull.size(); i++) {
			Point p = convexHull.get(i);
			xyPoints[i] = new Point2D.Double(p.x, p.y);
		}
	}

	/**
	 * Get the convex hull of the strip endpoints.
	 *
	 * @return the convex hull points
	 */
	public List<Point> getConvexHull() {
		return convexHull;
	}

	/**
	 * Get the XY coordinates of the convex hull points.
	 *
	 * @return the XY points
	 */
	public Point2D.Double[] getXYPoints() {
		return xyPoints;
	}

	/**
	 * Get the detector centroid.
	 *
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
	 * Get a strip by its 1-based strip number.
	 *
	 * @param strip the 1-based strip number, 1..count
	 * @return the strip, or {@code null} if the strip number is out of range
	 */
	public Line3D getStrip(int strip) {
		if (strip < 1 || strip > count) {
			System.err.println("Bad strip number: " + strip);
			return null;
		}

		return strips[strip - 1];
	}

	/**
	 * Write this detector data to the cache using explicit primitive values.
	 *
	 * @param output the cache output stream
	 */
	public void writeToCache(Output output) {
		output.writeInt(sector);
		output.writeInt(layer);
		output.writeInt(count);

		for (int strip = 0; strip < count; strip++) {
			writeLine(output, strips[strip]);
		}

		writeHull(output);
		writeXYPoints(output);
	}

	/**
	 * Read detector data from the cache.
	 *
	 * @param input the cache input stream
	 * @return reconstructed detector data
	 */
	public static UrWTDetectorData readFromCache(Input input) {
		int sector = input.readInt();
		int layer = input.readInt();
		int count = input.readInt();

		checkSectorLayer(sector, layer);

		if (count < 0) {
			throw new IllegalArgumentException("UrWTDetectorData: negative strip count in cache: " + count);
		}

		double[][][] stripLines = new double[count][][];

		for (int strip = 0; strip < count; strip++) {
			stripLines[strip] = GeometryPrimitiveIO.readCorners(input, LINE_ENDPOINT_COUNT, COORD_COUNT,
					"UrWT sector " + sector + " layer " + layer + " strip " + (strip + 1));
		}

		double[][] hullPoints = readHull(input);
		double[][] xyPointArray = readXYPoints(input);

		return new UrWTDetectorData(sector, layer, stripLines, hullPoints, xyPointArray);
	}

	/**
	 * Write one strip line to the cache as two 3D endpoints.
	 *
	 * @param output the output stream
	 * @param line   the line to write
	 */
	private static void writeLine(Output output, Line3D line) {
		if (line == null) {
			throw new IllegalArgumentException("UrWTDetectorData: cannot write null strip line.");
		}

		double[][] linePoints = new double[LINE_ENDPOINT_COUNT][COORD_COUNT];

		linePoints[0][0] = line.origin().x();
		linePoints[0][1] = line.origin().y();
		linePoints[0][2] = line.origin().z();

		linePoints[1][0] = line.end().x();
		linePoints[1][1] = line.end().y();
		linePoints[1][2] = line.end().z();

		GeometryPrimitiveIO.writeCorners(output, linePoints, LINE_ENDPOINT_COUNT, COORD_COUNT);
	}

	/**
	 * Write the convex hull to the cache.
	 *
	 * @param output the output stream
	 */
	private void writeHull(Output output) {
		if (convexHull == null) {
			output.writeInt(0);
			return;
		}

		output.writeInt(convexHull.size());

		for (Point p : convexHull) {
			output.writeDouble(p.x);
			output.writeDouble(p.y);
			output.writeDouble(p.z);
		}
	}

	/**
	 * Read the convex hull from the cache.
	 *
	 * @param input the input stream
	 * @return hull points shaped [point][xyz]
	 */
	private static double[][] readHull(Input input) {
		int count = input.readInt();

		if (count < 0) {
			throw new IllegalArgumentException("UrWTDetectorData: negative hull point count in cache: " + count);
		}

		double[][] hull = new double[count][COORD_COUNT];

		for (int i = 0; i < count; i++) {
			hull[i][0] = input.readDouble();
			hull[i][1] = input.readDouble();
			hull[i][2] = input.readDouble();
		}

		return hull;
	}

	/**
	 * Write XY hull points to the cache.
	 *
	 * @param output the output stream
	 */
	private void writeXYPoints(Output output) {
		if (xyPoints == null) {
			output.writeInt(0);
			return;
		}

		output.writeInt(xyPoints.length);

		for (Point2D.Double p : xyPoints) {
			output.writeDouble(p.x);
			output.writeDouble(p.y);
		}
	}

	/**
	 * Read XY hull points from the cache.
	 *
	 * @param input the input stream
	 * @return XY points shaped [point][xy]
	 */
	private static double[][] readXYPoints(Input input) {
		int count = input.readInt();

		if (count < 0) {
			throw new IllegalArgumentException("UrWTDetectorData: negative XY point count in cache: " + count);
		}

		double[][] xy = new double[count][XY_COORD_COUNT];

		for (int i = 0; i < count; i++) {
			xy[i][0] = input.readDouble();
			xy[i][1] = input.readDouble();
		}

		return xy;
	}

	/**
	 * Validate a strip line endpoint array.
	 *
	 * @param linePoints the line points
	 * @param context    context for error messages
	 */
	private static void validateLinePoints(double[][] linePoints, String context) {
		if ((linePoints == null) || (linePoints.length != LINE_ENDPOINT_COUNT)) {
			throw new IllegalArgumentException(context + ": invalid line endpoint count.");
		}

		for (int endpoint = 0; endpoint < LINE_ENDPOINT_COUNT; endpoint++) {
			if ((linePoints[endpoint] == null) || (linePoints[endpoint].length != COORD_COUNT)) {
				throw new IllegalArgumentException(context + ": invalid coordinate count for endpoint " + endpoint);
			}
		}
	}

	/**
	 * Validate sector/layer address.
	 *
	 * @param sector the 1-based sector
	 * @param layer  the 1-based layer
	 */
	private static void checkSectorLayer(int sector, int layer) {
		if ((sector < 1) || (sector > UrWTGeometry.NUM_SECTORS)) {
			throw new IllegalArgumentException("Bad sector in UrWT data: " + sector);
		}

		if ((layer < 1) || (layer > UrWTGeometry.NUM_LAYERS)) {
			throw new IllegalArgumentException("Bad layer in UrWT data: " + layer);
		}
	}
}