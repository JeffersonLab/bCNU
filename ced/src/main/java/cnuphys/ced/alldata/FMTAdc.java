package cnuphys.ced.alldata;

import java.util.function.Supplier;

import org.jlab.io.base.DataBank;

/** Read-through access to the current {@code FMT::adc} bank. */
public final class FMTAdc {

    public static final String BANK_NAME = "FMT::adc";
    private static final String[] REQUIRED_COLUMNS = { "layer", "component" };
    private static final FMTAdc INSTANCE = new FMTAdc(
            () -> DataWarehouse.getInstance().getBank(BANK_NAME));

    private final Supplier<DataBank> bankSupplier;

    private FMTAdc(Supplier<DataBank> bankSupplier) { this.bankSupplier = bankSupplier; }

    static FMTAdc forTesting(Supplier<DataBank> bankSupplier) { return new FMTAdc(bankSupplier); }
    public static FMTAdc getInstance() { return INSTANCE; }

    public int count() {
        DataBank bank = bank();
        if (bank == null) return 0;
        for (String column : REQUIRED_COLUMNS) {
            if (!DataWarehouse.hasColumn(bank, column)) return 0;
        }
        return bank.rows();
    }

    public byte layer(int row) { return bank().getByte("layer", row); }
    public short component(int row) { return bank().getShort("component", row); }

    public boolean hasHit(int layer, int component) {
        for (int row = 0; row < count(); row++) {
            if (layer(row) == layer && component(row) == component) return true;
        }
        return false;
    }

    private DataBank bank() { return bankSupplier.get(); }
}
