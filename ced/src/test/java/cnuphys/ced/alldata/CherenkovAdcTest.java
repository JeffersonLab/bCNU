package cnuphys.ced.alldata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.jlab.io.base.DataBank;
import org.junit.jupiter.api.Test;

class CherenkovAdcTest {

    @Test
    void readsDetectorBankAndFormatsFeedback() {
        CherenkovAdc adc = CherenkovAdc.forTesting("LTCC", CherenkovAdcTest::bank);
        assertEquals(1, adc.count());
        assertEquals(3, adc.sector(0));
        assertEquals(12, adc.component(0));

        List<String> feedback = new ArrayList<>();
        adc.addFeedback(0, feedback);
        assertEquals("LTCC adc: 2468 time:  105.250", feedback.get(0));
    }

    private static DataBank bank() {
        return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "rows" -> 1;
                    case "getByte" -> switch ((String) args[0]) {
                        case "sector" -> (byte) 3;
                        case "layer" -> (byte) 2;
                        case "order" -> (byte) 0;
                        default -> (byte) 0;
                    };
                    case "getShort" -> (short) 12;
                    case "getInt" -> 2468;
                    case "getFloat" -> 105.25f;
                    default -> null;
                });
    }
}
