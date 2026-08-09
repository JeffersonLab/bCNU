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

class BMTRecHitsTest {

    @Test
    void preservesEventOneHitAndFeedbackAcrossFreshWrappers() {
        BMTRecHits hits = BMTRecHits.forTesting(BMTRecHitsTest::bank);

        assertEquals(1, hits.count());
        assertEquals(2, hits.sector(0));
        assertEquals(5, hits.layer(0));
        assertEquals(660, hits.strip(0));
        assertEquals(18, hits.id(0));
        assertEquals(5, hits.clusterId(0));
        assertEquals(1, hits.trackId(0));

        hits.setLocation(0, new Point(100, 200));
        assertTrue(hits.contains(0, new Point(102, 198)));
        assertFalse(hits.contains(0, new Point(120, 200)));

        List<String> feedback = new ArrayList<>();
        hits.addFeedback(0, feedback);
        assertEquals(4, feedback.size());
        assertEquals("$wheat$BMTRecHit cluster 5 track 1", feedback.get(3));
    }

    private static DataBank bank() {
        return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "rows" -> 1;
                    case "getByte" -> switch ((String) args[0]) {
                        case "sector" -> (byte) 2;
                        case "layer" -> (byte) 5;
                        default -> (byte) 0;
                    };
                    case "getShort" -> switch ((String) args[0]) {
                        case "strip" -> (short) 660;
                        case "ID" -> (short) 18;
                        case "clusterID" -> (short) 5;
                        case "trkID" -> (short) 1;
                        default -> (short) 0;
                    };
                    case "getFloat" -> switch ((String) args[0]) {
                        case "energy" -> 53.0f;
                        case "time" -> 148.0f;
                        case "fitResidual" -> 0.25f;
                        default -> Float.NaN;
                    };
                    default -> null;
                });
    }
}
