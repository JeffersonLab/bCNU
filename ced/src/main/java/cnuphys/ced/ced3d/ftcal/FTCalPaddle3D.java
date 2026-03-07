package cnuphys.ced.ced3d.ftcal;

import java.awt.Color;

import com.jogamp.opengl.GLAutoDrawable;

import bCNU3D.Support3D;
import cnuphys.ced.alldata.datacontainer.ftcal.FTCalADCData;
import cnuphys.ced.ced3d.CedPanel3D;
import cnuphys.ced.ced3d.DetectorItem3D;
import cnuphys.ced.geometry.FTCALGeometry;
import cnuphys.lund.X11Colors;

public class FTCalPaddle3D extends DetectorItem3D {

	// paddle ID
	private short _id;

	// the cached vertices
	private float[] _coords = new float[24];

	// frame the paddle?
	private static boolean _frame = true;

	//data container
	private FTCalADCData adcData = FTCalADCData.getInstance();

	/**
	 * Create a FTCAL paddle
	 *
	 * @param panel3D the 3D panel this is associated with
	 * @param id      the paddle ID which is sparse, the first one is 8
	 */
	public FTCalPaddle3D(CedPanel3D panel3D, short id) {
		super(panel3D);
		_id = id;

		FTCALGeometry.paddleVertices(_id, _coords);
	}

	@Override
	public void drawShape(GLAutoDrawable drawable) {
		//hits use adc data
		Color color = X11Colors.getX11Color("light blue", getVolumeAlpha());

		// draw "hit" based on adc values
		for (int i = 0; i < adcData.count(); i++) {
			if (adcData.component[i] == _id) {
				int adc = adcData.adc[i];

				color = adcData.getADCColor(adc);
				color = new Color(color.getRed(), color.getGreen(), color.getBlue(), getVolumeAlpha());
				break;
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
	}

	@Override
	protected boolean show() {
		return true;
	}

}
