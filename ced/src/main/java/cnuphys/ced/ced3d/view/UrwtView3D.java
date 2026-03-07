package cnuphys.ced.ced3d.view;

import cnuphys.bCNU.util.UnicodeSupport;
import cnuphys.ced.ced3d.CedPanel3D;
import cnuphys.ced.ced3d.urwt.UrWTPanel3D;

public class UrwtView3D extends CedView3D {
	public static final float xdist = 0f;
	public static final float ydist = 0f;
	public static final float zdist = -450f;

	private static final float thetax = 0f;
	private static final float thetay = 90f;
	private static final float thetaz = 90f;

	public UrwtView3D() {
		super(UnicodeSupport.SMALL_MU + "RWT 3D View", thetax, thetay, thetaz, xdist, ydist, zdist);
	}

	@Override
	protected CedPanel3D make3DPanel(float angleX, float angleY, float angleZ, float xDist, float yDist, float zDist) {

		UrWTPanel3D panel = new UrWTPanel3D(this, angleX, angleY, angleZ, xDist, yDist, zDist);
		panel.loadIdentityMatrix();
		panel.rotateY(180f);
		return panel;
	}

}
