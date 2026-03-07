package cnuphys.ced.geometry;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jlab.detector.calib.utils.DatabaseConstantProvider;
import org.jlab.detector.geant4.v2.SVT.SVTStripFactory;
import org.jlab.geometry.prim.Line3d;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.frame.Ced;
import cnuphys.ced.geometry.cache.ACachedGeometry;
import eu.mihosoft.vrl.v3d.Vector3d;

public class BSTGeometry extends ACachedGeometry {

//	private static SVTStripFactory _svtFac;

	// BSTxy panels
	private static ArrayList<BSTxyPanel> _bstXYpanelsLayers = new ArrayList<>(); // new

	// for putting in dead zone
	private static final double ZGAP = 1.67; // mm

	/** sectors per superlayer */
	public static final int[] sectorsPerLayer = { 10, 10, 14, 14, 18, 18 };

	public BSTGeometry() {
		super("BST Geometry");
	}

	// all the strips
	private static HashMap<String, Line3d> _strips;

	/**
	 * Initialize the BST Geometry
	 */
	@Override
	public void initializeUsingCCDB() {

		System.out.println("\n=====================================");
		System.out.println("===  BST Geometry Initialization  ===");
		System.out.println("=====================================");

		String variationName = Ced.getGeometryVariation();
		DatabaseConstantProvider cp = new DatabaseConstantProvider(11, variationName);

		SVTStripFactory svtFac = new SVTStripFactory(cp, true);

		getStrips(svtFac);
		getBSTPanels();
	}

	/**
	 * Get the list of BST XY panels
	 *
	 * @return the panels
	 */
	public static List<BSTxyPanel> getBSTxyPanels() {
		return _bstXYpanelsLayers;
	}

	// read the bst geometry
	private static void getBSTPanels() {

		// use the geometry service

		double vals[] = new double[10];
		for (int layer = 0; layer < 6; layer++) {

			int numSect = BSTGeometry.sectorsPerLayer[layer];

			for (int sector = 0; sector < numSect; sector++) {

				BSTGeometry.getLimitValues(sector, layer, vals);
				_bstXYpanelsLayers.add(new BSTxyPanel(sector + 1, layer + 1, vals));
			}
		}

	}

	private static void getStrips(SVTStripFactory svtFac) {
		_strips = new HashMap<>();
		for (int layer = 0; layer < 6; layer++) {
			int numSect = BSTGeometry.sectorsPerLayer[layer];

			for (int sector = 0; sector < numSect; sector++) {

				for (int strip = 0; strip < 256; strip++) {
					Line3d line = svtFac.getStrip(layer, sector, strip);
					_strips.put(hashKey(layer, sector, strip), line);
				}

			}
		}
	}

	private static String hashKey(int layer, int sector, int strip) {
		return layer + "_" + sector + "_" + strip;
	}

	/**
	 * Get the strip as a line
	 *
	 * @param sector (0-based) number of sectors by layer are {10, 10, 14, 14, 18,
	 *               18}
	 * @param layer  (0-based) [0..5]
	 * @param strip  (0-based)should be in the range [0..255]
	 * @return the strip as a line units are mm
	 */
	public static Line3d getStrip(int sector, int layer, int strip) {
		try {
			return _strips.get(hashKey(layer, sector, strip));
		} catch (IllegalArgumentException e) {
			System.err.println("Event number: " + ClasIoEventManager.getInstance().getSequentialEventNumber() + "  "
					+ e.getMessage());
			System.err.println("Illegal Values: getStrip: sector=" + (sector + 1) + " layer=" + (layer + 1) + " strip="
					+ (strip + 1));
		}
		return null;
	}

	/**
	 * Get the coordinates (a line) for a strip for 3D view in cm
	 *
	 * @param sector (0-based) number of sectors by layer are {10, 10, 14, 14, 18,
	 *               18}
	 * @param layer  (0-based) [0..5]
	 * @param strip  (0-based)should be in the range [0..255]
	 * @param coords (dim = 6) will hold line as [x1,y1,z1,x2,y2,z2] in cm
	 */
	public static void getStripCM(int sector, int layer, int strip, float coords[]) {
		getStrip(sector, layer, strip, coords);
		for (int i = 0; i < coords.length; i++) {
			coords[i] /= 10;
		}
	}

	/**
	 * Get the coordinates (a line) for a strip for 3D view
	 *
	 * @param sector (0-based) number of sectors by layer are {10, 10, 14, 14, 18,
	 *               18}
	 * @param layer  (0-based) [0..5]
	 * @param strip  (0-based)should be in the range [0..255]
	 * @param coords (dim = 6) will hold line as [x1,y1,z1,x2,y2,z2] in mm
	 */
	public static void getStrip(int sector, int layer, int strip, float coords[]) {

		// note supl and lay just computed as zero based
		Line3d line = getStrip(sector, layer, strip);

		if (line != null) {
			coords[0] = (float) (line.origin().x);
			coords[1] = (float) (line.origin().y);
			coords[2] = (float) (line.origin().z);
			coords[3] = (float) (line.end().x);
			coords[4] = (float) (line.end().y);
			coords[5] = (float) (line.end().z);
		}
	}

	/**
	 * Get the triplet quad coordinates for 3D view. Units are cm
	 *
	 * @param sector (0-based) number of sectors are {10, 14, 18}
	 * @param layer  (0-based) [0..5]
	 * @param coords (dim = 26) will hold quads as [x1, y1, z1, ... x4, y4, z4] for
	 *               quad 1 (12 numbers) [x1, y1, z1, ... x4, y4, z4] for quad 2 (12
	 *               numbers) [x1, y1, z1, ... x4, y4, z4] for quad 3 (12 numbers)
	 */

	public static void getLayerQuads(int sector, int layer, float coords[]) {

		double vals[] = new double[10];

		getLimitValues(sector, layer, vals); // returns mm
		// covert to cm
		for (int i = 0; i < 10; i++) {
			vals[i] /= 10;
		}

		float x1 = (float) vals[0];
		float y1 = (float) vals[1];
		float x2 = (float) vals[2];
		float y2 = (float) vals[3];

		float z1 = (float) vals[4];
		float z2 = (float) vals[5];
		float z3 = (float) vals[6];
		float z4 = (float) vals[7];
		float z5 = (float) vals[8];
		float z6 = (float) vals[9];

		fillCoords(0, coords, x1, y1, x2, y2, z1, z2);
		fillCoords(12, coords, x1, y1, x2, y2, z3, z4);
		fillCoords(24, coords, x1, y1, x2, y2, z5, z6);
	}

	// fill the 3D coords
	private static void fillCoords(int index, float coords[], float x1, float y1, float x2, float y2, float zmin,
			float zmax) {

		coords[index++] = x1;
		coords[index++] = y1;
		coords[index++] = zmin;
		coords[index++] = x1;
		coords[index++] = y1;
		coords[index++] = zmax;
		coords[index++] = x2;
		coords[index++] = y2;
		coords[index++] = zmax;
		coords[index++] = x2;
		coords[index++] = y2;
		coords[index++] = zmin;

	}

	/**
	 * Get the points in the geometry service that were in the old file for drawing
	 * in the BST views
	 *
	 * @param sector (0-based) number of sectors by layer are {10, 10, 14, 14, 18,
	 *               18}
	 * @param layer  (0-based) [0..5]
	 * @param vals   holds for (10) numbers. All are in mm. The first four are x, y,
	 *               x, y for drawing the xy view. The last six are the six z values
	 *               that define (in z) the three active regions
	 */
	public static void getLimitValues(int sector, int layer, double vals[]) {

		Line3d line0 = getStrip(sector, layer, 0);
		Line3d line1 = getStrip(sector, layer, 255);
		Vector3d o = line0.origin();
		Vector3d e = line1.end();
		vals[0] = o.x;
		vals[1] = o.y;
		vals[2] = e.x;
		vals[3] = e.y;

		double z0 = Double.POSITIVE_INFINITY;
		double z5 = Double.NEGATIVE_INFINITY;

		for (int strip = 0; strip < 256; strip++) {
			Line3d line = getStrip(sector, layer, strip);
			Vector3d p0 = line.origin();
			Vector3d p1 = line.end();

			z0 = Math.min(z0, p0.z);
			z0 = Math.min(z0, p1.z);
			z5 = Math.max(z5, p0.z);
			z5 = Math.max(z5, p1.z);
		}
		// put in dead zone by hand
		double del = (z5 - z0) / 3;
		double z1 = z0 + del - ZGAP / 2;
		double z2 = z1 + ZGAP;
		double z3 = z5 - del - ZGAP / 2;
		double z4 = z3 + ZGAP;

		vals[4] = z0;
		vals[5] = z1;
		vals[6] = z2;
		vals[7] = z3;
		vals[8] = z4;
		vals[9] = z5;

	}

	/**
	 * Get the XY coordinates of the midpoint of the line
	 *
	 * @param sector (0-based) number of sectors by layer are {10, 10, 14, 14, 18,
	 *               18}
	 * @param layer  (0-based) [0..5]
	 * @param strip  (0-based)should be in the range [0..255]
	 * @return the midpoint of the strip, with the z component dropped
	 */
	public static Point2D.Double getStripMidpointXY(int sector, int layer, int strip) {
		Point2D.Double wp = new Point2D.Double();
		getStripMidpointXY(sector, layer, strip, wp);
		return wp;
	}

	/**
	 * Get the XY coordinates of the midpoint of the line
	 *
	 * @param sector (0-based) number of sectors by layer are {10, 10, 14, 14, 18,
	 *               18}
	 * @param layer  (0-based) [0..5]
	 * @param strip  (0-based)should be in the range [0..255]
	 * @param wp     will hold the point
	 * @return the midpoint of the strip, with the z component dropped units are mm
	 */
	public static void getStripMidpointXY(int sector, int layer, int strip, Point2D.Double wp) {

		Line3d line = getStrip(sector, layer, strip);
		Vector3d p0 = line.origin();
		Vector3d p1 = line.end();

		double xmp = 0.5 * (p0.x + p1.x);
		double ymp = 0.5 * (p0.y + p1.y);
		wp.setLocation(xmp, ymp);
	}

	/**
	 * Get the 3D midpoint
	 * 
	 * @param sector (0-based) number of sectors by layer are {10, 10, 14, 14, 18,
	 *               18}
	 * @param layer  (0-based) [0..5]
	 * @param strip  (0-based)should be in the range [0..255]
	 * @return the strip midpoint units are mm
	 */
	public static Vector3d getStripMidpoint(int sector, int layer, int strip) {

		Line3d line = getStrip(sector, layer, strip);
		Vector3d p0 = line.origin();
		Vector3d p1 = line.end();

		double xmp = 0.5 * (p0.x + p1.x);
		double ymp = 0.5 * (p0.y + p1.y);
		double zmp = 0.5 * (p0.z + p1.z);

		return new Vector3d(xmp, ymp, zmp);
	}


	@Override
	public boolean readGeometry(Kryo kryo, Input input) {
	    try {
	        // Read the BSTxy panels list.
	        _bstXYpanelsLayers = kryo.readObjectOrNull(input, ArrayList.class);
	        // Read the _strips map.
	        _strips = kryo.readObjectOrNull(input, HashMap.class);
	        return true;
	    } catch (Exception e) {
	        return false;
	    }
	}

	@Override
	public boolean writeGeometry(Kryo kryo, Output output) {
	    try {
	        // Write the BSTxy panels list.
	        kryo.writeObjectOrNull(output, _bstXYpanelsLayers, ArrayList.class);
	        // Write the _strips map.
	        kryo.writeObjectOrNull(output, _strips, HashMap.class);
	        return true;
	    } catch (Exception e) {
	        return false;
	    }
	}
}
