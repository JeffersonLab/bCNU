package cnuphys.ced.alldata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.lang.reflect.Proxy;

import org.jlab.io.base.DataBank;
import org.junit.jupiter.api.Test;

class MCParticlesTest {

    private static final String[] COLUMNS = { "vx", "vy", "vz", "px", "py", "pz", "pid" };

    @Test
    void readsParticleRows() {
        MCParticles particles = MCParticles.forTesting(MCParticles.Kind.PARTICLE, () -> bank(COLUMNS));

        assertEquals("MC::Particle", particles.bankName());
        assertEquals(1, particles.count());
        assertEquals(11, particles.pid(0));
        assertEquals(1.0f, particles.vx(0));
        assertEquals(6.0f, particles.pz(0));
        assertFalse(particles.hasRow(1));
    }

    @Test
    void rejectsMissingBankOrRequiredColumns() {
        assertEquals(0, MCParticles.forTesting(MCParticles.Kind.LUND, () -> null).count());
        assertEquals(0, MCParticles.forTesting(MCParticles.Kind.LUND,
                () -> bank(new String[] { "vx", "pid" })).count());
    }

    private static DataBank bank(String[] columns) {
        return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "rows" -> 1;
                    case "getColumnList" -> columns;
                    case "getInt" -> 11;
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
