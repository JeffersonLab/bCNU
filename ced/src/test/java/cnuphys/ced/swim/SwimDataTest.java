package cnuphys.ced.swim;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import cnuphys.CLAS12Swim.CLAS12Swimmer;
import cnuphys.adaptiveSwim.SwimType;
import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;
import cnuphys.lund.TrajectoryRowData;
import cnuphys.magfield.ZeroProbe;

class SwimDataTest {

    private final CLAS12Swimmer swimmer = new CLAS12Swimmer(new ZeroProbe());

    @Test
    void acceptsFiniteParticleAndPositiveIntegrationSettings() {
        assertTrue(data(row(1.0, 2.0, 3.0, 1000.0, 45.0, 30.0),
                SwimData.TrajectoryType.MC, 900.0, 0.001, 1.0e-6).isValid());
    }

    @Test
    void rejectsMissingRequiredObjects() {
        assertFalse(data(null, SwimData.TrajectoryType.MC, 900.0, 0.001, 1.0e-6).isValid());
        assertFalse(data(new TrajectoryRowData(1, null, 0.0, 0.0, 0.0, 1000.0,
                45.0, 0.0, 0, "test", SwimType.MCSWIM),
                SwimData.TrajectoryType.MC, 900.0, 0.001, 1.0e-6).isValid());
        assertFalse(data(row(0.0, 0.0, 0.0, 1000.0, 45.0, 0.0),
                null, 900.0, 0.001, 1.0e-6).isValid());
        assertFalse(new SwimData(row(0.0, 0.0, 0.0, 1000.0, 45.0, 0.0),
                SwimData.TrajectoryType.MC, 900.0, 0.001, 1.0e-6, null).isValid());
    }

    @Test
    void rejectsNonpositiveIntegrationSettings() {
        TrajectoryRowData row = row(0.0, 0.0, 0.0, 1000.0, 45.0, 0.0);
        assertFalse(data(row, SwimData.TrajectoryType.RECON, 0.0, 0.001, 1.0e-6).isValid());
        assertFalse(data(row, SwimData.TrajectoryType.RECON, 900.0, 0.0, 1.0e-6).isValid());
        assertFalse(data(row, SwimData.TrajectoryType.RECON, 900.0, 0.001, 0.0).isValid());
    }

    @Test
    void rejectsNonfiniteAndUnreasonablyLargeParticleValues() {
        assertFalse(data(row(Double.NaN, 0.0, 0.0, 1000.0, 45.0, 0.0),
                SwimData.TrajectoryType.MC, 900.0, 0.001, 1.0e-6).isValid());
        assertFalse(data(row(0.0, 0.0, 0.0, Double.POSITIVE_INFINITY, 45.0, 0.0),
                SwimData.TrajectoryType.MC, 900.0, 0.001, 1.0e-6).isValid());
        assertFalse(data(row(0.0, 0.0, 0.0, 1000.0, 1.0e15, 0.0),
                SwimData.TrajectoryType.MC, 900.0, 0.001, 1.0e-6).isValid());
    }

    private SwimData data(TrajectoryRowData row, SwimData.TrajectoryType type,
            double sMax, double step, double tolerance) {
        return new SwimData(row, type, sMax, step, tolerance, swimmer);
    }

    private static TrajectoryRowData row(double x, double y, double z, double momentum,
            double theta, double phi) {
        LundId electron = LundSupport.getInstance().get(11);
        return new TrajectoryRowData(1, electron, x, y, z, momentum, theta, phi,
                0, "test", SwimType.MCSWIM);
    }
}
