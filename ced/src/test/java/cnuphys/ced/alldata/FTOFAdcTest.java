package cnuphys.ced.alldata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.jlab.io.base.DataBank;
import org.junit.jupiter.api.Test;

class FTOFAdcTest {

    @Test
    void readsCurrentBankAndPreservesFeedback() {
        FTOFAdc adc = FTOFAdc.forTesting(FTOFAdcTest::bank);

        assertEquals(2, adc.count());
        assertEquals(2, adc.sector(0));
        assertEquals(1, adc.layer(0));
        assertEquals(15, adc.component(0));
        assertEquals(288, adc.adc(0));

        List<String> feedback = new ArrayList<>();
        adc.addFeedback(0, feedback);
        assertEquals("$cyan$FTOF adc 288 time 169.813 order 0", feedback.get(0));
    }

    private static DataBank bank() {
        return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
                (proxy, method, args) -> {
                    int row = (args != null && args.length > 1) ? (int) args[1] : 0;
                    return switch (method.getName()) {
                        case "rows" -> 2;
                        case "getByte" -> switch ((String) args[0]) {
                            case "sector" -> (byte) 2;
                            case "layer" -> (byte) 1;
                            case "order" -> (byte) row;
                            default -> (byte) 0;
                        };
                        case "getShort" -> (short) 15;
                        case "getInt" -> (row == 0) ? 288 : 209;
                        case "getFloat" -> (row == 0) ? 169.813f : 171.313f;
                        default -> null;
                    };
                });
    }
}
