package cnuphys.ced.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.junit.jupiter.api.Test;

class FTCALGeometryCacheTest {

	@Test
	void restoresExplicitPaddleCornersAndGridIndices() throws Exception {
		byte[] payload = createPayload();
		FTCALGeometry geometry = new FTCALGeometry();
		geometry.readGeometry(new DataInputStream(new ByteArrayInputStream(payload)));

		assertTrue(FTCALGeometry.isGoodId(1));
		assertFalse(FTCALGeometry.isGoodId(333));
		assertEquals(1, FTCALGeometry.xyIndicesToId(-11, -10));

		float[] vertices = new float[24];
		FTCALGeometry.paddleVertices(1, vertices);
		assertEquals(100.0f, vertices[0]);
		assertEquals(101.0f, vertices[1]);
		assertEquals(102.0f - FTCALGeometry.FTCAL_Z0, vertices[2]);

		ByteArrayOutputStream savedBytes = new ByteArrayOutputStream();
		geometry.writeGeometry(new DataOutputStream(savedBytes));
		assertTrue(savedBytes.size() > 0);
	}

	private static byte[] createPayload() throws Exception {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (DataOutputStream output = new DataOutputStream(bytes)) {
			output.writeInt(FTCALGeometry.MAXID + 1);
			for (int id = 0; id <= FTCALGeometry.MAXID; id++) {
				boolean present = id >= 1 && id <= 332;
				output.writeBoolean(present);
				if (present) {
					for (int corner = 0; corner < 8; corner++) {
						for (int coordinate = 0; coordinate < 3; coordinate++) {
							output.writeDouble(id * 100.0 + corner * 10.0 + coordinate);
						}
					}
				}
			}
			output.writeInt(332);
			for (short id = 1; id <= 332; id++) {
				output.writeShort(id);
				output.writeInt(id - 12);
				output.writeInt(id - 11);
			}
		}
		return bytes.toByteArray();
	}
}
