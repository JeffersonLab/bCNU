package cnuphys.ced.alldata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.jlab.io.base.DataBank;
import org.junit.jupiter.api.Test;

class CNDAdcTest {

    @Test
    void readsCurrentBankAndPreservesFeedback() {
        CNDAdc adc = CNDAdc.forTesting(CNDAdcTest::bank);

        assertEquals(1, adc.count());
        assertEquals(10, adc.sector(0));
        assertEquals(2, adc.layer(0));
        assertEquals(5248, adc.adc(0));

        List<String> feedback = new ArrayList<>();
        adc.addFeedback(0, feedback);
        assertEquals("$cyan$CND adc 5248 time 94.750 order 0", feedback.get(0));
    }

    private static DataBank bank() {
        return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "rows" -> 1;
                    case "getByte" -> switch ((String) args[0]) {
                        case "sector" -> (byte) 10;
                        case "layer" -> (byte) 2;
                        case "order" -> (byte) 0;
                        default -> (byte) 0;
                    };
                    case "getShort" -> (short) 1;
                    case "getInt" -> 5248;
                    case "getFloat" -> 94.750f;
                    default -> null;
                });
    }
}
