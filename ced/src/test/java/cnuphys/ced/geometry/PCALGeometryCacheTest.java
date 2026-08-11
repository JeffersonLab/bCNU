package cnuphys.ced.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.junit.jupiter.api.Test;

class PCALGeometryCacheTest {

	@Test
	void restoresAndRoundTripsExplicitGeometry() throws Exception {
		PCALGeometry restored = new PCALGeometry();
		restored.readGeometry(new DataInputStream(new ByteArrayInputStream(createPayload())));
		float[] strip = new float[24];
		PCALGeometry.getStrip(6, 3, 62, strip);
		assertTrue(Float.isFinite(strip[0]));
		assertEquals(PCALGeometry.PCAL_NUMSTRIP[2], 62);
		assertTrue(Double.isFinite(PCALGeometry.zFromX(0.0)));

		ByteArrayOutputStream saved = new ByteArrayOutputStream();
		restored.writeGeometry(new DataOutputStream(saved));
		assertTrue(saved.size() > 0);
	}

	private static byte[] createPayload() throws Exception {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (DataOutputStream output = new DataOutputStream(bytes)) {
			writePoint(output, 1.0);
			output.writeDouble(1.0);
			output.writeDouble(0.0);
			output.writeDouble(2.0);
			writeIdentity(output);
			writeIdentity(output);
			for (int view = 0; view < 3; view++) {
				int count = PCALGeometry.PCAL_NUMSTRIP[view];
				output.writeInt(count);
				for (int strip = 0; strip < count; strip++) {
					for (int point = 0; point < 4; point++) {
						writePoint(output, view * 10_000.0 + strip * 100.0 + point * 3.0);
					}
					for (int edge = 0; edge < 4; edge++) {
						writePoint(output, strip * 100.0 + edge * 6.0);
						writePoint(output, strip * 100.0 + edge * 6.0 + 3.0);
					}
				}
			}
			for (int sector = 0; sector < 6; sector++) {
				for (int view = 0; view < 3; view++) {
					for (int point = 0; point < 3; point++) {
						writePoint(output, sector * 100_000.0 + view * 10_000.0 + point * 3.0);
					}
					for (int strip = 0; strip < PCALGeometry.PCAL_NUMSTRIP[view]; strip++) {
						for (int corner = 0; corner < 8; corner++) {
							writePoint(output, sector * 100_000.0 + view * 10_000.0
									+ strip * 100.0 + corner * 3.0);
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
				output.writeDouble((column == row) ? 1.0 : 0.0);
			}
		}
	}

	private static void writePoint(DataOutputStream output, double base) throws Exception {
		output.writeDouble(base);
		output.writeDouble(base + 1.0);
		output.writeDouble(base + 2.0);
	}
}
