package cnuphys.ced.trigger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TriggerWordParserTest {

	@Test
	void acceptsSignedAndUnsignedThirtyTwoBitWords() {
		assertEquals(64, TriggerWordParser.parse(" 64 ").orElseThrow());
		assertEquals(Integer.MIN_VALUE, TriggerWordParser.parse("-2147483648").orElseThrow());
		assertEquals(-1, TriggerWordParser.parse("4294967295").orElseThrow());
	}

	@Test
	void rejectsValuesOutsideThirtyTwoBits() {
		assertTrue(TriggerWordParser.parse(null).isEmpty());
		assertTrue(TriggerWordParser.parse("trigger").isEmpty());
		assertTrue(TriggerWordParser.parse("-2147483649").isEmpty());
		assertTrue(TriggerWordParser.parse("4294967296").isEmpty());
	}
}
