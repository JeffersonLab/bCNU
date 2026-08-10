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
            manager.unregister(filter);
        }
    }

	@Test
	void evaluatesActiveFiltersInRegistrationOrder() {
		FilterManager manager = FilterManager.getInstance();
		StubFilter inactive = new StubFilter(false, false);
		StubFilter passing = new StubFilter(true, true);
		StubFilter rejecting = new StubFilter(true, false);
		try {
			manager.register(inactive);
			manager.register(passing);
			manager.register(rejecting);

			assertFalse(manager.pass());
			assertFalse(inactive.evaluated);
			assertTrue(passing.evaluated);
			assertTrue(rejecting.evaluated);
		} finally {
			manager.unregister(inactive);
			manager.unregister(passing);
			manager.unregister(rejecting);
		}
	}

    private static final class StubFilter implements IEventFilter {
		private boolean active;
		private final boolean result;
		private boolean evaluated;

		private StubFilter() { this(false, true); }
		private StubFilter(boolean active, boolean result) {
			this.active = active;
			this.result = result;
		}

		@Override public boolean pass() { evaluated = true; return result; }
		@Override public void setActive(boolean active) { this.active = active; }
		@Override public boolean isActive() { return active; }
        @Override public void setName(String name) {}
        @Override public String getName() { return "test"; }
        @Override public JComponent getMenuComponent() { return null; }
        @Override public void edit() {}
        @Override public void savePreferences() {}
        @Override public void readPreferences() {}
    }
}
