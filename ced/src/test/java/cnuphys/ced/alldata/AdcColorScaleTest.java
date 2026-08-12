package cnuphys.ced.alldata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.awt.Color;

import org.junit.jupiter.api.Test;

import edu.cnu.mdi.ui.colors.ScientificColorMap;

class AdcColorScaleTest {

	@Test
	void usesMdiTurboEndpoints() {
		AdcColorScale scale = AdcColorScale.getInstance();

		assertEquals(ScientificColorMap.TURBO.colorAt(0.0), scale.getColor(0.0));
		assertEquals(ScientificColorMap.TURBO.colorAt(1.0), scale.getColor(1.0));
	}

	@Test
	void preservesRequestedAlpha() {
		Color color = AdcColorScale.getInstance().getAlphaColor(0.5, 123);

		assertEquals(123, color.getAlpha());
	}

	@Test
	void remainsASingleton() {
		assertSame(AdcColorScale.getInstance(), AdcColorScale.getInstance());
	}
}
