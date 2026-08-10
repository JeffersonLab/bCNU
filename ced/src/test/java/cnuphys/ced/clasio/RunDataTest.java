package cnuphys.ced.clasio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import cnuphys.ced.alldata.RunConfig;

class RunDataTest {

    @Test
    void createsCompleteImmutableSnapshot() {
        RunConfig.Values values = new RunConfig.Values(19210, 5228740, 7L, 123L,
                (byte) 1, (byte) 2, -1.0f, 1.0f);

        RunData data = RunData.from(values);

        assertEquals(19210, data.run);
        assertEquals(5228740, data.event);
        assertEquals(7L, data.trigger);
        assertEquals(123L, data.timestamp);
        assertEquals(-1.0f, data.solenoid);
        assertEquals(1.0f, data.torus);
        assertNull(RunData.from(null));
    }

    @Test
    void emptySnapshotUsesUnavailableSentinels() {
        RunData data = RunData.empty();

        assertEquals(-1, data.run);
        assertEquals(-1, data.event);
        assertEquals(-1L, data.trigger);
        assertEquals(-1L, data.timestamp);
    }
}
