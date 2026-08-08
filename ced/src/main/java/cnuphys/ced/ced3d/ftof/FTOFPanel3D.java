package cnuphys.ced.ced3d.ftof;

import java.awt.Color;

import com.jogamp.opengl.GLAutoDrawable;

import cnuphys.ced.alldata.FTOFAdc;
import cnuphys.ced.ced3d.DetectorItem3D;
import cnuphys.ced.ced3d.PlainPanel3D;
import cnuphys.ced.geometry.ftof.FTOFGeometry;

public class FTOFPanel3D extends DetectorItem3D {

	// individual paddles
	private FTOFPaddle3D _paddles[];

	// one based sector [1..6]
	private final int _sector;

	// "superlayer" [PANEL_1A, PANEL_1B, PANEL_2] (0, 1, 2)
	private final int _panelId;

	//data containers
	private FTOFAdc _adcData = FTOFAdc.getInstance();


	/**
	 * An FTOF Panel 3D item
	 *
	 * @param panel3d the owner graphical panel
	 * @param sector  the sector 1..6
	 * @param panelId the super layer [PANEL_1A, PANEL_1B, PANEL_2] (0, 1, 2)
	 */
	public FTOFPanel3D(PlainPanel3D panel3D, int sector, int panelId) {
		super(panel3D);
		_sector = sector;
		_panelId = panelId;

		_paddles = new FTOFPaddle3D[FTOFGeometry.numPaddles[panelId]];
		for (int paddleId = 1; paddleId <= _paddles.length; paddleId++) {
			_paddles[paddleId - 1] = new FTOFPaddle3D(sector, panelId, paddleId);
		}
	}

	/**
	 * Get the number of paddles
	 *
	 * @return the number of paddles
	 */
	public int getPaddleCount() {
		return _paddles.length;
	}

	/**
	 * Get the paddle
	 *
	 * @param paddleId the 1-based index
	 * @return the paddle
	 */
	public FTOFPaddle3D getPaddle(int paddleId) {
		return _paddles[paddleId - 1];
	}

	@Override
	public void drawShape(GLAutoDrawable drawable) {
	}

	@Override
	public void drawData(GLAutoDrawable drawable) {
		// draw based on ADC data
		byte layer = (byte) (_panelId + 1);
		for (int i = 0; i < _adcData.count(); i++) {
			if ((_adcData.sector(i) == _sector) && (_adcData.layer(i) == layer)) {
				Color fc = _adcData.componentColor(_adcData.sector(i), _adcData.layer(i), _adcData.component(i),
						_adcData.order(i));
				getPaddle(_adcData.component(i)).drawPaddle(drawable, fc);
			}
		} //end has data
	}

	/**
	 * Get the sector [1..6]
	 *
	 * @return the sector 1..6
	 */
	public int getSector() {
		return _sector;
	}

	/**
	 * Get the superlayer [PANEL_1A, PANEL_1B, PANEL_2] (0, 1, 2)
	 *
	 * @return the superlayer [PANEL_1A, PANEL_1B, PANEL_2] (0, 1, 2)
	 */
	public int getSuperLayer() {
		return _panelId;
	}

	// show FTOFs?
	@Override
	protected boolean show() {
		return _cedPanel3D.showFTOF();
	}

}
