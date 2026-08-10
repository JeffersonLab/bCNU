package cnuphys.ced.alldata;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;

import org.jlab.io.base.DataBank;
import org.junit.jupiter.api.Test;

class RunTriggersTest {

    @Test
    void readsRowsAndColumns() {
        RunTriggers triggers = RunTriggers.forTesting(() -> bank(new String[] { "id", "trigger" }));

        assertEquals(2, triggers.count());
        assertTrue(triggers.hasTriggerRow(0));
        assertTrue(triggers.hasCompleteRow(0));
        assertFalse(triggers.hasRow(2));
        assertEquals(11, triggers.id(0));
        assertEquals(0x20, triggers.trigger(1));
        assertArrayEquals(new int[] { 11, 12 }, triggers.ids());
        assertArrayEquals(new int[] { 0x10, 0x20 }, triggers.triggers());
    }

    @Test
    void handlesMissingBankAndColumn() {
        RunTriggers missing = RunTriggers.forTesting(() -> null);
        assertEquals(0, missing.count());
        assertFalse(missing.hasTriggerRow(0));
        assertNull(missing.ids());

        RunTriggers noTrigger = RunTriggers.forTesting(() -> bank(new String[] { "id" }));
        assertFalse(noTrigger.hasTriggerRow(0));
        assertFalse(noTrigger.hasCompleteRow(0));
        assertNull(noTrigger.triggers());
    }

    private static DataBank bank(String[] columns) {
        return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "rows" -> 2;
                    case "getColumnList" -> columns;
                    case "getInt" -> "id".equals(args[0]) ? 11 + (int) args[1] : 0x10 << (int) args[1];
                    default -> null;
                });
    }
}
