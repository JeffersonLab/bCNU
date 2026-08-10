package cnuphys.ced.clasio;

import cnuphys.ced.alldata.RunConfig;

/** Immutable snapshot of the most recently valid {@code RUN::config} row. */
public final class RunData {

    public final int run;
    public final int event;
    public final long trigger;
    public final long timestamp;
    public final byte type;
    public final byte mode;
    public final float solenoid;
    public final float torus;

    private RunData(int run, int event, long trigger, long timestamp, byte type,
            byte mode, float solenoid, float torus) {
        this.run = run;
        this.event = event;
        this.trigger = trigger;
        this.timestamp = timestamp;
        this.type = type;
        this.mode = mode;
        this.solenoid = solenoid;
        this.torus = torus;
    }

    static RunData empty() {
        return new RunData(-1, -1, -1L, -1L, (byte) -1, (byte) -1,
                Float.NaN, Float.NaN);
    }

    static RunData from(RunConfig.Values values) {
        return values == null ? null : new RunData(values.run(), values.event(), values.trigger(),
                values.timestamp(), values.type(), values.mode(), values.solenoid(), values.torus());
    }

    @Override
    public String toString() {
        return "run: " + run
                + "\nevent: " + event
                + "\ntrigger: " + trigger
                + "\ntype: " + type
                + "\nmode: " + mode
                + "\nsolenoid: " + solenoid
                + "\ntorus: " + torus
                + "\ntimeStamp: " + timestamp;
    }
}
