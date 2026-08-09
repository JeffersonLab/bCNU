package cnuphys.ced.alldata;

import java.util.function.Supplier;

import org.jlab.io.base.DataBank;

/** Read-through access to the current {@code CVTRec::Cosmics} bank. */
public final class CosmicTracks {

    private static final String BANK_NAME = "CVTRec::Cosmics";
    private static final CosmicTracks INSTANCE = new CosmicTracks(
            () -> DataWarehouse.getInstance().getBank(BANK_NAME));

    private final Supplier<DataBank> bankSupplier;

    private CosmicTracks(Supplier<DataBank> bankSupplier) {
        this.bankSupplier = bankSupplier;
    }

    static CosmicTracks forTesting(Supplier<DataBank> bankSupplier) {
        return new CosmicTracks(bankSupplier);
    }

    public static CosmicTracks getInstance() { return INSTANCE; }

    public int count() {
        DataBank bank = bank();
        return bank == null ? 0 : bank.rows();
    }

    public short id(int row) { return bank().getShort("ID", row); }
    public float yxIntercept(int row) { return bank().getFloat("trkline_yx_interc", row); }
    public float yxSlope(int row) { return bank().getFloat("trkline_yx_slope", row); }
    public float yzIntercept(int row) { return bank().getFloat("trkline_yz_interc", row); }
    public float yzSlope(int row) { return bank().getFloat("trkline_yz_slope", row); }
    public float chi2(int row) { return bank().getFloat("chi2", row); }
    public float phi(int row) { return bank().getFloat("phi", row); }
    public float theta(int row) { return bank().getFloat("theta", row); }

    /** Track x coordinate in cm at the supplied y coordinate in cm. */
    public float xAtY(int row, float y) { return yxSlope(row) * y + yxIntercept(row); }

    /** Track z coordinate in cm at the supplied y coordinate in cm. */
    public float zAtY(int row, float y) { return yzSlope(row) * y + yzIntercept(row); }

    private DataBank bank() { return bankSupplier.get(); }
}
