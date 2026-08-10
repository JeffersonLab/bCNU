package cnuphys.ced.alldata;

import java.util.function.Supplier;

import org.jlab.io.base.DataBank;

/** Read-through access to one current Monte Carlo particle bank. */
public final class MCParticles {

    public enum Kind {
        PARTICLE("MC::Particle"),
        LUND("MC::Lund");

        private final String bankName;

        Kind(String bankName) { this.bankName = bankName; }
    }

    private static final String[] REQUIRED_COLUMNS = { "vx", "vy", "vz", "px", "py", "pz", "pid" };
    private static final MCParticles PARTICLES = create(Kind.PARTICLE);
    private static final MCParticles LUND = create(Kind.LUND);

    private final Kind kind;
    private final Supplier<DataBank> bankSupplier;

    private static MCParticles create(Kind kind) {
        return new MCParticles(kind, () -> DataWarehouse.getInstance().getBank(kind.bankName));
    }

    private MCParticles(Kind kind, Supplier<DataBank> bankSupplier) {
        this.kind = kind;
        this.bankSupplier = bankSupplier;
    }

    static MCParticles forTesting(Kind kind, Supplier<DataBank> bankSupplier) {
        return new MCParticles(kind, bankSupplier);
    }

    public static MCParticles particles() { return PARTICLES; }
    public static MCParticles lund() { return LUND; }

    public String bankName() { return kind.bankName; }

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
    public int pid(int row) { return bank().getInt("pid", row); }

    private DataBank bank() { return bankSupplier.get(); }
}
