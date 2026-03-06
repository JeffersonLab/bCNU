package cnuphys.ced.ced3d.urwt;

import java.awt.Color;
import java.awt.Font;

import cnuphys.ced.ced3d.CedPanel3D;
import cnuphys.ced.ced3d.TrajectoryDrawer3D;
import cnuphys.ced.ced3d.view.CedView3D;
import cnuphys.lund.X11Colors;
import item3D.Axes3D;

public class UrWTPanel3D extends CedPanel3D {
	
	// dimension of this panel are in cm
	private final float xmin = -200f;
	private final float xmax = 200f;
	private final float ymin = -200f;
	private final float ymax = 200f;
	private final float zmin = 150f;
	private final float zmax = 250f;

	
	// labels for the check box
	private static final String _cbaLabels[] = { SHOW_VOLUMES, SHOW_TRUTH, SHOW_RECON_CROSSES, SHOW_TB_TRACK,
			SHOW_HB_TRACK, SHOW_COSMIC, SHOW_URWT_LAYER_1, SHOW_URWT_LAYER_2, SHOW_URWT_LAYER_3, SHOW_URWT_LAYER_4};


	public UrWTPanel3D(CedView3D view, float angleX, float angleY, float angleZ, float xDist, float yDist,
			float zDist) {
		super(view, angleX, angleY, angleZ, xDist, yDist, zDist, _cbaLabels);
	}

	@Override
	public void createInitialItems() {
		// coordinate axes
		Axes3D axes = new Axes3D(this, xmin, xmax, ymin, ymax, zmin, zmax, null, Color.darkGray, 1f, 6, 6, 6,
				Color.black, X11Colors.getX11Color("Dark Green"), new Font("SansSerif", Font.PLAIN, 12), 0);
		addItem(axes);

		// trajectory drawer
		TrajectoryDrawer3D trajDrawer = new TrajectoryDrawer3D(this);
		addItem(trajDrawer);
		
		//detector items
		for (int sector = 1; sector <= 6; sector++) {
			for (int layer = 1; layer <= 4; layer++) {
				UrWTDetectorItem item = new UrWTDetectorItem(this, sector, layer);
				addItem(item);
			}
		}
	}
}
