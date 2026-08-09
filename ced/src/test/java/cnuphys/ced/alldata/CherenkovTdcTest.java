package cnuphys.ced.alldata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.jlab.io.base.DataBank;
import org.junit.jupiter.api.Test;

class CherenkovTdcTest {

    @Test
    void readsDetectorBankAndFormatsFeedback() {
        CherenkovTdc tdc = CherenkovTdc.forTesting("HTCC", CherenkovTdcTest::bank);
        assertEquals(1, tdc.count());
        assertEquals(7312, tdc.tdc(0));

        List<String> feedback = new ArrayList<>();
        tdc.addFeedback(0, feedback);
        assertEquals("HTCC tdc: 7312", feedback.get(0));
    }

    private static DataBank bank() {
        return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "rows" -> 1;
                    case "getByte" -> (byte) 1;
                    case "getShort" -> (short) 4;
                    case "getInt" -> 7312;
                    default -> null;
                });
    }
}
