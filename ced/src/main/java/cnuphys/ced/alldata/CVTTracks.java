package cnuphys.ced.alldata;

import java.util.function.Supplier;

import org.jlab.io.base.DataBank;

import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;

/** Read-through access to reconstructed central-vertex-tracker track banks. */
public final class CVTTracks {

    private static final String[] REQUIRED_COLUMNS = { "q", "pt", "phi0", "d0", "z0", "tandip", "ID" };
    private static final CVTTracks REC = create("CVTRec::Tracks");
    private static final CVTTracks PASS1 = create("CVT::Tracks");

    private final String bankName;
    private final Supplier<DataBank> bankSupplier;

    private CVTTracks(String bankName, Supplier<DataBank> bankSupplier) {
        this.bankName = bankName;
        this.bankSupplier = bankSupplier;
    }

    private static CVTTracks create(String bankName) {
        return new CVTTracks(bankName, () -> DataWarehouse.getInstance().getBank(bankName));
    }

    static CVTTracks forTesting(String bankName, Supplier<DataBank> bankSupplier) {
        return new CVTTracks(bankName, bankSupplier);
    }

    public static CVTTracks reconstructed() { return REC; }
    public static CVTTracks pass1() { return PASS1; }
    public String bankName() { return bankName; }

    public int count() {
        DataBank bank = bank();
        if (bank == null) return 0;
        for (String column : REQUIRED_COLUMNS) {
            if (!DataWarehouse.hasColumn(bank, column)) return 0;
        }
        return bank.rows();
    }

    public byte charge(int row) { return bank().getByte("q", row); }
    public float pt(int row) { return bank().getFloat("pt", row); }
    public float phi0(int row) { return bank().getFloat("phi0", row); }
    public float d0(int row) { return bank().getFloat("d0", row); }
    public float z0(int row) { return bank().getFloat("z0", row); }
    public float tanDip(int row) { return bank().getFloat("tandip", row); }
    public short id(int row) { return bank().getShort("ID", row); }
    public LundId lundId(int row) { return LundSupport.getCVTbased(charge(row)); }

    private DataBank bank() { return bankSupplier.get(); }
}
