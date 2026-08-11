package cnuphys.ced.geometry;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import cnuphys.ced.frame.Ced;
import cnuphys.ced.geometry.bmt.Constants;
import cnuphys.ced.geometry.bmt.ConstantsLoader;
import cnuphys.ced.geometry.bmt.ConstantsLoaderVZ;
import cnuphys.ced.geometry.bmt.Geometry;
import cnuphys.ced.geometry.cache.ACachedGeometry;


public class BMTGeometry extends ACachedGeometry {

	private static Geometry _geometry;

	public BMTGeometry() {
		super("BMTGeometry");
	}

	/**
	 * Initialize the BMT Geometry
	 */
	public void initializeUsingCCDB() {
		System.out.println("\n=====================================");
		System.out.println("===  BMT Geometry Initialization  ===");
		System.out.println("=====================================");

		if (Ced.forVeronique()) {
			ConstantsLoaderVZ.Load(11);
		} else {
			ConstantsLoader.Load(11);
		}
		Constants.Load();
		_geometry = new Geometry();

	}

	public static Geometry getGeometry() {
		return _geometry;
	}

	@Override
	public boolean supportsCache() {
		return true;
	}

	@Override
	public int getCacheFormatVersion() {
		return Ced.forVeronique() ? 2 : 1;
	}

	@Override
	public void writeGeometry(DataOutput output) throws IOException {
		writeDoubleArray(output, Constants.getCRZRADIUS());
		writeIntArray(output, Constants.getCRZNSTRIPS());
		writeDoubleArray(output, Constants.getCRZSPACING());
		writeDoubleArray(output, Constants.getCRZWIDTH());
		writeDoubleArray(output, Constants.getCRZLENGTH());
		writeDoubleArray(output, Constants.getCRZZMIN());
		writeDoubleArray(output, Constants.getCRZZMAX());
		writeDoubleArray(output, Constants.getCRZOFFSET());
		writeDoubleMatrix(output, Constants.getCRZEDGE1());
		writeDoubleMatrix(output, Constants.getCRZEDGE2());
		writeDoubleArray(output, Constants.getCRZXPOS());

		writeDoubleArray(output, Constants.getCRCRADIUS());
		writeIntArray(output, Constants.getCRCNSTRIPS());
		writeDoubleArray(output, Constants.getCRCSPACING());
		writeDoubleArray(output, Constants.getCRCLENGTH());
		writeDoubleArray(output, Constants.getCRCZMIN());
		writeDoubleArray(output, Constants.getCRCZMAX());
		writeDoubleArray(output, Constants.getCRCOFFSET());
		writeIntMatrix(output, Constants.getCRCGROUP());
		writeDoubleMatrix(output, Constants.getCRCWIDTH());
		writeDoubleMatrix(output, Constants.getCRCEDGE1());
		writeDoubleMatrix(output, Constants.getCRCEDGE2());
		writeDoubleArray(output, Constants.getCRCXPOS());
		output.writeDouble(Constants.getThetaL());
	}

	@Override
	public void readGeometry(DataInput input) throws IOException {
		Constants.setCRZRADIUS(readDoubleArray(input));
		Constants.setCRZNSTRIPS(readIntArray(input));
		Constants.setCRZSPACING(readDoubleArray(input));
		Constants.setCRZWIDTH(readDoubleArray(input));
		Constants.setCRZLENGTH(readDoubleArray(input));
		Constants.setCRZZMIN(readDoubleArray(input));
		Constants.setCRZZMAX(readDoubleArray(input));
		Constants.setCRZOFFSET(readDoubleArray(input));
		Constants.setCRZEDGE1(readDoubleMatrix(input));
		Constants.setCRZEDGE2(readDoubleMatrix(input));
		Constants.setCRZXPOS(readDoubleArray(input));

		Constants.setCRCRADIUS(readDoubleArray(input));
		Constants.setCRCNSTRIPS(readIntArray(input));
		Constants.setCRCSPACING(readDoubleArray(input));
		Constants.setCRCLENGTH(readDoubleArray(input));
		Constants.setCRCZMIN(readDoubleArray(input));
		Constants.setCRCZMAX(readDoubleArray(input));
		Constants.setCRCOFFSET(readDoubleArray(input));
		Constants.setCRCGROUP(readIntMatrix(input));
		Constants.setCRCWIDTH(readDoubleMatrix(input));
		Constants.setCRCEDGE1(readDoubleMatrix(input));
		Constants.setCRCEDGE2(readDoubleMatrix(input));
		Constants.setCRCXPOS(readDoubleArray(input));
		Constants.setThetaL(input.readDouble());
		Constants.Load();
		_geometry = new Geometry();
	}

	private static void writeDoubleArray(DataOutput output, double[] values) throws IOException {
		output.writeInt(values.length);
		for (double value : values) {
			output.writeDouble(value);
		}
	}

	private static double[] readDoubleArray(DataInput input) throws IOException {
		int length = checkedLength(input.readInt());
		double[] values = new double[length];
		for (int index = 0; index < length; index++) {
			values[index] = input.readDouble();
		}
		return values;
	}

	private static void writeIntArray(DataOutput output, int[] values) throws IOException {
		output.writeInt(values.length);
		for (int value : values) {
			output.writeInt(value);
		}
	}

	private static int[] readIntArray(DataInput input) throws IOException {
		int length = checkedLength(input.readInt());
		int[] values = new int[length];
		for (int index = 0; index < length; index++) {
			values[index] = input.readInt();
		}
		return values;
	}

	private static void writeDoubleMatrix(DataOutput output, double[][] values) throws IOException {
		output.writeInt(values.length);
		for (double[] row : values) {
			writeDoubleArray(output, row);
		}
	}

	private static double[][] readDoubleMatrix(DataInput input) throws IOException {
		int rows = checkedLength(input.readInt());
		double[][] values = new double[rows][];
		for (int row = 0; row < rows; row++) {
			values[row] = readDoubleArray(input);
		}
		return values;
	}

	private static void writeIntMatrix(DataOutput output, int[][] values) throws IOException {
		output.writeInt(values.length);
		for (int[] row : values) {
			writeIntArray(output, row);
		}
	}

	private static int[][] readIntMatrix(DataInput input) throws IOException {
		int rows = checkedLength(input.readInt());
		int[][] values = new int[rows][];
		for (int row = 0; row < rows; row++) {
			values[row] = readIntArray(input);
		}
		return values;
	}

	private static int checkedLength(int length) throws IOException {
		if (length < 1 || length > 10_000) {
			throw new IOException("Invalid BMT geometry array length: " + length);
		}
		return length;
	}

}
