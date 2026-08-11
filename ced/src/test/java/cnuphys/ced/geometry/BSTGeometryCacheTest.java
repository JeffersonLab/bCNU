package cnuphys.ced.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.junit.jupiter.api.Test;

class BSTGeometryCacheTest {

	@Test
	void restoresExplicitStripLinesAndDerivedPanels() throws Exception {
		BSTGeometry geometry = new BSTGeometry();
		geometry.readGeometry(new DataInputStream(new ByteArrayInputStream(createPayload())));

		assertNotNull(BSTGeometry.getStrip(9, 0, 255));
		assertEquals(84, BSTGeometry.getBSTxyPanels().size());
		float[] coordinates = new float[6];
		BSTGeometry.getStrip(0, 0, 0, coordinates);
		assertEquals(0.0f, coordinates[0]);
		assertEquals(5.0f, coordinates[5]);

		ByteArrayOutputStream saved = new ByteArrayOutputStream();
		geometry.writeGeometry(new DataOutputStream(saved));
		assertTrue(saved.size() > 0);
	}

	private static byte[] createPayload() throws Exception {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (DataOutputStream output = new DataOutputStream(bytes)) {
			output.writeInt(6);
			output.writeInt(256);
			for (int layer = 0; layer < 6; layer++) {
				output.writeInt(BSTGeometry.sectorsPerLayer[layer]);
				for (int sector = 0; sector < BSTGeometry.sectorsPerLayer[layer]; sector++) {
					for (int strip = 0; strip < 256; strip++) {
						double base = layer * 1_000_000.0 + sector * 10_000.0 + strip * 10.0;
						for (int coordinate = 0; coordinate < 6; coordinate++) {
							output.writeDouble(base + coordinate);
						}
					}
				}
			}
		}
		return bytes.toByteArray();
	}
}
