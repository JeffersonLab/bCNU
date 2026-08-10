package cnuphys.ced.alldata;

import java.awt.Color;
import java.util.List;
import java.util.function.Supplier;

import org.jlab.io.base.DataBank;

/** Read-through access to the current {@code AHDC::adc} bank. */
public final class AHDCAdc {

    public static final String BANK_NAME = "AHDC::adc";
    private static final AHDCAdc INSTANCE = new AHDCAdc(
            () -> DataWarehouse.getInstance().getBank(BANK_NAME));

    private final Supplier<DataBank> bankSupplier;

    private AHDCAdc(Supplier<DataBank> bankSupplier) { this.bankSupplier = bankSupplier; }

    static AHDCAdc forTesting(Supplier<DataBank> bankSupplier) { return new AHDCAdc(bankSupplier); }

    public static AHDCAdc getInstance() { return INSTANCE; }

    public int count() {
        DataBank bank = bank();
        return bank == null ? 0 : bank.rows();
    }

    public boolean hasRow(int row) { return row >= 0 && row < count(); }
    public byte sector(int row) { return bank().getByte("sector", row); }
    public byte layer(int row) { return bank().getByte("layer", row); }
    public short component(int row) { return bank().getShort("component", row); }
    public byte order(int row) { return bank().getByte("order", row); }
    public int adc(int row) { return bank().getInt("ADC", row); }
    public Color color(int row) { return ADCSupport.getADCColor(BANK_NAME, adc(row)); }

    public void addFeedback(int row, List<String> feedback) {
        DataBank bank = bank();
        if (bank == null || row < 0 || row >= bank.rows()) return;
        addInt(bank, "ADC", row, feedback);
        addInt(bank, "integral", row, feedback);
        addByte(bank, "order", row, feedback);
        addShort(bank, "ped", row, feedback);
        addFloat(bank, "time", row, feedback);
        addFloat(bank, "timeOverThreshold", row, feedback);
    }

    private static void addInt(DataBank bank, String column, int row, List<String> feedback) {
        if (DataWarehouse.hasColumn(bank, column)) {
            feedback.add(String.format("$orange$%s: %d", column, bank.getInt(column, row)));
        }
    }

    private static void addByte(DataBank bank, String column, int row, List<String> feedback) {
        if (DataWarehouse.hasColumn(bank, column)) {
            feedback.add(String.format("$orange$%s: %d", column, bank.getByte(column, row)));
        }
    }

    private static void addShort(DataBank bank, String column, int row, List<String> feedback) {
        if (DataWarehouse.hasColumn(bank, column)) {
            feedback.add(String.format("$orange$%s: %d", column, bank.getShort(column, row)));
        }
    }

    private static void addFloat(DataBank bank, String column, int row, List<String> feedback) {
        if (DataWarehouse.hasColumn(bank, column)) {
            feedback.add(String.format("$orange$%s: %10.5f", column, bank.getFloat(column, row)));
        }
    }

    private DataBank bank() { return bankSupplier.get(); }
}
