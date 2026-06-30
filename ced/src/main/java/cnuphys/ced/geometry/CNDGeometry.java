package cnuphys.ced.geometry;

import java.awt.geom.Point2D;

import org.jlab.detector.base.GeometryFactory;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.component.ScintillatorPaddle;
import org.jlab.geom.detector.cnd.CNDDetector;
import org.jlab.geom.detector.cnd.CNDFactory;
import org.jlab.geom.detector.cnd.CNDLayer;
import org.jlab.geom.detector.cnd.CNDSector;
import org.jlab.geom.detector.cnd.CNDSuperlayer;
import org.jlab.geom.prim.Point3D;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import cnuphys.ced.geometry.cache.ACachedGeometry;
import cnuphys.ced.geometry.cache.GeometryPrimitiveIO;

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

	// Runtime JLab geometry object graph. This is still used for now.
	private static ScintillatorPaddle paddles[][];

	// Explicit cache/runtime geometry data.
	// Index order: [layer][paddle][corner][xyz].
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
		CNDLayer[] cndLayers = new CNDLayer[3];
		paddles = new ScintillatorPaddle[3][48];
		for (int i = 0; i < cndLayers.length; i++) {
			cndLayers[i] = cndSuperlayer.getLayer(i);
			for (int j = 0; j < 48; j++) {

				paddles[i][j] = cndLayers[i].getComponent(j);

				// rotate do to geomtry change
				paddles[i][j].rotateZ(Math.toRadians(7.5));

			}
		}

		cachePaddleCornersFromPaddles();

	}

	/**
	 * Copy the current JLab paddle geometry into an explicit primitive corner
	 * cache.
	 */
	private static void cachePaddleCornersFromPaddles() {
		paddleCorners = new double[LAYER_COUNT][PADDLE_COUNT][CORNER_COUNT][COORD_COUNT];

		for (int layer = 0; layer < LAYER_COUNT; layer++) {
			for (int paddleId = 0; paddleId < PADDLE_COUNT; paddleId++) {
				ScintillatorPaddle paddle = paddles[layer][paddleId];

				for (int corner = 0; corner < CORNER_COUNT; corner++) {
					Point3D point = paddle.getVolumePoint(corner);
					paddleCorners[layer][paddleId][corner][0] = point.x();
					paddleCorners[layer][paddleId][corner][1] = point.y();
					paddleCorners[layer][paddleId][corner][2] = point.z();
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
		if ((layer < 1) || (layer > LAYER_COUNT) || (paddle < 1) || (paddle > PADDLE_COUNT) || (paddles == null)) {
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
		if (!validLayerAndPaddle(layer, paddleId) || (coords == null) || (coords.length < CORNER_COUNT * COORD_COUNT)) {
			return;
		}

		double corners[][] = paddleCorners[layer - 1][paddleId - 1];

		for (int i = 0; i < CORNER_COUNT; i++) {
			int j = COORD_COUNT * i;
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
		if (!validLayerAndPaddle(layer, paddleId) || !validXYArray(wp)) {
			return;
		}

		double corners[][] = paddleCorners[layer - 1][paddleId - 1];

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
		if (!validLayerAndPaddle(layer, paddleId) || (corners == null) || (corners.length < CORNER_COUNT)) {
			return;
		}

		double pcorners[][] = paddleCorners[layer - 1][paddleId - 1];

		for (int i = 0; i < CORNER_COUNT; i++) {
			corners[i] = new Point3D(pcorners[i][0], pcorners[i][1], pcorners[i][2]);
		}
	}

	private static boolean validLayerAndPaddle(int layer, int paddle) {
		return (layer >= 1) && (layer <= LAYER_COUNT) && (paddle >= 1) && (paddle <= PADDLE_COUNT)
				&& (paddleCorners != null);
	}

	private static boolean validXYArray(Point2D.Double[] wp) {
		if ((wp == null) || (wp.length < 4)) {
			return false;
		}

		for (int i = 0; i < 4; i++) {
			if (wp[i] == null) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean readGeometry(Kryo kryo, Input input) {
		try {
			int numLayers = input.readInt();
			if (numLayers != LAYER_COUNT) {
				System.err.printf("CNDGeometry: expected %d layers, found %d in cache.%n", LAYER_COUNT, numLayers);
				return false;
			}

			double corners[][][][] = new double[LAYER_COUNT][PADDLE_COUNT][CORNER_COUNT][COORD_COUNT];

			for (int layer = 0; layer < LAYER_COUNT; layer++) {
				int numPaddles = input.readInt();
				if (numPaddles != PADDLE_COUNT) {
					System.err.printf("CNDGeometry: expected %d paddles for layer %d, found %d in cache.%n",
							PADDLE_COUNT, layer + 1, numPaddles);
					return false;
				}

				for (int paddle = 0; paddle < PADDLE_COUNT; paddle++) {
					corners[layer][paddle] = GeometryPrimitiveIO.readCorners(input, CORNER_COUNT, COORD_COUNT,
							String.format("CNDGeometry layer %d paddle %d", layer + 1, paddle + 1));
				}
			}

			paddleCorners = corners;

			// The explicit cache provides the geometry used by drawing methods.
			// Do not keep stale JLab geometry objects around after a cache read.
			paddles = null;

			return true;
		} catch (Exception e) {
			System.err.println("CNDGeometry: Error reading cached geometry: " + e.getMessage());
			return false;
		}
	}

	@Override
	public boolean writeGeometry(Kryo kryo, Output output) {
		try {
			if (paddleCorners == null) {
				if (paddles == null) {
					System.err.println("CNDGeometry: no paddle geometry available to write.");
					return false;
				}
				cachePaddleCornersFromPaddles();
			}

			output.writeInt(LAYER_COUNT);

			for (int layer = 0; layer < LAYER_COUNT; layer++) {
				output.writeInt(PADDLE_COUNT);

				for (int paddle = 0; paddle < PADDLE_COUNT; paddle++) {
					GeometryPrimitiveIO.writeCorners(output, paddleCorners[layer][paddle], CORNER_COUNT, COORD_COUNT);
				}
			}

			return true;
		} catch (Exception e) {
			System.err.println("CNDGeometry: Error writing cached geometry: " + e.getMessage());
			return false;
		}
	}
}
