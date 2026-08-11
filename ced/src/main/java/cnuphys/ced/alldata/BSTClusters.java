package cnuphys.ced.alldata;

import java.awt.Point;
import java.util.List;
import java.util.function.Supplier;

import org.jlab.io.base.DataBank;

/** Read-through access to the current BST cluster bank. */
public final class BSTClusters {

    public static final String BANK_NAME = "BST::Clusters";
    public static final String LEGACY_BANK_NAME = "BSTRec::Clusters";
    private static final BSTClusters INSTANCE = new BSTClusters(
            () -> DataWarehouse.getInstance().getBank(BANK_NAME),
            () -> DataWarehouse.getInstance().getBank(LEGACY_BANK_NAME),
            () -> DataWarehouse.getInstance().getCurrentEvent());

    private final Supplier<DataBank> bankSupplier;
    private final Supplier<DataBank> legacyBankSupplier;
    private final Supplier<Object> eventSupplier;
    private Object locationEvent;
    private Point[] firstLocations;
    private Point[] secondLocations;

    private BSTClusters(Supplier<DataBank> bankSupplier, Supplier<DataBank> legacyBankSupplier,
            Supplier<Object> eventSupplier) {
        this.bankSupplier = bankSupplier;
        this.legacyBankSupplier = legacyBankSupplier;
        this.eventSupplier = eventSupplier;
    }

    static BSTClusters forTesting(Supplier<DataBank> bankSupplier) {
        Object event = new Object();
        return new BSTClusters(bankSupplier, () -> null, () -> event);
    }

    static BSTClusters forTesting(Supplier<DataBank> bankSupplier, Supplier<DataBank> legacyBankSupplier) {
        Object event = new Object();
        return new BSTClusters(bankSupplier, legacyBankSupplier, () -> event);
    }

    public static BSTClusters getInstance() { return INSTANCE; }

    public int count() {
        DataBank bank = bank();
        return bank == null ? 0 : bank.rows();
    }

    public boolean hasRow(int row) { return row >= 0 && row < count(); }
    public byte sector(int row) { return bank().getByte("sector", row); }
    public float x1(int row) { return bank().getFloat("x1", row); }
    public float y1(int row) { return bank().getFloat("y1", row); }
    public float x2(int row) { return bank().getFloat("x2", row); }
    public float y2(int row) { return bank().getFloat("y2", row); }

    public String activeBankName() { return bankSupplier.get() != null ? BANK_NAME : LEGACY_BANK_NAME; }

    public void setLocations(int row, Point first, Point second) {
        DataBank bank = bank();
        if (bank == null || row < 0 || row >= bank.rows()) return;
        Object event = eventSupplier.get();
        if (event != locationEvent || firstLocations == null || firstLocations.length != bank.rows()) {
            locationEvent = event;
            firstLocations = new Point[bank.rows()];
            secondLocations = new Point[bank.rows()];
        }
        firstLocations[row] = new Point(first);
        secondLocations[row] = new Point(second);
    }

    public boolean contains(int row, Point point) {
        if (eventSupplier.get() != locationEvent || firstLocations == null
                || row < 0 || row >= firstLocations.length) return false;
        return contains(firstLocations[row], point) || contains(secondLocations[row], point);
    }

    public void addFeedback(int row, List<String> feedbackStrings) {
        feedbackStrings.add(String.format("$magenta$BST cluster sector %d row %d (%s)",
                sector(row), row + 1, activeBankName()));
        feedbackStrings.add(String.format("$magenta$BST cluster endpoints (%-6.3f, %-6.3f) to (%-6.3f, %-6.3f) cm",
                x1(row), y1(row), x2(row), y2(row)));
    }

    private boolean contains(Point location, Point point) {
        return location != null && Math.abs(location.x - point.x) <= DataDrawSupport.HITHALF
                && Math.abs(location.y - point.y) <= DataDrawSupport.HITHALF;
    }

    private DataBank bank() {
        DataBank bank = bankSupplier.get();
        return bank != null ? bank : legacyBankSupplier.get();
    }
}
