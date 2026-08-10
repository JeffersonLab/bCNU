package cnuphys.ced.cedview.alert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AlertTOFGeometryNumberingTest {

	@Test
	void convertsValidHipoNumbering() {
		AlertTOFGeometryNumbering numbering = new AlertTOFGeometryNumbering();

		assertTrue(numbering.fromHipoNumbering(4, 2, 7, 0));
		assertEquals(4, numbering.sector);
		assertEquals(2, numbering.layer);
		assertEquals(1, numbering.superlayer);
		assertEquals(7, numbering.paddleIndex);

		assertTrue(numbering.fromHipoNumbering(4, 2, 10, 0));
		assertEquals(0, numbering.superlayer);
		assertEquals(0, numbering.paddleIndex);
	}

	@Test
	void rejectsInvalidIncomingCoordinates() {
		AlertTOFGeometryNumbering numbering = new AlertTOFGeometryNumbering();

		assertFalse(numbering.fromHipoNumbering(-1, 0, 0, 0));
		assertFalse(numbering.fromHipoNumbering(0, -1, 0, 0));
		assertFalse(numbering.fromHipoNumbering(0, 4, 0, 0));
		assertFalse(numbering.fromHipoNumbering(0, 0, 11, 0));
	}
}
