package cnuphys.ced.alldata;

import java.util.function.Supplier;

import org.jlab.io.base.DataBank;

/** Read-through access to the current {@code FMT::Tracks} bank. */
public final class FMTTracks {

	public static final String BANK_NAME = "FMT::Tracks";
	private static final String[] REQUIRED_COLUMNS = { "index", "status", "sector", "Vtx0_x", "Vtx0_y",
			"Vtx0_z", "p0_x", "p0_y", "p0_z", "q", "chi2", "NDF" };
	private static final FMTTracks INSTANCE = new FMTTracks(
			() -> DataWarehouse.getInstance().getBank(BANK_NAME));

	private final Supplier<DataBank> bankSupplier;

	private FMTTracks(Supplier<DataBank> bankSupplier) {
		this.bankSupplier = bankSupplier;
	}

	static FMTTracks forTesting(Supplier<DataBank> bankSupplier) {
		return new FMTTracks(bankSupplier);
	}

	public static FMTTracks getInstance() {
		return INSTANCE;
	}

	public int count() {
		DataBank bank = bank();
		if (bank == null) {
			return 0;
		}
		for (String column : REQUIRED_COLUMNS) {
			if (!DataWarehouse.hasColumn(bank, column)) {
				return 0;
			}
		}
		return bank.rows();
	}

	public short index(int row) { return bank().getShort("index", row); }
	public byte status(int row) { return bank().getByte("status", row); }
	public byte sector(int row) { return bank().getByte("sector", row); }
	public float vertexX(int row) { return bank().getFloat("Vtx0_x", row); }
	public float vertexY(int row) { return bank().getFloat("Vtx0_y", row); }
	public float vertexZ(int row) { return bank().getFloat("Vtx0_z", row); }
	public float momentumX(int row) { return bank().getFloat("p0_x", row); }
	public float momentumY(int row) { return bank().getFloat("p0_y", row); }
	public float momentumZ(int row) { return bank().getFloat("p0_z", row); }
	public byte charge(int row) { return bank().getByte("q", row); }
	public float chi2(int row) { return bank().getFloat("chi2", row); }
	public byte ndf(int row) { return bank().getByte("NDF", row); }

	/**
	 * Returns the status for a DC track index, or {@code -1} when that track is not
	 * represented in the current FMT bank.
	 */
	public int statusForIndex(short trackIndex) {
		for (int row = 0; row < count(); row++) {
			if (index(row) == trackIndex) {
				return status(row);
			}
		}
		return -1;
	}

	private DataBank bank() {
		return bankSupplier.get();
	}
}
