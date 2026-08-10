package cnuphys.ced.swim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import cnuphys.lund.SwimTrajectoryListener;
import cnuphys.swim.Swimming;

class EventTrajectoryUpdateTest {

    private final AtomicInteger notifications = new AtomicInteger();
    private final SwimTrajectoryListener listener = notifications::incrementAndGet;

    @AfterEach
    void cleanUp() {
        Swimming.removeSwimTrajectoryListener(listener);
        EventTrajectoryUpdate.clearWithoutNotification();
    }

    @Test
    void publishesOneNotificationForACompletedDisplayEvent() {
        Swimming.addSwimTrajectoryListener(listener);

        try (EventTrajectoryUpdate ignored = EventTrajectoryUpdate.begin(true)) {
            Swimming.addMCTrajectory(null);
            Swimming.addReconTrajectory(null);
            assertEquals(0, notifications.get());
        }

        assertEquals(1, notifications.get());
        assertEquals(1, Swimming.getMCTrajectories().size());
        assertEquals(1, Swimming.getReconTrajectories().size());
    }

    @Test
    void accumulationUpdateClearsSilently() {
        Swimming.addMCTrajectory(null);
        Swimming.addSwimTrajectoryListener(listener);

        try (EventTrajectoryUpdate ignored = EventTrajectoryUpdate.begin(false)) {
            assertTrue(Swimming.getMCTrajectories().isEmpty());
        }

        assertEquals(0, notifications.get());
    }
}
