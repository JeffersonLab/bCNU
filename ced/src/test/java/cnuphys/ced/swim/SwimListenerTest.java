package cnuphys.ced.swim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cnuphys.CLAS12Swim.CLAS12Swimmer;
import cnuphys.adaptiveSwim.SwimType;
import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;
import cnuphys.lund.TrajectoryRowData;
import cnuphys.magfield.ZeroProbe;
import cnuphys.swim.SwimTrajectory;
import cnuphys.swim.Swimming;

class SwimListenerTest {

    private final CLAS12Swimmer swimmer = new CLAS12Swimmer(new ZeroProbe());

    @BeforeEach
    void disableNotificationsAndClearTrajectories() {
        Swimming.setNotifyOn(false);
        Swimming.clearAllTrajectories();
    }

    @AfterEach
    void restoreSwimmingRegistry() {
        Swimming.clearAllTrajectories();
        Swimming.setNotifyOn(true);
    }

    @Test
    void routesMonteCarloTrajectoryAndCopiesMetadata() {
        TrajectoryRowData row = row("MC::Particle", SwimType.MCSWIM);

        new SwimListener(data(row, SwimData.TrajectoryType.MC)).newEvent(null);

        assertEquals(1, Swimming.getMCTrajectories().size());
        assertEquals(0, Swimming.getReconTrajectories().size());
        assertMetadata(row, Swimming.getMCTrajectories().get(0));
    }

    @Test
    void routesReconstructedTrajectoryAndCopiesMetadata() {
        TrajectoryRowData row = row("REC::Particle", SwimType.RECONSWIM);

        new SwimListener(data(row, SwimData.TrajectoryType.RECON)).newEvent(null);

        assertEquals(0, Swimming.getMCTrajectories().size());
        assertEquals(1, Swimming.getReconTrajectories().size());
        assertMetadata(row, Swimming.getReconTrajectories().get(0));
    }

    @Test
    void omitsUnsuccessfulSwimsFromTrajectoryRegistries() {
        TrajectoryRowData belowMinimumMomentum = row("MC::Particle", SwimType.MCSWIM, 0.001);

        new SwimListener(data(belowMinimumMomentum, SwimData.TrajectoryType.MC)).newEvent(null);

        assertEquals(0, Swimming.getMCTrajectories().size());
        assertEquals(0, Swimming.getReconTrajectories().size());
    }

    private SwimData data(TrajectoryRowData row, SwimData.TrajectoryType type) {
        return new SwimData(row, type, 10.0, 0.01, 1.0e-9, swimmer);
    }

    private static TrajectoryRowData row(String source, SwimType swimType) {
        return row(source, swimType, 1000.0);
    }

    private static TrajectoryRowData row(String source, SwimType swimType, double momentum) {
        LundId electron = LundSupport.getInstance().get(11);
        return new TrajectoryRowData(1, electron, 0.0, 0.0, 0.0,
                momentum, 90.0, 0.0, 0, source, swimType);
    }

    private static void assertMetadata(TrajectoryRowData row, SwimTrajectory trajectory) {
        assertEquals(row.getSource(), trajectory.getSource());
        assertSame(row.getLundId(), trajectory.getLundId());
        assertNotNull(trajectory.getGeneratedParticleRecord());
        assertEquals(10.0, trajectory.getFinalR(), 1.0e-7);
    }
}
