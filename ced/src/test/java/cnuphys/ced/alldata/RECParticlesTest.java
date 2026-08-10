package cnuphys.ced.alldata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.lang.reflect.Proxy;

import org.jlab.io.base.DataBank;
import org.junit.jupiter.api.Test;

import cnuphys.lund.LundSupport;

class RECParticlesTest {

    private static final String[] COLUMNS = {
            "vx", "vy", "vz", "px", "py", "pz", "charge", "status", "pid"
    };

    @Test
    void readsParticleRows() {
        RECParticles particles = RECParticles.forTesting(() -> bank(COLUMNS));

        assertEquals(1, particles.count());
        assertEquals(11, particles.pid(0));
        assertEquals(-1, particles.charge(0));
        assertEquals(2200, particles.status(0));
        assertEquals(1.0f, particles.vx(0));
        assertEquals(6.0f, particles.pz(0));
        assertFalse(particles.hasRow(1));
    }

    @Test
    void rejectsMissingBankOrRequiredColumns() {
        assertEquals(0, RECParticles.forTesting(() -> null).count());
        assertEquals(0, RECParticles.forTesting(() -> bank(new String[] { "vx", "pid" })).count());
    }

    @Test
    void preservesUnknownParticleCharge() {
        RECParticles particles = RECParticles.forTesting(() -> bank(COLUMNS, 0, -1));
        assertSame(LundSupport.unknownMinus, particles.lundId(0));

        particles = RECParticles.forTesting(() -> bank(COLUMNS, 0, 1));
        assertSame(LundSupport.unknownPlus, particles.lundId(0));
    }

    private static DataBank bank(String[] columns) {
        return bank(columns, 11, -1);
    }

    private static DataBank bank(String[] columns, int pid, int charge) {
        return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "rows" -> 1;
                    case "getColumnList" -> columns;
                    case "getInt" -> pid;
                    case "getByte" -> (byte) charge;
                    case "getShort" -> (short) 2200;
                    case "getFloat" -> switch ((String) args[0]) {
                        case "vx" -> 1.0f;
                        case "vy" -> 2.0f;
                        case "vz" -> 3.0f;
                        case "px" -> 4.0f;
                        case "py" -> 5.0f;
                        default -> 6.0f;
                    };
                    default -> null;
                });
    }
}
