package cnuphys.ced.alldata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;

import org.jlab.io.base.DataBank;
import org.junit.jupiter.api.Test;

class FMTClustersTest {

	@Test
	void findsMatchingClusterSeedStrip() {
		FMTClusters clusters = FMTClusters.forTesting(() -> bank(new String[] { "layer", "seedStrip" }));

		assertEquals(1, clusters.count());
		assertEquals(2, clusters.layer(0));
		assertEquals(384, clusters.seedStrip(0));
		assertTrue(clusters.hasSeedStrip(2, 384));
		assertFalse(clusters.hasSeedStrip(2, 385));
	}

	@Test
	void rejectsMissingBankOrRequiredColumns() {
		assertEquals(0, FMTClusters.forTesting(() -> null).count());
		assertEquals(0, FMTClusters.forTesting(() -> bank(new String[] { "layer" })).count());
	}

	private static DataBank bank(String[] columns) {
		return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
				(proxy, method, args) -> switch (method.getName()) {
					case "rows" -> 1;
					case "getColumnList" -> columns;
					case "getByte" -> (byte) 2;
					case "getShort" -> (short) 384;
					default -> null;
				});
	}
}
