package cnuphys.ced.clasio;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jlab.io.base.DataSource;
import org.junit.jupiter.api.Test;

class ClasIoEventManagerTest {

    @Test
    void replacesSourceOnlyAfterNewSourceOpens() {
        AtomicBoolean currentClosed = new AtomicBoolean();
        AtomicBoolean replacementOpened = new AtomicBoolean();
        DataSource current = source(currentClosed, null, null);
        DataSource replacement = source(null, replacementOpened, null);

        assertSame(replacement,
                ClasIoEventManager.openReplacement(current, replacement, "events.hipo"));
        assertTrue(replacementOpened.get());
        assertTrue(currentClosed.get());
    }

    @Test
    void retainsCurrentSourceWhenReplacementFails() {
        AtomicBoolean currentClosed = new AtomicBoolean();
        AtomicBoolean replacementClosed = new AtomicBoolean();
        RuntimeException failure = new RuntimeException("bad event file");
        DataSource current = source(currentClosed, null, null);
        DataSource replacement = source(replacementClosed, null, failure);

        assertSame(failure, assertThrows(RuntimeException.class,
                () -> ClasIoEventManager.openReplacement(current, replacement, "broken.hipo")));
        assertFalse(currentClosed.get());
        assertTrue(replacementClosed.get());
    }

    private static DataSource source(AtomicBoolean closed, AtomicBoolean opened,
            RuntimeException openFailure) {
        return (DataSource) Proxy.newProxyInstance(DataSource.class.getClassLoader(),
                new Class<?>[] { DataSource.class }, (proxy, method, args) -> {
                    if ("open".equals(method.getName())) {
                        if (openFailure != null) throw openFailure;
                        if (opened != null) opened.set(true);
                    } else if ("close".equals(method.getName()) && closed != null) {
                        closed.set(true);
                    }
                    return switch (method.getReturnType().getName()) {
                        case "boolean" -> false;
                        case "int" -> 0;
                        default -> null;
                    };
                });
    }
}
