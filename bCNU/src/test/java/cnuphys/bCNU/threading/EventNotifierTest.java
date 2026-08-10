package cnuphys.bCNU.threading;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class EventNotifierTest {

    @Test
    void notifiesRegisteredListenersSynchronouslyAndSupportsRemoval() {
        EventNotifier<String> notifier = new EventNotifier<>();
        List<String> received = new ArrayList<>();
        IEventListener<String> first = value -> received.add("first:" + value);
        IEventListener<String> second = value -> received.add("second:" + value);

        notifier.addListener(first);
        notifier.addListener(second);
        notifier.addListener(first);
        notifier.notifyListeners("one");

        assertEquals(List.of("first:one", "second:one"), received);

        notifier.removeListener(first);
        notifier.notifyListeners("two");

        assertEquals(List.of("first:one", "second:one", "second:two"), received);
    }

    @Test
    void safeNotificationReportsFailureAndContinues() {
        EventNotifier<String> notifier = new EventNotifier<>();
        List<String> received = new ArrayList<>();
        RuntimeException failure = new RuntimeException("broken view");
        IEventListener<String> broken = value -> {
            throw failure;
        };
        notifier.addListener(broken);
        notifier.addListener(received::add);

        List<IEventListener<String>> failedListeners = new ArrayList<>();
        List<RuntimeException> failures = new ArrayList<>();
        notifier.notifyListenersSafely("event", (listener, exception) -> {
            failedListeners.add(listener);
            failures.add(exception);
        });

        assertEquals(List.of("event"), received);
        assertEquals(List.of(broken), failedListeners);
        assertSame(failure, failures.get(0));
    }
}
