package cnuphys.bCNU.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.geom.Point2D;

import org.junit.jupiter.api.Test;

class MathUtilitiesTest {

    private static final double TOLERANCE = 1.0e-12;

    @Test
    void convertsSignedIntsToUnsignedLongs() {
        assertEquals(0L, MathUtilities.getUnsignedInt(0));
        assertEquals(Integer.MAX_VALUE, MathUtilities.getUnsignedInt(Integer.MAX_VALUE));
        assertEquals(2_147_483_648L, MathUtilities.getUnsignedInt(Integer.MIN_VALUE));
        assertEquals(4_294_967_295L, MathUtilities.getUnsignedInt(-1));
    }

    @Test
    void projectsOntoTheInteriorOfASegment() {
        Point2D.Double intersection = new Point2D.Double();
        double t = MathUtilities.perpendicularIntersection(
                new Point2D.Double(0.0, 0.0),
                new Point2D.Double(10.0, 0.0),
                new Point2D.Double(4.0, 3.0),
                intersection);

        assertEquals(0.4, t, TOLERANCE);
        assertEquals(4.0, intersection.x, TOLERANCE);
        assertEquals(0.0, intersection.y, TOLERANCE);
    }

    @Test
    void distanceClampsToTheNearestSegmentEndpoint() {
        Point2D.Double intersection = new Point2D.Double();
        double distance = MathUtilities.perpendicularDistance(
                new Point2D.Double(0.0, 0.0),
                new Point2D.Double(10.0, 0.0),
                new Point2D.Double(13.0, 4.0),
                intersection);

        assertEquals(5.0, distance, TOLERANCE);
        assertEquals(13.0, intersection.x, TOLERANCE);
        assertEquals(0.0, intersection.y, TOLERANCE);
    }

    @Test
    void indexSortReturnsOriginalIndicesInValueOrder() {
        double[] values = { 8.0, -2.0, 3.5, -2.0 };

        assertArrayEquals(new int[] { 1, 3, 2, 0 }, MathUtilities.indexSort(values));
    }
}
