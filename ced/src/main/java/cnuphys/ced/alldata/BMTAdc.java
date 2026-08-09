package cnuphys.ced.alldata;

import java.awt.Color;
import java.awt.Point;
import java.util.List;
import java.util.function.Supplier;

import org.jlab.io.base.DataBank;

/** Read-through access to the current {@code BMT::adc} bank. */
public final class BMTAdc {

    public static final String BANK_NAME = "BMT::adc";
    private static final BMTAdc INSTANCE = new BMTAdc(
            () -> DataWarehouse.getInstance().getBank(BANK_NAME),
            () -> DataWarehouse.getInstance().getCurrentEvent());

    private final Supplier<DataBank> bankSupplier;
    private final Supplier<Object> eventSupplier;
    private Object locationEvent;
    private Point[] locations;

    private BMTAdc(Supplier<DataBank> bankSupplier, Supplier<Object> eventSupplier) {
        this.bankSupplier = bankSupplier;
        this.eventSupplier = eventSupplier;
    }

    static BMTAdc forTesting(Supplier<DataBank> bankSupplier) {
        Object event = new Object();
        return new BMTAdc(bankSupplier, () -> event);
    }

    public static BMTAdc getInstance() { return INSTANCE; }

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

    public Color color(int row) { return ADCSupport.getADCColor(BANK_NAME, adc(row)); }

    public void setLocation(int row, Point point) {
        DataBank bank = bank();
        if (bank == null || row < 0 || row >= bank.rows()) return;
        Object event = eventSupplier.get();
        if (event != locationEvent || locations == null || locations.length != bank.rows()) {
            locationEvent = event;
            locations = new Point[bank.rows()];
        }
        locations[row] = new Point(point);
    }

    public boolean contains(int row, Point point) {
        if (eventSupplier.get() != locationEvent || locations == null || row < 0 || row >= locations.length) {
            return false;
        }
        Point location = locations[row];
        return location != null && Math.abs(location.x - point.x) <= DataDrawSupport.HITHALF
                && Math.abs(location.y - point.y) <= DataDrawSupport.HITHALF;
    }

    public void addFeedback(int row, List<String> feedback) {
        feedback.add(String.format("$cyan$BMT adc %d time %6.3f order %d", adc(row), time(row), order(row)));
    }

    private DataBank bank() { return bankSupplier.get(); }
}
