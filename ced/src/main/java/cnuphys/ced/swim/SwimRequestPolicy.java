package cnuphys.ced.swim;

import cnuphys.lund.LundId;
import cnuphys.lund.TrajectoryRowData;

final class SwimRequestPolicy {

    static final double DEFAULT_MAX_PATH = 900.0;
    static final double CVT_MAX_PATH = 150.0;

    private SwimRequestPolicy() {
    }

    static double maxPathForRecon(String source) {
        return source != null && source.contains("CVT") ? CVT_MAX_PATH : DEFAULT_MAX_PATH;
    }

    static String mcDuplicateKey(LundId lundId, TrajectoryRowData row) {
        return String.format("%s %10.6f  %10.6f  %10.6f  %10.6f  %10.6f  %10.6f",
                lundId.getName(), row.getXo(), row.getYo(), row.getZo(),
                row.getMomentum(), row.getTheta(), row.getPhi());
    }
}
