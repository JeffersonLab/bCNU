package cnuphys.ced.ced3d.urwt;

import java.awt.Color;
import java.util.List;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

import cnuphys.bCNU.util.X11Colors;
import cnuphys.ced.ced3d.DetectorItem3D;
import cnuphys.ced.ced3d.util.DrawSupport;
import cnuphys.ced.ced3d.util.Plane;
import cnuphys.ced.ced3d.util.PlaneHullUtility;
import cnuphys.ced.ced3d.util.Point;
import cnuphys.ced.geometry.urwt.UrWTDetectorData;
import cnuphys.ced.geometry.urwt.UrWTGeometry;

public class UrWTDetectorItem extends DetectorItem3D {

	// one based sector [1..6]
	private final int sector;

	// one based layer [1..4]
	private final int layer;

	private Plane plane;

	private UrWTDetectorData detectorData;

	private List<Point> convexHull;

	private static Color[] layerColors = new Color[] { X11Colors.getX11Color("dark red"),
			X11Colors.getX11Color("dark green"), X11Colors.getX11Color("dark blue"), X11Colors.getX11Color("orange") };
	
	private float[] coords;

	/**
	 * Create a UrWT detector item
	 * 
	 * @param panel3D the 3D panel this is associated with
	 * @param sector  1-based sector [1..6]
	 * @param layer   1-based layer [1..4]
	 */
	public UrWTDetectorItem(UrWTPanel3D panel3D, int sector, int layer) {
		super(panel3D);
		this.sector = sector;
		this.layer = layer;
		

		detectorData = UrWTGeometry.getDetectorData(sector, layer);
		plane = DrawSupport.findCommonPlane(detectorData.strips, 1e-6);

		if (plane == null) {
			System.err.println(
					"UrwtDetectorItem: Could not find common urwt plane for sector " + sector + " layer " + layer);
			return;
		}

		convexHull = PlaneHullUtility.getHullIfCoplanar(detectorData.strips, 1.0e-6);
		if (convexHull == null) {
			System.err.println(
					"UrwtDetectorItem: Could not compute convex hull for sector " + sector + " layer " + layer);
		}
		
		
		coords = new float[convexHull.size() * 3];
		for (int i = 0; i < convexHull.size(); i++) {
			Point p = convexHull.get(i);
			coords[3 * i] = (float) p.x;
			coords[3 * i + 1] = (float) p.y;
			coords[3 * i + 2] = (float) p.z;
		}
	}

	@Override
	public void drawShape(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		DrawSupport.drawPlaneAndHull(gl, plane, convexHull, 100.0f, layerColors[layer - 1], getVolumeAlpha());
	}

	@Override
	public void drawData(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub

	}

	@Override
	protected boolean show() {
		switch (layer) {
		case 1:
			return _cedPanel3D.showUrwtLayer1();

		case 2:
			return _cedPanel3D.showUrwtLayer2();

		case 3:
			return _cedPanel3D.showUrwtLayer3();

		case 4:
			return _cedPanel3D.showUrwtLayer4();

		default:
			return false;
		}
	}
}
