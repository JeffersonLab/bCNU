package cnuphys.ced.clasio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ClasIoEventMenuTest {

	@TempDir
	File temporaryDirectory;

	@Test
	void acceptsOnlyExistingReadableRegularFiles() throws IOException {
		File eventFile = new File(temporaryDirectory, "event.hipo");
		Files.write(eventFile.toPath(), new byte[] { 1, 2, 3 });

		assertTrue(ClasIoEventMenu.canOpenEventFile(eventFile));
		assertFalse(ClasIoEventMenu.canOpenEventFile(new File(temporaryDirectory, "missing.hipo")));
		assertFalse(ClasIoEventMenu.canOpenEventFile(temporaryDirectory));
		assertFalse(ClasIoEventMenu.canOpenEventFile(null));
	}

	@Test
	void normalizesAutomaticEventPeriod() {
		assertEquals(2.5f, ClasIoEventMenu.normalizedEventPeriod("2.5", 2f));
		assertEquals(0.001f, ClasIoEventMenu.normalizedEventPeriod("0", 2f));
		assertEquals(60f, ClasIoEventMenu.normalizedEventPeriod("100", 2f));
		assertEquals(2f, ClasIoEventMenu.normalizedEventPeriod("invalid", 2f));
		assertEquals(2f, ClasIoEventMenu.normalizedEventPeriod("NaN", 2f));
		assertEquals(2f, ClasIoEventMenu.normalizedEventPeriod("Infinity", 2f));
	}
}
