package cnuphys.ced.alldata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Proxy;

import org.jlab.io.base.DataBank;
import org.junit.jupiter.api.Test;

class FMTTracksTest {

	@Test
	void readsTrackFitAndFindsStatusByDcIndex() {
		FMTTracks tracks = FMTTracks.forTesting(FMTTracksTest::bank);

		assertEquals(1, tracks.count());
		assertEquals(12, tracks.index(0));
		assertEquals(0, tracks.status(0));
		assertEquals(3, tracks.sector(0));
		assertEquals(1.25f, tracks.vertexX(0));
		assertEquals(-2.5f, tracks.vertexY(0));
		assertEquals(3.75f, tracks.vertexZ(0));
		assertEquals(0.4f, tracks.momentumX(0));
		assertEquals(0.5f, tracks.momentumY(0));
		assertEquals(2.0f, tracks.momentumZ(0));
		assertEquals(-1, tracks.charge(0));
		assertEquals(4.5f, tracks.chi2(0));
		assertEquals(7, tracks.ndf(0));
		assertEquals(0, tracks.statusForIndex((short) 12));
		assertEquals(-1, tracks.statusForIndex((short) 99));
	}

	@Test
	void rejectsMissingOrIncompleteBank() {
		assertEquals(0, FMTTracks.forTesting(() -> null).count());
		assertEquals(0, FMTTracks.forTesting(FMTTracksTest::incompleteBank).count());
	}

	private static DataBank bank() {
		String[] columns = { "index", "status", "sector", "Vtx0_x", "Vtx0_y", "Vtx0_z", "p0_x", "p0_y",
				"p0_z", "q", "chi2", "NDF" };
		return proxy(columns);
	}

	private static DataBank incompleteBank() {
		return proxy(new String[] { "index", "status" });
	}

	private static DataBank proxy(String[] columns) {
		return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
				(proxy, method, args) -> switch (method.getName()) {
					case "rows" -> 1;
					case "getColumnList" -> columns;
					case "getShort" -> (short) 12;
					case "getByte" -> switch ((String) args[0]) {
						case "status" -> (byte) 0;
						case "sector" -> (byte) 3;
						case "q" -> (byte) -1;
						case "NDF" -> (byte) 7;
						default -> (byte) 0;
					};
					case "getFloat" -> switch ((String) args[0]) {
						case "Vtx0_x" -> 1.25f;
						case "Vtx0_y" -> -2.5f;
						case "Vtx0_z" -> 3.75f;
						case "p0_x" -> 0.4f;
						case "p0_y" -> 0.5f;
						case "p0_z" -> 2.0f;
						case "chi2" -> 4.5f;
						default -> Float.NaN;
					};
					default -> null;
				});
	}
}
