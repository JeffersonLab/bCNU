package cnuphys.ced.clasio;

import cnuphys.adaptiveSwim.SwimType;
import java.util.Vector;

import org.jlab.io.base.DataEvent;

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
	private static Vector<TrajectoryRowData> _trajData = new Vector<>();

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
			model.fireTableDataChanged();
			_trajectoryTable.repaint();
			_trajectoryTable.repaint();
		} // !accumulating
	}

	private void addDCTracks(Vector<TrajectoryRowData> data, DCTracks tracks) {
		try {
			for (int i = 0; i < tracks.count(); i++) {
				double px = tracks.px(i);
				double py = tracks.py(i);
				double pz = tracks.pz(i);
				double p = Math.sqrt(px * px + py * py + pz * pz);
				double phi = Math.atan2(py, px);
				double theta = Math.acos(pz / p);

				data.add(new TrajectoryRowData(tracks.id(i), tracks.lundId(i), tracks.vx(i), tracks.vy(i),
						tracks.vz(i), 1000 * p, Math.toDegrees(theta), Math.toDegrees(phi), tracks.status(i),
						tracks.bankName(), SwimType.RECONSWIM));
			}
		} catch (Exception e) {
			System.err.println("[ClasIoReconEventView.addDCTracks] " + e.getMessage());
		}
	}

	// add CVT reconstructed tracks
	private void addRECParticleTracks(Vector<TrajectoryRowData> data) {

		try {
			RECParticles particles = RECParticles.getInstance();
			if (particles.count() > 0) {
				for (int i = 0; i < particles.count(); i++) {
					LundId lid = particles.lundId(i);

					double xo = particles.vx(i); // cm
					double yo = particles.vy(i); // cm
					double zo = particles.vz(i); // cm

					double pxo = particles.px(i); // GeV/c
					double pyo = particles.py(i);
					double pzo = particles.pz(i);

					double p = Math.sqrt(pxo * pxo + pyo * pyo + pzo * pzo); // GeV
					double phi = Math.atan2(pyo, pxo);
					double theta = Math.acos(pzo / p);

					// note conversions to degrees and MeV
					TrajectoryRowData row = new TrajectoryRowData(0, lid, xo, yo, zo, 1000 * p, Math.toDegrees(theta),
							Math.toDegrees(phi), particles.status(i), RECParticles.BANK_NAME, SwimType.RECONSWIM);
					data.add(row);

				}
			}
		} catch (Exception e) {
			String warning = "[ClasIoReconEventView.addTracks] " + e.getMessage();
			System.err.println(warning);
		}
	}

	// add CVT reconstructed tracks
	private void addCVTTracks(Vector<TrajectoryRowData> data, CVTTracks tracks) {
		try {
			for (int i = 0; i < tracks.count(); i++) {
				double phi0 = tracks.phi0(i);
				double pt = tracks.pt(i);
				double xo = -tracks.d0(i) * Math.sin(phi0);
				double yo = tracks.d0(i) * Math.cos(phi0);
				double px = pt * Math.cos(phi0);
				double py = pt * Math.sin(phi0);
				double pz = pt * tracks.tanDip(i);
				double p = Math.sqrt(px * px + py * py + pz * pz);
				double theta = Math.acos(pz / p);

				data.add(new TrajectoryRowData(tracks.id(i), tracks.lundId(i), xo, yo, tracks.z0(i), 1000 * p,
						Math.toDegrees(theta), Math.toDegrees(phi0), 0, tracks.bankName(), SwimType.RECONSWIM));
			}
		} catch (Exception e) {
			String warning = "[ClasIoReconEventView.addCVTTracks] " + e.getMessage();
			System.err.println(warning);
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
