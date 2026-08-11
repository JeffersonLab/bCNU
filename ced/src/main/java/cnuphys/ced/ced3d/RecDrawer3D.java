package cnuphys.ced.ced3d;
import java.awt.Color;

import com.jogamp.opengl.GLAutoDrawable;

import bCNU3D.Support3D;
import cnuphys.ced.alldata.RecCalorimeter;
import cnuphys.ced.frame.CedColors;
import item3D.Item3D;

public class RecDrawer3D extends Item3D {

	private static final float POINTSIZE = 5f;
	private CedPanel3D _cedPanel3D;

	private final RecCalorimeter recCalData = RecCalorimeter.getInstance();


	public RecDrawer3D(CedPanel3D panel3D) {
		super(panel3D);
		_cedPanel3D = panel3D;
	}

	@Override
	public void draw(GLAutoDrawable drawable) {
		if (_panel3D instanceof ForwardPanel3D) { // forward detectors

			//show any data from REC::Calorimiter?
			if (((ForwardPanel3D) _panel3D).showRecCal()) {
				showReconCalorimeter(drawable);
			}
		}
	}


	//show data from REC::Calorimeter
	private void showReconCalorimeter(GLAutoDrawable drawable) {

		if (_cedPanel3D.showECAL()) {
			for (int i = 0; i < recCalData.count(); i++) {
				if (!recCalData.isECal(i)) continue;
				float x = recCalData.x(i);
				float y = recCalData.y(i);
				float z = recCalData.z(i);
				Support3D.drawPoint(drawable, x, y, z, Color.black, POINTSIZE, true);
				float radius = recCalData.radius(i);
				if (radius > 0) {
					Support3D.solidSphere(drawable, x, y, z, radius, 40, 40, CedColors.RECCalFill);
				}
			} // end for
		}

		if (_cedPanel3D.showPCAL()) {
			for (int i = 0; i < recCalData.count(); i++) {
				if (!recCalData.isPCal(i)) continue;
				float x = recCalData.x(i);
				float y = recCalData.y(i);
				float z = recCalData.z(i);
				Support3D.drawPoint(drawable, x, y, z, Color.black, POINTSIZE, true);
				float radius = recCalData.radius(i);
				if (radius > 0) {
					Support3D.solidSphere(drawable, x, y, z, radius, 40, 40, CedColors.RECCalFill);
				}
			} // end for
		}

	}

}
