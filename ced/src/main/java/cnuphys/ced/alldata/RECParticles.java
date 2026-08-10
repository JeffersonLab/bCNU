package cnuphys.ced.alldata;

import java.util.function.Supplier;

import org.jlab.io.base.DataBank;

import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;

/** Read-through access to the current {@code REC::Particle} bank. */
public final class RECParticles {

    public static final String BANK_NAME = "REC::Particle";
    private static final String[] REQUIRED_COLUMNS = {
            "vx", "vy", "vz", "px", "py", "pz", "charge", "status", "pid"
    };
    private static final RECParticles INSTANCE = new RECParticles(
            () -> DataWarehouse.getInstance().getBank(BANK_NAME));

    private final Supplier<DataBank> bankSupplier;

    private RECParticles(Supplier<DataBank> bankSupplier) { this.bankSupplier = bankSupplier; }

    static RECParticles forTesting(Supplier<DataBank> bankSupplier) { return new RECParticles(bankSupplier); }

    public static RECParticles getInstance() { return INSTANCE; }

    public int count() {
        DataBank bank = bank();
        if (bank == null) return 0;
        for (String column : REQUIRED_COLUMNS) {
            if (!DataWarehouse.hasColumn(bank, column)) return 0;
        }
        return bank.rows();
    }

    public boolean hasRow(int row) { return row >= 0 && row < count(); }
    public float vx(int row) { return bank().getFloat("vx", row); }
    public float vy(int row) { return bank().getFloat("vy", row); }
    public float vz(int row) { return bank().getFloat("vz", row); }
    public float px(int row) { return bank().getFloat("px", row); }
    public float py(int row) { return bank().getFloat("py", row); }
    public float pz(int row) { return bank().getFloat("pz", row); }
    public byte charge(int row) { return bank().getByte("charge", row); }
    public short status(int row) { return bank().getShort("status", row); }
    public int pid(int row) { return bank().getInt("pid", row); }

    public LundId lundId(int row) {
        if (pid(row) != 0) return LundSupport.getInstance().get(pid(row), charge(row));
        if (charge(row) < 0) return LundSupport.unknownMinus;
        if (charge(row) > 0) return LundSupport.unknownPlus;
        return LundSupport.unknownNeutral;
    }

    private DataBank bank() { return bankSupplier.get(); }
}
