package cnuphys.ced.alldata;

import java.util.function.Supplier;

import org.jlab.io.base.DataBank;

/** Read-through access to reconstructed drift-chamber segment banks. */
public final class DCSegments {
    private static final DCSegments HB = create("HitBasedTrkg::HBSegments");
    private static final DCSegments TB = create("TimeBasedTrkg::TBSegments");
    private static final DCSegments AI_HB = create("HitBasedTrkg::AISegments");
    private static final DCSegments AI_TB = create("TimeBasedTrkg::AISegments");

    private final Supplier<DataBank> bankSupplier;

    private DCSegments(Supplier<DataBank> bankSupplier) { this.bankSupplier = bankSupplier; }

    private static DCSegments create(String bankName) {
        return new DCSegments(() -> DataWarehouse.getInstance().getBank(bankName));
    }

    static DCSegments forTesting(Supplier<DataBank> bankSupplier) { return new DCSegments(bankSupplier); }
    public static DCSegments hitBased() { return HB; }
    public static DCSegments timeBased() { return TB; }
    public static DCSegments aiHitBased() { return AI_HB; }
    public static DCSegments aiTimeBased() { return AI_TB; }

    public int count() { DataBank bank = bank(); return bank == null ? 0 : bank.rows(); }
    public byte sector(int row) { return bank().getByte("sector", row); }
    public byte superlayer(int row) { return bank().getByte("superlayer", row); }
    public float x1(int row) { return bank().getFloat("SegEndPoint1X", row); }
    public float z1(int row) { return bank().getFloat("SegEndPoint1Z", row); }
    public float x2(int row) { return bank().getFloat("SegEndPoint2X", row); }
    public float z2(int row) { return bank().getFloat("SegEndPoint2Z", row); }
    private DataBank bank() { return bankSupplier.get(); }
}
