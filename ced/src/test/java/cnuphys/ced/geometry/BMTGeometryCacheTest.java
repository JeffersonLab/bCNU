package cnuphys.ced.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import cnuphys.ced.geometry.bmt.Constants;

class BMTGeometryCacheTest {

	@AfterEach
	void resetConstants() {
		Constants.setCRZRADIUS(new double[3]);
		Constants.setCRZNSTRIPS(new int[3]);
		Constants.setCRZSPACING(new double[3]);
		Constants.setCRZWIDTH(new double[3]);
		Constants.setCRZLENGTH(new double[3]);
		Constants.setCRZZMIN(new double[3]);
		Constants.setCRZZMAX(new double[3]);
		Constants.setCRZOFFSET(new double[3]);
		Constants.setCRZEDGE1(new double[3][3]);
		Constants.setCRZEDGE2(new double[3][3]);
		Constants.setCRZXPOS(new double[3]);
		Constants.setCRCRADIUS(new double[3]);
		Constants.setCRCNSTRIPS(new int[3]);
		Constants.setCRCSPACING(new double[3]);
		Constants.setCRCLENGTH(new double[3]);
		Constants.setCRCZMIN(new double[3]);
		Constants.setCRCZMAX(new double[3]);
		Constants.setCRCOFFSET(new double[3]);
		Constants.setCRCGROUP(new int[3][]);
		Constants.setCRCWIDTH(new double[3][]);
		Constants.setCRCEDGE1(new double[3][3]);
		Constants.setCRCEDGE2(new double[3][3]);
		Constants.setCRCXPOS(new double[3]);
		Constants.setThetaL(0.0);
		Constants.areConstantsLoaded = false;
	}

	@Test
	void restoresConstantsAndRuntimeGeometry() throws Exception {
		setSyntheticConstants();
		BMTGeometry source = new BMTGeometry();
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		source.writeGeometry(new DataOutputStream(bytes));

		Constants.setCRZRADIUS(new double[] {-1.0});
		BMTGeometry restored = new BMTGeometry();
		restored.readGeometry(new DataInputStream(new ByteArrayInputStream(bytes.toByteArray())));

		assertEquals(101.0, Constants.getCRZRADIUS()[0]);
		assertEquals(31, Constants.getCRCGROUP()[2][0]);
		assertEquals(0.25, Constants.getThetaL());
		assertNotNull(BMTGeometry.getGeometry());
	}

	private static void setSyntheticConstants() {
		Constants.setCRZRADIUS(doubles(101.0));
		Constants.setCRZNSTRIPS(ints(11));
		Constants.setCRZSPACING(doubles(1.0));
		Constants.setCRZWIDTH(doubles(2.0));
		Constants.setCRZLENGTH(doubles(3.0));
		Constants.setCRZZMIN(doubles(4.0));
		Constants.setCRZZMAX(doubles(5.0));
		Constants.setCRZOFFSET(doubles(6.0));
		Constants.setCRZEDGE1(matrix(7.0));
		Constants.setCRZEDGE2(matrix(8.0));
		Constants.setCRZXPOS(doubles(9.0));
		Constants.setCRCRADIUS(doubles(201.0));
		Constants.setCRCNSTRIPS(ints(21));
		Constants.setCRCSPACING(doubles(10.0));
		Constants.setCRCLENGTH(doubles(11.0));
		Constants.setCRCZMIN(doubles(12.0));
		Constants.setCRCZMAX(doubles(13.0));
		Constants.setCRCOFFSET(doubles(14.0));
		Constants.setCRCGROUP(new int[][] {{11}, {21}, {31}});
		Constants.setCRCWIDTH(matrix(15.0));
		Constants.setCRCEDGE1(matrix(16.0));
		Constants.setCRCEDGE2(matrix(17.0));
		Constants.setCRCXPOS(doubles(18.0));
		Constants.setThetaL(0.25);
	}

	private static double[] doubles(double start) {
		return new double[] {start, start + 1.0, start + 2.0};
	}

	private static int[] ints(int start) {
		return new int[] {start, start + 1, start + 2};
	}

	private static double[][] matrix(double start) {
		return new double[][] {
			{start, start + 1.0, start + 2.0},
			{start + 3.0, start + 4.0, start + 5.0},
			{start + 6.0, start + 7.0, start + 8.0}
		};
	}
}
