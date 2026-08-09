package cnuphys.ced.alldata;

import java.awt.Point;
import java.util.List;
import java.util.function.Supplier;

import org.jlab.io.base.DataBank;

/** Read-through access to the current {@code ECAL::clusters} bank. */
public final class ECalClusters {

    private static final String BANK_NAME = "ECAL::clusters";
    private static final ECalClusters INSTANCE = new ECalClusters(
            () -> DataWarehouse.getInstance().getBank(BANK_NAME),
            () -> DataWarehouse.getInstance().getCurrentEvent());

    private final Supplier<DataBank> bankSupplier;
    private final Supplier<Object> eventSupplier;
    private Object locationEvent;
    private Point[] locations;

    private ECalClusters(Supplier<DataBank> bankSupplier, Supplier<Object> eventSupplier) {
        this.bankSupplier = bankSupplier;
        this.eventSupplier = eventSupplier;
    }

    static ECalClusters forTesting(Supplier<DataBank> bankSupplier) {
        Object event = new Object();
        return new ECalClusters(bankSupplier, () -> event);
    }

    public static ECalClusters getInstance() { return INSTANCE; }

    public int count() {
        DataBank bank = bank();
        return (bank == null) ? 0 : bank.rows();
    }

    public byte sector(int row) { return bank().getByte("sector", row); }
    public byte layer(int row) { return bank().getByte("layer", row); }
    public float time(int row) { return bank().getFloat("time", row); }
    public float energy(int row) { return bank().getFloat("energy", row); }
    public float x(int row) { return bank().getFloat("x", row); }
    public float y(int row) { return bank().getFloat("y", row); }
    public float z(int row) { return bank().getFloat("z", row); }

    public boolean isPCal(int row) { return layer(row) >= 1 && layer(row) <= 3; }
    public boolean isECal(int row) { return layer(row) >= 4 && layer(row) <= 9; }
    public int view(int row) { return isPCal(row) ? layer(row) - 1 : (layer(row) - 4) % 3; }
    public int plane(int row) { return (layer(row) - 4) / 3; }

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
        String detector = isPCal(row) ? "PCAL" : "EC";
        feedbackStrings.add(String.format("$magenta$%s cluster xyz (%-6.3f, %-6.3f, %-6.3f) cm",
                detector, x(row), y(row), z(row)));
        if (isECal(row)) {
            feedbackStrings.add(String.format("$magenta$EC cluster plane %s",
                    cnuphys.ced.geometry.ECGeometry.PLANE_NAMES[plane(row)]));
        }
        feedbackStrings.add(String.format("$magenta$%s cluster view %s", detector,
                cnuphys.ced.geometry.ECGeometry.VIEW_NAMES[view(row)]));
        String energyFormat = isPCal(row) ? "$magenta$PCAL cluster Energy %-6.3f GeV"
                : "$magenta$EC cluster Energy %-7.4f GeV";
        feedbackStrings.add(String.format(energyFormat, energy(row)));
    }

    private DataBank bank() { return bankSupplier.get(); }
}
