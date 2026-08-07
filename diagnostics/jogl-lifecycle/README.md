# JOGL lifecycle probe

This probe creates a JOGL `GLJPanel`, displays it briefly, and disposes its
`JFrame` on Swing's event-dispatch thread. It isolates the native-window
teardown path involved in CED's Java 21 crash on macOS.

The source deliberately has no project dependency. It was used to demonstrate
the Java 21 crash with the former bundled JOGL 2.4.0 distribution and the fix
with Maven-resolved JOGL 2.6.0 artifacts.
