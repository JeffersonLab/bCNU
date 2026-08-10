package cnuphys.ced.alldata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;

import org.jlab.io.base.DataBank;
import org.junit.jupiter.api.Test;

class BMTClustersTest {

    @Test
    void readsEndpointsAndHandlesMissingBank() {
        BMTClusters clusters = BMTClusters.forTesting(BMTClustersTest::bank);

        assertEquals(1, clusters.count());
        assertTrue(clusters.hasRow(0));
        assertFalse(clusters.hasRow(-1));
        assertEquals(2, clusters.sector(0));
        assertEquals(-3.5f, clusters.x1(0));
        assertEquals(8.0f, clusters.y2(0));
        assertEquals(0, BMTClusters.forTesting(() -> null).count());
    }

    private static DataBank bank() {
        return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "rows" -> 1;
                    case "getByte" -> (byte) 2;
                    case "getFloat" -> switch ((String) args[0]) {
                        case "x1" -> -3.5f;
                        case "y1" -> 4.0f;
                        case "x2" -> 7.5f;
                        case "y2" -> 8.0f;
                        default -> Float.NaN;
                    };
                    default -> null;
                });
    }
}
