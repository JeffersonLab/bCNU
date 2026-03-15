package cnuphys.ced.cedview.sectorview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cnuphys.bCNU.drawable.DrawableAdapter;
import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.format.DoubleFormat;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.item.ItemList;
import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.util.UnicodeSupport;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.bCNU.view.BaseView;
import cnuphys.bCNU.view.PlotView;
import cnuphys.bCNU.view.ViewManager;
import cnuphys.ced.alldata.datacontainer.dc.ATrkgHitData;
import cnuphys.ced.alldata.datacontainer.dc.DCTDCandDOCAData;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.cedview.SliceView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.common.CrossDrawer;
import cnuphys.ced.common.FMTCrossDrawer;
import cnuphys.ced.common.SuperLayerDrawing;
import cnuphys.ced.component.ControlPanel;
import cnuphys.ced.component.DisplayBits;
import cnuphys.ced.frame.Ced;
import cnuphys.ced.geometry.GeometryManager;
import cnuphys.ced.geometry.ftof.FTOFGeometry;
import cnuphys.ced.geometry.ftof.FTOFPanel;
import cnuphys.ced.item.BeamLineItem;
import cnuphys.ced.item.FTOFPanelItem;
import cnuphys.ced.item.MagFieldItem;
import cnuphys.magfield.FieldProbe;
import cnuphys.magfield.MagneticFields;
import cnuphys.splot.fit.FitType;
import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.pdata.DataSetException;
import cnuphys.splot.pdata.DataSetType;
import cnuphys.splot.plot.PlotCanvas;
import cnuphys.swim.SwimTrajectory;
import cnuphys.swim.SwimTrajectory2D;

/**
 * This is the classic sector view.
 *
 * @author heddle
 *
 */
@SuppressWarnings("serial")
public class SectorView extends SliceView implements ChangeListener {


	// HTCC Items, 8 per sector, not geometrically realistic
	private SectorHTCCItem _htcc[][] = new SectorHTCCItem[4][2];

	// LTCC Items, 36 per sector, not geometrically realistic
	private SectorLTCCItem _ltcc[][] = new SectorLTCCItem[18][2];

	// superlayer (graphical) items. The first index [0..1] is for upper and
	// lower sectors.
	// the second is for for super layer 0..5debug
	private SectorSuperLayer _superLayers[][] = new SectorSuperLayer[2][6];

	// determines if the wire intersections must be recalculated. This is caused
	// by a change in phi using the phi slider.
	private Boolean _wiresDirty = true;

	// used to draw swum trajectories (if any) in the after drawer
	private SwimTrajectoryDrawer _swimTrajectoryDrawer;

	// drawing reconstructed data
	private ReconDrawer _reconDrawer;

	// reconstructed cross drawer for DC (and feedback handler)
	private CrossDrawer _dcCrossDrawer;

	// for fmt
	private FMTCrossDrawer _fmtCrossDrawer;


	//redraw the segments?
	private boolean segmentsOnTop = true;

	//bank matches
	private static String _defMatches[] = {"DC:", "HitBased", "TimeBased"};


	private static Color plotColors[] = { X11Colors.getX11Color("Dark Red"), X11Colors.getX11Color("Dark Blue"),
			X11Colors.getX11Color("Dark Green"), Color.black, Color.gray, X11Colors.getX11Color("wheat") };

	// data containers
	private static DCTDCandDOCAData _dcData = DCTDCandDOCAData.getInstance();


	/**
	 * Create a sector view
	 *
	 * @param keyVals variable set of arguments.
	 */
	private SectorView(DisplaySectors displaySectors, Object... keyVals) {
		super(displaySectors, keyVals);

		// draws any swum trajectories (in the after draw)
		_swimTrajectoryDrawer = new SwimTrajectoryDrawer(this);

		// dc cross drawer
		_dcCrossDrawer = new CrossDrawer(this);

		// fmt cross drawer
		_fmtCrossDrawer = new FMTCrossDrawer(this);

		// Recon drawer
		_reconDrawer = new ReconDrawer(this);

		addItems();
		setBeforeDraw();
		setAfterDraw();

	}

	/**
	 * Convenience method for creating a Sector View.
	 *
	 * @param displaySectors controls which opposite sectors are displayed.
	 * @return a new SectorView.
	 */
	public static SectorView createSectorView(DisplaySectors displaySectors) {
		SectorView view = null;

		double xo = -450.0; // cm. Think of sector 1. x is "vertical"
		double zo = -10.0; // cm. Think of sector 1. z is "horizontal"
		double wheight = -2 * xo;
		double wwidth = 840;

		double size[] = getSizeFromScreenFraction(0.70);
// adjust the world width and height to match the aspect ratio of the screen fraction size
		size[0] = (wwidth * size[1]) / wheight;
		int width = (int) size[0];
		int height = (int) size[1];


		// give the view a title based on what sectors are displayed
		String title = "Sectors ";
		String propname = "SECT";
		switch (displaySectors) {
		case SECTORS14:
			title += "1 and 4";
			propname += "14";
			break;
		case SECTORS25:
			title += "2 and 5";
			propname += "25";
			break;
		case SECTORS36:
			title += "3 and 6";
			propname += "36";
			break;
		}

		if (CLONE_COUNT[displaySectors.ordinal()] > 0) {
			title += "_(" + CLONE_COUNT[displaySectors.ordinal()] + ")";
		}

		// create the view
		view = new SectorView(displaySectors, PropertySupport.WORLDSYSTEM,
				new Rectangle2D.Double(zo, xo, wwidth, wheight),

				PropertySupport.LEFT, LEFT, PropertySupport.TOP, TOP, PropertySupport.WIDTH, width,
				PropertySupport.HEIGHT, height, PropertySupport.TOOLBAR, true, PropertySupport.TOOLBARBITS,
				CedView.TOOLBARBITS, PropertySupport.VISIBLE, true, PropertySupport.BACKGROUND,
				X11Colors.getX11Color("Alice Blue").darker(),
				PropertySupport.TITLE, title, PropertySupport.PROPNAME, propname, PropertySupport.STANDARDVIEWDECORATIONS, true);

		view._controlPanel = new ControlPanel(view,
				ControlPanel.NOISECONTROL + ControlPanel.DISPLAYARRAY + ControlPanel.PHISLIDER
						+ ControlPanel.FEEDBACK + ControlPanel.FIELDLEGEND
						+ ControlPanel.MATCHINGBANKSPANEL + ControlPanel.ACCUMULATIONLEGEND,
				DisplayBits.MAGFIELD + DisplayBits.CROSSES + DisplayBits.RECONHITS + DisplayBits.CLUSTERS
						+ DisplayBits.FMTCROSSES + DisplayBits.RECPART + DisplayBits.DC_HITS + DisplayBits.SEGMENTS + DisplayBits.GLOBAL_HB +
						+ DisplayBits.GLOBAL_AIHB + DisplayBits.GLOBAL_AITB
						+ DisplayBits.GLOBAL_TB + DisplayBits.ACCUMULATION + DisplayBits.DOCA + DisplayBits.MCTRUTH +
						DisplayBits.SECTORCHANGE + DisplayBits.RECCAL,
				3, 5);

		view.add(view._controlPanel, BorderLayout.EAST);

		view._displaySectors = displaySectors;

		//i.e. if none were in the properties
		if (view.hasNoBankMatches()) {
			view.setBankMatches(_defMatches);
		}
		view._controlPanel.getMatchedBankPanel().update();


		view.pack();

		LEFT += DELTAH;
		TOP += DELTAV;

		return view;
	}

	/**
	 * Add all the items on this view
	 */
	private void addItems() {

		// add a field object, which won't do anything unless we can read in the
		// field.
		ItemList magneticFieldLayer = getContainer().getItemList(_magneticFieldLayerName);
		new MagFieldItem(magneticFieldLayer, this);
		magneticFieldLayer.setVisible(false);

		ItemList detectorLayer = getContainer().getItemList(_detectorLayerName);
		new BeamLineItem(detectorLayer);

		// add the ltcc items
		for (short ring = 1; ring <= 18; ring++) {
			for (byte half = 1; half <= 2; half++) {

				switch (_displaySectors) {
				case SECTORS14:
					_ltcc[ring - 1][half - 1] = new SectorLTCCItem(detectorLayer, this, (byte)1, half, ring);
					_ltcc[ring - 1][half - 1] = new SectorLTCCItem(detectorLayer, this, (byte)4, half, ring);
					break;

				case SECTORS25:
					_ltcc[ring - 1][half - 1] = new SectorLTCCItem(detectorLayer, this, (byte)2, half, ring);
					_ltcc[ring - 1][half - 1] = new SectorLTCCItem(detectorLayer, this, (byte)5, half, ring);
					break;

				case SECTORS36:
					_ltcc[ring - 1][half - 1] = new SectorLTCCItem(detectorLayer, this, (byte)3, half, ring);
					_ltcc[ring - 1][half - 1] = new SectorLTCCItem(detectorLayer, this, (byte)6, half, ring);
					break;
				}

			}
		}

		// add the htcc items
		for (short ring = 1; ring <= 4; ring++) {
			for (byte half = 1; half <= 2; half++) {

				switch (_displaySectors) {
				case SECTORS14:
					_htcc[ring - 1][half - 1] = new SectorHTCCItem(detectorLayer, this, (byte)1, half, ring);
					_htcc[ring - 1][half - 1] = new SectorHTCCItem(detectorLayer, this, (byte)4, half, ring);
					break;

				case SECTORS25:
					_htcc[ring - 1][half - 1] = new SectorHTCCItem(detectorLayer, this, (byte)2, half, ring);
					_htcc[ring - 1][half - 1] = new SectorHTCCItem(detectorLayer, this, (byte)5, half, ring);
					break;

				case SECTORS36:
					_htcc[ring - 1][half - 1] = new SectorHTCCItem(detectorLayer, this, (byte)3, half, ring);
					_htcc[ring - 1][half - 1] = new SectorHTCCItem(detectorLayer, this, (byte)6, half, ring);
					break;
				}

			}
		}

		// add the superlayer items
		for (int superLayer = 0; superLayer < 6; superLayer++) {
			// SectorSuperLayer constructor expects a 1-based index

			switch (_displaySectors) {
			case SECTORS14:
				_superLayers[UPPER_SECTOR][superLayer] = new SectorSuperLayer(detectorLayer, this, 1, superLayer + 1);
				_superLayers[LOWER_SECTOR][superLayer] = new SectorSuperLayer(detectorLayer, this, 4, superLayer + 1);
				break;

			case SECTORS25:
				_superLayers[UPPER_SECTOR][superLayer] = new SectorSuperLayer(detectorLayer, this, 2, superLayer + 1);
				_superLayers[LOWER_SECTOR][superLayer] = new SectorSuperLayer(detectorLayer, this, 5, superLayer + 1);
				break;

			case SECTORS36:
				_superLayers[UPPER_SECTOR][superLayer] = new SectorSuperLayer(detectorLayer, this, 3, superLayer + 1);
				_superLayers[LOWER_SECTOR][superLayer] = new SectorSuperLayer(detectorLayer, this, 6, superLayer + 1);
				break;
			}

			_superLayers[UPPER_SECTOR][superLayer].getStyle().setFillColor(Color.gray);
			_superLayers[LOWER_SECTOR][superLayer].getStyle().setFillColor(Color.gray);
		}

		// add forward time of flight items
		FTOFPanel panels[] = FTOFGeometry.getFtofPanel();
		for (FTOFPanel ftof : panels) {
			switch (_displaySectors) {
			case SECTORS14:
				new FTOFPanelItem(detectorLayer, ftof, 1);
				new FTOFPanelItem(detectorLayer, ftof, 4);
				break;

			case SECTORS25:
				new FTOFPanelItem(detectorLayer, ftof, 2);
				new FTOFPanelItem(detectorLayer, ftof, 5);
				break;

			case SECTORS36:
				new FTOFPanelItem(detectorLayer, ftof, 3);
				new FTOFPanelItem(detectorLayer, ftof, 6);
				break;
			}
		}

		// add EC items
		switch (_displaySectors) {
		case SECTORS14:
			for (int planeIndex = 0; planeIndex < 2; planeIndex++) {
				for (int stripIndex = 0; stripIndex < 3; stripIndex++) {
					new SectorECALItem(detectorLayer, planeIndex, stripIndex, 1);
					new SectorECALItem(detectorLayer, planeIndex, stripIndex, 4);
				}
			}
			break;

		case SECTORS25:
			for (int planeIndex = 0; planeIndex < 2; planeIndex++) {
				for (int stripIndex = 0; stripIndex < 3; stripIndex++) {
					new SectorECALItem(detectorLayer, planeIndex, stripIndex, 2);
					new SectorECALItem(detectorLayer, planeIndex, stripIndex, 5);
				}
			}
			break;

		case SECTORS36:
			for (int planeIndex = 0; planeIndex < 2; planeIndex++) {
				for (int stripIndex = 0; stripIndex < 3; stripIndex++) {
					new SectorECALItem(detectorLayer, planeIndex, stripIndex, 3);
					new SectorECALItem(detectorLayer, planeIndex, stripIndex, 6);
				}
			}
			break;
		} // end switch

		// add PCAL items
		switch (_displaySectors) {
		case SECTORS14:
			for (int stripIndex = 0; stripIndex < 3; stripIndex++) {
				new SectorPCALItem(detectorLayer, stripIndex, 1);
				new SectorPCALItem(detectorLayer, stripIndex, 4);
			}
			break;

		case SECTORS25:
			for (int stripIndex = 0; stripIndex < 3; stripIndex++) {
				new SectorPCALItem(detectorLayer, stripIndex, 2);
				new SectorPCALItem(detectorLayer, stripIndex, 5);
			}
			break;

		case SECTORS36:
			for (int stripIndex = 0; stripIndex < 3; stripIndex++) {
				new SectorPCALItem(detectorLayer, stripIndex, 3);
				new SectorPCALItem(detectorLayer, stripIndex, 6);
			}
			break;
		} // end switch

	}

	/**
	 * Get the super layer drawer
	 *
	 * @param upperLower 0 for upper sector, 1 for lower sector
	 * @param superLayer super layer 1..6
	 * @return the drawer
	 */
	public SuperLayerDrawing getSuperLayerDrawer(int upperLower, int superLayer) {
		return _superLayers[upperLower][superLayer - 1].getSuperLayerDrawer();
	}

	/**
	 * Set the views before draw
	 */
	private void setBeforeDraw() {
		IDrawable beforeDraw = new DrawableAdapter() {

			@Override
			public void draw(Graphics g, IContainer container) {

				if (ClasIoEventManager.getInstance().isAccumulating()) {
					return;
				}


				drawTiltedAxis(g, container, UPPER_SECTOR);
				drawTiltedAxis(g, container, LOWER_SECTOR);

				// if the wires are dirty, recompute their projections
				if (_wiresDirty) {

					for (int superLayer = 0; superLayer < 6; superLayer++) {
						_superLayers[UPPER_SECTOR][superLayer].dirtyWires();
						_superLayers[LOWER_SECTOR][superLayer].dirtyWires();
					}
					_wiresDirty = false;

				}

			}

		};
		getContainer().setBeforeDraw(beforeDraw);
	}

	/**
	 * Set the views before draw
	 */
	private void setAfterDraw() {
		IDrawable afterDraw = new DrawableAdapter() {

			@Override
			public void draw(Graphics g, IContainer container) {

				if (ClasIoEventManager.getInstance().isAccumulating()) {
					return;
				}


				// draw trajectories
				_swimTrajectoryDrawer.draw(g, container);

				// draw reconstructed data
				_reconDrawer.draw(g, container);

				// draw reconstructed dc crosses
				if (showDCHBCrosses()) {
					_dcCrossDrawer.setMode(CrossDrawer.HB);
					_dcCrossDrawer.draw(g, container);
				}
				if (showDCTBCrosses()) {
					_dcCrossDrawer.setMode(CrossDrawer.TB);
					_dcCrossDrawer.draw(g, container);
				}
				if (showAIDCHBCrosses()) {
					_dcCrossDrawer.setMode(CrossDrawer.AIHB);
					_dcCrossDrawer.draw(g, container);
				}
				if (showAIDCTBCrosses()) {
					_dcCrossDrawer.setMode(CrossDrawer.AITB);
					_dcCrossDrawer.draw(g, container);
				}


				// Other (not DC) Crosses
				if (showCrosses()) {
					_fmtCrossDrawer.draw(g, container);
				}

				// scale
				if ((_scaleDrawer != null) && showScale()) {
					_scaleDrawer.draw(g, container);
				}

				// redraw segments
				if (segmentsOnTop) {
//					System.err.println("REDRAW SEGMENTS");
					redrawSegments(g, container);
				}

				// a clean rectangle
				Rectangle bounds = container.getComponent().getBounds();
				GraphicsUtilities.drawSimple3DRect(g, 0, 0, bounds.width - 1, bounds.height - 1, false);
			}

		};
		getContainer().setAfterDraw(afterDraw);
	}



	// redraw the segments on top
	private void redrawSegments(Graphics g, IContainer container) {

		// secty loop is just upper and lower (0-1, not 0-5)
		for (int sect = 0; sect < 2; sect++) {
			for (int supl = 0; supl < 6; supl++) {
				_superLayers[sect][supl].drawSegments(g, container);
			}
		}
	}



	/**
	 * This is used to listen for changes on components like sliders.
	 *
	 * @param e the causal event.
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		Object source = e.getSource();

		if (source == _controlPanel.getPhiSlider()) {
			// change the projection plane
			projectionPlane = GeometryManager.constantPhiPlane(getSliderPhi());

			_wiresDirty = true;
			getContainer().setDirty(true);
			getContainer().refresh();
		}
	}


	/**
	 * Some view specific feedback. Should always call super.getFeedbackStrings
	 * first.
	 *
	 * @param container the base container for the view.
	 * @param pp        the pixel point
	 * @param wp        the corresponding world location.
	 */
	@Override
	public void getFeedbackStrings(IContainer container, Point pp, Point2D.Double wp, List<String> feedbackStrings) {

		// get the common information
		super.getFeedbackStrings(container, pp, wp, feedbackStrings);
		commonFeedbackStrings(container, pp, wp, feedbackStrings);

		// near a swum trajectory?
		double mindist = _swimTrajectoryDrawer.closestApproach(wp);

		double pixlen = WorldGraphicsUtilities.getMeanPixelDensity(container) * mindist;

		// TODO FIX THIS
		_lastTrajStr = null;
		if (pixlen < 25.0) {
			SwimTrajectory2D traj2D = _swimTrajectoryDrawer.getClosestTrajectory();

			// in a sector change diamond
			int sectChangeIndices[] = traj2D.sectChangeIndices();
			if (sectChangeIndices != null) {
				Point scpp = new Point();
				Rectangle crect = new Rectangle();
				for (int idx : sectChangeIndices) {
					Point2D.Double scwp = traj2D.getPath()[idx];
					container.worldToLocal(scpp, scwp);
					crect.setBounds(scpp.x - 4, scpp.y - 4, 8, 8);
					if (crect.contains(pp)) {
						feedbackStrings.add(SwimTrajectory2D.fbColor + traj2D.sectorChangeString(idx));
					}
				}
			}

			if (traj2D != null) {
				traj2D.addToFeedback(feedbackStrings);
				_lastTrajStr = traj2D.summaryString();
			}
		}

		// DC Occupancy
		int sector = getSector(container, pp, wp);

		double totalOcc = 100. * _dcData.totalOccupancy();
		double sectorOcc = 100. * _dcData.totalSectorOccupancy(sector);
		String occStr = "Total DC occ " + DoubleFormat.doubleFormat(totalOcc, 2) + "%" + " sector " + sector + " occ "
				+ DoubleFormat.doubleFormat(sectorOcc, 2) + "%";
		feedbackStrings.add("$aqua$" + occStr);

		// reconstructed feedback?
		if (showDCHBCrosses()) {
			_dcCrossDrawer.setMode(CrossDrawer.HB);
			_dcCrossDrawer.vdrawFeedback(container, pp, wp, feedbackStrings, 0);
		}
		if (showDCTBCrosses()) {
			_dcCrossDrawer.setMode(CrossDrawer.TB);
			_dcCrossDrawer.vdrawFeedback(container, pp, wp, feedbackStrings, 0);
		}
		if (showAIDCHBCrosses()) {
			_dcCrossDrawer.setMode(CrossDrawer.AIHB);
			_dcCrossDrawer.vdrawFeedback(container, pp, wp, feedbackStrings, 0);
		}
		if (showAIDCTBCrosses()) {
			_dcCrossDrawer.setMode(CrossDrawer.AITB);
			_dcCrossDrawer.vdrawFeedback(container, pp, wp, feedbackStrings, 0);
		}


		// Other (not DC) Crosses
		if (showCrosses()) {
			_fmtCrossDrawer.vdrawFeedback(container, pp, wp, feedbackStrings, 0);
		}

		//draws HB hits and segs, TB hits and segs, and nn overlays
		_reconDrawer.vdrawFeedback(container, pp, wp, feedbackStrings, 0);
	}



	/**
	 * Called by a container when a right click is not handled. The usual reason is
	 * that the right click was on an inert spot.
	 *
	 * @param mouseEvent the causal event.
	 */
	@Override
	public boolean rightClicked(MouseEvent mouseEvent) {

		JPopupMenu popup = null;

		// near a swum trajectory?
		Point2D.Double wp = new Point2D.Double();
		getContainer().localToWorld(mouseEvent.getPoint(), wp);
		double mindist = _swimTrajectoryDrawer.closestApproach(wp);
		double pixlen = WorldGraphicsUtilities.getMeanPixelDensity(getContainer()) * mindist;

		if (pixlen < 25.0) {
			final SwimTrajectory2D traj2D = _swimTrajectoryDrawer.getClosestTrajectory();

			if (traj2D == null) {
				return false;
			}

			// get the phi from the trajectory
			final double desiredPhi = traj2D.getTrajectory3D().getOriginalPhi();

			if (popup == null) {
				popup = new JPopupMenu();
			}

			final JMenuItem rotateItem = new JMenuItem(
					"Rotate to match trajectory " + UnicodeSupport.SMALL_PHI + ": " + valStr(desiredPhi, 3));

			final JMenuItem integralItem = new JMenuItem("<html>Plot  " + UnicodeSupport.INTEGRAL + "|<bold>B</bold> "
					+ UnicodeSupport.TIMES + " <bold>dL</bold>|");

			ActionListener al = new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent ae) {

					Object source = ae.getSource();
					if (source == rotateItem) {
						double sliderPhi = getRelativePhi(desiredPhi);
						_controlPanel.getPhiSlider().setValue((int) sliderPhi);
						getContainer().refresh();
					} else if (source == integralItem) {
						PlotView pview = Ced.getCed().getPlotView();
						if (pview != null) {
							PlotCanvas canvas = pview.getPlotCanvas();
							try {
								SwimTrajectory traj = traj2D.getTrajectory3D();
								traj.computeBDL(FieldProbe.factory());

								// do we already have data?
								boolean havePlotData = (canvas.getDataSet() == null) ? false
										: canvas.getDataSet().dataAdded();

								if (!havePlotData) {
									initPlot(canvas, traj2D);
								} else { // have to add a curve
									int curveCount = canvas.getDataSet().getCurveCount();
									DataSet dataSet = canvas.getDataSet();
									dataSet.addCurve("X", traj2D.summaryString() + " ["
											+ MagneticFields.getInstance().getActiveFieldDescription() + "]");
									for (double v[] : traj) {
										dataSet.addToCurve(curveCount, v[SwimTrajectory.PATHLEN_IDX],
												v[SwimTrajectory.BXDL_IDX]);

										setCurveStyle(canvas, curveCount);
									}

								}

								ViewManager.getInstance().setVisible(pview, true);
								canvas.repaint();
							} catch (DataSetException e) {
								e.printStackTrace();
							}
						} // pview not null
					} // integral
				}
			};

			rotateItem.addActionListener(al);
			integralItem.addActionListener(al);
			popup.add(rotateItem);
			popup.add(integralItem);
		} // end near traj (pixlen)

		Point p = mouseEvent.getPoint();
		if (popup != null) {
			popup.show(getContainer().getComponent(), p.x, p.y);
			return true;
		}

		return false;
	}

	//initialize the bdl plot
	private void initPlot(PlotCanvas canvas, SwimTrajectory2D traj2D) throws DataSetException {
		SwimTrajectory traj = traj2D.getTrajectory3D();
		DataSet dataSet = new DataSet(DataSetType.XYXY, "X",
				traj2D.summaryString() + " [" + MagneticFields.getInstance().getActiveFieldDescription() + "]");

		canvas.getParameters().setPlotTitle("Magnetic Field Integral");
		canvas.getParameters().setXLabel("Path Length (m)");
		canvas.getParameters().setYLabel("<html>" + UnicodeSupport.INTEGRAL + "|<bold>B</bold> " + UnicodeSupport.TIMES
				+ " <bold>dL</bold>| kG-m");

		for (double v[] : traj) {
			dataSet.add(v[SwimTrajectory.PATHLEN_IDX], v[SwimTrajectory.BXDL_IDX]);
		}
		canvas.setDataSet(dataSet);
		setCurveStyle(canvas, 0);
	}

	//set the curve style
	private void setCurveStyle(PlotCanvas canvas, int index) {
		int cindex = index % plotColors.length;
		canvas.getDataSet().getCurveStyle(index).setFitLineColor(plotColors[cindex]);
		canvas.getDataSet().getCurveStyle(index).setBorderColor(plotColors[cindex]);
		canvas.getDataSet().getCurveStyle(index).setFillColor(plotColors[cindex]);
		canvas.getDataSet().getCurveStyle(index).setSymbolType(cnuphys.splot.style.SymbolType.X);
		canvas.getDataSet().getCurveStyle(index).setSymbolSize(6);
		canvas.getDataSet().getCurve(index).getFit().setFitType(FitType.CUBICSPLINE);

	}

	/**
	 * Draw a recon hit from hit based or time based tracking
	 *
	 * @param g the Graphics context
	 * @param container the drawing container
	 * @param fillColor the fill color
	 * @param frameColor the border color
	 */
	public void drawDCReconHit(Graphics g, IContainer container, Color fillColor, Color frameColor,
			ATrkgHitData hits, int index, boolean isTimeBased) {

		SectorSuperLayer sectSL = _superLayers[(hits.sector[index] < 4) ? 0 : 1][hits.superlayer[index] - 1];
		sectSL.drawDCReconHit(g, container, fillColor, frameColor, hits, index, isTimeBased);

	}

	/**
	 * Clone the view.
	 *
	 * @return the cloned view
	 */
	@Override
	public BaseView cloneView() {
		super.cloneView();
		CLONE_COUNT[_displaySectors.ordinal()]++;

		// limit
		if (CLONE_COUNT[_displaySectors.ordinal()] > 2) {
			return null;
		}

		Rectangle vr = getBounds();
		vr.x += 40;
		vr.y += 40;

		SectorView view = createSectorView(_displaySectors);
		view.setBounds(vr);
		return view;

	}



}
