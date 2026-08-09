package cnuphys.ced.alldata;

import java.awt.Color;
import java.util.List;
import java.util.function.Supplier;

import org.jlab.io.base.DataBank;

/** Read-through access to an HTCC or LTCC ADC bank. */
public final class CherenkovAdc {

    private static final CherenkovAdc HTCC = create("HTCC");
    private static final CherenkovAdc LTCC = create("LTCC");

    private final String detector;
    private final String bankName;
    private final Supplier<DataBank> bankSupplier;

    private CherenkovAdc(String detector, String bankName, Supplier<DataBank> bankSupplier) {
        this.detector = detector;
        this.bankName = bankName;
        this.bankSupplier = bankSupplier;
    }

    private static CherenkovAdc create(String detector) {
        String bankName = detector + "::adc";
        return new CherenkovAdc(detector, bankName, () -> DataWarehouse.getInstance().getBank(bankName));
    }

    static CherenkovAdc forTesting(String detector, Supplier<DataBank> bankSupplier) {
        return new CherenkovAdc(detector, detector + "::adc", bankSupplier);
    }

    public static CherenkovAdc htcc() { return HTCC; }
    public static CherenkovAdc ltcc() { return LTCC; }

    public int count() {
        DataBank bank = bank();
        return bank == null ? 0 : bank.rows();
    }

    public byte sector(int row) { return bank().getByte("sector", row); }
    public byte layer(int row) { return bank().getByte("layer", row); }
    public short component(int row) { return bank().getShort("component", row); }
    public byte order(int row) { return bank().getByte("order", row); }
    public int adc(int row) { return bank().getInt("ADC", row); }
    public float time(int row) { return bank().getFloat("time", row); }

    public Color alphaColor(int row) { return ADCSupport.getADCAlphaColor(bankName, adc(row)); }

    public void addFeedback(int row, List<String> feedback) {
        feedback.add(String.format("%s adc: %d time: %8.3f", detector, adc(row), time(row)));
    }

    private DataBank bank() { return bankSupplier.get(); }
}
