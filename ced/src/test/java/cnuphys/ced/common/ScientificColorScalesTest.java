package cnuphys.ced.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.Color;

import org.junit.jupiter.api.Test;

import edu.cnu.mdi.ui.colors.ScientificColorMap;

class ScientificColorScalesTest {

	@Test
	void denselySamplesInterpolatedMapIncludingEndpoints() {
		Color[] colors = ScientificColorScales.sample(ScientificColorMap.VIRIDIS, 256);

		assertEquals(256, colors.length);
		assertEquals(ScientificColorMap.VIRIDIS.colorAt(0.0), colors[0]);
		assertEquals(ScientificColorMap.VIRIDIS.colorAt(1.0), colors[255]);
		assertEquals(ScientificColorMap.VIRIDIS.colorAt(128.0 / 255.0), colors[128]);
	}

	@Test
	void rejectsInvalidArguments() {
		assertThrows(IllegalArgumentException.class, () -> ScientificColorScales.sample(null, 256));
		assertThrows(IllegalArgumentException.class,
				() -> ScientificColorScales.sample(ScientificColorMap.TURBO, 1));
	}
}
