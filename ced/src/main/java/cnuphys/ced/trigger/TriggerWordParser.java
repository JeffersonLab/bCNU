package cnuphys.ced.trigger;

import java.util.OptionalInt;

final class TriggerWordParser {

	private static final long MAX_UNSIGNED_INT = 0xFFFF_FFFFL;

	private TriggerWordParser() {}

	static OptionalInt parse(String text) {
		try {
			long value = Long.parseLong(text == null ? "" : text.trim());
			if (value < Integer.MIN_VALUE || value > MAX_UNSIGNED_INT) return OptionalInt.empty();
			return OptionalInt.of((int) value);
		} catch (NumberFormatException exception) {
			return OptionalInt.empty();
		}
	}
}
