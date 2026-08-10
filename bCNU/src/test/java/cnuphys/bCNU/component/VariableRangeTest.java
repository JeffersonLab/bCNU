package cnuphys.bCNU.component;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import org.junit.jupiter.api.Test;

class VariableRangeTest {

	@Test
	void rejectsNonFiniteRangeLimits() {
		assertEquals(2.5, VariableRange.finiteOrDefault("NaN", 2.5));
		assertEquals(2.5, VariableRange.finiteOrDefault("Infinity", 2.5));
		assertEquals(2.5, VariableRange.finiteOrDefault("bad", 2.5));
		assertEquals(-3.25, VariableRange.finiteOrDefault(" -3.25 ", 2.5));
	}

	@Test
	void suppliedSeedReproducesRangeValues() {
		double first = VariableRange.randomBetween(-10.0, 20.0, new Random(12345L));
		double repeated = VariableRange.randomBetween(-10.0, 20.0, new Random(12345L));

		assertEquals(first, repeated);
	}
}
