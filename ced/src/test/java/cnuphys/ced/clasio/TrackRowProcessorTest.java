package cnuphys.ced.clasio;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class TrackRowProcessorTest {

	@Test
	void continuesAfterAnInvalidTrackRow() {
		List<Integer> processed = new ArrayList<>();
		List<Integer> failed = new ArrayList<>();

		TrackRowProcessor.process(4, row -> {
			if (row == 1) {
				throw new IllegalArgumentException("bad row");
			}
			processed.add(row);
		}, (row, exception) -> failed.add(row));

		assertEquals(List.of(0, 2, 3), processed);
		assertEquals(List.of(1), failed);
	}
}
