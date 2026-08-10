package cnuphys.bCNU.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.file.Path;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SerialIOTest {

	@TempDir
	Path tempDirectory;

	@Test
	void roundTripsSerializableObjectsThroughByteArrays() {
		Properties properties = sampleProperties();

		byte[] bytes = SerialIO.serialWrite(properties);

		assertEquals(properties, SerialIO.serialRead(bytes));
	}

	@Test
	void roundTripsSerializableObjectsThroughFiles() {
		Properties properties = sampleProperties();
		Path file = tempDirectory.resolve("preferences.bin");

		SerialIO.serialWrite(properties, file.toString());

		assertEquals(properties, SerialIO.serialRead(file.toString()));
	}

	@Test
	void returnsNullForInvalidSerializedData() {
		assertNull(SerialIO.serialRead(new byte[] { 1, 2, 3 }));
		assertNull(SerialIO.serialRead(tempDirectory.resolve("missing.bin").toString()));
	}

	@Test
	void preservesPrimitiveArrayContents() {
		int[] values = { 3, 1, 4, 1, 5 };

		assertArrayEquals(values, (int[]) SerialIO.serialRead(SerialIO.serialWrite(values)));
	}

	private static Properties sampleProperties() {
		Properties properties = new Properties();
		properties.setProperty("view", "central");
		properties.setProperty("visible", "true");
		return properties;
	}
}
