package cnuphys.bCNU.threading;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.BiConsumer;

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
	 * Notify every listener, reporting a listener failure without preventing later
	 * listeners from receiving the event.
	 *
	 * @param data event data
	 * @param failureHandler receives the failing listener and exception
	 */
	public void notifyListenersSafely(T data,
			BiConsumer<IEventListener<T>, RuntimeException> failureHandler) {
		for (IEventListener<T> listener : listeners) {
			try {
				listener.newEvent(data);
			} catch (RuntimeException exception) {
				failureHandler.accept(listener, exception);
			}
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
