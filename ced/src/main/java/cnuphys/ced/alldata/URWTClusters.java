package cnuphys.ced.alldata;

import java.util.List;
import java.util.function.Supplier;

import org.jlab.io.base.DataBank;

/** Read-through access to the current {@code URWT::clusters} bank. */
public final class URWTClusters {

    public static final String BANK_NAME = "URWT::clusters";
    private static final URWTClusters INSTANCE = new URWTClusters(
            () -> DataWarehouse.getInstance().getBank(BANK_NAME));

    private final Supplier<DataBank> bankSupplier;

    private URWTClusters(Supplier<DataBank> bankSupplier) { this.bankSupplier = bankSupplier; }

    static URWTClusters forTesting(Supplier<DataBank> bankSupplier) { return new URWTClusters(bankSupplier); }

    public static URWTClusters getInstance() { return INSTANCE; }

    public int count() {
        DataBank bank = bank();
        return bank == null ? 0 : bank.rows();
    }

    public boolean hasRow(int row) { return row >= 0 && row < count(); }
    public byte sector(int row) { return bank().getByte("sector", row); }
    public byte layer(int row) { return bank().getByte("layer", row); }
    public short strip(int row) { return bank().getShort("strip", row); }
    public float xo(int row) { return bank().getFloat("xo", row); }
    public float yo(int row) { return bank().getFloat("yo", row); }
    public float zo(int row) { return bank().getFloat("zo", row); }
    public float xe(int row) { return bank().getFloat("xe", row); }
    public float ye(int row) { return bank().getFloat("ye", row); }
    public float ze(int row) { return bank().getFloat("ze", row); }

    public boolean hasValidGeometry(int row) {
        return hasRow(row) && sector(row) >= 1 && sector(row) <= 6
                && layer(row) >= 1 && layer(row) <= 4;
    }

    public void addFeedback(int row, List<String> feedback) {
        if (!hasRow(row)) return;
        feedback.add(String.format("cluster sector %d layer %d strip %d",
                sector(row), layer(row), strip(row)));
    }

    private DataBank bank() { return bankSupplier.get(); }
}
