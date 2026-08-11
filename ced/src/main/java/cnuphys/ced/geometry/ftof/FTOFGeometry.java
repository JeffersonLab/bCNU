package cnuphys.ced.geometry.ftof;

import java.awt.geom.Point2D;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

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


import cnuphys.bCNU.geometry.Plane;
import cnuphys.bCNU.geometry.Point;
import cnuphys.bCNU.geometry.Vector;
import cnuphys.ced.geometry.GeometryManager;
import cnuphys.ced.geometry.cache.ACachedGeometry;

public class FTOFGeometry extends ACachedGeometry {
	private static final int SECTOR_COUNT = 6;
	private static final int PANEL_COUNT = 3;
	private static final int CORNER_COUNT = 8;
	private static final int PROJECTION_EDGE_COUNT = 4;

	public static final int PANEL_1A = 0;
	public static final int PANEL_1B = 1;
	public static final int PANEL_2 = 2;

	private static final String panelNames[] = { "Panel 1A", "Panel 1B", "Panel 2" };
	private static final String briefPNames[] = { "1A", "1B", "2" };

	// first index -s sector 0..5
	// 2nd index = panel type (superlayer) 0..2 for 1A, 1B, 2
	private static FTOFSuperlayer[][] ftofSuperlayers = new FTOFSuperlayer[6][3];

	// planes for the face of the panels (NOT CACHED)
	private static Plane facePlanes[][] = new Plane[6][3];

	// there is only one layer per superlayer for FTOF so don't need third index
	private static FTOFLayer[][] ftofLayers = new FTOFLayer[6][3];

	public static int numPaddles[]; // number of paddles in each panel NOT CACHED

	// ftof panels (one sector stored--in sector cs--all assumed to be the same)
	private static FTOFPanel ftofPanel[] = new FTOFPanel[3];
	private static String ftofNames[] = { "Panel 1A", "Panel 1B", "Panel 2" };

	// Explicit runtime geometry, usable after the CCDB object graph is discarded.
	private static double[][][][][] paddleCorners;
	private static double[][][][][][] projectionEdges;
	private static double[][][] paddleLengths;

	public FTOFGeometry() {
		super("FTOFGeometry");
	}

	/**
	 * Initialize the FTOF Geometry
	 */
	@Override
	public void initializeUsingCCDB() {
		ConstantProvider tofDataProvider = GeometryFactory.getConstants(org.jlab.detector.base.DetectorType.FTOF);

		FTOFDetector ftofDetector = (new FTOFFactory()).createDetectorCLAS(tofDataProvider);

		for (int sect = 0; sect < 6; sect++) {
			FTOFSector ftofSector = ftofDetector.getSector(sect);
			for (int ptype = 0; ptype < 3; ptype++) {
				// only one layer (0)
				ftofSuperlayers[sect][ptype] = ftofSector.getSuperlayer(ptype);
				ftofLayers[sect][ptype] = ftofSuperlayers[sect][ptype].getLayer(0);
			}
		}

		capturePrimitiveGeometry();
		createPanels();
		createFacePlanes();
	}

	private static void capturePrimitiveGeometry() {
		numPaddles = new int[PANEL_COUNT];
		for (int panel = 0; panel < PANEL_COUNT; panel++) {
			numPaddles[panel] = ftofLayers[0][panel].getNumComponents();
		}
		paddleCorners = new double[SECTOR_COUNT][PANEL_COUNT][][][];
		projectionEdges = new double[SECTOR_COUNT][PANEL_COUNT][][][][];
		paddleLengths = new double[SECTOR_COUNT][PANEL_COUNT][];
		for (int sector = 0; sector < SECTOR_COUNT; sector++) {
			for (int panel = 0; panel < PANEL_COUNT; panel++) {
				int count = numPaddles[panel];
				paddleCorners[sector][panel] = new double[count][CORNER_COUNT][3];
				projectionEdges[sector][panel] = new double[count][PROJECTION_EDGE_COUNT][2][3];
				paddleLengths[sector][panel] = new double[count];
				for (int paddle = 0; paddle < count; paddle++) {
					ScintillatorPaddle source = ftofLayers[sector][panel].getComponent(paddle);
					for (int corner = 0; corner < CORNER_COUNT; corner++) {
						copyPoint(source.getVolumePoint(corner), paddleCorners[sector][panel][paddle][corner]);
					}
					for (int edge = 0; edge < PROJECTION_EDGE_COUNT; edge++) {
						Line3D line = source.getVolumeEdge(6 + edge);
						copyPoint(line.origin(), projectionEdges[sector][panel][paddle][edge][0]);
						copyPoint(line.end(), projectionEdges[sector][panel][paddle][edge][1]);
					}
					paddleLengths[sector][panel][paddle] = source.getLength();
				}
			}
		}
	}

	private static void copyPoint(Point3D point, double[] destination) {
		destination[0] = point.x();
		destination[1] = point.y();
		destination[2] = point.z();
	}

	private static void createPanels() {
		for (int superLayer = 0; superLayer < 3; superLayer++) {
			ftofPanel[superLayer] = new FTOFPanel(ftofNames[superLayer], numPaddles[superLayer]);
		}
	}

	// create the face planes, one for each panel in each sector
	private static void createFacePlanes() {
		for (int sect = 0; sect < 6; sect++) {
			for (int panel = 0; panel < 3; panel++) {
				facePlanes[sect][panel] = createFacePlane(sect + 1, panel);
			}
		}
	}

	// sect: 1 based plane: PANEL_1A, PANEL_1B or PANEL_12 (0, 1, 2)
	private static Plane createFacePlane(int sect, int panel) {
		// corners of front face
		Point3D corners[] = new Point3D[4];

		Point3D p0;
		Point3D p1;
		Point3D p2;
		Point3D p3;

		int numPaddle = getNumPaddles(sect, panel);
		frontFace(sect, panel, 1, corners);
		Line3D line = new Line3D(corners[0], corners[3]);
		p0 = new Point3D(line.midpoint());

		frontFace(sect, panel, numPaddle / 2, corners);
		line = new Line3D(corners[0], corners[3]);
		p1 = new Point3D(line.midpoint());

		frontFace(sect, panel, numPaddle, corners);
		p2 = corners[1];
		p3 = corners[2];

		Vector u = new Vector(p2.x() - p0.x(), p2.y() - p0.y(), p2.z() - p0.z());
		Vector v = new Vector(p3.x() - p0.x(), p3.y() - p0.y(), p3.z() - p0.z());
		Vector norm = Vector.cross(u, v); // doesn't have to be unit vector

		return new Plane(norm, p1.x(), p1.y(), p1.z());

	}

	/**
	 * Get the array of (3) forward time of flight panels.
	 *
	 * @return the ftofPanel array
	 */
	public static FTOFPanel[] getFtofPanel() {
		return ftofPanel;
	}

	/**
	 * Get the face plane used for drawing
	 * 
	 * @param sector the 1-based sector
	 * @param panel  the panel PANEL_1A, PANEL_1B or PANEL_12 (0, 1, 2)
	 * @return the projection plane used for drawing
	 */
	public static Plane getFacePlane(int sector, int panel) {
		return facePlanes[sector - 1][panel];
	}

	/**
	 * Get the paddle
	 * 
	 * @param sect     the 1-based sector
	 * @param panel    the panel PANEL_1A, PANEL_1B or PANEL_12 (0, 1, 2)
	 * @param paddleId the 1-based paddle id
	 * @return
	 */
	public static ScintillatorPaddle getPaddle(int sect, int panel, int paddleId) {
		ScintillatorPaddle paddle = ftofLayers[sect - 1][panel].getComponent(paddleId - 1);
		return paddle;
	}

	/**
	 * Used by the 3D drawing
	 *
	 * @param sector   the 1-based sector
	 * @param panel    PANEL_1A, PANEL_1B or PANEL_12 (0, 1, 2)
	 * @param paddleId the 1-based paddle ID
	 * @param coords   holds 8*3 = 24 values [x1, y1, z1, ..., x8, y8, z8]
	 */
	public static void paddleVertices(int sector, int panel, int paddleId, float[] coords) {
		for (int i = 0; i < 8; i++) {
			int j = 3 * i;
			coords[j] = (float) paddleCorners[sector - 1][panel][paddleId - 1][i][0];
			coords[j + 1] = (float) paddleCorners[sector - 1][panel][paddleId - 1][i][1];
			coords[j + 2] = (float) paddleCorners[sector - 1][panel][paddleId - 1][i][2];
		}
	}

	/**
	 * Get the 4 corners of the front face
	 * 
	 * @param sector   the 1-based sector
	 * @param panel    PANEL_1A, PANEL_1B or PANEL_12 (0, 1, 2)
	 * @param paddleId the 1-based paddle ID
	 * @param corners  will contain the 4 corners of the front face
	 */
	public static void frontFace(int sector, int panel, int paddleId, Point3D corners[]) {
		int[] indices = {0, 1, 5, 4};
		for (int index = 0; index < indices.length; index++) {
			double[] point = paddleCorners[sector - 1][panel][paddleId - 1][indices[index]];
			corners[index] = new Point3D(point[0], point[1], point[2]);
		}

	}

	/**
	 * Get the z that points the point on the face plane
	 * 
	 * @param sector the 1-based sector
	 * @param panel  PANEL_1A, PANEL_1B or PANEL_12 (0, 1, 2)
	 * @param x      the x coordinate
	 * @param y      the y coordinate
	 * @return the value of z that uts the point on the plane
	 */
	public static double getZ(int sector, int panel, double x, double y) {
		return facePlanes[sector - 1][panel].getZ(x, y);
	}

	/**
	 * Convert from clas3D to local coordinates
	 * 
	 * @param sector the 1-based sector
	 * @param panel  PANEL_1A, PANEL_1B or PANEL_12 (0, 1, 2)
	 * @param x      the x clas coordinate
	 * @param y      the y clas coordinate
	 * @param z      the z clas coordinate
	 * @param pp     the screen point
	 */
	public static void clasToWorld(int sector, int panel, double x, double y, double z, Point2D.Double wp) {
		Point cp = new Point();
		facePlanes[sector - 1][panel].closestPoint(x, y, z, cp);
		wp.setLocation(cp.x, cp.y);
	}

	/**
	 * Get the polygon for a paddle
	 * 
	 * @param sector   the 1-based sector
	 * @param panel    PANEL_1A, PANEL_1B or PANEL_12 (0, 1, 2)
	 * @param paddleId the 1-based paddle ID
	 * @param wp       will hold the four corners in world coordinates
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
	 * 
	 * @param panel PANEL_1A, PANEL_1B or PANEL_12 (0, 1, 2)
	 * @return the name of the panel
	 */
	public static String getPanelName(int panel) {
		return ftofNames[panel];
	}

	/**
	 * Get the number of paddles
	 * 
	 * @param sector the 1-based sector
	 * @param panel  PANEL_1A, PANEL_1B or PANEL_12 (0, 1, 2)
	 * @return the number of paddles
	 */
	public static int getNumPaddles(int sector, int panel) {
		return numPaddles[panel];
	}

	/**
	 * Chech paddle intersection
	 *
	 * @param superlayer the 0 based superlayer for 1A, 1B, 2
	 * @param paddleid   the 0 based paddle id
	 * @return if the projected polygon fully intersects the plane
	 */
	public static boolean doesProjectedPolyFullyIntersect(int superlayer, int paddleid, Plane3D projectionPlane) {

		// FTOFLayer ftofLayer = _clas_sector0.getSuperlayer(superlayer).getLayer(0);
		return GeometryManager.doesProjectedPolyIntersect(projectionEdges[0][superlayer][paddleid], projectionPlane);
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
		return GeometryManager.getProjectedPolygon(projectionEdges[0][superlayer][paddleid], projectionPlane, wp, null);
	}

	/**
	 * Get the length of a paddle in cm
	 *
	 * @param superlayer 0, 1 or 2 for 1A, 1B, 2
	 * @param paddleid   the 0-based paddle id
	 * @return the length of the paddle
	 */
	public static double getLength(int superlayer, int paddleId) {
//		FTOFLayer ftofLayer = _clas_sector0.getSuperlayer(superlayer).getLayer(0);
		return paddleLengths[0][superlayer][paddleId];
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

	@Override
	public boolean supportsCache() {
		return true;
	}

	@Override
	public void readGeometry(DataInput input) throws IOException {
		int sectors = input.readInt();
		int panels = input.readInt();
		if (sectors != SECTOR_COUNT || panels != PANEL_COUNT) {
			throw new IOException("Unexpected FTOF dimensions: " + sectors + " x " + panels);
		}
		int[] counts = new int[PANEL_COUNT];
		for (int panel = 0; panel < PANEL_COUNT; panel++) {
			counts[panel] = input.readInt();
			if (counts[panel] < 1 || counts[panel] > 1_000) {
				throw new IOException("Invalid FTOF paddle count: " + counts[panel]);
			}
		}
		double[][][][][] corners = new double[SECTOR_COUNT][PANEL_COUNT][][][];
		double[][][][][][] edges = new double[SECTOR_COUNT][PANEL_COUNT][][][][];
		double[][][] lengths = new double[SECTOR_COUNT][PANEL_COUNT][];
		for (int sector = 0; sector < SECTOR_COUNT; sector++) {
			for (int panel = 0; panel < PANEL_COUNT; panel++) {
				int count = counts[panel];
				corners[sector][panel] = new double[count][CORNER_COUNT][3];
				edges[sector][panel] = new double[count][PROJECTION_EDGE_COUNT][2][3];
				lengths[sector][panel] = new double[count];
				for (int paddle = 0; paddle < count; paddle++) {
					for (int corner = 0; corner < CORNER_COUNT; corner++) {
						readPoint(input, corners[sector][panel][paddle][corner]);
					}
					for (int edge = 0; edge < PROJECTION_EDGE_COUNT; edge++) {
						readPoint(input, edges[sector][panel][paddle][edge][0]);
						readPoint(input, edges[sector][panel][paddle][edge][1]);
					}
					lengths[sector][panel][paddle] = input.readDouble();
				}
			}
		}
		numPaddles = counts;
		paddleCorners = corners;
		projectionEdges = edges;
		paddleLengths = lengths;
		ftofLayers = null;
		ftofSuperlayers = null;
		createPanels();
		createFacePlanes();
	}

	@Override
	public void writeGeometry(DataOutput output) throws IOException {
		output.writeInt(SECTOR_COUNT);
		output.writeInt(PANEL_COUNT);
		for (int count : numPaddles) {
			output.writeInt(count);
		}
		for (int sector = 0; sector < SECTOR_COUNT; sector++) {
			for (int panel = 0; panel < PANEL_COUNT; panel++) {
				for (int paddle = 0; paddle < numPaddles[panel]; paddle++) {
					for (int corner = 0; corner < CORNER_COUNT; corner++) {
						writePoint(output, paddleCorners[sector][panel][paddle][corner]);
					}
					for (int edge = 0; edge < PROJECTION_EDGE_COUNT; edge++) {
						writePoint(output, projectionEdges[sector][panel][paddle][edge][0]);
						writePoint(output, projectionEdges[sector][panel][paddle][edge][1]);
					}
					output.writeDouble(paddleLengths[sector][panel][paddle]);
				}
			}
		}
	}

	private static void readPoint(DataInput input, double[] point) throws IOException {
		for (int coordinate = 0; coordinate < 3; coordinate++) {
			point[coordinate] = input.readDouble();
		}
	}

	private static void writePoint(DataOutput output, double[] point) throws IOException {
		for (double coordinate : point) {
			output.writeDouble(coordinate);
		}
	}

	/**
	 * Get the brief panel name
	 *
	 * @param layer the 1-based layer 1..3
	 * @return the brief panel name
	 */
	public static String getBriefPanelName(byte layer) {
		if ((layer < 1) || (layer > 3)) {
			return "" + layer;
		} else {
			return briefPNames[layer - 1];
		}
	}

	/**
	 * Get the name from the panel type
	 *
	 * @param panelType one of the constants (PANEL_1A, PANEL_1B, PANEL_2)
	 * @return the name of the panel type
	 */
	public static String panelName(int panelType) {
		if ((panelType < 0) || (panelType > 2)) {
			return "???";
		} else {
			return panelNames[panelType];
		}
	}

}
