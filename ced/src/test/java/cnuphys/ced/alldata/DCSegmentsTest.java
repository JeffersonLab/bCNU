package cnuphys.ced.alldata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Point;
import java.lang.reflect.Proxy;

import org.jlab.io.base.DataBank;
import org.junit.jupiter.api.Test;

class DCSegmentsTest {
    @Test
    void readsCurrentSegmentBank() {
        DCSegments segments = DCSegments.forTesting(DCSegmentsTest::bank);
        assertEquals(1, segments.count());
        assertEquals(6, segments.sector(0));
        assertEquals(4, segments.superlayer(0));
        assertEquals(-15.5f, segments.x1(0));
        assertEquals(230.0f, segments.z2(0));
    }

    @Test
    void hitTestsTheWholeDrawnSegment() {
        Point start = new Point(10, 20);
        Point end = new Point(110, 20);
        assertTrue(DCSegments.isNearScreenLine(start, end, new Point(60, 24), 5.0));
        assertFalse(DCSegments.isNearScreenLine(start, end, new Point(60, 27), 5.0));
        assertFalse(DCSegments.isNearScreenLine(start, end, new Point(120, 20), 5.0));
    }

    private static DataBank bank() {
        return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "rows" -> 1;
                    case "getByte" -> "sector".equals(args[0]) ? (byte) 6 : (byte) 4;
                    case "getFloat" -> switch ((String) args[0]) {
                        case "SegEndPoint1X" -> -15.5f;
                        case "SegEndPoint1Z" -> 120.0f;
                        case "SegEndPoint2X" -> 22.5f;
                        case "SegEndPoint2Z" -> 230.0f;
                        default -> 0.0f;
                    };
                    default -> null;
                });
    }
}
