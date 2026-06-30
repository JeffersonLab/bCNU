package cnuphys.ced.geometry;

import java.awt.Point;
import java.awt.geom.Point2D;
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

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import cnuphys.ced.geometry.cache.ACachedGeometry;

/**
 * Geometry support for the Forward Tagger Calorimeter (FTCAL).
 * <p>
 * FTCAL component ids are not contiguous. There are 332 valid components, but
 * the maximum component id is {@link #MAXID}. This class therefore keeps
 * component-id-indexed arrays whose length is {@code MAXID + 1}.
 * <p>
 * The original cache stored the JLab {@link ScintillatorPaddle} object graph.
 * The refactored cache stores explicit primitive corner coordinates instead:
 *
 * <pre>
 * paddleCorners[componentId][corner][xyz]
 * </pre>
 *
 * This makes the cached FTCAL geometry independent of the internal serialized
 * form of the JLab geometry classes.
 */
public class FTCALGeometry extends ACachedGeometry {

	// values of the grid limits
	private static final double gvals[] = { -16.7, -15.2, -13.7, -12.2, -10.7, -9.15, -7.6, -6.1, -4.6,
			-3.05, -1.5, 0., 1.5, 3.05, 4.6, 6.1, 7.6, 9.15, 10.7, 12.2, 13.7, 15.2, 16.7 };

	/**
	 * Maximum FTCAL component id. Not all ids in {@code 1..MAXID} are valid.
	 */
	public static final int MAXID = 475;

	/**
	 * Z offset in cm used to place the FTCAL 3D view at the origin.
	 */
	public static final float FTCAL_Z0 = 200f;

	/**
	 * Grid spacing used for 2D FTCAL drawing.
	 */
	public static final double FT_DEL = 2.5;

	/**
	 * Number of valid FTCAL component ids.
	 */
	private static final int GOOD_ID_COUNT = 332;

	/**
	 * Number of 3D volume corners for one paddle.
	 */
	private static final int CORNER_COUNT = 8;

	/**
	 * Number of coordinates per 3D corner.
	 */
	private static final int COORD_COUNT = 3;

	/**
	 * Number of XY corners used for 2D drawing.
	 */
	private static final int XY_CORNER_COUNT = 4;

	/**
	 * Runtime JLab geometry object graph.
	 * <p>
	 * This is only available after direct CCDB initialization. After cache
	 * initialization, FTCAL uses {@link #paddleCorners} and this array is set to
	 * {@code null}.
	 */
	private static ScintillatorPaddle paddles[];

	/**
	 * Explicit primitive geometry cache.
	 * <p>
	 * Index order:
	 *
	 * <pre>
	 * paddleCorners[componentId][corner][xyz]
	 * </pre>
	 *
	 * Invalid ids have {@code null} corner arrays.
	 */
	private static double paddleCorners[][][];

	/**
	 * Valid FTCAL component ids. There are {@link #GOOD_ID_COUNT} entries.
	 */
	private static short goodIds[];

	/**
	 * XY grid indices for each component id. Invalid ids have {@code null} entries.
	 */
	private static Point paddleXYIndices[];

	/**
	 * Reverse lookup from XY grid index to component id.
	 */
	private static Hashtable<Point, Integer> indicesToId = new Hashtable<>();

	/**
	 * Constructor.
	 */
	public FTCALGeometry() {
		super("FTCALGeometry");
	}

	/**
	 * Initialize FTCAL geometry from the JLab geometry factory.
	 * <p>
	 * This path obtains the authoritative geometry from CCDB/JLab geometry
	 * services, rotates each paddle to match the expected CED orientation, builds
	 * the valid id list, builds XY grid lookup tables, and copies the JLab paddle
	 * coordinates into the explicit primitive corner cache.
	 */
	@Override
	public void initializeUsingCCDB() {

		System.out.println("\n=====================================");
		System.out.println("=== FTCAL Geometry Initialization ===");
		System.out.println("=====================================");

		ConstantProvider ftCalDataProvider = GeometryFactory.getConstants(org.jlab.detector.base.DetectorType.FTCAL);

		FTCALFactory ftCalFactory = new FTCALFactory();
		FTCALDetector ftCalDetector = ftCalFactory.createDetectorCLAS(ftCalDataProvider);

		// one sector and one superlayer
		FTCALSector ftCalSector = ftCalDetector.getSector(0);
		FTCALSuperlayer ftCalSuperlayer = ftCalSector.getSuperlayer(0);
		FTCALLayer ftCalLayer = ftCalSuperlayer.getLayer(0);

		// get the components. Some entries will be null.
		paddles = new ScintillatorPaddle[MAXID + 1];
		paddleCorners = new double[MAXID + 1][][];
		paddleXYIndices = new Point[MAXID + 1];
		indicesToId.clear();

		// there are 332 good ids, not sequential; first is 8, last is 475
		goodIds = new short[GOOD_ID_COUNT];

		int count = 0;
		List<ScintillatorPaddle> padlist = ftCalLayer.getAllComponents();

		Point2D.Double wp = new Point2D.Double();

		for (ScintillatorPaddle sp : padlist) {

			// rotate to match actual geometry
			sp.rotateZ(Math.PI);

			int id = sp.getComponentId();
			paddles[id] = sp;
			goodIds[count] = (short) id;

			cachePaddleCornersFromPaddle(id, sp);

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
	 * Copy one JLab paddle into the explicit primitive corner cache.
	 *
	 * @param componentId the FTCAL component id
	 * @param paddle      the JLab scintillator paddle
	 */
	private static void cachePaddleCornersFromPaddle(int componentId, ScintillatorPaddle paddle) {
		if (!validId(componentId) || (paddle == null)) {
			return;
		}

		paddleCorners[componentId] = new double[CORNER_COUNT][COORD_COUNT];

		for (int corner = 0; corner < CORNER_COUNT; corner++) {
			Point3D point = paddle.getVolumePoint(corner);
			paddleCorners[componentId][corner][0] = point.x();
			paddleCorners[componentId][corner][1] = point.y();
			paddleCorners[componentId][corner][2] = point.z();
		}
	}

	/**
	 * Convert XY grid indices into a component id.
	 *
	 * @param p the XY grid indices
	 * @return the component id
	 */
	public static int xyIndicesToId(Point p) {
		return indicesToId.get(p);
	}

	/**
	 * Convert XY grid indices into a component id.
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
	 * Get a scintillator paddle.
	 * <p>
	 * This returns a JLab geometry object only when FTCAL was initialized directly
	 * from CCDB. After cache initialization, FTCAL uses explicit primitive corner
	 * data and this method returns {@code null}.
	 *
	 * @param componentId the component id
	 * @return the paddle, or {@code null} if unavailable
	 */
	public static ScintillatorPaddle getPaddle(int componentId) {
		if (!validId(componentId) || (paddles == null)) {
			return null;
		}
		return paddles[componentId];
	}

	/**
	 * Used by 3D drawing.
	 *
	 * @param paddleId the component id
	 * @param coords   holds 8*3 = 24 values [x1, y1, z1, ..., x8, y8, z8]
	 */
	public static void paddleVertices(int paddleId, float[] coords) {
		if (!validPaddleGeometry(paddleId) || (coords == null) || (coords.length < CORNER_COUNT * COORD_COUNT)) {
			return;
		}

		double corners[][] = paddleCorners[paddleId];

		for (int i = 0; i < CORNER_COUNT; i++) {
			int j = COORD_COUNT * i;
			coords[j] = (float) corners[i][0];
			coords[j + 1] = (float) corners[i][1];

			// note the offset
			coords[j + 2] = (float) corners[i][2] - FTCAL_Z0;
		}
	}

	/**
	 * Obtain the paddle XY corners for a 2D view.
	 *
	 * @param paddleId the component id
	 * @param wp       the four XY corners in cm
	 */
	public static void paddleXYCorners(int paddleId, Point2D.Double[] wp) {
		if (!validPaddleGeometry(paddleId) || !validXYArray(wp)) {
			return;
		}

		double corners[][] = paddleCorners[paddleId];

		for (int i = 0; i < XY_CORNER_COUNT; i++) {
			wp[i].x = corners[i][0];
			wp[i].y = corners[i][1];
		}
	}

	/**
	 * Get the XY center of a paddle.
	 *
	 * @param paddleId the component id
	 * @param center   receives the center, or NaNs if the id is invalid
	 */
	public static void paddleXYCenter(int paddleId, Point2D.Double center) {
		if (center == null) {
			return;
		}

		if (!validPaddleGeometry(paddleId)) {
			center.setLocation(Double.NaN, Double.NaN);
			return;
		}

		double corners[][] = paddleCorners[paddleId];

		double xsum = 0.0;
		double ysum = 0.0;

		for (int i = 0; i < CORNER_COUNT; i++) {
			xsum += corners[i][0];
			ysum += corners[i][1];
		}

		center.setLocation(xsum / CORNER_COUNT, ysum / CORNER_COUNT);
	}

	/**
	 * Get the XY grid indices for a component id.
	 *
	 * @param id the component id
	 * @return the XY grid indices, or {@code null}
	 */
	public static Point getXYIndices(int id) {
		if ((id < 1) || (id > MAXID)) {
			return null;
		}

		return paddleXYIndices[id];
	}

	/**
	 * Get the XY grid indices for a paddle.
	 * <p>
	 * This method is a legacy convenience for code paths that still have a JLab
	 * paddle object. After cache initialization, such objects are not retained.
	 *
	 * @param paddle the paddle
	 * @return the XY grid indices, or {@code null}
	 */
	public static Point getXYIndices(ScintillatorPaddle paddle) {
		if (paddle == null) {
			return null;
		}
		return getXYIndices(paddle.getComponentId());
	}

	/**
	 * Return the grid index [-11, -10, ... -1, 1, ..., 11] for a coordinate value.
	 *
	 * @param val an x or y coordinate
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
	 * Get the maximum absolute extent in x or y. Used for grid drawing.
	 *
	 * @return the maximum absolute extent in x or y
	 */
	public static final double getMaxAbsXYExtent() {
		return Math.abs(gvals[0]);
	}

	/**
	 * Convert an index [-11, -10, ... -1, 1, ..., 11] to coordinate limits.
	 *
	 * @param index the grid index
	 * @param range receives the coordinate limits
	 */
	public static void indexToRange(int index, double range[]) {
		if ((index < -11) || (index > 11) || (index == 0)) {
			range[0] = Double.NaN;
			range[1] = Double.NaN;
		} else {
			// take into account 0 is not valid
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
	 * @param paddleId the component id
	 * @param corners  receives the eight XYZ corners in cm, with z shifted by
	 *                 {@link #FTCAL_Z0}
	 */
	public static void paddle3DCorners(int paddleId, Point3D corners[]) {
		if (!validPaddleGeometry(paddleId) || (corners == null) || (corners.length < CORNER_COUNT)) {
			return;
		}

		double pcorners[][] = paddleCorners[paddleId];

		for (int i = 0; i < CORNER_COUNT; i++) {
			corners[i] = new Point3D(pcorners[i][0], pcorners[i][1], pcorners[i][2] - FTCAL_Z0);
		}
	}

	/**
	 * Check whether the id is one of the valid FTCAL component ids.
	 *
	 * @param id the component id to check
	 * @return {@code true} if it is a good id
	 */
	public static boolean isGoodId(int id) {
		return validPaddleGeometry(id);
	}

	/**
	 * Get all valid FTCAL component ids.
	 *
	 * @return all valid component ids
	 */
	public static short[] getGoodIds() {
		return goodIds;
	}

	/**
	 * Get a valid component id by good-id-array index.
	 *
	 * @param index the index into the good id array
	 * @return the component id
	 */
	public static short getGoodId(int index) {
		return goodIds[index];
	}

	/**
	 * Check whether an FTCAL component id is in the legal array range.
	 *
	 * @param id the component id
	 * @return {@code true} if the id is in range
	 */
	private static boolean validId(int id) {
		return (id >= 1) && (id <= MAXID);
	}

	/**
	 * Check whether explicit geometry exists for an FTCAL component id.
	 *
	 * @param id the component id
	 * @return {@code true} if the id has cached/runtime corner geometry
	 */
	private static boolean validPaddleGeometry(int id) {
		return validId(id) && (paddleCorners != null) && (paddleCorners[id] != null);
	}

	/**
	 * Check whether the caller supplied a usable four-point XY array.
	 *
	 * @param wp the XY corner array
	 * @return {@code true} if the array is usable
	 */
	private static boolean validXYArray(Point2D.Double[] wp) {
		if ((wp == null) || (wp.length < XY_CORNER_COUNT)) {
			return false;
		}

		for (int i = 0; i < XY_CORNER_COUNT; i++) {
			if (wp[i] == null) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Read FTCAL geometry from the cache.
	 * <p>
	 * This reads explicit primitive corner data rather than JLab
	 * {@link ScintillatorPaddle} objects.
	 *
	 * @param kryo  the Kryo instance, retained for interface compatibility
	 * @param input the Kryo input stream
	 * @return {@code true} if the geometry was read successfully
	 */
	@Override
	public boolean readGeometry(Kryo kryo, Input input) {
		try {
			int cornerArrayLen = input.readInt();
			if (cornerArrayLen != (MAXID + 1)) {
				System.err.printf("FTCALGeometry: expected corner array length %d, found %d in cache.%n",
						MAXID + 1, cornerArrayLen);
				return false;
			}

			double corners[][][] = new double[MAXID + 1][][];

			for (int id = 0; id < cornerArrayLen; id++) {
				boolean hasPaddle = input.readBoolean();
				if (hasPaddle) {
					int numCorners = input.readInt();
					if (numCorners != CORNER_COUNT) {
						System.err.printf("FTCALGeometry: expected %d corners for id %d, found %d in cache.%n",
								CORNER_COUNT, id, numCorners);
						return false;
					}

					corners[id] = new double[CORNER_COUNT][COORD_COUNT];

					for (int corner = 0; corner < CORNER_COUNT; corner++) {
						int numCoords = input.readInt();
						if (numCoords != COORD_COUNT) {
							System.err.printf(
									"FTCALGeometry: expected %d coordinates for id %d corner %d, found %d in cache.%n",
									COORD_COUNT, id, corner, numCoords);
							return false;
						}

						for (int coord = 0; coord < COORD_COUNT; coord++) {
							corners[id][corner][coord] = input.readDouble();
						}
					}
				}
			}

			int goodIdsLen = input.readInt();
			if (goodIdsLen != GOOD_ID_COUNT) {
				System.err.printf("FTCALGeometry: expected %d good ids, found %d in cache.%n", GOOD_ID_COUNT,
						goodIdsLen);
				return false;
			}

			short gids[] = new short[goodIdsLen];
			for (int i = 0; i < goodIdsLen; i++) {
				gids[i] = input.readShort();
			}

			int indicesLen = input.readInt();
			if (indicesLen != (MAXID + 1)) {
				System.err.printf("FTCALGeometry: expected index array length %d, found %d in cache.%n",
						MAXID + 1, indicesLen);
				return false;
			}

			Point xyIndices[] = new Point[indicesLen];
			for (int i = 0; i < indicesLen; i++) {
				boolean hasPoint = input.readBoolean();
				if (hasPoint) {
					xyIndices[i] = new Point(input.readInt(), input.readInt());
				}
			}

			Hashtable<Point, Integer> table = new Hashtable<>();
			int tableSize = input.readInt();
			for (int i = 0; i < tableSize; i++) {
				Point key = new Point(input.readInt(), input.readInt());
				int value = input.readInt();
				table.put(key, value);
			}

			paddleCorners = corners;
			goodIds = gids;
			paddleXYIndices = xyIndices;
			indicesToId = table;

			// Do not keep stale JLab geometry objects after a cache read.
			paddles = null;

			return true;
		} catch (Exception e) {
			System.err.println("FTCALGeometry: Error reading cached geometry: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Write FTCAL geometry to the cache.
	 * <p>
	 * This writes explicit primitive corner data rather than JLab
	 * {@link ScintillatorPaddle} objects.
	 *
	 * @param kryo   the Kryo instance, retained for interface compatibility
	 * @param output the Kryo output stream
	 * @return {@code true} if the geometry was written successfully
	 */
	@Override
	public boolean writeGeometry(Kryo kryo, Output output) {
		try {
			if ((paddleCorners == null) && (paddles != null)) {
				paddleCorners = new double[MAXID + 1][][];
				for (int id = 1; id <= MAXID; id++) {
					if (paddles[id] != null) {
						cachePaddleCornersFromPaddle(id, paddles[id]);
					}
				}
			}

			if (paddleCorners == null) {
				System.err.println("FTCALGeometry: no paddle corner geometry available to write.");
				return false;
			}

			output.writeInt(MAXID + 1);

			for (int id = 0; id <= MAXID; id++) {
				boolean hasPaddle = (paddleCorners[id] != null);
				output.writeBoolean(hasPaddle);

				if (hasPaddle) {
					output.writeInt(CORNER_COUNT);

					for (int corner = 0; corner < CORNER_COUNT; corner++) {
						output.writeInt(COORD_COUNT);

						for (int coord = 0; coord < COORD_COUNT; coord++) {
							output.writeDouble(paddleCorners[id][corner][coord]);
						}
					}
				}
			}

			if ((goodIds == null) || (goodIds.length != GOOD_ID_COUNT)) {
				System.err.println("FTCALGeometry: invalid good id array. Not writing cache.");
				return false;
			}

			output.writeInt(goodIds.length);
			for (short id : goodIds) {
				output.writeShort(id);
			}

			if ((paddleXYIndices == null) || (paddleXYIndices.length != (MAXID + 1))) {
				System.err.println("FTCALGeometry: invalid XY index array. Not writing cache.");
				return false;
			}

			output.writeInt(paddleXYIndices.length);
			for (Point pt : paddleXYIndices) {
				boolean hasPoint = (pt != null);
				output.writeBoolean(hasPoint);
				if (hasPoint) {
					output.writeInt(pt.x);
					output.writeInt(pt.y);
				}
			}

			output.writeInt(indicesToId.size());
			for (Point key : indicesToId.keySet()) {
				output.writeInt(key.x);
				output.writeInt(key.y);
				output.writeInt(indicesToId.get(key));
			}

			return true;
		} catch (Exception e) {
			System.err.println("FTCALGeometry: Error writing cached geometry: " + e.getMessage());
			return false;
		}
	}
}