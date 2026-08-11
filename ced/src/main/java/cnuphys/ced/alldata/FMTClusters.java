package cnuphys.ced.alldata;

import java.util.function.Supplier;

import org.jlab.io.base.DataBank;

/** Read-through access to the current {@code FMT::Clusters} bank. */
public final class FMTClusters {

	public static final String BANK_NAME = "FMT::Clusters";
	private static final String[] REQUIRED_COLUMNS = { "layer", "seedStrip" };
	private static final FMTClusters INSTANCE = new FMTClusters(
			() -> DataWarehouse.getInstance().getBank(BANK_NAME));

	private final Supplier<DataBank> bankSupplier;

	private FMTClusters(Supplier<DataBank> bankSupplier) {
		this.bankSupplier = bankSupplier;
	}

	static FMTClusters forTesting(Supplier<DataBank> bankSupplier) {
		return new FMTClusters(bankSupplier);
	}

	public static FMTClusters getInstance() {
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

	public byte layer(int row) {
		return bank().getByte("layer", row);
	}

	public short seedStrip(int row) {
		return bank().getShort("seedStrip", row);
	}

	public boolean hasSeedStrip(int layer, int strip) {
		for (int row = 0; row < count(); row++) {
			if (layer(row) == layer && seedStrip(row) == strip) {
				return true;
			}
		}
		return false;
	}

	private DataBank bank() {
		return bankSupplier.get();
	}
}
