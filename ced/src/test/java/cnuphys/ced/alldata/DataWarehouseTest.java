package cnuphys.ced.alldata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.jnp.hipo4.data.Schema;
import org.jlab.jnp.hipo4.data.SchemaFactory;
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

    @Test
    void findsOnlyColumnsReportedByTheBank() {
        DataBank bank = (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(),
                new Class<?>[] { DataBank.class }, (proxy, method, args) -> switch (method.getName()) {
                    case "getColumnList" -> new String[] { "pid", "px", "py", "pz" };
                    default -> null;
                });

        assertFalse(DataWarehouse.hasColumn(null, "pid"));
        assertFalse(DataWarehouse.hasColumn(bank, null));
        assertFalse(DataWarehouse.hasColumn(bank, "charge"));
        assertTrue(DataWarehouse.hasColumn(bank, "pid"));
    }

    @Test
    void handlesMissingSchemasAndColumnsAsUnknown() {
        SchemaFactory schemas = new SchemaFactory();
        Schema particle = new Schema("REC::Particle", 1, 1);
        schemas.addSchema(particle);

        assertNull(DataWarehouse.findSchema(null, "REC::Particle"));
        assertNull(DataWarehouse.findSchema(schemas, null));
        assertNull(DataWarehouse.findSchema(schemas, "MISSING::Bank"));
        assertSame(particle, DataWarehouse.findSchema(schemas, "REC::Particle"));
        assertEquals(DataWarehouse.UNKNOWN, DataWarehouse.columnType(schemas, "REC::Particle", "missing"));
        assertEquals("Unknown", DataWarehouse.typeName(-1));
        assertEquals("Unknown", DataWarehouse.typeName(100));
        assertEquals("int", DataWarehouse.typeName(DataWarehouse.INT32));
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type },
                (instance, method, args) -> null);
    }
}
