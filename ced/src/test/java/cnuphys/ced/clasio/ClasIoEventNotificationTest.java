package cnuphys.ced.clasio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.jlab.io.base.DataEvent;
import org.junit.jupiter.api.Test;

import cnuphys.ced.clasio.ClasIoEventManager.EventSourceType;

class ClasIoEventNotificationTest {

    @Test
    void dispatchesEachNotificationToItsTypedCallback() {
        RecordingListener listener = new RecordingListener();

        new ClasIoEventNotification.NewEvent(null).dispatchTo(listener);
        assertEquals("event", listener.callback);

        new ClasIoEventNotification.OpenedFile("events.hipo").dispatchTo(listener);
        assertEquals("events.hipo", listener.callback);

        new ClasIoEventNotification.SourceChanged(EventSourceType.ET).dispatchTo(listener);
        assertSame(EventSourceType.ET, listener.source);
    }

    private static class RecordingListener implements IClasIoEventListener {
        private String callback;
        private EventSourceType source;

        @Override
        public void newClasIoEvent(DataEvent event) {
            callback = "event";
        }

        @Override
        public void openedNewEventFile(String path) {
            callback = path;
        }

        @Override
        public void changedEventSource(EventSourceType eventSource) {
            source = eventSource;
        }
    }
}
