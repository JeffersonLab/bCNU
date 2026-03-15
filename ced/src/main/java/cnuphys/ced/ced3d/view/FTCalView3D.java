package cnuphys.ced.ced3d.view;

import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.view.ViewConfiguration;
import cnuphys.bCNU.view.VirtualView;
import cnuphys.ced.ced3d.CedPanel3D;
import cnuphys.ced.ced3d.ftcal.FTCalPanel3D;

@SuppressWarnings("serial")
public class FTCalView3D extends CedView3D {

	public static final float xdist = 0f;
	public static final float ydist = 0f;
	public static final float zdist = -100f;

	private static final float thetax = 0f;
	private static final float thetay = 90f;
	private static final float thetaz = 90f;
	
	private static final String TITLE = "FTCal 3D View";

	public FTCalView3D() {
		super(TITLE, thetax, thetay, thetaz, xdist, ydist, zdist);
	}

	@Override
	protected CedPanel3D make3DPanel(float angleX, float angleY, float angleZ, float xDist, float yDist, float zDist) {
		return new FTCalPanel3D(this, angleX, angleY, angleZ, xDist, yDist, zDist);
	}
	
	/**
	 * Static method to construct the view. This is used by the view manager
	 * for lazy construction.
	 */
	public static FTCalView3D construct(Object[] keyVals) {
		return new FTCalView3D();
	}
	

	/**
	 * Static method to get the view configuration. This is used by the view manager
	 * for lazy construction.
	 */
	public static ViewConfiguration<FTCalView3D> getConfiguration() {
		ViewConfiguration<FTCalView3D> configuration = new ViewConfiguration<>(FTCalView3D.class, true, 
				16, 0, 0, VirtualView.CENTER, getDefaultKeyVals());
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
