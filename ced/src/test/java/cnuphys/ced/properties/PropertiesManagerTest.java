package cnuphys.ced.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import cnuphys.bCNU.util.SerialIO;

class PropertiesManagerTest {

	@TempDir
	Path tempDirectory;

	@Test
	void loadsSerializedProperties() {
		File file = tempDirectory.resolve("preferences.bin").toFile();
		Properties expected = new Properties();
		expected.setProperty("CONNECTCLUSTER", "true");
		SerialIO.serialWrite(expected, file.getPath());

		Properties actual = PropertiesManager.loadProperties(file);

		assertEquals(expected, actual);
	}

	@Test
	void missingPreferenceFileProducesEmptyProperties() {
		Properties properties = PropertiesManager.loadProperties(tempDirectory.resolve("missing.bin").toFile());

		assertNotNull(properties);
		assertTrue(properties.isEmpty());
	}

	@Test
	void wrongSerializedTypeProducesEmptyProperties() {
		File file = tempDirectory.resolve("wrong-type.bin").toFile();
		SerialIO.serialWrite("not properties", file.getPath());

		Properties properties = PropertiesManager.loadProperties(file);

		assertNotNull(properties);
		assertTrue(properties.isEmpty());
	}
}
