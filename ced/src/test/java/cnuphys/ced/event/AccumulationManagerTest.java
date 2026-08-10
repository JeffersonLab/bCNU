package cnuphys.ced.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AccumulationManagerTest {

	@Test
	void incrementsValidDetectorIndices() {
		int[][][][] counts = new int[2][3][4][5];

		assertTrue(AccumulationManager.increment(counts, 1, 2, 3, 4));
		assertEquals(1, counts[1][2][3][4]);
	}

	@Test
	void rejectsInvalidDetectorIndicesWithoutChangingCounts() {
		int[][][] counts = new int[2][3][4];

		assertFalse(AccumulationManager.increment(counts, -1, 0, 0));
		assertFalse(AccumulationManager.increment(counts, 0, 3, 0));
		assertFalse(AccumulationManager.increment(counts, 0, 0, 4));
		assertEquals(0, counts[0][0][0]);
	}

	@Test
	void rejectsMissingAccumulationArrays() {
		assertFalse(AccumulationManager.increment((int[]) null, 0));
		assertFalse(AccumulationManager.increment((int[][]) null, 0, 0));
	}
}
