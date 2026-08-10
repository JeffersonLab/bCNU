package cnuphys.ced.alldata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.jlab.io.base.DataBank;
import org.junit.jupiter.api.Test;

class URWTHitsTest {

    @Test
    void readsGeometryAndPreservesFeedback() {
        URWTHits hits = URWTHits.forTesting(URWTHitsTest::bank);

        assertEquals(1, hits.count());
        assertTrue(hits.hasRow(0));
        assertFalse(hits.hasRow(1));
        assertEquals(3, hits.sector(0));
        assertEquals(2, hits.layer(0));
        assertEquals(417, hits.strip(0));
        assertTrue(hits.hasValidGeometry(0));

        List<String> feedback = new ArrayList<>();
        hits.addFeedback(0, feedback);
        assertEquals(List.of("hit sector 3 layer 2 strip 417"), feedback);
    }

    @Test
    void handlesMissingBank() {
        URWTHits hits = URWTHits.forTesting(() -> null);
        assertEquals(0, hits.count());
        assertFalse(hits.hasRow(0));
        assertFalse(hits.hasValidGeometry(0));
    }

    private static DataBank bank() {
        return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "rows" -> 1;
                    case "getByte" -> "sector".equals(args[0]) ? (byte) 3 : (byte) 2;
                    case "getShort" -> (short) 417;
                    default -> null;
                });
    }
}
