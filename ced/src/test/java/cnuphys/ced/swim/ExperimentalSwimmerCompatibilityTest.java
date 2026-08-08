package cnuphys.ced.swim;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import cnuphys.CLAS12Swim.CLAS12SwimResult;
import cnuphys.CLAS12Swim.CLAS12Swimmer;
import cnuphys.magfield.ZeroProbe;
import cnuphys.swim.Swimming;

class ExperimentalSwimmerCompatibilityTest {

    @Test
    void experimentalTrajectoryWorksWithProductionSwimmingRegistry() {
        CLAS12Swimmer swimmer = new CLAS12Swimmer(new ZeroProbe());
        CLAS12SwimResult result = swimmer.swim(1, 0.0, 0.0, 0.0, 1.0,
                90.0, 0.0, 10.0, 0.01, 1.0e-9);

        assertTrue(result.isSuccess(), result.statusString());

        Swimming.setNotifyOn(false);
        Swimming.clearMCTrajectories();
        try {
            Swimming.addMCTrajectory(result.getTrajectory());
            assertSame(result.getTrajectory(), Swimming.getMCTrajectories().get(0));
        } finally {
            Swimming.clearMCTrajectories();
            Swimming.setNotifyOn(true);
        }
    }
}
