package cnuphys.ced.cedview.urwt;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.drawable.DrawableAdapter;
import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.item.ItemList;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.bCNU.view.BaseView;
import cnuphys.ced.alldata.DataDrawSupport;
import cnuphys.ced.alldata.DataWarehouse;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.cedview.HexView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.component.ControlPanel;
import cnuphys.ced.component.DisplayBits;
import cnuphys.ced.geometry.GeometryManager;
import cnuphys.ced.geometry.urwt.UrWTDetectorData;
import cnuphys.ced.geometry.urwt.UrWTGeometry;
import cnuphys.ced.item.HexSectorItem;


@SuppressWarnings("serial")
public class UrWTXYView extends HexView {

	//data warehouse
	private static DataWarehouse _dataWarehouse = DataWarehouse.getInstance();

	private static int CLONE_COUNT = 0;


	// sector items
	private UrWTHexSectorItem _hexItems[];

	// chamber outline items
	private UrWTDetectorItem detectorItems[][];

	private SwimTrajectoryDrawer _swimTrajectoryDrawer;

	//the default world bounds
	protected static Rectangle2D.Double _defaultWorld;

	// the z location of the projection plane
	private double _zplane = 200;

	//work space
	private Point _pp1 = new Point();
	private Point _pp2 = new Point();
	private Point2D.Double _wp1 = new Point2D.Double();
	private Point2D.Double _wp2 = new Point2D.Double();
	private Point3D _p3d1 = new Point3D();
	private Point3D _p3d2 = new Point3D();
	private Rectangle _fbRect = new Rectangle();

	//for highlighting
	private HighlightData _highlightData = new HighlightData();

	//bank matches
	private static String _defMatches[] = {"URWT"};


	//size the world to be a bit larger than the hexagons
	static {
		double _xsize = 160;
		double _ysize = _xsize * 1.154734;

		_defaultWorld = new Rectangle2D.Double(_xsize, -_ysize, -2 * _xsize, 2 * _ysize);

	}

	//create the view
	private UrWTXYView(String title) {
		super(getAttributes(title));

		// projection plane
		projectionPlane = GeometryManager.xyPlane(_zplane);

		//draw trajectories
		_swimTrajectoryDrawer = new SwimTrajectoryDrawer(this);

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
				DisplayBits.ACCUMULATION + DisplayBits.CLUSTERS +  DisplayBits.CROSSES + DisplayBits.RECPART
						+ DisplayBits.GLOBAL_HB + DisplayBits.GLOBAL_TB + DisplayBits.GLOBAL_AIHB
						+ DisplayBits.GLOBAL_AITB + DisplayBits.URWTLAYERS
						+ DisplayBits.MCTRUTH + DisplayBits.SECTORCHANGE,
				3, 5);

		add(_controlPanel, BorderLayout.EAST);
		_controlPanel.addComponent(layerColorPanel());
		pack();
	}

	/**
	 * Create a panel showing the layer colors.
	 *
	 * @return the panel
	 */
	public static JComponent layerColorPanel() {
	    JPanel panel = new JPanel(new GridLayout(1, 4, 10, 0)); // Added 10px horizontal gap
	    panel.setBackground(Color.white);
	    panel.setOpaque(true);

	    // 1. Create the visible line border
	    Border line = BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1);
	    // 2. Create the internal padding (top, left, bottom, right)
	    Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
	    // 3. Combine them
	    panel.setBorder(BorderFactory.createCompoundBorder(line, padding));

	    for (int layer = 1; layer <= 4; layer++) {
	        JLabel label = new JLabel("Layer " + layer);
	        label.setFont(Fonts.mediumBoldFont);
	        label.setForeground(UrWTDetectorItem.layerColors[layer - 1]);
	        panel.add(label);
	    }
	    return panel;
	}

	/**
	 * Used to create a UrWTView
	 *
	 * @return the new view
	 */
	public static UrWTXYView createUrWTView() {
		String title = UrWTGeometry.NAME + "XY" + ((CLONE_COUNT == 0) ? "" : ("_(" + CLONE_COUNT + ")"));

		UrWTXYView view = new UrWTXYView(title);
		return view;
	}

	// add items to the view
	@Override
	protected void addItems() {
		ItemList detectorLayer = getContainer().getItemList(_detectorLayerName);

		_hexItems = new UrWTHexSectorItem[6];

		//hex items that form the outline of the sectors
		for (int sector = 0; sector < 6; sector++) {
			_hexItems[sector] = new UrWTHexSectorItem(detectorLayer, this, sector + 1);
			_hexItems[sector].getStyle().setFillColor(Color.lightGray);
		}

		//the detector items
		detectorItems = new UrWTDetectorItem[6][4];
		for (int sector = 0; sector < 6; sector++) {
			for (int layer = 3; layer >= 0; layer--) {
				detectorItems[sector][layer] = new UrWTDetectorItem(detectorLayer, sector+1, layer+1);
			}
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

				if (_eventManager.isAccumulating()) {
					return;
				}

				
				if (isSingleEventMode()) {

					// draw trajectories
					_swimTrajectoryDrawer.draw(g, container);

					for (int sector = 1; sector <= 6; sector++) {
						for (int layer = 4; layer >= 1; layer--) {
							if (!showLayer(layer)) {
								continue;
							}
							UrWTDetectorItem item = detectorItems[sector - 1][layer - 1];
							item.drawData(g, container);
						}
					}


//					//data selected highlight?
					drawDataSelectedHighlight(g, container);

					drawCoordinateSystem(g, container, null);
					drawSectorNumbers(g, container, null, 145);
				} // accumulation
				else {
					drawAccumulatedHits(g, container);
				}


				for (int sector = 1; sector <= 6; sector++) {
					for (int layer = 1; layer <= 4; layer++) {
						UrWTDetectorItem item = detectorItems[sector - 1][layer - 1];
						item.frame(g, container);
					}
				}
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

		//indices are zero based

		if (dataEvent.hasBank("URWT::clusters") && (_highlightData.cluster >= 0) && showClusters()) {
			int idx = _highlightData.cluster; // 0 based

			byte layer = _dataWarehouse.getByte("URWT::clusters", "layer")[idx];

			if (showLayer(layer)) {

				float xo = _dataWarehouse.getFloat("URWT::clusters", "xo")[idx];
				float yo = _dataWarehouse.getFloat("URWT::clusters", "yo")[idx];
				float zo = _dataWarehouse.getFloat("URWT::clusters", "zo")[idx];
				float xe = _dataWarehouse.getFloat("URWT::clusters", "xe")[idx];
				float ye = _dataWarehouse.getFloat("URWT::clusters", "ye")[idx];
				float ze = _dataWarehouse.getFloat("URWT::clusters", "ze")[idx];

				projectLine(container, xo, yo, zo, xe, ye, ze, _pp1, _pp2);
				GraphicsUtilities.drawThickHighlightedLine(g, _pp1.x, _pp1.y, _pp2.x, _pp2.y, Color.orange,
						Color.white);
			}
		}

		if (dataEvent.hasBank("URWT::crosses") && (_highlightData.cross >= 0) && showCrosses()) {
			int idx = _highlightData.cross; //0 based
			float x = _dataWarehouse.getFloat("URWT::crosses", "x")[idx];
			float y = _dataWarehouse.getFloat("URWT::crosses", "y")[idx];
			float z = _dataWarehouse.getFloat("URWT::crosses", "z")[idx];
			projectPoint(container, x, y, z, _pp1);
			DataDrawSupport.drawBiggerCross(g, _pp1.x, _pp1.y, 5);
		}


		if (dataEvent.hasBank("URWT::hits") && (_highlightData.hit >= 0)) {

			int idx = _highlightData.hit; // 0 based

			byte sector = _dataWarehouse.getByte("URWT::hits", "sector")[idx];
			byte layer = _dataWarehouse.getByte("URWT::hits", "layer")[idx];

			// are we showing that layer?
			if (showLayer(layer)) {

				short strip = _dataWarehouse.getShort("URWT::hits", "strip")[idx];

				UrWTDetectorData data = UrWTGeometry.getDetectorData(sector, layer);
				Line3D line = data.getStrip(strip);

				projectLine(container, (float) line.origin().x(), (float) line.origin().y(), (float) line.origin().z(),
						(float) line.end().x(), (float) line.end().y(), (float) line.end().z(), _pp1, _pp2);

				GraphicsUtilities.drawOval(g, _pp1.x, _pp1.y, 6, 6, Color.black, Color.cyan);
				GraphicsUtilities.drawOval(g, _pp2.x, _pp2.y, 6, 6, Color.black, Color.cyan);

			}
		}

	}



	// helper to determine if a layer should be drawn
	protected boolean showLayer(int layer) {
		if (layer == 1) {
			return showLayer1();
		} else if (layer == 2) {
			return showLayer2();
		} else if (layer == 3) {
			return showLayer3();
		} else if (layer == 4) {
			return showLayer4();
		}
		return false;
	}

	/**
	 *
	 * @param container the container
	 * @param sector 1-based sector [1..6]
	 * @param layer 1-based layer [1..4]
	 * @param strip 1-based strip
	 */
	protected void projectStrip(IContainer container, int sector,
			int layer, int strip,
			Point p0, Point p1) {

		UrWTDetectorData data = UrWTGeometry.getDetectorData(sector, layer);
		Line3D line = data.getStrip(strip);

		if (line == null) {
			System.err.println(String.format("null strip in UrWTXYView projectStrip for [sector, layer, chamberStrip] = [%d, %d, %d]",
					sector, layer, strip));
			return;
		}

		projectLine(container, (float) line.origin().x(), (float) line.origin().y(), (float) line.origin().z(),
				(float) line.end().x(), (float) line.end().y(), (float) line.end().z(),
				p0, p1);
	}

	/**
	 *
	 * @param container the container
	 * @param x1
	 * @param y1
	 * @param z1
	 * @param x2
	 * @param y2
	 * @param z2
	 */
	protected void projectLine(IContainer container, float x1, float y1, float z1, float x2, float y2, float z2, Point p1, Point p2) {

		_p3d1.set(x1, y1, z1);
		_p3d2.set(x2, y2, z2);

		projectClasToWorld(_p3d1, projectionPlane, _wp1);
		projectClasToWorld(_p3d2, projectionPlane, _wp2);

		container.worldToLocal(p1, _wp1);
		container.worldToLocal(p2, _wp2);

	}

	/**
	 *
	 * @param container the container
	 * @param x
	 * @param y
	 * @param z
	 */
	protected void projectPoint(IContainer container, float x, float y, float z,Point pp) {
		_p3d1.set(x, y, z);
		projectClasToWorld(_p3d1, projectionPlane, _wp1);
		container.worldToLocal(pp, _wp1);
	}



	//draw accumulated hits
	private void drawAccumulatedHits(Graphics g, IContainer container) {
		for (int sector = 1; sector <= 6; sector++) {
			for (int layer = 4; layer >= 1; layer--) {
				if (!showLayer(layer)) {
					continue;
				}
				UrWTDetectorItem item = detectorItems[sector - 1][layer - 1];
				item.drawAccumulatedData(g, container);
			}
		}
	}

	// get the attributes to pass to the super constructor
	private static Object[] getAttributes(String title) {

		Properties props = new Properties();
		props.put(PropertySupport.TITLE, title);
		props.put(PropertySupport.PROPNAME, "URWTXY");
		props.put(PropertySupport.WORLDSYSTEM, _defaultWorld);

		double size[] = getSizeFromScreenFraction(0.7);

		props.put(PropertySupport.WIDTH, (int) (0.866 * size[0]));
		props.put(PropertySupport.HEIGHT, (int)size[1]);

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


		//crosses feedback
		crossesFeedback(container, pp, wp, feedbackStrings);
	}

	//on a cross?
	private void crossesFeedback(IContainer container, Point pp, Point2D.Double wp, List<String> feedbackStrings) {

		if (isSingleEventMode()) {
			DataEvent event = ClasIoEventManager.getInstance().getCurrentEvent();
			if (event == null) {
				return;
			}

			byte sector[] = _dataWarehouse.getByte("URWT::crosses", "sector");

			int count = (sector == null) ? 0 : sector.length;
			if (count == 0) {
				return;
			}

			float x[] = _dataWarehouse.getFloat("URWT::crosses", "x");
			float y[] = _dataWarehouse.getFloat("URWT::crosses", "y");
			float z[] = _dataWarehouse.getFloat("URWT::crosses", "z");

			for (int i = 0; i < count; i++) {
				projectPoint(container, x[i], y[i], z[i], _pp1);
				_fbRect.setBounds(_pp1.x-5, _pp1.y-5, 10, 10);

				if (_fbRect.contains(pp)) {
					short id = _dataWarehouse.getShort("URWT::crosses", "id")[i];
					short cluster1 = _dataWarehouse.getShort("URWT::crosses", "cluster1")[i];
					short cluster2 = _dataWarehouse.getShort("URWT::crosses", "cluster2")[i];
					short status = _dataWarehouse.getShort("URWT::crosses", "status")[i];

					String fbs1 = String.format("$cyan$cross: %d  status: %d", id, status);
					String fbs2 = String.format("$cyan$cross clusters: %d and %d", cluster1, cluster2);
					feedbackStrings.add(fbs1);
					feedbackStrings.add(fbs2);
					return;
				}
			}

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

		UrWTXYView view = createUrWTView();
		view.setBounds(vr);
		return view;

	}


	/**
	 * In the BankDataTable a row was selected.
	 * @param bankName the name of the bank
	 * @param index the 0-based index into the bank
	 */
	@Override
	public void dataSelected(String bankName, int index) {

		if ("URWT::hits".equals(bankName)) {
			_highlightData.hit = index;
		}
		else if ("URWT::clusters".equals(bankName)) {
			_highlightData.cluster = index;
		}
		else if ("URWT::crosses".equals(bankName)) {
			_highlightData.cross = index;
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
		_highlightData.reset();
	}



}
