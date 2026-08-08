package cnuphys.ced.swim;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import cnuphys.bCNU.magneticfield.swim.ISwimAll;
import cnuphys.bCNU.log.Log;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.ClasIoMonteCarloView;
import cnuphys.lund.LundId;
import cnuphys.lund.TrajectoryRowData;
import cnuphys.swim.Swimming;

/**
 * Swims all the particles in the MC bank
 *
 * @author heddle
 *
 */
public class SwimAllMC implements ISwimAll {

	/**
	 * Get all the row data so the trajectory dialog can be updated.
	 *
	 * @param manager the swim manager
	 * @return a vector of TrajectoryRowData objects.
	 */
	@Override
	public Vector<TrajectoryRowData> getRowData() {
		return ClasIoMonteCarloView.getInstance().getRowData();
	}

	/**
	 * Swim all Monte Carlo particles
	 *
	 * @param manager the swim manager
	 */
	@Override
	public void swimAll() {

		if (ClasIoEventManager.getInstance().isAccumulating()) {
			return;
		}


		Swimming.clearMCTrajectories(); // clear all existing trajectories

		Vector<TrajectoryRowData> data = getRowData();
		if (data == null) {
			return;
		}

		double stepSize = 1.0e-3;
		double tolerance = 1.0e-6;

		//used to avoid swimming duplicates
		Set<String> swam = new HashSet<>();

		for (TrajectoryRowData trd : data) {
			LundId lid = trd.getLundId();

			if (lid != null) {

					String summaryStr = SwimRequestPolicy.mcDuplicateKey(lid, trd);

					if (!swam.add(summaryStr)) {
						continue;
					}

					SwimData swimData = new SwimData(trd, SwimData.TrajectoryType.MC,
							SwimRequestPolicy.DEFAULT_MAX_PATH, stepSize, tolerance);
					if (!swimData.isValid()) {
						Log.getInstance().warning("SwimAllMC invalid swim data for " + lid.getName());
						continue;
					}
					new SwimProcessor(swimData).process();

			}

		} //for trd

	}

}
