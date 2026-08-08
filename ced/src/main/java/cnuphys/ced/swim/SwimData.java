package cnuphys.ced.swim;

import cnuphys.CLAS12Swim.ICLAS12Swimmer;
import cnuphys.lund.TrajectoryRowData;

public class SwimData {
	private static final double MAX_REASONABLE_VALUE = 1.0e15;

	public enum TrajectoryType {
		MC, RECON
	}

	//holds the trajectory info
	public final TrajectoryRowData trd;

	public final TrajectoryType trajectoryType;

	//the swimmer
	public final ICLAS12Swimmer swimmer;

	//the max path length
	public final double sMax;

	public final double h;

	public final double tolerance;

	/**
	 * @param trd the trajectory row data
	 * @param sMax the max path length
	 * @param h the initial step size
	 * @param tolerance the tolerance
	 */
	public SwimData(TrajectoryRowData trd, TrajectoryType trajectoryType, double sMax, double h, double tolerance) {
		this(trd, trajectoryType, sMax, h, tolerance, CedSwimmerFactory.create());
	}

	SwimData(TrajectoryRowData trd, TrajectoryType trajectoryType, double sMax, double h, double tolerance,
			ICLAS12Swimmer swimmer) {
		this.swimmer = swimmer;
		this.trd = trd;
		this.trajectoryType = trajectoryType;
		this.sMax = sMax;
		this.h = h;
		this.tolerance = tolerance;
	}
	
	public boolean isValid() {
		return trd != null && trd.getLundId() != null && trajectoryType != null && swimmer != null
				&& sMax > 0 && h > 0 && tolerance > 0
				&& isReasonable(trd.getXo()) && isReasonable(trd.getYo()) && isReasonable(trd.getZo())
				&& isReasonable(trd.getTheta()) && isReasonable(trd.getPhi())
				&& isReasonable(trd.getMomentum());
	}

	private static boolean isReasonable(double value) {
		return Double.isFinite(value) && Math.abs(value) < MAX_REASONABLE_VALUE;
	}

}
