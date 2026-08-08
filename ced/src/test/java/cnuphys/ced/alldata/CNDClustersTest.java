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

class CNDClustersTest {

    @Test
    void readsCurrentBankAndSupportsFeedbackAcrossFreshWrappers() {
        CNDClusters clusters = CNDClusters.forTesting(CNDClustersTest::bank);

        assertEquals(1, clusters.count());
        assertEquals(10, clusters.sector(0));
        assertEquals(2, clusters.layer(0));
        assertEquals(2, clusters.id(0));
        assertEquals(18.348f, clusters.energy(0), 1.0e-5f);

        clusters.setLocation(0, new Point(100, 200));
        assertTrue(clusters.contains(0, new Point(102, 198)));
        assertFalse(clusters.contains(0, new Point(120, 200)));

        List<String> feedback = new ArrayList<>();
        clusters.addFeedback(0, feedback);
        assertEquals(3, feedback.size());
        assertEquals("$magenta$CND cluster ID 2  status 0", feedback.get(2));
    }

    private static DataBank bank() {
        return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "rows" -> 1;
                    case "getByte" -> "sector".equals(args[0]) ? (byte) 10 : (byte) 2;
                    case "getShort" -> "id".equals(args[0]) ? (short) 2 : (short) 0;
                    case "getFloat" -> switch ((String) args[0]) {
                        case "x" -> -25.262f;
                        case "y" -> 22.154f;
                        case "z" -> 9.613f;
                        case "energy" -> 18.348f;
                        case "time" -> 101.019f;
                        default -> Float.NaN;
                    };
                    default -> null;
                });
    }
}
