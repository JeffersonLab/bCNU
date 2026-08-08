package cnuphys.ced.swim;

import java.util.ArrayList;
import java.util.Vector;

import cnuphys.bCNU.magneticfield.swim.ISwimAll;
import cnuphys.bCNU.threading.EventNotifier;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.ClasIoMonteCarloView;
import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;
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
		ArrayList<String> swam = new ArrayList<>();

		EventNotifier<Object> swimNotifier = new EventNotifier<>();

		for (TrajectoryRowData trd : data) {
			LundId lid = LundSupport.getInstance().get(trd.getId());

			if (lid != null) {

					String summaryStr = SwimRequestPolicy.mcDuplicateKey(lid, trd);

					if (swam.contains(summaryStr)) {
						continue;
					}

					swam.add(summaryStr);

					SwimData swimData = new SwimData(trd, SwimData.TrajectoryType.MC,
							SwimRequestPolicy.DEFAULT_MAX_PATH, stepSize, tolerance);
					if (!swimData.isValid()) {
						System.err.println("SwimAllMC Invalid swim data for " + lid.getName());
						continue;
					}
					swimNotifier.addListener(new SwimListener(swimData));

			}

		} //for trd

		try {
			swimNotifier.nonThreadedTriggerEvent(null);
			swimNotifier.shutdown();
		} catch (InterruptedException | java.util.concurrent.ExecutionException e) {
			e.printStackTrace();
		}


	}

}
