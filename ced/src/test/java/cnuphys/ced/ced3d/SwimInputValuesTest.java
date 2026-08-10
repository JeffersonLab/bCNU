package cnuphys.ced.ced3d;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SwimInputValuesTest {

	@Test
	void parsesFiniteGeometryValues() {
		assertEquals(-125.5, SwimInputValues.finiteDouble(" -125.5 ").orElseThrow());
		assertEquals(168.0, SwimInputValues.positiveDouble("168").orElseThrow());
	}

	@Test
	void rejectsNonFiniteAndNonPositiveValues() {
		assertTrue(SwimInputValues.finiteDouble("NaN").isEmpty());
		assertTrue(SwimInputValues.finiteDouble("Infinity").isEmpty());
		assertTrue(SwimInputValues.finiteDouble("not-a-number").isEmpty());
		assertTrue(SwimInputValues.positiveDouble("0").isEmpty());
		assertTrue(SwimInputValues.positiveDouble("-1").isEmpty());
	}

	@Test
	void validatesPlaneNormals() {
		assertTrue(SwimInputValues.isNonZeroVector(new double[] { 0.0, 0.0, 1.0 }));
		assertFalse(SwimInputValues.isNonZeroVector(new double[] { 0.0, 0.0, 0.0 }));
		assertFalse(SwimInputValues.isNonZeroVector(null));
	}
}
