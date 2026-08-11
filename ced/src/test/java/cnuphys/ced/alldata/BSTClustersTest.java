package cnuphys.ced.alldata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

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

    @Test
    void prefersModernBankFallsBackToLegacyAndSupportsFeedbackHitTesting() {
        DataBank modern = bank();
        DataBank legacy = bank();
        BSTClusters clusters = BSTClusters.forTesting(() -> modern, () -> legacy);
        assertEquals(BSTClusters.BANK_NAME, clusters.activeBankName());

        clusters.setLocations(0, new Point(20, 30), new Point(40, 50));
        assertTrue(clusters.contains(0, new Point(40, 50)));
        List<String> feedback = new ArrayList<>();
        clusters.addFeedback(0, feedback);
        assertTrue(feedback.get(0).contains("BST cluster"));

        clusters = BSTClusters.forTesting(() -> null, () -> legacy);
        assertEquals(BSTClusters.LEGACY_BANK_NAME, clusters.activeBankName());
        assertEquals(1, clusters.count());
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
