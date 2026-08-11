package cnuphys.ced.alldata;

import java.util.function.Supplier;

import org.jlab.io.base.DataBank;

/** Read-through access to the current {@code FMT::Hits} bank. */
public final class FMTRecHits {

	public static final String BANK_NAME = "FMT::Hits";
	private static final String[] REQUIRED_COLUMNS = { "layer", "strip" };
	private static final FMTRecHits INSTANCE = new FMTRecHits(
			() -> DataWarehouse.getInstance().getBank(BANK_NAME));

	private final Supplier<DataBank> bankSupplier;

	private FMTRecHits(Supplier<DataBank> bankSupplier) {
		this.bankSupplier = bankSupplier;
	}

	static FMTRecHits forTesting(Supplier<DataBank> bankSupplier) {
		return new FMTRecHits(bankSupplier);
	}

	public static FMTRecHits getInstance() {
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

	public short strip(int row) {
		return bank().getShort("strip", row);
	}

	public boolean hasHit(int layer, int strip) {
		for (int row = 0; row < count(); row++) {
			if (layer(row) == layer && strip(row) == strip) {
				return true;
			}
		}
		return false;
	}

	private DataBank bank() {
		return bankSupplier.get();
	}
}
