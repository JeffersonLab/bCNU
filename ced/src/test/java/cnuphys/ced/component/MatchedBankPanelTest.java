package cnuphys.ced.component;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class MatchedBankPanelTest {

	@Test
	void parsesCommaSeparatedCaseSensitiveMatches() {
		assertArrayEquals(new String[] { "DC::tdc", "REC::Particle.pid" },
				MatchedBankPanel.parseMatches(" DC::tdc, REC::Particle.pid "));
	}

	@Test
	void emptyInputMeansKeepCurrentMatches() {
		assertNull(MatchedBankPanel.parseMatches(null));
		assertNull(MatchedBankPanel.parseMatches("  \t\n "));
	}
}
