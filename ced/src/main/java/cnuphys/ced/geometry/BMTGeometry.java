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

/**
 * Geometry-cache wrapper for the BMT geometry.
 * <p>
 * The {@link Geometry} object is stateless; it computes from the static
 * {@link Constants} data. Therefore the cache stores only the explicit primitive
 * constants. After a cache read, a new {@link Geometry} instance is created.
 */
public class BMTGeometry extends ACachedGeometry {

	/** Stateless BMT geometry helper. */
	private static Geometry _geometry;

	/**
	 * Constructor.
	 */
	public BMTGeometry() {
		super("BMTGeometry");
	}

	/**
	 * Initialize the BMT geometry from CCDB/JLab constants.
	 */
	@Override
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

	/**
	 * Get the BMT geometry helper.
	 *
	 * @return the BMT geometry helper
	 */
	public static Geometry getGeometry() {
		return _geometry;
	}

	/**
	 * Read BMT constants from the cache and reconstruct the stateless geometry
	 * helper.
	 *
	 * @param kryo  retained for interface compatibility
	 * @param input the cache input stream
	 * @return {@code true} if successful
	 */
	@Override
	public boolean readGeometry(Kryo kryo, Input input) {
		try {
			Constants.readConstants(input);
			_geometry = new Geometry();
			return true;
		} catch (Exception e) {
			System.err.println("BMTGeometry: Error reading geometry cache: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Write BMT constants to the cache as explicit primitive data.
	 *
	 * @param kryo   retained for interface compatibility
	 * @param output the cache output stream
	 * @return {@code true} if successful
	 */
	@Override
	public boolean writeGeometry(Kryo kryo, Output output) {
		try {
			Constants.writeConstants(output);
			return true;
		} catch (Exception e) {
			System.err.println("BMTGeometry: Error writing geometry cache: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
}