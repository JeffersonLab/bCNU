package cnuphys.ced.alldata;

import java.util.function.Supplier;

import org.jlab.io.base.DataBank;

/** Read-through access to the current {@code RUN::trigger} bank. */
public final class RunTriggers {

    public static final String BANK_NAME = "RUN::trigger";
    private static final RunTriggers INSTANCE = new RunTriggers(
            () -> DataWarehouse.getInstance().getBank(BANK_NAME));

    private final Supplier<DataBank> bankSupplier;

    private RunTriggers(Supplier<DataBank> bankSupplier) { this.bankSupplier = bankSupplier; }

    static RunTriggers forTesting(Supplier<DataBank> bankSupplier) { return new RunTriggers(bankSupplier); }

    public static RunTriggers getInstance() { return INSTANCE; }

    public int count() {
        DataBank bank = bank();
        return bank == null ? 0 : bank.rows();
    }

    public boolean hasRow(int row) { return row >= 0 && row < count(); }

    public boolean hasTriggerRow(int row) {
        DataBank bank = bank();
        return bank != null && row >= 0 && row < bank.rows() && DataWarehouse.hasColumn(bank, "trigger");
    }

    public boolean hasCompleteRow(int row) {
        DataBank bank = bank();
        return hasTriggerRow(row) && DataWarehouse.hasColumn(bank, "id");
    }

    public int id(int row) { return bank().getInt("id", row); }
    public int trigger(int row) { return bank().getInt("trigger", row); }

    public int[] ids() { return column("id"); }
    public int[] triggers() { return column("trigger"); }

    private int[] column(String name) {
        DataBank bank = bank();
        if (bank == null || !DataWarehouse.hasColumn(bank, name)) return null;
        int[] values = new int[bank.rows()];
        for (int row = 0; row < values.length; row++) values[row] = bank.getInt(name, row);
        return values;
    }

    private DataBank bank() { return bankSupplier.get(); }
}
