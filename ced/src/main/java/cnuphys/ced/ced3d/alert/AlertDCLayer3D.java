package cnuphys.ced.ced3d.alert;

import java.awt.Color;

import org.jlab.io.base.DataEvent;

import com.jogamp.opengl.GLAutoDrawable;

import bCNU3D.Support3D;
import cnuphys.ced.ced3d.CedPanel3D;
import cnuphys.ced.ced3d.DetectorItem3D;
import cnuphys.ced.cedview.alert.AlertDCGeometryNumbering;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.geometry.alert.AlertGeometry;
import cnuphys.ced.geometry.alert.DCLayer;

public class AlertDCLayer3D extends DetectorItem3D {

	private static final boolean frame = true;

	// zero based sector (0)
	private final int _sector;

	// one based superlayer [0..4]
	private final int _superlayer;

	// one based layer [0..1]
	private final int _layer;

	// the vertices
	private float[] coords = new float[6];

	private DCLayer _dcLayer;


	/**
	 * Create an Alert paddle
	 * @param panel3D the 3D panel this is associated with
	 * @param sector 0-based sector [0..0]
	 * @param superlayer 0-based superlayer [0..4]
	 * @param layer 0-based layer [0..2]
	 */
	public AlertDCLayer3D(CedPanel3D panel3D, int sector, int superlayer, int layer) {
		//just to be confusing, these are 1-based
		super(panel3D);

		_sector = sector;
		_superlayer = superlayer;
		_layer = layer;

		_dcLayer = AlertGeometry.getDCLayer(sector, superlayer, layer);
	}


	@Override
	public void drawShape(GLAutoDrawable drawable) {
		Color color = Color.lightGray;

		for (int wire = 0; wire < _dcLayer.numWires; wire++) {
			_dcLayer.getWireCoords(wire, coords);
			Support3D.drawLine(drawable, coords, color, 0.5f*WIRELINEWIDTH);
		}
	}

	@Override
	public void drawData(GLAutoDrawable drawable) {
		// draw adc hits in red
		DataEvent dataEvent = ClasIoEventManager.getInstance().getCurrentEvent();
		if (dataEvent == null) {
			return;
		}

		if (dataEvent.hasBank("AHDC::adc")) {

			short component[] = _dataWarehouse.getShort("AHDC::adc", "component");
			if (component != null) {
				int count = component.length;
				if (count > 0) {
					Color color = Color.red;
					byte sector[] = _dataWarehouse.getByte("AHDC::adc", "sector");
					byte compLayer[] = _dataWarehouse.getByte("AHDC::adc", "layer");
					byte order[] = _dataWarehouse.getByte("AHDC::adc", "order");

					AlertDCGeometryNumbering adcGeom = new AlertDCGeometryNumbering();

					for (int i = 0; i < count; i++) {
						adcGeom.fromDataNumbering(sector[i], compLayer[i], component[i], order[i]);

						if ((adcGeom.sector == _sector) && (adcGeom.superlayer == _superlayer)
								&& (adcGeom.layer == _layer)) {
							_dcLayer.getWireCoords(adcGeom.component, coords);
							Support3D.drawLine(drawable, coords, color, 2*WIRELINEWIDTH);
						}
					}
				} //count >0
			} //component != null

		}
	}

	@Override
	protected boolean show() {
		return _cedPanel3D.showDC(_superlayer+1, _layer+1);	}

}
