package cnuphys.ced.ced3d.cnd;

import java.awt.Color;
import java.util.Arrays;

import com.jogamp.opengl.GLAutoDrawable;

import cnuphys.bCNU.util.X11Colors;
import cnuphys.ced.alldata.CNDAdc;
import cnuphys.ced.ced3d.CedPanel3D;
import cnuphys.ced.ced3d.DetectorItem3D;
import cnuphys.ced.geometry.CNDGeometry;

public class CNDLayer3D extends DetectorItem3D {

	// 1-based layer 1..3
	private final int _layer;

	// the paddles
	private CNDPaddle3D _paddles[];
	private final CNDAdc _adcData = CNDAdc.getInstance();

	public CNDLayer3D(CedPanel3D panel3D, int layer) {
		super(panel3D);
		_layer = layer;

		_paddles = new CNDPaddle3D[48];
		for (int paddleId = 1; paddleId <= 48; paddleId++) {
			_paddles[paddleId - 1] = new CNDPaddle3D(layer, paddleId);
		}
	}

	@Override
	public void drawShape(GLAutoDrawable drawable) {
		Color outlineColor = X11Colors.getX11Color("Medium Spring Green", getVolumeAlpha());

		for (int paddleId = 1; paddleId <= 48; paddleId++) {
			getPaddle(paddleId).drawPaddle(drawable, outlineColor);
		}
	}

	@Override
	public void drawData(GLAutoDrawable drawable) {
		if (!_cedPanel3D.showCNDHits()) {
			return;
		}

		int[] bestRows = new int[49];
		Arrays.fill(bestRows, -1);
		for (int row = 0; row < _adcData.count(); row++) {
			if (_adcData.layer(row) != _layer || _adcData.adc(row) <= 0) {
				continue;
			}
			int paddleId = geometryPaddle(_adcData.sector(row), _layer, _adcData.component(row));
			if (paddleId >= 1 && paddleId <= 48
					&& (bestRows[paddleId] < 0 || _adcData.adc(row) > _adcData.adc(bestRows[paddleId]))) {
				bestRows[paddleId] = row;
			}
		}

		for (int paddleId = 1; paddleId <= 48; paddleId++) {
			int row = bestRows[paddleId];
			if (row >= 0) {
				getPaddle(paddleId).drawPaddle(drawable, _adcData.color(row));
			}
		}
	}

	static int geometryPaddle(int sector, int layer, int component) {
		int[] geometry = new int[3];
		CNDGeometry.realTripletToGeoTriplet(geometry, new int[] { sector, layer, component });
		return geometry[2];
	}

	@Override
	protected boolean show() {
		switch (_layer) {
		case 1:
			return _cedPanel3D.showCNDLayer1();

		case 2:
			return _cedPanel3D.showCNDLayer2();

		case 3:
			return _cedPanel3D.showCNDLayer3();
		}
		return false;
	}

	/**
	 * Get the 3D Paddle
	 *
	 * @param paddleId the paddle Id [..48]
	 * @return the 3D paddle
	 */
	public CNDPaddle3D getPaddle(int paddleId) {
		if ((paddleId < 1) || (paddleId > 48)) {
			return null;
		}

		return _paddles[paddleId - 1];
	}
}
