package cnuphys.ced.alldata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.util.List;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.junit.jupiter.api.Test;

import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;

class EventParticleIdsTest {

    @Test
    void findsKnownIdsInParticleBanksAndMovesDuplicatesLast() {
        EventParticleIds ids = EventParticleIds.forTesting(
                () -> event(new String[] { "MC::Particle", "REC::Particle", "DC::hits" }));

        List<LundId> result = ids.uniqueLundIds();
        assertEquals(2, result.size());
        assertEquals(LundSupport.getInstance().get(211), result.get(0));
        assertEquals(LundSupport.getInstance().get(11), result.get(1));
    }

    @Test
    void missingEventHasNoParticleIds() {
        assertTrue(EventParticleIds.forTesting(() -> null).uniqueLundIds().isEmpty());
    }

    private static DataEvent event(String[] bankNames) {
        return (DataEvent) Proxy.newProxyInstance(DataEvent.class.getClassLoader(), new Class<?>[] { DataEvent.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "getBankList" -> bankNames;
                    case "getBank" -> switch ((String) args[0]) {
                        case "MC::Particle" -> bank(new int[] { 11, 211, 99999999 });
                        case "REC::Particle" -> bank(new int[] { 11 });
                        default -> bank(new int[] { 2212 });
                    };
                    default -> null;
                });
    }

    private static DataBank bank(int[] pids) {
        return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(), new Class<?>[] { DataBank.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "rows" -> pids.length;
                    case "getColumnList" -> new String[] { "pid" };
                    case "getInt" -> pids[(int) args[1]];
                    default -> null;
                });
    }
}
