package cnuphys.ced.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.awt.geom.Point2D;

import org.junit.jupiter.api.Test;

class CherenkovAnalyticGeometryTest {

	@Test
	void htccGeometryIsAvailableWithoutInitialization() {
		Point2D.Double[] polygon = points();
		HTCCGeometry.getSimpleWorldPoly(1, 1, 0.0, polygon);
		assertFalse(Double.isNaN(polygon[0].x));
	}

	@Test
	void ltccGeometryIsAvailableWithoutInitialization() {
		Point2D.Double[] polygon = points();
		LTCCGeometry.getSimpleWorldPoly(18, 2, 0.0, polygon);
		assertFalse(Double.isNaN(polygon[0].x));
		assertEquals(4, polygon.length);
	}

	private static Point2D.Double[] points() {
		Point2D.Double[] points = new Point2D.Double[4];
		for (int index = 0; index < points.length; index++) {
			points[index] = new Point2D.Double();
		}
		return points;
	}
}
