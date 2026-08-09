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
import cnuphys.ced.alldata.ECalClusters;
import cnuphys.ced.alldata.RecCalorimeter;
import cnuphys.ced.alldata.DCHits;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.frame.CedColors;
import cnuphys.ced.geometry.ECGeometry;

public class ReconDrawer extends SectorViewDrawer {

	// cached for feedback
	private ArrayList<FBData> _fbData = new ArrayList<>();

	// data containers
	RecCalorimeter ecRecData = RecCalorimeter.getInstance();
	RecCalorimeter pcalRecData = RecCalorimeter.getInstance();
	private DCHits _hbData = DCHits.hitBased();
	private DCHits _tbData = DCHits.timeBased();
	private DCHits _hbAIData = DCHits.aiHitBased();
	private DCHits _tbAIData = DCHits.aiTimeBased();

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
			if (ecRecData.isECal(i) && _view.containsSector(ecRecData.sector(i))) {

				float x = ecRecData.x(i);
				float y = ecRecData.y(i);
				float z = ecRecData.z(i);

				_view.projectClasToWorld(x, y, z, _view.getProjectionPlane(), wp);
				container.worldToLocal(pp, wp);
				DataDrawSupport.drawECALRec(g, pp, false);

				double r = Math.sqrt(x * x + y * y + z * z);
				double theta = Math.toDegrees(Math.acos(z / r));
				double phi = Math.toDegrees(Math.atan2(y, x));

				float radius = ecRecData.radius(i);
				if (radius > 0) {
					container.localToWorld(pp, wp);
					wr.setRect(wp.x - radius, wp.y - radius, 2 * radius, 2 * radius);
					WorldGraphicsUtilities.drawWorldOval(g, container, wr, CedColors.RECCalFill, null);
				}

				_fbData.add(new FBData(pp, String.format("$magenta$REC xyz (%-6.3f, %-6.3f, %-6.3f) cm", x, y, z),
						String.format("$magenta$REC %s (%-6.3f, %-6.3f, %-6.3f)", CedView.rThetaPhi, r, theta, phi),
						String.format("$magenta$REC plane %s", ECGeometry.PLANE_NAMES[ecRecData.plane(i)]),
						String.format("$magenta$REC view %s", ECGeometry.VIEW_NAMES[ecRecData.view(i)]),
						String.format("$magenta$%s", ecRecData.pidString(i)),
						String.format("$magenta$REC Energy %-7.4f GeV", ecRecData.energy(i))));

			}
		} // for i

		// draw PCAL
		for (int i = 0; i < pcalRecData.count(); i++) {
			if (pcalRecData.isPCal(i) && _view.containsSector(pcalRecData.sector(i))) {

				float x = pcalRecData.x(i);
				float y = pcalRecData.y(i);
				float z = pcalRecData.z(i);

				_view.projectClasToWorld(x, y, z, _view.getProjectionPlane(), wp);
				container.worldToLocal(pp, wp);
				DataDrawSupport.drawECALRec(g, pp, false);

				double r = Math.sqrt(x * x + y * y + z * z);
				double theta = Math.toDegrees(Math.acos(z / r));
				double phi = Math.toDegrees(Math.atan2(y, x));

				float radius = pcalRecData.radius(i);
				if (radius > 0) {
					container.localToWorld(pp, wp);
					wr.setRect(wp.x - radius, wp.y - radius, 2 * radius, 2 * radius);
					WorldGraphicsUtilities.drawWorldOval(g, container, wr, CedColors.RECCalFill, null);
				}

				_fbData.add(new FBData(pp, String.format("$magenta$REC xyz (%-6.3f, %-6.3f, %-6.3f) cm", x, y, z),
						String.format("$magenta$REC %s (%-6.3f, %-6.3f, %-6.3f)", CedView.rThetaPhi, r, theta, phi),
						String.format("$magenta$REC view %s", ECGeometry.VIEW_NAMES[pcalRecData.view(i)]),
						String.format("$magenta$%s", pcalRecData.pidString(i)),
						String.format("$magenta$REC Energy %-7.4f GeV", pcalRecData.energy(i))));

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

		ECalClusters clusters = ECalClusters.getInstance();
		for (int i = 0; i < clusters.count(); i++) {
			if ((clusters.isPCal(i) || clusters.isECal(i)) && _view.containsSector(clusters.sector(i))) {
				_view.projectClasToWorld(clusters.x(i), clusters.y(i), clusters.z(i),
						_view.getProjectionPlane(), wp);
				container.worldToLocal(pp, wp);
				clusters.setLocation(i, pp);
				DataDrawSupport.drawCluster(g, pp);
			}
		}
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

			ECalClusters clusters = ECalClusters.getInstance();
			for (int i = 0; i < clusters.count(); i++) {
				if ((clusters.isPCal(i) || clusters.isECal(i)) && _view.containsSector(clusters.sector(i))
						&& clusters.contains(i, screenPoint)) {
					clusters.addFeedback(i, feedbackStrings);
					return true;
				}
			}

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
				if (_view.containsSector(_hbData.sector(i))) {
					if (_hbData.contains(i, screenPoint)) {
						_hbData.addFeedback(i, feedbackStrings);
						break;
					}
				}
			}
		} // show hb hits

		// DC TB Recon Hits
		if (_view.showDCTBHits()) {

			for (int i = 0; i < _tbData.count(); i++) {
				if (_view.containsSector(_tbData.sector(i))) {
					if (_tbData.contains(i, screenPoint)) {
						_tbData.addFeedback(i, feedbackStrings);
						break;
					}
				}
			}

		} // show tb hits

		// AI DC HB Recon Hits
		if (_view.showAIDCHBHits()) {
			for (int i = 0; i < _hbAIData.count(); i++) {
				if (_view.containsSector(_hbAIData.sector(i))) {
					if (_hbAIData.contains(i, screenPoint)) {
						_hbAIData.addFeedback(i, feedbackStrings);
						break;
					}
				}
			}
		} // show AI hb hits

		// AI DC TB Recon Hits
		if (_view.showAIDCTBHits()) {
			for (int i = 0; i < _tbAIData.count(); i++) {
				if (_view.containsSector(_tbAIData.sector(i))) {
					if (_tbAIData.contains(i, screenPoint)) {
						_tbAIData.addFeedback(i, feedbackStrings);
						break;
					}
				}
			}
		} // show AI tb hits

	}

	// draw a reconstructed hit list
	private void drawDCHitList(Graphics g, IContainer container, Color fillColor, DCHits hits,
			boolean isTimeBased) {
		if (hits == null) {
			return;
		}

		for (int i = 0; i < hits.count(); i++) {
			if (_view.containsSector(hits.sector(i))) {
				_view.drawDCReconHit(g, container, fillColor, Color.black, hits, i, isTimeBased);
			}
		}

	}

}
