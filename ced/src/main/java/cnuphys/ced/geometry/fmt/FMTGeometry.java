package cnuphys.ced.geometry.fmt;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import org.jlab.detector.base.GeometryFactory;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.component.TrackerStrip;
import org.jlab.geom.detector.fmt.FMTDetector;
import org.jlab.geom.detector.fmt.FMTFactory;
import org.jlab.geom.detector.fmt.FMTLayer;
import org.jlab.geom.detector.fmt.FMTSector;
import org.jlab.geom.detector.fmt.FMTSuperlayer;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import cnuphys.ced.frame.Ced;
import cnuphys.ced.geometry.cache.ACachedGeometry;
import cnuphys.ced.geometry.cache.GeometryPrimitiveIO;

/**
 * Geometry support for the Forward Micromegas Tracker (FMT).
 * <p>
 * FMT geometry is obtained from the JLab geometry factory when initialized from
 * CCDB. Historically, CED cached the JLab {@link FMTLayer} object graph directly
 * by serializing a {@code HashMap<String, FMTLayer>} through Kryo. This class now
 * keeps that object graph only as a CCDB-time legacy convenience and caches the
 * geometry in explicit primitive form instead.
 * <p>
 * The primitive cached geometry is stored as:
 *
 * <pre>
 * _stripCorners[sector][superlayer][layer][strip][corner][xyz]
 * _stripLinePoints[sector][superlayer][layer][strip][endpoint][xyz]
 * </pre>
 *
 * The corner data supports 3D drawing through {@link #stripVertices(int, int, int, int, float[])}.
 * The line endpoint data is available for a future FMT XY view that should not
 * depend on live JLab {@link TrackerStrip} objects.
 */
public class FMTGeometry extends ACachedGeometry {

	/**
	 * The detector name.
	 */
	public static final String NAME = "FMT";

	/**
	 * Current FMT sector count used by CED.
	 */
	private static final int SECTOR_COUNT = 1;

	/**
	 * Current FMT superlayer count used by CED.
	 */
	private static final int SUPERLAYER_COUNT = 1;

	/**
	 * Number of FMT layers.
	 */
	private static final int LAYER_COUNT = 6;

	/**
	 * Number of strips per FMT layer.
	 */
	private static final int STRIP_COUNT = 1024;

	/**
	 * Number of 3D volume corners for one strip.
	 */
	private static final int CORNER_COUNT = 8;

	/**
	 * Number of coordinates per point.
	 */
	private static final int COORD_COUNT = 3;

	/**
	 * Number of endpoints for a strip line.
	 */
	private static final int LINE_ENDPOINT_COUNT = 2;

	/**
	 * Legacy JLab geometry layer objects.
	 * <p>
	 * These are available only after direct CCDB initialization. After cache
	 * initialization, this map is set to {@code null}; runtime drawing should use
	 * the primitive arrays instead.
	 */
	private static HashMap<String, FMTLayer> _fmtLayers = new HashMap<>();

	/**
	 * Explicit primitive strip corner geometry.
	 * <p>
	 * Index order:
	 *
	 * <pre>
	 * [sector][superlayer][layer][strip][corner][xyz]
	 * </pre>
	 */
	private static double _stripCorners[][][][][][];

	/**
	 * Explicit primitive strip line geometry.
	 * <p>
	 * Index order:
	 *
	 * <pre>
	 * [sector][superlayer][layer][strip][endpoint][xyz]
	 * </pre>
	 *
	 * Endpoint 0 is the line origin. Endpoint 1 is the line end.
	 */
	private static double _stripLinePoints[][][][][][];

	/**
	 * Constructor.
	 */
	public FMTGeometry() {
		super("FMT");
	}

	/**
	 * Initialize FMT geometry from CCDB/JLab geometry services.
	 */
	@Override
	public void initializeUsingCCDB() {
		System.out.println("\n=======================================");
		System.out.println("===  " + NAME + " Geometry Initialization ===");
		System.out.println("=======================================");

		String variation = Ced.getGeometryVariation();
		ConstantProvider constantProvider = GeometryFactory.getConstants(org.jlab.detector.base.DetectorType.FMT, 11,
				variation);

		initialize(constantProvider);
	}

	/**
	 * Initialize FMT geometry from a constant provider.
	 *
	 * @param cp the geometry constants
	 */
	private static void initialize(ConstantProvider cp) {

		_fmtLayers = new HashMap<>();
		_stripCorners = new double[SECTOR_COUNT][SUPERLAYER_COUNT][LAYER_COUNT][STRIP_COUNT][][];
		_stripLinePoints = new double[SECTOR_COUNT][SUPERLAYER_COUNT][LAYER_COUNT][STRIP_COUNT][][];

		FMTFactory fmtFactory = new FMTFactory();
		FMTDetector fmtDetector = fmtFactory.createDetectorCLAS(cp);

		int numsect = Math.min(fmtDetector.getNumSectors(), SECTOR_COUNT);

		for (int sect = 0; sect < numsect; sect++) {

			FMTSector fmtSector = fmtFactory.createSector(cp, sect);
			int numsupl = Math.min(fmtSector.getNumSuperlayers(), SUPERLAYER_COUNT);

			for (int superlayer = 0; superlayer < numsupl; superlayer++) {

				FMTSuperlayer fmtSuperlayer = fmtFactory.createSuperlayer(cp, sect, superlayer);
				int numlay = Math.min(fmtSuperlayer.getNumLayers(), LAYER_COUNT);

				for (int layer = 0; layer < numlay; layer++) {
					FMTLayer fmtLayer = fmtFactory.createLayer(cp, sect, superlayer, layer);

					_fmtLayers.put(hash(sect, superlayer, layer), fmtLayer);
					cacheLayerPrimitiveGeometry(sect, superlayer, layer, fmtLayer);
				}
			}
		}
	}

	/**
	 * Copy one FMT layer from the JLab object representation into the explicit
	 * primitive geometry arrays.
	 *
	 * @param sector     the 0-based sector
	 * @param superlayer the 0-based superlayer
	 * @param layer      the 0-based layer
	 * @param fmtLayer   the JLab FMT layer
	 */
	private static void cacheLayerPrimitiveGeometry(int sector, int superlayer, int layer, FMTLayer fmtLayer) {
		if (!validLayerAddress(sector, superlayer, layer) || (fmtLayer == null)) {
			return;
		}

		for (int strip = 0; strip < STRIP_COUNT; strip++) {
			TrackerStrip trackerStrip = null;

			try {
				trackerStrip = fmtLayer.getComponent(strip);
			} catch (Exception e) {
				trackerStrip = null;
			}

			if (trackerStrip != null) {
				cacheStripPrimitiveGeometry(sector, superlayer, layer, strip, trackerStrip);
			}
		}
	}

	/**
	 * Copy one FMT strip from the JLab object representation into the explicit
	 * primitive geometry arrays.
	 *
	 * @param sector     the 0-based sector
	 * @param superlayer the 0-based superlayer
	 * @param layer      the 0-based layer
	 * @param strip      the 0-based strip
	 * @param trackerStrip the JLab tracker strip
	 */
	private static void cacheStripPrimitiveGeometry(int sector, int superlayer, int layer, int strip,
			TrackerStrip trackerStrip) {

		if (!validStripAddress(sector, superlayer, layer, strip) || (trackerStrip == null)) {
			return;
		}

		double corners[][] = new double[CORNER_COUNT][COORD_COUNT];

		for (int corner = 0; corner < CORNER_COUNT; corner++) {
			Point3D point = trackerStrip.getVolumePoint(corner);
			corners[corner][0] = point.x();
			corners[corner][1] = point.y();
			corners[corner][2] = point.z();
		}

		_stripCorners[sector][superlayer][layer][strip] = corners;

		Line3D line = trackerStrip.getLine();
		double linePoints[][] = new double[LINE_ENDPOINT_COUNT][COORD_COUNT];

		copyPoint(line.origin(), linePoints[0]);
		copyPoint(line.end(), linePoints[1]);

		_stripLinePoints[sector][superlayer][layer][strip] = linePoints;
	}

	/**
	 * Copy a JLab point into a primitive coordinate array.
	 *
	 * @param point  the JLab point
	 * @param coords the primitive coordinate array, length at least 3
	 */
	private static void copyPoint(Point3D point, double coords[]) {
		coords[0] = point.x();
		coords[1] = point.y();
		coords[2] = point.z();
	}

	/**
	 * Get a tracker strip.
	 * <p>
	 * This is a legacy convenience method. It returns a JLab {@link TrackerStrip}
	 * only after direct CCDB initialization. After cache initialization, the JLab
	 * object graph is not retained and this method returns {@code null}.
	 *
	 * @param sector     the 0-based sector
	 * @param superlayer the 0-based superlayer
	 * @param layer      the 0-based layer
	 * @param strip      the 0-based strip
	 * @return the tracker strip, or {@code null}
	 */
	public static TrackerStrip getStrip(int sector, int superlayer, int layer, int strip) {
		if ((_fmtLayers == null) || !validStripAddress(sector, superlayer, layer, strip)) {
			return null;
		}

		FMTLayer fmtLayer = _fmtLayers.get(hash(sector, superlayer, layer));
		if (fmtLayer != null) {
			try {
				return fmtLayer.getComponent(strip);
			} catch (Exception e) {
				return null;
			}
		}

		return null;
	}

	/**
	 * Create a map key for a 0-based sector, superlayer, and layer.
	 *
	 * @param sector     the sector
	 * @param superlayer the superlayer
	 * @param layer      the layer
	 * @return the key
	 */
	private static String hash(int sector, int superlayer, int layer) {
		return String.format("%d|%d|%d", sector, superlayer, layer);
	}

	/**
	 * Get all FMT layers.
	 * <p>
	 * This is a legacy method. After cache initialization, the JLab layer objects
	 * are not retained, so this returns an empty collection.
	 *
	 * @return the collection of FMT layers, or an empty collection after cache
	 *         initialization
	 */
	public static Collection<FMTLayer> getAllFMTLayers() {
		if (_fmtLayers == null) {
			return Collections.emptyList();
		}

		return _fmtLayers.values();
	}

	/**
	 * Used by 3D drawing.
	 *
	 * @param sector     the 0-based sector, currently 0..0
	 * @param superlayer the 0-based superlayer, currently 0..0
	 * @param layer      the 0-based layer, 0..5
	 * @param stripId    the 0-based strip, 0..1023
	 * @param coords     holds 8*3 = 24 values [x1, y1, z1, ..., x8, y8, z8]
	 */
	public static void stripVertices(int sector, int superlayer, int layer, int stripId, float[] coords) {
		if (!validStripGeometry(sector, superlayer, layer, stripId) || (coords == null)
				|| (coords.length < CORNER_COUNT * COORD_COUNT)) {
			return;
		}

		double corners[][] = _stripCorners[sector][superlayer][layer][stripId];

		for (int i = 0; i < CORNER_COUNT; i++) {
			int j = COORD_COUNT * i;
			coords[j] = (float) corners[i][0];
			coords[j + 1] = (float) corners[i][1];
			coords[j + 2] = (float) corners[i][2];
		}
	}

	/**
	 * Get the primitive line endpoints for a strip.
	 * <p>
	 * This method is intended for 2D projection code such as a future completed
	 * FMT XY view. The returned points are copied into caller-supplied arrays.
	 *
	 * @param sector     the 0-based sector
	 * @param superlayer the 0-based superlayer
	 * @param layer      the 0-based layer
	 * @param stripId    the 0-based strip
	 * @param origin     receives the line origin as [x, y, z]
	 * @param end        receives the line end as [x, y, z]
	 * @return {@code true} if line geometry was available
	 */
	public static boolean stripLine(int sector, int superlayer, int layer, int stripId, double origin[], double end[]) {
		if (!validStripLineGeometry(sector, superlayer, layer, stripId) || !validCoordArray(origin)
				|| !validCoordArray(end)) {
			return false;
		}

		double linePoints[][] = _stripLinePoints[sector][superlayer][layer][stripId];

		for (int i = 0; i < COORD_COUNT; i++) {
			origin[i] = linePoints[0][i];
			end[i] = linePoints[1][i];
		}

		return true;
	}

	/**
	 * Get the primitive line origin for a strip.
	 *
	 * @param sector     the 0-based sector
	 * @param superlayer the 0-based superlayer
	 * @param layer      the 0-based layer
	 * @param stripId    the 0-based strip
	 * @param origin     receives the line origin as [x, y, z]
	 * @return {@code true} if line geometry was available
	 */
	public static boolean stripLineOrigin(int sector, int superlayer, int layer, int stripId, double origin[]) {
		if (!validStripLineGeometry(sector, superlayer, layer, stripId) || !validCoordArray(origin)) {
			return false;
		}

		double linePoints[][] = _stripLinePoints[sector][superlayer][layer][stripId];

		for (int i = 0; i < COORD_COUNT; i++) {
			origin[i] = linePoints[0][i];
		}

		return true;
	}

	/**
	 * Get the 1-based FMT region for a 1-based strip.
	 *
	 * @param strip1 the 1-based strip, 1..1024
	 * @return the region, 1..4
	 */
	public static int getRegion(int strip1) {
		int i = strip1 - 1;
		int region = 0;
		if (i >= 0 && i < 320) {
			region = 1;
		}
		if (i >= 320 && i < 512) {
			region = 2;
		}
		if (i >= 512 && i < 832) {
			region = 3;
		}
		if (i >= 832 && i < 1024) {
			region = 4;
		}

		return region;
	}

	/**
	 * Check whether a layer address is valid.
	 *
	 * @param sector     the sector
	 * @param superlayer the superlayer
	 * @param layer      the layer
	 * @return {@code true} if the address is valid
	 */
	private static boolean validLayerAddress(int sector, int superlayer, int layer) {
		return (sector >= 0) && (sector < SECTOR_COUNT)
				&& (superlayer >= 0) && (superlayer < SUPERLAYER_COUNT)
				&& (layer >= 0) && (layer < LAYER_COUNT);
	}

	/**
	 * Check whether a strip address is valid.
	 *
	 * @param sector     the sector
	 * @param superlayer the superlayer
	 * @param layer      the layer
	 * @param strip      the strip
	 * @return {@code true} if the address is valid
	 */
	private static boolean validStripAddress(int sector, int superlayer, int layer, int strip) {
		return validLayerAddress(sector, superlayer, layer) && (strip >= 0) && (strip < STRIP_COUNT);
	}

	/**
	 * Check whether primitive corner geometry exists for a strip.
	 *
	 * @param sector     the sector
	 * @param superlayer the superlayer
	 * @param layer      the layer
	 * @param strip      the strip
	 * @return {@code true} if corner geometry exists
	 */
	private static boolean validStripGeometry(int sector, int superlayer, int layer, int strip) {
		return validStripAddress(sector, superlayer, layer, strip)
				&& (_stripCorners != null)
				&& (_stripCorners[sector][superlayer][layer][strip] != null);
	}

	/**
	 * Check whether primitive line geometry exists for a strip.
	 *
	 * @param sector     the sector
	 * @param superlayer the superlayer
	 * @param layer      the layer
	 * @param strip      the strip
	 * @return {@code true} if line geometry exists
	 */
	private static boolean validStripLineGeometry(int sector, int superlayer, int layer, int strip) {
		return validStripAddress(sector, superlayer, layer, strip)
				&& (_stripLinePoints != null)
				&& (_stripLinePoints[sector][superlayer][layer][strip] != null);
	}

	/**
	 * Check whether a coordinate array can receive three coordinates.
	 *
	 * @param coords the coordinate array
	 * @return {@code true} if the array is usable
	 */
	private static boolean validCoordArray(double coords[]) {
		return (coords != null) && (coords.length >= COORD_COUNT);
	}

	/**
	 * Read FMT geometry from the cache.
	 * <p>
	 * This reads explicit primitive strip geometry rather than JLab
	 * {@link FMTLayer} or {@link TrackerStrip} objects.
	 *
	 * @param kryo  the Kryo instance, retained for interface compatibility
	 * @param input the input stream
	 * @return {@code true} if the geometry was read successfully
	 */
	@Override
	public boolean readGeometry(Kryo kryo, Input input) {
		try {
			int sectorCount = input.readInt();
			if (sectorCount != SECTOR_COUNT) {
				System.err.printf("FMTGeometry: expected %d sectors, found %d in cache.%n", SECTOR_COUNT, sectorCount);
				return false;
			}

			double stripCorners[][][][][][] = new double[SECTOR_COUNT][SUPERLAYER_COUNT][LAYER_COUNT][STRIP_COUNT][][];
			double stripLinePoints[][][][][][] = new double[SECTOR_COUNT][SUPERLAYER_COUNT][LAYER_COUNT][STRIP_COUNT][][];

			for (int sector = 0; sector < SECTOR_COUNT; sector++) {
				int superlayerCount = input.readInt();
				if (superlayerCount != SUPERLAYER_COUNT) {
					System.err.printf("FMTGeometry: expected %d superlayers for sector %d, found %d in cache.%n",
							SUPERLAYER_COUNT, sector, superlayerCount);
					return false;
				}

				for (int superlayer = 0; superlayer < SUPERLAYER_COUNT; superlayer++) {
					int layerCount = input.readInt();
					if (layerCount != LAYER_COUNT) {
						System.err.printf(
								"FMTGeometry: expected %d layers for sector %d superlayer %d, found %d in cache.%n",
								LAYER_COUNT, sector, superlayer, layerCount);
						return false;
					}

					for (int layer = 0; layer < LAYER_COUNT; layer++) {
						int stripCount = input.readInt();
						if (stripCount != STRIP_COUNT) {
							System.err.printf(
									"FMTGeometry: expected %d strips for sector %d superlayer %d layer %d, found %d in cache.%n",
									STRIP_COUNT, sector, superlayer, layer, stripCount);
							return false;
						}

						for (int strip = 0; strip < STRIP_COUNT; strip++) {
							boolean hasStrip = input.readBoolean();

							if (hasStrip) {
								String context = String.format("FMTGeometry sector %d superlayer %d layer %d strip %d",
										sector, superlayer, layer, strip);

								stripCorners[sector][superlayer][layer][strip] = GeometryPrimitiveIO.readCorners(input,
										CORNER_COUNT, COORD_COUNT, context + " corners");

								stripLinePoints[sector][superlayer][layer][strip] = readLinePoints(input,
										context + " line");
							}
						}
					}
				}
			}

			_stripCorners = stripCorners;
			_stripLinePoints = stripLinePoints;

			// Do not keep stale JLab geometry objects after a cache read.
			_fmtLayers = null;

			return true;
		} catch (Exception e) {
			System.err.println("FMTGeometry: Error reading geometry cache: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Write FMT geometry to the cache.
	 * <p>
	 * This writes explicit primitive strip geometry rather than JLab
	 * {@link FMTLayer} or {@link TrackerStrip} objects.
	 *
	 * @param kryo   the Kryo instance, retained for interface compatibility
	 * @param output the output stream
	 * @return {@code true} if the geometry was written successfully
	 */
	@Override
	public boolean writeGeometry(Kryo kryo, Output output) {
		try {
			if (_stripCorners == null || _stripLinePoints == null) {
				if (_fmtLayers == null) {
					System.err.println("FMTGeometry: no primitive or JLab geometry available to write.");
					return false;
				}

				rebuildPrimitiveGeometryFromLayers();
			}

			output.writeInt(SECTOR_COUNT);

			for (int sector = 0; sector < SECTOR_COUNT; sector++) {
				output.writeInt(SUPERLAYER_COUNT);

				for (int superlayer = 0; superlayer < SUPERLAYER_COUNT; superlayer++) {
					output.writeInt(LAYER_COUNT);

					for (int layer = 0; layer < LAYER_COUNT; layer++) {
						output.writeInt(STRIP_COUNT);

						for (int strip = 0; strip < STRIP_COUNT; strip++) {
							boolean hasStrip = validStripGeometry(sector, superlayer, layer, strip)
									&& validStripLineGeometry(sector, superlayer, layer, strip);

							output.writeBoolean(hasStrip);

							if (hasStrip) {
								GeometryPrimitiveIO.writeCorners(output, _stripCorners[sector][superlayer][layer][strip],
										CORNER_COUNT, COORD_COUNT);
								writeLinePoints(output, _stripLinePoints[sector][superlayer][layer][strip]);
							}
						}
					}
				}
			}

			return true;
		} catch (Exception e) {
			System.err.println("FMTGeometry: Error writing geometry cache: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Rebuild primitive geometry from the retained JLab FMT layer objects.
	 */
	private static void rebuildPrimitiveGeometryFromLayers() {
		_stripCorners = new double[SECTOR_COUNT][SUPERLAYER_COUNT][LAYER_COUNT][STRIP_COUNT][][];
		_stripLinePoints = new double[SECTOR_COUNT][SUPERLAYER_COUNT][LAYER_COUNT][STRIP_COUNT][][];

		if (_fmtLayers == null) {
			return;
		}

		for (int sector = 0; sector < SECTOR_COUNT; sector++) {
			for (int superlayer = 0; superlayer < SUPERLAYER_COUNT; superlayer++) {
				for (int layer = 0; layer < LAYER_COUNT; layer++) {
					FMTLayer fmtLayer = _fmtLayers.get(hash(sector, superlayer, layer));
					cacheLayerPrimitiveGeometry(sector, superlayer, layer, fmtLayer);
				}
			}
		}
	}

	/**
	 * Write two line endpoint points.
	 *
	 * @param output     the output stream
	 * @param linePoints the line points, shaped [2][3]
	 */
	private static void writeLinePoints(Output output, double linePoints[][]) {
		validateLinePoints(linePoints, "write");

		output.writeInt(LINE_ENDPOINT_COUNT);

		for (int endpoint = 0; endpoint < LINE_ENDPOINT_COUNT; endpoint++) {
			output.writeInt(COORD_COUNT);

			for (int coord = 0; coord < COORD_COUNT; coord++) {
				output.writeDouble(linePoints[endpoint][coord]);
			}
		}
	}

	/**
	 * Read two line endpoint points.
	 *
	 * @param input   the input stream
	 * @param context context for error messages
	 * @return line endpoint points, shaped [2][3]
	 */
	private static double[][] readLinePoints(Input input, String context) {
		int endpointCount = input.readInt();

		if (endpointCount != LINE_ENDPOINT_COUNT) {
			throw new IllegalArgumentException(String.format("%s: expected %d endpoints, found %d.", context,
					LINE_ENDPOINT_COUNT, endpointCount));
		}

		double linePoints[][] = new double[LINE_ENDPOINT_COUNT][COORD_COUNT];

		for (int endpoint = 0; endpoint < LINE_ENDPOINT_COUNT; endpoint++) {
			int coordCount = input.readInt();

			if (coordCount != COORD_COUNT) {
				throw new IllegalArgumentException(String.format(
						"%s: expected %d coordinates for endpoint %d, found %d.", context, COORD_COUNT, endpoint,
						coordCount));
			}

			for (int coord = 0; coord < COORD_COUNT; coord++) {
				linePoints[endpoint][coord] = input.readDouble();
			}
		}

		return linePoints;
	}

	/**
	 * Validate line endpoint data.
	 *
	 * @param linePoints the line endpoint array
	 * @param operation  the operation name for error messages
	 */
	private static void validateLinePoints(double linePoints[][], String operation) {
		if (linePoints == null) {
			throw new IllegalArgumentException("Cannot " + operation + " null line point array.");
		}

		if (linePoints.length != LINE_ENDPOINT_COUNT) {
			throw new IllegalArgumentException(String.format(
					"Cannot %s line point array: expected %d endpoints, found %d.", operation, LINE_ENDPOINT_COUNT,
					linePoints.length));
		}

		for (int endpoint = 0; endpoint < LINE_ENDPOINT_COUNT; endpoint++) {
			if (linePoints[endpoint] == null) {
				throw new IllegalArgumentException(
						String.format("Cannot %s line point array: endpoint %d is null.", operation, endpoint));
			}

			if (linePoints[endpoint].length != COORD_COUNT) {
				throw new IllegalArgumentException(String.format(
						"Cannot %s line point array: expected %d coordinates for endpoint %d, found %d.", operation,
						COORD_COUNT, endpoint, linePoints[endpoint].length));
			}
		}
	}
}