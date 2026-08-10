package cnuphys.ced.alldata;

import java.util.List;
import java.util.function.Supplier;

import org.jlab.io.base.DataBank;

/** Read-through access to the current {@code FTOF::hbhits} bank. */
public final class FTOFHBHits {

    public static final String BANK_NAME = "FTOF::hbhits";
    private static final FTOFHBHits INSTANCE = new FTOFHBHits(
            () -> DataWarehouse.getInstance().getBank(BANK_NAME));

    private final Supplier<DataBank> bankSupplier;

    private FTOFHBHits(Supplier<DataBank> bankSupplier) { this.bankSupplier = bankSupplier; }

    static FTOFHBHits forTesting(Supplier<DataBank> bankSupplier) { return new FTOFHBHits(bankSupplier); }

    public static FTOFHBHits getInstance() { return INSTANCE; }

    public int count() {
        DataBank bank = bank();
        return bank == null ? 0 : bank.rows();
    }

    public boolean hasRow(int row) { return row >= 0 && row < count(); }
    public byte sector(int row) { return bank().getByte("sector", row); }
    public byte layer(int row) { return bank().getByte("layer", row); }
    public float x(int row) { return bank().getFloat("x", row); }
    public float y(int row) { return bank().getFloat("y", row); }
    public float z(int row) { return bank().getFloat("z", row); }
    public float energy(int row) { return bank().getFloat("energy", row); }
    public float time(int row) { return bank().getFloat("time", row); }
    public short status(int row) { return bank().getShort("status", row); }

    public boolean hasValidGeometry(int row) {
        return hasRow(row) && sector(row) >= 1 && sector(row) <= 6
                && layer(row) >= 1 && layer(row) <= 3;
    }

    public void addFeedback(int row, List<String> feedback) {
        if (!hasRow(row)) return;
        feedback.add(String.format("$yellow$hb hit loc (%7.3f, %7.3f, %7.3f)", x(row), y(row), z(row)));
        feedback.add(String.format("$yellow$hb hit energy %7.3f time %7.3f status %d",
                energy(row), time(row), status(row)));
    }

    private DataBank bank() { return bankSupplier.get(); }
}
