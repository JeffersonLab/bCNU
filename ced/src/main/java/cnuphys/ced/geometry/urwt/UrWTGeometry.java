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
 * Geometric data for the uRwell detector
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
	public static String NAME = UnicodeSupport.SMALL_MU + "rWT";


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
		return detectorData[sector - 1][layer - 1];
	}


	/**
	 * Initialize the geometry using the CCDB. This is called by the geometry cache when
	 * 
	 */
	@Override
	public void initializeUsingCCDB() {
		System.out.println("\n=======================================");
		System.out.println("===  " + NAME + " Geometry Initialization ===");
		System.out.println("=======================================");

		String variationName = Ced.getGeometryVariation();
		
		factory = new URWTStripFactory(11, variationName);
				
		for (int sector = 1; sector <= NUM_SECTORS; sector++) {
			for (int layer = 1; layer <= NUM_LAYERS; layer++) {
				detectorData[sector - 1][layer - 1] = new UrWTDetectorData(factory, sector, layer);
			}
		} // sector loop


		for (int layer = 1; layer <= NUM_LAYERS; layer++) {
			Plane plane = DrawSupport.findCommonPlane(detectorData[0][layer - 1].strips, 1e-6);
			System.out.println("Layer " + layer + " plane: " + plane);
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
		return detectorData[sector - 1][layer - 1].strips[strip - 1];
	}

	@Override
	public boolean readGeometry(Kryo kryo, Input input) {
		try {

			// Read the number of sectors and layers
			int numSectors = input.readInt();
			int numLayers = input.readInt();

			if (numSectors != NUM_SECTORS || numLayers != NUM_LAYERS) {
				System.err.println("Cache geometry sector/layer count mismatch: expected " + NUM_SECTORS + " sectors and " + NUM_LAYERS + " layers, but got " + numSectors + " sectors and " + numLayers + " layers");
				return false;
			}

			// Read the detector data for each sector and layer
			for (int sector = 1; sector <= NUM_SECTORS; sector++) {
				for (int layer = 1; layer <= NUM_LAYERS; layer++) {
					detectorData[sector - 1][layer - 1] = kryo.readObjectOrNull(input, UrWTDetectorData.class);
				}
			}

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean writeGeometry(Kryo kryo, Output output) {
		
		try {

			// Write the number of sectors and layers
			output.writeInt(NUM_SECTORS);
			output.writeInt(NUM_LAYERS);

			// Write the detector data for each sector and layer
			for (int sector = 1; sector <= NUM_SECTORS; sector++) {
				for (int layer = 1; layer <= NUM_LAYERS; layer++) {
					UrWTDetectorData data = detectorData[sector - 1][layer - 1];
					kryo.writeObjectOrNull(output, data, UrWTDetectorData.class);
				}
			}

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	public static void main(String args[]) {
		UrWTGeometry geometry = new UrWTGeometry();
		geometry.initializeUsingCCDB();

		List<Line3D[]> allLines = new ArrayList<>();
		for (int sector = 1; sector <= NUM_SECTORS; sector++) {
			for (int layer = 1; layer <= NUM_LAYERS; layer++) {
				allLines.add(UrWTGeometry.detectorData[sector - 1][layer - 1].strips);
			}
		}
		
		//print all the centriods
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
