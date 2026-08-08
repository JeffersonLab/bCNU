package cnuphys.ced.clasio;

import cnuphys.ced.alldata.DataWarehouse;
import cnuphys.bCNU.log.Log;
import cnuphys.magfield.MagneticFields;

/**
 * Information in the run bank
 *
 * @author heddle
 *
 */

public class RunData {

	//bank name
	private static final String bankName = "RUN::config";
	// data warehouse
	private static DataWarehouse _dw = DataWarehouse.getInstance();

	public int run = -1;
	public int event;
	public long trigger;
	public long timestamp;
	public byte type;
	public byte mode;
	public float solenoid;
	public float torus;

	public void reset() {
		run = -1;
	}

	/**
	 * Change the fields if the current event contains the run bank
	 *
	 * @return true if a run config bank was found and successfully parsed
	 */
	public boolean set() {

		boolean hasRunBank = _dw.hasBank("RUN::config");
		if (!hasRunBank) {
			return false;
		}

		int oldRun = run;

		try {
 			run = safeInt("run");
			if (run < 0) {
				return false;
			}

			event = safeInt("event");

			if (event < 0) {
				return false;
			}

			trigger = safeLong("trigger");
			timestamp = safeLong("timestamp");
			type = safeByte("type");
			mode = safeByte("mode");

			solenoid = safeFloat("solenoid");
			if (Float.isNaN(solenoid)) {
				return false;
			}

			torus = safeFloat("torus");
			if (Float.isNaN(torus)) {
				return false;
			}

			if (oldRun != run) {
				// set the mag field and menus
				MagneticFields.getInstance().changeFieldsAndMenus(torus, solenoid);
			}
			return true;
		} catch (Exception e) {
			Log.getInstance().error("Could not read RUN::config data");
			Log.getInstance().exception(e);
		}

		return false;
	}

	private long safeLong(String colName) {

		long[] data = _dw.getLong(bankName, colName);
		if ((data == null) || (data.length < 1)) {
			return -1;
		}
		return data[0];
	}

	private int safeInt(String colName) {
		int[] data = _dw.getInt(bankName, colName);
		if ((data == null) || (data.length < 1)) {
			return -1;
		}
		return data[0];
	}

	private byte safeByte(String colName) {
		byte[] data = _dw.getByte(bankName, colName);
		if ((data == null) || (data.length < 1)) {
			return -1;
		}
		return data[0];
	}

	private float safeFloat(String colName) {
		float[] data = _dw.getFloat(bankName, colName);
		if ((data == null) || (data.length < 1)) {
			return Float.NaN;
		}
		return data[0];
	}

	@Override
	public String toString() {
		String s = "run: " + run;
		s += "\nevent: " + event;
		s += "\ntrigger: " + trigger;
		s += "\ntype: " + type;
		s += "\nmode: " + mode;
		s += "\nsolenoid: " + solenoid;
		s += "\ntorus: " + torus;
		s += "\ntimeStamp: " + timestamp;
		return s;
	}
}
