package cnuphys.ced.clasio;

import cnuphys.adaptiveSwim.SwimType;
import java.util.Vector;

import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.log.Log;
import cnuphys.ced.alldata.MCParticles;
import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;
import cnuphys.lund.TrajectoryRowData;
import cnuphys.lund.TrajectoryTableModel;

@SuppressWarnings("serial")
public class ClasIoMonteCarloView extends ClasIoTrajectoryInfoView {

	// singleton
	private static volatile ClasIoMonteCarloView instance;

	// one row for each reconstructed trajectory
	private static final Vector<TrajectoryRowData> _trajData = new Vector<>();


	private ClasIoMonteCarloView() {
		super("Monte Carlo Tracks");
	}

	/**
	 * Get the monte carlo event view
	 *
	 * @return the monte carlo event view
	 */
	public static ClasIoMonteCarloView getInstance() {
		if (instance == null) {
			synchronized (ClasIoMonteCarloView.class) {
				if (instance == null) {
					instance = new ClasIoMonteCarloView();
				}
			}
		}
		return instance;
	}

	@Override
	public Vector<TrajectoryRowData> getRowData() {
		return _trajData;
	}

	@Override
	public void newClasIoEvent(DataEvent event) {
		_trajectoryTable.clear(); // remove existing events
		_trajData.clear();

		if (!_eventManager.isAccumulating()) {

			// now fill the table.
			TrajectoryTableModel model = _trajectoryTable.getTrajectoryModel();

			addTracks(_trajData, MCParticles.particles());
			addTracks(_trajData, MCParticles.lund());

			model.setData(_trajData);
			_trajectoryTable.repaint();
		} // !accumulating
	}

	// add tracks
	private void addTracks(Vector<TrajectoryRowData> data, MCParticles particles) {
		try {
			for (int i = 0; i < particles.count(); i++) {

				LundId lid = LundSupport.getInstance().get(particles.pid(i));

					if (lid == null) {
						//can't swim if don't know the charge!
					//	System.err.println("Cannot swim unknown LundID: " + pid[i]);
						continue;
					}

				double xo = particles.vx(i); // cm
				double yo = particles.vy(i); // cm
				double zo = particles.vz(i); // cm

				double pxo = particles.px(i); // GeV/c
				double pyo = particles.py(i);
				double pzo = particles.pz(i);

					TrackKinematics.Direction direction = TrackKinematics.fromMomentum(pxo, pyo, pzo);
					if (direction == null) {
						continue;
					}

					// note conversions to degrees and MeV
					TrajectoryRowData row = new TrajectoryRowData(i, lid, xo, yo, zo,
							1000 * direction.momentum(), direction.thetaDegrees(), direction.phiDegrees(), 0,
							particles.bankName(), SwimType.MCSWIM);
					data.add(row);

			}
		} catch (Exception e) {
			Log.getInstance().warning("Could not create Monte Carlo trajectories from " + particles.bankName());
			Log.getInstance().exception(e);
		}
	}


	@Override
	public void openedNewEventFile(String path) {
	}

	/**
	 * Change the event source type
	 *
	 * @param source the new source: File, ET
	 */
	@Override
	public void changedEventSource(ClasIoEventManager.EventSourceType source) {
	}

}
