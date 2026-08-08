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

class FTOFHitsTest {

    @Test
    void readsCurrentBankAndKeepsOnlyDrawingLocations() {
        // A DataEvent is allowed to return a new bank wrapper for each lookup.
        FTOFHits hits = FTOFHits.forTesting(FTOFHitsTest::bank);

        assertEquals(1, hits.count());
        assertEquals(2, hits.sector(0));
        assertEquals(1, hits.layer(0));
        assertEquals(15, hits.id(0));
        assertEquals(95.46f, hits.x(0), 1.0e-5f);

        Point location = new Point(100, 200);
        hits.setLocation(0, location);
        assertTrue(hits.contains(0, new Point(102, 198)));
        assertFalse(hits.contains(0, new Point(120, 200)));

        List<String> feedback = new ArrayList<>();
        hits.addFeedback(0, feedback);
        assertEquals("$wheat$FTOF id 15 hit loc (95.46, 128.84, 689.66) cm", feedback.get(0));
    }

    private static DataBank bank() {
        return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "rows" -> 1;
                    case "getByte" -> "sector".equals(args[0]) ? (byte) 2 : (byte) 1;
                    case "getShort" -> "id".equals(args[0]) ? (short) 15 : (short) 3;
                    case "getFloat" -> switch ((String) args[0]) {
                        case "x" -> 95.46f;
                        case "y" -> 128.84f;
                        case "z" -> 689.66f;
                        case "energy" -> 1.25f;
                        case "time" -> 73.5f;
                        default -> Float.NaN;
                    };
                    default -> null;
                });
    }
}
