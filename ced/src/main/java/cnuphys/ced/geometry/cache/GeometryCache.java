package cnuphys.ced.geometry.cache;

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

/**
 * Coordinates initialization of detector geometry from authoritative sources.
 *
 * <p>The historical class name is retained temporarily to minimize churn at
 * call sites while the former Kryo cache is replaced.</p>
 */
public final class GeometryCache {

	private static final List<IGeometryCache> GEOMETRIES = new ArrayList<>();

	private GeometryCache() {
	}

	public static void addGeometry(IGeometryCache geometry) {
		GEOMETRIES.remove(geometry);
		GEOMETRIES.add(geometry);
	}

	/** Initializes every detector geometry directly from CCDB. */
	public static void initializeAllGeometry() {
		new AlertGeometry();
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

		System.out.println("Initializing geometry from CCDB (cache disabled).");
		for (IGeometryCache geometry : GEOMETRIES) {
			geometry.initializeUsingCCDB();
		}
	}
}
