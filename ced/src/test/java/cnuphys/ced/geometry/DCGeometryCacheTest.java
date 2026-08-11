package cnuphys.ced.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.junit.jupiter.api.Test;

class DCGeometryCacheTest {

	@Test
	void restoresSenseWiresAndHexagonalVolumes() throws Exception {
		byte[] payload = createPayload();
		DCGeometry geometry = new DCGeometry();
		geometry.readGeometry(new DataInputStream(new ByteArrayInputStream(payload)));

		assertNotNull(DCGeometry.getWire(6, 6, 112));
		assertEquals(111, DCGeometry.getWire(6, 6, 112).getComponentId());
		assertEquals(-5.0, DCGeometry.getWire(1, 1, 1).getLine().origin().x());
		assertEquals(5.0, DCGeometry.getAbsMaxWireX());
		assertEquals(12, DCGeometry.getWire(1, 1, 1).getNumVolumePoints());

		ByteArrayOutputStream saved = new ByteArrayOutputStream();
		geometry.writeGeometry(new DataOutputStream(saved));
		assertEquals(payload.length, saved.size());
	}

	private static byte[] createPayload() throws Exception {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (DataOutputStream output = new DataOutputStream(bytes)) {
			output.writeInt(6);
			output.writeInt(6);
			output.writeInt(112);
			for (int superlayer = 0; superlayer < 6; superlayer++) {
				for (int layer = 0; layer < 6; layer++) {
					for (int wire = 0; wire < 112; wire++) {
						output.writeInt(wire);
						writePoint(output, 0.0, 0.0, superlayer * 10.0 + layer);
						writePoint(output, -5.0, wire, layer);
						writePoint(output, 5.0, wire, layer);
						for (int corner = 0; corner < 6; corner++) {
							double angle = Math.toRadians(-60.0 * corner);
							writePoint(output, -5.0, wire + Math.cos(angle), layer + Math.sin(angle));
						}
						for (int corner = 0; corner < 6; corner++) {
							double angle = Math.toRadians(-60.0 * corner);
							writePoint(output, 5.0, wire + Math.cos(angle), layer + Math.sin(angle));
						}
					}
				}
			}
		}
		return bytes.toByteArray();
	}

	private static void writePoint(DataOutputStream output, double x, double y, double z) throws Exception {
		output.writeDouble(x);
		output.writeDouble(y);
		output.writeDouble(z);
	}
}
