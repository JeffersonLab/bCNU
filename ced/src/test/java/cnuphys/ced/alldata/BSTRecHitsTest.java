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

class BSTRecHitsTest {

    @Test
    void preservesIndependentXYAndZBaselinesAcrossFreshWrappers() {
        BSTRecHits hits = BSTRecHits.forTesting(BSTRecHitsTest::bank);

        assertEquals(2, hits.count());
        assertEquals(8, hits.sector(0));
        assertEquals(5, hits.layer(0));
        assertEquals(160, hits.strip(0));
        assertEquals(82, hits.id(0));
        assertEquals(33, hits.clusterId(0));
        assertEquals(1, hits.trackId(0));
        assertEquals(7, hits.sector(1));
        assertEquals(1, hits.layer(1));
        assertEquals(52, hits.strip(1));
        assertEquals(-1, hits.trackId(1));
        assertTrue(hits.hasValidGeometry(0, new int[] { 10, 10, 10, 10, 10, 10 }));

        hits.setLocation(1, new Point(100, 200));
        assertTrue(hits.contains(1, new Point(102, 198)));
        assertFalse(hits.contains(1, new Point(120, 200)));

        List<String> feedback = new ArrayList<>();
        hits.addFeedback(1, feedback);
        assertEquals("$wheat$BSTRecHit cluster 25 track -1", feedback.get(3));
    }

    private static DataBank bank() {
        return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
                (proxy, method, args) -> {
                    int row = args == null || args.length < 2 ? 0 : (int) args[1];
                    return switch (method.getName()) {
                        case "rows" -> 2;
                        case "getByte" -> switch ((String) args[0]) {
                            case "sector" -> (byte) (row == 0 ? 8 : 7);
                            case "layer" -> (byte) (row == 0 ? 5 : 1);
                            default -> (byte) 0;
                        };
                        case "getShort" -> switch ((String) args[0]) {
                            case "strip" -> (short) (row == 0 ? 160 : 52);
                            case "ID" -> (short) (row == 0 ? 82 : 47);
                            case "clusterID" -> (short) (row == 0 ? 33 : 25);
                            case "trkID" -> (short) (row == 0 ? 1 : -1);
                            default -> (short) 0;
                        };
                        case "getFloat" -> switch ((String) args[0]) {
                            case "energy" -> 127.5f;
                            case "time" -> row == 0 ? 235.0f : 241.0f;
                            case "fitResidual" -> 0.25f;
                            default -> Float.NaN;
                        };
                        default -> null;
                    };
                });
    }
}
