package cnuphys.ced.alldata;

import java.awt.Color;
import java.util.function.Supplier;

import org.jlab.io.base.DataBank;

import cnuphys.ced.alldata.datacontainer.AdcColorScale;

/** Read-through access to the current {@code ECAL::adc} bank. */
public final class ECalAdc {

    private static final String BANK_NAME = "ECAL::adc";
    private static final Color ADC_ZERO = new Color(245, 245, 245);
    private static final ECalAdc INSTANCE = new ECalAdc(
            () -> DataWarehouse.getInstance().getBank(BANK_NAME));

    private final Supplier<DataBank> bankSupplier;

    private ECalAdc(Supplier<DataBank> bankSupplier) {
        this.bankSupplier = bankSupplier;
    }

    static ECalAdc forTesting(Supplier<DataBank> bankSupplier) {
        return new ECalAdc(bankSupplier);
    }

    public static ECalAdc getInstance() { return INSTANCE; }

    public int count() {
        DataBank bank = bank();
        return (bank == null) ? 0 : bank.rows();
    }

    public byte sector(int row) { return bank().getByte("sector", row); }
    public byte layer(int row) { return bank().getByte("layer", row); }
    public short strip(int row) { return bank().getShort("component", row); }
    public int adc(int row) { return bank().getInt("ADC", row); }
    public float time(int row) { return bank().getFloat("time", row); }

    public boolean isPCal(int row) { return layer(row) >= 1 && layer(row) <= 3; }
    public boolean isECal(int row) { return layer(row) >= 4 && layer(row) <= 9; }
    public byte view(int row) {
        return (byte) (isPCal(row) ? layer(row) - 1 : (layer(row) - 4) % 3);
    }
    public byte plane(int row) { return (byte) ((layer(row) - 4) / 3); }

    /** Maximum ADC within the same detector section as the supplied row. */
    public int maxAdc(int row) {
        boolean pcal = isPCal(row);
        int max = 0;
        for (int i = 0; i < count(); i++) {
            if ((pcal && isPCal(i)) || (!pcal && isECal(i))) {
                max = Math.max(max, adc(i));
            }
        }
        return max;
    }

    public Color adcColor(int row) {
        int value = adc(row);
        if (value <= 0) {
            return ADC_ZERO;
        }
        double fraction = Math.max(0, Math.min(1.0, (double) value / Math.max(1, maxAdc(row))));
        int alpha = Math.min(255, 128 + (int) (127 * fraction));
        return AdcColorScale.getInstance().getAlphaColor(fraction, alpha);
    }

    private DataBank bank() { return bankSupplier.get(); }
}
