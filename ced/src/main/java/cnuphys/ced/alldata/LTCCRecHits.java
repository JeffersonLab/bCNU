package cnuphys.ced.alldata;

import java.awt.Point;
import java.util.List;
import java.util.function.Supplier;

import org.jlab.io.base.DataBank;

/** Read-through access to the current {@code LTCC::rec} bank. */
public final class LTCCRecHits {

    public static final String BANK_NAME = "LTCC::rec";
    private static final LTCCRecHits INSTANCE = new LTCCRecHits(
            () -> DataWarehouse.getInstance().getBank(BANK_NAME),
            () -> DataWarehouse.getInstance().getCurrentEvent());

    private final Supplier<DataBank> bankSupplier;
    private final Supplier<Object> eventSupplier;
    private Object locationEvent;
    private Point[] locations;

    private LTCCRecHits(Supplier<DataBank> bankSupplier, Supplier<Object> eventSupplier) {
        this.bankSupplier = bankSupplier;
        this.eventSupplier = eventSupplier;
    }

    static LTCCRecHits forTesting(Supplier<DataBank> bankSupplier) {
        Object event = new Object();
        return new LTCCRecHits(bankSupplier, () -> event);
    }

    public static LTCCRecHits getInstance() { return INSTANCE; }

    public int count() {
        DataBank bank = bank();
        return bank == null ? 0 : bank.rows();
    }

    public short id(int row) { return bank().getShort("id", row); }
    public float x(int row) { return bank().getFloat("x", row); }
    public float y(int row) { return bank().getFloat("y", row); }
    public float z(int row) { return bank().getFloat("z", row); }

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
        String text = String.format("$Orange Red$LTCC id %d hit loc (%5.2f, %5.2f, %5.2f) cm",
                id(row), x(row), y(row), z(row));
        if (!feedback.contains(text)) feedback.add(text);
    }

    private DataBank bank() { return bankSupplier.get(); }
}
