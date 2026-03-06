package cnuphys.ced.ced3d.alert;

import java.awt.Color;

import org.jlab.io.base.DataEvent;

import com.jogamp.opengl.GLAutoDrawable;

import bCNU3D.Support3D;
import cnuphys.ced.ced3d.CedPanel3D;
import cnuphys.ced.ced3d.DetectorItem3D;
import cnuphys.ced.cedview.alert.AlertTOFGeometryNumbering;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.geometry.alert.AlertGeometry;
import cnuphys.lund.X11Colors;

public class AlertPaddle3D extends DetectorItem3D {

	//0-based sector [0..14]
	private final int _sectorId;

	//0-based superlayer [0..1]
	private final int _superlayerId;

	//0-based layer 0, 0--9
	private final int _layerId;

	//0-based paddle [ 0..9]
	// this is the index not the componentID
	private final int _paddleIndex;

	// the cached vertices
	private float[] _coords = new float[24];

	// frame the paddle?
	private static boolean _frame = true;


	/**
	 * Create an Alert paddle
	 * @param panel3D the 3D panel this is associated with
	 * @param sector 0-based sector [0..14]
	 * @param superlayer 0-based superlayer [0..1]
	 * @param layer 0-based layer [0..3]
	 * @param paddle 0-based paddle Id [0..9]
	 */
	public AlertPaddle3D(CedPanel3D panel3D, int sector, int superlayer, int layer, int paddle) {
		//just to be confusing, these are 1-based
		super(panel3D);

		_sectorId = sector;
		_superlayerId = superlayer;
		_layerId = layer;
		_paddleIndex = paddle;

		AlertGeometry.paddleVertices(_sectorId, _superlayerId, _layerId, _paddleIndex, _coords);
	}

	@Override
	public void drawShape(GLAutoDrawable drawable) {
		Color color = Color.gray;
		if (_superlayerId == 0) {
			color = X11Colors.getX11Color("light cyan", getVolumeAlpha());
		} else {
			if ((_layerId % 2) == 0) {
				color = X11Colors.getX11Color("light yellow", getVolumeAlpha());
			} else {
				color = X11Colors.getX11Color("light green", getVolumeAlpha());
			}
		}

		Support3D.drawQuad(drawable, _coords, 0, 1, 2, 3, color, 1f, _frame);
		Support3D.drawQuad(drawable, _coords, 3, 7, 6, 2, color, 1f, _frame);
		Support3D.drawQuad(drawable, _coords, 0, 4, 7, 3, color, 1f, _frame);
		Support3D.drawQuad(drawable, _coords, 0, 4, 5, 1, color, 1f, _frame);
		Support3D.drawQuad(drawable, _coords, 1, 5, 6, 2, color, 1f, _frame);
		Support3D.drawQuad(drawable, _coords, 4, 5, 6, 7, color, 1f, _frame);
	}

	@Override
	public void drawData(GLAutoDrawable drawable) {
		// draw adc hits in red
		DataEvent dataEvent = ClasIoEventManager.getInstance().getCurrentEvent();
		if (dataEvent == null) {
			return;
		}

		if (dataEvent.hasBank("ATOF::tdc")) {

			short component[] = _dataWarehouse.getShort("ATOF::tdc", "component");
			if (component != null) {
				int count = component.length;
				if (count > 0) {
					Color color = Color.red;

					byte sector[] = _dataWarehouse.getByte("ATOF::tdc", "sector");
					byte layer[] = _dataWarehouse.getByte("ATOF::tdc", "layer");
					byte order[] = _dataWarehouse.getByte("ATOF::tdc", "order");

					AlertTOFGeometryNumbering tdcGeom = new AlertTOFGeometryNumbering();

					for (int i = 0; i < count; i++) {
						tdcGeom.fromHipoNumbering(sector[i], layer[i], component[i], order[i]);

						if ((tdcGeom.sector == _sectorId) && (tdcGeom.superlayer == _superlayerId)
								&& (tdcGeom.layer == _layerId) && ((tdcGeom.paddleIndex) == _paddleIndex)) {

							Support3D.drawQuad(drawable, _coords, 0, 1, 2, 3, color, 1f, _frame);
							Support3D.drawQuad(drawable, _coords, 3, 7, 6, 2, color, 1f, _frame);
							Support3D.drawQuad(drawable, _coords, 0, 4, 7, 3, color, 1f, _frame);
							Support3D.drawQuad(drawable, _coords, 0, 4, 5, 1, color, 1f, _frame);
							Support3D.drawQuad(drawable, _coords, 1, 5, 6, 2, color, 1f, _frame);
							Support3D.drawQuad(drawable, _coords, 4, 5, 6, 7, color, 1f, _frame);


							return;
						}
					}
				}
			}


		}
	}

	@Override
	protected boolean show() {
		return _cedPanel3D.showTOF(_sectorId +1, _superlayerId+1, _layerId+1);
	}

}
