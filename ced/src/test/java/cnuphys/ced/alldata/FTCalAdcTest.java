package cnuphys.ced.alldata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.jlab.io.base.DataBank;
import org.junit.jupiter.api.Test;

class FTCalAdcTest {

    @Test
    void readsCurrentBankAndPreservesFeedback() {
        FTCalAdc adc = FTCalAdc.forTesting(FTCalAdcTest::bank);

        assertEquals(1, adc.count());
        assertEquals(359, adc.component(0));
        assertEquals(1553, adc.adc(0));

        List<String> feedback = new ArrayList<>();
        adc.addFeedback(0, feedback);
        assertEquals("$cyan$FTCAL adc 1553 time 142.500 order 0", feedback.get(0));
    }

    private static DataBank bank() {
        return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "rows" -> 1;
                    case "getByte" -> switch ((String) args[0]) {
                        case "sector", "layer" -> (byte) 1;
                        case "order" -> (byte) 0;
                        default -> (byte) 0;
                    };
                    case "getShort" -> (short) 359;
                    case "getInt" -> 1553;
                    case "getFloat" -> 142.500f;
                    default -> null;
                });
    }
}
