package cnuphys.ced.clasio;

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
}
