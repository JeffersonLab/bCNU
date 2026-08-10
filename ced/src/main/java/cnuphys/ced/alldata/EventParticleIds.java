package cnuphys.ced.alldata;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;

/** Finds the particle identities exposed by particle-like banks in the current event. */
public final class EventParticleIds {

    private static final EventParticleIds INSTANCE = new EventParticleIds(
            () -> DataWarehouse.getInstance().getCurrentEvent());

    private final Supplier<DataEvent> eventSupplier;

    private EventParticleIds(Supplier<DataEvent> eventSupplier) { this.eventSupplier = eventSupplier; }

    static EventParticleIds forTesting(Supplier<DataEvent> eventSupplier) {
        return new EventParticleIds(eventSupplier);
    }

    public static EventParticleIds getInstance() { return INSTANCE; }

    public List<LundId> uniqueLundIds() {
        List<LundId> result = new ArrayList<>();
        DataEvent event = eventSupplier.get();
        if (event == null || event.getBankList() == null) return result;

        for (String bankName : event.getBankList()) {
            if (!isParticleBank(bankName)) continue;
            DataBank bank = event.getBank(bankName);
            if (bank == null || !DataWarehouse.hasColumn(bank, "pid")) continue;

            for (int row = 0; row < bank.rows(); row++) {
                LundId id = LundSupport.getInstance().get(bank.getInt("pid", row));
                if (id != null) {
                    result.remove(id);
                    result.add(id);
                }
            }
        }
        return result;
    }

    private static boolean isParticleBank(String bankName) {
        return bankName != null && (bankName.contains("::Particle") || bankName.contains("::Lund"));
    }
}
