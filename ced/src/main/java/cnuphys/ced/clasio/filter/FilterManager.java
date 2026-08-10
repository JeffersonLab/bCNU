package cnuphys.ced.clasio.filter;

import java.util.ArrayList;

import cnuphys.ced.frame.Ced;

public class FilterManager extends ArrayList<IEventFilter> {


	// singleton
	private static volatile FilterManager _instance;

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
			for (IEventFilter filter : this) {
				if (filter.isActive()) {
					return true;
				}
		}
		return false;
	}

	/** Register a filter once, preserving filter evaluation order. */
	public boolean register(IEventFilter filter) {
		if (filter == null || contains(filter)) return false;
		return add(filter);
	}


	/**
	 * Do this late in ced initialization
	 */
	public void setUpFilterMenu() {
		if (!isEmpty()) {
			for (IEventFilter filter : this) {
				Ced.getCed().getEventFilterMenu().add(filter.getMenuComponent());
			}
		}
	}


	/**
	 * Does the event pass all the active registered filters?
	 * @return <code>true</code> if the event passes all the filters
	 */
	public boolean pass() {

		if (!isEmpty()) {
			for(IEventFilter filter : this) {
				if (filter.isActive()) {
					boolean pass = filter.pass();

					if (!pass) {
						return false;
					}
				}
			}
		}
		return true;
	}


}
