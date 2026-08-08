package cnuphys.ced.alldata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.lang.reflect.Proxy;

import org.jlab.io.base.DataEvent;
import org.junit.jupiter.api.Test;

import cnuphys.ced.alldata.datacontainer.IDataContainer;

class DataListenerTest {

    @Test
    void dispatchesExplicitUpdateAndClearNotifications() {
        RecordingContainer container = new RecordingContainer();
        DataListener listener = new DataListener(container);
        DataEvent event = (DataEvent) Proxy.newProxyInstance(
                DataEvent.class.getClassLoader(), new Class<?>[] { DataEvent.class }, (proxy, method, args) -> null);

        listener.newEvent(new DataContainerNotification.Update(event));
        assertSame(event, container.event);

        listener.newEvent(DataContainerNotification.Clear.INSTANCE);
        assertEquals(1, container.clearCount);
    }

    private static class RecordingContainer implements IDataContainer {
        private DataEvent event;
        private int clearCount;

        @Override
        public void clear() {
            clearCount++;
        }

        @Override
        public void update(DataEvent newEvent) {
            event = newEvent;
        }

        @Override
        public int count() {
            return 0;
        }
    }
}
