package cnuphys.ced.alldata;

import java.util.function.Supplier;

import org.jlab.io.base.DataBank;

/** Read-through access to the current {@code FMT::Trajectory} bank. */
public final class FMTTrajectories {

	public static final String BANK_NAME = "FMT::Trajectory";
	private static final String[] REQUIRED_COLUMNS = { "index", "layer", "x", "y", "z", "dx", "dy", "dz" };
	private static final FMTTrajectories INSTANCE = new FMTTrajectories(
			() -> DataWarehouse.getInstance().getBank(BANK_NAME));

	private final Supplier<DataBank> bankSupplier;

	private FMTTrajectories(Supplier<DataBank> bankSupplier) {
		this.bankSupplier = bankSupplier;
	}

	static FMTTrajectories forTesting(Supplier<DataBank> bankSupplier) {
		return new FMTTrajectories(bankSupplier);
	}

	public static FMTTrajectories getInstance() {
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

	public short trackIndex(int row) { return bank().getShort("index", row); }
	public byte layer(int row) { return bank().getByte("layer", row); }
	public float x(int row) { return bank().getFloat("x", row); }
	public float y(int row) { return bank().getFloat("y", row); }
	public float z(int row) { return bank().getFloat("z", row); }
	public float dcLocalX(int row) { return bank().getFloat("dx", row); }
	public float dcLocalY(int row) { return bank().getFloat("dy", row); }
	public float dcLocalZ(int row) { return bank().getFloat("dz", row); }

	private DataBank bank() {
		return bankSupplier.get();
	}
}
