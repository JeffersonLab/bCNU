package cnuphys.ced.cedview.dchex;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Properties;

import javax.swing.JTabbedPane;

import cnuphys.bCNU.drawable.DrawableAdapter;
import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.item.ItemList;
import cnuphys.bCNU.util.PropertySupport;
import edu.cnu.mdi.ui.colors.X11Colors;
import cnuphys.bCNU.view.BaseView;
import cnuphys.bCNU.view.ViewConfiguration;
import cnuphys.bCNU.view.VirtualView;
import cnuphys.ced.alldata.DCRawHits;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.cedview.HexView;
import cnuphys.ced.cedview.RollOverDCPanel;
import cnuphys.ced.component.ControlPanel;
import cnuphys.ced.component.DisplayBits;
import cnuphys.ced.geometry.DCGeometry;

@SuppressWarnings("serial")
public class DCHexView extends HexView {


	//cluster drawer
	private DCHexClusterDrawer _clusterDrawer;


	// for naming clones
	private static int CLONE_COUNT = 0;

	private static final Color _fillColor = X11Colors.getX11Color("steel blue");

	// base title
	private static final String _baseTitle = "DC Hex";

	// sector items
	private DCHexSectorItem _hexItems[];

	//bank matches
	private static String _defMatches[] = {"DC:", "HitBased", "TimeBased"};

	// data containers
	private static DCRawHits _dcData = DCRawHits.getInstance();

	//superlayer items
	private DCHexSuperLayer[][] _superLayerItems;

	//rollover panel for drawing clusters
	private RollOverDCPanel _rollOverPanel;
	
	// the title, which includes the clone count if this is a clone
	private static String title = _baseTitle + ((CLONE_COUNT == 0) ? "" : ("_(" + CLONE_COUNT + ")"));


	protected static Rectangle2D.Double _defaultWorld;

	static {
		double _xsize = 1.02 * DCGeometry.getAbsMaxWireX();
		double _ysize = 1.02 * _xsize * 1.154734;

		_defaultWorld = new Rectangle2D.Double(_xsize, -_ysize, -2 * _xsize, 2 * _ysize);

	}

	/**
	 * Create an allDCView
	 *
	 * @param keyVals variable set of arguments.
	 */
	private DCHexView(Object... keyVals) {
		super(keyVals);

		setBeforeDraw();
		setAfterDraw();
		getContainer().getComponent().setBackground(Color.gray);
		_clusterDrawer = new DCHexClusterDrawer(this);
	}
	
	/**
	 * Create a DCHexView view. This is used by lazy creation
	 *
	 * @param keyVals the key value pairs for the view properties
	 * @return a DCHexView View
	 */
	public static DCHexView construct(Object... keyVals) {
		DCHexView view = new DCHexView(getDefaultKeyVals());
		return view;
	}
	/**
	 * Get the view configuration for lazy creation
	 *
	 * @return the view configuration for lazy creation
	 */
	public static ViewConfiguration<DCHexView> getConfiguration() {
		return new ViewConfiguration<>(DCHexView.class, true, 
				6, 0, 0, VirtualView.CENTER, getDefaultKeyVals());
	}


	// add the control panel
	@Override
	protected void addControls() {

		_controlPanel = new ControlPanel(this,
				ControlPanel.NOISECONTROL + ControlPanel.DISPLAYARRAY
				+ ControlPanel.FEEDBACK + ControlPanel.ACCUMULATIONLEGEND + ControlPanel.MATCHINGBANKSPANEL
				+ ControlPanel.ALLDCDISPLAYPANEL,
				DisplayBits.ACCUMULATION, 3, 5);


		add(_controlPanel, BorderLayout.EAST);

		customize(this);

		//i.e. if none were in the properties
		if (hasNoBankMatches()) {
			setBankMatches(_defMatches);
		}
		_controlPanel.getMatchedBankPanel().update();

		pack();
	}

	/**
	 * Used to create the DCXY view
	 *
	 * @return the view
	 */
	public static DCHexView createDCHexView() {
		String title = _baseTitle + ((CLONE_COUNT == 0) ? "" : ("_(" + CLONE_COUNT + ")"));
		DCHexView view = new DCHexView(title);

		return view;
	}

	// add items to the view
	@Override
	protected void addItems() {
		ItemList detectorLayer = getContainer().getItemList(_detectorLayerName);

		_hexItems = new DCHexSectorItem[6];

		for (int sector = 0; sector < 6; sector++) {
			_hexItems[sector] = new DCHexSectorItem(detectorLayer, this, sector + 1);
			_hexItems[sector].getStyle().setFillColor(_fillColor);
			_hexItems[sector].getStyle().setLineColor(Color.cyan);
		}

		//superlayer items
		_superLayerItems = new DCHexSuperLayer[6][6];
		for (int sector = 1; sector <= 6; sector++) {
			for (int superlayer = 1; superlayer <= 6; superlayer++) {
				_superLayerItems[sector - 1][superlayer - 1] = new DCHexSuperLayer(detectorLayer, this, sector,
						superlayer);
			}
		}

	}

	//add the rollover panel
	private void customize(CedView view) {
		JTabbedPane tabbedPane =  _controlPanel.getTabbedPane();
		_rollOverPanel = new RollOverDCPanel(this,
				"DC Clusters", 1);

		tabbedPane.add(_rollOverPanel, "DC Clusters");
	}

	/**
	 * Convenience method to see it we show the montecarlo truth.
	 *
	 * @return <code>true</code> if we are to show the montecarlo truth, if it is
	 *         available.
	 */
	@Override
	public boolean showMcTruth() {
		return true;
	}



	/**
	 * Create the view's before drawer.
	 */
	private void setBeforeDraw() {
		// use a before-drawer to sector dividers and labels
		IDrawable beforeDraw = new DrawableAdapter() {

			@Override
			public void draw(Graphics g, IContainer container) {
				Rectangle b = container.getComponent().getBounds();
				g.setColor(_fillColor);
				g.fillRect(0, 0, b.width, b.height);
			}

		};

		getContainer().setBeforeDraw(beforeDraw);
	}

	private void setAfterDraw() {
		// use a after-drawer to sector dividers and labels
		IDrawable afterDraw = new DrawableAdapter() {

			@Override
			public void draw(Graphics g, IContainer container) {

				if (!_eventManager.isAccumulating()) {


					if (_rollOverPanel.roShowHBDCClusters) {
						_clusterDrawer.drawHBDCClusters(g, container);
					}

					if (_rollOverPanel.roShowTBDCClusters) {
						_clusterDrawer.drawTBDCClusters(g, container);
					}

					if (_rollOverPanel.roShowAIHBDCClusters) {
						_clusterDrawer.drawAIHBDCClusters(g, container);
					}

					if (_rollOverPanel.roShowAITBDCClusters) {
						_clusterDrawer.drawAITBDCClusters(g, container);
					}

					//row selected on bank dialog
					drawDataSelectedHighlight(g, container);


					drawSectorNumbers(g, container, Color.cyan, 85);

				} // not accumulating
			}
		};

		getContainer().setAfterDraw(afterDraw);

	}

	/**
	 * Get the wire polygon for a given wire
	 * @param sector 1-based sector
	 * @param superLayer 1-based superlayer
	 * @param layer 1-based layer
	 * @param wire 1-based wire
	 * @param poly the polygon to fill
	 */
	public boolean getWirePolygon(int sector, int superLayer, int layer, int wire, Point2D.Double[] poly) {
		if (sector < 1 || sector > _superLayerItems.length || superLayer < 1
				|| superLayer > _superLayerItems[sector - 1].length) return false;
		return _superLayerItems[sector - 1][superLayer - 1].getWirePolygon(layer, wire, poly);
	}


	//draw data selected hightlight data
	private void drawDataSelectedHighlight(Graphics g, IContainer container) {
	}


	// get the attributes to pass to the super constructor
	private static Object[] getDefaultKeyVals() {

		Properties props = new Properties();
		props.put(PropertySupport.TITLE, title);

		props.put(PropertySupport.PROPNAME, "DCXY");

		// set to a fraction of screen
		double size[] = getSizeFromScreenFraction(0.68);

		props.put(PropertySupport.WIDTH, (int) (0.866 * size[0]));
		props.put(PropertySupport.HEIGHT, (int)size[1]);

		props.put(PropertySupport.WORLDSYSTEM, _defaultWorld);

		props.put(PropertySupport.TOOLBAR, true);
		props.put(PropertySupport.TOOLBARBITS, CedView.TOOLBARBITS);
		props.put(PropertySupport.VISIBLE, true);

		props.put(PropertySupport.BACKGROUND, Color.black);
		props.put(PropertySupport.STANDARDVIEWDECORATIONS, true);

		return PropertySupport.toObjectArray(props);
	}

	@Override
	public void getFeedbackStrings(IContainer container, Point pp, Point2D.Double wp, List<String> feedbackStrings) {

		container.worldToLocal(pp, wp);

		// get the sector
		int sector = 0;
		int superlayer = 0;
		for (int i = 0; i < 6; i++) {
			if (_hexItems[i].contains(container, pp)) {
				sector = i + 1;
				break;
			}
		}
		if (sector > 0) {
			for (int j = 0; j < 6; j++) {
				if (_superLayerItems[sector - 1][j].contains(container, pp)) {
					superlayer = j + 1;
					break;
				}
			}
		}

		if (superlayer > 0) {
			feedbackStrings.add("$aqua$sector " + sector + " superlayer " + superlayer);
			int layer = _superLayerItems[sector - 1][superlayer - 1].whichLayer(container, pp);
			if (layer > 0) {
				int wire = _superLayerItems[sector - 1][superlayer - 1].whichWire(container, layer, pp);

				if (wire > 0) {
					feedbackStrings.add("$aqua$layer " + layer + " wire " + wire);
 			     	_superLayerItems[sector - 1][superlayer - 1].addToFeedback(layer, wire, feedbackStrings);
				}

			}


			double totalOcc = 100. * _dcData.totalOccupancy();
			double sectorOcc = 100. * _dcData.sectorOccupancy(sector);
			double superlayerOcc = 100. * _dcData.superlayerOccupancy(sector, superlayer);

			String occStr = String.format("occ  total %-6.2f%%  sect %-6.2f%%  suplay %-6.2f%%", totalOcc,
					sectorOcc, superlayerOcc);
			feedbackStrings.add("$aqua$" + occStr);


		}
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

		DCHexView view = createDCHexView();
		view.setBounds(vr);
		return view;
	}


	/**
	 * Display raw DC hits?
	 *
	 * @return <code> if we should display raw hits
	 */
	public boolean showRawHits() {
		return _controlPanel.getAllDCDisplayPanel().showRawHits();
	}

	/**
	 * Display hit based hits?
	 *
	 * @return <code> if we should display hit based hits
	 */
	public boolean showHBHits() {
		return _controlPanel.getAllDCDisplayPanel().showHBHits();
	}

	/**
	 * Display time based hits?
	 *
	 * @return <code> if we should display time based hits
	 */
	public boolean showTBHits() {
		return _controlPanel.getAllDCDisplayPanel().showTBHits();
	}

	/**
	 * Display AI hit based hits?
	 *
	 * @return <code> if we should display AI hit based hits
	 */
	public boolean showAIHBHits() {
		return _controlPanel.getAllDCDisplayPanel().showAIHBHits();
	}

	/**
	 * Display AI time based hits?
	 *
	 * @return <code> if we should display AI time based hits
	 */
	public boolean showAITBHits() {
		return _controlPanel.getAllDCDisplayPanel().showAITBHits();
	}



}
