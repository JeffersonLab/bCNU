package cnuphys.ced.swim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;
import cnuphys.lund.TrajectoryRowData;

class SwimRequestPolicyTest {

    @Test
    void cvtSourcesUseShorterPathLimit() {
        assertEquals(150.0, SwimRequestPolicy.maxPathForRecon("REC::CVTTracks"));
        assertEquals(900.0, SwimRequestPolicy.maxPathForRecon("REC::Particle"));
        assertEquals(900.0, SwimRequestPolicy.maxPathForRecon(null));
    }

    @Test
    void duplicateKeyMatchesIdenticalParticleCoordinates() {
        LundId electron = LundSupport.getInstance().get(11);
        TrajectoryRowData first = row(electron, 1.0, 2.0, 3.0, 1000.0, 45.0, 30.0);
        TrajectoryRowData duplicate = row(electron, 1.0, 2.0, 3.0, 1000.0, 45.0, 30.0);

        assertEquals(SwimRequestPolicy.mcDuplicateKey(electron, first),
                SwimRequestPolicy.mcDuplicateKey(electron, duplicate));
    }

    @Test
    void duplicateKeyChangesWithParticleKinematics() {
        LundId electron = LundSupport.getInstance().get(11);
        TrajectoryRowData first = row(electron, 1.0, 2.0, 3.0, 1000.0, 45.0, 30.0);
        TrajectoryRowData changed = row(electron, 1.0, 2.0, 3.0, 1000.0, 45.0, 31.0);

        assertNotEquals(SwimRequestPolicy.mcDuplicateKey(electron, first),
                SwimRequestPolicy.mcDuplicateKey(electron, changed));
    }

    private static TrajectoryRowData row(LundId lundId, double x, double y, double z,
            double momentum, double theta, double phi) {
        return new TrajectoryRowData(1, lundId, x, y, z, momentum, theta, phi,
                0, "test");
    }
}
