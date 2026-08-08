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

class FTOFClustersTest {

    @Test
    void readsCurrentBankAndSupportsFeedbackAcrossFreshWrappers() {
        FTOFClusters clusters = FTOFClusters.forTesting(FTOFClustersTest::bank);

        assertEquals(1, clusters.count());
        assertEquals(2, clusters.sector(0));
        assertEquals(1, clusters.layer(0));
        assertEquals(7, clusters.id(0));
        assertEquals(95.46f, clusters.x(0), 1.0e-5f);

        clusters.setLocation(0, new Point(100, 200));
        assertTrue(clusters.contains(0, new Point(102, 198)));
        assertFalse(clusters.contains(0, new Point(120, 200)));

        List<String> feedback = new ArrayList<>();
        clusters.addFeedback(0, feedback);
        assertEquals(3, feedback.size());
        assertEquals("$magenta$FTOF cluster ID 7  status 1", feedback.get(2));
    }

    private static DataBank bank() {
        return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "rows" -> 1;
                    case "getByte" -> "sector".equals(args[0]) ? (byte) 2 : (byte) 1;
                    case "getShort" -> switch ((String) args[0]) {
                        case "id" -> (short) 7;
                        case "status" -> (short) 1;
                        default -> (short) 3;
                    };
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
