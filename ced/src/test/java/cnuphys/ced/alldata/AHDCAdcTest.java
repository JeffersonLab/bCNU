package cnuphys.ced.alldata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.jlab.io.base.DataBank;
import org.junit.jupiter.api.Test;

class AHDCAdcTest {

    @Test
    void readsGeometryAndPreservesOptionalFeedback() {
        AHDCAdc adc = AHDCAdc.forTesting(AHDCAdcTest::bank);

        assertEquals(1, adc.count());
        assertTrue(adc.hasRow(0));
        assertFalse(adc.hasRow(1));
        assertEquals(1, adc.sector(0));
        assertEquals(3, adc.layer(0));
        assertEquals(17, adc.component(0));
        assertEquals(912, adc.adc(0));

        List<String> feedback = new ArrayList<>();
        adc.addFeedback(0, feedback);
        assertEquals(6, feedback.size());
        assertEquals("$orange$ADC: 912", feedback.get(0));
        assertEquals("$orange$timeOverThreshold:   12.50000", feedback.get(5));
    }

    private static DataBank bank() {
        String[] columns = { "sector", "layer", "component", "order", "ADC", "integral", "ped", "time",
                "timeOverThreshold" };
        return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "rows" -> 1;
                    case "getColumnList" -> columns;
                    case "getByte" -> "order".equals(args[0]) ? (byte) 0
                            : "sector".equals(args[0]) ? (byte) 1 : (byte) 3;
                    case "getShort" -> "component".equals(args[0]) ? (short) 17 : (short) 4;
                    case "getInt" -> "ADC".equals(args[0]) ? 912 : 900;
                    case "getFloat" -> "time".equals(args[0]) ? 42.25f : 12.5f;
                    default -> null;
                });
    }
}
