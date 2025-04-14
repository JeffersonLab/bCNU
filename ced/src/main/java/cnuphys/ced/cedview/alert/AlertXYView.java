package cnuphys.ced.cedview.alert;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.List;

import org.jlab.geom.prim.Plane3D;
import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.component.rangeslider.RangeSlider;
import cnuphys.bCNU.drawable.DrawableAdapter;
import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.item.YouAreHereItem;
import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.util.UnicodeSupport;
import cnuphys.bCNU.view.BaseView;
import cnuphys.ced.alldata.datacontainer.tof.CTOFADCData;
import cnuphys.ced.alldata.datacontainer.tof.CTOFClusterData;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.cedview.CedXYView;
import cnuphys.ced.cedview.ILabCoordinates;
import cnuphys.ced.cedview.central.CNDXYPolygon;
import cnuphys.ced.cedview.central.CTOFXYPolygon;
import cnuphys.ced.cedview.central.CentralXYHitDrawer;
import cnuphys.ced.cedview.central.ClusterDrawerXY;
import cnuphys.ced.cedview.central.ICentralXYView;
import cnuphys.ced.cedview.central.SwimTrajectoryDrawer;
import cnuphys.ced.cedview.urwell.HighlightData;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.component.ControlPanel;
import cnuphys.ced.component.DisplayBits;
import cnuphys.ced.geometry.BSTxyPanel;
import cnuphys.ced.geometry.CNDGeometry;
import cnuphys.ced.geometry.CTOFGeometry;
import cnuphys.ced.geometry.GeometryManager;
import cnuphys.ced.geometry.alert.AlertGeometry;
import cnuphys.ced.geometry.alert.DCLayer;
import cnuphys.ced.geometry.alert.TOFLayer;
import cnuphys.ced.geometry.bmt.BMTSectorItem;
import cnuphys.swim.SwimTrajectory2D;

public class AlertXYView extends CedXYView implements ILabCoordinates, ICentralXYView {


	// camera Z for projection
//	private double _zcamera = 450;

	// for naming clones
	private static int CLONE_COUNT = 0;

	// base title
	private static final String _baseTitle = "ALERT XY";

	// units are mm
//	private static Rectangle2D.Double _defaultWorldRectangle = new Rectangle2D.Double(-120, -120, 240, 240);
	private static Rectangle2D.Double _defaultWorldRectangle = new Rectangle2D.Double(400, -400, -800, 800);


	//for highlighting
	private HighlightData _highlightDataAHDC = new HighlightData();
	private HighlightData _highlightDataATOF = new HighlightData();
	
	//for coloring central tof
	private static Color _ctofColors[] = { new Color(240, 240, 240), new Color(224, 224, 224) };

	// the CTOF polygons
	private CTOFXYPolygon _ctofPoly[] = new CTOFXYPolygon[48];

	// the CND xy polygons
	private CNDXYPolygon _cndPoly[][] = new CNDXYPolygon[3][48];

	// bank matches
	private static String _defMatches[] = { "AHDC", "ATOF", "CND", "CTOF" };

	// used to draw swum trajectories (if any) in the after drawer
	private SwimTrajectoryDrawer _swimTrajectoryDrawer;

	//draw hits in the DC
	private AlertDCHitDrawer _dcHitDrawer;

	//draw hits in the TOF
	private AlertTOFHitDrawer _tofHitDrawer;

	//wire projection
	private AlertProjectionPanel _dcPanel;
	
	//max  adc in this event
//	private int _maxADCThisEvent = -1;
	
	// data containers
	private CTOFADCData _ctofADCData = CTOFADCData.getInstance();
	private CTOFClusterData _clusterCTOFData = CTOFClusterData.getInstance();
	
	// draws hits
	private CentralXYHitDrawer _hitDrawer;

	// draws reconstructed clusters
	private ClusterDrawerXY _clusterDrawer;


	/**
	 * Create a Alert detector XY View
	 *
	 * @param keyVals
	 */
	private AlertXYView(Object... keyVals) {
		super(keyVals);
		// draws any swum trajectories (in the after draw)
		_swimTrajectoryDrawer = new SwimTrajectoryDrawer(this);
		_swimTrajectoryDrawer.setMaxPathLength(getTrajMaxPathlength());
		_dcHitDrawer = new AlertDCHitDrawer(this);
		_tofHitDrawer = new AlertTOFHitDrawer(this);
		
		// add the CND polys
		for (int layer = 1; layer <= 3; layer++) {
			for (int paddleId = 1; paddleId <= 48; paddleId++) {
				_cndPoly[layer - 1][paddleId - 1] = new CNDXYPolygon(layer, paddleId);
			}
		}

		
		// add the ctof polygons
		for (int paddleId = 1; paddleId <= 48; paddleId++) {
			_ctofPoly[paddleId - 1] = new CTOFXYPolygon(paddleId);
		}

		_clusterDrawer = new ClusterDrawerXY(this);
		_hitDrawer = new CentralXYHitDrawer(this, this);

		
	}

	/**
	 * Create a Alert detector XY view
	 * @return a Alert detector XY view
	 */
	public static AlertXYView createAlertXYView() {
		// set to a fraction of screen
		Dimension d = GraphicsUtilities.screenFraction(0.48);

		// make it square
		int width = d.width;
		int height = width-100;

		String title = _baseTitle + ((CLONE_COUNT == 0) ? "" : ("_(" + CLONE_COUNT + ")"));

		final AlertXYView view = new AlertXYView(PropertySupport.WORLDSYSTEM, _defaultWorldRectangle,
				PropertySupport.WIDTH, width,
				PropertySupport.HEIGHT, height,
				PropertySupport.LEFTMARGIN, LMARGIN,
				PropertySupport.TOPMARGIN, TMARGIN,
				PropertySupport.RIGHTMARGIN, RMARGIN,
				PropertySupport.BOTTOMMARGIN, BMARGIN,
				PropertySupport.TOOLBAR, true,
				PropertySupport.TOOLBARBITS, CedView.TOOLBARBITS,
				PropertySupport.VISIBLE, true,
				PropertySupport.TITLE, title,
				PropertySupport.PROPNAME, "AlertXY",
				PropertySupport.STANDARDVIEWDECORATIONS, true);

		view._controlPanel = new ControlPanel(view,
				ControlPanel.DISPLAYARRAY + ControlPanel.FEEDBACK+ ControlPanel.ACCUMULATIONLEGEND
						+ ControlPanel.MATCHINGBANKSPANEL + ControlPanel.ALERTDC + ControlPanel.MINADCCUTOFF,
						DisplayBits.ACCUMULATION + DisplayBits.CROSSES + DisplayBits.CLUSTERS + DisplayBits.RECONHITS
						+ DisplayBits.CVTRECTRACKS +  DisplayBits.MCTRUTH
						+ DisplayBits.CVTRECTRAJ + DisplayBits.CVTRECKFTRAJ + DisplayBits.ADCDATA +
						+ DisplayBits.CVTP1TRACKS + DisplayBits.CVTP1TRAJ, 3, 5);
		view.add(view._controlPanel, BorderLayout.EAST);
		view.pack();

		// i.e. if none were in the properties
		if (view.hasNoBankMatches()) {
			view.setBankMatches(_defMatches);
		}

		view._controlPanel.getMatchedBankPanel().update();
		
//		RangeSlider trajRangeSlider = view._controlPanel.getTrajRangeSlider();
//		trajRangeSlider.setOnChange(value -> view.trajRangeChanging(value));
		
		RangeSlider minADCSlider = view._controlPanel.getMinADCRangeSlider();
		minADCSlider.setOnChange(value -> view.minADCChanging(value));

		//add dc projection panel
		view._dcPanel = view._controlPanel.getAlertDCPanel();
		
		// add quick zooms
		view.addQuickZoom("ALERT", 120, -120, -120, 120);


		return view;
	}
	
	/**
	 * Get the minimum ADC cutoff
	 * @return the minimum ADC cutoff
	 */
	public int getADCThreshold() {
		return _controlPanel.getMinADCRangeSlider().getValue();
	}
	

	//respond to the traj range change
	private void trajRangeChanging(int currentVal) {
		_swimTrajectoryDrawer.setMaxPathLength(currentVal);
		refresh();
	}
	
	//respond to the min adc cutoffs
	private void minADCChanging(int currentVal) {
		refresh();
	}

	@Override
	protected void setBeforeDraw() {
		IDrawable beforeDraw = new DrawableAdapter() {

			@Override
			public void draw(Graphics g, IContainer container) {

				if (!_eventManager.isAccumulating()) {

					Graphics2D g2 = (Graphics2D) g;

					// CND Polys
					for (int layer = 1; layer <= 3; layer++) {
						for (int paddleId = 1; paddleId <= 48; paddleId++) {
							if (_cndPoly[layer - 1][paddleId - 1] != null) {
								_cndPoly[layer - 1][paddleId - 1].draw(g2, container);
							}
						}

					}

					// CTOF Polys
					for (int paddleId = 1; paddleId <= 48; paddleId++) {
						if (_ctofPoly[paddleId - 1] != null) {
							_ctofPoly[paddleId - 1].draw(g2, container, paddleId, _ctofColors[paddleId % 2]);
						}
					}

					drawWires(g, container);
					drawATOFPaddles(g, container);

					AlertGeometry.drawAlertTOFSectorNumbers(g, container);
				}
			}

		};

		getContainer().setBeforeDraw(beforeDraw);
	}

	@Override
	protected void setAfterDraw() {
		final AlertXYView view = this;

		IDrawable afterDraw = new DrawableAdapter() {

			@Override
			public void draw(Graphics g, IContainer container) {

				if (!_eventManager.isAccumulating()) {


					_hitDrawer.draw(g, container);

					if (view.isSingleEventMode()) {
						drawSingleModeHits(g, container);
						if (showClusters()) {
							_clusterDrawer.draw(g, container);
						}
					}

					else {
						drawAccumulatedHits(g, container);
					}

					// data selected highlight?
					drawDataSelectedHighlight(g, container);

				}

				Rectangle screenRect = getActiveScreenRectangle(container);
				drawAxes(g, container, screenRect, false);

			}

		};

		getContainer().setAfterDraw(afterDraw);
	}
	

	//draw data selected highlighted data
	private void drawDataSelectedHighlight(Graphics g, IContainer container) {

		DataEvent dataEvent = ClasIoEventManager.getInstance().getCurrentEvent();
		if (dataEvent == null) {
			return;
		}

		//adc data
		if (this.showADCHits()) {
			if (dataEvent.hasBank("AHDC::adc") && (_highlightDataAHDC.hit >= 0)) {
				_dcHitDrawer.drawHighlightHit(g, container, dataEvent, _highlightDataAHDC.hit);
			}

			if (dataEvent.hasBank("ATOF::tdc") && (_highlightDataATOF.hit >= 0)) {
				_tofHitDrawer.drawHighlightHit(g, container, dataEvent, _highlightDataATOF.hit);
			}
		}
	}	//indices are zero based


	//draw the hits in single eventmode
	private void drawSingleModeHits(Graphics g, IContainer container) {
		_dcHitDrawer.drawHits(g, container);
		_tofHitDrawer.drawHits(g, container);

		//overlay trajectories
		_swimTrajectoryDrawer.draw(g, container);

	}

	//draw the accumulated hits
	private void drawAccumulatedHits(Graphics g, IContainer container) {
		_dcHitDrawer.drawAccumulatedHits(g, container);
		_tofHitDrawer.drawAccumulatedHits(g, container);

	}

	//set the projection plane
	public void setProjectionPlane(double z) {
		Plane3D plane = GeometryManager.constantZPlane(z);
		this.projectionPlane = plane;
	}


	@Override
	protected void addItems() {
	}


	//draw the dc wires
	private void drawWires(Graphics g, IContainer container) {

		Collection<DCLayer> dcLayers = AlertGeometry.getAllDCLayers();

		for (DCLayer dcl : dcLayers) {
			if (dcl.numWires >  0) {
				dcl.drawXYWires(g, container, getFixedZ());
			}
		}
	}


	//draw atof
	private void drawATOFPaddles(Graphics g, IContainer container) {
		Collection<TOFLayer> tofLayers = AlertGeometry.getAllTOFLayers();

		for (TOFLayer tof : tofLayers) {
			if (tof.numPaddles >  0) {
				tof.drawAllATOFPaddles(g, container);
			}

		}

	}


	/**
	 * Convert lab coordinates (CLAS x,y,z) to world coordinates (2D world system of
	 * the view). Used by the swim drawer.
	 *
	 * @param x  the CLAS12 x coordinate
	 * @param y  the CLAS12 y coordinate
	 * @param z  the CLAS12 z coordinate
	 * @param wp holds the world point
	 */
	@Override
	public void labToWorld(double x, double y, double z, Point2D.Double wp) {
		wp.x = x;
		wp.y = y;

		//do the projection
//		double zp = getFixedZ();
//		double scale = (_zcamera - zp) / _zcamera;
//		wp.x = x*scale;
//		wp.y = y*scale;
	}

	/**
     * Show all the TOF or just the intersecting TOF
     * @return true if all TOF should be shown
     */
	public boolean showAllTOF() {
		return _dcPanel.showAllTOF();
	}


	/**
	 * Clone the view.
	 *
	 * @return the cloned view
	 */
	@Override
	public BaseView cloneView() {
		super.cloneView();
		CLONE_COUNT++;

		// limit
		if (CLONE_COUNT > 2) {
			return null;
		}

		Rectangle vr = getBounds();
		vr.x += 40;
		vr.y += 40;

		AlertXYView view = createAlertXYView();
		view.setBounds(vr);
		return view;

	}

	/**
	 * Get the fixed Z value if we are not using midpoints
	 *
	 * @return the fixed Z value.
	 */
	public double getFixedZ() {
		return _dcPanel.getFixedZ();
	}


	/**
	 * Some view specific feedback. Should always call super.getFeedbackStrings
	 * first.
	 *
	 * @param container   the base container for the view.
	 * @param pp the pixel point
	 * @param wp  the corresponding world location.
	 */
	@Override
	public void getFeedbackStrings(IContainer container, Point pp, Point2D.Double wp,
			List<String> feedbackStrings) {

		basicFeedback(container, pp, wp, "mm", feedbackStrings);

		double z = getFixedZ();
		double r = Math.sqrt(wp.x * wp.x + wp.y * wp.y + z * z);
		if (r > 0) {
			double theta = Math.toDegrees(Math.acos(z / r));
			double phi = Math.toDegrees(Math.atan2(wp.y, wp.x));

			String fbs = String.format("(z, %s, %s ) = (%-6.2f mm, %-6.2f, %-6.2f)", UnicodeSupport.SMALL_THETA,
					UnicodeSupport.SMALL_PHI, z, theta, phi);
			feedbackStrings.add(fbs);
		} else {
			feedbackStrings.add("z: " + getFixedZ() + " mm");
		}

		Collection<DCLayer> dcLayers = AlertGeometry.getAllDCLayers();

		// anchor (urhere) feedback?
		YouAreHereItem item = getContainer().getYouAreHereItem();
		if (item != null) {
			Point2D.Double anchor = item.getFocus();
			String dstr = String.format("$khaki$Dist from ref. point: %5.2f mm", anchor.distance(wp));
			feedbackStrings.add(dstr);
		}

		for (DCLayer dcl : dcLayers) {
			if (dcl.containsXY(pp)) {
				dcl.feedbackXYString(pp, wp, feedbackStrings);
				_dcHitDrawer.getHitFeedbackStrings(container, pp, wp, dcl, feedbackStrings);
				return;
			}
		}

		Collection<TOFLayer> tofLayers = AlertGeometry.getAllTOFLayers();

		for (TOFLayer tof : tofLayers) {
			if (tof.feedbackXYString(pp, wp, feedbackStrings)) {
				_tofHitDrawer.getHitFeedbackStrings(container, pp, wp, tof, feedbackStrings);
				return;
			}

		}
		
		double rad = Math.hypot(wp.x, wp.y);
		boolean found = false;

		// cnd ?
		if ((rad > 288) && (rad < 382)) {

			for (int layer = 1; layer <= 3; layer++) {
				for (int paddleId = 1; paddleId <= 48; paddleId++) {

					found = _cndPoly[layer - 1][paddleId - 1].getFeedbackStrings(container, pp, wp,
							feedbackStrings);

					if (found) {
						break;
					}
				}

				if (found) {
					break;
				}

			}
		}

		//ctof?
		else if ((rad > CTOFGeometry.RINNER) && (rad < CTOFGeometry.ROUTER)) {

			for (short index = 0; index < 48; index++) {
				if (_ctofPoly[index].contains(pp)) {
					short paddle = (short) (index + 1); // now 1-based
					feedbackStrings.add("$cyan$CTOF paddle: " + paddle);

					for (int i = 0; i < _ctofADCData.count(); i++) {
						if (_ctofADCData.component[i] == paddle) {
							_ctofADCData.adcFeedback("CTOF", i, feedbackStrings);
						}
					}

					break;
				}
			}

			if (showClusters()) {
				for (int i = 0; i < _clusterCTOFData.count(); i++) {
					if (_clusterCTOFData.contains(i, pp)) {
						_clusterCTOFData.feedback("CTOF", i, feedbackStrings);
						break;
					}
				}
			}
		}

		// near a swum trajectory?
		double mindist = _swimTrajectoryDrawer.closestApproach(wp);
		double pixlen = WorldGraphicsUtilities.getMeanPixelDensity(container) * mindist;

		_lastTrajStr = null; // for hovering response
		if (pixlen < 25.0) {
			SwimTrajectory2D traj2D = _swimTrajectoryDrawer.getClosestTrajectory();
			if (traj2D != null) {
				traj2D.addToFeedback(feedbackStrings);
				_lastTrajStr = traj2D.summaryString();
			}
		}

		// cluster feedback
		if (showClusters()) {
			_clusterDrawer.feedback(container, pp, wp, feedbackStrings);
		}

		// hit feedback
		_hitDrawer.feedback(container, pp, wp, feedbackStrings);


	}

	/**
	 * In the BankDataTable a row was selected.
	 * @param bankName the name of the bank
	 * @param index the 0-based index into the bank
	 */
	@Override
	public void dataSelected(String bankName, int index) {
		if (bankName.equals("AHDC::adc")) {
			_highlightDataAHDC.hit = index;
		} else if (bankName.equals("ATOF::tdc")) {
			_highlightDataATOF.hit = index;
		}

		refresh();
	}

	/**
	 * Get the maximum path length for drawn trajectories
	 *
	 * @return the maximum path length for trajectories
	 */
	@Override
	public int getTrajMaxPathlength() {
		return 4000; // mm
	}

	/**
	 * Get a CTOF scintillator polygon
	 *
	 * @param index1 the 1=based index [1..48]
	 * @return the most recently drawn polygon
	 */
	@Override
	public CTOFXYPolygon getCTOFPolygon(int index1) {
		int index0 = index1 - 1;
		if ((index0 < 0) || (index0 > 47)) {
			return null;
		}
		return _ctofPoly[index0];
	}

	/**
	 * Get the CND polygon from Gagik's geometry layer and paddle
	 *
	 * @param layer    1..3
	 * @param paddleId 1..48
	 * @return the CND polygon
	 */
	@Override
	public CNDXYPolygon getCNDPolygon(int layer, int paddleId) {
		if ((layer < 1) || (layer > 3) || (paddleId < 1) || (paddleId > 48)) {
			return null;
		}

		return _cndPoly[layer - 1][paddleId - 1];
	}

	/**
	 * Get the CND polygon from "real" numbering
	 *
	 * @param sector    1..24
	 * @param layer     1..3
	 * @param component 1..2
	 * @return the CND polygon
	 */
	@Override
	public CNDXYPolygon getCNDPolygon(int sector, int layer, int component) {
		if ((sector < 1) || (sector > 24) || (layer < 1) || (layer > 3)) {
			return null;
		}
		if ((component < 1) || (component > 2)) {
			return null;
		}

		int real[] = { sector, layer, component };
		int geo[] = new int[3];

		CNDGeometry.realTripletToGeoTriplet(geo, real);

		return getCNDPolygon(geo[1], geo[2]);
	}
	@Override
	public void drawBSTPanel(Graphics2D g2, IContainer container, BSTxyPanel panel, Color color) {
	}

	@Override
	public BMTSectorItem getBMTSectorItem(int sector, int layer) {
		return null;
	}


}
