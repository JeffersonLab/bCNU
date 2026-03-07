package cnuphys.ced.ced3d.alert;


import java.awt.Color;
import java.awt.Font;

import cnuphys.ced.ced3d.CedPanel3D;
import cnuphys.ced.ced3d.TrajectoryDrawer3D;
import cnuphys.ced.ced3d.view.CedView3D;
import cnuphys.ced.geometry.alert.AlertGeometry;
import cnuphys.ced.geometry.alert.DCLayer;
import cnuphys.lund.X11Colors;
import item3D.Axes3D;


public class AlertPanel3D extends CedPanel3D {


	// dimension of this panel are in mm
	private final float xymax = 150f;
	private final float zmax = 200f;
	private final float zmin = -20f;

	// labels for the check box
	private static final String _cbaLabels[] = { SHOW_VOLUMES, SHOW_TRUTH, SHOW_RECON_CROSSES, SHOW_TB_TRACK,
			SHOW_HB_TRACK, SHOW_COSMIC, SHOW_DC, SHOW_TOF,
			DC_SUPLAY1_LAY1, DC_SUPLAY2_LAY1, DC_SUPLAY2_LAY2, DC_SUPLAY3_LAY1, DC_SUPLAY3_LAY2, DC_SUPLAY4_LAY1,
			DC_SUPLAY4_LAY2, DC_SUPLAY5_LAY1,
			SHOW_SECT1, SHOW_SECT2, SHOW_SECT3, SHOW_SECT4, SHOW_SECT5, SHOW_SECT6, SHOW_SECT7, SHOW_SECT8,
			SHOW_SECT9, SHOW_SECT10, SHOW_SECT11, SHOW_SECT12, SHOW_SECT13, SHOW_SECT14, SHOW_SECT15,
			TOF_SUPLAY1, TOF_SUPLAY2,
			TOF_LAY1, TOF_LAY2, TOF_LAY3, TOF_LAY4};


	/**
	 *
	 * @param view
	 * @param angleX
	 * @param angleY
	 * @param angleZ
	 * @param xDist
	 * @param yDist
	 * @param zDist
	 */
	public AlertPanel3D(CedView3D view, float angleX, float angleY, float angleZ, float xDist, float yDist,
			float zDist) {
		super(view, angleX, angleY, angleZ, xDist, yDist, zDist, _cbaLabels);
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

		//dc layers
		for (int sectorId = 0; sectorId < 1; sectorId++) {
			for (int superlayerId = 0; superlayerId < 5; superlayerId++) {
				for (int layerId = 0; layerId < 2; layerId++) {
					DCLayer dcLayer = AlertGeometry.getDCLayer(sectorId, superlayerId, layerId);
					if ((dcLayer != null) && (dcLayer.numWires > 0)) {
						AlertDCLayer3D dc = new AlertDCLayer3D(this, sectorId, superlayerId, layerId);
						addItem(dc);
					}
				}
			}
		}


		// tof paddles
		for (int sectorId = 0; sectorId < 15; sectorId++) {
			for (int superlayerId = 0; superlayerId < 2; superlayerId++) {
				int numPaddle = (superlayerId == 0) ? 1 : 10;
				for (int layerId = 0; layerId < 4; layerId++) {

					for (int index = numPaddle - 1; index >= 0; index--) {
						AlertPaddle3D tof = new AlertPaddle3D(this, sectorId, superlayerId, layerId, index);
						addItem(tof);
					}
				}
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
		return (zmax - zmin) / 50f;
	}
}
