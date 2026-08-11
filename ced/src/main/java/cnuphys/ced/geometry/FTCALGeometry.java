package cnuphys.ced.geometry;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

import org.jlab.detector.base.GeometryFactory;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.component.ScintillatorPaddle;
import org.jlab.geom.detector.ft.FTCALDetector;
import org.jlab.geom.detector.ft.FTCALFactory;
import org.jlab.geom.detector.ft.FTCALLayer;
import org.jlab.geom.detector.ft.FTCALSector;
import org.jlab.geom.detector.ft.FTCALSuperlayer;
import org.jlab.geom.prim.Point3D;


import cnuphys.ced.geometry.cache.ACachedGeometry;

public class FTCALGeometry extends ACachedGeometry {

	// values of the gtid limits
	private static final double gvals[] = { -16.7, -15.2, -13.7, -12.2, -10.7, -9.15, -7.6, -6.1, -4.6, -3.05, -1.5, 0.,
			1.5, 3.05, 4.6, 6.1, 7.6, 9.15, 10.7, 12.2, 13.7, 15.2, 16.7 };

	// max id, not all Id's are valid
	public static final int MAXID = 475;

	// z offset is a shift in z (cm) to place the 3d view at the origin
	public static final float FTCAL_Z0 = 200f;

	// used for 2D grid spacing
	public static final double FT_DEL = 2.5;
	private static final int GOOD_ID_COUNT = 332;
	private static final int CORNER_COUNT = 8;
	private static final int COORD_COUNT = 3;

	// there are 332 paddles but the IDs are not 1..332 they are 1..475
	private static ScintillatorPaddle paddles[];
	private static double paddleCorners[][][];
	private static short goodIds[];

	// gives the xy grid indices of each paddle
	private static Point paddleXYIndices[];
	private static Hashtable<Point, Integer> indicesToId = new Hashtable<>();

	/**
	 * Constructor
	 */
	public FTCALGeometry() {
		super("FTCALGeometry");
	}

	/**
	 * Initialize the FTCAL Geometry
	 */
	@Override
	public void initializeUsingCCDB() {

		ConstantProvider ftCalDataProvider = GeometryFactory.getConstants(org.jlab.detector.base.DetectorType.FTCAL);

		FTCALFactory ftCalFactory = new FTCALFactory();
		FTCALDetector ftCalDetector = ftCalFactory.createDetectorCLAS(ftCalDataProvider);

		// one sector and one superlayer
		FTCALSector ftCalSector = ftCalDetector.getSector(0);
		FTCALSuperlayer ftCalSuperlayer = ftCalSector.getSuperlayer(0);
		FTCALLayer ftCalLayer = ftCalSuperlayer.getLayer(0);

		// get the components. Some entries will be null
		paddles = new ScintillatorPaddle[MAXID + 1];
		paddleCorners = new double[MAXID + 1][][];
		paddleXYIndices = new Point[MAXID + 1];
		indicesToId.clear();
		for (int i = 0; i < paddles.length; i++) {
			paddles[i] = null;
			paddleXYIndices[i] = null;
		}

		// there are 332 good ids, not sequential, first is 8, last is 475
		goodIds = new short[GOOD_ID_COUNT];

		int count = 0;
		List<ScintillatorPaddle> padlist = ftCalLayer.getAllComponents();

		Point2D.Double wp = new Point2D.Double();

		for (ScintillatorPaddle sp : padlist) {

			// rotate do to match actual geometry
			sp.rotateZ(Math.PI);

			int id = sp.getComponentId();
			paddles[id] = sp;
			goodIds[count] = (short) id;
			paddleCorners[id] = new double[CORNER_COUNT][COORD_COUNT];
			for (int corner = 0; corner < CORNER_COUNT; corner++) {
				Point3D point = sp.getVolumePoint(corner);
				paddleCorners[id][corner][0] = point.x();
				paddleCorners[id][corner][1] = point.y();
				paddleCorners[id][corner][2] = point.z();
			}

			Point p = new Point();
			paddleXYCenter(id, wp);

			p.x = valToIndex(wp.x);
			p.y = valToIndex(wp.y);

			paddleXYIndices[id] = p;

			indicesToId.put(p, id);

			count++;
		}

	}

	/**
	 * Convert the x y indices into a component id
	 *
	 * @param p the indices
	 * @return the component id
	 */
	public static int xyIndicesToId(Point p) {
		return indicesToId.get(p);
	}

	/**
	 * Convert the x y indices into a component id
	 *
	 * @param x the x index
	 * @param y the y index
	 * @return the component id
	 */
	public static int xyIndicesToId(int x, int y) {
		Point p = new Point(x, y);
		return xyIndicesToId(p);
	}

	/**
	 * Get a scintillator paddle
	 *
	 * @param componentId the componentId
	 * @return the paddle, might be null
	 */
	public static ScintillatorPaddle getPaddle(int componentId) {
		return (paddles == null || componentId < 0 || componentId >= paddles.length) ? null : paddles[componentId];
	}

	/**
	 * Used by the 3D drawing
	 *
	 * @param paddleId the 1-based paddle 1..332
	 * @param coords   holds 8*3 = 24 values [x1, y1, z1, ..., x8, y8, z8]
	 */
	public static void paddleVertices(int paddleId, float[] coords) {
		double[][] corners = getPaddleCorners(paddleId);
		if (corners == null || coords == null || coords.length < CORNER_COUNT * COORD_COUNT) {
			return;
		}
		for (int i = 0; i < CORNER_COUNT; i++) {
			int j = 3 * i;
			coords[j] = (float) corners[i][0];
			coords[j + 1] = (float) corners[i][1];

			// note the offset!!!!
			coords[j + 2] = (float) corners[i][2] - FTCAL_Z0;
		}
	}

	/**
	 * Obtain the paddle xy corners for 2D view
	 *
	 * @param paddleId the paddle ID 1..48
	 * @param wp       the four XY corners (cm)
	 */
	public static void paddleXYCorners(int paddleId, Point2D.Double[] wp) {
		double[][] corners = getPaddleCorners(paddleId);
		if (corners == null || wp == null || wp.length < 4) {
			return;
		}

		for (int i = 0; i < 4; i++) {
			wp[i].x = corners[i][0];
			wp[i].y = corners[i][1];
		}
	}

	/**
	 * Get the XY (for 2D) center of the paddle
	 *
	 * @param paddleId the padle id
	 * @param center   the center, or NaNs
	 */
	public static void paddleXYCenter(int paddleId, Point2D.Double center) {
		double[][] corners = getPaddleCorners(paddleId);
		if (corners == null) {
			center.setLocation(Double.NaN, Double.NaN);
			return;
		}
		double x = 0.0;
		double y = 0.0;
		for (double[] corner : corners) {
			x += corner[0];
			y += corner[1];
		}
		center.setLocation(x / CORNER_COUNT, y / CORNER_COUNT);
	}

	/**
	 * Get the indices from the id
	 *
	 * @param id the id
	 * @return the XY grid indices or null
	 */
	public static Point getXYIndices(int id) {
		if ((id < 1) || (id > MAXID)) {
			return null;
		}

		return paddleXYIndices[id];
	}

	/**
	 * Get the indices from the padle
	 *
	 * @param paddle the paddle
	 * @return the XY grid indices or null
	 */
	public static Point getXYIndices(ScintillatorPaddle paddle) {
		if (paddle == null) {
			return null;
		}
		return getXYIndices(paddle.getComponentId());
	}

	/**
	 * Returns the grid index [-11, -10, ... -1, 1, .., 11] for the given value
	 *
	 * @param val an x or y value
	 * @return the grid index, or 0 on error. Zero is not a possible value.
	 */

	public static int valToIndex(double val) {
		int len = gvals.length;
		int lm1 = len - 1;

		if ((val < gvals[0]) || (val > gvals[lm1])) {
			return 0;
		}

		for (int i = 1; i <= lm1; i++) {
			if (val < gvals[i]) {

				int index = -12 + i;

				if (index < 0) {
					return index;
				} else {
					return index + 1;
				}
			}
		}
		return 0;
	}

	/**
	 * Get the maximum absolute extent in x or y. Used for grid drawing
	 *
	 * @return the maximum absolute extent in x or y
	 */
	public static final double getMaxAbsXYExtent() {
		return Math.abs(gvals[0]);
	}

	/**
	 * Take an index [-11, -10, ... -1, 1, .., 11] and get the coordinate limits
	 *
	 * @param index the index
	 * @param range the limits
	 */
	public static void indexToRange(int index, double range[]) {
		if ((index < -11) || (index > 11) || (index == 0)) {
			range[0] = Double.NaN;
			range[1] = Double.NaN;
		} else {
			// take into account 0 isn't valid
			if (index > 1) {
				index--;
			}

			int leftIndex = index + 11;

			range[0] = gvals[leftIndex];
			range[1] = gvals[leftIndex + 1];
		}
	}

	/**
	 * Obtain the paddle 3D corners. Order: <br>
	 * 0: xmin, ymin, zmax <br>
	 * 1: xmax, ymin, zmax <br>
	 * 2: xmax, ymax, zmax <br>
	 * 3: xmin, ymax, zmax <br>
	 * 4: xmin, ymin, zmin <br>
	 * 5: xmax, ymin, zmin <br>
	 * 6: xmax, ymax, zmin <br>
	 * 7: xmin, ymax, zmin <br>
	 *
	 * @param paddleId the paddle ID 1..48
	 * @param corners  the eight XYZ corners (cm)
	 */
	public static void paddle3DCorners(int paddleId, Point3D corners[]) {
		double[][] cachedCorners = getPaddleCorners(paddleId);
		if (cachedCorners == null || corners == null || corners.length < CORNER_COUNT) {
			return;
		}

		for (int i = 0; i < CORNER_COUNT; i++) {
			corners[i] = new Point3D(cachedCorners[i][0], cachedCorners[i][1], cachedCorners[i][2] - FTCAL_Z0);
		}
	}

	/**
	 * Check whether the id is one of the good id
	 *
	 * @param id the 1-based id to check
	 * @return true if it is a good id
	 */
	public static boolean isGoodId(int id) {

		if ((id < 1) || (id > MAXID)) {
			return false;
		}
		return getPaddleCorners(id) != null;
	}

	/**
	 * Get all the good ids
	 *
	 * @return all the good ids
	 */
	public static short[] getGoodIds() {
		return goodIds;
	}

	public static short getGoodId(int index) {
		return goodIds[index];
	}

	private static double[][] getPaddleCorners(int id) {
		return (paddleCorners == null || id < 1 || id > MAXID) ? null : paddleCorners[id];
	}

	@Override
	public boolean supportsCache() {
		return true;
	}

	@Override
	public void readGeometry(DataInput input) throws IOException {
		int arrayLength = input.readInt();
		if (arrayLength != MAXID + 1) {
			throw new IOException("Invalid FTCal paddle array length: " + arrayLength);
		}
		double[][][] corners = new double[arrayLength][][];
		for (int id = 0; id < arrayLength; id++) {
			if (input.readBoolean()) {
				corners[id] = new double[CORNER_COUNT][COORD_COUNT];
				for (int corner = 0; corner < CORNER_COUNT; corner++) {
					for (int coordinate = 0; coordinate < COORD_COUNT; coordinate++) {
						corners[id][corner][coordinate] = input.readDouble();
					}
				}
			}
		}

		int idCount = input.readInt();
		if (idCount != GOOD_ID_COUNT) {
			throw new IOException("Invalid FTCal good-id count: " + idCount);
		}
		short[] ids = new short[idCount];
		Point[] xyIndices = new Point[MAXID + 1];
		Hashtable<Point, Integer> reverseIndices = new Hashtable<>();
		for (int index = 0; index < idCount; index++) {
			short id = input.readShort();
			int x = input.readInt();
			int y = input.readInt();
			if (id < 1 || id > MAXID || corners[id] == null) {
				throw new IOException("Invalid FTCal component id: " + id);
			}
			ids[index] = id;
			Point point = new Point(x, y);
			xyIndices[id] = point;
			reverseIndices.put(point, (int) id);
		}

		paddles = null;
		paddleCorners = corners;
		goodIds = ids;
		paddleXYIndices = xyIndices;
		indicesToId = reverseIndices;
	}

	@Override
	public void writeGeometry(DataOutput output) throws IOException {
		if (paddleCorners == null || goodIds == null || goodIds.length != GOOD_ID_COUNT) {
			throw new IOException("FTCal geometry is not initialized");
		}
		output.writeInt(MAXID + 1);
		for (int id = 0; id <= MAXID; id++) {
			double[][] corners = paddleCorners[id];
			output.writeBoolean(corners != null);
			if (corners != null) {
				for (double[] corner : corners) {
					for (double coordinate : corner) {
						output.writeDouble(coordinate);
					}
				}
			}
		}
		output.writeInt(goodIds.length);
		for (short id : goodIds) {
			Point point = paddleXYIndices[id];
			if (point == null) {
				throw new IOException("Missing FTCal grid index for component " + id);
			}
			output.writeShort(id);
			output.writeInt(point.x);
			output.writeInt(point.y);
		}
	}

}
