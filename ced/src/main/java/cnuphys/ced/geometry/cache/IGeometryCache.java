package cnuphys.ced.geometry.cache;

/** A detector geometry that can initialize itself from its authoritative source. */
public interface IGeometryCache {

	String getName();

	void initializeUsingCCDB();
}
