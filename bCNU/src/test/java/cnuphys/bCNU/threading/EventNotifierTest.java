package cnuphys.bCNU.threading;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        notifier.nonThreadedTriggerEvent("one");

        assertEquals(List.of("first:one", "second:one"), received);

        notifier.removeListener(first);
        notifier.nonThreadedTriggerEvent("two");

        assertEquals(List.of("first:one", "second:one", "second:two"), received);
    }
}
