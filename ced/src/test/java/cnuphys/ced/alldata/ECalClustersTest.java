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

class ECalClustersTest {

    @Test
    void mapsPCalAndECalRowsAndSupportsFeedbackAcrossFreshWrappers() {
        ECalClusters clusters = ECalClusters.forTesting(ECalClustersTest::bank);

        assertEquals(2, clusters.count());
        assertTrue(clusters.isPCal(0));
        assertEquals(0, clusters.view(0));
        assertEquals(54.866f, clusters.x(0), 1.0e-5f);
        assertTrue(clusters.isECal(1));
        assertEquals(1, clusters.plane(1));
        assertEquals(0, clusters.view(1));
        assertEquals(0.0713f, clusters.energy(1), 1.0e-5f);

        clusters.setLocation(1, new Point(100, 200));
        assertTrue(clusters.contains(1, new Point(102, 198)));
        assertFalse(clusters.contains(1, new Point(120, 200)));

        List<String> feedback = new ArrayList<>();
        clusters.addFeedback(1, feedback);
        assertEquals(4, feedback.size());
        assertEquals("$magenta$EC cluster plane Outer", feedback.get(1));
        assertEquals("$magenta$EC cluster view U", feedback.get(2));
    }

    private static DataBank bank() {
        return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "rows" -> 2;
                    case "getByte" -> switch ((String) args[0]) {
                        case "sector" -> (byte) 6;
                        case "layer" -> (byte) (((int) args[1] == 0) ? 1 : 7);
                        default -> (byte) 0;
                    };
                    case "getFloat" -> {
                        int row = (int) args[1];
                        yield switch ((String) args[0]) {
                            case "x" -> row == 0 ? 54.866f : 64.252f;
                            case "y" -> row == 0 ? -112.964f : -123.057f;
                            case "z" -> row == 0 ? 731.336f : 781.739f;
                            case "energy" -> row == 0 ? 1.027f : 0.0713f;
                            case "time" -> 100.0f;
                            default -> Float.NaN;
                        };
                    }
                    default -> null;
                });
    }
}
