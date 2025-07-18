package cnuphys.ced.geometry.cache;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import cnuphys.ced.frame.Ced;

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
