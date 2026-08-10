package cnuphys.ced.clasio.filter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.swing.JComponent;

import org.junit.jupiter.api.Test;

class FilterManagerTest {

    @Test
    void registersSameFilterOnlyOnce() {
        FilterManager manager = FilterManager.getInstance();
        IEventFilter filter = new StubFilter();
        try {
            assertTrue(manager.register(filter));
            assertFalse(manager.register(filter));
        } finally {
            manager.remove(filter);
        }
    }

    private static final class StubFilter implements IEventFilter {
        @Override public boolean pass() { return true; }
        @Override public void setActive(boolean active) {}
        @Override public boolean isActive() { return false; }
        @Override public void setName(String name) {}
        @Override public String getName() { return "test"; }
        @Override public JComponent getMenuComponent() { return null; }
        @Override public void edit() {}
        @Override public void savePreferences() {}
        @Override public void readPreferences() {}
    }
}
