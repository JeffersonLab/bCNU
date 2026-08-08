package cnuphys.ced.alldata;

import java.util.Objects;

import org.jlab.io.base.DataEvent;

import cnuphys.ced.alldata.datacontainer.IDataContainer;

/** Internal notification protocol for legacy data containers. */
sealed interface DataContainerNotification {

    void dispatchTo(IDataContainer container);

    record Update(DataEvent event) implements DataContainerNotification {
        public Update {
            Objects.requireNonNull(event, "event");
        }

        @Override
        public void dispatchTo(IDataContainer container) {
            container.update(event);
        }
    }

    enum Clear implements DataContainerNotification {
        INSTANCE;

        @Override
        public void dispatchTo(IDataContainer container) {
            container.clear();
        }
    }
}
