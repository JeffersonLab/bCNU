package cnuphys.ced.ced3d;

import java.awt.Color;

import com.jogamp.opengl.GLAutoDrawable;

import bCNU3D.Support3D;
import cnuphys.ced.alldata.DCCrosses;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.frame.CedColors;
import item3D.Item3D;

public class CrossDrawer3D extends Item3D {

	protected static final float CROSS_LEN = 30f; // in cm
	protected static final double COS_TILT = Math.cos(Math.toRadians(25.));
	protected static final double SIN_TILT = Math.sin(Math.toRadians(25.));

	private CedPanel3D _cedPanel3D;

	//data containers
	private DCCrosses _hbCrossData = DCCrosses.hitBased();
	private DCCrosses _tbCrossData = DCCrosses.timeBased();
	private DCCrosses _hbAICrossData = DCCrosses.aiHitBased();
	private DCCrosses _tbAICrossData = DCCrosses.aiTimeBased();


	public CrossDrawer3D(CedPanel3D panel3D) {
		super(panel3D);
		_cedPanel3D = panel3D;
	}

	@Override
	public void draw(GLAutoDrawable drawable) {

		if (ClasIoEventManager.getInstance().isAccumulating()) {
			return;
		}

		DCCrosses crossData = null;
		if (_cedPanel3D.showHBCross()) {
			crossData = _hbCrossData;
			drawCrossData(drawable, crossData, CedColors.HB_COLOR);
		}
		if (_cedPanel3D.showTBCross()) {
            crossData = _tbCrossData;
            drawCrossData(drawable, crossData, CedColors.TB_COLOR);
		}
		if (_cedPanel3D.showAIHBCross()) {
			crossData = _hbAICrossData;
			drawCrossData(drawable, crossData, CedColors.AIHB_COLOR);
		}
 		if (_cedPanel3D.showAITBCross()) {
 			crossData = _tbAICrossData;
            drawCrossData(drawable, crossData, CedColors.AITB_COLOR);
 		}

	}

	private void drawCrossData(GLAutoDrawable drawable, DCCrosses crossData, Color color) {
		if (crossData == null) {
			return;
		}

		for (int i = 0; i < crossData.count(); i++) {
			float[] p3d0 = new float[3];
			float[] p3d1 = new float[3];

			tiltedToSector(crossData.x(i), crossData.y(i), crossData.z(i), p3d0);
			float x = p3d0[0];
			float y = p3d0[1];
			float z = p3d0[2];

			float tx = crossData.x(i) + CROSS_LEN * crossData.directionX(i);
			float ty = crossData.y(i) + CROSS_LEN * crossData.directionY(i);
			float tz = crossData.z(i) + CROSS_LEN * crossData.directionZ(i);
			tiltedToSector(tx, ty, tz, p3d1);

			Support3D.drawLine(drawable, x, y, z, p3d1[0], p3d1[1], p3d1[2], Color.black, 3f);
			Support3D.drawLine(drawable, x, y, z, p3d1[0], p3d1[1], p3d1[2], Color.gray, 1f);

			Support3D.drawPoint(drawable, x, y, z, Color.black, 13, true);
			Support3D.drawPoint(drawable, x, y, z, color, 11, true);
		}

	}

	/**
	 * Convert tilted sector coordinates to sector coordinates. The two vectors can
	 * be the same in which case it is overwritten.
	 *
	 * @param tiltedXYZ will hold the tilted coordinates
	 * @param sectorXYZ the sector coordinates
	 */
	public void tiltedToSector(float tiltx, float tilty, float tiltz, float[] sectorXYZ) {

		double sectx = tiltx * COS_TILT + tiltz * SIN_TILT;
		double secty = tilty;
		double sectz = -tiltx * SIN_TILT + tiltz * COS_TILT;

		sectorXYZ[0] = (float) sectx;
		sectorXYZ[1] = (float) secty;
		sectorXYZ[2] = (float) sectz;
	}

}
