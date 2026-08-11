package cnuphys.ced.cedview.dcxy;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jlab.geom.prim.Line3D;

import cnuphys.bCNU.drawable.DrawableAdapter;
import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.style.LineStyle;
import cnuphys.bCNU.item.ItemList;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.bCNU.view.ViewConfiguration;
import cnuphys.bCNU.view.VirtualView;
import cnuphys.ced.alldata.DCRawHits;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.cedview.HexView;
import cnuphys.ced.cedview.SwimTrajectoryDrawerXY;
import cnuphys.ced.common.FMTCrossDrawer;
import cnuphys.ced.component.ControlPanel;
import cnuphys.ced.component.DisplayBits;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.geometry.DCGeometry;
import cnuphys.ced.geometry.GeometryManager;
import cnuphys.ced.item.HexSectorItem;

@SuppressWarnings("serial")
public class DCXYView extends HexView {

	// for naming clones
	private static int CLONE_COUNT = 0;

	// base title
	private static final String _baseTitle = "DC XY";

	// sector items
	private DCXYSectorItem _hexItems[];

	// used to draw swum trajectories (if any) in the after drawer
	private SwimTrajectoryDrawerXY _swimTrajectoryDrawer;

	// draws reconstructed crosses
	private CrossDrawer _crossDrawer;

	// for fmt
	private FMTCrossDrawer _fmtCrossDrawer;

	//bank matches
	private static String _defMatches[] = {"DC:"};

	// data containers
	private static DCRawHits _dcData = DCRawHits.getInstance();


	// each superlayer in a different color
	private static Color _wireColors[] = { Color.red, X11Colors.getX11Color("dark red"),
			X11Colors.getX11Color("cadet blue"), X11Colors.getX11Color("dark blue"), X11Colors.getX11Color("olive"),
			X11Colors.getX11Color("dark green") };

	// the title, which includes the clone count if this is a clone
	private static String title = _baseTitle + ((CLONE_COUNT == 0) ? "" : ("_(" + CLONE_COUNT + ")"));

	private static Stroke stroke = GraphicsUtilities.getStroke(0.5f, LineStyle.SOLID);

	// the z location of the projection plane
	private double _zplane = 100;

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
	private DCXYView(Object... keyVals) {
		super(keyVals);

		// projection plane
		projectionPlane = GeometryManager.xyPlane(_zplane);

		// draws any swum trajectories (in the after draw)
		_swimTrajectoryDrawer = new SwimTrajectoryDrawerXY(this);
		_crossDrawer = new CrossDrawer(this);

		// fmt cross drawer
		_fmtCrossDrawer = new FMTCrossDrawer(this);

		setBeforeDraw();
		setAfterDraw();
		getContainer().getComponent().setBackground(Color.gray);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		JComponent wireLegend = new JComponent() {
			@Override
			public void paintComponent(Graphics g) {
				Rectangle b = getBounds();
				g.setColor(Color.darkGray);
				g.fillRect(b.x, b.y, b.width, b.height);
				int yc = b.y + b.height / 2;
				int linelen = 40;
				g.setFont(Fonts.mediumFont);
				FontMetrics fm = this.getFontMetrics(Fonts.mediumFont);

				int x = 6;
				for (int supl0 = 0; supl0 < 6; supl0++) {
					String s = " superlayer " + (supl0 + 1) + "    ";
					g.setColor(_wireColors[supl0]);
					g.drawLine(x, yc, x + linelen, yc);
					g.drawLine(x + 1, yc + 1, x + linelen + 1, yc + 1);
					x = x + linelen + 4;
					g.setColor(Color.white);
					g.drawString(s, x, yc + 4);
					x += fm.stringWidth(s);
				}
			}

			@Override
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				d.height = 24;
				return d;
			}
		};
		panel.add(wireLegend, BorderLayout.CENTER);
		panel.setBorder(BorderFactory.createEtchedBorder());
		add(panel, BorderLayout.SOUTH);

		// add a quick zoom
		double qzlim = 25;
		addQuickZoom("Central Region", qzlim, -qzlim, -qzlim, qzlim);

	}
	
	/**
	 * Create a DCXYView view. This is used by lazy creation
	 *
	 * @param keyVals the key value pairs for the view properties
	 * @return a DCXYView View
	 */
	public static DCXYView construct(Object... keyVals) {
		DCXYView view = new DCXYView(getDefaultKeyVals());
		return view;
	}
	/**
	 * Get the view configuration for lazy creation
	 *
	 * @return the view configuration for lazy creation
	 */
	public static ViewConfiguration<DCXYView> getConfiguration() {
		return new ViewConfiguration<>(DCXYView.class, true, 
				10, 0, 0, VirtualView.CENTER, getDefaultKeyVals());
	}

	// add the control panel
	@Override
	protected void addControls() {
		System.out.println("DCXYView adding controls");

		_controlPanel = new ControlPanel(this,
				ControlPanel.DISPLAYARRAY + ControlPanel.FEEDBACK + ControlPanel.ACCUMULATIONLEGEND
						+ ControlPanel.MATCHINGBANKSPANEL,
				DisplayBits.ACCUMULATION + DisplayBits.CROSSES + DisplayBits.FMTCROSSES + DisplayBits.RECPART
						+ DisplayBits.GLOBAL_HB + DisplayBits.GLOBAL_TB + DisplayBits.GLOBAL_AIHB
						+ DisplayBits.GLOBAL_AITB + DisplayBits.CVTRECTRACKS + DisplayBits.MCTRUTH
						+ DisplayBits.SECTORCHANGE + DisplayBits.CVTP1TRACKS,
				3, 5);

		add(_controlPanel, BorderLayout.EAST);

		//i.e. if none were in the properties
		if (hasNoBankMatches()) {
			setBankMatches(_defMatches);
		}
		_controlPanel.getMatchedBankPanel().update();


		pack();
	}


	// add items to the view
	@Override
	protected void addItems() {
		ItemList detectorLayer = getContainer().getItemList(_detectorLayerName);

		_hexItems = new DCXYSectorItem[6];

		for (int sector = 0; sector < 6; sector++) {
			_hexItems[sector] = new DCXYSectorItem(detectorLayer, this, sector + 1);
			_hexItems[sector].getStyle().setFillColor(Color.lightGray);
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

	private void setAfterDraw() {
		// use a after-drawer to sector dividers and labels
		IDrawable afterDraw = new DrawableAdapter() {

			@Override
			public void draw(Graphics g, IContainer container) {

				if (!_eventManager.isAccumulating()) {

					// draw trajectories
					_swimTrajectoryDrawer.draw(g, container);

					drawHits(g, container);

					// draw reconstructed dc crosses
					if (showDCHBCrosses()) {
						_crossDrawer.setMode(CrossDrawer.HB);
						_crossDrawer.draw(g, container);
					}
					if (showDCTBCrosses()) {
						_crossDrawer.setMode(CrossDrawer.TB);
						_crossDrawer.draw(g, container);
					}
					if (showAIDCHBCrosses()) {
						_crossDrawer.setMode(CrossDrawer.AIHB);
						_crossDrawer.draw(g, container);
					}
					if (showAIDCTBCrosses()) {
						_crossDrawer.setMode(CrossDrawer.AITB);
						_crossDrawer.draw(g, container);
					}

					// Other (not DC) Crosses
					if (showCrosses()) {
						_fmtCrossDrawer.draw(g, container);
					}

					drawCoordinateSystem(g, container, null);
					drawSectorNumbers(g, container, null, 400);
				} // not acumulating
			}

		};

		getContainer().setAfterDraw(afterDraw);
	}

	private void drawHits(Graphics g, IContainer container) {

		if (isSingleEventMode()) {

			int count = _dcData.count();
			if (count > 0) {
				Graphics2D g2 = (Graphics2D) g;
				Stroke oldStroke = g2.getStroke();
				g2.setStroke(stroke);

				Point pp1 = new Point();
				Point pp2 = new Point();
				Point2D.Double wp1 = new Point2D.Double();
				Point2D.Double wp2 = new Point2D.Double();

				for (int i = 0; i < count; i++) {
					int sect = _dcData.sector(i);
					int supl = _dcData.superlayer(i);
					int lay = _dcData.layerInSuperlayer(i);
					int wire = _dcData.wire(i);
					projectWire(g, container, sect, supl, lay, wire, wp1, wp2, pp1, pp2);
					g.setColor(_wireColors[supl - 1]);
					g.drawLine(pp1.x, pp1.y, pp2.x, pp2.y);
				}

				g2.setStroke(oldStroke);
			}

		} else {
			drawAccumulatedHits(g, container);
		}
	}

	//everything is one based
	private void projectWire(Graphics g, IContainer container, int sect1, int supl1, int layer1, int wire1,
			Point2D.Double wp1, Point2D.Double wp2, Point p1, Point p2) {
		Line3D line = DCGeometry.getWire(sect1, supl1, layer1, wire1);
		projectClasToWorld(line.origin(), projectionPlane, wp1);
		projectClasToWorld(line.end(), projectionPlane, wp2);

		container.worldToLocal(p1, wp1);
		container.worldToLocal(p2, wp2);
	}




	// draw the gemc global hits
	private void drawAccumulatedHits(Graphics g, IContainer container) {

		int dcAccumulatedData[][][][] = AccumulationManager.getInstance().getAccumulatedDCData();

		Point pp1 = new Point();
		Point pp2 = new Point();
		Point2D.Double wp1 = new Point2D.Double();
		Point2D.Double wp2 = new Point2D.Double();

		for (int sect0 = 0; sect0 < 6; sect0++) {
			for (int supl0 = 0; supl0 < 6; supl0++) {

				int maxHit = AccumulationManager.getInstance().getMaxDCCount(supl0);

				for (int lay0 = 0; lay0 < 6; lay0++) {
					for (int wire0 = 0; wire0 < 112; wire0++) {

						int hitCount = dcAccumulatedData[sect0][supl0][lay0][wire0];

						if (hitCount > 0) {
							double fract = (maxHit == 0) ? 0 : (((double) hitCount) / maxHit);
							Color color = AccumulationManager.getInstance().getAlphaColor(getColorScaleModel(), fract,
									128);

							projectWire(g, container, sect0 + 1, supl0 + 1, lay0 + 1, wire0 + 1, wp1, wp2, pp1, pp2);

							g.setColor(color);
							g.drawLine(pp1.x, pp1.y, pp2.x, pp2.y);

						} // hitcount > 0
					}
				}
			}
		}
	}

	// get the attributes to pass to the super constructor
	public static Object[] getDefaultKeyVals() {

		Properties props = new Properties();
		props.put(PropertySupport.TITLE, title);
		props.put(PropertySupport.PROPNAME, "DCXY");

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

		container.worldToLocal(pp, wp);

		super.getFeedbackStrings(container, pp, wp, feedbackStrings);

		// reconstructed feedback?
		if (showDCHBCrosses()) {
			_crossDrawer.setMode(CrossDrawer.HB);
			_crossDrawer.feedback(container, pp, wp, feedbackStrings);
		}
		if (showDCTBCrosses()) {
			_crossDrawer.setMode(CrossDrawer.TB);
			_crossDrawer.feedback(container, pp, wp, feedbackStrings);
		}
		if (showAIDCHBCrosses()) {
			_crossDrawer.setMode(CrossDrawer.AIHB);
			_crossDrawer.feedback(container, pp, wp, feedbackStrings);
		}
		if (showAIDCTBCrosses()) {
			_crossDrawer.setMode(CrossDrawer.AITB);
			_crossDrawer.feedback(container, pp, wp, feedbackStrings);
		}


		// Other (not DC) Crosses
		if (showCrosses()) {
			_fmtCrossDrawer.vdrawFeedback(container, pp, wp, feedbackStrings, 0);
		}

	}

	/**
	 * Lab (CLAS) xy coordinates to local screen coordinates.
	 *
	 * @param container the drawing container
	 * @param pp        will hold the graphical world coordinates
	 * @param lab       the lab coordinates
	 */
	public static void labToLocal(IContainer container, Point pp, Point2D.Double lab) {
		container.worldToLocal(pp, lab);
	}

	/**
	 * Get the hex item for the given 1-based sector
	 *
	 * @param sector the 1-based sector
	 * @return the corresponding item
	 */
	public HexSectorItem getHexSectorItem(int sector) {
		if ((sector < 1) || (sector > 6)) {
			System.err.println("Bad sector in DCXYView getHexSectorItem, sector = " + sector);
			return null;
		}
		return _hexItems[sector - 1];
	}


}
