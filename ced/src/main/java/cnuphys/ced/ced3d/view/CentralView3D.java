package cnuphys.ced.ced3d.view;

import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.view.ViewConfiguration;
import cnuphys.bCNU.view.VirtualView;
import cnuphys.ced.ced3d.CedPanel3D;
import cnuphys.ced.ced3d.CentralPanel3D;

public class CentralView3D extends CedView3D {

	public static final float xdist = 0f;
	public static final float ydist = 0f;
	public static final float zdist = -140f;

	private static final float thetax = 0f;
	private static final float thetay = 90f;
	private static final float thetaz = 90f;
	
	// view title
	private static final String TITLE = "Central 3D View";


	public CentralView3D() {
		super(TITLE, thetax, thetay, thetaz, xdist, ydist, zdist);
	}

	@Override
	protected CedPanel3D make3DPanel(float angleX, float angleY, float angleZ, float xDist, float yDist, float zDist) {

		CentralPanel3D panel = new CentralPanel3D(this, angleX, angleY, angleZ, xDist, yDist, zDist);
		panel.loadIdentityMatrix();
		panel.rotateY(180f);
		return panel;
	}
	
	
	/**
	 * Static method to construct the view. This is used by the view manager
	 * for lazy construction.
	 */
	public static CentralView3D construct(Object[] keyVals) {
		return new CentralView3D();
	}
	

	/**
	 * Static method to get the view configuration. This is used by the view manager
	 * for lazy construction.
	 */
	public static ViewConfiguration<CentralView3D> getConfiguration() {
		ViewConfiguration<CentralView3D> configuration = new ViewConfiguration<>(CentralView3D.class, true, 
				14, 0, 0, VirtualView.CENTER, getDefaultKeyVals());
		return configuration;
	}
	

	/**
	 * Get the default key values for the view. This is used by lazy creation
	 *
	 * @return the default key values for the view
	 */
	public static Object[] getDefaultKeyVals() {
		return new Object[] {PropertySupport.TITLE, TITLE};
	}


}
