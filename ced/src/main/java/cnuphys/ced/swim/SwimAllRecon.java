package cnuphys.ced.swim;

import java.util.Vector;

import cnuphys.bCNU.magneticfield.swim.ISwimAll;
import cnuphys.bCNU.log.Log;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.ClasIoReconEventView;
import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;
import cnuphys.lund.TrajectoryRowData;
import cnuphys.swim.Swimming;

/**
 * Swims all the particles in the Recon bank
 *
 * @author heddle
 *
 */
public class SwimAllRecon implements ISwimAll {

	/**
	 * Get all the row data so the trajectory dialog can be updated.
	 *
	 * @param manager the swim manager
	 * @return a vector of TrajectoryRowData objects.
	 */
	@Override
	public Vector<TrajectoryRowData> getRowData() {
		return ClasIoReconEventView.getInstance().getRowData();
	}

	/**
	 * Swim all reconstructed particles
	 *
	 * @param manager the swim manager
	 */
	@Override
	public void swimAll() {
		if (ClasIoEventManager.getInstance().isAccumulating()) {
			return;
		}

		Swimming.clearReconTrajectories();

		Vector<TrajectoryRowData> data = getRowData();
		if (data == null) {
			return;
		}

		double stepSize = 1.0e-3;
		double tolerance = 1.0e-6;

		for (TrajectoryRowData trd : data) {
			LundId lid = LundSupport.getInstance().get(trd.getId());

			if (lid != null) {
				double sf = SwimRequestPolicy.maxPathForRecon(trd.getSource());
				SwimData swimData = new SwimData(trd, SwimData.TrajectoryType.RECON, sf, stepSize, tolerance);
				if (!swimData.isValid()) {
					Log.getInstance().warning("SwimAllRecon invalid swim data for " + lid.getName());
					continue;
				}
				new SwimListener(swimData).newEvent(null);

			}
		} //for trd

	}

}
