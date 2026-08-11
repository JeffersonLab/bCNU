package cnuphys.ced.clasio;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.Proxy;

import org.jlab.io.base.DataEvent;
import org.junit.jupiter.api.Test;

class ClasIoPresentBankPanelTest {

	@Test
	void sortsDefensiveCopyOfEventBankNames() {
		String[] eventBanks = { "REC::Particle", "BMT::adc", "RUN::config" };
		DataEvent event = event(eventBanks);

		assertArrayEquals(new String[] { "BMT::adc", "REC::Particle", "RUN::config" },
				ClasIoPresentBankPanel.sortedBankNames(event));
		assertArrayEquals(new String[] { "REC::Particle", "BMT::adc", "RUN::config" }, eventBanks);
	}

	@Test
	void handlesMissingEventOrBankList() {
		assertNull(ClasIoPresentBankPanel.sortedBankNames(null));
		assertNull(ClasIoPresentBankPanel.sortedBankNames(event(null)));
	}

	private static DataEvent event(String[] banks) {
		return (DataEvent) Proxy.newProxyInstance(DataEvent.class.getClassLoader(),
				new Class<?>[] { DataEvent.class }, (proxy, method, args) ->
						"getBankList".equals(method.getName()) ? banks : null);
	}
}
