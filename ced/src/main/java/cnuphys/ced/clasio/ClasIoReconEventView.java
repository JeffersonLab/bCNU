package cnuphys.ced.clasio;

import cnuphys.adaptiveSwim.SwimType;
import java.util.Vector;

import org.jlab.io.base.DataEvent;

import cnuphys.ced.alldata.DCTracks;
import cnuphys.ced.alldata.DataWarehouse;
import cnuphys.ced.alldata.RECParticles;
import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;
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
			addTracks(_trajData, "REC::Particle");

			addDCTracks(_trajData, DCTracks.aiHitBased());
			addDCTracks(_trajData, DCTracks.aiTimeBased());

			// look for cvt tyracks
			addTracks(_trajData, "CVTRec::Tracks");
			addTracks(_trajData, "CVT::Tracks"); // pass 1

			model.setData(_trajData);
			model.fireTableDataChanged();
			_trajectoryTable.repaint();
			_trajectoryTable.repaint();
		} // !accumulating
	}

	// add tracks
	private void addTracks(Vector<TrajectoryRowData> data, String bankName) {
		try {

			if (bankName.contains("CVT::Tracks") || bankName.contains("CVTRec::Tracks")) {
				addCVTTracks(data, bankName);
				return;
			}

			if (bankName.contains("REC::Particle")) {
				addRECParticleTracks(data);
				return;
			}

		} catch (Exception e) {
			String warning = "[ClasIoReconEventView.addTracks] " + e.getMessage();
			System.err.println(warning);
		}
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
	private void addCVTTracks(Vector<TrajectoryRowData> data, String bankName) {
		try {
			DataWarehouse dm = DataWarehouse.getInstance();
			byte q[] = dm.getByte(bankName, "q");
			int count = (q == null) ? 0 : q.length;

			// System.err.println("Number of cvt tracks found: " + count);
			if (count > 0) {
				float pt[] = dm.getFloat(bankName, "pt");
				float phi0[] = dm.getFloat(bankName, "phi0");
				float d0[] = dm.getFloat(bankName, "d0");
				float z0[] = dm.getFloat(bankName, "z0");
				float tandip[] = dm.getFloat(bankName, "tandip");
				short id[] = dm.getShort(bankName, "ID");

				for (int i = 0; i < count; i++) {

					LundId lid = LundSupport.getCVTbased(q[i]);

					double xo = -d0[i] * Math.sin(phi0[i]);
					double yo = d0[i] * Math.cos(phi0[i]);
					double zo = z0[i];
					double pxo = pt[i] * Math.cos(phi0[i]);
					double pyo = pt[i] * Math.sin(phi0[i]);
					double pzo = pt[i] * tandip[i];

					double p = Math.sqrt(pxo * pxo + pyo * pyo + pzo * pzo);
					double theta = Math.acos(pzo / p);
					TrajectoryRowData row = new TrajectoryRowData(id[i], lid, xo, yo, zo, 1000 * p,
							Math.toDegrees(theta), Math.toDegrees(phi0[i]), 0, bankName, SwimType.RECONSWIM);
					data.add(row);
				}
			}

//			X_vtx = -d0*sin(phi0)
//			Y_vtx = d0*cos(phi0)
//			Z_vtx = z0
//			Px_vtx = pt*cos(phi0)
//			Py_vtx = pt*sin(phi0)
//			Pz_vtx = pt*tandip

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
