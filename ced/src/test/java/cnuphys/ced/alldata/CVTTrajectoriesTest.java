package cnuphys.ced.alldata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Point;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.jlab.io.base.DataBank;
import org.junit.jupiter.api.Test;

class CVTTrajectoriesTest {

    @Test
    void readsRegularTrajectoryAndPreservesHitTesting() {
        CVTTrajectories data = CVTTrajectories.forTesting(CVTTrajectories.Kind.RECONSTRUCTED,
                CVTTrajectoriesTest::regularBank);

        assertEquals(1, data.count());
        assertEquals(1, data.id(0));
        assertEquals(13.471f, data.path(0), 1.0e-5f);
        data.setLocation(0, new Point(100, 200));
        assertTrue(data.contains(0, new Point(102, 198)));
        assertFalse(data.contains(0, new Point(120, 200)));

        List<String> feedback = new ArrayList<>();
        data.addFeedback("CVTRecTraj", 0, feedback);
        assertEquals(4, feedback.size());
        assertTrue(feedback.get(2).contains("path 13.471"));
    }

    @Test
    void preservesKalmanFilterIndexFeedback() {
        CVTTrajectories data = CVTTrajectories.forTesting(CVTTrajectories.Kind.KALMAN_FILTER,
                CVTTrajectoriesTest::kalmanBank);
        List<String> feedback = new ArrayList<>();
        data.addFeedback("CVTRecKFTraj", 0, feedback);
        assertEquals(3, feedback.size());
        assertTrue(feedback.get(2).endsWith("index 9"));
    }

    private static DataBank regularBank() { return bank(false); }
    private static DataBank kalmanBank() { return bank(true); }

    private static DataBank bank(boolean kalman) {
        return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "rows" -> 1;
                    case "getShort" -> (short) 1;
                    case "getByte" -> switch ((String) args[0]) {
                        case "detector" -> (byte) 5;
                        case "sector" -> (byte) 8;
                        case "layer" -> (byte) 6;
                        case "index" -> (byte) 9;
                        default -> (byte) 0;
                    };
                    case "getFloat" -> switch ((String) args[0]) {
                        case "x" -> -7.165f;
                        case "y" -> 9.950f;
                        case "z" -> 1.038f;
                        case "path" -> 13.471f;
                        case "phi" -> 1.633f;
                        case "theta" -> 1.202f;
                        case "langle" -> -0.06f;
                        case "centroid" -> 47.00f;
                        default -> kalman ? Float.NaN : 0.0f;
                    };
                    default -> null;
                });
    }
}
