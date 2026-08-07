package cnuphys.ced.trigger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TriggerMatchTest {

    @Test
    void exactRequiresIdenticalWords() {
        assertTrue(TriggerMatch.EXACT.matches(0b1010, 0b1010));
        assertFalse(TriggerMatch.EXACT.matches(0b1010, 0b1110));
    }

    @Test
    void anyRequiresAtLeastOneSharedBit() {
        assertTrue(TriggerMatch.ANY.matches(0b1010, 0b0110));
        assertFalse(TriggerMatch.ANY.matches(0b1010, 0b0101));
        assertFalse(TriggerMatch.ANY.matches(0, 0xFFFFFFFF));
    }

    @Test
    void allRequiresEveryPatternBit() {
        assertTrue(TriggerMatch.ALL.matches(0b1010, 0b1110));
        assertFalse(TriggerMatch.ALL.matches(0b1010, 0b0010));
        assertTrue(TriggerMatch.ALL.matches(0, 0));
    }

    @Test
    void matchingIncludesTheSignBit() {
        int signBit = Integer.MIN_VALUE;

        assertTrue(TriggerMatch.EXACT.matches(signBit, signBit));
        assertTrue(TriggerMatch.ANY.matches(signBit, 0x80000001));
        assertTrue(TriggerMatch.ALL.matches(signBit | 1, 0x80000003));
    }
}
