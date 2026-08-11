package cnuphys.ced.geometry;

import org.jlab.detector.base.GeometryFactory;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.detector.ec.ECDetector;
import org.jlab.geom.detector.ec.ECFactory;
import org.jlab.geom.detector.ec.ECLayer;
import org.jlab.geom.detector.ec.ECSector;
import org.jlab.geom.detector.ec.ECSuperlayer;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Transformable;
import org.jlab.geom.prim.Transformation3D;

public class Transformations {

	private DetectorType _detectorType;

	private Transformation3D _localToSector;
	private Transformation3D _sectorToLocal;

	// Explicit affine forms [output coordinate][x,y,z,translation]. These can be
	// persisted without retaining or reconstructing the JLab geometry object graph.
	private double[][] _localToSectorAffine;
	private double[][] _sectorToLocalAffine;

	public Transformations(DetectorType dtype) {
		_detectorType = dtype;

		// BST, DC, EC_INNER, EC_OUTER, FTOT, PCAL
		switch (dtype) {

		case BST:
			break;

		case DC:
			break;

		case EC_INNER:
			initCal(1); // 1 for ec inner
			break;

		case EC_OUTER:
			initCal(2); // 2 for ec outer
			break;

		case FTOT:
			break;

		case PCAL:
			initCal(0); // 0 for pcal
			break;
		}

	}

	/** Restore transformations from explicit affine coefficients. */
	public Transformations(DetectorType dtype, double[][] localToSector, double[][] sectorToLocal) {
		_detectorType = dtype;
		_localToSectorAffine = copyAffine(localToSector);
		_sectorToLocalAffine = copyAffine(sectorToLocal);
	}

	// init for cal superlayer = (0,1,2) for PCAL, EC_IN, EC_OUT
	private void initCal(int superlayer) {
		ConstantProvider provider = GeometryFactory.getConstants(org.jlab.detector.base.DetectorType.ECAL);
		ECFactory ecFactory = new ECFactory();

		// detector in sector coordinates
		ECDetector clasDetector = ecFactory.createDetectorSector(provider);

		ECSector clas_sector = clasDetector.getSector(0);

		// superlayer 0 for pcal
		ECSuperlayer clas_ecSuperlayer = clas_sector.getSuperlayer(superlayer);

		// layer 0 for U
		ECLayer clas_ecLayerU = clas_ecSuperlayer.getLayer(0);

		_localToSector = clas_ecLayerU.getTransformation();
		_sectorToLocal = _localToSector.inverse();
		_localToSectorAffine = sampleAffine(_localToSector);
		_sectorToLocalAffine = sampleAffine(_sectorToLocal);
	}

	/**
	 * Convert from the local system to the sector system
	 *
	 * @param txf a Transferable in the local system that will be modified to be in
	 *            the sector system
	 */
	public void localToSector(Transformable txf) {
		if (txf instanceof Point3D point) {
			apply(_localToSectorAffine, point);
		} else {
			_localToSector.apply(txf);
		}
	}

	/**
	 * Convert from the sector system to the local system
	 *
	 * @param txf a Transferable in the sector system that will be modified to be in
	 *            the local system
	 */
	public void sectorToLocal(Transformable txf) {
		if (txf instanceof Point3D point) {
			apply(_sectorToLocalAffine, point);
		} else {
			_sectorToLocal.apply(txf);
		}
	}

	/**
	 * Convert from the local system to the sector system
	 *
	 * @param localP  a point in the local system (not modified)
	 * @param sectorP a point in the sector system (modified)
	 */
	public void localToSector(Point3D localP, Point3D sectorP) {
		sectorP.set(localP.x(), localP.y(), localP.z());
		apply(_localToSectorAffine, sectorP);
	}

	/**
	 * Convert from the sector system to the local system
	 *
	 * @param localP  a point in the local system (modified)
	 * @param sectorP a point in the sector system (not modified)
	 */
	public void sectorToLocal(Point3D localP, Point3D sectorP) {
		localP.set(sectorP.x(), sectorP.y(), sectorP.z());
		apply(_sectorToLocalAffine, localP);
	}

	/**
	 * Convert from the sector system to the local system
	 *
	 * @param localP a point in the local system (modified)
	 * @param clasP  a point in the clas (lab) system (not modified)
	 */
	public void clasToLocal(Point3D localP, Point3D clasP) {
		Point3D sectorP = new Point3D();
		GeometryManager.clasToSector(clasP, sectorP);
		sectorToLocal(localP, sectorP);
	}

	/**
	 * Convert from the clas (lab) system to the local system
	 *
	 * @param localP a point in the local system (not modified)
	 * @param clasP  a point in the clas (lab) system (modified)
	 */
	public void localToClas(int sector, Point3D localP, Point3D clasP) {
		Point3D sectorP = new Point3D();
		localToSector(localP, sectorP);
		GeometryManager.sectorToClas(sector, clasP, sectorP);
	}

	/**
	 * Get the detector type of the transformation
	 *
	 * @return the detector type
	 */
	public DetectorType getDetectorType() {
		return _detectorType;
	}

	public double[][] getLocalToSectorAffine() {
		return copyAffine(_localToSectorAffine);
	}

	public double[][] getSectorToLocalAffine() {
		return copyAffine(_sectorToLocalAffine);
	}

	private static double[][] sampleAffine(Transformation3D transformation) {
		Point3D origin = transformed(transformation, 0, 0, 0);
		Point3D xAxis = transformed(transformation, 1, 0, 0);
		Point3D yAxis = transformed(transformation, 0, 1, 0);
		Point3D zAxis = transformed(transformation, 0, 0, 1);
		return new double[][] {
			{ xAxis.x() - origin.x(), yAxis.x() - origin.x(), zAxis.x() - origin.x(), origin.x() },
			{ xAxis.y() - origin.y(), yAxis.y() - origin.y(), zAxis.y() - origin.y(), origin.y() },
			{ xAxis.z() - origin.z(), yAxis.z() - origin.z(), zAxis.z() - origin.z(), origin.z() }
		};
	}

	private static Point3D transformed(Transformation3D transformation, double x, double y, double z) {
		Point3D point = new Point3D(x, y, z);
		transformation.apply(point);
		return point;
	}

	private static void apply(double[][] affine, Point3D point) {
		if (affine == null) {
			throw new IllegalStateException("Transformation coefficients are unavailable");
		}
		double x = point.x();
		double y = point.y();
		double z = point.z();
		point.set(affine[0][0] * x + affine[0][1] * y + affine[0][2] * z + affine[0][3],
				affine[1][0] * x + affine[1][1] * y + affine[1][2] * z + affine[1][3],
				affine[2][0] * x + affine[2][1] * y + affine[2][2] * z + affine[2][3]);
	}

	private static double[][] copyAffine(double[][] source) {
		if (source == null || source.length != 3) {
			throw new IllegalArgumentException("Affine transformation must have three rows");
		}
		double[][] copy = new double[3][4];
		for (int row = 0; row < 3; row++) {
			if (source[row] == null || source[row].length != 4) {
				throw new IllegalArgumentException("Affine transformation rows must have four values");
			}
			System.arraycopy(source[row], 0, copy[row], 0, 4);
		}
		return copy;
	}
}
