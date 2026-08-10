package cnuphys.ced.frame;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.awt.Color;

import org.junit.jupiter.api.Test;

class OrderColorsTest {

	@Test
	void parsesSavedRgbValues() {
		Color expected = new Color(17, 34, 51);

		assertEquals(expected, OrderColors.parseSavedColor(Integer.toString(expected.getRGB())));
	}

	@Test
	void rejectsMissingOrMalformedRgbValues() {
		assertNull(OrderColors.parseSavedColor(null));
		assertNull(OrderColors.parseSavedColor("not-a-color"));
	}
}
