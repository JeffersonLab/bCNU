package cnuphys.ced.geometry.urwt;

import org.jlab.detector.geant4.v2.MPGD.URWT.URWTStripFactory;
import org.jlab.geom.prim.Line3D;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import cnuphys.bCNU.util.UnicodeSupport;
import cnuphys.ced.ced3d.util.DrawSupport;
import cnuphys.ced.ced3d.util.Plane;
import cnuphys.ced.frame.Ced;
import cnuphys.ced.geometry.cache.ACachedGeometry;

/**
 * Geometric data for the uRwell detector
 * 
 * 3/5/26 BIG CHANGE Chamber concept gone, now a given sector and layer make "detector"
  * 
 * @author heddle
 *
 */
public class UrWTGeometry extends ACachedGeometry {
	
	/** the number of sectors */
	public static final int NUM_SECTORS = 6;
	
	/** the number of layers per sector */
	public static final int NUM_LAYERS = 4;

	// the name of the detector
	public static String NAME = UnicodeSupport.SMALL_MU + "rWT";


	// the strip factory
	private static  URWTStripFactory factory;
	

	// used to make outline polygons (taken from sector 1 and rotated as needed)
	// the index corresponds to layer
	public static double minX[] = { Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY };
	public static double maxX[] = { Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY };
	public static double minY[] = { Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY };
	public static double maxY[] = { Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY };
	
	//  the detectors
	private static UrWTDetectorData[][] detectorData = new UrWTDetectorData[NUM_SECTORS][NUM_LAYERS];

	/**
	 * Constructor
	 */
	public UrWTGeometry() {
		super(NAME);
	}
	
	/**
	 * Get the detector data for a given sector and layer
	 * @param sector the 1-based sector [1..6]
	 * @param layer the 1-based layer [1..4]
	 * @return the detector data
	 */
	public static UrWTDetectorData getDetectorData(int sector, int layer) {
		return detectorData[sector - 1][layer - 1];
	}


	/**
	 * Initialize the geometry using the CCDB. This is called by the geometry cache when
	 * 
	 */
	@Override
	public void initializeUsingCCDB() {
		System.out.println("\n=======================================");
		System.out.println("===  " + NAME + " Geometry Initialization ===");
		System.out.println("=======================================");

		String variationName = Ced.getGeometryVariation();
		
		factory = new URWTStripFactory(11, variationName);
				
		for (int sector = 1; sector <= NUM_SECTORS; sector++) {
			for (int layer = 1; layer <= NUM_LAYERS; layer++) {
				detectorData[sector - 1][layer - 1] = new UrWTDetectorData(factory, sector, layer);
			}
		} // sector loop

		// get the bounds from sector 1, will rotate as needed for other sectors
		for (int layer = 1; layer <= NUM_LAYERS; layer++) {
			UrWTDetectorData data = detectorData[0][layer - 1];
			
			Line3D firstStrip = data.strips[0];
			Line3D lastStrip = data.strips[data.count - 1];
			
			minX[layer - 1] = Math.min(firstStrip.origin().x(), minX[layer - 1]);
			minX[layer - 1] = Math.min(firstStrip.end().x(), minX[layer - 1]);	
			minX[layer - 1] = Math.max(lastStrip.origin().x(), minX[layer - 1]);
			minX[layer - 1] = Math.max(lastStrip.end().x(), minX[layer - 1]);
			
			minY[layer - 1] = Math.min(firstStrip.origin().y(), minY[layer - 1]);
			minY[layer - 1] = Math.min(firstStrip.end().y(), minY[layer - 1]);	
			minY[layer - 1] = Math.max(lastStrip.origin().y(), minY[layer - 1]);
			minY[layer - 1] = Math.max(lastStrip.end().y(), minY[layer - 1]);
			
			maxX[layer - 1] = Math.max(firstStrip.origin().x(), maxX[layer - 1]);
			maxX[layer - 1] = Math.max(firstStrip.end().x(), maxX[layer - 1]);
			maxX[layer - 1] = Math.max(lastStrip.origin().x(), maxX[layer - 1]);
			maxX[layer - 1] = Math.max(lastStrip.end().x(), maxX [layer - 1]);
			
			maxY[layer - 1] = Math.max(firstStrip.origin().y(), maxY[layer - 1]);
			maxY[layer - 1] = Math.max(firstStrip.end().y(), maxY[layer - 1]);
			maxY[layer - 1] = Math.max(lastStrip.origin().y(), maxY[layer - 1]);
			maxY[layer - 1] = Math.max(lastStrip.end().y(), maxY[layer - 1]);
			
		}

		for (int layer = 1; layer <= NUM_LAYERS; layer++) {
			Plane plane = DrawSupport.findCommonPlane(detectorData[0][layer - 1].strips, 1e-6);
			System.out.println("Layer " + layer + " plane: " + plane);
		}
	}
	

	/**
	 * Get a strip
	 * 
	 * @param sector       the 1-based sector [1..6]
	 * @param layer        the 1-based layer [1..2]
	 * @param strip        the 1-based strip
	 * @return the strip
	 */
	public static Line3D getStrip(int sector, int layer, int strip) {
		return detectorData[sector - 1][layer - 1].strips[strip - 1];
	}

	@Override
	public boolean readGeometry(Kryo kryo, Input input) {
		return false;
//		try {
//			// Read _chamberData (3D array)
//			int outer = input.readInt();
//			if (outer == 0) {
//				_chamberData = null;
//			} else {
//				_chamberData = new ChamberData[outer][][];
//				for (int i = 0; i < outer; i++) {
//					int inner = input.readInt();
//					if (inner == 0) {
//						_chamberData[i] = null;
//					} else {
//						_chamberData[i] = new ChamberData[inner][];
//						for (int j = 0; j < inner; j++) {
//							int third = input.readInt();
//							if (third == 0) {
//								_chamberData[i][j] = null;
//							} else {
//								_chamberData[i][j] = new ChamberData[third];
//								for (int k = 0; k < third; k++) {
//									_chamberData[i][j][k] = kryo.readObjectOrNull(input, ChamberData.class);
//								}
//							}
//						}
//					}
//				}
//			}
//
//			// Read numStripsByChamber (int[])
//			int lenNum = input.readInt();
//			if (lenNum == 0) {
//				numStripsByChamber = null;
//			} else {
//				numStripsByChamber = new int[lenNum];
//				for (int i = 0; i < lenNum; i++) {
//					numStripsByChamber[i] = input.readInt();
//				}
//			}
//
//			// Read _inidices (int[][])
//			int outer2 = input.readInt();
//			if (outer2 == 0) {
//				_inidices = null;
//			} else {
//				_inidices = new int[outer2][];
//				for (int i = 0; i < outer2; i++) {
//					int inner2 = input.readInt();
//					if (inner2 == 0) {
//						_inidices[i] = null;
//					} else {
//						_inidices[i] = new int[inner2];
//						for (int j = 0; j < inner2; j++) {
//							_inidices[i][j] = input.readInt();
//						}
//					}
//				}
//			}
//
//			// Read maxStrip (int)
//			maxStrip = input.readInt();
//
//			// Read maxX (double[])
//			int lenMaxX = input.readInt();
//			if (lenMaxX == 0) {
//				maxX = null;
//			} else {
//				maxX = new double[lenMaxX];
//				for (int i = 0; i < lenMaxX; i++) {
//					maxX[i] = input.readDouble();
//				}
//			}
//
//			// Read minX (double[])
//			int lenMinX = input.readInt();
//			if (lenMinX == 0) {
//				minX = null;
//			} else {
//				minX = new double[lenMinX];
//				for (int i = 0; i < lenMinX; i++) {
//					minX[i] = input.readDouble();
//				}
//			}
//
//			// Read maxY (double[])
//			int lenMaxY = input.readInt();
//			if (lenMaxY == 0) {
//				maxY = null;
//			} else {
//				maxY = new double[lenMaxY];
//				for (int i = 0; i < lenMaxY; i++) {
//					maxY[i] = input.readDouble();
//				}
//			}
//
//			// Read minY (double[])
//			int lenMinY = input.readInt();
//			if (lenMinY == 0) {
//				minY = null;
//			} else {
//				minY = new double[lenMinY];
//				for (int i = 0; i < lenMinY; i++) {
//					minY[i] = input.readDouble();
//				}
//			}
//
//			return true;
//		} catch (Exception e) {
//			return false;
//		}
	}

	@Override
	public boolean writeGeometry(Kryo kryo, Output output) {
		return false;
//		try {
//			// Write _chamberData (3D array:
//			// [6][URWTConstants.NCHAMBERS][URWTConstants.NLAYERS])
//			if (_chamberData == null) {
//				output.writeInt(0);
//			} else {
//				output.writeInt(_chamberData.length); // expected 6 sectors
//				for (int i = 0; i < _chamberData.length; i++) {
//					if (_chamberData[i] == null) {
//						output.writeInt(0);
//					} else {
//						output.writeInt(_chamberData[i].length); // number of chambers
//						for (int j = 0; j < _chamberData[i].length; j++) {
//							if (_chamberData[i][j] == null) {
//								output.writeInt(0);
//							} else {
//								output.writeInt(_chamberData[i][j].length); // number of layers
//								for (int k = 0; k < _chamberData[i][j].length; k++) {
//									kryo.writeObjectOrNull(output, _chamberData[i][j][k], ChamberData.class);
//								}
//							}
//						}
//					}
//				}
//			}
//
//			// Write numStripsByChamber (int[])
//			if (numStripsByChamber == null) {
//				output.writeInt(0);
//			} else {
//				output.writeInt(numStripsByChamber.length);
//				for (int i = 0; i < numStripsByChamber.length; i++) {
//					output.writeInt(numStripsByChamber[i]);
//				}
//			}
//
//			// Write _inidices (int[][])
//			if (_inidices == null) {
//				output.writeInt(0);
//			} else {
//				output.writeInt(_inidices.length);
//				for (int i = 0; i < _inidices.length; i++) {
//					if (_inidices[i] == null) {
//						output.writeInt(0);
//					} else {
//						output.writeInt(_inidices[i].length); // should be 2
//						for (int j = 0; j < _inidices[i].length; j++) {
//							output.writeInt(_inidices[i][j]);
//						}
//					}
//				}
//			}
//
//			// Write maxStrip (int)
//			output.writeInt(maxStrip);
//
//			// Write maxX (double[])
//			if (maxX == null) {
//				output.writeInt(0);
//			} else {
//				output.writeInt(maxX.length);
//				for (int i = 0; i < maxX.length; i++) {
//					output.writeDouble(maxX[i]);
//				}
//			}
//
//			// Write minX (double[])
//			if (minX == null) {
//				output.writeInt(0);
//			} else {
//				output.writeInt(minX.length);
//				for (int i = 0; i < minX.length; i++) {
//					output.writeDouble(minX[i]);
//				}
//			}
//
//			// Write maxY (double[])
//			if (maxY == null) {
//				output.writeInt(0);
//			} else {
//				output.writeInt(maxY.length);
//				for (int i = 0; i < maxY.length; i++) {
//					output.writeDouble(maxY[i]);
//				}
//			}
//
//			// Write minY (double[])
//			if (minY == null) {
//				output.writeInt(0);
//			} else {
//				output.writeInt(minY.length);
//				for (int i = 0; i < minY.length; i++) {
//					output.writeDouble(minY[i]);
//				}
//			}
//
//			return true;
//		} catch (Exception e) {
//			return false;
//		}
	}

	public static void main(String args[]) {
		UrWTGeometry geometry = new UrWTGeometry();
		geometry.initializeUsingCCDB();
		

	}
	
	

}
