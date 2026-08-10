package cnuphys.ced.alldata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Point;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.jlab.io.base.DataBank;
import org.junit.jupiter.api.Test;

class FMTCrossesTest {

    @Test
    void readsCrossAndPreservesFeedbackAndLocation() {
        FMTCrosses crosses = FMTCrosses.forTesting(FMTCrossesTest::bank);

        assertEquals(1, crosses.count());
        assertEquals(7, crosses.id(0));
        assertEquals(12.5f, crosses.x(0));
        assertFalse(crosses.contains(0, new Point(30, 40)));
        crosses.setLocation(0, new Point(30, 40));
        assertTrue(crosses.contains(0, new Point(32, 38)));

        List<String> feedback = new ArrayList<>();
        crosses.addFeedback(0, feedback);
        assertEquals("$Forest Green$FMTRec cross ID 7", feedback.get(0));
        assertEquals(5, feedback.size());
    }

    @Test
    void missingBankHasNoRows() {
        assertEquals(0, FMTCrosses.forTesting(() -> null).count());
    }

    private static DataBank bank() {
        return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "rows" -> 1;
                    case "getByte" -> "sector".equals(args[0]) ? (byte) 2 : (byte) 3;
                    case "getShort" -> (short) 7;
                    case "getFloat" -> switch ((String) args[0]) {
                        case "x" -> 12.5f;
                        case "y" -> -8.0f;
                        case "z" -> 42.0f;
                        case "ux" -> 0.1f;
                        case "uy" -> 0.2f;
                        case "uz" -> 0.3f;
                        default -> 0.01f;
                    };
                    default -> null;
                });
    }
}
