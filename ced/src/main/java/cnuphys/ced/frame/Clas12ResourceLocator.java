package cnuphys.ced.frame;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

/** Locates the coatjava resource root that contains the CLAS12 bank definitions. */
final class Clas12ResourceLocator {

    private static final Path BANK_DEFINITIONS = Path.of("etc", "bankdefs", "hipo4");

    private Clas12ResourceLocator() { }

    static File locate() throws IOException {
        Path codeLocation = null;
        try {
            codeLocation = Path.of(Ced.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException | NullPointerException ignored) {
            // Working-directory discovery can still succeed.
        }

        return locate(System.getProperty("CLAS12DIR"), System.getenv("CLAS12DIR"),
                Path.of(System.getProperty("user.dir", ".")), codeLocation);
    }

    static File locate(String property, String environment, Path workingDirectory, Path codeLocation)
            throws IOException {
        Set<Path> candidates = new LinkedHashSet<>();
        addExplicit(candidates, property);
        addExplicit(candidates, environment);
        addSearchCandidates(candidates, workingDirectory);
        addSearchCandidates(candidates, codeLocation);

        for (Path candidate : candidates) {
            Path normalized = candidate.toAbsolutePath().normalize();
            if (Files.isDirectory(normalized.resolve(BANK_DEFINITIONS))) {
                return normalized.toFile().getCanonicalFile();
            }
        }
        throw new IOException("Could not locate CLAS12 bank definitions under a coatjava resource directory.");
    }

    private static void addExplicit(Set<Path> candidates, String value) {
        if (value != null && !value.isBlank()) candidates.add(Path.of(value));
    }

    private static void addSearchCandidates(Set<Path> candidates, Path start) {
        if (start == null) return;
        Path current = Files.isDirectory(start) ? start : start.getParent();
        for (int depth = 0; current != null && depth < 10; depth++, current = current.getParent()) {
            candidates.add(current);
            candidates.add(current.resolve("coatjava"));
        }
    }
}
