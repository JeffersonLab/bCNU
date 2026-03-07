package cnuphys.ced.ced3d.view;

import cnuphys.ced.ced3d.CedPanel3D;
import cnuphys.ced.ced3d.alert.AlertPanel3D;

public class AlertView3D extends CedView3D {

	public static final float xdist = 0f;
	public static final float ydist = 0f;
	public static final float zdist = -400f;

	private static final float thetax = 0f;
	private static final float thetay = 90f;
	private static final float thetaz = 90f;

	public AlertView3D() {
		super("ALERT 3D View", thetax, thetay, thetaz, xdist, ydist, zdist);
	}

	@Override
	protected CedPanel3D make3DPanel(float angleX, float angleY, float angleZ, float xDist, float yDist, float zDist) {

		AlertPanel3D panel = new AlertPanel3D(this, angleX, angleY, angleZ, xDist, yDist, zDist);
		panel.loadIdentityMatrix();
		panel.rotateY(180f);
		return panel;
	}

}
