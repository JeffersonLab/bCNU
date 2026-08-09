package cnuphys.ced.alldata;

import java.awt.Point;
import java.util.List;
import java.util.function.Supplier;

import org.jlab.io.base.DataBank;

/** Read-through access to one current CVT trajectory bank. */
public final class CVTTrajectories {

    public enum Kind {
        PASS1("CVT::Trajectory", false),
        RECONSTRUCTED("CVTRec::Trajectory", false),
        KALMAN_FILTER("CVTRec::KFTrajectory", true);

        private final String bankName;
        private final boolean kalmanFilter;

        Kind(String bankName, boolean kalmanFilter) {
            this.bankName = bankName;
            this.kalmanFilter = kalmanFilter;
        }
    }

    private static final CVTTrajectories PASS1 = create(Kind.PASS1);
    private static final CVTTrajectories RECONSTRUCTED = create(Kind.RECONSTRUCTED);
    private static final CVTTrajectories KALMAN_FILTER = create(Kind.KALMAN_FILTER);

    private final Kind kind;
    private final Supplier<DataBank> bankSupplier;
    private final Supplier<Object> eventSupplier;
    private Object locationEvent;
    private Point[] locations;

    private static CVTTrajectories create(Kind kind) {
        return new CVTTrajectories(kind, () -> DataWarehouse.getInstance().getBank(kind.bankName),
                () -> DataWarehouse.getInstance().getCurrentEvent());
    }

    private CVTTrajectories(Kind kind, Supplier<DataBank> bankSupplier, Supplier<Object> eventSupplier) {
        this.kind = kind;
        this.bankSupplier = bankSupplier;
        this.eventSupplier = eventSupplier;
    }

    static CVTTrajectories forTesting(Kind kind, Supplier<DataBank> bankSupplier) {
        Object event = new Object();
        return new CVTTrajectories(kind, bankSupplier, () -> event);
    }

    public static CVTTrajectories pass1() { return PASS1; }
    public static CVTTrajectories reconstructed() { return RECONSTRUCTED; }
    public static CVTTrajectories kalmanFilter() { return KALMAN_FILTER; }

    public int count() {
        DataBank bank = bank();
        return bank == null ? 0 : bank.rows();
    }

    public short id(int row) { return bank().getShort("id", row); }
    public byte detector(int row) { return bank().getByte("detector", row); }
    public byte sector(int row) { return bank().getByte("sector", row); }
    public byte layer(int row) { return bank().getByte("layer", row); }
    public byte index(int row) { return bank().getByte("index", row); }
    public float x(int row) { return bank().getFloat("x", row); }
    public float y(int row) { return bank().getFloat("y", row); }
    public float z(int row) { return bank().getFloat("z", row); }
    public float phi(int row) { return bank().getFloat("phi", row); }
    public float theta(int row) { return bank().getFloat("theta", row); }
    public float localAngle(int row) { return bank().getFloat("langle", row); }
    public float centroid(int row) { return bank().getFloat("centroid", row); }
    public float path(int row) { return bank().getFloat("path", row); }

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

    public void addFeedback(String name, int row, List<String> feedback) {
        String color = kind.kalmanFilter ? "$white$" : "$yellow$";
        feedback.add(String.format("%s%s index %d", color, name, row + 1));
        if (kind.kalmanFilter) {
            feedback.add(String.format("$white$id %d  detector %d  layer %d", id(row), detector(row), layer(row)));
            feedback.add(String.format("$white$(x,y,z) (%6.3f, %6.3f, %6.3f) cm  index %d",
                    x(row), y(row), z(row), index(row)));
        } else {
            feedback.add(String.format("$yellow$id %d  detector %d  sector %d  layer %d",
                    id(row), detector(row), sector(row), layer(row)));
            feedback.add(String.format("$yellow$(x,y,z) (%6.3f, %6.3f, %6.3f) cm  path %6.3f",
                    x(row), y(row), z(row), path(row)));
            feedback.add(String.format("$yellow$phi %6.3f  theta %6.3f  langle %5.2f  cent %5.2f",
                    phi(row), theta(row), localAngle(row), centroid(row)));
        }
    }

    private DataBank bank() { return bankSupplier.get(); }
}
