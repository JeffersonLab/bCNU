package cnuphys.ced.alldata;

import java.util.List;

import org.jlab.io.base.DataBank;

import cnuphys.lund.DoubleFormat;

/** Read-through access to raw DC timing data with event-scoped derived noise flags. */
public final class DCRawHits {
    private static final int TOTAL_WIRES = 24192;
    private static final int WIRES_PER_SECTOR = 4032;
    private static final DCRawHits INSTANCE = new DCRawHits();

    private Object noiseEvent;
    private boolean[] noise;

    private DCRawHits() {}

    public static DCRawHits getInstance() { return INSTANCE; }

    public int count() { DataBank bank = timingBank(); return bank == null ? 0 : bank.rows(); }
    public byte sector(int row) { return timingBank().getByte("sector", row); }
    public byte layer(int row) { return timingBank().getByte("layer", row); }
    public byte superlayer(int row) { return superlayerForLayer(layer(row)); }
    public byte layerInSuperlayer(int row) { return layerInSuperlayerForLayer(layer(row)); }
    public short wire(int row) { return timingBank().getShort("component", row); }
    public byte order(int row) { return timingBank().getByte("order", row); }
    public int tdc(int row) { return timingBank().getInt("TDC", row); }

    public boolean hasTimeOverThreshold() { return isNonEmpty(bank("DC::tot")) && !isNonEmpty(bank("DC::tdc")); }
    public short timeOverThreshold(int row) { return hasTimeOverThreshold() ? timingBank().getShort("ToT", row) : 0; }

    public byte leftRight(int row) { return docaBank().getByte("LR", row); }
    public float doca(int row) { return docaBank().getFloat("doca", row); }
    public float smearedDoca(int row) { return docaBank().getFloat("sdoca", row); }
    public float time(int row) { return docaBank().getFloat("time", row); }
    public float smearedTime(int row) { return docaBank().getFloat("stime", row); }
    public boolean hasDoca(int row) { DataBank bank = docaBank(); return bank != null && row >= 0 && row < bank.rows(); }

    public void setNoiseFlags(boolean[] flags) {
        noiseEvent = DataWarehouse.getInstance().getCurrentEvent();
        noise = flags == null ? null : flags.clone();
    }

    public boolean isNoise(int row) {
        return noiseEvent == DataWarehouse.getInstance().getCurrentEvent() && noise != null
                && row >= 0 && row < noise.length && noise[row];
    }

    public double totalOccupancy() { return ((double) count()) / TOTAL_WIRES; }

    public double sectorOccupancy(int wantedSector) {
        if (wantedSector < 1 || wantedSector > 6) return 0;
        int hits = 0;
        for (int row = 0; row < count(); row++) if (sector(row) == wantedSector) hits++;
        return ((double) hits) / WIRES_PER_SECTOR;
    }

    public double superlayerOccupancy(int wantedSector, int wantedSuperlayer) {
        if (wantedSector < 1 || wantedSector > 6 || wantedSuperlayer < 1 || wantedSuperlayer > 6) return 0;
        int hits = 0;
        for (int row = 0; row < count(); row++) {
            if (sector(row) == wantedSector && superlayer(row) == wantedSuperlayer) hits++;
        }
        return hits / 672.0;
    }

    public void addFeedback(int row, boolean showNoise, boolean showDoca, List<String> feedback) {
        String color = "$Orange$";
        feedback.add(color + "DC sector " + sector(row) + " suplay " + superlayer(row)
                + " layer " + layerInSuperlayer(row) + " wire " + wire(row));
        String timing = "tdc " + tdc(row) + "  order " + order(row);
        if (hasTimeOverThreshold()) timing += "  ToT: " + timeOverThreshold(row);
        if (tdc(row) >= 0) feedback.add(color + timing);
        if (showNoise) feedback.add(color + "DC Noise guess " + (isNoise(row) ? "noise" : "not noise"));
        if (showDoca && hasDoca(row)) {
            addDocaFeedback(feedback, color + "DC SIM (doca, time) ", doca(row), time(row));
            addDocaFeedback(feedback, color + "DC SIM (sdoca, stime) ", smearedDoca(row), smearedTime(row));
        }
    }

    private static void addDocaFeedback(List<String> feedback, String prefix, float distance, float time) {
        if (!Float.isNaN(distance) && !Float.isNaN(time)) {
            feedback.add(prefix + "DC (" + DoubleFormat.doubleFormat(distance, 3) + " mm, "
                    + DoubleFormat.doubleFormat(time, 3) + ")");
        }
    }

    private DataBank timingBank() {
        DataBank bank = bank("DC::tdc");
        return isNonEmpty(bank) ? bank : nonEmptyOrNull(bank("DC::tot"));
    }
    private DataBank docaBank() { return bank("DC::doca"); }
    private DataBank bank(String name) { return DataWarehouse.getInstance().getBank(name); }
    private static boolean isNonEmpty(DataBank bank) { return bank != null && bank.rows() > 0; }
    private static DataBank nonEmptyOrNull(DataBank bank) { return isNonEmpty(bank) ? bank : null; }
    static byte superlayerForLayer(int layer) { return (byte) (((layer - 1) / 6) + 1); }
    static byte layerInSuperlayerForLayer(int layer) { return (byte) (((layer - 1) % 6) + 1); }
}
