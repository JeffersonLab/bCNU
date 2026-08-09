package cnuphys.ced.alldata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;

import org.jlab.io.base.DataBank;
import org.junit.jupiter.api.Test;

class ECalAdcTest {

    @Test
    void mapsBothDetectorSectionsAndNormalizesThemSeparately() {
        ECalAdc data = ECalAdc.forTesting(ECalAdcTest::bank);

        assertEquals(3, data.count());
        assertTrue(data.isPCal(0));
        assertEquals(0, data.view(0));
        assertEquals(1200, data.maxAdc(0));
        assertTrue(data.isECal(2));
        assertEquals(1, data.plane(2));
        assertEquals(0, data.view(2));
        assertEquals(300, data.maxAdc(2));
    }

    private static DataBank bank() {
        return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "rows" -> 3;
                    case "getByte" -> switch ((String) args[0]) {
                        case "sector" -> (byte) 6;
                        case "layer" -> (byte) new int[] { 1, 2, 7 }[(int) args[1]];
                        default -> (byte) 0;
                    };
                    case "getShort" -> (short) (10 + (int) args[1]);
                    case "getInt" -> new int[] { 1200, 600, 300 }[(int) args[1]];
                    case "getFloat" -> 100.0f + (int) args[1];
                    default -> null;
                });
    }
}
