package cnuphys.ced.geometry.alert;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.List;

import org.jlab.geom.component.ScintillatorPaddle;
import org.jlab.geom.detector.alert.ATOF.AlertTOFLayer;
import org.jlab.geom.prim.Point3D;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.ced.cedview.alert.AlertXYView;
import cnuphys.lund.X11Colors;

public class TOFLayer {

	private static Color superlayer0Color = X11Colors.getX11Color("Alice Blue");
	private static Color[][] fillColors = {
			{ X11Colors.getX11Color("Antique White"), X11Colors.getX11Color("Burlywood", 128) },
			{ X11Colors.getX11Color("Light Cyan"), X11Colors.getX11Color("Light Blue", 128) },
			{ X11Colors.getX11Color("Aquamarine"), X11Colors.getX11Color("Light Green", 128) } };

	//work points
	private Point2D.Double wp[] = new Point2D.Double[4];
	private Point pp = new Point();

	/** the 0-based sector of this layer */
	public final int sector;

	/** the 0-based superlayer of this layer */
	public final int superlayer;

	/** the 0-based layer ID of this layer */
	public final int layer;

	/** the number of paddles */
	public final int numPaddles;

	//radii are only geo accurate for superlayer 0
	//for superlayer 1 they are modified to show all 10 layers
	private double _innerRad;
	private double _outerRad;

	private static double _deltaR = Double.NaN;


	//all the paddle objects
	public List<ScintillatorPaddle> paddles;

	//used by feedback
	HashMap<ScintillatorPaddle, Polygon> polyhash = new HashMap<>();


	/**
	 * Create a TOF layer from an alert TOF layer
	 *
	 * @param geoAlertTOFLayer the alert TOF layer from the geometry service
	 */
	public TOFLayer(AlertTOFLayer geoAlertTOFLayer) {
		sector = geoAlertTOFLayer.getSectorId();
		superlayer = geoAlertTOFLayer.getSuperlayerId();
		layer = geoAlertTOFLayer.getLayerId();
		numPaddles = geoAlertTOFLayer.getNumComponents();

		paddles = geoAlertTOFLayer.getAllComponents();

		setLimitValues();
	}

	/**
	 * Get a paddle from this layer
	 * @param paddleId 0-based INDEX (not the component ID)
	 * @return the paddle
	 */
	public ScintillatorPaddle getPaddle(int paddleId) {
		try {
			ScintillatorPaddle paddle = paddles.get(paddleId);
			return paddle;
		}
		catch (Exception e) {
            System.err.println("Exception in TOFLayer.getPaddle: " + e);
            e.printStackTrace();
        }
		return null;
	}


	/**
	 * Get a strip outline
	 * @param view the view
	 * @param paddle the paddle from the geometry service
	 * @return the paddle outline in world coordinates
	 */
	private Point2D.Double[] getWorldPolygon(AlertXYView view, ScintillatorPaddle paddle) {

		Point2D.Double wp[] = AlertGeometry.getIntersections(sector, superlayer, layer, paddle,
				view.getProjectionPlane(), true);

		return wp;
	}


	/**
	 * Draw all the paddles in this layer
	 *
	 * @param g         the graphics context
	 * @param container the container
	 */
	public void drawAllATOFPaddles(Graphics g, IContainer container) {
		//the hash is used for feedback
		polyhash.clear();

		for (int paddleId = 0; paddleId < numPaddles; paddleId++) {

			ScintillatorPaddle paddle = paddles.get(paddleId);
			Color fc;
			if (superlayer == 0) {
				fc = superlayer0Color;
			} else {
				fc = fillColors[sector % 3][paddle.getComponentId() % 2];
			}
			
			

			drawPaddle(g, container, paddle, fc, fc.darker());
		}
	}

	private void setLimitValues()  {
		ScintillatorPaddle paddle = paddles.get(0);

		Point3D p3d = paddle.getVolumePoint(0);
		_innerRad = Math.hypot(p3d.x(), p3d.y());

		p3d = paddle.getVolumePoint(1);
		_outerRad = Math.hypot(p3d.x(), p3d.y());

		if (superlayer == 1) {
			_deltaR = (_outerRad - _innerRad)/10;

			if (Double.isNaN(_deltaR)) {
				_deltaR = (_outerRad - _innerRad)/10;
			}
		}

	}

	/**
	 * Does the paddle contain the point? This uses the polyhash
	 * @param paddle
	 * @param pp
	 * @return <code>true</code> if the paddle contains the point
	 */
	public boolean paddleContains(ScintillatorPaddle paddle, Point pp) {
		Polygon poly = polyhash.get(paddle);
		return (poly != null) && poly.contains(pp);
	}

	/**
	 * Draw a paddle in the unrealistic mode
	 * @param g
	 * @param container
	 * @param paddle
	 * @param fillColor
	 * @param lineColor
	 */
	public  void drawPaddle(Graphics g, IContainer container, ScintillatorPaddle paddle, Color fillColor, Color lineColor) {

		AlertXYView view = (AlertXYView) container.getView();

		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Polygon poly = new Polygon();
		int paddleId = paddle.getComponentId();
		
		if (Double.isNaN(_deltaR)) {
			setLimitValues();
		}


		if (view.showAllTOF()) { //unrealistic

			for (int i = 0; i < 4; i++) { //for corners of paddle

				double x = paddle.getVolumePoint(i).x();
				double y = paddle.getVolumePoint(i).y();
				
				wp[i] = new Point2D.Double(x, y);

				// shift the point if not superlayer 0

				if (superlayer == 1) {
					if ((i == 0) || (i == 3)) {
						double dR = paddleId * _deltaR;
						shiftPoint(wp[i], dR);
					} else { // outer
						double dR = -(9 - paddleId) * _deltaR;
						shiftPoint(wp[i], dR);
					}
				}

				container.worldToLocal(pp, wp[i]);
				poly.addPoint(pp.x, pp.y);
			}

		}
		else {
			boolean intersects = AlertGeometry.doesProjectedPolyFullyIntersect(sector, superlayer, layer,
					paddle, view.getProjectionPlane());

			if (!intersects) {
				return;
			}

			Point2D.Double wp[] = getWorldPolygon(view, paddle);
			if (wp != null) {
				Point pp = new Point();
				for (java.awt.geom.Point2D.Double element : wp) {
                    container.worldToLocal(pp, element);
                    poly.addPoint(pp.x, pp.y);
				}
			}

		}

		if (fillColor != null) {
			g.setColor(fillColor);
			g.fillPolygon(poly);
		}
		g.setColor(lineColor);
		g.drawPolygon(poly);
		polyhash.put(paddle, poly);
	}


	//used for unrealistic mode
	private static void shiftPoint(Point2D.Double wp, double dR) {
		double r = Math.hypot(wp.x, wp.y);
		double theta = Math.atan2(wp.y, wp.x);

		r += dR;
		wp.x = r*Math.cos(theta);
		wp.y = r*Math.sin(theta);
	}

	/**
	 * Basic fb string for XY view
	 * @param feedbackStrings list to add to
	 */
	public boolean feedbackXYString(Point pp, Point2D.Double wp, List<String> feedbackStrings) {

		double rad = Math.hypot(wp.x, wp.y);
		if ((rad < _innerRad) || (rad > _outerRad)) {
			return false;
		}

		for (ScintillatorPaddle paddle : paddles) {
			Polygon poly = polyhash.get(paddle);

			if ((poly != null) && poly.contains(pp)) {
				feedbackStrings.add(String.format("TOF sector: %d (0-based)", sector));
				feedbackStrings.add(String.format("TOF superlayer: %d (0-based)", superlayer));
				feedbackStrings.add(String.format("TOF layer: %d (0-based)", layer ));
				feedbackStrings.add(String.format("TOF paddle: %d (0-based)", paddle.getComponentId()));
				return true;
			}
		}

		return false;

	}
}
