package cnuphys.ced.clasio;

import org.jlab.io.base.DataEvent;

import cnuphys.ced.clasio.ClasIoEventManager.EventSourceType;

/** A type-safe notification emitted by the CLAS IO event manager. */
public sealed interface ClasIoEventNotification {

    void dispatchTo(IClasIoEventListener listener);

    record NewEvent(DataEvent event) implements ClasIoEventNotification {
        @Override
        public void dispatchTo(IClasIoEventListener listener) {
            listener.newClasIoEvent(event);
        }
    }

    record OpenedFile(String path) implements ClasIoEventNotification {
        @Override
        public void dispatchTo(IClasIoEventListener listener) {
            listener.openedNewEventFile(path);
        }
    }

    record SourceChanged(EventSourceType source) implements ClasIoEventNotification {
        @Override
        public void dispatchTo(IClasIoEventListener listener) {
            listener.changedEventSource(source);
        }
    }
}
