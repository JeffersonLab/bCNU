package cnuphys.ced.frame;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Clas12ResourceLocatorTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void explicitPropertyTakesPrecedence() throws IOException {
        Path propertyRoot = resourceRoot(temporaryDirectory.resolve("property"));
        resourceRoot(temporaryDirectory.resolve("coatjava"));

        File found = Clas12ResourceLocator.locate(propertyRoot.toString(), null, temporaryDirectory, null);
        assertEquals(propertyRoot.toFile().getCanonicalFile(), found);
    }

    @Test
    void searchesAncestorsOfWorkingAndCodeDirectories() throws IOException {
        Path resourceRoot = resourceRoot(temporaryDirectory.resolve("project").resolve("coatjava"));
        Path workingDirectory = Files.createDirectories(temporaryDirectory.resolve("project").resolve("ced"));

        File found = Clas12ResourceLocator.locate(null, null, workingDirectory, null);
        assertEquals(resourceRoot.toFile().getCanonicalFile(), found);
    }

    @Test
    void rejectsDirectoriesWithoutBankDefinitions() throws IOException {
        Path empty = Files.createDirectories(temporaryDirectory.resolve("empty"));
        assertThrows(IOException.class, () -> Clas12ResourceLocator.locate(empty.toString(), null, empty, null));
    }

    private static Path resourceRoot(Path root) throws IOException {
        Files.createDirectories(root.resolve(Path.of("etc", "bankdefs", "hipo4")));
        return root;
    }
}
