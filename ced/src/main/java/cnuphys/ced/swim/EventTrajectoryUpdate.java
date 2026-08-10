package cnuphys.ced.swim;

import cnuphys.swim.Swimming;

/** Batches trajectory changes so renderers only observe a completed event update. */
public final class EventTrajectoryUpdate implements AutoCloseable {

    private final boolean notifyWhenComplete;
    private boolean closed;

    private EventTrajectoryUpdate(boolean notifyWhenComplete) {
        this.notifyWhenComplete = notifyWhenComplete;
        Swimming.setNotifyOn(false);
        clearCollections();
    }

    public static EventTrajectoryUpdate begin(boolean notifyWhenComplete) {
        return new EventTrajectoryUpdate(notifyWhenComplete);
    }

    public static void clearWithoutNotification() {
        Swimming.setNotifyOn(false);
        try {
            clearCollections();
        } finally {
            Swimming.setNotifyOn(true);
        }
    }

    private static void clearCollections() {
        // Do not call clearAllTrajectories(): it re-enables notifications internally.
        Swimming.clearMCTrajectories();
        Swimming.clearReconTrajectories();
        Swimming.clearAuxTrajectories();
    }

    @Override
    public void close() {
        if (closed) return;
        closed = true;
        Swimming.setNotifyOn(true);
        if (notifyWhenComplete) Swimming.notifyListeners();
    }
}
