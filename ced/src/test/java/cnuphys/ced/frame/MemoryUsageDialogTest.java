package cnuphys.ced.frame;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MemoryUsageDialogTest {

	@Test
	void allocatedHeapIsReportedInMegabytes() {
		double expected = Runtime.getRuntime().totalMemory() / 1_048_576.0;
		double actual = MemoryUsageDialog.allocatedHeapMegabytes();

		assertTrue(actual > 0.0);
		assertTrue(Math.abs(expected - actual) < 1.0);
	}
}
