package cnuphys.ced.clasio;

final class TrackKinematics {

	private TrackKinematics() {
	}

	static Direction fromMomentum(double px, double py, double pz) {
		if (!Double.isFinite(px) || !Double.isFinite(py) || !Double.isFinite(pz)) {
			return null;
		}

		double momentum = Math.sqrt(px * px + py * py + pz * pz);
		if (!Double.isFinite(momentum) || (momentum <= 0.0)) {
			return null;
		}

		double cosineTheta = Math.max(-1.0, Math.min(1.0, pz / momentum));
		return new Direction(momentum, Math.toDegrees(Math.acos(cosineTheta)),
				Math.toDegrees(Math.atan2(py, px)));
	}

	record Direction(double momentum, double thetaDegrees, double phiDegrees) {
	}
}
