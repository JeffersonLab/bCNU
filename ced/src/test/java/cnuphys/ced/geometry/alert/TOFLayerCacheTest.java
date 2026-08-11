package cnuphys.ced.geometry.alert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.junit.jupiter.api.Test;

class TOFLayerCacheTest {

	@Test
	void restoresPaddleIdsAndCorners() throws Exception {
		TOFLayer layer = TOFLayer.readFromCache(new DataInputStream(new ByteArrayInputStream(createPayload())));
		assertEquals(4, layer.sector);
		assertEquals(1, layer.superlayer);
		assertEquals(3, layer.layer);
		assertEquals(2, layer.numPaddles);
		assertNotNull(layer.getPaddle(1));
		assertEquals(11, layer.getPaddle(1).getComponentId());
		assertEquals(100.0, layer.getPaddle(1).getVolumePoint(0).x());

		ByteArrayOutputStream saved = new ByteArrayOutputStream();
		layer.writeToCache(new DataOutputStream(saved));
		assertEquals(createPayload().length, saved.size());
	}

	private static byte[] createPayload() throws Exception {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (DataOutputStream output = new DataOutputStream(bytes)) {
			output.writeInt(4);
			output.writeInt(1);
			output.writeInt(3);
			output.writeInt(2);
			for (int paddle = 0; paddle < 2; paddle++) {
				output.writeInt(10 + paddle);
				double[][] corners = {
					{0, 1, 0}, {1, 1, 0}, {1, 0, 0}, {0, 0, 0},
					{0, 1, 1}, {1, 1, 1}, {1, 0, 1}, {0, 0, 1}
				};
				for (int corner = 0; corner < 8; corner++) {
					output.writeDouble(paddle * 100.0 + corners[corner][0]);
					output.writeDouble(corners[corner][1]);
					output.writeDouble(corners[corner][2]);
				}
			}
		}
		return bytes.toByteArray();
	}
}
