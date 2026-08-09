package cnuphys.ced.alldata;

import java.util.function.Supplier;

import org.jlab.io.base.DataBank;

/** Read-through access to reconstructed drift-chamber cluster banks. */
public final class DCClusters {
    private static final int MAX_HITS = 12;

    private static final DCClusters HB = create("HitBasedTrkg::HBClusters");
    private static final DCClusters TB = create("TimeBasedTrkg::TBClusters");
    private static final DCClusters AI_HB = create("HitBasedTrkg::AIClusters");
    private static final DCClusters AI_TB = create("TimeBasedTrkg::AIClusters");

    private final Supplier<DataBank> bankSupplier;

    private DCClusters(Supplier<DataBank> bankSupplier) { this.bankSupplier = bankSupplier; }

    private static DCClusters create(String bankName) {
        return new DCClusters(() -> DataWarehouse.getInstance().getBank(bankName));
    }

    static DCClusters forTesting(Supplier<DataBank> bankSupplier) { return new DCClusters(bankSupplier); }
    public static DCClusters hitBased() { return HB; }
    public static DCClusters timeBased() { return TB; }
    public static DCClusters aiHitBased() { return AI_HB; }
    public static DCClusters aiTimeBased() { return AI_TB; }

    public int count() { DataBank bank = bank(); return bank == null ? 0 : bank.rows(); }
    public byte sector(int row) { return bank().getByte("sector", row); }
    public byte superlayer(int row) { return bank().getByte("superlayer", row); }
    public short id(int row) { return bank().getShort("id", row); }
    public byte size(int row) { return bank().getByte("size", row); }
    public short status(int row) { return bank().getShort("status", row); }
    public float averageWire(int row) { return bank().getFloat("avgWire", row); }
    public float fitChiSquareProbability(int row) { return bank().getFloat("fitChisqProb", row); }
    public float fitIntercept(int row) { return bank().getFloat("fitInterc", row); }
    public float fitInterceptError(int row) { return bank().getFloat("fitIntercErr", row); }
    public float fitSlope(int row) { return bank().getFloat("fitSlope", row); }
    public float fitSlopeError(int row) { return bank().getFloat("fitSlopeErr", row); }

    public short[] hitIds(int row) {
        DataBank bank = bank();
        short[] hitIds = new short[MAX_HITS];
        for (int i = 0; i < MAX_HITS; i++) {
            hitIds[i] = bank.getShort("Hit" + (i + 1) + "_ID", row);
        }
        return hitIds;
    }

    private DataBank bank() { return bankSupplier.get(); }
}
