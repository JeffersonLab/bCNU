package cnuphys.ced.alldata;

import java.util.List;
import java.util.function.Supplier;

import org.jlab.io.base.DataBank;

/** Read-through access to the current {@code URWT::crosses} bank. */
public final class URWTCrosses {

    public static final String BANK_NAME = "URWT::crosses";
    private static final URWTCrosses INSTANCE = new URWTCrosses(
            () -> DataWarehouse.getInstance().getBank(BANK_NAME));

    private final Supplier<DataBank> bankSupplier;

    private URWTCrosses(Supplier<DataBank> bankSupplier) { this.bankSupplier = bankSupplier; }

    static URWTCrosses forTesting(Supplier<DataBank> bankSupplier) { return new URWTCrosses(bankSupplier); }

    public static URWTCrosses getInstance() { return INSTANCE; }

    public int count() {
        DataBank bank = bank();
        return bank == null ? 0 : bank.rows();
    }

    public boolean hasRow(int row) { return row >= 0 && row < count(); }
    public byte sector(int row) { return bank().getByte("sector", row); }
    public short id(int row) { return bank().getShort("id", row); }
    public short cluster1(int row) { return bank().getShort("cluster1", row); }
    public short cluster2(int row) { return bank().getShort("cluster2", row); }
    public short status(int row) { return bank().getShort("status", row); }
    public float x(int row) { return bank().getFloat("x", row); }
    public float y(int row) { return bank().getFloat("y", row); }
    public float z(int row) { return bank().getFloat("z", row); }

    public boolean hasValidSector(int row) {
        return hasRow(row) && sector(row) >= 1 && sector(row) <= 6;
    }

    public void addFeedback(int row, List<String> feedback) {
        if (!hasRow(row)) return;
        feedback.add(String.format("$cyan$cross: %d  status: %d", id(row), status(row)));
        feedback.add(String.format("$cyan$cross clusters: %d and %d", cluster1(row), cluster2(row)));
    }

    private DataBank bank() { return bankSupplier.get(); }
}
