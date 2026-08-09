package cnuphys.ced.alldata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.jlab.io.base.DataBank;
import org.junit.jupiter.api.Test;

class LTCCRecHitsTest {

    @Test
    void readsCurrentBankAndUsesCorrectDetectorLabel() {
        LTCCRecHits hits = LTCCRecHits.forTesting(LTCCRecHitsTest::bank);
        assertEquals(1, hits.count());

        List<String> feedback = new ArrayList<>();
        hits.addFeedback(0, feedback);
        assertEquals("$Orange Red$LTCC id 9 hit loc (-8.00,  4.50, 650.25) cm", feedback.get(0));
    }

    private static DataBank bank() {
        return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "rows" -> 1;
                    case "getShort" -> (short) 9;
                    case "getFloat" -> switch ((String) args[0]) {
                        case "x" -> -8.0f;
                        case "y" -> 4.5f;
                        case "z" -> 650.25f;
                        default -> 0.0f;
                    };
                    default -> null;
                });
    }
}
