package cnuphys.ced.ced3d;

import java.util.OptionalDouble;

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
}
