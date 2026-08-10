package cnuphys.ced.alldata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.lang.reflect.Proxy;

import org.jlab.io.base.DataBank;
import org.junit.jupiter.api.Test;

import cnuphys.lund.LundSupport;

class DCTracksTest {

    private static final String[] COLUMNS = {
            "Vtx0_x", "Vtx0_y", "Vtx0_z", "p0_x", "p0_y", "p0_z", "q", "status", "id"
    };

    @Test
    void readsTrackRowsAndPreservesBankIdentity() {
        DCTracks tracks = DCTracks.forTesting("HitBasedTrkg::HBTracks", true, () -> bank(COLUMNS));

        assertEquals(1, tracks.count());
        assertEquals("HitBasedTrkg::HBTracks", tracks.bankName());
        assertEquals(17, tracks.id(0));
        assertEquals(2200, tracks.status(0));
        assertEquals(-1, tracks.charge(0));
        assertEquals(1.0f, tracks.vx(0));
        assertEquals(6.0f, tracks.pz(0));
        assertSame(LundSupport.getHitbased(-1), tracks.lundId(0));
    }

    @Test
    void selectsTrackBasedParticleIdentity() {
        DCTracks tracks = DCTracks.forTesting("TimeBasedTrkg::TBTracks", false, () -> bank(COLUMNS));
        assertSame(LundSupport.getTrackbased(-1), tracks.lundId(0));
    }

    @Test
    void rejectsMissingBankOrRequiredColumns() {
        assertEquals(0, DCTracks.forTesting("test", true, () -> null).count());
        assertEquals(0, DCTracks.forTesting("test", true, () -> bank(new String[] { "q", "id" })).count());
    }

    private static DataBank bank(String[] columns) {
        return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "rows" -> 1;
                    case "getColumnList" -> columns;
                    case "getByte" -> (byte) -1;
                    case "getShort" -> "id".equals(args[0]) ? (short) 17 : (short) 2200;
                    case "getFloat" -> switch ((String) args[0]) {
                        case "Vtx0_x" -> 1.0f;
                        case "Vtx0_y" -> 2.0f;
                        case "Vtx0_z" -> 3.0f;
                        case "p0_x" -> 4.0f;
                        case "p0_y" -> 5.0f;
                        default -> 6.0f;
                    };
                    default -> null;
                });
    }
}
