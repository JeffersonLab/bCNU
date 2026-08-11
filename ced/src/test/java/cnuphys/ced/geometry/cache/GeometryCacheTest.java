package cnuphys.ced.geometry.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GeometryCacheTest {

	@Test
	void normalizesDetectorNamesForConsoleOutput() {
		assertEquals("CTOF", GeometryCache.displayName("CTOFGeometry"));
		assertEquals("BST", GeometryCache.displayName("BST Geometry"));
		assertEquals("ALERT", GeometryCache.displayName("ALERT"));
		assertEquals("μrWT", GeometryCache.displayName("μrWT"));
	}
}
