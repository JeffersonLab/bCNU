package cnuphys.ced.frame;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import cnuphys.bCNU.dialog.IColorChangeListener;
import cnuphys.bCNU.dialog.SimpleDialog;
import cnuphys.bCNU.log.Log;
import edu.cnu.mdi.ui.colors.X11Colors;
import cnuphys.ced.properties.PropertiesManager;

public class OrderColors extends SimpleDialog implements IColorChangeListener {

	private static String COL_KEY = "OCCOL_";
	private static String CHK_KEY = "OCCHK_";


	public static Color TRANSCOLOR = new Color(0, 0, 0, 10);


	private static ColorOption[] _colorOptions;

	public OrderColors() {
		super("Color Selection", false, "Close");

		Dimension size = getPreferredSize();

		size.height = Math.max(size.height, 100 + 28*_colorOptions.length);

		//get saved values

		setSize(size);

		readSavedValues();
	}

	//write saved values

	private void readSavedValues() {
		for (int i = 0; i < _colorOptions.length; i++) {
			String cprop = COL_KEY + i;
			String vprop = CHK_KEY + i;

			String rgbstr = PropertiesManager.getInstance().get(cprop);
			Color savedColor = parseSavedColor(rgbstr);
			if (savedColor != null) {
				_colorOptions[i].setCurrentColor(savedColor);
			}

			String chkstr = PropertiesManager.getInstance().get(vprop);
			if (chkstr != null) {
				boolean checked = Boolean.parseBoolean(chkstr);
				_colorOptions[i].setChecked(checked);
			}

		}

	}

	static Color parseSavedColor(String rgbText) {
		if (rgbText == null) {
			return null;
		}

		try {
			return new Color(Integer.parseInt(rgbText));
		} catch (NumberFormatException exception) {
			Log.getInstance().warning("Ignoring invalid saved DC order color: " + rgbText);
			return null;
		}
	}

	//write the properties
	private static void writeSavedValues() {
		for (int i = 0; i < _colorOptions.length; i++) {
			String cprop = COL_KEY + i;
			String vprop = CHK_KEY + i;

			PropertiesManager.getInstance().put(cprop, ""+_colorOptions[i].getCurrentColor().getRGB());
			PropertiesManager.getInstance().put(vprop, ""+_colorOptions[i].isChecked());

		}

		PropertiesManager.getInstance().writeProperties();
	}


	@Override
	protected Component createNorthComponent() {
		return new JLabel("<html><div style='text-align:center'>"
				+ "DC Cell Fill Color Based on DC::tdc.order<br>"
				+ "<small>Click a row to enable or disable it. Command-click (macOS) or Ctrl-click opens the color chooser.</small>"
				+ "</div></html>");
	}

	@Override
	protected Component createWestComponent() {
		return Box.createHorizontalStrut(10);
	}

	@Override
	protected Component createEastComponent() {
		return Box.createHorizontalStrut(10);
	}

	@Override
	protected Component createCenterComponent() {


		Color defaultColors[] = {
				X11Colors.getX11Color("cadet blue"),
				X11Colors.getX11Color("red"),
				X11Colors.getX11Color("coral"),
				X11Colors.getX11Color("dark green"),
				X11Colors.getX11Color("orange"),
				X11Colors.getX11Color("blue"),
				X11Colors.getX11Color("magenta"),
				X11Colors.getX11Color("brown"),
				X11Colors.getX11Color("wheat"),
				X11Colors.getX11Color("fuchsia"),
				X11Colors.getX11Color("forest green"),
		};

		_colorOptions = new ColorOption[defaultColors.length];


		JPanel panel = new JPanel();

		BoxLayout boxLayout = new BoxLayout(panel, BoxLayout.Y_AXIS);
		panel.setLayout(boxLayout);

		panel.add(Box.createVerticalStrut(8));

		int len = defaultColors.length;

		for(int i = 0; i < len; i++) {
			String prompt = String.format("%d - %d", 10*i, 10*i + 9);
			_colorOptions[i] = new ColorOption(this, defaultColors[i], prompt);
			panel.add(_colorOptions[i]);
		}

		panel.add(Box.createVerticalStrut(8));


		return panel;
	}

	/**
	 * Get the color corresponding to the order value
	 * @param order
	 * @return
	 */
	public static Color getOrderColor(int order) {

		if (order < 0) {
			return TRANSCOLOR;
		}

		int index = order / 10;

		if (index >= _colorOptions.length) {
			return TRANSCOLOR;
		}

		return _colorOptions[index].getColor();
	}


	@Override
	public void colorChanged(Component component, Color color) {
		writeSavedValues();
		Ced.refresh();
	}

}
