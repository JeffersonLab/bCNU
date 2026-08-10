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
        return DataWarehouse.hasColumn(bank, column) ? bank.getLong(column, 0) : -1L;
    }

    private byte byteValue(String column) {
        DataBank bank = bank();
        return DataWarehouse.hasColumn(bank, column) ? bank.getByte(column, 0) : (byte) -1;
    }

    private DataBank bank() { return bankSupplier.get(); }
}
