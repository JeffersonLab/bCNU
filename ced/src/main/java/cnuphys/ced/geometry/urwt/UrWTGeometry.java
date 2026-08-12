package cnuphys.ced.geometry.urwt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.jlab.detector.geant4.v2.MPGD.URWT.URWTStripFactory;
import org.jlab.geom.prim.Line3D;


import edu.cnu.mdi.util.UnicodeUtils;
import cnuphys.ced.frame.Ced;
import cnuphys.ced.geometry.cache.ACachedGeometry;

/**
 * Geometric data for the UrWT detector
 * 
 * 3/5/26 BIG CHANGE Chamber concept gone, now a given sector and layer make "detector"
  * 
 * @author heddle
 *
 */
public class UrWTGeometry extends ACachedGeometry {
	
	/** the number of sectors */
	public static final int NUM_SECTORS = 6;
	
	/** the number of layers per sector */
	public static final int NUM_LAYERS = 4;

	// the name of the detector
	public static String NAME = UnicodeUtils.SMALL_MU + "rWT";


	// the strip factory
	private static  URWTStripFactory factory;
	
	//  the detectors
	private static UrWTDetectorData[][] detectorData = new UrWTDetectorData[NUM_SECTORS][NUM_LAYERS];

	/**
	 * Constructor
	 */
	public UrWTGeometry() {
		super(NAME);
	}
	
	/**
	 * Get the detector data for a given sector and layer
	 * @param sector the 1-based sector [1..6]
	 * @param layer the 1-based layer [1..4]
	 * @return the detector data
	 */
	public static UrWTDetectorData getDetectorData(int sector, int layer) {
		if (sector < 1 || sector > NUM_SECTORS || layer < 1 || layer > NUM_LAYERS) {
			return null;
		}
		return detectorData[sector - 1][layer - 1];
	}


	/**
	 * Initialize the geometry using the CCDB. This is called by the geometry cache when
	 * 
	 */
	@Override
	public void initializeUsingCCDB() {
		String variationName = Ced.getGeometryVariation();
		
		factory = new URWTStripFactory(11, variationName);
		detectorData = new UrWTDetectorData[NUM_SECTORS][NUM_LAYERS];
				
		for (int sector = 1; sector <= NUM_SECTORS; sector++) {
			for (int layer = 1; layer <= NUM_LAYERS; layer++) {
				detectorData[sector - 1][layer - 1] = new UrWTDetectorData(factory, sector, layer);
			}
		} // sector loop
	}

	@Override
	public boolean supportsCache() {
		return true;
	}

	@Override
	public void readGeometry(DataInput input) throws IOException {
		int sectors = input.readInt();
		int layers = input.readInt();
		if (sectors != NUM_SECTORS || layers != NUM_LAYERS) {
			throw new IOException("Unexpected URWT dimensions: " + sectors + " x " + layers);
		}

		UrWTDetectorData[][] restored = new UrWTDetectorData[NUM_SECTORS][NUM_LAYERS];
		for (int sector = 1; sector <= NUM_SECTORS; sector++) {
			for (int layer = 1; layer <= NUM_LAYERS; layer++) {
				UrWTDetectorData data = UrWTDetectorData.readFromCache(input);
				if (data.sector != sector || data.layer != layer) {
					throw new IOException("Unexpected cached URWT detector address");
				}
				restored[sector - 1][layer - 1] = data;
			}
		}
		detectorData = restored;
		factory = null;
	}

	@Override
	public void writeGeometry(DataOutput output) throws IOException {
		output.writeInt(NUM_SECTORS);
		output.writeInt(NUM_LAYERS);
		for (int sector = 1; sector <= NUM_SECTORS; sector++) {
			for (int layer = 1; layer <= NUM_LAYERS; layer++) {
				UrWTDetectorData data = detectorData[sector - 1][layer - 1];
				if (data == null) {
					throw new IOException("Missing URWT detector " + sector + "/" + layer);
				}
				data.writeToCache(output);
			}
		}
	}
	

	/**
	 * Get a strip
	 * 
	 * @param sector       the 1-based sector [1..6]
	 * @param layer        the 1-based layer [1..2]
	 * @param strip        the 1-based strip
	 * @return the strip
	 */
	public static Line3D getStrip(int sector, int layer, int strip) {
		UrWTDetectorData data = getDetectorData(sector, layer);
		return (data == null) ? null : data.getStrip(strip);
	}

}
