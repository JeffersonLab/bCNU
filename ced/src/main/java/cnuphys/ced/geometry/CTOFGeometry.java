package cnuphys.ced.geometry;

import cnuphys.ced.geometry.cache.ACachedGeometry;
import cnuphys.ced.geometry.cache.GeometryCache;
import cnuphys.ced.geometry.cache.IGeometryCache;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import java.awt.geom.Point2D;

public class CTOFGeometry extends ACachedGeometry {

	// existing constants and members
	private static final double THETA0 = 0;
	private static final double DTHETA = -7.5; // 360/48
	public static final double RINNER = 251.1; // mm
	public static final double ROUTER = RINNER + 30.226; // mm
	public static final int COUNT = 48;
	private static Point2D.Double _quads[][];
	
	/**
     * Constructor
     */
	public CTOFGeometry() {
		super("CTOFGeometry");
	}

	public void initializeUsingCCDB() {
		System.out.println("\n=====================================");
		System.out.println("===  CTOF Geometry Initialization ===");
		System.out.println("===  WARNING: SIMPLE GEOMETRY ===");
		System.out.println("=====================================");
		initQuads();
	}


	// initializes the CTOF quads
	private static void initQuads() {
		_quads = new Point2D.Double[COUNT][];
		for (int i = 0; i < COUNT; i++) {
			double theta1 = THETA0 - i * DTHETA;
			double theta2 = theta1 - DTHETA;
			theta1 = Math.toRadians(theta1);
			theta2 = Math.toRadians(theta2);
			_quads[i] = new Point2D.Double[4];
			_quads[i][0] = new Point2D.Double(RINNER * Math.cos(theta1), RINNER * Math.sin(theta1));
			_quads[i][1] = new Point2D.Double(ROUTER * Math.cos(theta1), ROUTER * Math.sin(theta1));
			_quads[i][2] = new Point2D.Double(ROUTER * Math.cos(theta2), ROUTER * Math.sin(theta2));
			_quads[i][3] = new Point2D.Double(RINNER * Math.cos(theta2), RINNER * Math.sin(theta2));
		}
	}
	
	/**
	 * Get the quad for a paddle
	 *
	 * @param paddle the 1-based paddle
	 * @return the quad for a paddle
	 */
	public static Point2D.Double[] getQuad(int paddle) {
		return _quads[paddle - 1];
	}

	/**
	 * Get the quad for a paddle
	 * @param paddleId the 1-based paddle
	 * @param coords the 3D coordinates of the paddle corners (cm)
	 */
	public static void paddleVertices(int paddleId, float[] coords) {
		Point2D.Double quad[] = getQuad(paddleId);
		// convert mm to cm
		float x1 = (float) quad[0].x / 10;
		float x2 = (float) quad[3].x / 10;
		float x3 = (float) quad[2].x / 10;
		float x4 = (float) quad[1].x / 10;
		float y1 = (float) quad[0].y / 10;
		float y2 = (float) quad[3].y / 10;
		float y3 = (float) quad[2].y / 10;
		float y4 = (float) quad[1].y / 10;

		// approx
		float len = (float) (35.4 * 2.54); // cm
		float z1 = -len / 2;
		float z2 = len / 2;

		setCoords(1, x1, y1, z1, coords);
		setCoords(2, x2, y2, z1, coords);
		setCoords(3, x3, y3, z1, coords);
		setCoords(4, x4, y4, z1, coords);
		setCoords(5, x1, y1, z2, coords);
		setCoords(6, x2, y2, z2, coords);
		setCoords(7, x3, y3, z2, coords);
		setCoords(8, x4, y4, z2, coords);
	}

	/**
	 * Set the coordinates for a corner
	 *
	 * @param corner the corner number (1-8)
	 * @param x      x coordinate
	 * @param y      y coordinate
	 * @param z      z coordinate
	 * @param coords the array to set the coordinates in
	 */
	private static void setCoords(int corner, float x, float y, float z, float[] coords) {
		int i = (corner - 1) * 3;
		coords[i] = x;
		coords[i + 1] = y;
		coords[i + 2] = z;
	}

	@Override
	public boolean readGeometry(Kryo kryo, Input input) {
		try {
			// Read the outer array length
			int outerLength = input.readInt();
			_quads = new Point2D.Double[outerLength][];
			// For each row, read its length and the points within it
			for (int i = 0; i < outerLength; i++) {
				int innerLength = input.readInt();
				_quads[i] = new Point2D.Double[innerLength];
				for (int j = 0; j < innerLength; j++) {
					double x = input.readDouble();
					double y = input.readDouble();
					_quads[i][j] = new Point2D.Double(x, y);
				}
			}
			return true;
		} catch (Exception e) {
			System.err.println("CTOFGeometry: Error reading _quads from cache: " + e.getMessage());
			return false;
		}
	}

	@Override
	public boolean writeGeometry(Kryo kryo, Output output) {
		if (_quads == null) {
			System.err.println("CTOFGeometry: _quads is null. Nothing to write to cache.");
			return false;
		}
		// Write the number of rows in _quads
		output.writeInt(_quads.length);
		// Write each quad (row) to the output
		for (Point2D.Double[] quad : _quads) {
			// Write the length of the row (should be 4)
			output.writeInt(quad.length);
			for (Point2D.Double point : quad) {
				output.writeDouble(point.x);
				output.writeDouble(point.y);
			}
		}
		return true;
	}
}
