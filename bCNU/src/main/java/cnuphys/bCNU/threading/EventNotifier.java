package cnuphys.bCNU.threading;

import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class EventNotifier<T> {
    private final Set<IEventListener<T>> listeners = new CopyOnWriteArraySet<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final CompletionService<Void> completionService;

    public EventNotifier() {
        this.completionService = new ExecutorCompletionService<>(executor);
    }

    public void addListener(IEventListener<T> listener) {
        listeners.add(listener);
    }

    public void removeListener(IEventListener<T> listener) {
        listeners.remove(listener);
    }

	public void nonThreadedTriggerEvent(T data) throws InterruptedException, ExecutionException {
		for (IEventListener<T> listener : listeners) {
			listener.newEvent(data);
		}
	}



    public void threadedTriggerEvent(T data) throws InterruptedException, ExecutionException {
        for (IEventListener<T> listener : listeners) {
            completionService.submit(() -> {
                listener.newEvent(data);
                return null; // Callable must return something, so we return null
            });
        }

        for (int i = 0; i < listeners.size(); i++) {
            Future<Void> future = completionService.take(); // Blocks until a task is completed
            future.get(); // Wait for the task to complete, throw exceptions if any
        }
    }

    public void shutdown() {
        executor.shutdown();
    }
}
