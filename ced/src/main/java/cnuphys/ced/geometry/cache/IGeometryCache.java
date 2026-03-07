package cnuphys.ced.geometry.cache;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public interface IGeometryCache {
	
	public boolean readGeometry(Kryo kryo, Input input);
	
	public boolean writeGeometry(Kryo kryo, Output output);
	
	public String getName();
	
	public void initializeUsingCCDB();

}
