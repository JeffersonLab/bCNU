package cnuphys.ced.geometry.fmt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.junit.jupiter.api.Test;

class FMTLayerDataTest {

	@Test
	void restoresVerticesAndDerivedStripLine() throws Exception {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (DataOutputStream output = new DataOutputStream(bytes)) {
			output.writeInt(0);
			output.writeInt(0);
			output.writeInt(2);
			output.writeInt(1);
			for (int corner = 0; corner < 8; corner++) {
				output.writeDouble(corner < 4 ? 1.0 : 9.0);
				output.writeDouble(corner);
				output.writeDouble(30.0);
			}
		}

		FMTLayerData layer = FMTLayerData.readFromCache(
				new DataInputStream(new ByteArrayInputStream(bytes.toByteArray())));
		assertEquals(1, layer.stripCount());
		assertEquals(1.0, layer.stripLine(0).origin().x());
		assertEquals(9.0, layer.stripLine(0).end().x());
		assertEquals(1.5, layer.stripLine(0).origin().y());

		float[] vertices = new float[24];
		layer.copyStripVertices(0, vertices);
		assertEquals(9.0f, vertices[12]);
	}
}
