package cnuphys.ced.alldata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;

import org.jlab.io.base.DataBank;
import org.junit.jupiter.api.Test;

class FMTAdcTest {

    private static final String[] COLUMNS = { "layer", "component" };

    @Test
    void findsMatchingStrip() {
        FMTAdc adc = FMTAdc.forTesting(() -> bank(COLUMNS));

        assertEquals(1, adc.count());
        assertEquals(3, adc.layer(0));
        assertEquals(271, adc.component(0));
        assertTrue(adc.hasHit(3, 271));
        assertFalse(adc.hasHit(3, 272));
    }

    @Test
    void rejectsMissingBankOrRequiredColumns() {
        assertEquals(0, FMTAdc.forTesting(() -> null).count());
        assertEquals(0, FMTAdc.forTesting(() -> bank(new String[] { "layer" })).count());
    }

    private static DataBank bank(String[] columns) {
        return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "rows" -> 1;
                    case "getColumnList" -> columns;
                    case "getByte" -> (byte) 3;
                    case "getShort" -> (short) 271;
                    default -> null;
                });
    }
}
