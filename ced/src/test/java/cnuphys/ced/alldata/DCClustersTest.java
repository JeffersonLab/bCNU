package cnuphys.ced.alldata;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Proxy;

import org.jlab.io.base.DataBank;
import org.junit.jupiter.api.Test;

class DCClustersTest {
    @Test
    void readsCurrentClusterBank() {
        DCClusters clusters = DCClusters.forTesting(DCClustersTest::bank);
        assertEquals(1, clusters.count());
        assertEquals(5, clusters.sector(0));
        assertEquals(3, clusters.superlayer(0));
        assertEquals(27, clusters.id(0));
        assertEquals(3, clusters.size(0));
        assertEquals(42.5f, clusters.averageWire(0));
        assertArrayEquals(new short[] { 101, 102, 103, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, clusters.hitIds(0));
    }

    private static DataBank bank() {
        return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "rows" -> 1;
                    case "getByte" -> "sector".equals(args[0]) ? (byte) 5
                            : "superlayer".equals(args[0]) ? (byte) 3 : (byte) 3;
                    case "getShort" -> {
                        String column = (String) args[0];
                        if ("id".equals(column)) yield (short) 27;
                        if ("status".equals(column)) yield (short) 1;
                        if (column.startsWith("Hit")) {
                            int hit = Integer.parseInt(column.substring(3, column.indexOf('_')));
                            yield hit <= 3 ? (short) (100 + hit) : (short) 0;
                        }
                        yield (short) 0;
                    }
                    case "getFloat" -> "avgWire".equals(args[0]) ? 42.5f : 0.25f;
                    default -> null;
                });
    }
}
