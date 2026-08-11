package cnuphys.ced.geometry.urwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.jlab.geom.prim.Line3D;
import org.junit.jupiter.api.Test;

class UrWTGeometryCacheTest {

	@Test
	void restoresExplicitStripEndpointsAndDerivedGeometry() throws Exception {
		UrWTGeometry geometry = new UrWTGeometry();
		geometry.readGeometry(new DataInputStream(new ByteArrayInputStream(createPayload())));

		Line3D strip = UrWTGeometry.getStrip(2, 3, 2);
		assertNotNull(strip);
		assertEquals(2302.0, strip.origin().x());
		assertEquals(1.0, strip.origin().y());
		assertEquals(2303.0, strip.end().x());

		UrWTDetectorData data = UrWTGeometry.getDetectorData(2, 3);
		assertEquals(4, data.count);
		assertNotNull(data.getConvexHull());
		assertTrue(data.getConvexHull().size() >= 4);
		assertEquals(data.getConvexHull().size(), data.getXYPoints().length);

		ByteArrayOutputStream saved = new ByteArrayOutputStream();
		geometry.writeGeometry(new DataOutputStream(saved));
		assertTrue(saved.size() > 0);
	}

	private static byte[] createPayload() throws Exception {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (DataOutputStream output = new DataOutputStream(bytes)) {
			output.writeInt(UrWTGeometry.NUM_SECTORS);
			output.writeInt(UrWTGeometry.NUM_LAYERS);
			for (int sector = 1; sector <= UrWTGeometry.NUM_SECTORS; sector++) {
				for (int layer = 1; layer <= UrWTGeometry.NUM_LAYERS; layer++) {
					output.writeInt(sector);
					output.writeInt(layer);
					output.writeInt(4);
					double base = sector * 1000.0 + layer * 100.0;
					for (int strip = 0; strip < 4; strip++) {
						output.writeDouble(base + strip * 2.0);
						output.writeDouble(strip);
						output.writeDouble(layer);
						output.writeDouble(base + strip * 2.0 + 1.0);
						output.writeDouble(strip);
						output.writeDouble(layer);
					}
				}
			}
		}
		return bytes.toByteArray();
	}
}
