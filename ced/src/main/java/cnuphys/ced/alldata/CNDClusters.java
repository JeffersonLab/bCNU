package cnuphys.ced.alldata;

import java.awt.Point;
import java.util.List;
import java.util.function.Supplier;

import org.jlab.io.base.DataBank;

/** Read-through access to the current {@code CND::clusters} bank. */
public final class CNDClusters {

    private static final String BANK_NAME = "CND::clusters";
    private static final CNDClusters INSTANCE = new CNDClusters(
            () -> DataWarehouse.getInstance().getBank(BANK_NAME),
            () -> DataWarehouse.getInstance().getCurrentEvent());

    private final Supplier<DataBank> bankSupplier;
    private final Supplier<Object> eventSupplier;
    private Object locationEvent;
    private Point[] locations;

    private CNDClusters(Supplier<DataBank> bankSupplier, Supplier<Object> eventSupplier) {
        this.bankSupplier = bankSupplier;
        this.eventSupplier = eventSupplier;
    }

    static CNDClusters forTesting(Supplier<DataBank> bankSupplier) {
        Object event = new Object();
        return new CNDClusters(bankSupplier, () -> event);
    }

    public static CNDClusters getInstance() { return INSTANCE; }

    public int count() {
        DataBank bank = bank();
        return (bank == null) ? 0 : bank.rows();
    }

    public byte sector(int row) { return bank().getByte("sector", row); }
    public byte layer(int row) { return bank().getByte("layer", row); }
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
        feedbackStrings.add(String.format("$magenta$CND cluster xyz (%-6.3f, %-6.3f, %-6.3f) cm",
                x(row), y(row), z(row)));
        feedbackStrings.add(String.format("$magenta$CND cluster Energy %-6.3f GeV", energy(row)));
        feedbackStrings.add(String.format("$magenta$CND cluster ID %d  status %d", id(row), status(row)));
    }

    private DataBank bank() { return bankSupplier.get(); }
}
