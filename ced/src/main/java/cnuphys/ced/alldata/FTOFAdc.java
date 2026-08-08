package cnuphys.ced.alldata;

import java.awt.Color;
import java.util.List;
import java.util.function.Supplier;

import org.jlab.io.base.DataBank;

import cnuphys.ced.alldata.datacontainer.AdcColorScale;

/** Read-through access to the current {@code FTOF::adc} bank. */
public final class FTOFAdc {

    public static final String BANK_NAME = "FTOF::adc";
    private static final FTOFAdc INSTANCE = new FTOFAdc(
            () -> DataWarehouse.getInstance().getBank(BANK_NAME));

    private final Supplier<DataBank> bankSupplier;

    private FTOFAdc(Supplier<DataBank> bankSupplier) {
        this.bankSupplier = bankSupplier;
    }

    static FTOFAdc forTesting(Supplier<DataBank> bankSupplier) {
        return new FTOFAdc(bankSupplier);
    }

    public static FTOFAdc getInstance() { return INSTANCE; }

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

    public Color adcColor(int row) {
        return ADCSupport.getADCColor(BANK_NAME, adc(row));
    }

    public Color componentColor(byte sector, byte layer, short component, byte order) {
        int adc = componentAverageAdc(sector, layer, component, order);
        if (adc <= 0) {
            return Color.white;
        }
        int maxAdc = ADCSupport.getMaxADC(BANK_NAME);
        double fraction = Math.max(0, Math.min(1.0, ((double) adc) / maxAdc));
        return AdcColorScale.getInstance().getAlphaColor(fraction, 255);
    }

    public void addFeedback(int row, List<String> feedbackStrings) {
        feedbackStrings.add(String.format("$cyan$FTOF adc %d time %6.3f order %d",
                adc(row), time(row), order(row)));
    }

    private int componentAverageAdc(byte sector, byte layer, short component, byte order) {
        DataBank bank = bank();
        if (bank == null) {
            return 0;
        }
        int count = 0;
        int sum = 0;
        for (int row = 0; row < bank.rows(); row++) {
            if (bank.getByte("sector", row) == sector && bank.getByte("layer", row) == layer
                    && bank.getShort("component", row) == component && bank.getByte("order", row) == order) {
                sum += bank.getInt("ADC", row);
                count++;
            }
        }
        return (count == 0) ? 0 : sum / count;
    }

    private DataBank bank() { return bankSupplier.get(); }
}
