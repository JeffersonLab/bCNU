package cnuphys.ced.swim;

import cnuphys.CLAS12Swim.CLAS12SwimResult;
import cnuphys.CLAS12Swim.CLAS12Values;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.threading.IEventListener;
import cnuphys.lund.GeneratedParticleRecord;
import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;
import cnuphys.lund.TrajectoryRowData;
import cnuphys.swim.Swimming;

public class SwimListener implements IEventListener<Object> {

	private SwimData data;

	public SwimListener(SwimData data) {
		this.data = data;
	}

	@Override
	public void newEvent(Object o) {
		try {
			LundId lid = LundSupport.getInstance().get(data.trd.getId());

			CLAS12SwimResult result = null;
			TrajectoryRowData trd = data.trd;

			// have to convert trd momentum to GeV
			double p = trd.getMomentum() / 1000;

			result = data.swimmer.swim(lid.getCharge(), trd.getXo(), trd.getYo(), trd.getZo(), p, trd.getTheta(),
					trd.getPhi(), data.sMax, data.h, data.tolerance);
			if (!result.isSuccess()) {
				Log.getInstance().warning("Omitting unsuccessful swim for track " + trd.getTrackId()
						+ " from " + trd.getSource() + ": " + result.statusString());
				return;
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
				Log.getInstance().warning("Unknown trajectory type in SwimListener: " + data.trajectoryType);
			}
		} catch (Exception e) {
			Log.getInstance().error("Swim failed for track " + data.trd.getTrackId()
					+ " from " + data.trd.getSource());
			Log.getInstance().exception(e);
		}
	}

}
