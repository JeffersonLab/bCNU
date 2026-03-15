package cnuphys.ced.ced3d.view;

import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.view.ViewConfiguration;
import cnuphys.bCNU.view.VirtualView;
import cnuphys.ced.ced3d.CedPanel3D;
import cnuphys.ced.ced3d.ForwardPanel3D;

@SuppressWarnings("serial")
public class ForwardView3D extends CedView3D {

	public static final float xdist = -200f;
	public static final float ydist = 0f;
	public static final float zdist = -1600f;

	private static final float thetax = -90f;
	private static final float thetay = 0f;
	private static final float thetaz = -90f;
	
	// view title
	private static final String TITLE = "Forward 3D View";


	public ForwardView3D() {
		super(TITLE, thetax, thetay, thetaz, xdist, ydist, zdist);
	}

	@Override
	protected CedPanel3D make3DPanel(float angleX, float angleY, float angleZ, float xDist, float yDist, float zDist) {
		return new ForwardPanel3D(this, angleX, angleY, angleZ, xDist, yDist, zDist);
	}
	
	
	/**
	 * Static method to construct the view. This is used by the view manager
	 * for lazy construction.
	 */
	public static ForwardView3D construct(Object[] keyVals) {
		return new ForwardView3D();
	}
	
	/**
	 * Static method to get the view configuration. This is used by the view manager
	 * for lazy construction.
	 */
	public static ViewConfiguration<ForwardView3D> getConfiguration() {
		ViewConfiguration<ForwardView3D> configuration = new ViewConfiguration<>(ForwardView3D.class, true, 
				15, 0, 0, VirtualView.CENTER, getDefaultKeyVals());
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
