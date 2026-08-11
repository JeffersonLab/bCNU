package cnuphys.ced.alldata;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
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

    @Test
    void returnsSortedBankNamesWithoutMutatingTheEventArray() {
        String[] eventBanks = { "REC::Particle", "RUN::config", "BMT::adc" };

        String[] sorted = DataWarehouse.sortedCopy(eventBanks);

        assertArrayEquals(new String[] { "BMT::adc", "REC::Particle", "RUN::config" }, sorted);
        assertArrayEquals(new String[] { "REC::Particle", "RUN::config", "BMT::adc" }, eventBanks);
        assertNull(DataWarehouse.sortedCopy(null));
    }

    @Test
    void publishesCompleteSortedSchemaSnapshots() {
        DataWarehouse warehouse = DataWarehouse.getInstance();
        SchemaFactory schemas = new SchemaFactory();
        schemas.addSchema(new Schema("RUN::config", 1, 1));
        schemas.addSchema(new Schema("BMT::adc", 2, 1));

        try {
            warehouse.updateSchema(schemas);
            assertArrayEquals(new String[] { "BMT::adc", "RUN::config" }, warehouse.getKnownBanks());

            warehouse.updateSchema(new SchemaFactory());
            assertArrayEquals(new String[0], warehouse.getKnownBanks());
        } finally {
            warehouse.updateSchema(null);
        }
    }

    @Test
    void buildsColumnSnapshotFromOnlyBanksPresentInEvent() {
        DataWarehouse warehouse = DataWarehouse.getInstance();
        SchemaFactory schemas = new SchemaFactory();
        Schema run = new Schema("RUN::config", 1, 1);
        run.addEntry("event", "I", "event number");
        Schema adc = new Schema("BMT::adc", 2, 1);
        adc.addEntry("ADC", "I", "ADC value");
        adc.addEntry("sector", "B", "sector number");
        schemas.addSchema(run);
        schemas.addSchema(adc);

        String[] eventBanks = { "RUN::config", "BMT::adc" };
        String[] adcColumns = { "sector", "ADC" };
        List<String> requestedBanks = new ArrayList<>();
        DataBank runBank = bank(new String[] { "event" });
        DataBank adcBank = bank(adcColumns);
        DataEvent event = (DataEvent) Proxy.newProxyInstance(DataEvent.class.getClassLoader(),
                new Class<?>[] { DataEvent.class }, (proxy, method, args) -> switch (method.getName()) {
                    case "getBankList" -> eventBanks;
                    case "hasBank" -> List.of(eventBanks).contains(args[0]);
                    case "getBank" -> {
                        String name = (String) args[0];
                        requestedBanks.add(name);
                        yield "RUN::config".equals(name) ? runBank : adcBank;
                    }
                    default -> null;
                });

        try {
            warehouse.updateSchema(schemas);
            warehouse.newClasIoEvent(event);

            assertEquals(List.of("BMT::adc", "RUN::config"), requestedBanks);
            assertArrayEquals(new String[] { "RUN::config", "BMT::adc" }, eventBanks);
            assertArrayEquals(new String[] { "sector", "ADC" }, adcColumns);
            assertEquals(List.of("BMT::adc.ADC", "BMT::adc.sector", "RUN::config.event"),
                    warehouse.getColumnData().stream().map(column -> column.fullName).toList());
        } finally {
            warehouse.updateSchema(null);
        }
    }

    @Test
    void clearsColumnSnapshotForNullEvent() {
        DataWarehouse warehouse = DataWarehouse.getInstance();
        warehouse.newClasIoEvent(null);
        assertTrue(warehouse.getColumnData().isEmpty());
    }

    private static DataBank bank(String[] columns) {
        return (DataBank) Proxy.newProxyInstance(DataBank.class.getClassLoader(),
                new Class<?>[] { DataBank.class }, (proxy, method, args) -> switch (method.getName()) {
                    case "getColumnList" -> columns;
                    default -> null;
                });
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type },
                (instance, method, args) -> null);
    }
}
