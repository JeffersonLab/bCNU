package cnuphys.ced.ced3d.fmt;

import java.awt.Color;
import java.awt.Font;

import cnuphys.bCNU.util.X11Colors;
import cnuphys.ced.ced3d.CedPanel3D;
import cnuphys.ced.ced3d.TrajectoryDrawer3D;
import cnuphys.ced.ced3d.view.CedView3D;
import cnuphys.ced.component.MatchedBankPanel;
import cnuphys.ced.component.RangePanel;
import item3D.Axes3D;

@SuppressWarnings("serial")
public class FMTPanel3D extends CedPanel3D {

	// dimension of this panel are in mm
	private final float xymax = 25f;
	private final float zmax = 50f;
	private final float zmin = 0;

	private RangePanel _rangePanel;

	// labels for the check box
	private static final String _cbaLabels[] = { SHOW_VOLUMES, SHOW_TRUTH, SHOW_RECON_CROSSES, SHOW_TB_TRACK,
			SHOW_HB_TRACK, SHOW_COSMIC, SHOW_FMT_LAYER_1, SHOW_FMT_LAYER_2, SHOW_FMT_LAYER_3, SHOW_FMT_LAYER_4,
			SHOW_FMT_LAYER_5, SHOW_FMT_LAYER_6, SHOW_FMT_REGION_1, SHOW_FMT_REGION_2, SHOW_FMT_REGION_3,
			SHOW_FMT_REGION_4};

	public FMTPanel3D(CedView3D view3D, float angleX, float angleY, float angleZ, float xDist, float yDist,
			float zDist) {
		super(view3D, angleX, angleY, angleZ, xDist, yDist, zDist, _cbaLabels);

		_rangePanel = new RangePanel(view3D, "Full Range: 1-1024", "1-1024");

		_matchedBankPanel = new MatchedBankPanel(view3D, view3D);
		getEastPanel().add(_rangePanel);
		getEastPanel().add(_matchedBankPanel);
	}

	public MatchedBankPanel getMatchedBankPanel() {
		return _matchedBankPanel;
	}

	@Override
	public void createInitialItems() {
		// coordinate axes
		Axes3D axes = new Axes3D(this, -xymax, xymax, -xymax, xymax, zmin, zmax, null, Color.darkGray, 1f, 6, 6, 6,
				Color.black, X11Colors.getX11Color("Dark Green"), new Font("SansSerif", Font.PLAIN, 12), 0);
		addItem(axes);

		// trajectory drawer
		TrajectoryDrawer3D trajDrawer = new TrajectoryDrawer3D(this);
		addItem(trajDrawer);

		//the layers
		for (int layer = 0; layer < 6; layer++) {
			for (int strip = 0; strip < 1024; strip++) {
				FMTStrip3D fmt = new FMTStrip3D(this, layer, strip);
				addItem(fmt);
			}
		}
	}

	/**
	 * This gets the z step used by the mouse and key adapters, to see how fast we
	 * move in or in in response to mouse wheel or up/down arrows. It should be
	 * overridden to give something sensible. like the scale/100;
	 *
	 * @return the z step (changes to zDist) for moving in and out
	 */
	@Override
	public float getZStep() {
		return (zmax - zmin) / 25f;
	}


	/**
     * Show the strip
     *
     * @param strip the 1-based strip to show
     * @return <code>true</code> if the strip
     */
	public boolean showStrip(int strip) {
		return _rangePanel.showStrip(strip);
	}


}
