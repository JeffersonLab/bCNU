package cnuphys.bCNU.util;

import java.io.PrintStream;

public class FilteredStackTraceException extends Exception {

    public FilteredStackTraceException(String message) {
        super(message);
    }

    @Override
    public void printStackTrace(PrintStream s) {
        s.println(this);
        for (StackTraceElement element : getStackTrace()) {
            String line = element.toString();
            // Skip lines that start with java.desktop/java
			if (!line.startsWith("java.desktop/java") && !line.startsWith("java.base/java")) {
                s.println("\tat " + line);
            }
        }
        Throwable cause = getCause();
        if (cause != null) {
            s.print("Caused by: ");
            cause.printStackTrace(s);
        }
    }
}
