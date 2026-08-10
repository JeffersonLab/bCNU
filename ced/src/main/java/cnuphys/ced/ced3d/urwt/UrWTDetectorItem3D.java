package cnuphys.ced.ced3d.urwt;

import java.awt.Color;
import java.util.List;

import org.jlab.geom.prim.Line3D;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

import bCNU3D.Support3D;
import cnuphys.ced.ced3d.DetectorItem3D;
import cnuphys.ced.ced3d.util.DrawSupport;
import cnuphys.ced.ced3d.util.Point;
import cnuphys.ced.cedview.urwt.UrWTDetectorItem;
import cnuphys.ced.alldata.URWTHits;
import cnuphys.ced.geometry.urwt.UrWTDetectorData;
import cnuphys.ced.geometry.urwt.UrWTGeometry;

public class UrWTDetectorItem3D extends DetectorItem3D {

	// the color for the frame of the detector
	private Color frameColor = Color.darkGray;
	
	/**
	 * The sector of this detector item, stored as 1-based values to 
	 * match the database and geometry conventions. The sector is in the range 
	 * [1..6].
	 */
	public final int sector; // 1-based [1..6]
	
	/**
	 * The layer of this detector item, stored as 1-based values to match the 
	 * database and geometry conventions. The layer is in the range [1..4].
	 */
	public final int layer;  // 1-based [1..4]
	
	// the color for this layer, used for drawing the detector shape and hits.
	private final Color layerColor;

	// the data for this detector, including the strip lines and the 
	// precomputed convex hull of the strip endpoints, which is used for 
	// drawing the detector
	private UrWTDetectorData detectorData;

	// the convex hull points of the strip endpoints, used for 
	// drawing the detector shape
	private List<Point> convexHull;

	
	// the coordinates of the convex hull points, stored as a flat array for 
	// efficient drawing in 3D with OpenGL. Each group of three floats corresponds 
	// to the x, y, z coordinates of a point.
	private float[] coords;

	/**
	 * Create a UrWT detector item
	 * 
	 * @param panel3D the 3D panel this is associated with
	 * @param sector  1-based sector [1..6]
	 * @param layer   1-based layer [1..4]
	 */
	public UrWTDetectorItem3D(UrWTPanel3D panel3D, int sector, int layer) {
		super(panel3D);
		this.sector = sector;
		this.layer = layer;

		detectorData = UrWTGeometry.getDetectorData(sector, layer);

		convexHull = detectorData.getConvexHull();
		
		layerColor = UrWTDetectorItem.layerColors[layer - 1];
		
		// get the OpenGL-friendly coordinates for the convex hull points
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
		DrawSupport.drawHull(gl, convexHull, 100.0f, 
				layerColor, 
				frameColor, getVolumeAlpha());
	}

	@Override
	public void drawData(GLAutoDrawable drawable) {
			URWTHits hits = URWTHits.getInstance();
			
			if (!show()) return;

			for (int i = 0; i < hits.count(); i++) {
				if (!hits.hasValidGeometry(i)
						|| hits.sector(i) != sector || hits.layer(i) != layer) continue;
				
				Line3D stripLine = UrWTGeometry.getStrip(hits.sector(i), hits.layer(i), hits.strip(i));
				
				float x1 = (float) stripLine.origin().x();
				float y1 = (float) stripLine.origin().y();
				float z1 = (float) stripLine.origin().z();
				float x2 = (float) stripLine.end().x();
				float y2 = (float) stripLine.end().y();
				float z2 = (float) stripLine.end().z();		
				
				Support3D.drawLine(drawable, x1, y1, z1,
						x2, y2, z2,
						layerColor, 1f);
			}
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
