package cnuphys.ced.ced3d.cnd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CNDLayer3DTest {

	@Test
	void mapsRealBankComponentsToGeometryPaddles() {
		assertEquals(48, CNDLayer3D.geometryPaddle(1, 1, 1));
		assertEquals(1, CNDLayer3D.geometryPaddle(1, 1, 2));
		assertEquals(46, CNDLayer3D.geometryPaddle(24, 3, 1));
		assertEquals(47, CNDLayer3D.geometryPaddle(24, 3, 2));
	}
}
