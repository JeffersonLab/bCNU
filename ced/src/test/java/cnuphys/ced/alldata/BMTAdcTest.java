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

class BMTAdcTest {

    @Test
    void readsCurrentBankAndPreservesFeedbackAndHitLocation() {
        BMTAdc adc = BMTAdc.forTesting(BMTAdcTest::bank);

        assertEquals(1, adc.count());
        assertEquals(2, adc.sector(0));
        assertEquals(5, adc.layer(0));
        assertEquals(660, adc.component(0));
        assertEquals(5248, adc.adc(0));

        List<String> feedback = new ArrayList<>();
        adc.addFeedback(0, feedback);
        assertEquals("$cyan$BMT adc 5248 time 94.750 order 0", feedback.get(0));

        assertFalse(adc.contains(0, new Point(100, 200)));
        adc.setLocation(0, new Point(100, 200));
        assertTrue(adc.contains(0, new Point(102, 198)));
    }

    private static DataBank bank() {
        return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "rows" -> 1;
                    case "getByte" -> switch ((String) args[0]) {
                        case "sector" -> (byte) 2;
                        case "layer" -> (byte) 5;
                        case "order" -> (byte) 0;
                        default -> (byte) 0;
                    };
                    case "getShort" -> (short) 660;
                    case "getInt" -> 5248;
                    case "getFloat" -> 94.750f;
                    default -> null;
                });
    }
}
