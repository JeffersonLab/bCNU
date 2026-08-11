package cnuphys.ced.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.geom.Point2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.junit.jupiter.api.Test;

class CNDGeometryCacheTest {

	@Test
	void restoresExplicitPaddleCorners() throws Exception {
		CNDGeometry geometry = new CNDGeometry();
		geometry.readGeometry(new DataInputStream(new ByteArrayInputStream(createPayload())));

		float[] vertices = new float[24];
		CNDGeometry.paddleVertices(2, 3, vertices);
		assertEquals(20300.0f, vertices[0]);
		assertEquals(20301.0f, vertices[1]);
		assertEquals(20302.0f, vertices[2]);

		Point2D.Double[] corners = new Point2D.Double[4];
		for (int index = 0; index < corners.length; index++) {
			corners[index] = new Point2D.Double();
		}
		CNDGeometry.paddleXYCorners(2, 3, corners);
		assertEquals(20300.0, corners[0].x);
		assertEquals(20301.0, corners[0].y);

		ByteArrayOutputStream saved = new ByteArrayOutputStream();
		geometry.writeGeometry(new DataOutputStream(saved));
		assertTrue(saved.size() > 0);
	}

	private static byte[] createPayload() throws Exception {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (DataOutputStream output = new DataOutputStream(bytes)) {
			output.writeInt(3);
			output.writeInt(48);
			output.writeInt(8);
			output.writeInt(3);
			for (int layer = 1; layer <= 3; layer++) {
				for (int paddle = 1; paddle <= 48; paddle++) {
					for (int corner = 0; corner < 8; corner++) {
						for (int coordinate = 0; coordinate < 3; coordinate++) {
							output.writeDouble(layer * 10000.0 + paddle * 100.0 + corner * 10.0 + coordinate);
						}
					}
				}
			}
		}
		return bytes.toByteArray();
	}
}
