package cnuphys.ced.alldata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Proxy;

import org.jlab.io.base.DataBank;
import org.junit.jupiter.api.Test;

class FMTTrajectoriesTest {

	@Test
	void readsTrajectoryCoordinatesAndIdentity() {
		FMTTrajectories trajectories = FMTTrajectories.forTesting(FMTTrajectoriesTest::bank);

		assertEquals(1, trajectories.count());
		assertEquals(12, trajectories.trackIndex(0));
		assertEquals(5, trajectories.layer(0));
		assertEquals(1.25f, trajectories.x(0));
		assertEquals(-2.5f, trajectories.y(0));
		assertEquals(32.0f, trajectories.z(0));
		assertEquals(-1.5f, trajectories.dcLocalX(0));
		assertEquals(2.75f, trajectories.dcLocalY(0));
		assertEquals(0.0f, trajectories.dcLocalZ(0));
	}

	@Test
	void rejectsMissingBank() {
		assertEquals(0, FMTTrajectories.forTesting(() -> null).count());
	}

	private static DataBank bank() {
		String[] columns = { "index", "layer", "x", "y", "z", "dx", "dy", "dz" };
		return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
				(proxy, method, args) -> switch (method.getName()) {
					case "rows" -> 1;
					case "getColumnList" -> columns;
					case "getShort" -> (short) 12;
					case "getByte" -> (byte) 5;
					case "getFloat" -> switch ((String) args[0]) {
						case "x" -> 1.25f;
						case "y" -> -2.5f;
						case "z" -> 32.0f;
						case "dx" -> -1.5f;
						case "dy" -> 2.75f;
						case "dz" -> 0.0f;
						default -> Float.NaN;
					};
					default -> null;
				});
	}
}
