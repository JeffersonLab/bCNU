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

class FTCalHitsTest {

    @Test
    void readsCurrentBankAndSupportsFeedbackAcrossFreshWrappers() {
        FTCalHits hits = FTCalHits.forTesting(FTCalHitsTest::bank);

        assertEquals(1, hits.count());
        assertEquals(12, hits.id(0));
        assertEquals(-5.5f, hits.x(0), 1.0e-5f);

        hits.setLocation(0, new Point(100, 200));
        assertTrue(hits.contains(0, new Point(102, 198)));
        assertFalse(hits.contains(0, new Point(120, 200)));

        List<String> feedback = new ArrayList<>();
        hits.addFeedback(0, feedback);
        assertEquals("$Orange Red$FTCAL id 12 hit loc (-5.50,  8.25, 190.00) cm", feedback.get(0));
    }

    private static DataBank bank() {
        return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "rows" -> 1;
                    case "getShort" -> (short) 12;
                    case "getFloat" -> switch ((String) args[0]) {
                        case "x" -> -5.5f;
                        case "y" -> 8.25f;
                        case "z" -> 190.0f;
                        default -> Float.NaN;
                    };
                    default -> null;
                });
    }
}
