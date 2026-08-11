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
			writeIdentity(output);
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
		float[] global = new float[3];
		layer.localToGlobal(2f, 3f, 4f, global);
		assertEquals(2f, global[0]);
		assertEquals(3f, global[1]);
		assertEquals(4f, global[2]);
	}

	private static void writeIdentity(DataOutputStream output) throws Exception {
		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 4; column++) {
				output.writeDouble(row == column ? 1.0 : 0.0);
			}
		}
	}
}
