package cnuphys.ced.geometry.ftof;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.junit.jupiter.api.Test;

class FTOFGeometryCacheTest {

	@Test
	void restoresExplicitPaddleGeometry() throws Exception {
		FTOFGeometry geometry = new FTOFGeometry();
		geometry.readGeometry(new DataInputStream(new ByteArrayInputStream(createPayload())));

		float[] vertices = new float[24];
		FTOFGeometry.paddleVertices(2, FTOFGeometry.PANEL_1B, 1, vertices);
		assertEquals(2100.0f, vertices[0]);
		assertEquals(0.0f, vertices[1]);
		assertEquals(1.0f, vertices[2]);
		assertEquals(42.0, FTOFGeometry.getLength(FTOFGeometry.PANEL_1B, 0));

		ByteArrayOutputStream saved = new ByteArrayOutputStream();
		geometry.writeGeometry(new DataOutputStream(saved));
		assertTrue(saved.size() > 0);
	}

	private static byte[] createPayload() throws Exception {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (DataOutputStream output = new DataOutputStream(bytes)) {
			output.writeInt(6);
			output.writeInt(3);
			output.writeInt(4);
			output.writeInt(4);
			output.writeInt(4);
			for (int sector = 0; sector < 6; sector++) {
				for (int panel = 0; panel < 3; panel++) {
					for (int paddle = 0; paddle < 4; paddle++) {
						double base = (sector + 1) * 1000.0 + panel * 100.0 + paddle * 10.0;
						for (int corner = 0; corner < 8; corner++) {
							output.writeDouble(base + ((corner == 1 || corner == 2 || corner == 5 || corner == 6) ? 1.0 : 0.0));
							output.writeDouble(paddle * 2.0 + ((corner >= 4) ? 1.0 : 0.0));
							output.writeDouble(panel);
						}
						for (int edge = 0; edge < 4; edge++) {
							writePoint(output, base + edge * 6.0);
							writePoint(output, base + edge * 6.0 + 3.0);
						}
						output.writeDouble(41.0 + panel);
					}
				}
			}
		}
		return bytes.toByteArray();
	}

	private static void writePoint(DataOutputStream output, double base) throws Exception {
		output.writeDouble(base);
		output.writeDouble(base + 1.0);
		output.writeDouble(base + 2.0);
	}
}
