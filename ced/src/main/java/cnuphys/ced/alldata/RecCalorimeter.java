package cnuphys.ced.alldata;

import java.util.function.Supplier;

import org.jlab.io.base.DataBank;

import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;

/** Read-through access to {@code REC::Calorimeter} and its particle association. */
public final class RecCalorimeter {

    private static final String BANK_NAME = "REC::Calorimeter";
    private static final int NO_PID = -999999;
    private static final RecCalorimeter INSTANCE = new RecCalorimeter(
            () -> DataWarehouse.getInstance().getBank(BANK_NAME),
            () -> DataWarehouse.getInstance().getBank("REC::Particle"));

    private final Supplier<DataBank> bankSupplier;
    private final Supplier<DataBank> particleSupplier;

    private RecCalorimeter(Supplier<DataBank> bankSupplier, Supplier<DataBank> particleSupplier) {
        this.bankSupplier = bankSupplier;
        this.particleSupplier = particleSupplier;
    }

    static RecCalorimeter forTesting(Supplier<DataBank> bankSupplier, Supplier<DataBank> particleSupplier) {
        return new RecCalorimeter(bankSupplier, particleSupplier);
    }

    public static RecCalorimeter getInstance() { return INSTANCE; }

    public int count() {
        DataBank bank = bank();
        return bank == null ? 0 : bank.rows();
    }

    public byte sector(int row) { return bank().getByte("sector", row); }
    public byte layer(int row) { return bank().getByte("layer", row); }
    public short particleIndex(int row) { return bank().getShort("pindex", row); }
    public float time(int row) { return bank().getFloat("time", row); }
    public float energy(int row) { return bank().getFloat("energy", row); }
    public float x(int row) { return bank().getFloat("x", row); }
    public float y(int row) { return bank().getFloat("y", row); }
    public float z(int row) { return bank().getFloat("z", row); }

    public boolean isPCal(int row) { return layer(row) >= 1 && layer(row) <= 3; }
    public boolean isECal(int row) { return layer(row) >= 4 && layer(row) <= 9; }
    public byte view(int row) {
        return (byte) (isPCal(row) ? layer(row) - 1 : (layer(row) - 4) % 3);
    }
    public byte plane(int row) { return (byte) ((layer(row) - 4) / 3); }

    public float radius(int row) {
        double value = energy(row);
        if (value < 0.05) {
            return 0;
        }
        return (float) Math.max(1, Math.min(40, Math.log((value + 1.0e-8) / 1.0e-8)));
    }

    public int pid(int row) {
        DataBank particles = particleSupplier.get();
        int index = particleIndex(row);
        return particles == null || index < 0 || index >= particles.rows() ? NO_PID : particles.getInt("pid", index);
    }

    public String pidString(int row) {
        int value = pid(row);
        if (value == NO_PID) {
            return "REC PID not available";
        }
        LundId id = LundSupport.getInstance().get(value);
        return id == null ? "REC PID " + value : "REC PID " + id.getName();
    }

    private DataBank bank() { return bankSupplier.get(); }
}
