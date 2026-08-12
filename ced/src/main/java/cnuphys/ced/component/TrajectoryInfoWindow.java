package cnuphys.ced.component;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;

import javax.swing.JLabel;
import javax.swing.JWindow;

import edu.cnu.mdi.ui.colors.X11Colors;
import edu.cnu.mdi.ui.fonts.Fonts;

/** Floating trajectory feedback shown while the pointer hovers over a track. */
public final class TrajectoryInfoWindow extends JWindow {

	private static final Color BACKGROUND = X11Colors.getX11Color("Alice Blue");
	private static TrajectoryInfoWindow window;

	private final JLabel label = new JLabel();

	private TrajectoryInfoWindow() {
		setBackground(BACKGROUND);
		label.setFont(Fonts.commonFont(Font.PLAIN, 10));
		label.setForeground(Color.black);
		getContentPane().add(label);
		if (isTranslucencySupported()) {
			setOpacity(0.6f);
		}
	}

	public static void showInfo(String text, Point screenPoint) {
		if (window == null) {
			window = new TrajectoryInfoWindow();
		} else {
			window.setVisible(false);
		}
		window.label.setText(text);
		window.pack();
		window.setLocation(screenPoint);
		window.setVisible(true);
	}

	public static void closeInfoWindow() {
		if (window != null) {
			window.setVisible(false);
		}
	}

	private static boolean isTranslucencySupported() {
		GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice device = environment.getDefaultScreenDevice();
		return device.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT);
	}

	@Override
	public Insets getInsets() {
		Insets insets = super.getInsets();
		return new Insets(insets.top + 2, insets.left + 2, insets.bottom + 2, insets.right + 2);
	}
}
