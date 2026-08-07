# JOGL lifecycle probe

This probe creates a JOGL `GLJPanel`, displays it briefly, and disposes its
`JFrame` on Swing's event-dispatch thread. It isolates the native-window
teardown path involved in CED's Java 21 crash on macOS.

The source deliberately has no project dependency so it can be compiled and
run against either CED's bundled JOGL distribution or Maven-resolved JOGL
artifacts.
