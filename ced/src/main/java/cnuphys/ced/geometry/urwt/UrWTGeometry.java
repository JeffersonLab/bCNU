package cnuphys.ced.geometry.urwt;

import java.util.ArrayList;
import java.util.List;

import org.jlab.detector.geant4.v2.MPGD.URWT.URWTStripFactory;
import org.jlab.geom.prim.Line3D;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import cnuphys.bCNU.util.UnicodeSupport;
import cnuphys.ced.ced3d.util.BoundingBox3D;
import cnuphys.ced.ced3d.util.DrawSupport;
import cnuphys.ced.ced3d.util.Plane;
import cnuphys.ced.ced3d.util.Point;
import cnuphys.ced.frame.Ced;
import cnuphys.ced.geometry.cache.ACachedGeometry;

/**
 * Geometry manager for the micro-resistive well tracker.
 * <p>
 * A UrWT detector panel is identified by a 1-based sector and layer:
 *
 * <pre>
 * sector: 1..6
 * layer:  1..4
 * </pre>
 *
 * This class owns the sector/layer table of {@link UrWTDetectorData}. The cache
 * representation is explicit primitive data written and read by
 * {@link UrWTDetectorData}; this avoids serializing UrWT detector data objects
 * directly through Kryo.
 *
 * <p>
 * 3/5/26: Chamber concept gone; now a given sector and layer make a detector.
 */
public class UrWTGeometry extends ACachedGeometry {

	/**
	 * Number of sectors.
	 */
	public static final int NUM_SECTORS = 6;

	/**
	 * Number of layers per sector.
	 */
	public static final int NUM_LAYERS = 4;

	/**
	 * Detector display name.
	 */
	public static final String NAME = UnicodeSupport.SMALL_MU + "rWT";

	/**
	 * Strip factory used only during direct CCDB initialization.
	 */
	private static URWTStripFactory factory;

	/**
	 * Detector data indexed as [sector - 1][layer - 1].
	 */
	private static UrWTDetectorData[][] detectorData = new UrWTDetectorData[NUM_SECTORS][NUM_LAYERS];

	/**
	 * Constructor.
	 */
	public UrWTGeometry() {
		super(NAME);
	}

	/**
	 * Get detector data for a sector and layer.
	 *
	 * @param sector the 1-based sector, 1..6
	 * @param layer  the 1-based layer, 1..4
	 * @return the detector data, or {@code null} if the address is invalid
	 */
	public static UrWTDetectorData getDetectorData(int sector, int layer) {
		if (!validSectorLayer(sector, layer)) {
			return null;
		}

		return detectorData[sector - 1][layer - 1];
	}

	/**
	 * Initialize geometry from CCDB/JLab geometry services.
	 */
	@Override
	public void initializeUsingCCDB() {
		System.out.println("\n=======================================");
		System.out.println("===  " + NAME + " Geometry Initialization ===");
		System.out.println("=======================================");

		String variationName = Ced.getGeometryVariation();

		factory = new URWTStripFactory(11, variationName);
		detectorData = new UrWTDetectorData[NUM_SECTORS][NUM_LAYERS];

		for (int sector = 1; sector <= NUM_SECTORS; sector++) {
			for (int layer = 1; layer <= NUM_LAYERS; layer++) {
				detectorData[sector - 1][layer - 1] = new UrWTDetectorData(factory, sector, layer);
				int stripcount = detectorData[sector - 1][layer - 1].strips.length;
				System.out.println("Sector " + sector + " Layer " + layer + " strip count = " + stripcount);
			}
		}

		for (int layer = 1; layer <= NUM_LAYERS; layer++) {
			Plane plane = DrawSupport.findCommonPlane(detectorData[0][layer - 1].strips, 1e-6);
			System.out.println("Layer " + layer + " plane: " + plane);
		}
	}

	/**
	 * Get a strip.
	 *
	 * @param sector the 1-based sector, 1..6
	 * @param layer  the 1-based layer, 1..4
	 * @param strip  the 1-based strip
	 * @return the strip, or {@code null} if unavailable
	 */
	public static Line3D getStrip(int sector, int layer, int strip) {
		UrWTDetectorData data = getDetectorData(sector, layer);

		if (data == null) {
			return null;
		}

		return data.getStrip(strip);
	}

	/**
	 * Read UrWT geometry from the cache.
	 * <p>
	 * This reads explicit primitive detector data rather than Kryo-serialized
	 * {@link UrWTDetectorData} objects.
	 *
	 * @param kryo  the Kryo instance, retained for interface compatibility
	 * @param input the cache input stream
	 * @return {@code true} if all UrWT geometry was read successfully
	 */
	@Override
	public boolean readGeometry(Kryo kryo, Input input) {
		try {
			int numSectors = input.readInt();
			int numLayers = input.readInt();

			if ((numSectors != NUM_SECTORS) || (numLayers != NUM_LAYERS)) {
				System.err.println("UrWTGeometry: cache sector/layer count mismatch: expected " + NUM_SECTORS
						+ " sectors and " + NUM_LAYERS + " layers, but got " + numSectors + " sectors and "
						+ numLayers + " layers");
				return false;
			}

			UrWTDetectorData[][] data = new UrWTDetectorData[NUM_SECTORS][NUM_LAYERS];

			for (int sector = 1; sector <= NUM_SECTORS; sector++) {
				for (int layer = 1; layer <= NUM_LAYERS; layer++) {
					boolean hasData = input.readBoolean();

					if (!hasData) {
						System.err.println("UrWTGeometry: missing detector data for sector " + sector + " layer "
								+ layer);
						return false;
					}

					UrWTDetectorData detector = UrWTDetectorData.readFromCache(input);

					if ((detector.sector != sector) || (detector.layer != layer)) {
						System.err.println("UrWTGeometry: detector address mismatch while reading cache. Expected sector "
								+ sector + " layer " + layer + ", found sector " + detector.sector + " layer "
								+ detector.layer);
						return false;
					}

					data[sector - 1][layer - 1] = detector;
				}
			}

			detectorData = data;

			// The strip factory is only needed for CCDB initialization.
			factory = null;

			return true;
		} catch (Exception e) {
			System.err.println("UrWTGeometry: Error reading geometry cache: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Write UrWT geometry to the cache.
	 * <p>
	 * This writes explicit primitive detector data rather than Kryo-serialized
	 * {@link UrWTDetectorData} objects.
	 *
	 * @param kryo   the Kryo instance, retained for interface compatibility
	 * @param output the cache output stream
	 * @return {@code true} if all UrWT geometry was written successfully
	 */
	@Override
	public boolean writeGeometry(Kryo kryo, Output output) {

		try {
			output.writeInt(NUM_SECTORS);
			output.writeInt(NUM_LAYERS);

			for (int sector = 1; sector <= NUM_SECTORS; sector++) {
				for (int layer = 1; layer <= NUM_LAYERS; layer++) {
					UrWTDetectorData data = detectorData[sector - 1][layer - 1];

					boolean hasData = (data != null);
					output.writeBoolean(hasData);

					if (!hasData) {
						System.err.println("UrWTGeometry: missing detector data for sector " + sector + " layer "
								+ layer);
						return false;
					}

					data.writeToCache(output);
				}
			}

			return true;
		} catch (Exception e) {
			System.err.println("UrWTGeometry: Error writing geometry cache: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Check whether a sector/layer address is valid.
	 *
	 * @param sector the 1-based sector
	 * @param layer  the 1-based layer
	 * @return {@code true} if the address is valid
	 */
	private static boolean validSectorLayer(int sector, int layer) {
		return (sector >= 1) && (sector <= NUM_SECTORS) && (layer >= 1) && (layer <= NUM_LAYERS);
	}

	/**
	 * Simple standalone geometry test.
	 *
	 * @param args command-line arguments
	 */
	public static void main(String args[]) {
		UrWTGeometry geometry = new UrWTGeometry();
		geometry.initializeUsingCCDB();

		List<Line3D[]> allLines = new ArrayList<>();
		for (int sector = 1; sector <= NUM_SECTORS; sector++) {
			for (int layer = 1; layer <= NUM_LAYERS; layer++) {
				allLines.add(UrWTGeometry.detectorData[sector - 1][layer - 1].strips);
			}
		}

		for (int sector = 1; sector <= NUM_SECTORS; sector++) {
			for (int layer = 1; layer <= NUM_LAYERS; layer++) {
				Point centroid = UrWTGeometry.detectorData[sector - 1][layer - 1].getCentroid();
				System.out.println("Sector " + sector + " Layer " + layer + " Centroid: " + centroid);
			}
		}

		BoundingBox3D box = DrawSupport.getBoundingBox(allLines);
		System.out.println("Bounding Box: " + box);
	}
}