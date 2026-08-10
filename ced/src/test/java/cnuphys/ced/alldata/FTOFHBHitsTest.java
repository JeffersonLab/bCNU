package cnuphys.ced.alldata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.jlab.io.base.DataBank;
import org.junit.jupiter.api.Test;

class FTOFHBHitsTest {

    @Test
    void readsHitAndPreservesFeedback() {
        FTOFHBHits hits = FTOFHBHits.forTesting(FTOFHBHitsTest::bank);

        assertEquals(1, hits.count());
        assertTrue(hits.hasValidGeometry(0));
        assertFalse(hits.hasRow(1));
        assertEquals(4, hits.sector(0));
        assertEquals(2, hits.layer(0));

        List<String> feedback = new ArrayList<>();
        hits.addFeedback(0, feedback);
        assertEquals(List.of("$yellow$hb hit loc (  1.250,   2.500,   3.750)",
                "$yellow$hb hit energy   0.625 time  18.500 status 7"), feedback);
    }

    @Test
    void handlesMissingBank() {
        FTOFHBHits hits = FTOFHBHits.forTesting(() -> null);
        assertEquals(0, hits.count());
        assertFalse(hits.hasValidGeometry(0));
    }

    private static DataBank bank() {
        return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "rows" -> 1;
                    case "getByte" -> "sector".equals(args[0]) ? (byte) 4 : (byte) 2;
                    case "getShort" -> (short) 7;
                    case "getFloat" -> switch ((String) args[0]) {
                        case "x" -> 1.25f;
                        case "y" -> 2.5f;
                        case "z" -> 3.75f;
                        case "energy" -> 0.625f;
                        default -> 18.5f;
                    };
                    default -> null;
                });
    }
}
