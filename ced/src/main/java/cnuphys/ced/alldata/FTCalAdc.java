package cnuphys.ced.alldata;

import java.awt.Color;
import java.util.List;
import java.util.function.Supplier;

import org.jlab.io.base.DataBank;

/** Read-through access to the current {@code FTCAL::adc} bank. */
public final class FTCalAdc {

    public static final String BANK_NAME = "FTCAL::adc";
    private static final FTCalAdc INSTANCE = new FTCalAdc(
            () -> DataWarehouse.getInstance().getBank(BANK_NAME));

    private final Supplier<DataBank> bankSupplier;

    private FTCalAdc(Supplier<DataBank> bankSupplier) { this.bankSupplier = bankSupplier; }

    static FTCalAdc forTesting(Supplier<DataBank> bankSupplier) { return new FTCalAdc(bankSupplier); }

    public static FTCalAdc getInstance() { return INSTANCE; }

    public int count() {
        DataBank bank = bank();
        return (bank == null) ? 0 : bank.rows();
    }

    public byte sector(int row) { return bank().getByte("sector", row); }
    public byte layer(int row) { return bank().getByte("layer", row); }
    public short component(int row) { return bank().getShort("component", row); }
    public byte order(int row) { return bank().getByte("order", row); }
    public int adc(int row) { return bank().getInt("ADC", row); }
    public float time(int row) { return bank().getFloat("time", row); }

    public Color color(int row) { return ADCSupport.getADCColor(BANK_NAME, adc(row)); }

    public void addFeedback(int row, List<String> feedbackStrings) {
        feedbackStrings.add(String.format("$cyan$FTCAL adc %d time %6.3f order %d",
                adc(row), time(row), order(row)));
    }

    private DataBank bank() { return bankSupplier.get(); }
}
