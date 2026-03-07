package cnuphys.ced.geometry.cache;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import cnuphys.ced.frame.Ced;

/**
 * This geometry item is used to store the version string of the geometry in the cache. When reading from the cache, it checks if the version string matches the current version. If not, it indicates that the cache is invalid and should be rebuilt.
 * 
 * This is a simple way to ensure that we don't use outdated cached geometry data when the geometry has changed.
 * 
 * 6/5/24 - created
 * @author heddle
 *
 */
public class GeometryVersion extends ACachedGeometry {
	
	public GeometryVersion() {
		super("GeometryVersion");
	}

	@Override
	public boolean readGeometry(Kryo kryo, Input input) {
		
		
		if (Ced.forVeronique()) {
			return false; // no cache geo for Veronique
		}
		
		// Read version string
		String cachedVersion = kryo.readObject(input, String.class);
		if (!cachedVersion.equals(versionString())) {
			System.err.println(
					"Cache version mismatch: cached=" + cachedVersion + ", current=" + versionString());
			return false;
		}
		return true;
	}

	@Override
	public boolean writeGeometry(Kryo kryo, Output output) {
		
		if (Ced.forVeronique()) {
			return false; // no cache geo for Veronique
		}
		
		kryo.writeObject(output, versionString());
		return true;
	}

	@Override
	public void initializeUsingCCDB() {
		//do nothing
	}
	
	// use the ced version as the version string
	private static String versionString() {
		return Ced.release;
	}


}
