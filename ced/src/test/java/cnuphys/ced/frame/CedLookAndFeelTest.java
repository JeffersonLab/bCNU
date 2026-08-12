package cnuphys.ced.frame;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.swing.UIManager;

import org.junit.jupiter.api.Test;

import com.formdev.flatlaf.FlatIntelliJLaf;

import edu.cnu.mdi.ui.fonts.Fonts;

class CedLookAndFeelTest {

	@Test
	void installsFlatLafAndRefreshesMdiFonts() {
		CedLookAndFeel.install();

		assertEquals(FlatIntelliJLaf.class, UIManager.getLookAndFeel().getClass());
		assertNotNull(Fonts.defaultFont);
		assertNotNull(Fonts.defaultBoldFont);
		assertNotNull(Fonts.defaultMono);
	}
}
