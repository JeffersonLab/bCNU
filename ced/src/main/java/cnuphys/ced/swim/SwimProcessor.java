package cnuphys.ced.swim;

import cnuphys.CLAS12Swim.CLAS12SwimResult;
import cnuphys.CLAS12Swim.CLAS12Values;
import cnuphys.bCNU.log.Log;
import cnuphys.lund.GeneratedParticleRecord;
import cnuphys.lund.LundId;
import cnuphys.lund.TrajectoryRowData;
import cnuphys.swim.Swimming;

public class SwimProcessor {

	private final SwimData data;

	public SwimProcessor(SwimData data) {
		this.data = data;
	}

	public boolean process() {
		if (data == null || !data.isValid()) {
			Log.getInstance().warning("Omitting invalid swim request");
			return false;
		}

		try {
			CLAS12SwimResult result = null;
			TrajectoryRowData trd = data.trd;
			LundId lid = trd.getLundId();

			// have to convert trd momentum to GeV
			double p = trd.getMomentum() / 1000;

			result = data.swimmer.swim(lid.getCharge(), trd.getXo(), trd.getYo(), trd.getZo(), p, trd.getTheta(),
					trd.getPhi(), data.sMax, data.h, data.tolerance);
			if (!result.isSuccess()) {
				Log.getInstance().warning("Omitting unsuccessful swim for track " + trd.getTrackId()
						+ " from " + trd.getSource() + ": " + result.statusString());
				return false;
			}

			result.getTrajectory().setLundId(lid);
			result.getTrajectory().setSource(trd.getSource());

			if (result.getTrajectory().getGeneratedParticleRecord() == null) {
				CLAS12Values iv = result.getInitialValues();
				GeneratedParticleRecord genPart = new GeneratedParticleRecord(iv.q, iv.x, iv.y, iv.z, iv.p, iv.theta,
						iv.phi);
				result.getTrajectory().setGeneratedParticleRecord(genPart);
			}

			if (data.trajectoryType == SwimData.TrajectoryType.MC) {
				Swimming.addMCTrajectory(result.getTrajectory());
			} else if (data.trajectoryType == SwimData.TrajectoryType.RECON) {
				Swimming.addReconTrajectory(result.getTrajectory());
			} else {
				Log.getInstance().warning("Unknown trajectory type in SwimProcessor: " + data.trajectoryType);
				return false;
			}
			return true;
		} catch (Exception e) {
			Log.getInstance().error("Swim failed for track " + data.trd.getTrackId()
					+ " from " + data.trd.getSource());
			Log.getInstance().exception(e);
			return false;
		}
	}

}
