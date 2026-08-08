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

class CTOFClustersTest {

    @Test
    void readsCurrentBankAndSupportsFeedbackAcrossFreshWrappers() {
        CTOFClusters clusters = CTOFClusters.forTesting(CTOFClustersTest::bank);

        assertEquals(1, clusters.count());
        assertEquals(1, clusters.id(0));
        assertEquals(16.205f, clusters.energy(0), 1.0e-5f);

        clusters.setLocation(0, new Point(100, 200));
        assertTrue(clusters.contains(0, new Point(102, 198)));
        assertFalse(clusters.contains(0, new Point(120, 200)));

        List<String> feedback = new ArrayList<>();
        clusters.addFeedback(0, feedback);
        assertEquals(3, feedback.size());
        assertEquals("$magenta$CTOF cluster ID 1  status 0", feedback.get(2));
    }

    private static DataBank bank() {
        return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "rows" -> 1;
                    case "getShort" -> "id".equals(args[0]) ? (short) 1 : (short) 0;
                    case "getFloat" -> switch ((String) args[0]) {
                        case "x" -> 23.876f;
                        case "y" -> -11.774f;
                        case "z" -> 4.738f;
                        case "energy" -> 16.205f;
                        case "time" -> 105.0f;
                        default -> Float.NaN;
                    };
                    default -> null;
                });
    }
}
