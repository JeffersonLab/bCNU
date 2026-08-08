package cnuphys.ced.alldata;

import java.awt.Point;
import java.util.List;
import java.util.function.Supplier;

import org.jlab.io.base.DataBank;

/** Read-through access to the current {@code CTOF::clusters} bank. */
public final class CTOFClusters {

    private static final String BANK_NAME = "CTOF::clusters";
    private static final CTOFClusters INSTANCE = new CTOFClusters(
            () -> DataWarehouse.getInstance().getBank(BANK_NAME),
            () -> DataWarehouse.getInstance().getCurrentEvent());

    private final Supplier<DataBank> bankSupplier;
    private final Supplier<Object> eventSupplier;
    private Object locationEvent;
    private Point[] locations;

    private CTOFClusters(Supplier<DataBank> bankSupplier, Supplier<Object> eventSupplier) {
        this.bankSupplier = bankSupplier;
        this.eventSupplier = eventSupplier;
    }

    static CTOFClusters forTesting(Supplier<DataBank> bankSupplier) {
        Object event = new Object();
        return new CTOFClusters(bankSupplier, () -> event);
    }

    public static CTOFClusters getInstance() { return INSTANCE; }

    public int count() {
        DataBank bank = bank();
        return (bank == null) ? 0 : bank.rows();
    }

    public short component(int row) { return bank().getShort("component", row); }
    public short id(int row) { return bank().getShort("id", row); }
    public short status(int row) { return bank().getShort("status", row); }
    public float energy(int row) { return bank().getFloat("energy", row); }
    public float time(int row) { return bank().getFloat("time", row); }
    public float x(int row) { return bank().getFloat("x", row); }
    public float y(int row) { return bank().getFloat("y", row); }
    public float z(int row) { return bank().getFloat("z", row); }

    public void setLocation(int row, Point point) {
        DataBank bank = bank();
        if (bank == null || row < 0 || row >= bank.rows()) {
            return;
        }
        Object event = eventSupplier.get();
        if (event != locationEvent || locations == null || locations.length != bank.rows()) {
            locationEvent = event;
            locations = new Point[bank.rows()];
        }
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
        feedbackStrings.add(String.format("$magenta$CTOF cluster xyz (%-6.3f, %-6.3f, %-6.3f) cm",
                x(row), y(row), z(row)));
        feedbackStrings.add(String.format("$magenta$CTOF cluster Energy %-6.3f GeV", energy(row)));
        feedbackStrings.add(String.format("$magenta$CTOF cluster ID %d  status %d", id(row), status(row)));
    }

    private DataBank bank() { return bankSupplier.get(); }
}
