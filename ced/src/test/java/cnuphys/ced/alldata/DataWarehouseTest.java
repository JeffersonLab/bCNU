package cnuphys.ced.alldata;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.junit.jupiter.api.Test;

class DataWarehouseTest {

    @Test
    void findsOnlyBanksReportedPresentByTheEvent() {
        DataBank bank = proxy(DataBank.class);
        AtomicBoolean getBankCalled = new AtomicBoolean();
        DataEvent event = (DataEvent) Proxy.newProxyInstance(DataEvent.class.getClassLoader(),
                new Class<?>[] { DataEvent.class }, (proxy, method, args) -> switch (method.getName()) {
                    case "hasBank" -> "REC::Particle".equals(args[0]);
                    case "getBank" -> {
                        getBankCalled.set(true);
                        yield bank;
                    }
                    default -> null;
                });

        assertNull(DataWarehouse.findBank(null, "REC::Particle"));
        assertNull(DataWarehouse.findBank(event, null));
        assertNull(DataWarehouse.findBank(event, "MISSING::Bank"));
        assertFalse(getBankCalled.get());
        assertSame(bank, DataWarehouse.findBank(event, "REC::Particle"));
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type },
                (instance, method, args) -> null);
    }
}
