package cnuphys.ced.geometry.alert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.junit.jupiter.api.Test;

class AlertGeometryCacheTest {

	@Test
	void restoresLayersAndDerivedSectorBoundaries() throws Exception {
		AlertGeometry geometry = new AlertGeometry();
		geometry.readGeometry(new DataInputStream(new ByteArrayInputStream(createPayload())));
		assertNotNull(AlertGeometry.getDCLayer(0, 0, 0));
		assertNotNull(AlertGeometry.getTOFLayer(14, 1, 3));
		assertNotNull(AlertGeometry.tofSectorXY[14][15]);
		assertEquals(120, AlertGeometry.getAllTOFLayers().size());

		ByteArrayOutputStream saved = new ByteArrayOutputStream();
		geometry.writeGeometry(new DataOutputStream(saved));
		assertTrue(saved.size() > 0);
	}

	private static byte[] createPayload() throws Exception {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (DataOutputStream output = new DataOutputStream(bytes)) {
			output.writeInt(1);
			writeDCLayer(output);
			output.writeInt(120);
			for (int sector = 0; sector < 15; sector++) {
				for (int superlayer = 0; superlayer < 2; superlayer++) {
					for (int layer = 0; layer < 4; layer++) {
						writeTOFLayer(output, sector, superlayer, layer);
					}
				}
			}
		}
		return bytes.toByteArray();
	}

	private static void writeDCLayer(DataOutputStream output) throws Exception {
		output.writeInt(0);
		output.writeInt(0);
		output.writeInt(0);
		output.writeInt(1);
		for (int coordinate = 0; coordinate < 6; coordinate++) {
			output.writeDouble(coordinate);
		}
	}

	private static void writeTOFLayer(DataOutputStream output, int sector, int superlayer, int layer) throws Exception {
		output.writeInt(sector);
		output.writeInt(superlayer);
		output.writeInt(layer);
		output.writeInt(1);
		output.writeInt(0);
		double angle = Math.toRadians(sector * 24.0);
		double radius = 100.0 + superlayer * 20.0 + layer * 2.0;
		double[][] corners = {
			{0, 1, 0}, {1, 1, 0}, {1, 0, 0}, {0, 0, 0},
			{0, 1, 1}, {1, 1, 1}, {1, 0, 1}, {0, 0, 1}
		};
		for (double[] corner : corners) {
			double radial = radius + corner[0];
			double tangential = corner[1];
			output.writeDouble(radial * Math.cos(angle) - tangential * Math.sin(angle));
			output.writeDouble(radial * Math.sin(angle) + tangential * Math.cos(angle));
			output.writeDouble(corner[2]);
		}
	}
}
