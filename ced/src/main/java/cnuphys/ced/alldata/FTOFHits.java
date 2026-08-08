package cnuphys.ced.alldata;

import java.awt.Point;
import java.util.List;
import java.util.function.Supplier;

import org.jlab.io.base.DataBank;

/** Read-through access to the current {@code FTOF::hits} bank. */
public final class FTOFHits {

    private static final String BANK_NAME = "FTOF::hits";
    private static final FTOFHits INSTANCE = new FTOFHits(
            () -> DataWarehouse.getInstance().getBank(BANK_NAME),
            () -> DataWarehouse.getInstance().getCurrentEvent());

    private final Supplier<DataBank> bankSupplier;
    private final Supplier<Object> eventSupplier;
    private Object locationEvent;
    private Point[] locations;

    private FTOFHits(Supplier<DataBank> bankSupplier, Supplier<Object> eventSupplier) {
        this.bankSupplier = bankSupplier;
        this.eventSupplier = eventSupplier;
    }

    static FTOFHits forTesting(Supplier<DataBank> bankSupplier) {
        Object event = new Object();
        return new FTOFHits(bankSupplier, () -> event);
    }

    public static FTOFHits getInstance() {
        return INSTANCE;
    }

    public int count() {
        DataBank bank = bank();
        return (bank == null) ? 0 : bank.rows();
    }

    public byte sector(int row) { return bank().getByte("sector", row); }
    public byte layer(int row) { return bank().getByte("layer", row); }
    public short component(int row) { return bank().getShort("component", row); }
    public short id(int row) { return bank().getShort("id", row); }
    public float x(int row) { return bank().getFloat("x", row); }
    public float y(int row) { return bank().getFloat("y", row); }
    public float z(int row) { return bank().getFloat("z", row); }
    public float energy(int row) { return bank().getFloat("energy", row); }
    public float time(int row) { return bank().getFloat("time", row); }

    public void setLocation(int row, Point point) {
        DataBank bank = bank();
        if (bank == null || row < 0 || row >= bank.rows()) {
            return;
        }
        ensureLocations(bank, eventSupplier.get());
        locations[row] = new Point(point);
    }

    public boolean contains(int row, Point point) {
        DataBank bank = bank();
        if (bank == null || eventSupplier.get() != locationEvent || locations == null
                || row < 0 || row >= locations.length) {
            return false;
        }
        Point location = locations[row];
        return location != null && Math.abs(location.x - point.x) <= DataDrawSupport.HITHALF
                && Math.abs(location.y - point.y) <= DataDrawSupport.HITHALF;
    }

    public void addFeedback(int row, List<String> feedbackStrings) {
        String feedback = String.format("$wheat$FTOF id %d hit loc (%5.2f, %5.2f, %5.2f) cm",
                id(row), x(row), y(row), z(row));
        if (!feedbackStrings.contains(feedback)) {
            feedbackStrings.add(feedback);
        }
    }

    private DataBank bank() {
        return bankSupplier.get();
    }

    private void ensureLocations(DataBank bank, Object event) {
        if (event != locationEvent || locations == null || locations.length != bank.rows()) {
            locationEvent = event;
            locations = new Point[bank.rows()];
        }
    }
}
