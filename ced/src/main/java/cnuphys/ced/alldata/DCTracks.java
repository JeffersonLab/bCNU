package cnuphys.ced.alldata;

import java.util.function.Supplier;

import org.jlab.io.base.DataBank;

import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;

/** Read-through access to reconstructed drift-chamber track banks. */
public final class DCTracks {

    private static final String[] REQUIRED_COLUMNS = {
            "Vtx0_x", "Vtx0_y", "Vtx0_z", "p0_x", "p0_y", "p0_z", "q", "status", "id"
    };

    private static final DCTracks HB = create("HitBasedTrkg::HBTracks", true);
    private static final DCTracks TB = create("TimeBasedTrkg::TBTracks", false);
    private static final DCTracks AI_HB = create("HitBasedTrkg::AITracks", true);
    private static final DCTracks AI_TB = create("TimeBasedTrkg::AITracks", false);

    private final String bankName;
    private final boolean hitBased;
    private final Supplier<DataBank> bankSupplier;

    private DCTracks(String bankName, boolean hitBased, Supplier<DataBank> bankSupplier) {
        this.bankName = bankName;
        this.hitBased = hitBased;
        this.bankSupplier = bankSupplier;
    }

    private static DCTracks create(String bankName, boolean hitBased) {
        return new DCTracks(bankName, hitBased, () -> DataWarehouse.getInstance().getBank(bankName));
    }

    static DCTracks forTesting(String bankName, boolean hitBased, Supplier<DataBank> bankSupplier) {
        return new DCTracks(bankName, hitBased, bankSupplier);
    }

    public static DCTracks hitBased() { return HB; }
    public static DCTracks timeBased() { return TB; }
    public static DCTracks aiHitBased() { return AI_HB; }
    public static DCTracks aiTimeBased() { return AI_TB; }

    public String bankName() { return bankName; }

    public int count() {
        DataBank bank = bank();
        if (bank == null) return 0;
        for (String column : REQUIRED_COLUMNS) {
            if (!DataWarehouse.hasColumn(bank, column)) return 0;
        }
        return bank.rows();
    }

    public float vx(int row) { return bank().getFloat("Vtx0_x", row); }
    public float vy(int row) { return bank().getFloat("Vtx0_y", row); }
    public float vz(int row) { return bank().getFloat("Vtx0_z", row); }
    public float px(int row) { return bank().getFloat("p0_x", row); }
    public float py(int row) { return bank().getFloat("p0_y", row); }
    public float pz(int row) { return bank().getFloat("p0_z", row); }
    public byte charge(int row) { return bank().getByte("q", row); }
    public short status(int row) { return bank().getShort("status", row); }
    public short id(int row) { return bank().getShort("id", row); }

    public LundId lundId(int row) {
        return hitBased ? LundSupport.getHitbased(charge(row)) : LundSupport.getTrackbased(charge(row));
    }

    private DataBank bank() { return bankSupplier.get(); }
}
