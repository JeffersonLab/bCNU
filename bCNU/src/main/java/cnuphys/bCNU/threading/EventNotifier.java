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

	public void notifyListeners(T data) {
		for (IEventListener<T> listener : listeners) {
			listener.newEvent(data);
		}
	}

	/**
	 * @deprecated notifications are synchronous; use {@link #notifyListeners(Object)}
	 */
	@Deprecated(forRemoval = false)
	public void nonThreadedTriggerEvent(T data) {
		notifyListeners(data);
	}
}
