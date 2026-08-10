package cnuphys.ced.alldata;

import java.awt.Point;
import java.util.List;
import java.util.function.Supplier;

import org.jlab.io.base.DataBank;

/** Read-through access to the current {@code FMTRec::Crosses} bank. */
public final class FMTCrosses {

    public static final String BANK_NAME = "FMTRec::Crosses";
    private static final FMTCrosses INSTANCE = new FMTCrosses(
            () -> DataWarehouse.getInstance().getBank(BANK_NAME),
            () -> DataWarehouse.getInstance().getCurrentEvent());

    private final Supplier<DataBank> bankSupplier;
    private final Supplier<Object> eventSupplier;
    private Object locationEvent;
    private Point[] locations;

    private FMTCrosses(Supplier<DataBank> bankSupplier, Supplier<Object> eventSupplier) {
        this.bankSupplier = bankSupplier;
        this.eventSupplier = eventSupplier;
    }

    static FMTCrosses forTesting(Supplier<DataBank> bankSupplier) {
        Object event = new Object();
        return new FMTCrosses(bankSupplier, () -> event);
    }

    public static FMTCrosses getInstance() { return INSTANCE; }
    public int count() { DataBank bank = bank(); return bank == null ? 0 : bank.rows(); }
    public short id(int row) { return bank().getShort("ID", row); }
    public byte sector(int row) { return bank().getByte("sector", row); }
    public byte region(int row) { return bank().getByte("region", row); }
    public float x(int row) { return bank().getFloat("x", row); }
    public float y(int row) { return bank().getFloat("y", row); }
    public float z(int row) { return bank().getFloat("z", row); }
    public float errorX(int row) { return bank().getFloat("err_x", row); }
    public float errorY(int row) { return bank().getFloat("err_y", row); }
    public float errorZ(int row) { return bank().getFloat("err_z", row); }
    public float ux(int row) { return bank().getFloat("ux", row); }
    public float uy(int row) { return bank().getFloat("uy", row); }
    public float uz(int row) { return bank().getFloat("uz", row); }

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
        if (eventSupplier.get() != locationEvent || locations == null || row < 0 || row >= locations.length) return false;
        Point location = locations[row];
        return location != null && Math.abs(location.x - point.x) <= DataDrawSupport.HITHALF
                && Math.abs(location.y - point.y) <= DataDrawSupport.HITHALF;
    }

    public void addFeedback(int row, List<String> feedback) {
        feedback.add(String.format("$Forest Green$FMTRec cross ID %d", id(row)));
        feedback.add(String.format("$Forest Green$FMTRec sector %d region %d", sector(row), region(row)));
        feedback.add(String.format("$Forest Green$FMTRec cross xyz (%-6.3f, %-6.3f, %-6.3f) cm",
                x(row), y(row), z(row)));
        feedback.add(String.format("$Forest Green$FMTRec cross error (%-6.3f, %-6.3f, %-6.3f) cm",
                errorX(row), errorY(row), errorZ(row)));
        feedback.add(String.format("$Forest Green$FMTRec cross direction (%-6.3f, %-6.3f, %-6.3f)",
                ux(row), uy(row), uz(row)));
    }

    private DataBank bank() { return bankSupplier.get(); }
}
