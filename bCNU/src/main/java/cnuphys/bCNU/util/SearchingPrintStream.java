package cnuphys.bCNU.util;

import java.io.*;

public class SearchingPrintStream extends PrintStream {
    private final String keyword;

    public SearchingPrintStream(OutputStream out, String keyword) {
        super(out, true /* autoFlush */);
        this.keyword = keyword;
    }

    @Override
    public void println(String x) {
        // First, mirror the original behavior
        super.println(x);

        // Then, if our keyword appeared, print a stack trace
        if (x != null && x.contains(keyword)) {
            // You can use Exception, Error, or even just new Throwable()
            new Exception("Detected keyword “" + keyword + "” in stderr").printStackTrace(this);
            System.exit(1);
        }
    }
    
    public void print(String x) {
        // First, mirror the original behavior
        super.print(x);

        // Then, if our keyword appeared, print a stack trace
        if (x != null && x.contains(keyword)) {
            // You can use Exception, Error, or even just new Throwable()
            new Exception("Detected keyword “" + keyword + "” in stderr").printStackTrace(this);
            System.exit(1);
        }
    }

    
    

    // You may also want to override other print/printf methods if needed,
    // but most code uses println().
    
    public static void main(String[] args) {
        // Replace System.err with our watcher
        System.setErr(new SearchingPrintStream(System.err, "SEARCH_STRING"));

        // Example:
        System.err.println("This is just a warning.");
        System.err.println("Oops: found SEARCH_STRING in here!");
    }

}
