package cnuphys.ced.geometry;

import cnuphys.ced.frame.Ced;
import cnuphys.ced.geometry.bmt.Constants;
import cnuphys.ced.geometry.bmt.ConstantsLoader;
import cnuphys.ced.geometry.bmt.ConstantsLoaderVZ;
import cnuphys.ced.geometry.bmt.Geometry;
import cnuphys.ced.geometry.cache.ACachedGeometry;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

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

	@Override
	public boolean readGeometry(Kryo kryo, Input input) {
		try {
			_geometry = kryo.readObjectOrNull(input, Geometry.class);
			// Restore the Constants state.
			Constants.readConstants(kryo, input);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public boolean writeGeometry(Kryo kryo, Output output) {
		try {
			kryo.writeObjectOrNull(output, _geometry, Geometry.class);
			// Save the loaded state of Constants.
			Constants.writeConstants(kryo, output);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
