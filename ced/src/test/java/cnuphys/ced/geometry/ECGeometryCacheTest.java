package cnuphys.ced.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.junit.jupiter.api.Test;

class ECGeometryCacheTest {

	@Test
	void restoresAndRoundTripsExplicitGeometry() throws Exception {
		ECGeometry geometry = new ECGeometry();
		geometry.readGeometry(new DataInputStream(new ByteArrayInputStream(createPayload())));
		float[] triangle = new float[9];
		ECGeometry.getViewTriangle(6, 2, 3, triangle);
		assertTrue(Float.isFinite(triangle[0]));
		assertEquals(36, ECGeometry.EC_NUMSTRIP);
		assertTrue(Double.isFinite(ECGeometry.zFromX(ECGeometry.EC_OUTER, 0)));

		ByteArrayOutputStream saved = new ByteArrayOutputStream();
		geometry.writeGeometry(new DataOutputStream(saved));
		assertTrue(saved.size() > 0);
	}

	private static byte[] createPayload() throws Exception {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (DataOutputStream output = new DataOutputStream(bytes)) {
			for (int plane = 0; plane < 2; plane++) {
				writePoint(output, plane * 1000.0 + 1.0);
				output.writeDouble(15.0 + plane);
				output.writeDouble(2.0 + plane);
				writeIdentity(output);
				writeIdentity(output);
				for (int view = 0; view < 3; view++) {
					for (int strip = 0; strip < 36; strip++) {
						for (int point = 0; point < 4; point++) {
							writePoint(output, plane * 100_000.0 + view * 10_000.0 + strip * 100.0 + point * 3.0);
						}
						for (int edge = 0; edge < 4; edge++) {
							writePoint(output, strip * 100.0 + edge * 6.0);
							writePoint(output, strip * 100.0 + edge * 6.0 + 3.0);
						}
					}
				}
			}
			output.writeDouble(0.4);
			output.writeDouble(0.9);
			output.writeDouble(0.4);
			output.writeDouble(0.5);
			for (int sector = 0; sector < 6; sector++) {
				for (int plane = 0; plane < 2; plane++) {
					for (int view = 0; view < 3; view++) {
						for (int point = 0; point < 3; point++) {
							writePoint(output, sector * 100_000.0 + plane * 10_000.0 + view * 1000.0 + point * 3.0);
						}
					}
				}
			}
		}
		return bytes.toByteArray();
	}

	private static void writeIdentity(DataOutputStream output) throws Exception {
		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 4; column++) {
				output.writeDouble((row == column) ? 1.0 : 0.0);
			}
		}
	}

	private static void writePoint(DataOutputStream output, double base) throws Exception {
		output.writeDouble(base);
		output.writeDouble(base + 1.0);
		output.writeDouble(base + 2.0);
	}
}
