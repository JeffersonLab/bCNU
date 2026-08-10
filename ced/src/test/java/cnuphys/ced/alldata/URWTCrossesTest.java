package cnuphys.ced.alldata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.jlab.io.base.DataBank;
import org.junit.jupiter.api.Test;

class URWTCrossesTest {

    @Test
    void readsCrossAndPreservesFeedback() {
        URWTCrosses crosses = URWTCrosses.forTesting(URWTCrossesTest::bank);

        assertEquals(1, crosses.count());
        assertTrue(crosses.hasValidSector(0));
        assertFalse(crosses.hasRow(1));
        assertEquals(2, crosses.sector(0));
        assertEquals(17, crosses.id(0));
        assertEquals(1.25f, crosses.x(0));
        assertEquals(3.75f, crosses.z(0));

        List<String> feedback = new ArrayList<>();
        crosses.addFeedback(0, feedback);
        assertEquals(List.of("$cyan$cross: 17  status: 4", "$cyan$cross clusters: 8 and 12"), feedback);
    }

    @Test
    void handlesMissingBank() {
        URWTCrosses crosses = URWTCrosses.forTesting(() -> null);
        assertEquals(0, crosses.count());
        assertFalse(crosses.hasValidSector(0));
    }

    private static DataBank bank() {
        return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "rows" -> 1;
                    case "getByte" -> (byte) 2;
                    case "getShort" -> switch ((String) args[0]) {
                        case "id" -> (short) 17;
                        case "cluster1" -> (short) 8;
                        case "cluster2" -> (short) 12;
                        default -> (short) 4;
                    };
                    case "getFloat" -> "x".equals(args[0]) ? 1.25f : "y".equals(args[0]) ? 2.5f : 3.75f;
                    default -> null;
                });
    }
}
