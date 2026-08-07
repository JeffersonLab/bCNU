package cnuphys.ced.clasio;

import java.util.Vector;

import org.jlab.io.base.DataEvent;

import cnuphys.ced.alldata.DataWarehouse;
import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;
import cnuphys.lund.TrajectoryRowData;
import cnuphys.lund.TrajectoryType;
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

			addTracks( _trajData, "HitBasedTrkg::HBTracks");
			addTracks(_trajData, "TimeBasedTrkg::TBTracks");
			addTracks(_trajData, "REC::Particle");

			addTracks(_trajData, "HitBasedTrkg::AITracks");
			addTracks(_trajData, "TimeBasedTrkg::AITracks");

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
				addRECParticleTracks(data, bankName);
				return;
			}

			boolean hitBased = bankName.contains("HitBased");

			DataWarehouse dm = DataWarehouse.getInstance();
			float[] vx = dm.getFloat(bankName, "Vtx0_x"); // vertex x cm
			if ((vx != null) && (vx.length > 0)) {
				float[] vy = dm.getFloat(bankName, "Vtx0_y"); // vertex y cm
				float[] vz = dm.getFloat(bankName, "Vtx0_z"); // vertex z cm
				float px[] = dm.getFloat(bankName, "p0_x");
				float py[] = dm.getFloat(bankName, "p0_y");
				float pz[] = dm.getFloat(bankName, "p0_z");
				byte q[] = dm.getByte(bankName, "q");
				short status[] = dm.getShort(bankName, "status");
				short id[] = dm.getShort(bankName, "id");

				for (int i = 0; i < vx.length; i++) {

					LundId lid = (hitBased ? LundSupport.getHitbased(q[i]) : LundSupport.getTrackbased(q[i]));

					double xo = vx[i]; // cm
					double yo = vy[i]; // cm
					double zo = vz[i]; // cm

					double pxo = px[i]; // GeV/c
					double pyo = py[i];
					double pzo = pz[i];

					double p = Math.sqrt(pxo * pxo + pyo * pyo + pzo * pzo); // GeV
					double phi = Math.atan2(pyo, pxo);
					double theta = Math.acos(pzo / p);

					// note conversions to degrees and MeV
					TrajectoryRowData row = new TrajectoryRowData(id[i], lid, xo, yo, zo, 1000 * p,
							Math.toDegrees(theta), Math.toDegrees(phi), status[i], bankName, TrajectoryType.RECON);
					data.add(row);

				}
			}
		} catch (Exception e) {
			String warning = "[ClasIoReconEventView.addTracks] " + e.getMessage();
			System.err.println(warning);
		}
	}

	// add CVT reconstructed tracks
	private void addRECParticleTracks(Vector<TrajectoryRowData> data, String bankName) {

		DataWarehouse dm = DataWarehouse.getInstance();

		try {
			float[] vx = dm.getFloat(bankName, "vx"); // vertex x cm
			if ((vx != null) && (vx.length > 0)) {
				float[] vy = dm.getFloat(bankName, "vy"); // vertex y cm
				float[] vz = dm.getFloat(bankName, "vz"); // vertex z cm
				float px[] = dm.getFloat(bankName, "px");
				float py[] = dm.getFloat(bankName, "py");
				float pz[] = dm.getFloat(bankName, "pz");
				byte charge[] = dm.getByte(bankName, "charge");
				short status[] = dm.getShort(bankName, "status");
				int pid[] = dm.getInt(bankName, "pid");

				LundId lid;

				for (int i = 0; i < vx.length; i++) {

					if (pid[i] == 0) {
						if (charge[i] == -1) {
							lid = LundSupport.unknownMinus;
						}
						if (charge[i] == 1) {
							lid = LundSupport.unknownPlus;
						} else {
							lid = LundSupport.unknownNeutral;

						}
					} else {
						lid = LundSupport.getInstance().get(pid[i], charge[i]);
					}

					double xo = vx[i]; // cm
					double yo = vy[i]; // cm
					double zo = vz[i]; // cm

					double pxo = px[i]; // GeV/c
					double pyo = py[i];
					double pzo = pz[i];

					double p = Math.sqrt(pxo * pxo + pyo * pyo + pzo * pzo); // GeV
					double phi = Math.atan2(pyo, pxo);
					double theta = Math.acos(pzo / p);

					// note conversions to degrees and MeV
					TrajectoryRowData row = new TrajectoryRowData(0, lid, xo, yo, zo, 1000 * p, Math.toDegrees(theta),
							Math.toDegrees(phi), status[i], bankName, TrajectoryType.RECON);
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
							Math.toDegrees(theta), Math.toDegrees(phi0[i]), 0, bankName, TrajectoryType.RECON);
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
