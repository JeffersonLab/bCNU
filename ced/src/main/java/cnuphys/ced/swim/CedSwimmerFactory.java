package cnuphys.ced.swim;

import cnuphys.CLAS12Swim.CLAS12Swimmer;
import cnuphys.CLAS12Swim.ICLAS12Swimmer;

/**
 * Creates the swimming implementation supported by CED.
 */
public final class CedSwimmerFactory {

	private CedSwimmerFactory() {
	}

	/**
	 * Create a swimmer backed by the Apache Commons Math ODE integrator.
	 *
	 * @return the supported CED swimmer
	 */
	public static ICLAS12Swimmer create() {
		return new CLAS12Swimmer();
	}
}
