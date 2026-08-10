package cnuphys.ced.alldata;

import java.util.List;
import java.util.function.Supplier;

import org.jlab.io.base.DataBank;

/** Read-through access to the current {@code URWT::hits} bank. */
public final class URWTHits {

    public static final String BANK_NAME = "URWT::hits";
    private static final URWTHits INSTANCE = new URWTHits(
            () -> DataWarehouse.getInstance().getBank(BANK_NAME));

    private final Supplier<DataBank> bankSupplier;

    private URWTHits(Supplier<DataBank> bankSupplier) { this.bankSupplier = bankSupplier; }

    static URWTHits forTesting(Supplier<DataBank> bankSupplier) { return new URWTHits(bankSupplier); }

    public static URWTHits getInstance() { return INSTANCE; }

    public int count() {
        DataBank bank = bank();
        return bank == null ? 0 : bank.rows();
    }

    public boolean hasRow(int row) { return row >= 0 && row < count(); }
    public byte sector(int row) { return bank().getByte("sector", row); }
    public byte layer(int row) { return bank().getByte("layer", row); }
    public short strip(int row) { return bank().getShort("strip", row); }

    public boolean hasValidGeometry(int row) {
        return hasRow(row) && sector(row) >= 1 && sector(row) <= 6
                && layer(row) >= 1 && layer(row) <= 4
                && strip(row) >= 1 && strip(row) <= 1485;
    }

    public void addFeedback(int row, List<String> feedback) {
        if (!hasRow(row)) return;
        feedback.add(String.format("hit sector %d layer %d strip %d",
                sector(row), layer(row), strip(row)));
    }

    private DataBank bank() { return bankSupplier.get(); }
}
