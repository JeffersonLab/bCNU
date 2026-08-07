package cnuphys.ced.geometry;

import cnuphys.ced.frame.Ced;
import cnuphys.ced.geometry.bmt.Constants;
import cnuphys.ced.geometry.bmt.ConstantsLoader;
import cnuphys.ced.geometry.bmt.ConstantsLoaderVZ;
import cnuphys.ced.geometry.bmt.Geometry;
import cnuphys.ced.geometry.cache.ACachedGeometry;


public class BMTGeometry extends ACachedGeometry {

	private static Geometry _geometry;

	public BMTGeometry() {
		super("BMTGeometry");
	}

	/**
	 * Initialize the BMT Geometry
	 */
	public void initializeUsingCCDB() {
		System.out.println("\n=====================================");
		System.out.println("===  BMT Geometry Initialization  ===");
		System.out.println("=====================================");

		if (Ced.forVeronique()) {
			ConstantsLoaderVZ.Load(11);
		} else {
			ConstantsLoader.Load(11);
		}
		Constants.Load();
		_geometry = new Geometry();

	}

	public static Geometry getGeometry() {
		return _geometry;
	}

}
