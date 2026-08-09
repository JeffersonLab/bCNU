package cnuphys.ced.alldata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Proxy;

import org.jlab.io.base.DataBank;
import org.junit.jupiter.api.Test;

class CNDTdcTest {

    @Test
    void readsCurrentBank() {
        CNDTdc tdc = CNDTdc.forTesting(CNDTdcTest::bank);

        assertEquals(1, tdc.count());
        assertEquals(10, tdc.sector(0));
        assertEquals(2, tdc.layer(0));
        assertEquals(2, tdc.order(0));
        assertEquals(10313, tdc.tdc(0));
    }

    private static DataBank bank() {
        return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "rows" -> 1;
                    case "getByte" -> switch ((String) args[0]) {
                        case "sector" -> (byte) 10;
                        case "layer", "order" -> (byte) 2;
                        default -> (byte) 0;
                    };
                    case "getShort" -> (short) 1;
                    case "getInt" -> 10313;
                    default -> null;
                });
    }
}
