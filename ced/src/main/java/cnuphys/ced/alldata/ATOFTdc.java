package cnuphys.ced.alldata;

import java.util.List;
import java.util.function.Supplier;

import org.jlab.io.base.DataBank;

/** Read-through access to the current {@code ATOF::tdc} bank. */
public final class ATOFTdc {

    public static final String BANK_NAME = "ATOF::tdc";
    private static final ATOFTdc INSTANCE = new ATOFTdc(
            () -> DataWarehouse.getInstance().getBank(BANK_NAME));

    private final Supplier<DataBank> bankSupplier;

    private ATOFTdc(Supplier<DataBank> bankSupplier) { this.bankSupplier = bankSupplier; }

    static ATOFTdc forTesting(Supplier<DataBank> bankSupplier) { return new ATOFTdc(bankSupplier); }

    public static ATOFTdc getInstance() { return INSTANCE; }

    public int count() {
        DataBank bank = bank();
        return bank == null ? 0 : bank.rows();
    }

    public boolean hasRow(int row) { return row >= 0 && row < count(); }
    public byte sector(int row) { return bank().getByte("sector", row); }
    public byte layer(int row) { return bank().getByte("layer", row); }
    public short component(int row) { return bank().getShort("component", row); }
    public byte order(int row) { return bank().getByte("order", row); }

    public void addFeedback(int row, List<String> feedback) {
        DataBank bank = bank();
        if (bank == null || row < 0 || row >= bank.rows()) return;
        addInt(bank, "TDC", row, feedback);
        if (DataWarehouse.hasColumn(bank, "order")) {
            feedback.add(String.format("$orange$order: %d", bank.getByte("order", row)));
        }
        addInt(bank, "ToT", row, feedback);
    }

    private static void addInt(DataBank bank, String column, int row, List<String> feedback) {
        if (DataWarehouse.hasColumn(bank, column)) {
            feedback.add(String.format("$orange$%s: %d", column, bank.getInt(column, row)));
        }
    }

    private DataBank bank() { return bankSupplier.get(); }
}
