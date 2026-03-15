package cnuphys.ced.ced3d.view;

import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.view.ViewConfiguration;
import cnuphys.bCNU.view.VirtualView;
import cnuphys.ced.ced3d.CedPanel3D;
import cnuphys.ced.ced3d.fmt.FMTPanel3D;

@SuppressWarnings("serial")
public class FMTView3D extends CedView3D {

	public static final float xdist = 0f;
	public static final float ydist = 0f;
	public static final float zdist = -60f;

	private static final float thetax = 0f;
	private static final float thetay = 90f;
	private static final float thetaz = 90f;

	//bank matches
	private static String _defMatches[] = {"FMT"};

	// view title
	private static final String TITLE = "FMT 3D View";

	public FMTView3D() {
		super(TITLE, thetax, thetay, thetaz, xdist, ydist, zdist);
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
	
	
	/**
	 * Static method to construct the view. This is used by the view manager
	 * for lazy construction.
	 */
	public static FMTView3D construct(Object[] keyVals) {
		return new FMTView3D();
	}
	

	/**
	 * Static method to get the view configuration. This is used by the view manager
	 * for lazy construction.
	 */
	public static ViewConfiguration<FMTView3D> getConfiguration() {
		ViewConfiguration<FMTView3D> configuration = new ViewConfiguration<>(FMTView3D.class, true, 
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
