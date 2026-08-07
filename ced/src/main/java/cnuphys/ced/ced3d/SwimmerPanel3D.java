package cnuphys.ced.ced3d;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import cnuphys.CLAS12Swim.geometry.Plane;
import cnuphys.ced.ced3d.view.PlainView3D;
import cnuphys.ced.ced3d.view.SwimmingTestView3D;
import item3D.Cylinder;
import item3D.Item3D;
import item3D.Quad3D;

public class SwimmerPanel3D extends PlainPanel3D  {

	// the control panel
	private SwimmerControlPanel _controlPanel;

	// changing display item
	private Item3D _displayItem;
	private Color _displayItemColor = new Color(0, 0, 0, 24);

	private static final String _cbaLabels[] = { SHOW_VOLUMES, SHOW_SECTOR_1, SHOW_SECTOR_2, SHOW_SECTOR_3,
			SHOW_SECTOR_4, SHOW_SECTOR_5, SHOW_SECTOR_6, SHOW_DC, SHOW_ECAL, SHOW_PCAL, SHOW_FTOF, SHOW_MAP_EXTENTS };

	public SwimmerPanel3D(PlainView3D view, float angleX, float angleY, float angleZ, float xDist, float yDist,
			float zDist) {
		super(view, angleX, angleY, angleZ, xDist, yDist, zDist, 0.92f, 0.92f, 0.92f, _cbaLabels);

		for (String s : _cbaLabels) {
			AbstractButton ab = _checkBoxArray.getButton(s);
			ab.setSelected(false);
		}

		// trajectory drawer
		SwimResultDrawer trajDrawer = new SwimResultDrawer(this);
		addItem(trajDrawer);

	}

	// add north panel
	@Override
	protected void addNorth() {
		JPanel np = new JPanel();
		np.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));

		JLabel label = new JLabel("This is a stand-alone view independent of any physics events.");

		np.add(label);

		np.add(_volumeAlphaSlider, BorderLayout.EAST);
		add(np, BorderLayout.NORTH);
	}

	// add west panel
	@Override
	protected void addWest() {
		_controlPanel = new SwimmerControlPanel((SwimmingTestView3D) _view, this);

		add(_controlPanel, BorderLayout.WEST);
	}

	// add south panel
	@Override
	protected void addSouth() {
	}

	/**
	 * Remove the display item
	 */
	public void removeDisplayItem() {
		if (_displayItem != null) {
			removeItem(_displayItem);
		}

		_displayItem = null;
	}

	/**
	 * Set the display item to a constant z plane,
	 * which is in fact a cylinder centered on the z axis.
	 * @param rho
	 */
	public void setDisplayItemConstantZ(float z) {
		_displayItem = Quad3D.constantZQuad(this, z, 1000, _displayItemColor, 1f, true);
		addItem(_displayItem);

	}

	/**
	 * Set the display item to a constant rho cylinder,
	 * which is in fact a cylinder centered on the z axis.
	 * @param rho
	 */
	public void setDisplayItemConstantRho(float rho) {
		_displayItem = new Cylinder(this, 0f, 0f, -100f, 0f, 0f, 1500f, rho, _displayItemColor);
		addItem(_displayItem);

	}

	/**
	 * Set the display item to a plane
	 * @param normal normal to the plane
	 * @param point a point in the plane
	 */
	public void setDisplayItemPlane(double[] normal, double[] point) {

		Plane plane = new Plane(normal, point);
		float coords[] = plane.planeQuadCoordinates(1000);
		_displayItem = new Quad3D(this, coords, _displayItemColor, 1f, true);
		addItem(_displayItem);
	}

	/**
	 * Set the display item to a cylinder
	 * @param p1 one point of the end line
	 * @param p2 another point of the end line
	 * @param radius the radius of the cylinder
	 */
	public void setDisplayItemCylinder(double[] p1, double[] p2, double radius) {

		float data[] = new float[7];
		for (int i = 0; i < 3; i++) {
			data[i] = (float)p1[i];
			data[i+3] = (float)p2[i];
		}
		data[6] = (float)radius;

		Cylinder cylinder = new Cylinder(this, data, _displayItemColor);
		cylinder.setExtend(true);
		cylinder.setExtensionFactor(2);


		_displayItem = cylinder;
		addItem(_displayItem);
	}


}
