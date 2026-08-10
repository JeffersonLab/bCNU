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

	@Test
	void validatesSwimCountsAndNormalizesRandomSeeds() {
		assertEquals(25, SwimInputValues.positiveInt("25").orElseThrow());
		assertTrue(SwimInputValues.positiveInt("0").isEmpty());
		assertTrue(SwimInputValues.positiveInt("-4").isEmpty());
		assertEquals(1234L, SwimInputValues.randomSeed("1234").orElseThrow());
		assertEquals(0L, SwimInputValues.randomSeed("-1").orElseThrow());
		assertTrue(SwimInputValues.randomSeed("bad-seed").isEmpty());
	}

	@Test
	void honorsFixedAndRandomChargeSelections() {
		assertEquals(1, SwimInputValues.selectCharge(SwimmerControlPanel.CHARGE.POSITIVE, 0.0));
		assertEquals(-1, SwimInputValues.selectCharge(SwimmerControlPanel.CHARGE.NEGATIVE, 1.0));
		assertEquals(-1, SwimInputValues.selectCharge(SwimmerControlPanel.CHARGE.RANDOM, 0.2));
		assertEquals(0, SwimInputValues.selectCharge(SwimmerControlPanel.CHARGE.RANDOM, 0.5));
		assertEquals(1, SwimInputValues.selectCharge(SwimmerControlPanel.CHARGE.RANDOM, 0.8));
	}
}
