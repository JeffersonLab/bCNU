package cnuphys.ced.alldata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;

import org.jlab.io.base.DataBank;
import org.junit.jupiter.api.Test;

class BSTClustersTest {

    @Test
    void readsEndpointsAndValidatesRows() {
        BSTClusters clusters = BSTClusters.forTesting(BSTClustersTest::bank);

        assertEquals(1, clusters.count());
        assertTrue(clusters.hasRow(0));
        assertFalse(clusters.hasRow(1));
        assertEquals(3, clusters.sector(0));
        assertEquals(1.25f, clusters.x1(0));
        assertEquals(4.5f, clusters.y2(0));
    }

    private static DataBank bank() {
        return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "rows" -> 1;
                    case "getByte" -> (byte) 3;
                    case "getFloat" -> switch ((String) args[0]) {
                        case "x1" -> 1.25f;
                        case "y1" -> 2.5f;
                        case "x2" -> 3.75f;
                        case "y2" -> 4.5f;
                        default -> Float.NaN;
                    };
                    default -> null;
                });
    }
}
