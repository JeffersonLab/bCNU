package cnuphys.ced.alldata;

import java.util.function.Supplier;

import org.jlab.io.base.DataBank;

/** Read-through access to the first row of the current {@code RUN::config} bank. */
public final class RunConfig {

    public static final String BANK_NAME = "RUN::config";
    private static final String[] REQUIRED_COLUMNS = { "run", "event", "solenoid", "torus" };
    private static final RunConfig INSTANCE = new RunConfig(
            () -> DataWarehouse.getInstance().getBank(BANK_NAME));

    private final Supplier<DataBank> bankSupplier;

    private RunConfig(Supplier<DataBank> bankSupplier) { this.bankSupplier = bankSupplier; }

    static RunConfig forTesting(Supplier<DataBank> bankSupplier) { return new RunConfig(bankSupplier); }

    public static RunConfig getInstance() { return INSTANCE; }

    public boolean hasEvent() {
        DataBank bank = bank();
        return bank != null && bank.rows() > 0 && DataWarehouse.hasColumn(bank, "event");
    }

    public int eventOrMinusOne() { return hasEvent() ? event() : -1; }

    /** A complete, internally consistent first-row snapshot. */
    public record Values(int run, int event, long trigger, long timestamp, byte type,
            byte mode, float solenoid, float torus) {}

    public Values valuesOrNull() {
        DataBank bank = bank();
        if (bank == null || bank.rows() < 1) return null;
        for (String column : REQUIRED_COLUMNS) {
            if (!DataWarehouse.hasColumn(bank, column)) return null;
        }

        int run = bank.getInt("run", 0);
        int event = bank.getInt("event", 0);
        float solenoid = bank.getFloat("solenoid", 0);
        float torus = bank.getFloat("torus", 0);
        if (run < 0 || event < 0 || !Float.isFinite(solenoid) || !Float.isFinite(torus)) return null;

        return new Values(run, event, longValue(bank, "trigger"), longValue(bank, "timestamp"),
                byteValue(bank, "type"), byteValue(bank, "mode"), solenoid, torus);
    }

    public boolean hasUsableRow() {
        DataBank bank = bank();
        if (bank == null || bank.rows() < 1) return false;
        for (String column : REQUIRED_COLUMNS) {
            if (!DataWarehouse.hasColumn(bank, column)) return false;
        }
        return true;
    }

    public int run() { return bank().getInt("run", 0); }
    public int event() { return bank().getInt("event", 0); }
    public long trigger() { return longValue("trigger"); }
    public long timestamp() { return longValue("timestamp"); }
    public byte type() { return byteValue("type"); }
    public byte mode() { return byteValue("mode"); }
    public float solenoid() { return bank().getFloat("solenoid", 0); }
    public float torus() { return bank().getFloat("torus", 0); }

    private long longValue(String column) {
        DataBank bank = bank();
        return longValue(bank, column);
    }

    private byte byteValue(String column) {
        DataBank bank = bank();
        return byteValue(bank, column);
    }

    private static long longValue(DataBank bank, String column) {
        return DataWarehouse.hasColumn(bank, column) ? bank.getLong(column, 0) : -1L;
    }

    private static byte byteValue(DataBank bank, String column) {
        return DataWarehouse.hasColumn(bank, column) ? bank.getByte(column, 0) : (byte) -1;
    }

    private DataBank bank() { return bankSupplier.get(); }
}
