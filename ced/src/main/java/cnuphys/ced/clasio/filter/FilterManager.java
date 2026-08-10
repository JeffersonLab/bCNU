package cnuphys.ced.clasio.filter;

import java.util.ArrayList;
import java.util.List;

import cnuphys.ced.frame.Ced;

public final class FilterManager {


	// singleton
	private static volatile FilterManager _instance;
	private final List<IEventFilter> filters = new ArrayList<>();

	// private constructor for singleton
	private FilterManager() {
		//trigger filter added by trigger manager
		//add other standard filters
	}

	/**
	 * Public access to the FilterManager
	 *
	 * @return the FilterManager singleton
	 */
	public static FilterManager getInstance() {
		if (_instance == null) {
			synchronized (FilterManager.class) {
				if (_instance == null) {
					_instance = new FilterManager();
				}
			}
		}
		return _instance;
	}

	/**
	 * Check if there are any active filters
	 *
	 * @return <code>true</code> if there are any active filters
	 */
	public boolean isFilteringOn() {
		for (IEventFilter filter : snapshot()) {
				if (filter.isActive()) {
					return true;
				}
		}
		return false;
	}

	/** Register a filter once, preserving filter evaluation order. */
	public synchronized boolean register(IEventFilter filter) {
		if (filter == null || filters.contains(filter)) return false;
		return filters.add(filter);
	}

	/** Remove a previously registered filter. */
	synchronized boolean unregister(IEventFilter filter) {
		return filters.remove(filter);
	}


	/**
	 * Do this late in ced initialization
	 */
	public void setUpFilterMenu() {
		for (IEventFilter filter : snapshot()) {
			Ced.getCed().getEventFilterMenu().add(filter.getMenuComponent());
		}
	}


	/**
	 * Does the event pass all the active registered filters?
	 * @return <code>true</code> if the event passes all the filters
	 */
	public boolean pass() {

		for (IEventFilter filter : snapshot()) {
			if (filter.isActive()) {
				if (!filter.pass()) {
					return false;
				}
			}
		}
		return true;
	}

	private synchronized List<IEventFilter> snapshot() {
		return List.copyOf(filters);
	}


}
