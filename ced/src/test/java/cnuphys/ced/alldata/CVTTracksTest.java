package cnuphys.ced.alldata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.lang.reflect.Proxy;

import org.jlab.io.base.DataBank;
import org.junit.jupiter.api.Test;

import cnuphys.lund.LundSupport;

class CVTTracksTest {

    private static final String[] COLUMNS = { "q", "pt", "phi0", "d0", "z0", "tandip", "ID" };

    @Test
    void readsTrackRowsAndPreservesBankIdentity() {
        CVTTracks tracks = CVTTracks.forTesting("CVTRec::Tracks", () -> bank(COLUMNS));

        assertEquals(1, tracks.count());
        assertEquals("CVTRec::Tracks", tracks.bankName());
        assertEquals(19, tracks.id(0));
        assertEquals(-1, tracks.charge(0));
        assertEquals(1.0f, tracks.pt(0));
        assertEquals(5.0f, tracks.tanDip(0));
        assertSame(LundSupport.getCVTbased(-1), tracks.lundId(0));
    }

    @Test
    void rejectsMissingBankOrRequiredColumns() {
        assertEquals(0, CVTTracks.forTesting("test", () -> null).count());
        assertEquals(0, CVTTracks.forTesting("test", () -> bank(new String[] { "q", "ID" })).count());
    }

    private static DataBank bank(String[] columns) {
        return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "rows" -> 1;
                    case "getColumnList" -> columns;
                    case "getByte" -> (byte) -1;
                    case "getShort" -> (short) 19;
                    case "getFloat" -> switch ((String) args[0]) {
                        case "pt" -> 1.0f;
                        case "phi0" -> 2.0f;
                        case "d0" -> 3.0f;
                        case "z0" -> 4.0f;
                        default -> 5.0f;
                    };
                    default -> null;
                });
    }
}
