package cnuphys.ced.alldata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;

import org.jlab.io.base.DataBank;
import org.junit.jupiter.api.Test;

class RecCalorimeterTest {

    @Test
    void mapsDetectorSectionsAndLooksUpAssociatedParticleSafely() {
        RecCalorimeter data = RecCalorimeter.forTesting(RecCalorimeterTest::calorimeter,
                RecCalorimeterTest::particles);

        assertEquals(2, data.count());
        assertTrue(data.isPCal(0));
        assertEquals(0, data.view(0));
        assertTrue(data.isECal(1));
        assertEquals(1, data.plane(1));
        assertEquals(0, data.view(1));
        assertEquals(11, data.pid(0));
        assertEquals("REC PID e⁻", data.pidString(0));
        assertTrue(data.radius(0) > 0);
    }

    private static DataBank calorimeter() {
        return bank(2, (method, column, row) -> switch (method) {
            case "getByte" -> "sector".equals(column) ? (byte) 6 : (byte) (row == 0 ? 1 : 7);
            case "getShort" -> (short) row;
            case "getFloat" -> switch (column) {
                case "energy" -> row == 0 ? 1.027f : 0.0713f;
                case "x" -> row == 0 ? 54.866f : 64.252f;
                case "y" -> row == 0 ? -112.964f : -123.057f;
                case "z" -> row == 0 ? 731.336f : 781.739f;
                default -> 100.0f;
            };
            default -> null;
        });
    }

    private static DataBank particles() {
        return bank(2, (method, column, row) -> "getInt".equals(method) ? (row == 0 ? 11 : 2212) : null);
    }

    private interface Value { Object get(String method, String column, int row); }

    private static DataBank bank(int rows, Value value) {
        return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
                (proxy, method, args) -> "rows".equals(method.getName()) ? rows
                        : value.get(method.getName(), (String) args[0], (int) args[1]));
    }
}
