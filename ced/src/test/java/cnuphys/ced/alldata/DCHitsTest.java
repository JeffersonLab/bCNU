package cnuphys.ced.alldata;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Point;
import java.lang.reflect.Proxy;
import java.util.ArrayList;

import org.jlab.io.base.DataBank;
import org.junit.jupiter.api.Test;

class DCHitsTest {
    @Test
    void readsHitAndMaintainsDrawingLocation() {
        DCHits hits = DCHits.forTesting(DCHitsTest::bank, "TBHit");
        assertEquals(1, hits.count());
        assertEquals(6, hits.sector(0));
        assertEquals(4, hits.superlayer(0));
        assertEquals(3, hits.layer(0));
        assertEquals(72, hits.wire(0));
        assertEquals(0, hits.indexFromId((short) 19));
        hits.setLocation(0, new Point(100, 200));
        assertTrue(hits.contains(0, new Point(103, 197)));
        var feedback = new ArrayList<String>();
        hits.addFeedback(0, feedback);
        assertTrue(feedback.getFirst().contains("TBHit sect 6 supl 4  layer 3  wire 72"));
    }

    private static DataBank bank() {
        return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "rows" -> 1;
                    case "getByte" -> switch ((String) args[0]) {
                        case "sector" -> (byte) 6; case "superlayer" -> (byte) 4; case "layer" -> (byte) 3;
                        default -> (byte) 1;
                    };
                    case "getShort" -> switch ((String) args[0]) {
                        case "wire" -> (short) 72; case "id" -> (short) 19; case "clusterID" -> (short) 8;
                        default -> (short) 0;
                    };
                    case "getInt" -> 145;
                    case "getFloat" -> 0.35f;
                    default -> null;
                });
    }
}
