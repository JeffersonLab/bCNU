package cnuphys.ced.alldata;

import java.awt.Color;
import java.util.List;
import java.util.function.Supplier;

import org.jlab.io.base.DataBank;

/** Read-through access to the current {@code CND::adc} bank. */
public final class CNDAdc {

    public static final String BANK_NAME = "CND::adc";
    private static final CNDAdc INSTANCE = new CNDAdc(
            () -> DataWarehouse.getInstance().getBank(BANK_NAME));

    private final Supplier<DataBank> bankSupplier;

    private CNDAdc(Supplier<DataBank> bankSupplier) { this.bankSupplier = bankSupplier; }

    static CNDAdc forTesting(Supplier<DataBank> bankSupplier) { return new CNDAdc(bankSupplier); }

    public static CNDAdc getInstance() { return INSTANCE; }

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
        feedbackStrings.add(String.format("$cyan$CND adc %d time %6.3f order %d",
                adc(row), time(row), order(row)));
    }

    private DataBank bank() { return bankSupplier.get(); }
}
