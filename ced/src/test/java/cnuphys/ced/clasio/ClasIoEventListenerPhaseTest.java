package cnuphys.ced.clasio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ClasIoEventListenerPhaseTest {

    @Test
    void mapsLegacyIndexesToNamedPhases() {
        assertEquals(ClasIoEventListenerPhase.DATA, ClasIoEventListenerPhase.fromIndex(0));
        assertEquals(ClasIoEventListenerPhase.DERIVED, ClasIoEventListenerPhase.fromIndex(1));
        assertEquals(ClasIoEventListenerPhase.VIEW, ClasIoEventListenerPhase.fromIndex(2));
        assertThrows(IllegalArgumentException.class, () -> ClasIoEventListenerPhase.fromIndex(-1));
        assertThrows(IllegalArgumentException.class, () -> ClasIoEventListenerPhase.fromIndex(3));
    }
}
