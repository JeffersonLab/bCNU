package cnuphys.ced.clasio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class TrackKinematicsTest {

	private static final double TOLERANCE = 1.0e-12;

	@Test
	void calculatesMomentumAndAngles() {
		TrackKinematics.Direction direction = TrackKinematics.fromMomentum(3.0, 4.0, 0.0);

		assertEquals(5.0, direction.momentum(), TOLERANCE);
		assertEquals(90.0, direction.thetaDegrees(), TOLERANCE);
		assertEquals(Math.toDegrees(Math.atan2(4.0, 3.0)), direction.phiDegrees(), TOLERANCE);
	}

	@Test
	void rejectsZeroAndNonFiniteMomentum() {
		assertNull(TrackKinematics.fromMomentum(0.0, 0.0, 0.0));
		assertNull(TrackKinematics.fromMomentum(Double.NaN, 1.0, 1.0));
		assertNull(TrackKinematics.fromMomentum(1.0, Double.POSITIVE_INFINITY, 1.0));
	}

	@Test
	void handlesTracksAlongTheBeamAxis() {
		assertEquals(0.0, TrackKinematics.fromMomentum(0.0, 0.0, 2.0).thetaDegrees(), TOLERANCE);
		assertEquals(180.0, TrackKinematics.fromMomentum(0.0, 0.0, -2.0).thetaDegrees(), TOLERANCE);
	}
}
