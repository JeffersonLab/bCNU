package cnuphys.ced.ced3d;



import java.awt.Color;

import org.jlab.io.base.DataEvent;

import com.jogamp.opengl.GLAutoDrawable;

import bCNU3D.Support3D;
import cnuphys.ced.alldata.RecCalorimeter;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.frame.CedColors;
import item3D.Item3D;

public class RecDrawer3D extends Item3D {

	// the event manager
	ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();

	//the current event
	private DataEvent _currentEvent;

	private static final float POINTSIZE = 5f;
	private CedPanel3D _cedPanel3D;

//data containers
	RecCalorimeter ecRecData = RecCalorimeter.getInstance();
	RecCalorimeter pcalRecData = RecCalorimeter.getInstance();


	public RecDrawer3D(CedPanel3D panel3D) {
		super(panel3D);
		_cedPanel3D = panel3D;
	}

	@Override
	public void draw(GLAutoDrawable drawable) {

		_currentEvent = _eventManager.getCurrentEvent();
		if (_currentEvent == null) {
			return;
		}

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
			for (int i = 0; i < ecRecData.count(); i++) {
				if (!ecRecData.isECal(i)) continue;
				float x = ecRecData.x(i);
				float y = ecRecData.y(i);
				float z = ecRecData.z(i);
				Support3D.drawPoint(drawable, x, y, z, Color.black, POINTSIZE, true);
				float radius = ecRecData.radius(i);
				if (radius > 0) {
					Support3D.solidSphere(drawable, x, y, z, radius, 40, 40, CedColors.RECCalFill);
				}
			} // end for
		}

		if (_cedPanel3D.showPCAL()) {
			for (int i = 0; i < pcalRecData.count(); i++) {
				if (!pcalRecData.isPCal(i)) continue;
				float x = pcalRecData.x(i);
				float y = pcalRecData.y(i);
				float z = pcalRecData.z(i);
				Support3D.drawPoint(drawable, x, y, z, Color.black, POINTSIZE, true);
				float radius = pcalRecData.radius(i);
				if (radius > 0) {
					Support3D.solidSphere(drawable, x, y, z, radius, 40, 40, CedColors.RECCalFill);
				}
			} // end for
		}

	}

}
