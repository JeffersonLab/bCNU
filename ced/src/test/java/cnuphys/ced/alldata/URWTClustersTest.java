package cnuphys.ced.alldata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.jlab.io.base.DataBank;
import org.junit.jupiter.api.Test;

class URWTClustersTest {

    @Test
    void readsGeometryAndPreservesFeedback() {
        URWTClusters clusters = URWTClusters.forTesting(URWTClustersTest::bank);

        assertEquals(1, clusters.count());
        assertTrue(clusters.hasValidGeometry(0));
        assertFalse(clusters.hasRow(1));
        assertEquals(5, clusters.sector(0));
        assertEquals(4, clusters.layer(0));
        assertEquals(731, clusters.strip(0));
        assertEquals(1.5f, clusters.xo(0));
        assertEquals(6.5f, clusters.ze(0));

        List<String> feedback = new ArrayList<>();
        clusters.addFeedback(0, feedback);
        assertEquals(List.of("cluster sector 5 layer 4 strip 731"), feedback);
    }

    @Test
    void handlesMissingBank() {
        URWTClusters clusters = URWTClusters.forTesting(() -> null);
        assertEquals(0, clusters.count());
        assertFalse(clusters.hasValidGeometry(0));
    }

    private static DataBank bank() {
        return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "rows" -> 1;
                    case "getByte" -> "sector".equals(args[0]) ? (byte) 5 : (byte) 4;
                    case "getShort" -> (short) 731;
                    case "getFloat" -> switch ((String) args[0]) {
                        case "xo" -> 1.5f;
                        case "yo" -> 2.5f;
                        case "zo" -> 3.5f;
                        case "xe" -> 4.5f;
                        case "ye" -> 5.5f;
                        default -> 6.5f;
                    };
                    default -> null;
                });
    }
}
