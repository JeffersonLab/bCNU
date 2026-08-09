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

class CentralCrossesTest {
    @Test
    void readsBmtCrossAndPreservesFeedbackAndLocation() {
        CentralCrosses crosses = CentralCrosses.forTesting("BMT", true, CentralCrossesTest::bank);
        assertEquals(1, crosses.count());
        assertEquals(5, crosses.layer(0));
        assertEquals(12.5f, crosses.x(0));
        assertFalse(crosses.contains(0, new Point(30, 40)));
        crosses.setLocation(0, new Point(30, 40));
        assertTrue(crosses.contains(0, new Point(32, 38)));
        List<String> feedback = new ArrayList<>();
        crosses.addFeedback(0, feedback);
        assertEquals("$Forest Green$BMTCross cross layer 5 ID 7", feedback.get(0));
        assertEquals(6, feedback.size());
    }

    private static DataBank bank() {
        return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "rows" -> 1;
                    case "getByte" -> switch ((String) args[0]) {
                        case "sector" -> (byte) 2;
                        case "region" -> (byte) 3;
                        case "layer" -> (byte) 5;
                        default -> (byte) 0;
                    };
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
