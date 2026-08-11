package cnuphys.ced.alldata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;

import org.jlab.io.base.DataBank;
import org.junit.jupiter.api.Test;

class FMTRecHitsTest {

	@Test
	void findsMatchingReconstructedStrip() {
		FMTRecHits hits = FMTRecHits.forTesting(() -> bank(new String[] { "layer", "strip" }));

		assertEquals(1, hits.count());
		assertEquals(4, hits.layer(0));
		assertEquals(517, hits.strip(0));
		assertTrue(hits.hasHit(4, 517));
		assertFalse(hits.hasHit(4, 518));
	}

	@Test
	void rejectsMissingBankOrRequiredColumns() {
		assertEquals(0, FMTRecHits.forTesting(() -> null).count());
		assertEquals(0, FMTRecHits.forTesting(() -> bank(new String[] { "layer" })).count());
	}

	private static DataBank bank(String[] columns) {
		return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
				(proxy, method, args) -> switch (method.getName()) {
					case "rows" -> 1;
					case "getColumnList" -> columns;
					case "getByte" -> (byte) 4;
					case "getShort" -> (short) 517;
					default -> null;
				});
	}
}
