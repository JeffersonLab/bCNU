package cnuphys.ced.clasio;

/** Ordered phases used when notifying CLAS IO event listeners. */
public enum ClasIoEventListenerPhase {
    DATA,
    DERIVED,
    VIEW;

    static ClasIoEventListenerPhase fromIndex(int index) {
        ClasIoEventListenerPhase[] phases = values();
        if (index < 0 || index >= phases.length) {
            throw new IllegalArgumentException("Invalid CLAS IO listener phase index: " + index);
        }
        return phases[index];
    }
}
