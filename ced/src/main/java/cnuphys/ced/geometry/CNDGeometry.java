package cnuphys.ced.geometry;

import java.awt.geom.Point2D;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.jlab.detector.base.GeometryFactory;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.component.ScintillatorPaddle;
import org.jlab.geom.detector.cnd.CNDDetector;
import org.jlab.geom.detector.cnd.CNDFactory;
import org.jlab.geom.detector.cnd.CNDLayer;
import org.jlab.geom.detector.cnd.CNDSector;
import org.jlab.geom.detector.cnd.CNDSuperlayer;
import org.jlab.geom.prim.Point3D;


import cnuphys.ced.geometry.cache.ACachedGeometry;

/**
 * Central Neutron Detector
 *
 * @author heddle
 *
 */
public class CNDGeometry extends ACachedGeometry {

	public CNDGeometry() {
		super("CNDGeometry");
	}
	private static final int LAYER_COUNT = 3;
	private static final int PADDLE_COUNT = 48;
	private static final int CORNER_COUNT = 8;
	private static final int COORD_COUNT = 3;

	// there are 48 paddles per layer
	private static ScintillatorPaddle paddles[][];
	private static double paddleCorners[][][][];

	/**
	 * Initialize the CND Geometry by loading all the wires
	 */
	@Override
	public void initializeUsingCCDB() {

		System.out.println("\n=====================================");
		System.out.println("==== CND Geometry Initialization ====");
		System.out.println("=====================================");

		ConstantProvider cndDataProvider = GeometryFactory.getConstants(org.jlab.detector.base.DetectorType.CND);

		CNDFactory cndFactory = new CNDFactory();
		CNDDetector cndDetector = cndFactory.createDetectorCLAS(cndDataProvider);

		// one sector, one superlayer
		CNDSector cndSector = cndDetector.getSector(0);
		CNDSuperlayer cndSuperlayer = cndSector.getSuperlayer(0);

		// three layers
		CNDLayer[] cndLayers = new CNDLayer[LAYER_COUNT];
		paddles = new ScintillatorPaddle[LAYER_COUNT][PADDLE_COUNT];
		paddleCorners = new double[LAYER_COUNT][PADDLE_COUNT][CORNER_COUNT][COORD_COUNT];
		for (int i = 0; i < cndLayers.length; i++) {
			cndLayers[i] = cndSuperlayer.getLayer(i);
			for (int j = 0; j < PADDLE_COUNT; j++) {

				paddles[i][j] = cndLayers[i].getComponent(j);

				// rotate do to geomtry change
				paddles[i][j].rotateZ(Math.toRadians(7.5));
				for (int corner = 0; corner < CORNER_COUNT; corner++) {
					Point3D point = paddles[i][j].getVolumePoint(corner);
					paddleCorners[i][j][corner][0] = point.x();
					paddleCorners[i][j][corner][1] = point.y();
					paddleCorners[i][j][corner][2] = point.z();
				}

			}
		}

	}

	/**
	 * Converts the numbering from Gagik's database to real. This should not be
	 * necessary but yet it is.
	 *
	 * @param geo  the geo triplets where sect=1, layer=1..3, component = 1..48
	 * @param real the real triplets where sector = 1..24, layer=1..3, component =
	 *             1..2
	 */
	public static void geoTripletToRealTriplet(int geo[], int real[]) {
		int gL = geo[1]; // 1..3
		int gC = geo[2]; // 1.48

		int t = 1 + (gC % 48);
		int s = 1 + ((t - 1) / 2);
		int c = (t % 2) == 0 ? 2 : 1;

		real[0] = s;
		real[1] = gL;
		real[2] = c;
	}

	/**
	 * Converts the numbering from real to Gagik's database to real. This should not
	 * be necessary but yet it is.
	 *
	 * @param geo  the geo triplets where sect=1, layer=1..3, component = 1..48
	 * @param real the real triplets where sector = 1..24, layer=1..3, component =
	 *             1..2
	 */
	public static void realTripletToGeoTriplet(int geo[], int real[]) {
		int s = real[0]; // 1..24
		int l = real[1]; // 1..3
		int c = real[2]; // 1..2

		int u = 2 * (s - 1) + c;
		int gC = (u - 1) % 48;
		if (gC == 0) {
			gC = 48;
		}

		geo[0] = 1;
		geo[1] = l;
		geo[2] = gC;
	}

	/**
	 * Get a scintillator paddle
	 *
	 * @param layer  the layer [1..3]
	 * @param paddle the paddles [1..48]
	 * @return the paddle
	 */
	public static ScintillatorPaddle getPaddle(int layer, int paddle) {
		if ((layer < 1) || (layer > LAYER_COUNT) || (paddle < 1) || (paddle > PADDLE_COUNT)
				|| paddles == null) {
			return null;
		}
		return paddles[layer - 1][paddle - 1];
	}

	/**
	 * Used by the 3D drawing
	 *
	 * @param layer    the 1-based layer 1..3
	 * @param paddleId the 1-based paddle 1..48
	 * @param coords   holds 8*3 = 24 values [x1, y1, z1, ..., x8, y8, z8]
	 */
	public static void paddleVertices(int layer, int paddleId, float[] coords) {
		double[][] corners = getPaddleCorners(layer, paddleId);
		if (corners == null || coords == null || coords.length < CORNER_COUNT * COORD_COUNT) {
			return;
		}
		for (int i = 0; i < CORNER_COUNT; i++) {
			int j = 3 * i;
			coords[j] = (float) corners[i][0];
			coords[j + 1] = (float) corners[i][1];
			coords[j + 2] = (float) corners[i][2];
		}
	}

	/**
	 * Obtain the paddle xy corners for 2D view
	 *
	 * @param layer    the layer 1..3
	 * @param paddleId the paddle ID 1..48
	 * @param wp       the four XY corners (cm)
	 */
	public static void paddleXYCorners(int layer, int paddleId, Point2D.Double[] wp) {
		double[][] corners = getPaddleCorners(layer, paddleId);
		if (corners == null || wp == null || wp.length < 4) {
			return;
		}

		for (int i = 0; i < 4; i++) {
			wp[i].x = corners[i][0];
			wp[i].y = corners[i][1];
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
	 * @param layer    the layer 1..3
	 * @param paddleId the paddle ID 1..48
	 * @param corners  the eight XYZ corners (cm)
	 */
	public static void paddle3DCorners(int layer, int paddleId, Point3D corners[]) {
		double[][] cachedCorners = getPaddleCorners(layer, paddleId);
		if (cachedCorners == null || corners == null || corners.length < CORNER_COUNT) {
			return;
		}

		for (int i = 0; i < CORNER_COUNT; i++) {
			corners[i] = new Point3D(cachedCorners[i][0], cachedCorners[i][1], cachedCorners[i][2]);
		}

	}

	private static double[][] getPaddleCorners(int layer, int paddle) {
		if (paddleCorners == null || layer < 1 || layer > LAYER_COUNT || paddle < 1 || paddle > PADDLE_COUNT) {
			return null;
		}
		return paddleCorners[layer - 1][paddle - 1];
	}

	@Override
	public boolean supportsCache() {
		return true;
	}

	@Override
	public void readGeometry(DataInput input) throws IOException {
		int layers = input.readInt();
		int paddleCount = input.readInt();
		int cornerCount = input.readInt();
		int coordinateCount = input.readInt();
		if (layers != LAYER_COUNT || paddleCount != PADDLE_COUNT || cornerCount != CORNER_COUNT
				|| coordinateCount != COORD_COUNT) {
			throw new IOException("Invalid CND geometry dimensions");
		}
		double[][][][] corners = new double[LAYER_COUNT][PADDLE_COUNT][CORNER_COUNT][COORD_COUNT];
		for (int layer = 0; layer < LAYER_COUNT; layer++) {
			for (int paddle = 0; paddle < PADDLE_COUNT; paddle++) {
				for (int corner = 0; corner < CORNER_COUNT; corner++) {
					for (int coordinate = 0; coordinate < COORD_COUNT; coordinate++) {
						corners[layer][paddle][corner][coordinate] = input.readDouble();
					}
				}
			}
		}
		paddles = null;
		paddleCorners = corners;
	}

	@Override
	public void writeGeometry(DataOutput output) throws IOException {
		if (paddleCorners == null) {
			throw new IOException("CND geometry is not initialized");
		}
		output.writeInt(LAYER_COUNT);
		output.writeInt(PADDLE_COUNT);
		output.writeInt(CORNER_COUNT);
		output.writeInt(COORD_COUNT);
		for (double[][][] layer : paddleCorners) {
			for (double[][] paddle : layer) {
				for (double[] corner : paddle) {
					for (double coordinate : corner) {
						output.writeDouble(coordinate);
					}
				}
			}
		}
	}

}
