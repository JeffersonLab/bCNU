package cnuphys.ced.geometry.cache;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cnuphys.ced.geometry.BMTGeometry;
import cnuphys.ced.geometry.BSTGeometry;
import cnuphys.ced.geometry.CNDGeometry;
import cnuphys.ced.geometry.CTOFGeometry;
import cnuphys.ced.geometry.DCGeometry;
import cnuphys.ced.geometry.ECGeometry;
import cnuphys.ced.geometry.FTCALGeometry;
import cnuphys.ced.geometry.HTCCGeometry;
import cnuphys.ced.geometry.LTCCGeometry;
import cnuphys.ced.geometry.PCALGeometry;
import cnuphys.ced.geometry.alert.AlertGeometry;
import cnuphys.ced.geometry.fmt.FMTGeometry;
import cnuphys.ced.geometry.ftof.FTOFGeometry;
import cnuphys.ced.geometry.urwt.UrWTGeometry;

public class GeometryCache {

	// the cache file name
	private static final String CACHE_FILE_NAME = ".cedGeometry.cache";

	// registered geometries
	private static final ArrayList<IGeometryCache> _geometries = new ArrayList<>();

	// current cache store implementation
	private static final GeometryCacheStore _cacheStore = new KryoGeometryCacheStore();
	/**
	 * Add a geometry to the list of objects to cache.
	 *
	 * @param geometry the geometry object to add
	 */
	public static void addGeometry(IGeometryCache geometry) {
		_geometries.remove(geometry);
		_geometries.add(geometry);
	}

	/**
	 * Read all the geometry from the cache.
	 *
	 * @return true if the geometry was read successfully
	 */
	public static boolean readAllGeometries() {
		return _cacheStore.readAll(getCacheFile(), _geometries);
	}

	/**
	 * Write the geometry to the cache.
	 *
	 * @return true if the geometry was written successfully
	 */
	public static boolean writeAllGeometries() {
		return _cacheStore.writeAll(getCacheFile(), _geometries);
	}

	// get the cache file assumed in the current working directory
	private static File getCacheFile() {
		return new File(System.getProperty("user.home"), CACHE_FILE_NAME);
	}

	/**
	 * Initialize all geometries. This will first try to read from the cache. If
	 * that fails, it will initialize using CCDB and then write to the cache.
	 */
	public static void initializeAllGeometry() {
		createGeometries();

		boolean readSuccess = readAllGeometries();
		if (readSuccess) {
			System.out.println("Successfully read geometry from cache.");
			return;
		}

		initializeAllUsingCCDB();

		boolean writeSuccess = writeAllGeometries();
		if (writeSuccess) {
			System.out.println("Successfully wrote geometry to cache.");
		} else {
			System.err.println("Failed to write geometry to cache.");
		}
	}

	/**
	 * Create the geometry objects. The constructors register the geometry objects
	 * with this cache through {@link #addGeometry(IGeometryCache)}.
	 */
	private static void createGeometries() {
		new AlertGeometry();
		new GeometryVersion();
		new DCGeometry();
		new UrWTGeometry();
		new CTOFGeometry();
		new FTCALGeometry();
		new CNDGeometry();
		new HTCCGeometry();
		new LTCCGeometry();
		new FTOFGeometry();
		new PCALGeometry();
		new ECGeometry();
		new BSTGeometry();
		new BMTGeometry();
		new FMTGeometry();
	}

	/**
	 * Initialize all registered geometries from CCDB.
	 */
	private static void initializeAllUsingCCDB() {
		for (IGeometryCache geometry : _geometries) {
			geometry.initializeUsingCCDB();
		}
	}

	/**
	 * Return the currently registered geometries.
	 * <p>
	 * This is package-visible to support future cache-store refactors and testing.
	 *
	 * @return the registered geometries
	 */
	static List<IGeometryCache> getGeometries() {
		return _geometries;
	}

	// main program for testing
	public static void main(String arg[]) {
		initializeAllGeometry();
	}
}