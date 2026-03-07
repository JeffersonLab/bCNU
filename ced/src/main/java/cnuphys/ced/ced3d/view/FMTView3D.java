package cnuphys.ced.ced3d.view;

import cnuphys.ced.ced3d.CedPanel3D;
import cnuphys.ced.ced3d.fmt.FMTPanel3D;

public class FMTView3D extends CedView3D {

	public static final float xdist = 0f;
	public static final float ydist = 0f;
	public static final float zdist = -60f;

	private static final float thetax = 0f;
	private static final float thetay = 90f;
	private static final float thetaz = 90f;

	//bank matches
	private static String _defMatches[] = {"FMT"};


	public FMTView3D() {
		super("FMT 3D View", thetax, thetay, thetaz, xdist, ydist, zdist);
		//i.e. if none were in the properties
		if (hasNoBankMatches()) {
			setBankMatches(_defMatches);
		}

		FMTPanel3D panel = (FMTPanel3D) _panel3D;
		panel.getMatchedBankPanel().update();

	}

	@Override
	protected CedPanel3D make3DPanel(float angleX, float angleY, float angleZ, float xDist, float yDist, float zDist) {

		FMTPanel3D panel = new FMTPanel3D(this, angleX, angleY, angleZ, xDist, yDist, zDist);
		panel.loadIdentityMatrix();
		panel.rotateY(180f);
		return panel;
	}

}
