package cnuphys.ced.clasio;

import java.util.function.BiConsumer;
import java.util.function.IntConsumer;

final class TrackRowProcessor {

	private TrackRowProcessor() {
	}

	static void process(int rowCount, IntConsumer rowProcessor,
			BiConsumer<Integer, RuntimeException> failureHandler) {
		for (int row = 0; row < rowCount; row++) {
			try {
				rowProcessor.accept(row);
			} catch (RuntimeException exception) {
				failureHandler.accept(row, exception);
			}
		}
	}
}
