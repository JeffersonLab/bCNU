package cnuphys.ced.component;

import java.awt.Component;

import edu.cnu.mdi.dialog.DialogUtils;
import cnuphys.bCNU.dialog.SimpleDialog;

public class DrawingLegendDialog extends SimpleDialog {

	//singleton
	private static DrawingLegendDialog _dialog;

	// button names for closeout
	private static String[] closeoutButtons = { "Close" };

	/**
	 * Create the panel for selected
	 *
	 * @param id
	 * @param level
	 */
	private DrawingLegendDialog() {
		super("Drawing Symbology", false, closeoutButtons);
	}

	/**
	 * Override to create the component that goes in the center.
	 *
	 * @return the component that is placed in the center
	 */
	@Override
	protected Component createCenterComponent() {
		return new DrawingLegend();
	}

	public static void showDialog() {

		if (_dialog == null) {
			_dialog = new DrawingLegendDialog();
		}

		if (!_dialog.isVisible()) {
			DialogUtils.centerDialog(_dialog);
		}
		_dialog.setVisible(true);
	}


}
