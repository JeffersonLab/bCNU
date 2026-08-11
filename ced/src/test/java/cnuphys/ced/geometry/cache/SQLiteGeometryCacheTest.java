package cnuphys.ced.geometry.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SQLiteGeometryCacheTest {

	@TempDir
	Path tempDirectory;

	@Test
	void roundTripsVersionedDetectorPayload() throws Exception {
		Path database = tempDirectory.resolve("geometry.sqlite");
		TestGeometry written = new TestGeometry(42);
		try (SQLiteGeometryCache cache = new SQLiteGeometryCache(database, "1.0", "default")) {
			cache.open();
			assertTrue(cache.write(written));
		}

		TestGeometry read = new TestGeometry(0);
		try (SQLiteGeometryCache cache = new SQLiteGeometryCache(database, "1.0", "default")) {
			cache.open();
			assertTrue(cache.read(read));
		}
		assertEquals(42, read.value);
	}

	@Test
	void applicationVersionChangeInvalidatesPayloads() throws Exception {
		Path database = tempDirectory.resolve("geometry.sqlite");
		try (SQLiteGeometryCache cache = new SQLiteGeometryCache(database, "1.0", "default")) {
			cache.open();
			assertTrue(cache.write(new TestGeometry(42)));
		}

		try (SQLiteGeometryCache cache = new SQLiteGeometryCache(database, "1.1", "default")) {
			cache.open();
			assertFalse(cache.read(new TestGeometry(0)));
		}
	}

	@Test
	void variationChangeInvalidatesPayloads() throws Exception {
		Path database = tempDirectory.resolve("geometry.sqlite");
		try (SQLiteGeometryCache cache = new SQLiteGeometryCache(database, "1.0", "default")) {
			cache.open();
			assertTrue(cache.write(new TestGeometry(42)));
		}

		try (SQLiteGeometryCache cache = new SQLiteGeometryCache(database, "1.0", "survey")) {
			cache.open();
			assertFalse(cache.read(new TestGeometry(0)));
		}
	}

	private static final class TestGeometry implements IGeometryCache {
		private int value;

		private TestGeometry(int value) {
			this.value = value;
		}

		@Override
		public String getName() {
			return "test";
		}

		@Override
		public void initializeUsingCCDB() {
		}

		@Override
		public boolean supportsCache() {
			return true;
		}

		@Override
		public void readGeometry(DataInput input) throws IOException {
			value = input.readInt();
		}

		@Override
		public void writeGeometry(DataOutput output) throws IOException {
			output.writeInt(value);
		}
	}
}
