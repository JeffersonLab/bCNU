package cnuphys.ced.alldata;

import java.awt.Point;
import java.util.List;
import java.util.function.Supplier;

import org.jlab.io.base.DataBank;

/** Read-through access to reconstructed drift-chamber cross banks. */
public final class DCCrosses {
    private static final DCCrosses HB = create("HitBasedTrkg::HBCrosses", "HBCross");
    private static final DCCrosses TB = create("TimeBasedTrkg::TBCrosses", "TBCross");
    private static final DCCrosses AI_HB = create("HitBasedTrkg::AICrosses", "HBAICross");
    private static final DCCrosses AI_TB = create("TimeBasedTrkg::AICrosses", "TBAICross");

    private final Supplier<DataBank> bankSupplier;
    private final Supplier<Object> eventSupplier;
    private final String feedbackName;
    private Object locationEvent;
    private Point[] locations;

    private DCCrosses(Supplier<DataBank> bankSupplier, Supplier<Object> eventSupplier, String feedbackName) {
        this.bankSupplier = bankSupplier;
        this.eventSupplier = eventSupplier;
        this.feedbackName = feedbackName;
    }

    private static DCCrosses create(String bankName, String feedbackName) {
        DataWarehouse warehouse = DataWarehouse.getInstance();
        return new DCCrosses(() -> warehouse.getBank(bankName), warehouse::getCurrentEvent, feedbackName);
    }

    static DCCrosses forTesting(Supplier<DataBank> bankSupplier, String feedbackName) {
        Object event = new Object();
        return new DCCrosses(bankSupplier, () -> event, feedbackName);
    }

    public static DCCrosses hitBased() { return HB; }
    public static DCCrosses timeBased() { return TB; }
    public static DCCrosses aiHitBased() { return AI_HB; }
    public static DCCrosses aiTimeBased() { return AI_TB; }

    public int count() { DataBank bank = bank(); return bank == null ? 0 : bank.rows(); }
    public short id(int row) { return bank().getShort("id", row); }
    public byte sector(int row) { return bank().getByte("sector", row); }
    public byte region(int row) { return bank().getByte("region", row); }
    public float x(int row) { return bank().getFloat("x", row); }
    public float y(int row) { return bank().getFloat("y", row); }
    public float z(int row) { return bank().getFloat("z", row); }
    public float errorX(int row) { return bank().getFloat("err_x", row); }
    public float errorY(int row) { return bank().getFloat("err_y", row); }
    public float errorZ(int row) { return bank().getFloat("err_z", row); }
    public float directionX(int row) { return bank().getFloat("ux", row); }
    public float directionY(int row) { return bank().getFloat("uy", row); }
    public float directionZ(int row) { return bank().getFloat("uz", row); }
    public short segment1Id(int row) { return bank().getShort("Segment1_ID", row); }
    public short segment2Id(int row) { return bank().getShort("Segment2_ID", row); }

    public boolean isFullLocationBad(int row) { return Float.isNaN(x(row)) || Float.isNaN(y(row)) || Float.isNaN(z(row)); }
    public boolean isXYLocationBad(int row) { return Float.isNaN(x(row)) || Float.isNaN(y(row)); }
    public boolean isErrorBad(int row) { return Float.isNaN(errorX(row)) || Float.isNaN(errorY(row)) || Float.isNaN(errorZ(row)); }

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
        feedback.add(String.format("$Forest Green$%s cross ID %d", feedbackName, id(row)));
        feedback.add(String.format("$Forest Green$%s sector %d region %d", feedbackName, sector(row), region(row)));
        feedback.add(String.format("$Forest Green$%s cross xyz (%-6.3f, %-6.3f, %-6.3f) cm", feedbackName, x(row), y(row), z(row)));
        feedback.add(String.format("$Forest Green$%s cross error (%-6.3f, %-6.3f, %-6.3f) cm", feedbackName, errorX(row), errorY(row), errorZ(row)));
        feedback.add(String.format("$Forest Green$%s cross direction (%-6.3f, %-6.3f, %-6.3f)", feedbackName, directionX(row), directionY(row), directionZ(row)));
        feedback.add(String.format("$Forest Green$%s cross seg ids %d, %d", feedbackName, segment1Id(row), segment2Id(row)));
    }

    private DataBank bank() { return bankSupplier.get(); }
}
