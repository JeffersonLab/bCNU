package cnuphys.ced.alldata;

import java.util.function.Supplier;

import org.jlab.io.base.DataBank;

/** Read-through access to the current {@code CND::tdc} bank. */
public final class CNDTdc {

    private static final String BANK_NAME = "CND::tdc";
    private static final CNDTdc INSTANCE = new CNDTdc(
            () -> DataWarehouse.getInstance().getBank(BANK_NAME));

    private final Supplier<DataBank> bankSupplier;

    private CNDTdc(Supplier<DataBank> bankSupplier) { this.bankSupplier = bankSupplier; }

    static CNDTdc forTesting(Supplier<DataBank> bankSupplier) { return new CNDTdc(bankSupplier); }

    public static CNDTdc getInstance() { return INSTANCE; }

    public int count() {
        DataBank bank = bank();
        return (bank == null) ? 0 : bank.rows();
    }

    public byte sector(int row) { return bank().getByte("sector", row); }
    public byte layer(int row) { return bank().getByte("layer", row); }
    public short component(int row) { return bank().getShort("component", row); }
    public byte order(int row) { return bank().getByte("order", row); }
    public int tdc(int row) { return bank().getInt("TDC", row); }

    private DataBank bank() { return bankSupplier.get(); }
}
