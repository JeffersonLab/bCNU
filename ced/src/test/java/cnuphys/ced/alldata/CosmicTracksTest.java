package cnuphys.ced.alldata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Proxy;

import org.jlab.io.base.DataBank;
import org.junit.jupiter.api.Test;

class CosmicTracksTest {

    @Test
    void readsEveryBankFieldAndEvaluatesTrackLines() {
        CosmicTracks tracks = CosmicTracks.forTesting(CosmicTracksTest::bank);

        assertEquals(1, tracks.count());
        assertEquals(7, tracks.id(0));
        assertEquals(2.5f, tracks.xAtY(0, 10), 1.0e-6f);
        assertEquals(-1.0f, tracks.zAtY(0, 10), 1.0e-6f);
        assertEquals(3.5f, tracks.chi2(0), 1.0e-6f);
        assertEquals(1.2f, tracks.phi(0), 1.0e-6f);
        assertEquals(2.1f, tracks.theta(0), 1.0e-6f);
    }

    private static DataBank bank() {
        return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "rows" -> 1;
                    case "getShort" -> (short) 7;
                    case "getFloat" -> switch ((String) args[0]) {
                        case "trkline_yx_interc" -> 0.5f;
                        case "trkline_yx_slope" -> 0.2f;
                        case "trkline_yz_interc" -> 2.0f;
                        case "trkline_yz_slope" -> -0.3f;
                        case "chi2" -> 3.5f;
                        case "phi" -> 1.2f;
                        case "theta" -> 2.1f;
                        default -> Float.NaN;
                    };
                    default -> null;
                });
    }
}
