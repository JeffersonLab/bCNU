package cnuphys.ced.alldata;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Point;
import java.lang.reflect.Proxy;
import java.util.ArrayList;

import org.jlab.io.base.DataBank;
import org.junit.jupiter.api.Test;

class DCCrossesTest {
    @Test
    void readsCrossAndMaintainsFeedbackLocation() {
        DCCrosses crosses = DCCrosses.forTesting(DCCrossesTest::bank, "HBCross");
        assertEquals(1, crosses.count());
        assertEquals(6, crosses.sector(0));
        assertEquals(2, crosses.region(0));
        assertEquals(12.5f, crosses.x(0));
        assertEquals(31, crosses.segment2Id(0));
        crosses.setLocation(0, new Point(50, 70));
        assertTrue(crosses.contains(0, new Point(52, 68)));
        var feedback = new ArrayList<String>();
        crosses.addFeedback(0, feedback);
        assertTrue(feedback.getFirst().contains("HBCross cross ID 7"));
    }

    private static DataBank bank() {
        return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "rows" -> 1;
                    case "getByte" -> "sector".equals(args[0]) ? (byte) 6 : (byte) 2;
                    case "getShort" -> switch ((String) args[0]) {
                        case "id" -> (short) 7; case "Segment1_ID" -> (short) 30; case "Segment2_ID" -> (short) 31;
                        default -> (short) 0;
                    };
                    case "getFloat" -> "x".equals(args[0]) ? 12.5f : 0.25f;
                    default -> null;
                });
    }
}
