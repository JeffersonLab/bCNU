package cnuphys.ced.clasio;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jlab.io.base.DataSource;
import org.jlab.io.base.DataEvent;
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

    @Test
    void treatsInconsistentReaderBoundaryAsExhausted() throws Exception {
        ClasIoEventManager manager = ClasIoEventManager.getInstance();
        DataSource source = (DataSource) Proxy.newProxyInstance(DataSource.class.getClassLoader(),
                new Class<?>[] { DataSource.class }, (proxy, method, args) -> {
                    if ("hasEvent".equals(method.getName())) return true;
                    if ("getNextEvent".equals(method.getName())) {
                        throw new IndexOutOfBoundsException("record index past trailer");
                    }
                    return switch (method.getReturnType().getName()) {
                        case "boolean" -> false;
                        case "int" -> 0;
                        default -> null;
                    };
                });

        Field dataSource = field("_dataSource");
        Field sourceType = field("_sourceType");
        Field exhausted = field("_sourceExhausted");
        Object oldDataSource = dataSource.get(manager);
        Object oldSourceType = sourceType.get(manager);
        boolean oldExhausted = exhausted.getBoolean(manager);
        try {
            dataSource.set(manager, source);
            sourceType.set(manager, ClasIoEventManager.EventSourceType.HIPOFILE);
            exhausted.setBoolean(manager, false);

            assertNull(manager.getNextEvent());
            assertFalse(manager.hasEvent());
            assertFalse(manager.isNextOK());
        } finally {
            dataSource.set(manager, oldDataSource);
            sourceType.set(manager, oldSourceType);
            exhausted.setBoolean(manager, oldExhausted);
        }
    }

    @Test
    void countsEverySuccessfullyConsumedSourceEvent() throws Exception {
        ClasIoEventManager manager = ClasIoEventManager.getInstance();
        DataEvent event = (DataEvent) Proxy.newProxyInstance(DataEvent.class.getClassLoader(),
                new Class<?>[] { DataEvent.class }, (proxy, method, args) -> null);
        DataSource source = (DataSource) Proxy.newProxyInstance(DataSource.class.getClassLoader(),
                new Class<?>[] { DataSource.class }, (proxy, method, args) -> {
                    if ("hasEvent".equals(method.getName())) return true;
                    if ("getNextEvent".equals(method.getName())) return event;
                    return switch (method.getReturnType().getName()) {
                        case "boolean" -> false;
                        case "int" -> 0;
                        default -> null;
                    };
                });

        Field dataSource = field("_dataSource");
        Field sourceType = field("_sourceType");
        Field exhausted = field("_sourceExhausted");
        Field eventIndex = field("_currentEventIndex");
        Object oldDataSource = dataSource.get(manager);
        Object oldSourceType = sourceType.get(manager);
        boolean oldExhausted = exhausted.getBoolean(manager);
        int oldEventIndex = eventIndex.getInt(manager);
        try {
            dataSource.set(manager, source);
            sourceType.set(manager, ClasIoEventManager.EventSourceType.HIPOFILE);
            exhausted.setBoolean(manager, false);
            eventIndex.setInt(manager, 8);

            assertSame(event, manager.readNextDecodedEvent());
            assertEquals(9, manager.getSequentialEventNumber());
        } finally {
            dataSource.set(manager, oldDataSource);
            sourceType.set(manager, oldSourceType);
            exhausted.setBoolean(manager, oldExhausted);
            eventIndex.setInt(manager, oldEventIndex);
        }
    }

    private static Field field(String name) throws NoSuchFieldException {
        Field field = ClasIoEventManager.class.getDeclaredField(name);
        field.setAccessible(true);
        return field;
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
