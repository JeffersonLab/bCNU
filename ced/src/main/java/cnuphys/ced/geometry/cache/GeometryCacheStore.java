package cnuphys.ced.geometry.cache;

import java.io.File;
import java.util.List;

/**
 * Storage backend for cached CED geometry.
 * <p>
 * This interface intentionally does not expose Kryo, SQLite, or any other
 * concrete storage technology. GeometryCache uses this interface to read and
 * write the registered geometry objects.
 */
public interface GeometryCacheStore {

	/**
	 * Read all registered geometries from the given cache file.
	 *
	 * @param file       the cache file
	 * @param geometries the registered geometry objects
	 * @return {@code true} if all geometries were read successfully
	 */
	boolean readAll(File file, List<IGeometryCache> geometries);

	/**
	 * Write all registered geometries to the given cache file.
	 *
	 * @param file       the cache file
	 * @param geometries the registered geometry objects
	 * @return {@code true} if all geometries were written successfully
	 */
	boolean writeAll(File file, List<IGeometryCache> geometries);
}