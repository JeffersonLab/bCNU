package cnuphys.ced.geometry.alert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.junit.jupiter.api.Test;

class DCLayerCacheTest {

	@Test
	void restoresExplicitWireEndpoints() throws Exception {
		DCLayer layer = DCLayer.readFromCache(new DataInputStream(new ByteArrayInputStream(createPayload())));
		assertEquals(2, layer.sector);
		assertEquals(1, layer.superlayer);
		assertEquals(0, layer.layer);
		assertEquals(3, layer.numWires);
		assertNotNull(layer.getLine(2));
		assertEquals(20.0, layer.getLine(2).origin().x());

		ByteArrayOutputStream saved = new ByteArrayOutputStream();
		layer.writeToCache(new DataOutputStream(saved));
		assertEquals(createPayload().length, saved.size());
	}

	@Test
	void restoresEmptyCcdbLayer() throws Exception {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (DataOutputStream output = new DataOutputStream(bytes)) {
			output.writeInt(0);
			output.writeInt(1);
			output.writeInt(2);
			output.writeInt(0);
		}

		DCLayer layer = DCLayer.readFromCache(
				new DataInputStream(new ByteArrayInputStream(bytes.toByteArray())));
		assertEquals(0, layer.numWires);
	}

	private static byte[] createPayload() throws Exception {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (DataOutputStream output = new DataOutputStream(bytes)) {
			output.writeInt(2);
			output.writeInt(1);
			output.writeInt(0);
			output.writeInt(3);
			for (int wire = 0; wire < 3; wire++) {
				for (int coordinate = 0; coordinate < 6; coordinate++) {
					output.writeDouble(wire * 10.0 + coordinate);
				}
			}
		}
		return bytes.toByteArray();
	}
}
