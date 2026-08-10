package cnuphys.ced.ced3d;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

final class SwimInputValues {

	private SwimInputValues() {
	}

	static OptionalDouble finiteDouble(String text) {
		if (text == null) {
			return OptionalDouble.empty();
		}

		try {
			double value = Double.parseDouble(text.trim());
			return Double.isFinite(value) ? OptionalDouble.of(value) : OptionalDouble.empty();
		} catch (NumberFormatException exception) {
			return OptionalDouble.empty();
		}
	}

	static OptionalDouble positiveDouble(String text) {
		OptionalDouble value = finiteDouble(text);
		return value.isPresent() && (value.getAsDouble() > 0.0) ? value : OptionalDouble.empty();
	}

	static boolean isNonZeroVector(double[] vector) {
		return (vector != null) && (vector.length == 3)
				&& ((vector[0] != 0.0) || (vector[1] != 0.0) || (vector[2] != 0.0));
	}

	static OptionalInt positiveInt(String text) {
		if (text == null) {
			return OptionalInt.empty();
		}

		try {
			int value = Integer.parseInt(text.trim());
			return (value > 0) ? OptionalInt.of(value) : OptionalInt.empty();
		} catch (NumberFormatException exception) {
			return OptionalInt.empty();
		}
	}

	static OptionalLong randomSeed(String text) {
		if (text == null) {
			return OptionalLong.empty();
		}

		try {
			long value = Long.parseLong(text.trim());
			return OptionalLong.of(Math.max(0L, value));
		} catch (NumberFormatException exception) {
			return OptionalLong.empty();
		}
	}

	static int selectCharge(SwimmerControlPanel.CHARGE charge, double randomValue) {
		if (charge == SwimmerControlPanel.CHARGE.NEGATIVE) {
			return -1;
		}
		if (charge == SwimmerControlPanel.CHARGE.POSITIVE) {
			return 1;
		}
		if (randomValue < 0.4) {
			return -1;
		}
		return (randomValue > 0.6) ? 1 : 0;
	}
}
