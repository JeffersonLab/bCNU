package cnuphys.ced.item;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import edu.cnu.mdi.ui.colors.ScientificColorMap;

class MagFieldItemTest {

	@Test
	void fieldScalesUseViridisEndpoints() {
		assertViridisScale(MagFieldItem._colorScaleModelTorus.getColors());
		assertViridisScale(MagFieldItem._colorScaleModelSolenoid.getColors());
		assertViridisScale(MagFieldItem._colorScaleModelGradient.getColors());
	}

	private static void assertViridisScale(java.awt.Color[] colors) {
		assertEquals(256, colors.length);
		assertEquals(ScientificColorMap.VIRIDIS.colorAt(0.0), colors[0]);
		assertEquals(ScientificColorMap.VIRIDIS.colorAt(1.0), colors[255]);
	}
}
