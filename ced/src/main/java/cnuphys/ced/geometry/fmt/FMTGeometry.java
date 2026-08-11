package cnuphys.ced.geometry.fmt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;

import org.jlab.detector.base.GeometryFactory;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.detector.fmt.FMTDetector;
import org.jlab.geom.detector.fmt.FMTFactory;
import org.jlab.geom.detector.fmt.FMTLayer;
import org.jlab.geom.detector.fmt.FMTSector;
import org.jlab.geom.detector.fmt.FMTSuperlayer;
import org.jlab.geom.prim.Line3D;


import cnuphys.ced.frame.Ced;
import cnuphys.ced.geometry.cache.ACachedGeometry;

public class FMTGeometry extends ACachedGeometry {

	// the name of the detector
	public static String NAME = "FMT";

	// the layer objects used for FMT geometry and drawing
	private static HashMap<String, FMTLayerData> _fmtLayers = new HashMap<>();

	public FMTGeometry() {
		super("FMT");
	}

	/**
	 * Init the Alert geometry
	 */
	@Override
	public void initializeUsingCCDB() {
		System.out.println("\n=======================================");
		System.out.println("===  " + NAME + " Geometry Initialization ===");
		System.out.println("=======================================");

		String variation = Ced.getGeometryVariation();
		ConstantProvider constantProvider = GeometryFactory.getConstants(org.jlab.detector.base.DetectorType.FMT, 11,
				variation);

		initialize(constantProvider);
	}

	// init the time of flight
	private static void initialize(ConstantProvider cp) {
		_fmtLayers = new HashMap<>();

		FMTFactory fmtFactory = new FMTFactory();
		FMTDetector fmtDetector = fmtFactory.createDetectorCLAS(cp);

		int numsect = fmtDetector.getNumSectors();

		for (int sect = 0; sect < numsect; sect++) {

			FMTSector fmtSector = fmtFactory.createSector(cp, sect);
			int numsupl = fmtSector.getNumSuperlayers();

			for (int superlayer = 0; superlayer < numsupl; superlayer++) {

				FMTSuperlayer fmtSuperlayer = fmtFactory.createSuperlayer(cp, sect, superlayer);
				int numlay = fmtSuperlayer.getNumLayers();

				for (int layer = 0; layer < numlay; layer++) {
					FMTLayer fmtLayer = fmtFactory.createLayer(cp, sect, superlayer, layer);

					_fmtLayers.put(hash(sect, superlayer, layer), new FMTLayerData(fmtLayer));
				}
			}
		}

	}

	public static Line3D getStripLine(int sector, int superlayer, int layer, int strip) {
		FMTLayerData fmtLayer = _fmtLayers.get(hash(sector, superlayer, layer));
		if (fmtLayer != null && strip >= 0 && strip < fmtLayer.stripCount()) {
			return fmtLayer.stripLine(strip);
		}
		return null;
	}

	// all 0 based
	private static String hash(int sector, int superlayer, int layer) {
		return String.format("%d|%d|%d", sector, superlayer, layer);
	}

	/**
	 * Get all the fmt layers
	 *
	 * @return the collection of FMT layers
	 */
	static Collection<FMTLayerData> getAllFMTLayers() {
		return _fmtLayers.values();
	}

	/**
	 * Used by the 3D drawing
	 *
	 * @param sector     the 0-based sector 0..0
	 * @param superlayer the 0-based layer 0..0
	 * @param layer      the 0-based layer 0..5
	 * @param stripId    the 0-based paddle 0..1023
	 * @param coords     holds 8*3 = 24 values [x1, y1, z1, ..., x8, y8, z8]
	 */
	public static void stripVertices(int sector, int superlayer, int layer, int stripId, float[] coords) {

		FMTLayerData fmtLayer = _fmtLayers.get(hash(sector, superlayer, layer));
		fmtLayer.copyStripVertices(stripId, coords);
	}

	@Override
	public boolean supportsCache() {
		return true;
	}

	@Override
	public void writeGeometry(DataOutput output) throws IOException {
		ArrayList<FMTLayerData> layers = new ArrayList<>(_fmtLayers.values());
		layers.sort(Comparator.comparingInt((FMTLayerData value) -> value.sector)
				.thenComparingInt(value -> value.superlayer).thenComparingInt(value -> value.layer));
		output.writeInt(layers.size());
		for (FMTLayerData layer : layers) {
			layer.writeToCache(output);
		}
	}

	@Override
	public void readGeometry(DataInput input) throws IOException {
		int count = input.readInt();
		if (count < 1 || count > 100) {
			throw new IOException("Invalid FMT layer count: " + count);
		}
		HashMap<String, FMTLayerData> layers = new HashMap<>();
		for (int index = 0; index < count; index++) {
			FMTLayerData layer = FMTLayerData.readFromCache(input);
			layers.put(hash(layer.sector, layer.superlayer, layer.layer), layer);
		}
		_fmtLayers = layers;
	}

	/**
	 * Get the 1-based region 1..4
	 *
	 * @param strip1 the 1-based strip 1..1024
	 * @return the region 1..4
	 */
	public static int getRegion(int strip1) {
		int i = strip1 - 1;
		int region = 0;
		if (i >= 0 && i < 320) {
			region = 1;
		}
		if (i >= 320 && i < 512) {
			region = 2;
		}
		if (i >= 512 && i < 832) {
			region = 3;
		}
		if (i >= 832 && i < 1024) {
			region = 4;
		}

		return region;
	}

}
