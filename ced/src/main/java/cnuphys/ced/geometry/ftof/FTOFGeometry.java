package cnuphys.ced.geometry.ftof;

import java.awt.geom.Point2D;

import org.jlab.detector.base.GeometryFactory;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.component.ScintillatorPaddle;
import org.jlab.geom.detector.ftof.FTOFDetector;
import org.jlab.geom.detector.ftof.FTOFFactory;
import org.jlab.geom.detector.ftof.FTOFLayer;
import org.jlab.geom.detector.ftof.FTOFSector;
import org.jlab.geom.detector.ftof.FTOFSuperlayer;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.logging.DefaultLogger;

import cnuphys.bCNU.geometry.Plane;
import cnuphys.bCNU.geometry.Point;
import cnuphys.bCNU.geometry.Vector;
import cnuphys.ced.geometry.GeometryManager;

public class FTOFGeometry {

	public static final int PANEL_1A = 0;
	public static final int PANEL_1B = 1;
	public static final int PANEL_2 = 2;

	// the overall detector
	private static FTOFDetector _ftofDetector;

	// first index -s sector 0..5
	// 2nd index = panel type (superlayer) 0..2 for 1A, 1B, 2
	private static FTOFSuperlayer[][] _ftofSuperlayers = new FTOFSuperlayer[6][3];


	//planes for the face of the panels
	private static Plane _facePlanes[][] = new Plane[6][3];

	//there is only one layer per superlayer for FTOF so don't need third index
	private static FTOFLayer[][] _ftofLayers = new FTOFLayer[6][3];


	public static int numPaddles[] = new int[3];

	// ftof panels (one sector stored--in sector cs--all assumed to be the same)
	private static FTOFPanel _ftofPanel[] = new FTOFPanel[3];
	private static String ftofNames[] = { "Panel 1A", "Panel 1B", "Panel 2" };

	private static ConstantProvider _tofDataProvider;


	// only need sector 0
	private static FTOFSector _clas_sector0;

	/**
	 * Get the array of (3) forward time of flight panels.
	 *
	 * @return the ftofPanel array
	 */
	public static FTOFPanel[] getFtofPanel() {
		return _ftofPanel;
	}


	/**
	 * Initialize the FTOF Geometry
	 */
	public static void initialize() {
		System.out.println("\n=====================================");
		System.out.println("===  FTOF Geometry Initialization ===");
		System.out.println("=====================================");

		_tofDataProvider = GeometryFactory
				.getConstants(org.jlab.detector.base.DetectorType.FTOF);


		_ftofDetector = (new FTOFFactory()).createDetectorCLAS(_tofDataProvider);

		for (int sect = 0; sect < 6; sect++) {
			FTOFSector ftofSector = _ftofDetector.getSector(sect);
			for (int ptype = 0; ptype < 3; ptype++) {
				// only one layer (0)
				_ftofSuperlayers[sect][ptype] = ftofSector.getSuperlayer(ptype);
				_ftofLayers[sect][ptype] = _ftofSuperlayers[sect][ptype].getLayer(0);
			}
		}

		_clas_sector0 = _ftofDetector.getSector(0);

		// here superlayers are panels 1a, 1b, 2
		for (int superLayer = 0; superLayer < 3; superLayer++) {

			// there is only a layer 0
			FTOFLayer ftofLayer = _clas_sector0.getSuperlayer(superLayer).getLayer(0);

			numPaddles[superLayer] = ftofLayer.getNumComponents();

			_ftofPanel[superLayer] = new FTOFPanel(ftofNames[superLayer], numPaddles[superLayer]);
		}

		createFacePlanes();

	}

	//create the face planes, one for each panel in each sector
	private static void createFacePlanes() {
		for (int sect = 0; sect < 6; sect++) {
			for (int panel = 0; panel < 3; panel++) {
				_facePlanes[sect][panel] = createFacePlane(sect+1, panel);
			}
		}
	}

	//sect: 1 based plane: PANEL_1A, PANEL_1B or PANEL_12 (0, 1, 2)
	private static Plane createFacePlane(int sect, int panel) {
		//corners of front face
		Point3D corners[] = new Point3D[4];

		Point3D p0;
		Point3D p1;
		Point3D p2;
		Point3D p3;

		int numPaddle = _ftofLayers[sect-1][panel].getNumComponents();
		frontFace(sect, panel, 1, corners);
		Line3D line = new Line3D(corners[0], corners[3]);
		p0 = new Point3D(line.midpoint());

		frontFace(sect, panel, numPaddle/2, corners);
		line = new Line3D(corners[0], corners[3]);
		p1 = new Point3D(line.midpoint());

		frontFace(sect, panel, numPaddle, corners);
		p2 = corners[1];
		p3 = corners[2];

		Vector u = new Vector(p2.x() - p0.x(), p2.y() - p0.y(), p2.z() - p0.z());
		Vector v = new Vector(p3.x() - p0.x(), p3.y() - p0.y(), p3.z() - p0.z());
		Vector norm = Vector.cross(u, v); //doesn't have to be unit vector

		return new Plane(norm, p1.x(), p1.y(), p1.z());

	}

	/**
	 * Get the face plane used for drawing
	 * @param sector the 1-based sector
	 * @param panel the panel PANEL_1A, PANEL_1B or PANEL_12 (0, 1, 2)
	 * @return the projection plane used for drawing
	 */
	public static Plane getFacePlane(int sector, int panel) {
		return _facePlanes[sector-1][panel];
	}


	/**
	 * Get the paddle
	 * @param sect the 1-based sector
	 * @param panel the panel PANEL_1A, PANEL_1B or PANEL_12 (0, 1, 2)
	 * @param paddleId the 1-based paddle id
	 * @return
	 */
	public static ScintillatorPaddle getPaddle(int sect, int panel, int paddleId) {
		ScintillatorPaddle paddle = _ftofLayers[sect-1][panel].getComponent(paddleId - 1);
		return paddle;
	}



	/**
	 * Used by the 3D drawing
	 *
	 * @param sector     the 1-based sector
	 * @param panel PANEL_1A, PANEL_1B or PANEL_12 (0, 1, 2)
	 * @param paddleId   the 1-based paddle ID
	 * @param coords     holds 8*3 = 24 values [x1, y1, z1, ..., x8, y8, z8]
	 */
	public static void paddleVertices(int sector, int panel, int paddleId, float[] coords) {

		Point3D v[] = new Point3D[8];

		ScintillatorPaddle paddle = getPaddle(sector, panel, paddleId);
		for (int i = 0; i < 8; i++) {
			v[i] = new Point3D(paddle.getVolumePoint(i));
		}

		for (int i = 0; i < 8; i++) {
			int j = 3 * i;
			coords[j] = (float) v[i].x();
			coords[j + 1] = (float) v[i].y();
			coords[j + 2] = (float) v[i].z();
		}
	}

	/**
	 * Get the 4 corners of the front face
	 * @param sector     the 1-based sector
	 * @param panel PANEL_1A, PANEL_1B or PANEL_12 (0, 1, 2)
	 * @param paddleId   the 1-based paddle ID
	 * @param corners will contain the 4 corners of the front face
	 */
	public static void frontFace(int sector, int panel, int paddleId, Point3D corners[]) {
		ScintillatorPaddle paddle = getPaddle(sector, panel, paddleId);
		corners[0] = new Point3D(paddle.getVolumePoint(0));
		corners[1] = new Point3D(paddle.getVolumePoint(1));
		corners[2] = new Point3D(paddle.getVolumePoint(5));
		corners[3] = new Point3D(paddle.getVolumePoint(4));

	}

	/**
	 * Get the z that points the point on the face plane
	 * @param sector     the 1-based sector
	 * @param panel PANEL_1A, PANEL_1B or PANEL_12 (0, 1, 2)
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @return the value of z that uts the point on the plane
	 */
	public static double getZ(int sector, int panel, double x, double y) {
		return _facePlanes[sector-1][panel].getZ(x, y);
	}

	/**
	 * Convert from clas3D to local coordinates
	 * @param sector the 1-based sector
	 * @param panel  PANEL_1A, PANEL_1B or PANEL_12 (0, 1, 2)
	 * @param x the x  clas coordinate
	 * @param y the y  clas coordinate
	 * @param z the z  clas coordinate
	 * @param pp the screen point
	 */
	public static void clasToWorld(int sector, int panel, double x, double y, double z, Point2D.Double wp) {
		Point cp = new Point();
		_facePlanes[sector-1][panel].closestPoint(x, y, z, cp);
		wp.setLocation(cp.x, cp.y);
	}

	/**
	 * Get the polygon for a paddle
	 * @param sector the 1-based sector
	 * @param panel PANEL_1A, PANEL_1B or PANEL_12 (0, 1, 2)
	 * @param paddleId   the 1-based paddle ID
	 * @param wp will hold the four corners in world coordinates
	 */
	public static void paddlePolygon(int sector, int panel, int paddleId, Point2D.Double wp[]) {
		Point3D corners[] = new Point3D[4];
		frontFace(sector, panel, paddleId, corners);
		for (int i = 0; i < 4; i++) {
			wp[i].x = corners[i].x();
			wp[i].y = corners[i].y();
		}
	}

	/**
	 * Get the name of the panel
	 * @param panel PANEL_1A, PANEL_1B or PANEL_12 (0, 1, 2)
	 * @return the name of the panel
	 */
	public static String getPanelName(int panel) {
		return ftofNames[panel];
	}

	/**
	 * Get the number of paddles
	 * @param sector the 1-based sector
	 * @param panel PANEL_1A, PANEL_1B or PANEL_12 (0, 1, 2)
	 * @return the number of paddles
	 */
	public static int getNumPaddles(int sector, int panel) {
		return _ftofLayers[sector-1][panel].getNumComponents();
	}


	/**
	 * Chech paddle intersection
	 *
	 * @param superlayer the 0 based superlayer for 1A, 1B, 2
	 * @param paddleid   the 0 based paddle id
	 * @return if the projected polygon fully intersects the plane
	 */
	public static boolean doesProjectedPolyFullyIntersect(int superlayer, int paddleid, Plane3D projectionPlane) {

		FTOFLayer ftofLayer = _clas_sector0.getSuperlayer(superlayer).getLayer(0);
		ScintillatorPaddle paddle = ftofLayer.getComponent(paddleid);
		boolean isects = false;

		try {
			isects = GeometryManager.doesProjectedPolyIntersect(paddle, projectionPlane, 6, 4);
		} catch (Exception e) {

			System.err.println("Exception in FTOFGeometry doesProjectedPolyFullyIntersect");
			System.err.println("panel: " + ftofNames[superlayer] + " paddleID: " + paddleid);
		}

		return isects;
	}

	/**
	 * Get the intersections with a constant phi plane. If the paddle does not
	 * intersect (happens as phi grows) return null;
	 *
	 * @param superlayer      0, 1 or 2 for 1A, 1B, 2
	 * @param paddleid        the 0-based paddle id
	 * @param projectionPlane the projection plane
	 */
	public static boolean getIntersections(int superlayer, int paddleid, Plane3D projectionPlane, Point2D.Double wp[]) {
		FTOFLayer ftofLayer = _clas_sector0.getSuperlayer(superlayer).getLayer(0);
		ScintillatorPaddle paddle = ftofLayer.getComponent(paddleid);
		return GeometryManager.getProjectedPolygon(paddle, projectionPlane, 6, 4, wp, null);
	}

	/**
	 * Get the length of a paddle in cm
	 *
	 * @param superlayer 0, 1 or 2 for 1A, 1B, 2
	 * @param paddleid   the 0-based paddle id
	 * @return the length of the paddle
	 */
	public static double getLength(int superlayer, int paddleId) {
		FTOFLayer ftofLayer = _clas_sector0.getSuperlayer(superlayer).getLayer(0);
		ScintillatorPaddle paddle = ftofLayer.getComponent(paddleId);
		return paddle.getLength();
	}

	/**
	 * Get an array of all the lengths
	 *
	 * @param superlayer 0, 1 or 2 for 1A, 1B, 2
	 * @return an array of all the paddle lengths
	 */
	public static double[] getLengths(int superlayer) {
		double[] length = new double[numPaddles[superlayer]];
		for (int i = 0; i < length.length; i++) {
			length[i] = getLength(superlayer, i);
		}
		return length;
	}

	public static void main(String arg[]) {
		// this is supposed to create less pounding of ccdb
		DefaultLogger.initialize();

		initialize();

		int sect = 1;
		int panel = PANEL_1B;
		int paddleId = 43;

		Point3D corners[] = new Point3D[4];
		frontFace(sect, panel, paddleId, corners);

		System.out.println(String.format("\nsect: %d    panel: %d    paddle: %d", sect, panel, paddleId));
		for (int i = 0; i < 4; i++) {
			System.out.println("[" + i + "]    " + corners[i]);
		}

		sect = 5;
		panel = PANEL_2;
		paddleId = 3;
		frontFace(sect, panel, paddleId, corners);

		System.out.println(String.format("\nsect: %d    panel: %d    paddle: %d", sect, panel, paddleId));
		for (int i = 0; i < 4; i++) {
			System.out.println("[" + i + "]    " + corners[i]);
		}

		System.out.println("FTOF geo done");
	}
}
