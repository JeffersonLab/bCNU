package cnuphys.ced.alldata;

import java.awt.Point;
import java.util.List;
import java.util.function.Supplier;

import org.jlab.io.base.DataBank;

/** Read-through access to the current {@code BSTRec::Hits} bank. */
public final class BSTRecHits {

    private static final String BANK_NAME = "BSTRec::Hits";
    private static final BSTRecHits INSTANCE = new BSTRecHits(
            () -> DataWarehouse.getInstance().getBank(BANK_NAME),
            () -> DataWarehouse.getInstance().getCurrentEvent());

    private final Supplier<DataBank> bankSupplier;
    private final Supplier<Object> eventSupplier;
    private Object locationEvent;
    private Point[] locations;

    private BSTRecHits(Supplier<DataBank> bankSupplier, Supplier<Object> eventSupplier) {
        this.bankSupplier = bankSupplier;
        this.eventSupplier = eventSupplier;
    }

    static BSTRecHits forTesting(Supplier<DataBank> bankSupplier) {
        Object event = new Object();
        return new BSTRecHits(bankSupplier, () -> event);
    }

    public static BSTRecHits getInstance() { return INSTANCE; }

    public int count() {
        DataBank bank = bank();
        return bank == null ? 0 : bank.rows();
    }

    public byte sector(int row) { return bank().getByte("sector", row); }
    public byte layer(int row) { return bank().getByte("layer", row); }
    public short strip(int row) { return bank().getShort("strip", row); }
    public float energy(int row) { return bank().getFloat("energy", row); }
    public float time(int row) { return bank().getFloat("time", row); }
    public short id(int row) { return bank().getShort("ID", row); }
    public byte status(int row) { return bank().getByte("status", row); }
    public float fitResidual(int row) { return bank().getFloat("fitResidual", row); }
    public short clusterId(int row) { return bank().getShort("clusterID", row); }
    public short trackId(int row) { return bank().getShort("trkID", row); }

    public boolean hasValidGeometry(int row, int[] sectorsPerLayer) {
        int sector = sector(row);
        int layer = layer(row);
        int strip = strip(row);
        return sector > 0 && layer > 0 && strip > 0 && layer < 7 && strip < 257
                && sector <= sectorsPerLayer[layer - 1];
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
        if (eventSupplier.get() != locationEvent || locations == null || row < 0 || row >= locations.length) {
            return false;
        }
        Point location = locations[row];
        return location != null && Math.abs(location.x - point.x) <= DataDrawSupport.HITHALF
                && Math.abs(location.y - point.y) <= DataDrawSupport.HITHALF;
    }

    public void addFeedback(int row, List<String> feedback) {
        feedback.add(String.format("$wheat$BSTRecHit  sector %d layer %d strip %d",
                sector(row), layer(row), strip(row)));
        feedback.add(String.format("$wheat$energy %6.3f time %6.3f", energy(row), time(row)));
        feedback.add("$wheat$BSTRecHit ID " + id(row) + " status: " + status(row));
        feedback.add("$wheat$BSTRecHit cluster " + clusterId(row) + " track " + trackId(row));
    }

    private DataBank bank() { return bankSupplier.get(); }
}
