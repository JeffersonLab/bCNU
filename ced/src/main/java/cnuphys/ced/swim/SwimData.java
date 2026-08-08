package cnuphys.ced.swim;

import cnuphys.CLAS12Swim.ICLAS12Swimmer;
import cnuphys.lund.TrajectoryRowData;

public class SwimData {
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
		return (trd != null && trajectoryType != null && swimmer != null && sMax > 0 && h > 0 && tolerance > 0
				&& Math.abs(trd.getXo()) < 1.0e15 && Math.abs(trd.getYo()) < 1.0e15 
				&& Math.abs(trd.getZo()) < 1.0e15 && Math.abs(trd.getTheta()) < 1.0e15
				&& Math.abs(trd.getPhi()) < 1.0e15 && Math.abs(trd.getMomentum()) < 1.0e15
				&& !Double.isNaN(trd.getXo()) && !Double.isNaN(trd.getYo())
				&& !Double.isNaN(trd.getZo()) && !Double.isNaN(trd.getTheta())
				&& !Double.isNaN(trd.getPhi()) && !Double.isNaN(trd.getMomentum()));
	}

}
