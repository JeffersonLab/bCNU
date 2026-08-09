package cnuphys.ced.alldata;

import java.awt.Point;
import java.util.List;
import java.util.function.Supplier;

import org.jlab.io.base.DataBank;

/** Read-through access to reconstructed drift-chamber hit banks. */
public final class DCHits {
    private static final DCHits HB = create("HitBasedTrkg::Hits", "HBHit");
    private static final DCHits TB = create("TimeBasedTrkg::TBHits", "TBHit");
    private static final DCHits AI_HB = create("HitBasedTrkg::AIHits", "HBAIHit");
    private static final DCHits AI_TB = create("TimeBasedTrkg::AIHits", "TBAIHit");

    private final Supplier<DataBank> bankSupplier;
    private final Supplier<Object> eventSupplier;
    private final String feedbackName;
    private Object locationEvent;
    private Point[] locations;

    private DCHits(Supplier<DataBank> bankSupplier, Supplier<Object> eventSupplier, String feedbackName) {
        this.bankSupplier = bankSupplier;
        this.eventSupplier = eventSupplier;
        this.feedbackName = feedbackName;
    }

    private static DCHits create(String bankName, String feedbackName) {
        DataWarehouse warehouse = DataWarehouse.getInstance();
        return new DCHits(() -> warehouse.getBank(bankName), warehouse::getCurrentEvent, feedbackName);
    }

    static DCHits forTesting(Supplier<DataBank> bankSupplier, String feedbackName) {
        Object event = new Object();
        return new DCHits(bankSupplier, () -> event, feedbackName);
    }

    public static DCHits hitBased() { return HB; }
    public static DCHits timeBased() { return TB; }
    public static DCHits aiHitBased() { return AI_HB; }
    public static DCHits aiTimeBased() { return AI_TB; }

    public int count() { DataBank bank = bank(); return bank == null ? 0 : bank.rows(); }
    public byte sector(int row) { return bank().getByte("sector", row); }
    public byte superlayer(int row) { return bank().getByte("superlayer", row); }
    public byte layer(int row) { return bank().getByte("layer", row); }
    public short wire(int row) { return bank().getShort("wire", row); }
    public short id(int row) { return bank().getShort("id", row); }
    public short status(int row) { return bank().getShort("status", row); }
    public byte leftRight(int row) { return bank().getByte("LR", row); }
    public int tdc(int row) { return bank().getInt("TDC", row); }
    public short clusterId(int row) { return bank().getShort("clusterID", row); }
    public float trackDoca(int row) { return bank().getFloat("trkDoca", row); }

    /** Hit-based banks omit docaError, matching the old sentinel behavior. */
    public float docaError(int row) {
        DataBank bank = bank();
        try {
            return bank.getFloat("docaError", row);
        } catch (RuntimeException exception) {
            return -1f;
        }
    }

    public int indexFromId(short wantedId) {
        for (int row = 0; row < count(); row++) if (id(row) == wantedId) return row;
        return -1;
    }

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
        feedback.add(String.format("$red$%s sect %d supl %d  layer %d  wire %d", feedbackName,
                sector(row), superlayer(row), layer(row), wire(row)));
    }

    private DataBank bank() { return bankSupplier.get(); }
}
