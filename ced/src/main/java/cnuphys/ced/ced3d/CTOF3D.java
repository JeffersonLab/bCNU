package cnuphys.ced.ced3d;

import java.awt.Color;

import com.jogamp.opengl.GLAutoDrawable;

import cnuphys.ced.alldata.datacontainer.tof.CTOFADCData;
import cnuphys.lund.X11Colors;

public class CTOF3D extends DetectorItem3D {

	// child layer items
	private CTOFPaddle3D _paddles[];

	//data containers
	private CTOFADCData _adcData = CTOFADCData.getInstance();

	/**
	 * The 3D CND
	 *
	 * @param panel3d the 3D panel owner
	 */
	public CTOF3D(CedPanel3D panel3D) {
		super(panel3D);

		_paddles = new CTOFPaddle3D[48];
		for (int paddleId = 1; paddleId <= 48; paddleId++) {
			_paddles[paddleId - 1] = new CTOFPaddle3D(paddleId);
		}
	}

	@Override
	public void drawShape(GLAutoDrawable drawable) {

		Color color = X11Colors.getX11Color("Dodger blue", getVolumeAlpha());

		for (int paddleId = 1; paddleId <= 48; paddleId++) {
			_paddles[paddleId - 1].drawPaddle(drawable, color);
		}

		for (int i = 0; i < _adcData.count(); i++) {
			short paddleId = _adcData.component[i];
			byte order = _adcData.order[i];
			Color fc = _adcData.getColor((byte)1, (byte)1, paddleId, order);
			_paddles[paddleId-1].drawPaddle(drawable, fc);
		}
	}

	@Override
	public void drawData(GLAutoDrawable drawable) {
		// Children handle drawing

	}

	@Override
	protected boolean show() {
		return _cedPanel3D.showCTOF();
	}

}
