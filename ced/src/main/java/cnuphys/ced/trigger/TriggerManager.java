package cnuphys.ced.trigger;

import org.jlab.io.base.DataEvent;

import cnuphys.ced.alldata.DataWarehouse;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.ClasIoEventManager.EventSourceType;
import cnuphys.ced.clasio.IClasIoEventListener;
import cnuphys.ced.clasio.filter.FilterManager;

public class TriggerManager implements IClasIoEventListener {

	//data warehouse
	private DataWarehouse _dataWarehouse = DataWarehouse.getInstance();

	// singleton
	private static volatile TriggerManager _instance;

	// the bank name
	private static String _bankName = "RUN::trigger";

	// the trigger filter
	private static TriggerFilter _filter;

	// the data columns in the Run::trigger bank
	private int _id[];
	private int _trigger[];

	// private constructor for singleton
	private TriggerManager() {
		ClasIoEventManager.getInstance().addClasIoEventListener(this, 2);
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
					_instance = new TriggerManager();
					_filter = new TriggerFilter.Builder().setActive(false).setBits(0xFFFFFFFF)
							.setType(TriggerMatch.ANY).setName("Trigger Filter").build();
				}
			}
		}


		FilterManager.getInstance().add(_filter);
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

			_id = _dataWarehouse.getInt(_bankName, "id");
			if (_id != null) {
				_trigger = _dataWarehouse.getInt(_bankName, "trigger");
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
