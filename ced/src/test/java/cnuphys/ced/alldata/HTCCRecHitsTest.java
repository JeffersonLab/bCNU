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

class HTCCRecHitsTest {

    @Test
    void readsCurrentBankAndPreservesLocationsAndFeedback() {
        HTCCRecHits hits = HTCCRecHits.forTesting(HTCCRecHitsTest::bank);
        assertEquals(1, hits.count());
        assertEquals(7, hits.id(0));
        assertEquals(12.5f, hits.x(0));

        assertFalse(hits.contains(0, new Point(40, 50)));
        hits.setLocation(0, new Point(40, 50));
        assertTrue(hits.contains(0, new Point(42, 48)));

        List<String> feedback = new ArrayList<>();
        hits.addFeedback(0, feedback);
        assertEquals("$Orange Red$HTCC id 7 hit loc (12.50, -3.25, 611.00) cm", feedback.get(0));
    }

    private static DataBank bank() {
        return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "rows" -> 1;
                    case "getShort" -> (short) 7;
                    case "getFloat" -> switch ((String) args[0]) {
                        case "x" -> 12.5f;
                        case "y" -> -3.25f;
                        case "z" -> 611.0f;
                        default -> 0.0f;
                    };
                    default -> null;
                });
    }
}
