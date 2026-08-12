package cnuphys.ced.frame;

import javax.swing.UIManager;

import com.formdev.flatlaf.FlatIntelliJLaf;

import edu.cnu.mdi.ui.fonts.Fonts;

/** Installs the shared MDI look and feel before CED creates Swing components. */
final class CedLookAndFeel {

	private CedLookAndFeel() {
	}

	static void install() {
		if (!FlatIntelliJLaf.setup()) {
			throw new IllegalStateException("Unable to install FlatIntelliJLaf");
		}

		UIManager.put("Component.focusWidth", 1);
		UIManager.put("Component.arc", 6);
		UIManager.put("Button.arc", 6);
		UIManager.put("TabbedPane.showTabSeparators", true);
		Fonts.refresh();
	}
}
