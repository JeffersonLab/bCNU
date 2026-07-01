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
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.ced.cedview.alert.AlertXYView;
import cnuphys.ced.geometry.cache.GeometryPrimitiveIO;

/**
 * Runtime geometry and drawing support for one ALERT time-of-flight layer.
 * <p>
 * The authoritative geometry-service constructor still accepts an
 * {@link AlertTOFLayer}, but the cached representation is explicit primitive
 * paddle geometry:
 *
 * <pre>
 * paddleCorners[paddle][corner][xyz]
 * projectionEdges[paddle][edge][endpoint][xyz]
 * componentIds[paddle]
 * </pre>
 *
 * Drawing and feedback use 0-based paddle indices so they continue to work after
 * cache reads, when no live {@link ScintillatorPaddle} objects are retained.
 */
public class TOFLayer {

	/** Number of volume corners per paddle. */
	private static final int CORNER_COUNT = 8;

	/** Number of coordinates per 3D point. */
	private static final int COORD_COUNT = 3;

	/** First JLab volume edge used for projected-polygon drawing. */
	private static final int PROJECTION_EDGE_START = 6;

	/** Number of projected-polygon edges. */
	private static final int PROJECTION_EDGE_COUNT = 4;

	/** Number of endpoints per edge. */
	private static final int EDGE_ENDPOINT_COUNT = 2;

	/** Superlayer zero fill color. */
	private static Color superlayer0Color = X11Colors.getX11Color("Alice Blue");

	/** Fill colors for other layers. */
	private static Color[][] fillColors = {
			{ X11Colors.getX11Color("Antique White"), X11Colors.getX11Color("Burlywood", 128) },
			{ X11Colors.getX11Color("Light Cyan"), X11Colors.getX11Color("Light Blue", 128) },
			{ X11Colors.getX11Color("Aquamarine"), X11Colors.getX11Color("Light Green", 128) } };

	/** Work points. */
	private Point2D.Double wp[] = new Point2D.Double[4];

	/** Work pixel point. */
	private Point pp = new Point();

	/** The 0-based sector of this layer. */
	public final int sector;

	/** The 0-based superlayer of this layer. */
	public final int superlayer;

	/** The 0-based layer id of this layer. */
	public final int layer;

	/** Number of paddles. */
	public final int numPaddles;

	/** Legacy JLab paddles, available only after CCDB initialization. */
	public List<ScintillatorPaddle> paddles;

	/** Primitive paddle corner data shaped [paddle][corner][xyz]. */
	private double paddleCorners[][][];

	/** Primitive projection edge data shaped [paddle][edge][endpoint][xyz]. */
	private double projectionEdges[][][][];

	/** Component ids for paddles. */
	private int componentIds[];

	/** Used by feedback, keyed by 0-based paddle index. */
	private HashMap<Integer, Polygon> polyhash = new HashMap<>();

	/** Inner radius used by feedback. */
	private double _innerRad;

	/** Outer radius used by feedback. */
	private double _outerRad;

	/** Layer radial spacing adjustment for unrealistic mode. */
	private double _deltaR = Double.NaN;

	/**
	 * Create a TOF layer from an ALERT TOF geometry-service layer.
	 *
	 * @param geoAlertTOFLayer the geometry-service ALERT TOF layer
	 */
	public TOFLayer(AlertTOFLayer geoAlertTOFLayer) {
		sector = geoAlertTOFLayer.getSectorId();
		superlayer = geoAlertTOFLayer.getSuperlayerId();
		layer = geoAlertTOFLayer.getLayerId();
		numPaddles = geoAlertTOFLayer.getNumComponents();

		paddles = geoAlertTOFLayer.getAllComponents();

		paddleCorners = new double[numPaddles][CORNER_COUNT][COORD_COUNT];
		projectionEdges = new double[numPaddles][PROJECTION_EDGE_COUNT][EDGE_ENDPOINT_COUNT][COORD_COUNT];
		componentIds = new int[numPaddles];

		for (int paddleId = 0; paddleId < numPaddles; paddleId++) {
			ScintillatorPaddle paddle = paddles.get(paddleId);
			componentIds[paddleId] = paddle.getComponentId();
			cachePaddle(paddleId, paddle);
		}

		setLimitValues();
	}

	/**
	 * Create a TOF layer from primitive cache data.
	 *
	 * @param sector          the 0-based sector
	 * @param superlayer      the 0-based superlayer
	 * @param layer           the 0-based layer
	 * @param componentIds    component ids indexed by paddle
	 * @param paddleCorners   primitive paddle corners
	 * @param projectionEdges primitive projection edges
	 */
	private TOFLayer(int sector, int superlayer, int layer, int componentIds[], double paddleCorners[][][],
			double projectionEdges[][][][]) {
		this.sector = sector;
		this.superlayer = superlayer;
		this.layer = layer;
		this.componentIds = componentIds;
		this.paddleCorners = paddleCorners;
		this.projectionEdges = projectionEdges;
		this.numPaddles = (paddleCorners == null) ? 0 : paddleCorners.length;

		paddles = null;

		setLimitValues();
	}

	/**
	 * Cache one JLab paddle into primitive arrays.
	 *
	 * @param paddleId the 0-based paddle id
	 * @param paddle   the source paddle
	 */
	private void cachePaddle(int paddleId, ScintillatorPaddle paddle) {
		for (int corner = 0; corner < CORNER_COUNT; corner++) {
			Point3D point = paddle.getVolumePoint(corner);
			paddleCorners[paddleId][corner][0] = point.x();
			paddleCorners[paddleId][corner][1] = point.y();
			paddleCorners[paddleId][corner][2] = point.z();
		}

		for (int edge = 0; edge < PROJECTION_EDGE_COUNT; edge++) {
			Line3D line = paddle.getVolumeEdge(PROJECTION_EDGE_START + edge);
			copyPoint(line.origin(), projectionEdges[paddleId][edge][0]);
			copyPoint(line.end(), projectionEdges[paddleId][edge][1]);
		}
	}

	/**
	 * Copy a JLab point to primitive coordinates.
	 *
	 * @param point  the source point
	 * @param coords the destination coordinate array
	 */
	private static void copyPoint(Point3D point, double coords[]) {
		coords[0] = point.x();
		coords[1] = point.y();
		coords[2] = point.z();
	}

	/**
	 * Get a live geometry-service paddle.
	 * <p>
	 * This is a legacy CCDB-only method. After cache reads, the geometry-service
	 * paddle list is not retained and this method returns {@code null}.
	 *
	 * @param paddleId 0-based paddle index
	 * @return the live paddle, or {@code null}
	 */
	public ScintillatorPaddle getPaddle(int paddleId) {
		if ((paddles == null) || (paddleId < 0) || (paddleId >= numPaddles)) {
			return null;
		}

		try {
			return paddles.get(paddleId);
		} catch (Exception e) {
			System.err.println("Exception in TOFLayer.getPaddle: " + e);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Get a primitive paddle corner.
	 *
	 * @param paddleId the 0-based paddle id
	 * @param corner   the corner index
	 * @return the primitive corner
	 */
	public double[] getCorner(int paddleId, int corner) {
		if (!validPaddle(paddleId) || (corner < 0) || (corner >= CORNER_COUNT)) {
			return null;
		}

		return paddleCorners[paddleId][corner];
	}

	/**
	 * Get the primitive projection edges for a paddle.
	 *
	 * @param paddleId the 0-based paddle id
	 * @return the projection edges shaped [edge][endpoint][xyz], or {@code null}
	 */
	public double[][][] getProjectionEdges(int paddleId) {
		if (!validPaddle(paddleId)) {
			return null;
		}

		return projectionEdges[paddleId];
	}

	/**
	 * Get the component id for a paddle.
	 *
	 * @param paddleId the 0-based paddle id
	 * @return the component id
	 */
	public int getComponentId(int paddleId) {
		if ((componentIds == null) || (paddleId < 0) || (paddleId >= componentIds.length)) {
			return paddleId;
		}

		return componentIds[paddleId];
	}

	/**
	 * Get a projected paddle polygon in world coordinates.
	 *
	 * @param view     the ALERT XY view
	 * @param paddleId the 0-based paddle id
	 * @return the world polygon, or {@code null}
	 */
	private Point2D.Double[] getWorldPolygon(AlertXYView view, int paddleId) {
		return AlertGeometry.getIntersections(sector, superlayer, layer, paddleId, view.getProjectionPlane(), true);
	}

	/**
	 * Draw all ATOF paddles in this layer.
	 *
	 * @param g         the graphics context
	 * @param container the drawing container
	 */
	public void drawAllATOFPaddles(Graphics g, IContainer container) {
		polyhash.clear();

		for (int paddleId = 0; paddleId < numPaddles; paddleId++) {
			Color fc;

			if (superlayer == 0) {
				fc = superlayer0Color;
			} else {
				fc = fillColors[sector % 3][getComponentId(paddleId) % 2];
			}

			drawPaddle(g, container, paddleId, fc, fc.darker());
		}
	}

	/**
	 * Set radial limit values from primitive paddle geometry.
	 */
	private void setLimitValues() {
		if ((paddleCorners == null) || (paddleCorners.length < 1)) {
			_innerRad = 0;
			_outerRad = 0;
			return;
		}

		double p0[] = paddleCorners[0][0];
		_innerRad = Math.hypot(p0[0], p0[1]);

		double p1[] = paddleCorners[0][1];
		_outerRad = Math.hypot(p1[0], p1[1]);

		if (superlayer == 1) {
			_deltaR = (_outerRad - _innerRad) / 10.0;
		}
	}

	/**
	 * Does a paddle polygon contain a pixel point?
	 *
	 * @param paddleId the 0-based paddle id
	 * @param pp       the pixel point
	 * @return {@code true} if contained
	 */
	public boolean paddleContains(int paddleId, Point pp) {
		Polygon poly = polyhash.get(paddleId);
		return (poly != null) && poly.contains(pp);
	}

	/**
	 * Legacy paddle containment method.
	 *
	 * @param paddle the live paddle
	 * @param pp     the pixel point
	 * @return {@code true} if contained
	 */
	public boolean paddleContains(ScintillatorPaddle paddle, Point pp) {
		int paddleId = paddleIndex(paddle);
		return (paddleId >= 0) && paddleContains(paddleId, pp);
	}

	/**
	 * Draw a paddle.
	 *
	 * @param g         the graphics context
	 * @param container the drawing container
	 * @param paddleId  the 0-based paddle id
	 * @param fillColor the fill color
	 * @param lineColor the outline color
	 */
	public void drawPaddle(Graphics g, IContainer container, int paddleId, Color fillColor, Color lineColor) {
		if (!validPaddle(paddleId)) {
			return;
		}

		AlertXYView view = (AlertXYView) container.getView();

		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Polygon poly = new Polygon();

		if (Double.isNaN(_deltaR)) {
			setLimitValues();
		}

		if (view.showAllTOF()) {
			for (int i = 0; i < 4; i++) {
				double corner[] = paddleCorners[paddleId][i];

				wp[i] = new Point2D.Double(corner[0], corner[1]);

				if (superlayer == 1) {
					int componentId = getComponentId(paddleId);

					if ((i == 0) || (i == 3)) {
						double dR = componentId * _deltaR;
						shiftPoint(wp[i], dR);
					} else {
						double dR = -(9 - componentId) * _deltaR;
						shiftPoint(wp[i], dR);
					}
				}

				container.worldToLocal(pp, wp[i]);
				poly.addPoint(pp.x, pp.y);
			}
		} else {
			boolean intersects = AlertGeometry.doesProjectedPolyFullyIntersect(sector, superlayer, layer, paddleId,
					view.getProjectionPlane());

			if (!intersects) {
				return;
			}

			Point2D.Double worldPoly[] = getWorldPolygon(view, paddleId);
			if (worldPoly != null) {
				Point pp = new Point();

				for (Point2D.Double element : worldPoly) {
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

		polyhash.put(paddleId, poly);
	}

	/**
	 * Legacy draw method using a live geometry-service paddle.
	 *
	 * @param g         the graphics context
	 * @param container the drawing container
	 * @param paddle    the live paddle
	 * @param fillColor the fill color
	 * @param lineColor the line color
	 */
	public void drawPaddle(Graphics g, IContainer container, ScintillatorPaddle paddle, Color fillColor,
			Color lineColor) {
		int paddleId = paddleIndex(paddle);

		if (paddleId >= 0) {
			drawPaddle(g, container, paddleId, fillColor, lineColor);
		}
	}

	/**
	 * Find the 0-based index for a live paddle.
	 *
	 * @param paddle the live paddle
	 * @return the 0-based paddle index, or -1
	 */
	private int paddleIndex(ScintillatorPaddle paddle) {
		if (paddle == null) {
			return -1;
		}

		if (paddles != null) {
			int index = paddles.indexOf(paddle);
			if (index >= 0) {
				return index;
			}
		}

		int componentId = paddle.getComponentId();

		for (int i = 0; i < numPaddles; i++) {
			if (getComponentId(i) == componentId) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * Shift a world point radially.
	 *
	 * @param wp the world point
	 * @param dR radial shift
	 */
	private static void shiftPoint(Point2D.Double wp, double dR) {
		double r = Math.hypot(wp.x, wp.y);
		double theta = Math.atan2(wp.y, wp.x);

		r += dR;
		wp.x = r * Math.cos(theta);
		wp.y = r * Math.sin(theta);
	}

	/**
	 * Basic feedback string for XY view.
	 *
	 * @param pp              the pixel point
	 * @param wp              the world point
	 * @param feedbackStrings the feedback list
	 * @return {@code true} if feedback was added
	 */
	public boolean feedbackXYString(Point pp, Point2D.Double wp, List<String> feedbackStrings) {
		double rad = Math.hypot(wp.x, wp.y);

		if ((rad < _innerRad) || (rad > _outerRad)) {
			return false;
		}

		for (int paddleId = 0; paddleId < numPaddles; paddleId++) {
			Polygon poly = polyhash.get(paddleId);

			if ((poly != null) && poly.contains(pp)) {
				feedbackStrings.add(String.format("TOF sector: %d (0-based)", sector));
				feedbackStrings.add(String.format("TOF superlayer: %d (0-based)", superlayer));
				feedbackStrings.add(String.format("TOF layer: %d (0-based)", layer));
				feedbackStrings.add(String.format("TOF paddle: %d (0-based)", getComponentId(paddleId)));
				return true;
			}
		}

		return false;
	}

	/**
	 * Write this TOF layer to the cache using explicit primitive geometry.
	 *
	 * @param output the cache output stream
	 */
	public void writeToCache(Output output) {
		output.writeInt(sector);
		output.writeInt(superlayer);
		output.writeInt(layer);
		output.writeInt(numPaddles);

		for (int paddleId = 0; paddleId < numPaddles; paddleId++) {
			output.writeInt(getComponentId(paddleId));

			GeometryPrimitiveIO.writeCorners(output, paddleCorners[paddleId], CORNER_COUNT, COORD_COUNT);
			writeProjectionEdges(output, projectionEdges[paddleId]);
		}
	}

	/**
	 * Read a TOF layer from the cache.
	 *
	 * @param input the cache input stream
	 * @return the reconstructed TOF layer
	 */
	public static TOFLayer readFromCache(Input input) {
		int sector = input.readInt();
		int superlayer = input.readInt();
		int layer = input.readInt();
		int numPaddles = input.readInt();

		if (numPaddles < 0) {
			throw new IllegalArgumentException("ALERT TOF: negative paddle count in cache.");
		}

		int componentIds[] = new int[numPaddles];
		double paddleCorners[][][] = new double[numPaddles][][];
		double projectionEdges[][][][] = new double[numPaddles][][][];

		for (int paddleId = 0; paddleId < numPaddles; paddleId++) {
			componentIds[paddleId] = input.readInt();

			String context = "ALERT TOF sector " + sector + " superlayer " + superlayer + " layer " + layer
					+ " paddle " + paddleId;

			paddleCorners[paddleId] = GeometryPrimitiveIO.readCorners(input, CORNER_COUNT, COORD_COUNT,
					context + " corners");
			projectionEdges[paddleId] = readProjectionEdges(input, context + " projection");
		}

		return new TOFLayer(sector, superlayer, layer, componentIds, paddleCorners, projectionEdges);
	}

	/**
	 * Write primitive projection edges.
	 *
	 * @param output the cache output stream
	 * @param edges  edge data shaped [edge][endpoint][xyz]
	 */
	private static void writeProjectionEdges(Output output, double edges[][][]) {
		if ((edges == null) || (edges.length != PROJECTION_EDGE_COUNT)) {
			throw new IllegalArgumentException("ALERT TOF: invalid projection edge array.");
		}

		output.writeInt(PROJECTION_EDGE_COUNT);

		for (int edge = 0; edge < PROJECTION_EDGE_COUNT; edge++) {
			GeometryPrimitiveIO.writeCorners(output, edges[edge], EDGE_ENDPOINT_COUNT, COORD_COUNT);
		}
	}

	/**
	 * Read primitive projection edges.
	 *
	 * @param input   the cache input stream
	 * @param context context for error messages
	 * @return projection edge data shaped [edge][endpoint][xyz]
	 */
	private static double[][][] readProjectionEdges(Input input, String context) {
		int edgeCount = input.readInt();

		if (edgeCount != PROJECTION_EDGE_COUNT) {
			throw new IllegalArgumentException(
					context + ": expected " + PROJECTION_EDGE_COUNT + " projection edges, found " + edgeCount);
		}

		double edges[][][] = new double[PROJECTION_EDGE_COUNT][][];

		for (int edge = 0; edge < PROJECTION_EDGE_COUNT; edge++) {
			edges[edge] = GeometryPrimitiveIO.readCorners(input, EDGE_ENDPOINT_COUNT, COORD_COUNT,
					context + " edge " + edge);
		}

		return edges;
	}

	/**
	 * Check whether a paddle id is valid and primitive geometry exists.
	 *
	 * @param paddleId the 0-based paddle id
	 * @return {@code true} if valid
	 */
	private boolean validPaddle(int paddleId) {
		return (paddleId >= 0) && (paddleId < numPaddles) && (paddleCorners != null) && (projectionEdges != null)
				&& (paddleId < paddleCorners.length) && (paddleId < projectionEdges.length)
				&& (paddleCorners[paddleId] != null) && (projectionEdges[paddleId] != null);
	}
}