package cnuphys.ced.alldata;

import java.util.List;
import java.util.function.Supplier;

import org.jlab.io.base.DataBank;

/** Read-through access to an HTCC or LTCC TDC bank. */
public final class CherenkovTdc {

    private static final CherenkovTdc HTCC = create("HTCC");
    private static final CherenkovTdc LTCC = create("LTCC");

    private final String detector;
    private final Supplier<DataBank> bankSupplier;

    private CherenkovTdc(String detector, Supplier<DataBank> bankSupplier) {
        this.detector = detector;
        this.bankSupplier = bankSupplier;
    }

    private static CherenkovTdc create(String detector) {
        String bankName = detector + "::tdc";
        return new CherenkovTdc(detector, () -> DataWarehouse.getInstance().getBank(bankName));
    }

    static CherenkovTdc forTesting(String detector, Supplier<DataBank> bankSupplier) {
        return new CherenkovTdc(detector, bankSupplier);
    }

    public static CherenkovTdc htcc() { return HTCC; }
    public static CherenkovTdc ltcc() { return LTCC; }

    public int count() {
        DataBank bank = bank();
        return bank == null ? 0 : bank.rows();
    }

    public byte sector(int row) { return bank().getByte("sector", row); }
    public byte layer(int row) { return bank().getByte("layer", row); }
    public short component(int row) { return bank().getShort("component", row); }
    public byte order(int row) { return bank().getByte("order", row); }
    public int tdc(int row) { return bank().getInt("TDC", row); }

    public void addFeedback(int row, List<String> feedback) {
        feedback.add(String.format("%s tdc: %d", detector, tdc(row)));
    }

    private DataBank bank() { return bankSupplier.get(); }
}
