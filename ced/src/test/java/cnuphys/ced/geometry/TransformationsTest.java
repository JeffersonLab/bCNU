package cnuphys.ced.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.jlab.geom.prim.Point3D;
import org.junit.jupiter.api.Test;

class TransformationsTest {

	@Test
	void restoresExplicitAffinePointTransformations() {
		double[][] forward = {
			{ 0, -1, 0, 10 },
			{ 1,  0, 0, 20 },
			{ 0,  0, 1, 30 }
		};
		double[][] inverse = {
			{ 0, 1, 0, -20 },
			{-1, 0, 0,  10 },
			{ 0, 0, 1, -30 }
		};
		Transformations transformations = new Transformations(DetectorType.PCAL, forward, inverse);

		Point3D sector = new Point3D();
		transformations.localToSector(new Point3D(2, 3, 4), sector);
		assertEquals(7.0, sector.x());
		assertEquals(22.0, sector.y());
		assertEquals(34.0, sector.z());

		Point3D local = new Point3D();
		transformations.sectorToLocal(local, sector);
		assertEquals(2.0, local.x());
		assertEquals(3.0, local.y());
		assertEquals(4.0, local.z());
	}

	@Test
	void returnedCoefficientsAreDefensiveCopies() {
		double[][] identity = {
			{1, 0, 0, 0}, {0, 1, 0, 0}, {0, 0, 1, 0}
		};
		Transformations transformations = new Transformations(DetectorType.PCAL, identity, identity);
		double[][] copy = transformations.getLocalToSectorAffine();
		copy[0][0] = 99;
		assertEquals(1.0, transformations.getLocalToSectorAffine()[0][0]);
	}
}
