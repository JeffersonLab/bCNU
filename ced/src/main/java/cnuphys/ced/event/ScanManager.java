package cnuphys.ced.event;

import java.util.TreeMap;

import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.log.Log;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.ClasIoEventListenerPhase;
import cnuphys.ced.clasio.ClasIoEventManager.EventSourceType;
import cnuphys.ced.clasio.IClasIoEventListener;

/**
 * This class manages the rapid scanning of events
 * for making as map of event index to true event number
 */
public class ScanManager implements IClasIoEventListener {

	// event manager
	private final ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();

	// singleton
	private static volatile ScanManager _instance;


	//private constructor for singleton
	private ScanManager() {
		_eventManager.addClasIoEventListener(this, ClasIoEventListenerPhase.DERIVED);
	}

	//the map. Keys are true even numbers, values are sequential numbers
	private final TreeMap<Integer, Integer> _map = new TreeMap<>();

	//return to the current event using index
	public int _saveIndex = -1;


	private void scan() {

		//do we already have a map?
		if (!_map.isEmpty() || !_eventManager.isGotoOK()) {
			return;
		}

		int count = _eventManager.getEventCount();
		if (count < 2) {
			return;
		}

		Log.getInstance().info("Scanning event file to create true-to-sequential event map");

		//hold the current index;

		if (_eventManager.getEventSourceType() != EventSourceType.HIPOFILE) {
			return;
		}


		_saveIndex = _eventManager.getSequentialEventNumber();

		_eventManager.setScanning(true);
		try {
			_eventManager.gotoEvent(1);

			for (int i = 1; i < count; i++) {
				_eventManager.getNextEvent();
				if ((i % 100) == 0) {
					Log.getInstance().info("Scanning events " + i + "/" + count);
				}
			}
		} catch (RuntimeException e) {
			_map.clear();
			Log.getInstance().error("Could not create true-to-sequential event map");
			Log.getInstance().exception(e);
		} finally {
			_eventManager.setScanning(false);
		}

	}


	public void gotoTrue(int trueEventNumber) {
		scan();
		Integer enumber = _map.get(trueEventNumber);

		if (enumber == null) {
			Log.getInstance().warning("No event with true number " + trueEventNumber);
			_eventManager.gotoEvent(_saveIndex);
			return;
		}

		_eventManager.gotoEvent(enumber);
	}

	/**
	 * Get the singleton
	 *
	 * @return the singleton
	 */
	public static ScanManager getInstance() {

		if (_instance == null) {
			synchronized (ScanManager.class) {
				if (_instance == null) {
					_instance = new ScanManager();
				}
			}
		}
		return _instance;
	}

	@Override
	public void newClasIoEvent(DataEvent event) {
		// only care if I am scanning
		if (!_eventManager.isScanning() || (event == null)) {
			return;
		}

		int seqNum = _eventManager.getSequentialEventNumber();
		int trueNum = _eventManager.getTrueEventNumber();

		_map.put(trueNum, seqNum);
	}

	@Override
	public void openedNewEventFile(String path) {
		_map.clear();
		_saveIndex = -1;
	}

	@Override
	public void changedEventSource(EventSourceType source) {
		_map.clear();
		_saveIndex = -1;
	}


}
