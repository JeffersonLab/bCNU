package cnuphys.ced.geometry.fmt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.junit.jupiter.api.Test;

class FMTGeometryCacheTest {

	@Test
	void restoresLayerLookupAndWritesPayload() throws Exception {
		ByteArrayOutputStream payload = new ByteArrayOutputStream();
		try (DataOutputStream output = new DataOutputStream(payload)) {
			output.writeInt(1);
			output.writeInt(0);
			output.writeInt(0);
			output.writeInt(5);
			writeTranslation(output, 10.0, 20.0, 30.0);
			output.writeInt(1);
			for (int coordinate = 0; coordinate < 24; coordinate++) {
				output.writeDouble(coordinate);
			}
		}

		FMTGeometry geometry = new FMTGeometry();
		geometry.readGeometry(new DataInputStream(new ByteArrayInputStream(payload.toByteArray())));
		assertEquals(4.5, FMTGeometry.getStripLine(0, 0, 5, 0).origin().x());

		float[] vertices = new float[24];
		FMTGeometry.stripVertices(0, 0, 5, 0, vertices);
		assertEquals(23.0f, vertices[23]);
		float[] global = new float[3];
		FMTGeometry.localToGlobal(5, 1f, 2f, 3f, global);
		assertEquals(11f, global[0]);
		assertEquals(22f, global[1]);
		assertEquals(33f, global[2]);

		ByteArrayOutputStream saved = new ByteArrayOutputStream();
		geometry.writeGeometry(new DataOutputStream(saved));
		assertEquals(payload.size(), saved.size());
	}

	private static void writeTranslation(DataOutputStream output, double x, double y, double z) throws Exception {
		double[] translation = { x, y, z };
		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 4; column++) {
				output.writeDouble(column == row ? 1.0 : column == 3 ? translation[row] : 0.0);
			}
		}
	}
}
