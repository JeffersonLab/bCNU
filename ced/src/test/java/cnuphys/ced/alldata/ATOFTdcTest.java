package cnuphys.ced.alldata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.jlab.io.base.DataBank;
import org.junit.jupiter.api.Test;

class ATOFTdcTest {

    @Test
    void readsGeometryAndPreservesFeedback() {
        ATOFTdc tdc = ATOFTdc.forTesting(ATOFTdcTest::bank);

        assertEquals(1, tdc.count());
        assertTrue(tdc.hasRow(0));
        assertFalse(tdc.hasRow(1));
        assertEquals(7, tdc.sector(0));
        assertEquals(2, tdc.layer(0));
        assertEquals(10, tdc.component(0));

        List<String> feedback = new ArrayList<>();
        tdc.addFeedback(0, feedback);
        assertEquals(List.of("$orange$TDC: 1454", "$orange$order: 1", "$orange$ToT: 32"), feedback);
    }

    private static DataBank bank() {
        String[] columns = { "sector", "layer", "component", "order", "TDC", "ToT" };
        return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "rows" -> 1;
                    case "getColumnList" -> columns;
                    case "getByte" -> "sector".equals(args[0]) ? (byte) 7
                            : "layer".equals(args[0]) ? (byte) 2 : (byte) 1;
                    case "getShort" -> (short) 10;
                    case "getInt" -> "TDC".equals(args[0]) ? 1454 : 32;
                    default -> null;
                });
    }
}
