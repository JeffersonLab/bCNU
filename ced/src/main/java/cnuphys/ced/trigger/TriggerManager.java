package cnuphys.ced.trigger;

import org.jlab.io.base.DataEvent;

import cnuphys.ced.alldata.RunTriggers;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.ClasIoEventListenerPhase;
import cnuphys.ced.clasio.ClasIoEventManager.EventSourceType;
import cnuphys.ced.clasio.IClasIoEventListener;
import cnuphys.ced.clasio.filter.FilterManager;

public class TriggerManager implements IClasIoEventListener {

	// singleton
	private static volatile TriggerManager _instance;

	// the trigger filter
	private static TriggerFilter _filter;

	// the data columns in the Run::trigger bank
	private int _id[];
	private int _trigger[];

	// private constructor for singleton
	private TriggerManager() {
		ClasIoEventManager.getInstance().addClasIoEventListener(this, ClasIoEventListenerPhase.VIEW);
	}

	/**
	 * Public access to the TriggerManager
	 *
	 * @return the TriggerManager singleton
	 */
	public static TriggerManager getInstance() {

		if (_instance == null) {
			synchronized (TriggerManager.class) {
				if (_instance == null) {
					_filter = new TriggerFilter.Builder().setActive(false).setBits(0xFFFFFFFF)
							.setType(TriggerMatch.ANY).setName("Trigger Filter").build();
					TriggerManager manager = new TriggerManager();
					FilterManager.getInstance().register(_filter);
					_instance = manager;
				}
			}
		}

		return _instance;
	}

	/**
	 * Set the active state of the trigger filter. Will take effect
	 *
	 * @param active the active state of the trigger filter
	 */
	public void setFilterActive(boolean active) {
		_filter.setActive(active);
	}

	/**
	 * Get the Trigger filter
	 *
	 * @return the trigger filter
	 */
	protected TriggerFilter getTriggerFilter() {
		return _filter;
	}

	@Override
	public void newClasIoEvent(DataEvent event) {
		if (ClasIoEventManager.getInstance().isAccumulating()) {
		} else { // single event

			_id = null;
			_trigger = null;

			RunTriggers triggers = RunTriggers.getInstance();
			_id = triggers.ids();
			if (_id != null) {
				_trigger = triggers.triggers();
			}

			TriggerDialog.getInstance().setCurrentEvent(_id, _trigger);
		}
	}

	@Override
	public void openedNewEventFile(String path) {
	}

	@Override
	public void changedEventSource(EventSourceType source) {
	}


}
