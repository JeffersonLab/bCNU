package cnuphys.ced.ced3d;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import cnuphys.ced.ced3d.view.PlainView3D;
import cnuphys.ced.component.MatchedBankPanel;

public class CedPanel3D extends PlainPanel3D {


	// show what particles are present
	private PIDLegend _pidLegend;

	//for matching banks
	protected MatchedBankPanel _matchedBankPanel;


	/*
	 * The panel that holds the 3D objects
	 *
	 * @param angleX the initial x rotation angle in degrees
	 *
	 * @param angleY the initial y rotation angle in degrees
	 *
	 * @param angleZ the initial z rotation angle in degrees
	 *
	 * @param xdist move viewpoint left/right
	 *
	 * @param ydist move viewpoint up/down
	 *
	 * @param zdist the initial viewer z distance should be negative
	 */
	public CedPanel3D(PlainView3D view, float angleX, float angleY, float angleZ,
			float xDist, float yDist, float zDist, String... cbaLabels) {
		super(view, angleX, angleY, angleZ, xDist, yDist, zDist, 0.92f, 0.92f, 0.92f, cbaLabels);
	}

	// add north panel
	@Override
	protected void addNorth() {
		JPanel np = new JPanel();
		np.setLayout(new BorderLayout(20, 0));

		_pidLegend = new PIDLegend(this);

		np.add(_volumeAlphaSlider, BorderLayout.EAST);
		np.add(_pidLegend, BorderLayout.CENTER);
		add(np, BorderLayout.NORTH);
	}

	// add west panel
	@Override
	protected void addWest() {
	}

	// add south panel
	@Override
	protected void addSouth() {
	}

	@Override
	public void refresh() {
		super.refresh();
		if (_pidLegend != null) {
			_pidLegend.repaint();
		}
	}



}
