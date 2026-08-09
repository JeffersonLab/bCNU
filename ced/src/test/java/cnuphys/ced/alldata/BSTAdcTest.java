package cnuphys.ced.alldata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.jlab.io.base.DataBank;
import org.junit.jupiter.api.Test;

class BSTAdcTest {

    @Test
    void readsCurrentBankAndPreservesFeedback() {
        BSTAdc adc = BSTAdc.forTesting(BSTAdcTest::bank);

        assertEquals(4, adc.count());
        assertEquals(10, adc.sector(0));
        assertEquals(6, adc.layer(0));
        assertEquals(80, adc.component(0));
        assertEquals(0, adc.adc(0));

        List<String> feedback = new ArrayList<>();
        adc.addFeedback(0, feedback);
        assertEquals("$cyan$BST strip 80 adc 0 time 137.000 order 0", feedback.get(0));
    }

    private static DataBank bank() {
        return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "rows" -> 4;
                    case "getByte" -> switch ((String) args[0]) {
                        case "sector" -> (byte) 10;
                        case "layer" -> (byte) 6;
                        case "order" -> (byte) 0;
                        default -> (byte) 0;
                    };
                    case "getShort" -> (short) 80;
                    case "getInt" -> 0;
                    case "getFloat" -> 137.0f;
                    default -> null;
                });
    }
}
