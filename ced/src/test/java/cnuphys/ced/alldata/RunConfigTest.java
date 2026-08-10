package cnuphys.ced.alldata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;

import org.jlab.io.base.DataBank;
import org.junit.jupiter.api.Test;

class RunConfigTest {

    private static final String[] COLUMNS = {
            "run", "event", "trigger", "timestamp", "type", "mode", "solenoid", "torus"
    };

    @Test
    void readsCompleteConfiguration() {
        RunConfig config = RunConfig.forTesting(() -> bank(COLUMNS));

        assertTrue(config.hasUsableRow());
        assertEquals(19210, config.run());
        assertEquals(5228740, config.event());
        assertEquals(0x40000000L, config.trigger());
        assertEquals(123456789L, config.timestamp());
        assertEquals(1, config.type());
        assertEquals(2, config.mode());
        assertEquals(-1.0f, config.solenoid());
        assertEquals(1.0f, config.torus());
    }

    @Test
    void rejectsMissingBankRowsAndColumns() {
        assertFalse(RunConfig.forTesting(() -> null).hasUsableRow());
        assertFalse(RunConfig.forTesting(() -> bank(new String[] { "run" })).hasUsableRow());
    }

    private static DataBank bank(String[] columns) {
        return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "rows" -> 1;
                    case "getColumnList" -> columns;
                    case "getInt" -> "run".equals(args[0]) ? 19210 : 5228740;
                    case "getLong" -> "trigger".equals(args[0]) ? 0x40000000L : 123456789L;
                    case "getByte" -> "type".equals(args[0]) ? (byte) 1 : (byte) 2;
                    case "getFloat" -> "solenoid".equals(args[0]) ? -1.0f : 1.0f;
                    default -> null;
                });
    }
}
