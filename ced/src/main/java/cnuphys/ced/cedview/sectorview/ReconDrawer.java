package cnuphys.ced.cedview.sectorview;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.view.FBData;
import cnuphys.ced.alldata.DataDrawSupport;
import cnuphys.ced.alldata.datacontainer.cal.ECalClusterData;
import cnuphys.ced.alldata.datacontainer.cal.ECalReconData;
import cnuphys.ced.alldata.datacontainer.cal.PCalClusterData;
import cnuphys.ced.alldata.datacontainer.cal.PCalReconData;
import cnuphys.ced.alldata.datacontainer.dc.ATrkgHitData;
import cnuphys.ced.alldata.datacontainer.dc.HBTrkgAIHitData;
import cnuphys.ced.alldata.datacontainer.dc.HBTrkgHitData;
import cnuphys.ced.alldata.datacontainer.dc.TBTrkgAIHitData;
import cnuphys.ced.alldata.datacontainer.dc.TBTrkgHitData;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.frame.CedColors;
import cnuphys.ced.geometry.ECGeometry;

public class ReconDrawer extends SectorViewDrawer {

	// cached for feedback
	private ArrayList<FBData> _fbData = new ArrayList<>();

	// data containers
	ECalReconData ecRecData = ECalReconData.getInstance();
	PCalReconData pcalRecData = PCalReconData.getInstance();
	private HBTrkgHitData _hbData = HBTrkgHitData.getInstance();
	private TBTrkgHitData _tbData = TBTrkgHitData.getInstance();
	private HBTrkgAIHitData _hbAIData = HBTrkgAIHitData.getInstance();
	private TBTrkgAIHitData _tbAIData = TBTrkgAIHitData.getInstance();

	/**
	 * Reconstructed hits drawer
	 *
	 * @param view
	 */
	public ReconDrawer(SectorView view) {
		super(view);
	}

	@Override
	public void draw(Graphics g, IContainer container) {

		_fbData.clear();

		if (_eventManager.isAccumulating() || !_view.isSingleEventMode()) {
			return;
		}
		
		if (!ClasIoEventManager.getInstance().hasCurrentEvent()) {
			return;
		}

		// DC HB and TB Hits
		drawDCReconAndDOCA(g, container);

		// Reconstructed clusters
		if (_view.showClusters()) {
			drawClusters(g, container);
		}

		if (_view.showDCHBSegments()) {
			for (int supl = 1; supl <= 6; supl++) {
				_view.getSuperLayerDrawer(0, supl).drawHitBasedSegments(g, container);
			}
		}

		if (_view.showDCTBSegments()) {
			for (int supl = 1; supl <= 6; supl++) {
				_view.getSuperLayerDrawer(0, supl).drawTimeBasedSegments(g, container);
			}
		}

		if (_view.showAIDCHBSegments()) {
			for (int supl = 1; supl <= 6; supl++) {
				_view.getSuperLayerDrawer(0, supl).drawAIHitBasedSegments(g, container);
			}
		}

		if (_view.showAIDCTBSegments()) {
			for (int supl = 1; supl <= 6; supl++) {
				_view.getSuperLayerDrawer(0, supl).drawAITimeBasedSegments(g, container);
			}
		}

		if (_view.showRecCal()) {
			drawRecCal(g, container);
		}

	}

	// draw data from the REC::Calorimeter bank
	private void drawRecCal(Graphics g, IContainer container) {

		Point pp = new Point();
		Point2D.Double wp = new Point2D.Double();
		Rectangle2D.Double wr = new Rectangle2D.Double();

		// draw ECAL
		for (int i = 0; i < ecRecData.count(); i++) {
			if (_view.containsSector(ecRecData.sector.get(i))) {

				float x = ecRecData.x.get(i);
				float y = ecRecData.y.get(i);
				float z = ecRecData.z.get(i);

				_view.projectClasToWorld(x, y, z, _view.getProjectionPlane(), wp);
				container.worldToLocal(pp, wp);
				DataDrawSupport.drawECALRec(g, pp, false);

				double r = Math.sqrt(x * x + y * y + z * z);
				double theta = Math.toDegrees(Math.acos(z / r));
				double phi = Math.toDegrees(Math.atan2(y, x));

				float radius = ecRecData.getRadius(ecRecData.energy.get(i));
				if (radius > 0) {
					container.localToWorld(pp, wp);
					wr.setRect(wp.x - radius, wp.y - radius, 2 * radius, 2 * radius);
					WorldGraphicsUtilities.drawWorldOval(g, container, wr, CedColors.RECCalFill, null);
				}

				_fbData.add(new FBData(pp, String.format("$magenta$REC xyz (%-6.3f, %-6.3f, %-6.3f) cm", x, y, z),
						String.format("$magenta$REC %s (%-6.3f, %-6.3f, %-6.3f)", CedView.rThetaPhi, r, theta, phi),
						String.format("$magenta$REC plane %s", ECGeometry.PLANE_NAMES[ecRecData.plane.get(i)]),
						String.format("$magenta$REC view %s", ECGeometry.VIEW_NAMES[ecRecData.view.get(i)]),
						String.format("$magenta$%s", ecRecData.getPIDStr(i)),
						String.format("$magenta$REC Energy %-7.4f GeV", ecRecData.energy.get(i))));

			}
		} // for i

		// draw PCAL
		for (int i = 0; i < pcalRecData.count(); i++) {
			if (_view.containsSector(pcalRecData.sector.get(i))) {

				float x = pcalRecData.x.get(i);
				float y = pcalRecData.y.get(i);
				float z = pcalRecData.z.get(i);

				_view.projectClasToWorld(x, y, z, _view.getProjectionPlane(), wp);
				container.worldToLocal(pp, wp);
				DataDrawSupport.drawECALRec(g, pp, false);

				double r = Math.sqrt(x * x + y * y + z * z);
				double theta = Math.toDegrees(Math.acos(z / r));
				double phi = Math.toDegrees(Math.atan2(y, x));

				float radius = pcalRecData.getRadius(pcalRecData.energy.get(i));
				if (radius > 0) {
					container.localToWorld(pp, wp);
					wr.setRect(wp.x - radius, wp.y - radius, 2 * radius, 2 * radius);
					WorldGraphicsUtilities.drawWorldOval(g, container, wr, CedColors.RECCalFill, null);
				}

				_fbData.add(new FBData(pp, String.format("$magenta$REC xyz (%-6.3f, %-6.3f, %-6.3f) cm", x, y, z),
						String.format("$magenta$REC %s (%-6.3f, %-6.3f, %-6.3f)", CedView.rThetaPhi, r, theta, phi),
						String.format("$magenta$REC view %s", ECGeometry.VIEW_NAMES[pcalRecData.view.get(i)]),
						String.format("$magenta$%s", pcalRecData.getPIDStr(i)),
						String.format("$magenta$REC Energy %-7.4f GeV", pcalRecData.energy.get(i))));

			}
		} // for i

	}

	// draw reconstructed clusters
	private void drawClusters(Graphics g, IContainer container) {
		drawCalClusters(g, container);
	}

	// draw calorimeter clusters
	private void drawCalClusters(Graphics g, IContainer container) {

		Point2D.Double wp = new Point2D.Double();
		Point pp = new Point();

		// ECal
		ECalClusterData ecClusterData = ECalClusterData.getInstance();
		for (int i = 0; i < ecClusterData.count(); i++) {
			if (_view.containsSector(ecClusterData.sector.get(i))) {

				_view.projectClasToWorld(ecClusterData.x.get(i), ecClusterData.y.get(i), ecClusterData.z.get(i),
						_view.getProjectionPlane(), wp);
				container.worldToLocal(pp, wp);
				ecClusterData.setLocation(i, pp);
				DataDrawSupport.drawCluster(g, pp);
			}
		} // for i

		// PCal
		PCalClusterData pcalClusterData = PCalClusterData.getInstance();
		for (int i = 0; i < pcalClusterData.count(); i++) {
			if (_view.containsSector(pcalClusterData.sector.get(i))) {

				_view.projectClasToWorld(pcalClusterData.x.get(i), pcalClusterData.y.get(i), pcalClusterData.z.get(i),
						_view.getProjectionPlane(), wp);
				container.worldToLocal(pp, wp);
				pcalClusterData.setLocation(i, pp);
				DataDrawSupport.drawCluster(g, pp);
			}
		} // for i
	}

	// draw reconstructed DC hit Hit based and time based based hits
	private void drawDCReconAndDOCA(Graphics g, IContainer container) {
		if (_view.showDCHBHits()) {
			drawDCHitList(g, container, CedColors.HB_COLOR, _hbData, false);
		}
		if (_view.showDCTBHits()) {
			drawDCHitList(g, container, CedColors.TB_COLOR, _tbData, true);
		}
		if (_view.showAIDCHBHits()) {
			drawDCHitList(g, container, CedColors.AIHB_COLOR, _hbAIData, false);
		}
		if (_view.showAIDCTBHits()) {
			drawDCHitList(g, container, CedColors.AITB_COLOR, _tbAIData, true);
		}
	}

	// feedback for calorimeter clusters
	private boolean calClusterFeedback(IContainer container, Point screenPoint, Point2D.Double worldPoint,
			List<String> feedbackStrings) {
		if (_view.showClusters()) {

			// ECal
			ECalClusterData ecClusterData = ECalClusterData.getInstance();
			for (int i = 0; i < ecClusterData.count(); i++) {
				if (_view.containsSector(ecClusterData.sector.get(i))) {
					if (ecClusterData.contains(i, screenPoint)) {

						float x = ecClusterData.x.get(i);
						float y = ecClusterData.y.get(i);
						float z = ecClusterData.z.get(i);

						feedbackStrings
								.add(String.format("$magenta$EC cluster xyz (%-6.3f, %-6.3f, %-6.3f) cm", x, y, z));
						feedbackStrings.add(String.format("$magenta$EC cluster plane %s",
								ECGeometry.PLANE_NAMES[ecClusterData.plane.get(i)]));
						feedbackStrings.add(String.format("$magenta$EC cluster view %s",
								ECGeometry.VIEW_NAMES[ecClusterData.view.get(i)]));
						feedbackStrings.add(
								String.format("$magenta$EC cluster Energy %-7.4f GeV", ecClusterData.energy.get(i)));

						return true;
					}
				}
			} // for i

			// PCal
			PCalClusterData pcalClusterData = PCalClusterData.getInstance();
			for (int i = 0; i < pcalClusterData.count(); i++) {
				if (_view.containsSector(pcalClusterData.sector.get(i))) {
					if (pcalClusterData.contains(i, screenPoint)) {

						float x = pcalClusterData.x.get(i);
						float y = pcalClusterData.y.get(i);
						float z = pcalClusterData.z.get(i);

						feedbackStrings
								.add(String.format("$magenta$PCAL cluster xyz (%-6.3f, %-6.3f, %-6.3f) cm", x, y, z));
						feedbackStrings.add(String.format("$magenta$PCAL cluster view %s",
								ECGeometry.VIEW_NAMES[pcalClusterData.view.get(i)]));
						feedbackStrings.add(String.format("$magenta$PCAL cluster Energy %-6.3f GeV",
								pcalClusterData.energy.get(i)));

						return true;
					}
				}
			} // for i

		}

		return false;
	}

	/**
	 * Use what was drawn to generate feedback strings
	 *
	 * @param container       the drawing container
	 * @param screenPoint     the mouse location
	 * @param worldPoint      the corresponding world location
	 * @param feedbackStrings add strings to this collection
	 * @param option          0 for hit based, 1 for time based
	 */
	@Override
	public void vdrawFeedback(IContainer container, Point screenPoint, Point2D.Double worldPoint,
			List<String> feedbackStrings, int option) {

		if (calClusterFeedback(container, screenPoint, worldPoint, feedbackStrings)) {
			return;
		}

		if (_view.showReconHits()) {
		}

		// data from REC::Calorimeter
		if (_view.showRecCal()) {
			for (FBData fbdata : _fbData) {
				boolean added = fbdata.addFeedback(screenPoint, feedbackStrings);
				if (added) {
					break;
				}
			}
		}

		// DC HB Recon Hits
		if (_view.showDCHBHits()) {

			for (int i = 0; i < _hbData.count(); i++) {
				if (_view.containsSector(_hbData.sector[i])) {
					if (_hbData.contains(i, screenPoint)) {
						_hbData.feedback(i, feedbackStrings);
						break;
					}
				}
			}
		} // show hb hits

		// DC TB Recon Hits
		if (_view.showDCTBHits()) {

			for (int i = 0; i < _tbData.count(); i++) {
				if (_view.containsSector(_tbData.sector[i])) {
					if (_tbData.contains(i, screenPoint)) {
						_tbData.feedback(i, feedbackStrings);
						break;
					}
				}
			}

		} // show tb hits

		// AI DC HB Recon Hits
		if (_view.showAIDCHBHits()) {
			for (int i = 0; i < _hbAIData.count(); i++) {
				if (_view.containsSector(_hbAIData.sector[i])) {
					if (_hbAIData.contains(i, screenPoint)) {
						_hbAIData.feedback(i, feedbackStrings);
						break;
					}
				}
			}
		} // show AI hb hits

		// AI DC TB Recon Hits
		if (_view.showAIDCTBHits()) {
			for (int i = 0; i < _tbAIData.count(); i++) {
				if (_view.containsSector(_tbAIData.sector[i])) {
					if (_tbAIData.contains(i, screenPoint)) {
						_tbAIData.feedback(i, feedbackStrings);
						break;
					}
				}
			}
		} // show AI tb hits

	}

	// draw a reconstructed hit list
	private void drawDCHitList(Graphics g, IContainer container, Color fillColor, ATrkgHitData hits,
			boolean isTimeBased) {
		if (hits == null) {
			return;
		}

		for (int i = 0; i < hits.count(); i++) {
			if (_view.containsSector(hits.sector[i])) {
				_view.drawDCReconHit(g, container, fillColor, Color.black, hits, i, isTimeBased);
			}
		}

	}

}
