package cnuphys.ced.clasio;

import java.util.Vector;

import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.log.Log;
import cnuphys.ced.alldata.CVTTracks;
import cnuphys.ced.alldata.DCTracks;
import cnuphys.ced.alldata.RECParticles;
import cnuphys.lund.LundId;
import cnuphys.lund.TrajectoryRowData;
import cnuphys.lund.TrajectoryTableModel;

public class ClasIoReconEventView extends ClasIoTrajectoryInfoView {

	// singleton
	private static volatile ClasIoReconEventView instance;

	// one row for each reconstructed trajectory
	private static final Vector<TrajectoryRowData> _trajData = new Vector<>();

	private ClasIoReconEventView() {
		super("Reconstructed Tracks");
	}

	/**
	 * Get the reconstructed event view
	 *
	 * @return the reconstructed event view
	 */
	public static ClasIoReconEventView getInstance() {
		if (instance == null) {
			synchronized (ClasIoReconEventView.class) {
				if (instance == null) {
					instance = new ClasIoReconEventView();
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

			addDCTracks(_trajData, DCTracks.hitBased());
			addDCTracks(_trajData, DCTracks.timeBased());
			addRECParticleTracks(_trajData);

			addDCTracks(_trajData, DCTracks.aiHitBased());
			addDCTracks(_trajData, DCTracks.aiTimeBased());

			addCVTTracks(_trajData, CVTTracks.reconstructed());
			addCVTTracks(_trajData, CVTTracks.pass1());

			model.setData(_trajData);
			_trajectoryTable.repaint();
		} // !accumulating
	}

	private void addDCTracks(Vector<TrajectoryRowData> data, DCTracks tracks) {
		TrackRowProcessor.process(tracks.count(), i -> {
				TrackKinematics.Direction direction = TrackKinematics.fromMomentum(tracks.px(i), tracks.py(i),
						tracks.pz(i));
				if (direction == null) {
					return;
				}

				data.add(new TrajectoryRowData(tracks.id(i), tracks.lundId(i), tracks.vx(i), tracks.vy(i),
						tracks.vz(i), 1000 * direction.momentum(), direction.thetaDegrees(), direction.phiDegrees(),
						tracks.status(i), tracks.bankName()));
		}, (row, exception) -> logTrackFailure("DC", tracks.bankName(), row, exception));
	}

	// add CVT reconstructed tracks
	private void addRECParticleTracks(Vector<TrajectoryRowData> data) {

		RECParticles particles = RECParticles.getInstance();
		TrackRowProcessor.process(particles.count(), i -> {
					LundId lid = particles.lundId(i);

					double xo = particles.vx(i); // cm
					double yo = particles.vy(i); // cm
					double zo = particles.vz(i); // cm

					double pxo = particles.px(i); // GeV/c
					double pyo = particles.py(i);
					double pzo = particles.pz(i);

					TrackKinematics.Direction direction = TrackKinematics.fromMomentum(pxo, pyo, pzo);
					if (direction == null) {
						return;
					}

					// note conversions to degrees and MeV
					TrajectoryRowData row = new TrajectoryRowData(0, lid, xo, yo, zo,
							1000 * direction.momentum(), direction.thetaDegrees(), direction.phiDegrees(),
							particles.status(i), RECParticles.BANK_NAME);
					data.add(row);
		}, (row, exception) -> logTrackFailure("REC particle", RECParticles.BANK_NAME, row, exception));
	}

	// add CVT reconstructed tracks
	private void addCVTTracks(Vector<TrajectoryRowData> data, CVTTracks tracks) {
		TrackRowProcessor.process(tracks.count(), i -> {
				double phi0 = tracks.phi0(i);
				double pt = tracks.pt(i);
				double xo = -tracks.d0(i) * Math.sin(phi0);
				double yo = tracks.d0(i) * Math.cos(phi0);
				double px = pt * Math.cos(phi0);
				double py = pt * Math.sin(phi0);
				double pz = pt * tracks.tanDip(i);
				TrackKinematics.Direction direction = TrackKinematics.fromMomentum(px, py, pz);
				if (direction == null) {
					return;
				}

				data.add(new TrajectoryRowData(tracks.id(i), tracks.lundId(i), xo, yo, tracks.z0(i),
						1000 * direction.momentum(), direction.thetaDegrees(), direction.phiDegrees(), 0,
						tracks.bankName()));
		}, (row, exception) -> logTrackFailure("CVT", tracks.bankName(), row, exception));
	}

	private static void logTrackFailure(String kind, String bankName, int row, RuntimeException exception) {
		Log.getInstance().warning("Could not create " + kind + " trajectory from " + bankName + " row " + row);
		Log.getInstance().exception(exception);
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
