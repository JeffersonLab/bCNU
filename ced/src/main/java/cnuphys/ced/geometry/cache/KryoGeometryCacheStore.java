package cnuphys.ced.geometry.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.objenesis.strategy.StdInstantiatorStrategy;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;

/**
 * Kryo-backed implementation of the geometry cache store.
 * <p>
 * This class intentionally contains the Kryo-specific details that used to
 * live in {@link GeometryCache}: Kryo creation, class registration, stream
 * handling, and cache-file read/write mechanics.
 * <p>
 * It is a transitional class. Once CED geometry caching is moved to a stable,
 * explicit format, this class can be replaced by another store implementation.
 */
public class KryoGeometryCacheStore implements GeometryCacheStore {
	
	// File-level cache header. This is separate from detector geometry versioning.
	private static final String CACHE_MAGIC = "CED_GEOMETRY_CACHE";
	private static final int CACHE_FORMAT_VERSION = 12;

	/**
	 * Registers all complex classes and custom types used by the geometry cache
	 * with the provided Kryo instance.
	 *
	 * @param kryo the Kryo instance to configure
	 */
	private static void registerClasses(Kryo kryo) {
		kryo.register(cnuphys.ced.geometry.cache.GeometryCacheMetadata.class);
		kryo.register(cnuphys.ced.ced3d.util.Point.class);
		kryo.register(cnuphys.ced.geometry.urwt.UrWTDetectorData.class);
		kryo.register(org.jlab.geom.component.ScintillatorPaddle.class);
		kryo.register(org.jlab.geom.prim.Vector3D.class);
		kryo.register(org.jlab.geom.prim.Line3D.class);
		kryo.register(org.jlab.geom.prim.Line3D[].class);
		kryo.register(org.jlab.geometry.prim.Line3d.class);
		kryo.register(org.jlab.geom.prim.Point3D.class);
		kryo.register(java.util.ArrayList.class);
		kryo.register(org.jlab.geom.prim.Shape3D.class);
		kryo.register(org.jlab.geom.prim.Triangle3D.class);
		kryo.register(java.awt.Point.class);
		kryo.register(org.jlab.geom.detector.ftof.FTOFSuperlayer.class);
		kryo.register(org.jlab.geom.detector.ftof.FTOFLayer.class);
		kryo.register(org.jlab.geom.DetectorId.class);

		try {
			Class<?> unmodListClass = Class.forName("java.util.Collections$UnmodifiableRandomAccessList");
			kryo.register(unmodListClass, new UnmodifiableCollectionsSerializer());
		} catch (ClassNotFoundException e) {
			System.err.println("Could not register unmodifiable list class: " + e.getMessage());
		}

		kryo.register(java.util.HashMap.class);
		kryo.register(org.jlab.geom.prim.Plane3D.class);
		kryo.register(org.jlab.geom.prim.Transformation3D.class);
		kryo.register(org.jlab.geom.prim.Transformation3D.TranslationXYZ.class);
		kryo.register(org.jlab.geom.prim.Transformation3D.RotationY.class);
		kryo.register(org.jlab.geom.prim.Transformation3D.RotationZ.class);
		kryo.register(org.jlab.geom.detector.ec.ECLayer.class);
		kryo.register(org.jlab.geom.prim.Transformation3D.RotationX.class);
		kryo.register(cnuphys.ced.geometry.Transformations.class);
		kryo.register(cnuphys.ced.geometry.DetectorType.class);
		kryo.register(cnuphys.ced.geometry.BSTxyPanel.class);
		kryo.register(boolean[].class);
		kryo.register(java.awt.geom.Line2D.Double.class);
		kryo.register(eu.mihosoft.vrl.v3d.Vector3d.class);
		kryo.register(cnuphys.ced.geometry.bmt.Geometry.class);
		kryo.register(double[].class);
		kryo.register(int[].class);
		kryo.register(double[][].class);
		kryo.register(int[][].class);
		kryo.register(org.jlab.geom.component.DriftChamberWire.class);
		kryo.register(org.jlab.geom.detector.fmt.FMTLayer.class);
		kryo.register(org.jlab.geom.prim.Sector3D.class);
		kryo.register(org.jlab.geom.prim.Arc3D.class);
		kryo.register(org.jlab.geom.component.TrackerStrip.class);
		kryo.register(cnuphys.ced.geometry.alert.DCLayer.class);
		kryo.register(cnuphys.ced.geometry.alert.TOFLayer.class);
		kryo.register(java.awt.geom.Rectangle2D.Double[].class);
		kryo.register(java.awt.geom.Rectangle2D.Double.class);
		kryo.register(java.awt.geom.Point2D.Double[].class);
		kryo.register(java.awt.geom.Point2D.Double[][].class);
		kryo.register(java.awt.geom.Point2D.Double.class);
	}

	/**
	 * Creates and configures a new Kryo instance.
	 *
	 * @return a configured Kryo instance
	 */
	private static Kryo getKryo() {
		Kryo kryo = new Kryo();
		kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
		registerClasses(kryo);
		return kryo;
	}

	/**
	 * Reads all registered geometries from the given cache file.
	 *
	 * @param file       the cache file
	 * @param geometries the registered geometries
	 * @return {@code true} if all geometries were read successfully
	 */
	@Override
	public boolean readAll(File file, List<IGeometryCache> geometries) {
		if (!file.exists()) {
			System.err.println("Cache file does not exist: " + file.getAbsolutePath());
			return false;
		}

		Kryo kryo = getKryo();

		try (FileInputStream fis = new FileInputStream(file); Input input = new Input(fis)) {
			String magic = input.readString();
			if (!CACHE_MAGIC.equals(magic)) {
				System.err.println("Invalid geometry cache header: " + magic);
				return false;
			}

			int cacheFormatVersion = input.readInt();
			if (cacheFormatVersion != CACHE_FORMAT_VERSION) {
				System.err.println("Unsupported geometry cache format version: " + cacheFormatVersion);
				return false;
			}

			GeometryCacheMetadata metadata = kryo.readObject(input, GeometryCacheMetadata.class);
			System.out.println("Geometry cache metadata: " + metadata);

			for (IGeometryCache geometry : geometries) {
				boolean success = geometry.readGeometry(kryo, input);				if (!success) {
					System.err.println("Failed to read geometry from cache for " + geometry.getName());
					return false;
				}
			}
			return true;
		} catch (Exception e) {
			System.err.println("Error reading cache: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Writes all registered geometries to the given cache file.
	 *
	 * @param file       the cache file
	 * @param geometries the registered geometries
	 * @return {@code true} if all geometries were written successfully
	 */
	@Override
	public boolean writeAll(File file, List<IGeometryCache> geometries) {
		if (!prepareCacheFile(file)) {
			return false;
		}

		Kryo kryo = getKryo();

		try (FileOutputStream fos = new FileOutputStream(file); Output output = new Output(fos)) {
			output.writeString(CACHE_MAGIC);
			output.writeInt(CACHE_FORMAT_VERSION);

			GeometryCacheMetadata metadata = new GeometryCacheMetadata("kryo", CACHE_FORMAT_VERSION, "CED");
			kryo.writeObject(output, metadata);

			for (IGeometryCache geometry : geometries) {
				boolean success = geometry.writeGeometry(kryo, output);
				if (!success) {
					System.err.println("Failed to write geometry to cache for " + geometry.getName());
					deleteCacheFile(file);
					return false;
				}
			}

			output.flush();
			return true;
		} catch (IOException e) {
			System.err.println("Failed to write cache: " + e.getMessage());
			deleteCacheFile(file);
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Ensures the cache file can be written.
	 *
	 * @param file the cache file
	 * @return {@code true} if the file is ready to be written
	 */
	private boolean prepareCacheFile(File file) {
		if (file.exists()) {
			if (!file.delete()) {
				System.err.println("Unable to delete cache file: " + file.getAbsolutePath());
				return false;
			}
		} else {
			File parentDir = file.getParentFile();
			if ((parentDir != null) && !parentDir.exists() && !parentDir.mkdirs()) {
				System.err.println("Unable to create cache directory: " + parentDir.getAbsolutePath());
				return false;
			}
		}

		return true;
	}

	/**
	 * Deletes the cache file after a failed write.
	 *
	 * @param file the cache file
	 */
	private void deleteCacheFile(File file) {
		if (file.exists() && !file.delete()) {
			System.err.println("Unable to delete cache file: " + file.getAbsolutePath());
		}
	}
}