package cnuphys.ced.cedview.ftof;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.drawable.DrawableAdapter;
import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.item.ItemList;
import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.bCNU.view.BaseView;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.cedview.HexView;
import cnuphys.ced.component.ControlPanel;
import cnuphys.ced.component.DisplayArray;
import cnuphys.ced.component.DisplayBits;
import cnuphys.ced.geometry.ftof.FTOFGeometry;

public class FTOFView extends HexView {

	// sector items
	private FTOFHexSectorItem _hexItems[];

	private static final Color paddleFillColor = X11Colors.getX11Color("mint cream");

	//cache the world polygons for the paddles
	private Hashtable<String, Point2D.Double[]> polyHash =new Hashtable<>();

	// for naming clones
	private static int CLONE_COUNT = 0;

	// panel values
	public static final int FTOF_1A = 0;
	public static final int FTOF_1B = 1;
	public static final int FTOF_2 = 2;

	// base title
	private static final String _baseTitle = "FTOF";

	private static final double _xsize = 440.0;
	private static final double _ysize = _xsize * 1.154734;

	//data drawer
	private FTOFDataDrawer _dataDrawer;

	//for handling highlight data
	private FTOFHighlightHandler _highlightHandler;

	//world limits
	protected static Rectangle2D.Double _defaultWorld = new Rectangle2D.Double(_xsize, -_ysize, -2 * _xsize,
			2 * _ysize);

	//bank matches
	private static String _defMatches[] = {"FTOF"};

	//private constructor
	private FTOFView(String title) {
		super(getAttributes(title));

		_dataDrawer = new FTOFDataDrawer(this);

		//for handling highlights
		_highlightHandler = new FTOFHighlightHandler(this);

		//default to panel 1a
		setIntProperty(DisplayArray.TOFPANEL_PROPERTY, 0);

		setBeforeDraw();
		setAfterDraw();
		getContainer().getComponent().setBackground(Color.gray);

		//i.e. if none were in the properties
		if (hasNoBankMatches()) {
			setBankMatches(_defMatches);
		}
		_controlPanel.getMatchedBankPanel().update();

	}

	// add the control panel
	@Override
	protected void addControls() {

		_controlPanel = new ControlPanel(this,
				ControlPanel.DISPLAYARRAY + ControlPanel.FEEDBACK + ControlPanel.ACCUMULATIONLEGEND
						+ ControlPanel.MATCHINGBANKSPANEL,
				DisplayBits.ACCUMULATION + DisplayBits.MCTRUTH + DisplayBits.TOFPANELS + DisplayBits.CLUSTERS
				+ DisplayBits.GLOBAL_HB+ DisplayBits.RECONHITS + DisplayBits.ADCDATA, 3, 5);

		add(_controlPanel, BorderLayout.EAST);
		pack();
	}

	/**
	 * Used to create the FTOF view
	 *
	 * @return the view
	 */
	public static FTOFView createFTOFView() {
		FTOFView view = new FTOFView(_baseTitle + ((CLONE_COUNT == 0) ? "" : ("_(" + CLONE_COUNT + ")")));
		return view;
	}

	// add items to the view
	@Override
	protected void addItems() {
		ItemList detectorLayer = getContainer().getItemList(_detectorLayerName);

		_hexItems = new FTOFHexSectorItem[6];

		for (int sector = 0; sector < 6; sector++) {
			_hexItems[sector] = new FTOFHexSectorItem(detectorLayer, this, sector + 1);
			_hexItems[sector].getStyle().setFillColor(X11Colors.getX11Color("light cyan"));
		}
	}

	/**
	 * Create the view's before drawer.
	 */
	private void setBeforeDraw() {
		// use a before-drawer to sector dividers and labels
		IDrawable beforeDraw = new DrawableAdapter() {

			@Override
			public void draw(Graphics g, IContainer container) {
			}

		};

		getContainer().setBeforeDraw(beforeDraw);
	}

	/**
	 * Create the view's after drawer.
	 */
	private void setAfterDraw() {
		// use a before-drawer to sector dividers and labels
		IDrawable draw = new DrawableAdapter() {

			@Override
			public void draw(Graphics g, IContainer container) {


				if (!_eventManager.isAccumulating()) {

					drawAllPaddles(g, container);


					//draw all data
					_dataDrawer.draw(g, container);

					//highlight?
					_highlightHandler.draw(g, container);

					drawCoordinateSystem(g, container, null);

					double xx = (displayPanel() != 2) ? 420 : 340;
					drawSectorNumbers(g, container, null, xx);
				} // not acumulating

				g.setColor(TRANSTEXT2);
				g.setFont(_font);
				g.drawString(FTOFGeometry.getPanelName(displayPanel()), 5, g.getFontMetrics().getAscent());

			}

		};

		getContainer().setAfterDraw(draw);
	}

	//draw all paddles
	private void drawAllPaddles(Graphics g, IContainer container) {
		Polygon poly = new Polygon();

		for (int sect = 1; sect <= 6; sect++) {
			for (int paddleId = 1; paddleId <= FTOFGeometry.getNumPaddles(sect, displayPanel()); paddleId++) {
				getPaddlePolygon(container, sect, displayPanel(), paddleId, poly);
				g.setColor(paddleFillColor);
				g.fillPolygon(poly);
				g.setColor(Color.LIGHT_GRAY);
				g.drawPolygon(poly);
			}
		}
	}



	/**
	 * Get the world based polygon for the paddle
	 * @param sector 1-based sector
	 * @param panel 0, 1, 2 for 1A, 1B, 2
	 * @param paddleId 1-based paddle id
	 * @return the world based polygon for the paddle
	 */
	private Point2D.Double[] getWorldPoly(int sector, int panel, int paddleId) {
		String hash = "" + sector + "|" + panel + "|" + paddleId;
		Point2D.Double wp[] = polyHash.get(hash);
		if (wp == null) {
			wp = new Point2D.Double[4];
			for (int i = 0; i < 4; i++) {
				wp[i] = new Point2D.Double();
			}

			FTOFGeometry.paddlePolygon(sector, panel, paddleId, wp);
			polyHash.put(hash, wp);
		}
		return wp;
	}

	/**
	 * Get the world based polygon for the paddle
	 * @param container the container
	 * @param sector 1-based sector
	 * @param panel 0, 1, 2 for 1A, 1B, 2
	 * @param paddleId 1-based paddle id
	 * @param poly the polygon to fill
	 */
	public void getPaddlePolygon(IContainer container,  int sector, int panel, int paddleId, Polygon poly) {
		Point2D.Double wp[] = getWorldPoly(sector, panel, paddleId);
		poly.reset();
		Point pp = new Point();
		for (Point2D.Double pd : wp) {
			container.worldToLocal(pp, pd);
			poly.addPoint(pp.x, pp.y);
		}
	}


	// get the attributes to pass to the super constructor
	private static Object[] getAttributes(String title) {

		Properties props = new Properties();
		props.put(PropertySupport.TITLE, title);

		props.put(PropertySupport.PROPNAME, "FTOF");

		// set to a fraction of screen
		double size[] = getSizeFromScreenFraction(0.7);

		props.put(PropertySupport.WIDTH, (int) (0.866 * size[0]));
		props.put(PropertySupport.HEIGHT, (int)size[1]);

		props.put(PropertySupport.WORLDSYSTEM, _defaultWorld);

		props.put(PropertySupport.TOOLBAR, true);
		props.put(PropertySupport.TOOLBARBITS, CedView.TOOLBARBITS);
		props.put(PropertySupport.VISIBLE, true);
		props.put(PropertySupport.BACKGROUND, X11Colors.getX11Color("Alice Blue"));
		props.put(PropertySupport.STANDARDVIEWDECORATIONS, true);

		return PropertySupport.toObjectArray(props);
	}

	@Override
	public void getFeedbackStrings(IContainer container, Point pp, Point2D.Double wp, List<String> feedbackStrings) {

		feedbackStrings.add("$white$" + FTOFGeometry.getPanelName(displayPanel()));
		super.getFeedbackStrings(container, pp, wp, feedbackStrings);

		for (int sect = 0; sect < 6; sect++) {
			int sect1 = sect+1;
			if (_hexItems[sect].contains(container, pp)) {
				//basic feedback
				double z = FTOFGeometry.getZ(sect1, displayPanel(), wp.x, wp.y);
				String xyz = String.format("%s (%6.2f, %6.2f, %4.0f (approx)) cm", CedView.xyz, wp.x, wp.y, z);
				feedbackStrings.add(xyz);

				int onPaddleId = -1;
				int numPaddles = FTOFGeometry.getNumPaddles(sect1, displayPanel());
				Polygon poly = new Polygon();
				for (int paddleId = 1; paddleId <= numPaddles; paddleId++) {
					getPaddlePolygon(container, sect1, displayPanel(), paddleId, poly);
					if (poly.contains(pp)) {
						feedbackStrings.add("paddle " + paddleId);
						onPaddleId = paddleId;
						break;
					}
				}
				//data feedback
				_dataDrawer.getFeedbackStrings(container, sect1, displayPanel(), onPaddleId, pp, wp, feedbackStrings);

				break;
			}
		}

	}

	/**
	 * Convert from clas3D to local coordinates
	 * @param sector the 1-based sector
	 * @param x the x  clas coordinate
	 * @param y the y  clas coordinate
	 * @param z the z  clas coordinate
	 * @param pp the screen point
	 */
	public void clasToLocal(int sector, double x, double y, double z, Point pp) {
		Point2D.Double wp = new Point2D.Double();
		FTOFGeometry.clasToWorld(sector, displayPanel(), x, y, z, wp);
		getContainer().worldToLocal(pp, wp);
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

		FTOFView view = createFTOFView();
		view.setBounds(vr);
		return view;

	}

	/**
	 * A new event has arrived.
	 *
	 * @param event the new event.
	 */
	@Override
	public void newClasIoEvent(final DataEvent event) {
		super.newClasIoEvent(event);
	}

	/**
	 * In the BankDataTable a row was selected.
	 * @param bankName the name of the bank
	 * @param index the 0-based index into the bank
	 */
	@Override
	public void dataSelected(String bankName, int index) {
		if (bankName.startsWith("FTOF")) {
			_highlightHandler.set(bankName, index);
		}

		refresh();
	}

	/**
	 * Opened a new event file
	 *
	 * @param path the path to the new file
	 */
	@Override
	public void openedNewEventFile(final String path) {
		super.openedNewEventFile(path);
		_highlightHandler.reset();
	}

	/**
	 * Which layer should be displayed
	 * @return 0, 1 or 2 for panels 1A, 1B, and 2
	 */
	public int displayPanel() {
		return getIntProperty(DisplayArray.TOFPANEL_PROPERTY);
	}




}
