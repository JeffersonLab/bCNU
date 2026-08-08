package cnuphys.ced.alldata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.jlab.io.base.DataBank;
import org.junit.jupiter.api.Test;

class CTOFAdcTest {

    @Test
    void readsCurrentBankAndPreservesFeedback() {
        CTOFAdc adc = CTOFAdc.forTesting(CTOFAdcTest::bank);

        assertEquals(2, adc.count());
        assertEquals(45, adc.component(0));
        assertEquals(1454, adc.adc(0));

        List<String> feedback = new ArrayList<>();
        adc.addFeedback(0, feedback);
        assertEquals("$cyan$CTOF adc 1454 time 107.125 order 1", feedback.get(0));
    }

    private static DataBank bank() {
        return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
                (proxy, method, args) -> {
                    int row = (args != null && args.length > 1) ? (int) args[1] : 0;
                    return switch (method.getName()) {
                        case "rows" -> 2;
                        case "getByte" -> switch ((String) args[0]) {
                            case "sector", "layer" -> (byte) 1;
                            case "order" -> (byte) (1 - row);
                            default -> (byte) 0;
                        };
                        case "getShort" -> (short) 45;
                        case "getInt" -> (row == 0) ? 1454 : 1422;
                        case "getFloat" -> (row == 0) ? 107.125f : 109.625f;
                        default -> null;
                    };
                });
    }
}
