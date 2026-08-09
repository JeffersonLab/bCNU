package cnuphys.ced.alldata;

import java.awt.Point;
import java.util.List;
import java.util.function.Supplier;

import org.jlab.io.base.DataBank;

/** Read-through access to BST or BMT reconstructed crosses. */
public final class CentralCrosses {
    private static final CentralCrosses BST = create("BST", "BSTRec::Crosses", false);
    private static final CentralCrosses BMT = create("BMT", "BMTRec::Crosses", true);

    private final String detector;
    private final boolean hasLayer;
    private final Supplier<DataBank> bankSupplier;
    private final Supplier<Object> eventSupplier;
    private Object locationEvent;
    private Point[] locations;

    private CentralCrosses(String detector, boolean hasLayer, Supplier<DataBank> bankSupplier,
            Supplier<Object> eventSupplier) {
        this.detector = detector;
        this.hasLayer = hasLayer;
        this.bankSupplier = bankSupplier;
        this.eventSupplier = eventSupplier;
    }

    private static CentralCrosses create(String detector, String bankName, boolean hasLayer) {
        return new CentralCrosses(detector, hasLayer,
                () -> DataWarehouse.getInstance().getBank(bankName),
                () -> DataWarehouse.getInstance().getCurrentEvent());
    }

    static CentralCrosses forTesting(String detector, boolean hasLayer, Supplier<DataBank> bankSupplier) {
        Object event = new Object();
        return new CentralCrosses(detector, hasLayer, bankSupplier, () -> event);
    }

    public static CentralCrosses bst() { return BST; }
    public static CentralCrosses bmt() { return BMT; }
    public int count() { DataBank bank = bank(); return bank == null ? 0 : bank.rows(); }
    public short id(int row) { return bank().getShort("ID", row); }
    public byte sector(int row) { return bank().getByte("sector", row); }
    public byte region(int row) { return bank().getByte("region", row); }
    public byte layer(int row) { return hasLayer ? bank().getByte("layer", row) : 0; }
    public float x(int row) { return bank().getFloat("x", row); }
    public float y(int row) { return bank().getFloat("y", row); }
    public float z(int row) { return bank().getFloat("z", row); }
    public float errorX(int row) { return bank().getFloat("err_x", row); }
    public float errorY(int row) { return bank().getFloat("err_y", row); }
    public float errorZ(int row) { return bank().getFloat("err_z", row); }
    public float ux(int row) { return bank().getFloat("ux", row); }
    public float uy(int row) { return bank().getFloat("uy", row); }
    public float uz(int row) { return bank().getFloat("uz", row); }
    public short cluster1Id(int row) { return bank().getShort("Cluster1_ID", row); }
    public short cluster2Id(int row) { return bank().getShort("Cluster2_ID", row); }
    public boolean isFullLocationBad(int row) { return Float.isNaN(x(row)) || Float.isNaN(y(row)) || Float.isNaN(z(row)); }
    public boolean isXYLocationBad(int row) { return Float.isNaN(x(row)) || Float.isNaN(y(row)); }
    public boolean isDirectionBad(int row) { return Float.isNaN(ux(row)) || Float.isNaN(uy(row)) || Float.isNaN(uz(row)); }

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
        String name = detector + "Cross";
        if (hasLayer) feedback.add(String.format("$Forest Green$%s cross layer %d ID %d", name, layer(row), id(row)));
        else feedback.add(String.format("$Forest Green$%s cross ID %d", name, id(row)));
        feedback.add(String.format("$Forest Green$%s sector %d region %d", name, sector(row), region(row)));
        feedback.add(String.format("$Forest Green$%s cross xyz (%-6.3f, %-6.3f, %-6.3f) cm", name, x(row), y(row), z(row)));
        feedback.add(String.format("$Forest Green$%s cross error (%-6.3f, %-6.3f, %-6.3f) cm", name, errorX(row), errorY(row), errorZ(row)));
        feedback.add(String.format("$Forest Green$%s cross cluster 1 ID %d cluster 2 ID %d", name, cluster1Id(row), cluster2Id(row)));
        feedback.add(String.format("$Forest Green$%s cross direction (%-6.3f, %-6.3f, %-6.3f)", name, ux(row), uy(row), uz(row)));
    }

    private DataBank bank() { return bankSupplier.get(); }
}
