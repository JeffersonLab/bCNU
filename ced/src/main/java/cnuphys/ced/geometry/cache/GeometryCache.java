package cnuphys.ced.geometry.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.objenesis.strategy.StdInstantiatorStrategy;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;

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
import cnuphys.ced.geometry.urwell.UrWELLGeometry;

public class GeometryCache {

	// the cache file name
	private static String _cacheFileName = ".cedGeometry.cache";

	private static ArrayList<IGeometryCache> _geometries = new ArrayList<>();

	/**
	 * Add a geometry to the the list of objects to cache
	 * 
	 * @param geometry the geometry object to add
	 */
	public static void addGeometry(IGeometryCache geometry) {
		_geometries.remove(geometry);
		_geometries.add(geometry);
	}

	/**
	 * Registers all complex classes and custom types used by the geometry cache
	 * with the provided Kryo instance.
	 * 
	 * <p>
	 * This method ensures that all the classes required for serializing and
	 * deserializing geometry-related objects (including detector components,
	 * geometry primitives, transformation objects, and common collections) are
	 * registered. It also registers a custom serializer for the unmodifiable list
	 * type returned by {@code Collections.unmodifiableRandomAccessList} (accessed
	 * via reflection) so that these special collection types can be correctly
	 * handled during serialization.
	 * 
	 * @param kryo the Kryo instance to be configured for caching geometry objects.
	 */
	private static void registerClasses(Kryo kryo) {
		kryo.register(cnuphys.ced.geometry.urwell.ChamberData.class);
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
		kryo.register(org.jlab.geom.prim.Triangle3D.class);
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
	 * Read all the geometry from the cache
	 * 
	 * @return true if the geometry was read successfully
	 */
	public static boolean readAllGeometries() {
		File file = getCacheFile();
		if (!file.exists()) {
			System.err.println("Cache file does not exist: " + file.getAbsolutePath());
			return false;
		}

		Kryo kryo = getKryo();
		FileInputStream fis = null;
		Input input = null;
		try {
			fis = new FileInputStream(file);
			input = new Input(fis);

			// loop through the geometries and read them
			for (IGeometryCache geometry : _geometries) {
				boolean success = geometry.readGeometry(kryo, input);
				if (!success) {
					System.err.println("Failed to read geometry from cache for " + geometry.getName());
					return false;
				}
			}
			return true;
		} catch (Exception e) {
			System.err.println("Error reading cache: " + e.getMessage());
			return false;
		} finally {
			if (input != null) {
				input.close();
			}
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					System.err.println("Error closing FileInputStream: " + e.getMessage());
				}
			}
		}
	}

	/**
	 * 
	 * Creates and configures a new Kryo instance for caching geometry objects.
	 * 
	 * <p>
	 * This method instantiates a new Kryo serializer and sets an instantiation
	 * strategy (using StdInstantiatorStrategy) to allow object creation without a
	 * no-argument constructor.
	 * 
	 * It then registers all complex geometry classes and any custom serializers
	 * needed by calling
	 * 
	 * {@code registerClasses(kryo)}.
	 * 
	 * @return a fully configured Kryo instance ready for serialization and
	 *         deserialization of geometry data.
	 */
	private static Kryo getKryo() {
		Kryo kryo = new Kryo();
		kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
		registerClasses(kryo);
		return kryo;
	}

	// delete the cache file if it exists. This is done if there is an error.
	private static void deleteCacheFile() {
		File file = getCacheFile();
		if (file.exists()) {
			if (!file.delete()) {
				System.err.println("Unable to delete cache file: " + file.getAbsolutePath());
			}
		}
	}

	/**
	 * Write the geometry to the cache
	 * 
	 * @return true if the geometry was written successfully
	 */
	public static boolean writeAllGeometries() {
		File file = getCacheFile();

		// delete the file if it exists
		if (file.exists()) {
			if (!file.delete()) {
				System.err.println("Unable to delete cache file: " + file.getAbsolutePath());
				return false;
			}
		} else {
			// create the parent directory if it doesn't exist
			File parentDir = file.getParentFile();
			if (!parentDir.exists() && !parentDir.mkdirs()) {
				System.err.println("Unable to create cache directory: " + parentDir.getAbsolutePath());
				return false;
			}
		}

		Kryo kryo = getKryo();
		FileOutputStream fos = null;
		Output output = null;
		try {
			fos = new FileOutputStream(file);
			output = new Output(fos);
			// loop through the geometries and write them
			for (IGeometryCache geometry : _geometries) {
				boolean success = geometry.writeGeometry(kryo, output);
				if (!success) {
					System.err.println("Failed to write geometry to cache for " + geometry.getName());
					deleteCacheFile();
					return false;
				}
			}
			output.flush();
			return true;
		} catch (IOException e) {
			System.err.println("Failed to write cache: " + e.getMessage());

			if (file.exists()) {
				if (!file.delete()) {
					System.err.println("Unable to delete cache file: " + file.getAbsolutePath());
				}
			}
			e.printStackTrace();
			return false;
		} finally {
			if (output != null) {
				output.close();
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					System.err.println("Error closing FileOutputStream: " + e.getMessage());
				}
			}
		}
	}

	// get the cache file assumed in the current working directory
	private static File getCacheFile() {
		return new File(System.getProperty("user.home"), _cacheFileName);
	}

	/**
	 * Initialize all geometries. This will first try to read from the cache. If
	 * that fails, it will initialize using CCDB and then write to the cache.
	 */
	public static void initializeAllGeometry() {
		// first create all the geometries
		new AlertGeometry();
		new GeometryVersion();
		new DCGeometry();
		new UrWELLGeometry();
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

		// now try to read from the cache
		boolean readSuccess = readAllGeometries();
		if (readSuccess) {
			System.err.println("Successfully read geometry from cache.");
			return;
		}

		// failed so initialize using CCDB
		for (IGeometryCache geometry : _geometries) {
			geometry.initializeUsingCCDB();
		}

		// write to the cache
		boolean writeSuccess = writeAllGeometries();
		if (writeSuccess) {
			System.err.println("Successfully wrote geometry to cache.");
		} else {
			System.err.println("Failed to write geometry to cache.");
		}
	}

	//main program for testing
	public static void main(String arg[]) {
		initializeAllGeometry();
	}
}
