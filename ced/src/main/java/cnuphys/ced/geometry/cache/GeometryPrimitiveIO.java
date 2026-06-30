package cnuphys.ced.geometry.cache;

import java.awt.Point;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Utility methods for reading and writing simple primitive geometry data.
 * <p>
 * This class is intended for the geometry-cache refactor. It provides small,
 * explicit, validated read/write helpers for primitive geometry structures such
 * as corner-coordinate arrays and integer points. It deliberately does not know
 * about detector-specific classes such as paddles, wires, strips, sectors, or
 * JLab geometry objects.
 */
public final class GeometryPrimitiveIO {

	/**
	 * Private constructor for utility class.
	 */
	private GeometryPrimitiveIO() {
	}

	/**
	 * Write one fixed-size corner-coordinate array.
	 * <p>
	 * The written format is:
	 *
	 * <pre>
	 * cornerCount
	 *   coordCount
	 *     coord values...
	 * </pre>
	 *
	 * For an 8-corner 3D solid, the supplied array should be {@code [8][3]}.
	 *
	 * @param output        the output stream
	 * @param corners       the corner-coordinate array
	 * @param expectedCorners the expected number of corners
	 * @param expectedCoords  the expected number of coordinates per corner
	 * @throws IllegalArgumentException if the array does not match the expected
	 *                                  shape
	 */
	public static void writeCorners(Output output, double[][] corners, int expectedCorners, int expectedCoords) {
		validateCorners(corners, expectedCorners, expectedCoords, "write");

		output.writeInt(expectedCorners);

		for (int corner = 0; corner < expectedCorners; corner++) {
			output.writeInt(expectedCoords);

			for (int coord = 0; coord < expectedCoords; coord++) {
				output.writeDouble(corners[corner][coord]);
			}
		}
	}

	/**
	 * Read one fixed-size corner-coordinate array.
	 * <p>
	 * The read format must match {@link #writeCorners(Output, double[][], int, int)}.
	 *
	 * @param input           the input stream
	 * @param expectedCorners the expected number of corners
	 * @param expectedCoords  the expected number of coordinates per corner
	 * @param context         short detector-specific context for error messages
	 * @return the corner-coordinate array
	 * @throws IllegalArgumentException if the cached data does not match the
	 *                                  expected shape
	 */
	public static double[][] readCorners(Input input, int expectedCorners, int expectedCoords, String context) {
		int numCorners = input.readInt();

		if (numCorners != expectedCorners) {
			throw new IllegalArgumentException(String.format("%s: expected %d corners, found %d.",
					context, expectedCorners, numCorners));
		}

		double corners[][] = new double[expectedCorners][expectedCoords];

		for (int corner = 0; corner < expectedCorners; corner++) {
			int numCoords = input.readInt();

			if (numCoords != expectedCoords) {
				throw new IllegalArgumentException(String.format(
						"%s: expected %d coordinates for corner %d, found %d.",
						context, expectedCoords, corner, numCoords));
			}

			for (int coord = 0; coord < expectedCoords; coord++) {
				corners[corner][coord] = input.readDouble();
			}
		}

		return corners;
	}

	/**
	 * Write a nullable {@link Point}.
	 *
	 * @param output the output stream
	 * @param point  the point, possibly {@code null}
	 */
	public static void writePoint(Output output, Point point) {
		boolean hasPoint = (point != null);
		output.writeBoolean(hasPoint);

		if (hasPoint) {
			output.writeInt(point.x);
			output.writeInt(point.y);
		}
	}

	/**
	 * Read a nullable {@link Point}.
	 *
	 * @param input the input stream
	 * @return the point, or {@code null}
	 */
	public static Point readPoint(Input input) {
		boolean hasPoint = input.readBoolean();

		if (!hasPoint) {
			return null;
		}

		return new Point(input.readInt(), input.readInt());
	}

	/**
	 * Validate a fixed-size corner-coordinate array.
	 *
	 * @param corners         the array to validate
	 * @param expectedCorners the expected number of corners
	 * @param expectedCoords  the expected number of coordinates per corner
	 * @param operation       operation name for error messages
	 * @throws IllegalArgumentException if the array is malformed
	 */
	private static void validateCorners(double[][] corners, int expectedCorners, int expectedCoords, String operation) {
		if (corners == null) {
			throw new IllegalArgumentException("Cannot " + operation + " null corner array.");
		}

		if (corners.length != expectedCorners) {
			throw new IllegalArgumentException(String.format(
					"Cannot %s corner array: expected %d corners, found %d.",
					operation, expectedCorners, corners.length));
		}

		for (int corner = 0; corner < expectedCorners; corner++) {
			if (corners[corner] == null) {
				throw new IllegalArgumentException(
						String.format("Cannot %s corner array: corner %d is null.", operation, corner));
			}

			if (corners[corner].length != expectedCoords) {
				throw new IllegalArgumentException(String.format(
						"Cannot %s corner array: expected %d coordinates for corner %d, found %d.",
						operation, expectedCoords, corner, corners[corner].length));
			}
		}
	}
}