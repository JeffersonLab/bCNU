package cnuphys.bCNU.threading;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class EventNotifier<T> {
    private final Set<IEventListener<T>> listeners = new CopyOnWriteArraySet<>();

    public void addListener(IEventListener<T> listener) {
        listeners.add(listener);
    }

    public void removeListener(IEventListener<T> listener) {
        listeners.remove(listener);
    }

	public void nonThreadedTriggerEvent(T data) {
		for (IEventListener<T> listener : listeners) {
			listener.newEvent(data);
		}
	}
}
