package cnuphys.ced.geometry;

import java.awt.geom.Point2D;

import org.jlab.detector.base.GeometryFactory;
import org.jlab.detector.geom.dc.DCGeantFactory;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.component.DriftChamberWire;
import org.jlab.geom.detector.dc.DCDetector;
import org.jlab.geom.detector.dc.DCLayer;
import org.jlab.geom.detector.dc.DCSector;
import org.jlab.geom.detector.dc.DCSuperlayer;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Triangle3D;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import cnuphys.ced.frame.Ced;
import cnuphys.ced.geometry.cache.ACachedGeometry;

public class DCGeometry extends ACachedGeometry {

	private static double minWireX;
	private static double maxWireX;

	/**
	 * These are the drift chamber wires from the geometry service. The indices are
	 * 0-based: [superlayer 0:5][layer 0:5][wire 0:111] NOTE: a DriftChamberWire is
	 * actually the full hexagonal volume. Its getLine method returns the line of
	 * the sense wire.
	 */
	private static DriftChamberWire wires[][][];

	public DCGeometry() {
		super("DriftChamber");
	}

	/**
	 * Initialize the DC Geometry by loading all the wires
	 */
	@Override
	public void initializeUsingCCDB() {

		int run = 4013;
		String variation = Ced.getGeometryVariation();
		ConstantProvider cp = GeometryFactory.getConstants(org.jlab.detector.base.DetectorType.DC, run, variation);

		DCGeantFactory factory = new DCGeantFactory();

		DCDetector _dcDetector = factory.createDetectorCLAS(cp);

		DCSector sector0 = _dcDetector.getSector(0);

		minWireX = Double.POSITIVE_INFINITY;
		maxWireX = Double.NEGATIVE_INFINITY;

		wires = new DriftChamberWire[6][6][112];
		for (int suplay = 0; suplay < 6; suplay++) {
			DCSuperlayer sl = sector0.getSuperlayer(suplay);

			for (int lay = 0; lay < 6; lay++) {
				DCLayer dcLayer = sl.getLayer(lay);

				for (int w = 0; w < 112; w++) {
					DriftChamberWire dcw = dcLayer.getComponent(w);

					wires[suplay][lay][w] = dcw;

					Line3D line = dcw.getLine();
					double xx0 = line.origin().x();
					double xx1 = line.end().x();

					minWireX = Math.min(minWireX, xx0);
					minWireX = Math.min(minWireX, xx1);
					maxWireX = Math.max(maxWireX, xx0);
					maxWireX = Math.max(maxWireX, xx1);
				}
			}
		}

	}

	/**
	 * Used by the 3D drawing
	 *
	 * @param sector     the 1-based sector
	 * @param superlayer 1 based superlayer [1..6]
	 * @param coords     holds 6*3 = 18 values [x1, y1, z1, ..., x6, y6, z6]
	 */
	public static void superLayerVertices(int sector, int superlayer, float[] coords) {

		Line3D wire1 = getWire(sector, superlayer, 1, 1);
		Line3D wire2 = getWire(sector, superlayer, 1, 112);

		Line3D wire3 = getWire(sector, superlayer, 6, 1);
		Line3D wire4 = getWire(sector, superlayer, 6, 112);

		Triangle3D triangle1 = new Triangle3D(wire1.midpoint(), wire2.origin(), wire2.end());
		Triangle3D triangle6 = new Triangle3D(wire3.midpoint(), wire4.origin(), wire4.end());

		if (triangle1 != null) {

			for (int i = 0; i < 3; i++) {
				Point3D v1 = new Point3D(triangle1.point(i));
				Point3D v6 = new Point3D(triangle6.point(i));

				int j = 3 * i;
				int k = j + 9;

				coords[j] = (float) v1.x();
				coords[j + 1] = (float) v1.y();
				coords[j + 2] = (float) v1.z();

				coords[k] = (float) v6.x();
				coords[k + 1] = (float) v6.y();
				coords[k + 2] = (float) v6.z();
			}
		}

	}

	/**
	 * Get the absolute value of the largest x coordinate of any wire.
	 *
	 * @return the absolute value of the largest x coordinate of any wire.
	 */
	public static double getAbsMaxWireX() {
		return Math.max(Math.abs(minWireX), maxWireX);
	}

	/**
	 * Get the midpoint of the untransformed wire in sector 1 NOTE: the indices are
	 * 1-based
	 *
	 * @param superlayer the superlayer [1..6]
	 * @param layer      the layer [1..6]
	 * @param wire       the wire [1..112]
	 * @return the mid point of the wire in sector 1
	 */
	public static Point3D getMidPoint(int superlayer, int layer, int wire) {
		return wires[superlayer - 1][layer - 1][wire - 1].getMidpoint();
	}

	/**
	 * Get the wire in given sector NOTE: the indices are 1-based
	 *
	 * @param sector     the 1-based sector [1..6]
	 * @param superlayer the superlayer [1..6]
	 * @param layer      the layer [1..6]
	 * @param wire       the wire [1..112]
	 * @return the wire transformed to the given sector
	 */
	public static Line3D getWire(int sector, int superlayer, int layer, int wire) {
		DriftChamberWire dcwire = getWire(superlayer, layer, wire);

		Line3D line = new Line3D(dcwire.getLine());
		if (sector > 1) {
			line.rotateZ(Math.toRadians(60 * (sector - 1)));
		}
		return line;
	}

	/**
	 * Get the wire in sector 0 NOTE: the indices are 1-based
	 *
	 * @param superlayer the superlayer [1..6]
	 * @param layer      the layer [1..6]
	 * @param wire       the wire [1..112]
	 * @return the untransformed wire in sector 0
	 */
	public static DriftChamberWire getWire(int superlayer, int layer, int wire) {
		if ((superlayer < 1) || (superlayer > 6)) {
			System.err.println("BAD HIPO DATA DCGeometry.getWire superlayer must be [1..6], was " + superlayer);
			return null;
		}
		if ((layer < 1) || (layer > 6)) {
			System.err.println("BAD HIPO DATA DCGeometry.getWire layer must be [1..6], was " + layer);
			return null;
		}
		if ((wire < 1) || (wire > 112)) {
			System.err.println("BAD HIPO DATA DCGeometry.getWire wire must be [1..112], was " + wire);
			return null;
		}
		return wires[superlayer - 1][layer - 1][wire - 1];
	}

	/**
	 * Get the origin of the wire in sector 0 NOTE: the indices are 1-based
	 *
	 * @param superlayer the superlayer [1..6]
	 * @param layer      the layer [1..6]
	 * @param wire       the wire [1..112]
	 * @return the origin (one end) of the wire in sector 0
	 */
	public static Point3D getOrigin(int superlayer, int layer, int wire) {
		return wires[superlayer - 1][layer - 1][wire - 1].getLine().origin();
	}

	/**
	 * Get the end of the wire in sector 0 NOTE: the indices are 1-based
	 *
	 * @param superlayer the superlayer [1..6]
	 * @param layer      the layer [1..6]
	 * @param wire       the wire [1..112]
	 * @return the end (one end) of the wire in sector 0
	 */
	public static Point3D getEnd(int superlayer, int layer, int wire) {
		return wires[superlayer - 1][layer - 1][wire - 1].getLine().end();
	}

	/**
	 * Get the intersections of a dcwire with a constant phi plane. If the wire does
	 * not intersect (happens as phi grows) return null;
	 *
	 * NOTE: the indices are 1-based
	 *
	 * @param superlayer      the superlayer [1..6]
	 * @param layer           the layer [1..6]
	 * @param wire            the wire [1..112]
	 * @param projectionPlane the projection plane
	 */
	public static boolean getHexagon(int superlayer, int layer, int wire, Plane3D projectionPlane, Point2D.Double wp[],
			Point2D.Double centroid) {

		DriftChamberWire dcw = DCGeometry.getWire(superlayer, layer, wire);
		if (dcw == null) {
			return false;
		}
		return GeometryManager.getProjectedPolygon(dcw, projectionPlane, 10, 6, wp, centroid, false);
	}

	/**
	 * Get the approximate center of the projected hexagon
	 *
	 * NOTE: the indices are 1-based
	 *
	 * @param superlayer  the superlayer [1..6]
	 * @param layer       the layer [1..6]
	 * @param wire        the wire [1..112]
	 * @param transform3D the transformation to the constant phi
	 * @return the approximate center of the projected hexagon
	 */
	public static Point2D.Double getCenter(int superlayer, int layer, int wire, Plane3D projectionPlane) {

		Point2D.Double centroid = new Point2D.Double();
//		projectionPlane = GeometryManager.constantPhiPlane(0);

		DriftChamberWire dcw = DCGeometry.getWire(superlayer, layer, wire);
		Line3D l3D = dcw.getLine();
		Point3D p3 = new Point3D();
		projectionPlane.intersection(l3D, p3);

		centroid.x = p3.z();
		centroid.y = Math.hypot(p3.x(), p3.y());

		return centroid;
	}

	/**
	 * Get a point on either side of a layer
	 *
	 * NOTE: the indices are 1-based
	 *
	 * @param superlayer  the superlayer [1..6]
	 * @param layer       the layer [1..6]
	 * @param wire        the wire [1..112]
	 * @param transform3D the transformation to the constant phi
	 * @param wp          on return will hold the two extended points. The 0 point
	 *                    will be to the "right" of wire 0. The 1 point will be to
	 *                    the left of wire 111.
	 */
	public static void getLayerExtendedPoints(int superLayer, int layer, Plane3D projectionPlane, Point2D.Double wp[]) {

		Point2D.Double hexagon[] = GeometryManager.allocate(6);

		getHexagon(superLayer, layer, 1, projectionPlane, hexagon, null);
		Point2D.Double first = new Point2D.Double(hexagon[0].x, hexagon[0].y);

		getHexagon(superLayer, layer, 2, projectionPlane, hexagon, null);
		Point2D.Double second = new Point2D.Double(hexagon[0].x, hexagon[0].y);

		getHexagon(superLayer, layer, 111, projectionPlane, hexagon, null);
		Point2D.Double nexttolast = new Point2D.Double(hexagon[0].x, hexagon[0].y);

		getHexagon(superLayer, layer, 112, projectionPlane, hexagon, null);
		Point2D.Double last = new Point2D.Double(hexagon[0].x, hexagon[0].y);

		extPoint(first, second, wp[0]);
		extPoint(last, nexttolast, wp[1]);

	}

	/**
	 * Get the boundary of a layer
	 *
	 * NOTE: the indices are 1-based
	 *
	 * @param superlayer  the superlayer [1..6]
	 * @param layer       the layer [1..6]
	 * @param transform3D the transformation to the constant phi
	 * @param wp          a four point layer boundary
	 */
	public static void getLayerPolygon(int superLayer, int layer, Plane3D plane, Point2D.Double wp[]) {

		Point2D.Double hex[] = GeometryManager.allocate(6);

		int firstWire = 1;
		while ((firstWire < 112) && !getHexagon(superLayer, layer, firstWire, plane, hex, null)) {
			firstWire++;
		}

		getHexagon(superLayer, layer, 1, plane, hex, null);

		/*
		 * The mappings of the old geo hex indices to the new is 0 --> 5 1 --> 4 2 --> 3
		 * 3 --> 2 4 --> 1 5 --> 0
		 */

		assignFromHex(wp, 0, hex, 5);
		assignFromHex(wp, 11, hex, 2);
		assignFromHex(wp, 12, hex, 1);
		assignFromHex(wp, 13, hex, 0);

		int sindex = Math.max(13, firstWire + 8);
		getHexagon(superLayer, layer, sindex, plane, hex, null);

		assignFromHex(wp, 1, hex, 5);
		assignFromHex(wp, 10, hex, 2);

		sindex = Math.max(57, sindex + 12);
		getHexagon(superLayer, layer, 57, plane, hex, null);

		assignFromHex(wp, 2, hex, 5);
		assignFromHex(wp, 9, hex, 2);

		sindex = Math.max(99, sindex + 29);
		getHexagon(superLayer, layer, 99, plane, hex, null);

		assignFromHex(wp, 3, hex, 5);
		assignFromHex(wp, 8, hex, 2);

		getHexagon(superLayer, layer, 112, plane, hex, null);

		assignFromHex(wp, 4, hex, 5);
		assignFromHex(wp, 5, hex, 4);
		assignFromHex(wp, 6, hex, 3);
		assignFromHex(wp, 7, hex, 2);
	}

	/**
	 * Get the boundary of a super layer
	 *
	 * NOTE: the indices are 1-based
	 *
	 * @param superlayer  the superlayer [1..6]
	 * @param transform3D the transformation to the constant phi
	 * @param wp          a four point super layer boundary
	 */
	public static void getSuperLayerPolygon(int superLayer, Plane3D projectionPlane, Point2D.Double wp[]) {

		Point2D.Double layBoundry[] = GeometryManager.allocate(14);
		getLayerPolygon(superLayer, 1, projectionPlane, layBoundry);
		wp[0].setLocation(layBoundry[12]);
		wp[1].setLocation(layBoundry[13]);
		wp[2].setLocation(layBoundry[0]);
		wp[3].setLocation(layBoundry[1]);
		wp[4].setLocation(layBoundry[2]);
		wp[5].setLocation(layBoundry[3]);
		wp[6].setLocation(layBoundry[4]);
		wp[7].setLocation(layBoundry[5]);
		wp[8].setLocation(layBoundry[6]);

		getLayerPolygon(superLayer, 2, projectionPlane, layBoundry);
		wp[9].setLocation(layBoundry[5]);
		wp[10].setLocation(layBoundry[6]);
		wp[32].setLocation(layBoundry[12]);
		wp[33].setLocation(layBoundry[13]);

		getLayerPolygon(superLayer, 3, projectionPlane, layBoundry);
		wp[11].setLocation(layBoundry[5]);
		wp[12].setLocation(layBoundry[6]);
		wp[30].setLocation(layBoundry[12]);
		wp[31].setLocation(layBoundry[13]);

		getLayerPolygon(superLayer, 4, projectionPlane, layBoundry);
		wp[13].setLocation(layBoundry[5]);
		wp[14].setLocation(layBoundry[6]);
		wp[28].setLocation(layBoundry[12]);
		wp[29].setLocation(layBoundry[13]);

		getLayerPolygon(superLayer, 5, projectionPlane, layBoundry);
		wp[15].setLocation(layBoundry[5]);
		wp[16].setLocation(layBoundry[6]);
		wp[26].setLocation(layBoundry[12]);
		wp[27].setLocation(layBoundry[13]);

		getLayerPolygon(superLayer, 6, projectionPlane, layBoundry);
		wp[17].setLocation(layBoundry[5]);
		wp[18].setLocation(layBoundry[6]);
		wp[19].setLocation(layBoundry[7]);
		wp[20].setLocation(layBoundry[8]);
		wp[21].setLocation(layBoundry[9]);
		wp[22].setLocation(layBoundry[10]);
		wp[23].setLocation(layBoundry[11]);
		wp[24].setLocation(layBoundry[12]);
		wp[25].setLocation(layBoundry[13]);

	}

	private static void assignFromHex(Point2D.Double wp[], int wpIndex, Point2D.Double hex[], int hexIndex) {

		hexIndex = hexIndex % 6;
		Point2D.Double p = new Point2D.Double(hex[hexIndex].x, hex[hexIndex].y);
		wp[wpIndex] = p;
	}

	// extend a point
	private static void extPoint(Point2D.Double p0, Point2D.Double p1, Point2D.Double ext) {

		if ((p0 == null) || (p1 == null) || (ext == null)) {
			System.err.println("null point in DCGeometry.extPoint");
			return;
		}

		ext.x = p0.x + (p0.x - p1.x);
		ext.y = p0.y + (p0.y - p1.y);
	}

	@Override
	public boolean readGeometry(Kryo kryo, Input input) {
		try {
			// Read the min and max wire x values.
			minWireX = input.readDouble();
			maxWireX = input.readDouble();

			// Read the dimensions of the wires array.
			int dim1 = input.readInt();
			wires = new DriftChamberWire[dim1][][];
			for (int i = 0; i < dim1; i++) {
				int dim2 = input.readInt();
				wires[i] = new DriftChamberWire[dim2][];
				for (int j = 0; j < dim2; j++) {
					int dim3 = input.readInt();
					wires[i][j] = new DriftChamberWire[dim3];
					for (int k = 0; k < dim3; k++) {
						wires[i][j][k] = kryo.readObjectOrNull(input, DriftChamberWire.class);
					}
				}
			}
			return true;
		} catch (Exception e) {
			System.err.println("DCGeometry: Error reading geometry cache: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean writeGeometry(Kryo kryo, Output output) {
		try {
			// Write the min and max wire x values.
			output.writeDouble(minWireX);
			output.writeDouble(maxWireX);

			// Write the dimensions of the wires 3D array.
			int dim1 = (wires != null) ? wires.length : 0;
			output.writeInt(dim1);
			for (int i = 0; i < dim1; i++) {
				int dim2 = (wires[i] != null) ? wires[i].length : 0;
				output.writeInt(dim2);
				for (int j = 0; j < dim2; j++) {
					int dim3 = (wires[i][j] != null) ? wires[i][j].length : 0;
					output.writeInt(dim3);
					for (int k = 0; k < dim3; k++) {
						kryo.writeObjectOrNull(output, wires[i][j][k], DriftChamberWire.class);
					}
				}
			}
			return true;
		} catch (Exception e) {
			System.err.println("DCGeometry: Error writing geometry cache: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

}
