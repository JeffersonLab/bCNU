package cnuphys.ced.geometry;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jlab.detector.calib.utils.DatabaseConstantProvider;
import org.jlab.detector.geant4.v2.SVT.SVTStripFactory;
import org.jlab.geometry.prim.Line3d;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.frame.Ced;
import cnuphys.ced.geometry.cache.ACachedGeometry;
import eu.mihosoft.vrl.v3d.Vector3d;

/**
 * Geometry support for the Barrel Silicon Tracker (BST).
 * <p>
 * The BST geometry used by CED consists of:
 *
 * <pre>
 * strips[layer][sector][strip] -> Line3d
 * XY panel summaries            -> BSTxyPanel
 * </pre>
 *
 * Historically, this class cached the {@code ArrayList<BSTxyPanel>} and
 * {@code HashMap<String, Line3d>} directly through Kryo. The cache now stores
 * only explicit primitive strip endpoint data. The strip map and XY panel list
 * are reconstructed after a cache read.
 */
public class BSTGeometry extends ACachedGeometry {

	/** Number of BST layers. */
	private static final int LAYER_COUNT = 6;

	/** Number of strips per sector/layer. */
	private static final int STRIP_COUNT = 256;

	/** Number of endpoints in one strip line. */
	private static final int LINE_ENDPOINT_COUNT = 2;

	/** Number of coordinates per endpoint. */
	private static final int COORD_COUNT = 3;

	/** BST XY panels. */
	private static ArrayList<BSTxyPanel> _bstXYpanelsLayers = new ArrayList<>();

	/** Dead-zone gap in mm. */
	private static final double ZGAP = 1.67;

	/** Sectors per BST layer. */
	public static final int[] sectorsPerLayer = { 10, 10, 14, 14, 18, 18 };

	/** All strips, keyed by layer_sector_strip. */
	private static HashMap<String, Line3d> _strips;

	/**
	 * Constructor.
	 */
	public BSTGeometry() {
		super("BST Geometry");
	}

	/**
	 * Initialize the BST geometry from CCDB/JLab geometry services.
	 */
	@Override
	public void initializeUsingCCDB() {
		System.out.println("\n=====================================");
		System.out.println("===  BST Geometry Initialization  ===");
		System.out.println("=====================================");

		String variationName = Ced.getGeometryVariation();
		DatabaseConstantProvider cp = new DatabaseConstantProvider(11, variationName);

		SVTStripFactory svtFac = new SVTStripFactory(cp, true);

		getStrips(svtFac);
		getBSTPanels();
	}

	/**
	 * Get the list of BST XY panels.
	 *
	 * @return the panels
	 */
	public static List<BSTxyPanel> getBSTxyPanels() {
		return _bstXYpanelsLayers;
	}

	/**
	 * Build the BST XY panel list from the current strip geometry.
	 */
	private static void getBSTPanels() {
		_bstXYpanelsLayers = new ArrayList<>();

		double vals[] = new double[10];

		for (int layer = 0; layer < LAYER_COUNT; layer++) {
			int numSect = sectorsPerLayer[layer];

			for (int sector = 0; sector < numSect; sector++) {
				getLimitValues(sector, layer, vals);
				_bstXYpanelsLayers.add(new BSTxyPanel(sector + 1, layer + 1, vals));
			}
		}
	}

	/**
	 * Build the strip map from the SVT strip factory.
	 *
	 * @param svtFac the SVT strip factory
	 */
	private static void getStrips(SVTStripFactory svtFac) {
		_strips = new HashMap<>();

		for (int layer = 0; layer < LAYER_COUNT; layer++) {
			int numSect = sectorsPerLayer[layer];

			for (int sector = 0; sector < numSect; sector++) {
				for (int strip = 0; strip < STRIP_COUNT; strip++) {
					Line3d line = svtFac.getStrip(layer, sector, strip);
					_strips.put(hashKey(layer, sector, strip), line);
				}
			}
		}
	}

	/**
	 * Build a unique strip key.
	 *
	 * @param layer  the 0-based layer
	 * @param sector the 0-based sector
	 * @param strip  the 0-based strip
	 * @return the key
	 */
	private static String hashKey(int layer, int sector, int strip) {
		return layer + "_" + sector + "_" + strip;
	}

	/**
	 * Get the strip as a line.
	 *
	 * @param sector 0-based sector
	 * @param layer  0-based layer
	 * @param strip  0-based strip, 0..255
	 * @return the strip line, in mm, or {@code null}
	 */
	public static Line3d getStrip(int sector, int layer, int strip) {
		try {
			if (!validStripAddress(sector, layer, strip) || (_strips == null)) {
				return null;
			}

			return _strips.get(hashKey(layer, sector, strip));
		} catch (IllegalArgumentException e) {
			System.err.println("Event number: " + ClasIoEventManager.getInstance().getSequentialEventNumber() + "  "
					+ e.getMessage());
			System.err.println("Illegal Values: getStrip: sector=" + (sector + 1) + " layer=" + (layer + 1)
					+ " strip=" + (strip + 1));
		}

		return null;
	}

	/**
	 * Get the strip coordinates for 3D drawing in cm.
	 *
	 * @param sector 0-based sector
	 * @param layer  0-based layer
	 * @param strip  0-based strip
	 * @param coords receives [x1, y1, z1, x2, y2, z2] in cm
	 */
	public static void getStripCM(int sector, int layer, int strip, float coords[]) {
		getStrip(sector, layer, strip, coords);

		if (coords != null) {
			for (int i = 0; i < coords.length; i++) {
				coords[i] /= 10;
			}
		}
	}

	/**
	 * Get the strip coordinates for 3D drawing in mm.
	 *
	 * @param sector 0-based sector
	 * @param layer  0-based layer
	 * @param strip  0-based strip
	 * @param coords receives [x1, y1, z1, x2, y2, z2] in mm
	 */
	public static void getStrip(int sector, int layer, int strip, float coords[]) {
		if ((coords == null) || (coords.length < 6)) {
			return;
		}

		Line3d line = getStrip(sector, layer, strip);

		if (line != null) {
			coords[0] = (float) line.origin().x;
			coords[1] = (float) line.origin().y;
			coords[2] = (float) line.origin().z;
			coords[3] = (float) line.end().x;
			coords[4] = (float) line.end().y;
			coords[5] = (float) line.end().z;
		}
	}

	/**
	 * Get the triplet quad coordinates for the 3D view. Units are cm.
	 *
	 * @param sector 0-based sector
	 * @param layer  0-based layer
	 * @param coords receives three quads, each with four 3D points
	 */
	public static void getLayerQuads(int sector, int layer, float coords[]) {
		if ((coords == null) || (coords.length < 36)) {
			return;
		}

		double vals[] = new double[10];

		getLimitValues(sector, layer, vals);

		for (int i = 0; i < vals.length; i++) {
			vals[i] /= 10;
		}

		float x1 = (float) vals[0];
		float y1 = (float) vals[1];
		float x2 = (float) vals[2];
		float y2 = (float) vals[3];

		float z1 = (float) vals[4];
		float z2 = (float) vals[5];
		float z3 = (float) vals[6];
		float z4 = (float) vals[7];
		float z5 = (float) vals[8];
		float z6 = (float) vals[9];

		fillCoords(0, coords, x1, y1, x2, y2, z1, z2);
		fillCoords(12, coords, x1, y1, x2, y2, z3, z4);
		fillCoords(24, coords, x1, y1, x2, y2, z5, z6);
	}

	/**
	 * Fill one quad in a flat coordinate array.
	 *
	 * @param index starting index
	 * @param coords coordinate array
	 * @param x1 first x
	 * @param y1 first y
	 * @param x2 second x
	 * @param y2 second y
	 * @param zmin minimum z
	 * @param zmax maximum z
	 */
	private static void fillCoords(int index, float coords[], float x1, float y1, float x2, float y2, float zmin,
			float zmax) {
		coords[index++] = x1;
		coords[index++] = y1;
		coords[index++] = zmin;
		coords[index++] = x1;
		coords[index++] = y1;
		coords[index++] = zmax;
		coords[index++] = x2;
		coords[index++] = y2;
		coords[index++] = zmax;
		coords[index++] = x2;
		coords[index++] = y2;
		coords[index++] = zmin;
	}

	/**
	 * Get the BST panel limit values used by the BST views.
	 * <p>
	 * All values are in mm. The first four are x/y endpoints for drawing the XY
	 * view. The last six are the z values that define the three active regions.
	 *
	 * @param sector 0-based sector
	 * @param layer  0-based layer
	 * @param vals   receives ten values
	 */
	public static void getLimitValues(int sector, int layer, double vals[]) {
		if ((vals == null) || (vals.length < 10)) {
			return;
		}

		Line3d line0 = getStrip(sector, layer, 0);
		Line3d line1 = getStrip(sector, layer, STRIP_COUNT - 1);

		if ((line0 == null) || (line1 == null)) {
			for (int i = 0; i < 10; i++) {
				vals[i] = Double.NaN;
			}
			return;
		}

		Vector3d o = line0.origin();
		Vector3d e = line1.end();

		vals[0] = o.x;
		vals[1] = o.y;
		vals[2] = e.x;
		vals[3] = e.y;

		double z0 = Double.POSITIVE_INFINITY;
		double z5 = Double.NEGATIVE_INFINITY;

		for (int strip = 0; strip < STRIP_COUNT; strip++) {
			Line3d line = getStrip(sector, layer, strip);

			if (line == null) {
				continue;
			}

			Vector3d p0 = line.origin();
			Vector3d p1 = line.end();

			z0 = Math.min(z0, p0.z);
			z0 = Math.min(z0, p1.z);
			z5 = Math.max(z5, p0.z);
			z5 = Math.max(z5, p1.z);
		}

		double del = (z5 - z0) / 3;
		double z1 = z0 + del - ZGAP / 2;
		double z2 = z1 + ZGAP;
		double z3 = z5 - del - ZGAP / 2;
		double z4 = z3 + ZGAP;

		vals[4] = z0;
		vals[5] = z1;
		vals[6] = z2;
		vals[7] = z3;
		vals[8] = z4;
		vals[9] = z5;
	}

	/**
	 * Get the XY coordinates of the midpoint of a strip.
	 *
	 * @param sector 0-based sector
	 * @param layer  0-based layer
	 * @param strip  0-based strip
	 * @return the midpoint with z dropped
	 */
	public static Point2D.Double getStripMidpointXY(int sector, int layer, int strip) {
		Point2D.Double wp = new Point2D.Double();
		getStripMidpointXY(sector, layer, strip, wp);
		return wp;
	}

	/**
	 * Get the XY coordinates of the midpoint of a strip.
	 *
	 * @param sector 0-based sector
	 * @param layer  0-based layer
	 * @param strip  0-based strip
	 * @param wp     receives the midpoint
	 */
	public static void getStripMidpointXY(int sector, int layer, int strip, Point2D.Double wp) {
		if (wp == null) {
			return;
		}

		Line3d line = getStrip(sector, layer, strip);

		if (line == null) {
			wp.setLocation(Double.NaN, Double.NaN);
			return;
		}

		Vector3d p0 = line.origin();
		Vector3d p1 = line.end();

		double xmp = 0.5 * (p0.x + p1.x);
		double ymp = 0.5 * (p0.y + p1.y);

		wp.setLocation(xmp, ymp);
	}

	/**
	 * Get the 3D midpoint of a strip.
	 *
	 * @param sector 0-based sector
	 * @param layer  0-based layer
	 * @param strip  0-based strip
	 * @return the strip midpoint in mm
	 */
	public static Vector3d getStripMidpoint(int sector, int layer, int strip) {
		Line3d line = getStrip(sector, layer, strip);

		if (line == null) {
			return new Vector3d(Double.NaN, Double.NaN, Double.NaN);
		}

		Vector3d p0 = line.origin();
		Vector3d p1 = line.end();

		double xmp = 0.5 * (p0.x + p1.x);
		double ymp = 0.5 * (p0.y + p1.y);
		double zmp = 0.5 * (p0.z + p1.z);

		return new Vector3d(xmp, ymp, zmp);
	}

	/**
	 * Read BST geometry from the cache.
	 * <p>
	 * The cache stores explicit primitive strip endpoint data. The strip map and
	 * XY panel list are reconstructed after the primitive strip data is read.
	 *
	 * @param kryo retained for interface compatibility
	 * @param input the cache input stream
	 * @return {@code true} if successful
	 */
	@Override
	public boolean readGeometry(Kryo kryo, Input input) {
		try {
			int layerCount = input.readInt();

			if (layerCount != LAYER_COUNT) {
				System.err.printf("BSTGeometry: expected %d layers, found %d in cache.%n", LAYER_COUNT, layerCount);
				return false;
			}

			HashMap<String, Line3d> strips = new HashMap<>();

			for (int layer = 0; layer < LAYER_COUNT; layer++) {
				int sectorCount = input.readInt();

				if (sectorCount != sectorsPerLayer[layer]) {
					System.err.printf("BSTGeometry: expected %d sectors for layer %d, found %d in cache.%n",
							sectorsPerLayer[layer], layer, sectorCount);
					return false;
				}

				for (int sector = 0; sector < sectorCount; sector++) {
					int stripCount = input.readInt();

					if (stripCount != STRIP_COUNT) {
						System.err.printf(
								"BSTGeometry: expected %d strips for layer %d sector %d, found %d in cache.%n",
								STRIP_COUNT, layer, sector, stripCount);
						return false;
					}

					for (int strip = 0; strip < STRIP_COUNT; strip++) {
						boolean hasStrip = input.readBoolean();

						if (!hasStrip) {
							System.err.printf("BSTGeometry: missing strip data for layer %d sector %d strip %d.%n",
									layer, sector, strip);
							return false;
						}

						Line3d line = readLine(input, layer, sector, strip);
						strips.put(hashKey(layer, sector, strip), line);
					}
				}
			}

			_strips = strips;
			getBSTPanels();

			return true;
		} catch (Exception e) {
			System.err.println("BSTGeometry: Error reading geometry cache: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Write BST geometry to the cache.
	 * <p>
	 * The cache stores explicit primitive strip endpoint data. The panel list is not
	 * written because it is derived from the strip data.
	 *
	 * @param kryo retained for interface compatibility
	 * @param output the cache output stream
	 * @return {@code true} if successful
	 */
	@Override
	public boolean writeGeometry(Kryo kryo, Output output) {
		try {
			output.writeInt(LAYER_COUNT);

			for (int layer = 0; layer < LAYER_COUNT; layer++) {
				int sectorCount = sectorsPerLayer[layer];
				output.writeInt(sectorCount);

				for (int sector = 0; sector < sectorCount; sector++) {
					output.writeInt(STRIP_COUNT);

					for (int strip = 0; strip < STRIP_COUNT; strip++) {
						Line3d line = getStrip(sector, layer, strip);
						boolean hasStrip = (line != null);

						output.writeBoolean(hasStrip);

						if (!hasStrip) {
							System.err.printf("BSTGeometry: missing strip data for layer %d sector %d strip %d.%n",
									layer, sector, strip);
							return false;
						}

						writeLine(output, line);
					}
				}
			}

			return true;
		} catch (Exception e) {
			System.err.println("BSTGeometry: Error writing geometry cache: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Write a strip line to the cache.
	 *
	 * @param output the cache output stream
	 * @param line   the strip line
	 */
	private static void writeLine(Output output, Line3d line) {
		output.writeInt(LINE_ENDPOINT_COUNT);

		writeVector(output, line.origin());
		writeVector(output, line.end());
	}

	/**
	 * Read a strip line from the cache.
	 *
	 * @param input the cache input stream
	 * @param layer 0-based layer, used for diagnostics
	 * @param sector 0-based sector, used for diagnostics
	 * @param strip 0-based strip, used for diagnostics
	 * @return the line
	 */
	private static Line3d readLine(Input input, int layer, int sector, int strip) {
		int endpointCount = input.readInt();

		if (endpointCount != LINE_ENDPOINT_COUNT) {
			throw new IllegalArgumentException(String.format(
					"BSTGeometry: expected %d endpoints for layer %d sector %d strip %d, found %d.",
					LINE_ENDPOINT_COUNT, layer, sector, strip, endpointCount));
		}

		Vector3d origin = readVector(input, layer, sector, strip, 0);
		Vector3d end = readVector(input, layer, sector, strip, 1);

		return new Line3d(origin, end);
	}

	/**
	 * Write a vector to the cache.
	 *
	 * @param output the cache output stream
	 * @param vector the vector
	 */
	private static void writeVector(Output output, Vector3d vector) {
		output.writeInt(COORD_COUNT);
		output.writeDouble(vector.x);
		output.writeDouble(vector.y);
		output.writeDouble(vector.z);
	}

	/**
	 * Read a vector from the cache.
	 *
	 * @param input the cache input stream
	 * @param layer 0-based layer, used for diagnostics
	 * @param sector 0-based sector, used for diagnostics
	 * @param strip 0-based strip, used for diagnostics
	 * @param endpoint endpoint index, used for diagnostics
	 * @return the vector
	 */
	private static Vector3d readVector(Input input, int layer, int sector, int strip, int endpoint) {
		int coordCount = input.readInt();

		if (coordCount != COORD_COUNT) {
			throw new IllegalArgumentException(String.format(
					"BSTGeometry: expected %d coordinates for layer %d sector %d strip %d endpoint %d, found %d.",
					COORD_COUNT, layer, sector, strip, endpoint, coordCount));
		}

		return new Vector3d(input.readDouble(), input.readDouble(), input.readDouble());
	}

	/**
	 * Check whether a strip address is valid.
	 *
	 * @param sector the 0-based sector
	 * @param layer  the 0-based layer
	 * @param strip  the 0-based strip
	 * @return {@code true} if valid
	 */
	private static boolean validStripAddress(int sector, int layer, int strip) {
		return (layer >= 0) && (layer < LAYER_COUNT)
				&& (sector >= 0) && (sector < sectorsPerLayer[layer])
				&& (strip >= 0) && (strip < STRIP_COUNT);
	}
}