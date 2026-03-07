package cnuphys.ced.ced3d.fmt;

import java.awt.Color;

import org.jlab.io.base.DataEvent;

import com.jogamp.opengl.GLAutoDrawable;

import bCNU3D.Support3D;
import cnuphys.ced.ced3d.CedPanel3D;
import cnuphys.ced.ced3d.DetectorItem3D;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.geometry.fmt.FMTGeometry;
import cnuphys.lund.X11Colors;

public class FMTStrip3D extends DetectorItem3D {

	private final int _sector = 0;
	private final int _superlayer = 0;
	private int _layerId; // 0..5
	private int _stripId; // 0..1023

	// the cached vertices
	private float[] _coords = new float[24];

	// frame the paddle?
	private static boolean _frame = true;

	/**
	 * Create an FMT strip
	 *
	 * @param panel3D the 3D panel this is associated with
	 * @param layer   0-based layer [0..5]
	 * @param strip   0-based strip Id [0..1023]
	 */
	public FMTStrip3D(CedPanel3D panel3D, int sector, int superlayer, int layer, int strip) {
		// just to be confusing, these are 1-based
		super(panel3D);

		_layerId = layer;
		_stripId = strip;
		try {
			FMTGeometry.stripVertices(_sector, _superlayer, _layerId, _stripId, _coords);
		} catch (Exception e) {
			System.err.println(String.format("ERROR sector = %d  superlayer = %d  layer = %d   strip = %d", _sector, _superlayer, _layerId, _stripId));
			e.printStackTrace();
			System.exit(1);
		}

	}

	@Override
	public void drawShape(GLAutoDrawable drawable) {
		Color color = Color.gray;
		if ((_layerId % 2) == 0) {
			color = X11Colors.getX11Color("light yellow", getVolumeAlpha());
		} else {
			color = X11Colors.getX11Color("light green", getVolumeAlpha());
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

		if (dataEvent.hasBank("FMT::adc")) {

			short component[] = _dataWarehouse.getShort("FMT::adc", "component");
			if (component != null) {
				int count = component.length;
				if (count > 0) {
					Color color = X11Colors.getX11Color("orange", getVolumeAlpha());

					byte layer[] = _dataWarehouse.getByte("FMT::adc", "layer");

					for (int i = 0; i < count; i++) {

						int lm1 = layer[i]-1;
						int stripm1 = component[i]-1;
						if ((lm1 == _layerId) && (stripm1 == _stripId)) {

							Support3D.drawQuad(drawable, _coords, 0, 1, 2, 3, color, 1f, _frame);
							Support3D.drawQuad(drawable, _coords, 3, 7, 6, 2, color, 1f, _frame);
							Support3D.drawQuad(drawable, _coords, 0, 4, 7, 3, color, 1f, _frame);
							Support3D.drawQuad(drawable, _coords, 0, 4, 5, 1, color, 1f, _frame);
							Support3D.drawQuad(drawable, _coords, 1, 5, 6, 2, color, 1f, _frame);
							Support3D.drawQuad(drawable, _coords, 4, 5, 6, 7, color, 1f, _frame);

							return;
						}
					}
				} //count > 0
			}
		}
	}

	@Override
	protected boolean show() {
		FMTPanel3D _fmtPanel3D = (FMTPanel3D) _panel3D;
		boolean inRange = _fmtPanel3D.showStrip(_stripId+1);
		if (!inRange) {
			return false;
		}
		boolean showLayer = _fmtPanel3D.showFMTLayer(_layerId+1);
		if (!showLayer) {
			return false;
		}

		int region = FMTGeometry.getRegion(_stripId + 1);
		boolean showRegion = _fmtPanel3D.showFMTRegion(region);
		return showRegion;
	}
}
