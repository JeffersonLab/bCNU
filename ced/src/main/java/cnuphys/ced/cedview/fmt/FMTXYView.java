package cnuphys.ced.cedview.fmt;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.jlab.geom.component.TrackerStrip;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;

import cnuphys.bCNU.drawable.DrawableAdapter;
import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.util.PropertySupport;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.cedview.CedXYView;
import cnuphys.ced.component.ControlPanel;
import cnuphys.ced.component.DisplayBits;
import cnuphys.ced.component.GeoDisplayArray;
import cnuphys.ced.component.GeoDisplayBits;
import cnuphys.ced.geometry.GeometryManager;
import cnuphys.ced.geometry.fmt.FMTGeometry;

public class FMTXYView extends CedXYView  {

	// camera Z for projection
	private double _zcamera = -45;

	// for naming clones
	private static int CLONE_COUNT = 0;

	// base title
	private static final String _baseTitle = "FMT XY";

	// units are mm
	private static Rectangle2D.Double _defaultWorldRectangle = new Rectangle2D.Double(20, 20, -40, -40);

	// bank matches
	private static String _defMatches[] = { "FMT" };

	private static final double _zmid = 32.8247;

	//check boxes for regions and layers
	private GeoDisplayArray _geoDisplayArray;

	private  FMTXYView(Object... keyVals) {
		super(keyVals);
	}

	/**
	 * Create a Alert detector XY view
	 * @return a Alert detector XY view
	 */
	public static FMTXYView createFMTXYView() {
		// set to a fraction of screen
		// set to a fraction of screen
		double size[] = getSizeFromScreenFraction(0.4);

		// make it square
		int width = (int) size[0];
		int height = width - 100; // leave room for control panel

		String title = _baseTitle + ((CLONE_COUNT == 0) ? "" : ("_(" + CLONE_COUNT + ")"));

		final FMTXYView view = new FMTXYView(PropertySupport.WORLDSYSTEM, _defaultWorldRectangle,
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
				ControlPanel.DISPLAYARRAY + ControlPanel.FEEDBACK + ControlPanel.ACCUMULATIONLEGEND +
				ControlPanel.MATCHINGBANKSPANEL,
				DisplayBits.ACCUMULATION
						+ DisplayBits.ADCDATA + DisplayBits.MCTRUTH,
				3, 5);

		view.add(view._controlPanel, BorderLayout.EAST);
		view.pack();

		// i.e. if none were in the properties
		if (view.hasNoBankMatches()) {
			view.setBankMatches(_defMatches);
		}

		view._controlPanel.getMatchedBankPanel().update();

		//add check boxes for regions and layers
		view._geoDisplayArray = new GeoDisplayArray(view, GeoDisplayBits.FMT_REGIONS + GeoDisplayBits.FMT_LAYERS, 2, 50);
		view._controlPanel.addComponent(view._geoDisplayArray);


		//add dc projection panel
//		view._dcPanel = view._controlPanel.getAlertDCPanel();

		return view;
	}

	@Override
	protected void setBeforeDraw() {
		IDrawable beforeDraw = new DrawableAdapter() {

			@Override
			public void draw(Graphics g, IContainer container) {
				drawStrips(g, container);
			}

		};

		getContainer().setBeforeDraw(beforeDraw);
	}

	@Override
	protected void setAfterDraw() {
		final FMTXYView view = this;

		IDrawable afterDraw = new DrawableAdapter() {

			@Override
			public void draw(Graphics g, IContainer container) {

				Rectangle screenRect = getActiveScreenRectangle(container);
				drawAxes(g, container, screenRect, false);

			}

		};

		getContainer().setAfterDraw(afterDraw);
	}

	//set the projection plane
	public void setProjectionPlane(double z) {
		Plane3D plane = GeometryManager.constantZPlane(z);
		this.projectionPlane = plane;
	}


	private static final Color[] _colors = { Color.red, Color.green, Color.blue, Color.orange, Color.cyan, Color.gray };
	private static final double[] _scales = { 1.0, 1.01, 1.02, 1.03, 1.04, 1.05 };

	private void drawStrips(Graphics g, IContainer container) {
//		Point pp = new Point();
//		Point2D.Double wp = new Point2D.Double();
//		for (int layer = 0; layer < 6; layer++) {
//			g.setColor(_colors[layer]);
//			for (int stripId = 0; stripId < 1024; stripId++) {
//				TrackerStrip strip = FMTGeometry.getStrip(0, 0, layer, stripId);
//				Point3D p = strip.getMidpoint();
//				wp.x = _scales[layer]*p.x();
//				wp.y = _scales[5-layer]*p.y();
//				container.worldToLocal(pp, wp);
//				g.fillOval(pp.x - 2, pp.y - 2, 4, 4);
//			}
//		}

		Point p0 = new Point();
		Point p1 = new Point();
		Point2D.Double wp0 = new Point2D.Double();
		Point2D.Double wp1 = new Point2D.Double();

		for (int layer = 0; layer < 6; layer++) {
			if (!showFMTLayer(layer + 1)) {
				continue;
			}

			g.setColor(_colors[layer]);
			for (int stripId = 0; stripId < 1024; stripId++) {

				int region = FMTGeometry.getRegion(stripId + 1);
				if (!showFMTRegion(region)) {
					continue;
				}

				TrackerStrip strip = FMTGeometry.getStrip(0, 0, layer, stripId);
				Line3D line = strip.getLine();
					labToWorld(layer, line.origin(), wp0);
				container.worldToLocal(p0, wp0);
				g.fillOval(p0.x - 2, p0.y - 2, 4, 4);


//				getXYatZ(line, wp0, _zmid);
//				container.worldToLocal(p0, wp0);
//				g.fillOval(p0.x - 2, p0.y - 2, 4, 4);

//				Point3D origin = line.origin();
//				Point3D end = line.end();
//				wp0.x = _scales[layer]*origin.x();
//				wp0.y = _scales[5-layer]*origin.y();
//				wp1.x = _scales[layer]*end.x();
//				wp1.y = _scales[5-layer]*end.y();
//
//				shrink(wp0, wp1, 0.01);
//				container.worldToLocal(p0, wp0);
//				container.worldToLocal(p1, wp1);
//				g.drawLine(p0.x, p0.y, p1.x, p1.y);

			}
		}

	}

	private static final double layerZ[] = {29.75, 30.94, 32.13, 33.52, 34.71, 35.90};
	/**
	 * Convert lab coordinates (CLAS x,y,z) to world coordinates (2D world system of
	 * the view). Used by the swim drawer.
	 *
	 * @param x  the CLAS12 x coordinate
	 * @param y  the CLAS12 y coordinate
	 * @param z  the CLAS12 z coordinate
	 * @param wp holds the world point
	 */
	public void labToWorld(int layer, Point3D p3d, Point2D.Double wp) {
		//do the projection
		double zp = layerZ[layer];
		double scale = (_zcamera - zp) / _zcamera;
		wp.x = p3d.x()*scale;
		wp.y = p3d.y()*scale;
	}

	private void shrink(Point2D.Double wp0, Point2D.Double wp1, double scale) {
		double dx = wp1.x - wp0.x;
		double dy = wp1.y - wp0.y;
		double len = Math.sqrt(dx * dx + dy * dy);
		double newLen = len * scale;
		double t = newLen / len;
		wp1.x = wp0.x + t * dx;
		wp1.y = wp0.y + t * dy;
	}

	public double getXYatZ(Line3D line, Point2D.Double xy, double z) {
		Point3D p0 = line.origin();
		Point3D p1 = line.end();
		double t = (z - p0.z()) / (p1.z() - p0.z());
		xy.x = p0.x() + t * (p1.x() - p0.x());
		xy.y = p0.y() + t * (p1.y() - p0.y());
		return t;
	}


	@Override
	protected void addItems() {
	}

	/**
	 * Show FMT Layer?
	 *
	 * @param layer the 1-based layer to show
	 * @return <code>true</code> if we are to show the layer
	 */
	public boolean showFMTLayer(int layer) {
		switch (layer) {
		case 1:
			return _geoDisplayArray.showLayer1();
		case 2:
			return _geoDisplayArray.showLayer2();
		case 3:
			return _geoDisplayArray.showLayer3();
		case 4:
			return _geoDisplayArray.showLayer4();
		case 5:
			return _geoDisplayArray.showLayer5();
		case 6:
			return _geoDisplayArray.showLayer6();
		}
		return false;
	}

	/**
	 * Show FMT Region?
	 *
	 * @param region the 1-based region to show
	 * @return <code>true</code> if we are to show the region
	 */
	public boolean showFMTRegion(int region) {
		switch (region) {
		case 1:
			return _geoDisplayArray.showRegion1();
		case 2:
			return _geoDisplayArray.showRegion2();
		case 3:
			return _geoDisplayArray.showRegion3();
		case 4:
			return _geoDisplayArray.showRegion4();
		}
		return false;
	}

}
