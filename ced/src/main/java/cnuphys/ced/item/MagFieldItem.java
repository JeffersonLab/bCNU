package cnuphys.ced.item;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import cnuphys.bCNU.graphics.colorscale.ColorScaleModel;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.style.LineStyle;
import cnuphys.bCNU.item.AItem;
import cnuphys.bCNU.item.ItemList;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.cedview.SliceView;
import cnuphys.ced.cedview.central.CentralZView;
import cnuphys.ced.cedview.magfieldview.MagfieldView;
import cnuphys.ced.cedview.sectorview.SectorView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.component.MagFieldDisplayArray;
import cnuphys.ced.common.ScientificColorScales;
import cnuphys.magfield.FieldProbe;
import cnuphys.magfield.GridCoordinate;
import cnuphys.magfield.MagneticFieldChangeListener;
import cnuphys.magfield.MagneticFields;
import cnuphys.magfield.Solenoid;
import cnuphys.magfield.Torus;
import edu.cnu.mdi.ui.colors.ScientificColorMap;

/**
 * This is a magnetic field item. It is restricted to live only on sector views.
 * It will show the composite magnetic field--the sum of all fields loaded and
 * not set to the zero field.
 *
 * @author heddle
 *
 */
public class MagFieldItem extends AItem implements MagneticFieldChangeListener {

	// sector view parent
	private CedView _view;

	private FieldProbe _activeProbe;

	// if mag field failed to , give up
	private static boolean _failedToLoad = false;

	// common colorscale
	public static ColorScaleModel _colorScaleModelTorus = new ColorScaleModel("", getTorusValues(), getTorusColors(), 2,
			1);
	public static ColorScaleModel _colorScaleModelSolenoid = new ColorScaleModel("", getSolenoidValues(),
			getSolenoidColors(), 2, 1);
	public static ColorScaleModel _colorScaleModelGradient = new ColorScaleModel("", getGradientValues(),
			getGradientColors(), 2, 1);

	// pixel step size
	private int pixelStep = 3;

	// the world coordinate boundary of the field
	private static Rectangle2D.Double fieldBoundary;

	/**
	 * Create a magnetic field item. Only allowed on sector views
	 *
	 * @param itemList the list this item lives on
	 * @param view  the view--which must be a SectorView
	 */
	public MagFieldItem(ItemList itemList, CedView view) {
		super(itemList);
		_view = view;
		_style.setFillColor(null);
		_style.setLineColor(Color.red);
		_style.setLineStyle(LineStyle.DASH);
		MagneticFields.getInstance().addMagneticFieldChangeListener(this);
	}

	/**
	 * Always return false for this item.
	 *
	 * @return false--never care if mouse is inside the field.
	 */
	@Override
	public boolean contains(IContainer container, Point screenPoint) {
		return false;
	}

	/**
	 * Custom drawing for the field.
	 *
	 * @param g         the Graphics context.
	 * @param container the rendering container.
	 */
	@Override
	public void drawItem(Graphics g, IContainer container) {

		if (ClasIoEventManager.getInstance().isAccumulating() || _failedToLoad) {
			return;
		}

		if (_activeProbe == null) {
			_activeProbe = FieldProbe.factory();
		}

		if ((_activeProbe == null) || ClasIoEventManager.getInstance().isAccumulating()) {
			return;
		}

		// what is the display option?
		int displayOption = _view.getMagFieldDisplayOption();
		// should be unnecessary
		if (displayOption == MagFieldDisplayArray.NOMAGDISPLAY) {
			return;
		}

		boolean hasTorus = MagneticFields.getInstance().hasActiveTorus();
		boolean hasSolenoid = MagneticFields.getInstance().hasActiveSolenoid();

		if (_view instanceof SectorView) {
			drawItemSectorView(g, container, displayOption, hasTorus, hasSolenoid);
		} else if (_view instanceof MagfieldView) {
			drawItemMagfieldView(g, container, displayOption, hasTorus, hasSolenoid);
		} else if (_view instanceof CentralZView) {
			drawItemCentralZView(g, container, displayOption, hasTorus, hasSolenoid);
		}

	}

	// drawer for Central Z views
	private void drawItemCentralZView(Graphics g, IContainer container, int displayOption, boolean hasTorus,
			boolean hasSolenoid) {

		if (!hasSolenoid) {
			return;
		}

		Rectangle fieldRect = getFieldRect(container, hasTorus, hasSolenoid);

		Rectangle bounds = container.getComponent().getBounds();
		bounds.x = 0;
		bounds.y = 0;

		Rectangle updateRect = bounds.intersection(fieldRect);

		int xsteps = updateRect.width / pixelStep + 1;
		int ysteps = updateRect.height / pixelStep + 1;

		Point2D.Double wp = new Point2D.Double();
		Point pp = new Point();

		int pstep2 = pixelStep / 2;

		float result[] = new float[3];
		double coords[] = new double[5];

		pp.x = updateRect.x + pstep2;
		for (int i = 0; i < xsteps; i++) {
			pp.y = updateRect.y + pstep2;
			for (int j = 0; j < ysteps; j++) {
				container.localToWorld(pp, wp);

				// get the true Cartesian coordinates
				((CentralZView) (_view)).getCLASCordinates(container, pp, wp, coords);

				float x = (float) coords[0];
				float y = (float) coords[1];
				float z = (float) coords[2];
				double phi = coords[4];

				if (displayOption == MagFieldDisplayArray.BMAGDISPLAY) {
					// note conversion to cm from mm
					double bmag = _activeProbe.fieldMagnitude(x / 10, y / 10, z / 10) / 10.;

					Color color = _colorScaleModelSolenoid.getColor(bmag);
					g.setColor(color);
					g.fillRect(pp.x - pstep2, pp.y - pstep2, pixelStep, pixelStep);
				} else if (displayOption == MagFieldDisplayArray.BGRADDISPLAY) {
					_activeProbe.gradient(x, y, z, result);
					double gmag = Math.sqrt(result[0] * result[0] + result[1] * result[1] + result[2] * result[2]);

					// convert to T/m
					gmag *= 10;
					Color color = _colorScaleModelGradient.getColor(gmag);
					g.setColor(color);
					g.fillRect(pp.x - pstep2, pp.y - pstep2, pixelStep, pixelStep);
				} else { // one of the components
							// note conversion to cm from mm
					_activeProbe.field(x / 10, y / 10, z / 10, result);
					double comp = 0.0;
					switch (displayOption) {
					case MagFieldDisplayArray.BXDISPLAY:
						comp = result[0] / 10.;
						break;
					case MagFieldDisplayArray.BYDISPLAY:
						comp = result[1] / 10.;
						break;
					case MagFieldDisplayArray.BZDISPLAY:
						comp = result[2] / 10.;
						break;
					case MagFieldDisplayArray.BPERPDISPLAY:
						// normal vect to sect view is
						// -sin(phi)*i + cos(phi)*j
						double sinp = Math.sin(Math.toRadians(phi));
						double cosp = Math.cos(Math.toRadians(phi));
						comp = (-result[0] * sinp + result[1] * cosp) / 10.;
						break;
					}
					Color color = _colorScaleModelSolenoid.getColor(Math.abs(comp));
					g.setColor(color);

					if (comp > 0) {
						g.fillRect(pp.x - pstep2, pp.y - pstep2, pixelStep, pixelStep);
					} else {
						g.drawRect(pp.x - pstep2, pp.y - pstep2, pixelStep - 1, pixelStep - 1);
					}

				}

				pp.y += pixelStep;
			}
			pp.x += pixelStep;
		} // end for (xsteps)
	}

	private Rectangle getFieldRect(IContainer container, boolean hasTorus, boolean hasSolenoid) {

		Solenoid solenoid = MagneticFields.getInstance().getSolenoid();
		Torus torus = MagneticFields.getInstance().getTorus();

		double solZmin = hasSolenoid ? (solenoid.getZMin() + solenoid.getShiftZ()) : Double.POSITIVE_INFINITY;
		double solZmax = hasSolenoid ? (solenoid.getZMax() + solenoid.getShiftZ()) : Double.NEGATIVE_INFINITY;
		double torZmin = hasTorus ? (torus.getZMin() + torus.getShiftZ()) : Double.POSITIVE_INFINITY;
		double torZmax = hasTorus ? (torus.getZMax() + torus.getShiftZ()) : Double.NEGATIVE_INFINITY;

		double zmin = Math.min(solZmin, torZmin);
		double zmax = Math.max(solZmax, torZmax);

		fieldBoundary = new Rectangle2D.Double();
		GridCoordinate rCoordinate = null;
		if (hasTorus) {
			rCoordinate = MagneticFields.getInstance().getTorus().getRCoordinate();
		} else if (hasSolenoid) {
			rCoordinate = MagneticFields.getInstance().getSolenoid().getRCoordinate();
		}

		if (rCoordinate == null) {
			return new Rectangle();
		}

		fieldBoundary.x = zmin;
		fieldBoundary.width = (zmax - zmin);


		fieldBoundary.y = -rCoordinate.getMax();
		fieldBoundary.height = 2 * rCoordinate.getMax();

		Rectangle fieldRect = new Rectangle();
		container.worldToLocal(fieldRect, fieldBoundary);
		return fieldRect;
	}

	// drawer for sector views
	private void drawItemMagfieldView(Graphics g, IContainer container,
			int displayOption, boolean hasTorus,
			boolean hasSolenoid) {
		drawItemSectorView(g, container, displayOption, hasTorus, hasSolenoid);
	}

	// drawer for sector views
	private void drawItemSectorView(Graphics g, IContainer container, int displayOption, boolean hasTorus,
			boolean hasSolenoid) {

		if (_activeProbe == null) {
			return;
		}

		Rectangle bounds = container.getComponent().getBounds();
		bounds.x = 0;
		bounds.y = 0;

		// get the boundary
		Rectangle fieldRect;

		fieldRect = getFieldRect(container, hasTorus, hasSolenoid);


		Rectangle updateRect = bounds.intersection(fieldRect);

		int xsteps = updateRect.width / pixelStep + 1;
		int ysteps = updateRect.height / pixelStep + 1;

		Point2D.Double wp = new Point2D.Double();
		Point pp = new Point();

		int pstep2 = pixelStep / 2;

		float result[] = new float[3];
		double coords[] = new double[5];

		pp.x = updateRect.x + pstep2;
		for (int i = 0; i < xsteps; i++) {
			pp.y = updateRect.y + pstep2;
			for (int j = 0; j < ysteps; j++) {
				container.localToWorld(pp, wp);

				// get the true Cartesian coordinates
				((SliceView) (_view)).getCLASCordinates(container, pp, wp, coords);

				float x = (float) coords[0];
				float y = (float) coords[1];
				float z = (float) coords[2];
				double phi = coords[4];

				// if (_activeProbe.containsCylindrical(phi, rho, z)) {

				if (displayOption == MagFieldDisplayArray.BMAGDISPLAY) {
					double bmag = _activeProbe.fieldMagnitude(x, y, z) / 10.;

					Color color = _colorScaleModelTorus.getColor(bmag);
					g.setColor(color);
					g.fillRect(pp.x - pstep2, pp.y - pstep2, pixelStep, pixelStep);
				} else if (displayOption == MagFieldDisplayArray.BGRADDISPLAY) {
					_activeProbe.gradient(x, y, z, result);
					double gmag = Math.sqrt(result[0] * result[0] + result[1] * result[1] + result[2] * result[2]);

					// convert to T/m
					gmag *= 10;

					Color color = _colorScaleModelGradient.getColor(gmag);

					if (color.getAlpha() < 255) {
						color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 255);
					}

					g.setColor(color);
					g.fillRect(pp.x - pstep2, pp.y - pstep2, pixelStep, pixelStep);

				} else { // one of the components
					_activeProbe.field(x, y, z, result);
					double comp = 0.0;
					switch (displayOption) {
					case MagFieldDisplayArray.BXDISPLAY:
						comp = result[0] / 10.;
						break;
					case MagFieldDisplayArray.BYDISPLAY:
						comp = result[1] / 10.;
						break;
					case MagFieldDisplayArray.BZDISPLAY:
						comp = result[2] / 10.;
						break;
					case MagFieldDisplayArray.BPERPDISPLAY:
						// normal vect to sect view is
						// -sin(phi)*i + cos(phi)*j
						double sinp = Math.sin(Math.toRadians(phi));
						double cosp = Math.cos(Math.toRadians(phi));
						comp = (-result[0] * sinp + result[1] * cosp) / 10.;
						break;

					}
					Color color = _colorScaleModelTorus.getColor(Math.abs(comp));
					g.setColor(color);

					// distinguish positive and negative
					if (comp > 0) {
						g.fillRect(pp.x - pstep2, pp.y - pstep2, pixelStep, pixelStep);
					} else {
						// g.drawRect(pp.x - pstep2, pp.y - pstep2,
						// pixelStep -
						// 1,
						// pixelStep - 1);
						g.fillOval(pp.x - pstep2, pp.y - pstep2, pixelStep, pixelStep);
					}

				} // a component
				// } //active probe contains

				pp.y += pixelStep;
			}
			pp.x += pixelStep;
		} // end for (xsteps)
	}

	/**
	 * Checks whether the item should be drawn. This is an additional check, beyond
	 * the simple visibility flag check. For example, it might check whether the
	 * item intersects the area being drawn.
	 *
	 * @param g         the graphics context.
	 * @param container the graphical container being rendered.
	 * @return <code>true</code> if the item passes any and all tests, and should be
	 *         drwan.
	 */
	@Override
	public boolean shouldDraw(Graphics g, IContainer container) {

		if (fieldBoundary == null) {
			return true;
		}
		Rectangle r = new Rectangle();
		container.worldToLocal(r, fieldBoundary);
		return container.getComponent().getBounds().intersects(r);
	}

	private static double[] getGradientValues() {
		return getNonlinearValues(getGradientColors().length + 1, 15.0);
	}

	/**
	 * Get the values array for the plot.
	 *
	 * @return the values array.
	 */
	private static double[] getTorusValues() {

		double max = MagneticFields.getInstance().maxFieldMagnitude() / 10.0;
		return getNonlinearValues(getTorusColors().length + 1, max);
	}

	/**
	 * Get the values array for the plot.
	 *
	 * @return the values array.
	 */
	private static double[] getSolenoidValues() {

		double max = MagneticFields.getInstance().maxFieldMagnitude() / 10.0;
		return getNonlinearValues(getSolenoidColors().length + 1, max);
	}

	private static double[] getNonlinearValues(int length, double max) {
		double[] values = new double[length];
		if (max <= 0.0) {
			return values;
		}

		double speedup = 6.0;
		double denominator = Math.expm1(speedup);
		for (int i = 1; i < length; i++) {
			double fraction = (double) i / (length - 1);
			values[i] = max * Math.expm1(speedup * fraction) / denominator;
		}
		return values;
	}

	private static Color[] getGradientColors() {
		return ScientificColorScales.sample(ScientificColorMap.VIRIDIS);
	}


	/**
	 * Get the color array for the plot.
	 *
	 * @return the color array for the plot.
	 */
	private static Color[] getTorusColors() {
		return ScientificColorScales.sample(ScientificColorMap.VIRIDIS);
	}

	/**
	 * Get the color array for the plot.
	 *
	 * @return the color array for the plot.
	 */
	private static Color[] getSolenoidColors() {
		return ScientificColorScales.sample(ScientificColorMap.VIRIDIS);
	}

	@Override
	public void modify() {
	}

	@Override
	public Rectangle2D.Double getWorldBounds() {
		return null;
	}

	@Override
	public void magneticFieldChanged() {
		_activeProbe = FieldProbe.factory();
	}

}
