package cnuphys.ced.alldata;

import java.util.function.Supplier;

import org.jlab.io.base.DataBank;

/** Read-through access to the current {@code BSTRec::Clusters} bank. */
public final class BSTClusters {

    private static final String BANK_NAME = "BSTRec::Clusters";
    private static final BSTClusters INSTANCE = new BSTClusters(
            () -> DataWarehouse.getInstance().getBank(BANK_NAME));

    private final Supplier<DataBank> bankSupplier;

    private BSTClusters(Supplier<DataBank> bankSupplier) {
        this.bankSupplier = bankSupplier;
    }

    static BSTClusters forTesting(Supplier<DataBank> bankSupplier) {
        return new BSTClusters(bankSupplier);
    }

    public static BSTClusters getInstance() { return INSTANCE; }

    public int count() {
        DataBank bank = bank();
        return bank == null ? 0 : bank.rows();
    }

    public boolean hasRow(int row) { return row >= 0 && row < count(); }
    public byte sector(int row) { return bank().getByte("sector", row); }
    public float x1(int row) { return bank().getFloat("x1", row); }
    public float y1(int row) { return bank().getFloat("y1", row); }
    public float x2(int row) { return bank().getFloat("x2", row); }
    public float y2(int row) { return bank().getFloat("y2", row); }

    private DataBank bank() { return bankSupplier.get(); }
}
