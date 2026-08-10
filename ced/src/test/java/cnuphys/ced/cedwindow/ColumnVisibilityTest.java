package cnuphys.ced.cedwindow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ColumnVisibilityTest {

	@Test
	void encodesAndReadsColumnSelections() {
		long mask = ColumnVisibility.selectedMask(new boolean[] { true, false, true });

		assertTrue(ColumnVisibility.isVisible(mask, 0));
		assertFalse(ColumnVisibility.isVisible(mask, 1));
		assertTrue(ColumnVisibility.isVisible(mask, 2));
	}

	@Test
	void ignoresMissingAndCorruptPreferences() {
		assertTrue(ColumnVisibility.parseMask(null).isEmpty());
		assertTrue(ColumnVisibility.parseMask("not-a-number").isEmpty());
		assertEquals(17L, ColumnVisibility.parseMask("17").orElseThrow());
	}
}
