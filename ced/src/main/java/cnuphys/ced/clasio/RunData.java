package cnuphys.ced.clasio;

import cnuphys.ced.alldata.RunConfig;
import cnuphys.bCNU.log.Log;
import cnuphys.magfield.MagneticFields;

/**
 * Information in the run bank
 *
 * @author heddle
 *
 */

public class RunData {

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

		RunConfig config = RunConfig.getInstance();
		if (!config.hasUsableRow()) {
			return false;
		}

		int oldRun = run;

		try {
			run = config.run();
			if (run < 0) {
				return false;
			}

			event = config.event();

			if (event < 0) {
				return false;
			}

			trigger = config.trigger();
			timestamp = config.timestamp();
			type = config.type();
			mode = config.mode();

			solenoid = config.solenoid();
			if (Float.isNaN(solenoid)) {
				return false;
			}

			torus = config.torus();
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
