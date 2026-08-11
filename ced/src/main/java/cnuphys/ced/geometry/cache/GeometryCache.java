package cnuphys.ced.geometry.cache;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import cnuphys.ced.geometry.BMTGeometry;
import cnuphys.ced.geometry.BSTGeometry;
import cnuphys.ced.geometry.CNDGeometry;
import cnuphys.ced.geometry.CTOFGeometry;
import cnuphys.ced.geometry.DCGeometry;
import cnuphys.ced.geometry.ECGeometry;
import cnuphys.ced.geometry.FTCALGeometry;
import cnuphys.ced.geometry.PCALGeometry;
import cnuphys.ced.geometry.alert.AlertGeometry;
import cnuphys.ced.geometry.fmt.FMTGeometry;
import cnuphys.ced.geometry.ftof.FTOFGeometry;
import cnuphys.ced.geometry.urwt.UrWTGeometry;
import cnuphys.ced.frame.Ced;

/**
 * Coordinates initialization of detector geometry from authoritative sources.
 *
 * <p>The historical class name is retained temporarily to minimize churn at
 * call sites while the former Kryo cache is replaced.</p>
 */
public final class GeometryCache {

	private static final List<IGeometryCache> GEOMETRIES = new ArrayList<>();
	private static final String CACHE_DIRECTORY = ".ced";
	private static final String CACHE_FILE = "geometry-cache.sqlite";

	private GeometryCache() {
	}

	public static void addGeometry(IGeometryCache geometry) {
		GEOMETRIES.remove(geometry);
		GEOMETRIES.add(geometry);
	}

	/** Initializes every detector geometry from SQLite when available, otherwise from CCDB. */
	public static void initializeAllGeometry() {
		GEOMETRIES.clear();
		new AlertGeometry();
		new DCGeometry();
		new UrWTGeometry();
		new CTOFGeometry();
		new FTCALGeometry();
		new CNDGeometry();
		new FTOFGeometry();
		new PCALGeometry();
		new ECGeometry();
		new BSTGeometry();
		new BMTGeometry();
		new FMTGeometry();

		try (SQLiteGeometryCache cache = new SQLiteGeometryCache(
				getCachePath(), Ced.release, Ced.getGeometryVariation())) {
			cache.open();
			for (IGeometryCache geometry : GEOMETRIES) {
				if (cache.read(geometry)) {
					System.out.println("Loaded " + displayName(geometry.getName()) + " geometry from cache.");
				} else {
					geometry.initializeUsingCCDB();
					cache.write(geometry);
				}
			}
		} catch (IOException | SQLException e) {
			System.err.println("Geometry cache unavailable; initializing from CCDB: " + e.getMessage());
			for (IGeometryCache geometry : GEOMETRIES) {
				geometry.initializeUsingCCDB();
			}
		}
	}

	static String displayName(String detectorName) {
		return detectorName.replaceFirst("\\s*Geometry$", "");
	}

	/** Location of the per-user SQLite geometry cache. */
	public static Path getCachePath() {
		return Path.of(System.getProperty("user.home"), CACHE_DIRECTORY, CACHE_FILE);
	}

	/**
	 * Delete the cache. Current in-memory geometry remains valid; the next CED run
	 * recreates the database from authoritative sources.
	 */
	public static boolean deleteCache() throws IOException {
		Path path = getCachePath();
		boolean deleted = Files.deleteIfExists(path);
		Files.deleteIfExists(Path.of(path + "-wal"));
		Files.deleteIfExists(Path.of(path + "-shm"));
		return deleted;
	}
}
