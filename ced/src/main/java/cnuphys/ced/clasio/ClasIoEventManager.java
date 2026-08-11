package cnuphys.ced.clasio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JInternalFrame;

import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataSource;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioETSource;
import org.jlab.io.evio.EvioSource;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.jnp.hipo4.data.SchemaFactory;

import cnuphys.bCNU.application.Desktop;
import cnuphys.bCNU.dialog.DialogUtilities;
import cnuphys.bCNU.graphics.component.IpField;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.magneticfield.swim.ISwimAll;
import cnuphys.bCNU.threading.EventNotifier;
import cnuphys.ced.alldata.DataWarehouse;
import cnuphys.ced.alldata.EventParticleIds;
import cnuphys.ced.alldata.RunConfig;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.clasio.et.ConnectETDialog;
import cnuphys.ced.clasio.filter.FilterManager;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.event.ScanManager;
import cnuphys.ced.frame.Ced;
import cnuphys.ced.swim.EventTrajectoryUpdate;
import cnuphys.lund.LundId;
import cnuphys.magfield.MagneticFields;

public class ClasIoEventManager {

	// Unique lund ids in the event (if any)
	private volatile List<LundId> _uniqueLundIds;

	// used in pcal and ec hex gradient displays
	private double maxEDepCal[] = { Double.NaN, Double.NaN, Double.NaN };

	// Data from the special run bank
	private volatile RunData _runData = RunData.empty();

	// for HIPO ring
	public IpField _ipField;

	// connect to ring
	public JButton _connectButton;

	// decode legacy EVIO events for the HIPO-based display accessors
	private final EvioToHipoDecoder _evioDecoder = new EvioToHipoDecoder(
			schemaFactory -> DataWarehouse.getInstance().updateSchema(schemaFactory));

	// reset everytime hipo or evio file is opened
	private volatile int _currentEventIndex;
	private volatile boolean _sourceExhausted;

	// sources of events (the type, not the actual source)
	public enum EventSourceType {
		HIPOFILE, ET, EVIOFILE
	}

	// the current source type
	private EventSourceType _sourceType = EventSourceType.HIPOFILE;

	// ET dialog
	private ConnectETDialog _etDialog;

	// flag that set set to <code>true</code> if we are accumulating events
	private volatile boolean _accumulating = false;
	private volatile boolean _updatingEventDisplay;

	// flag that set set to <code>true</code> if we are quickly scanning events events
	private volatile boolean _scanning = false;




	// Listener phases are traversed in enum declaration order: authoritative data,
	// derived processing, then views.
	private final Map<ClasIoEventListenerPhase, EventNotifier<ClasIoEventNotification>> eventNotifiers =
			createEventNotifiers();

	private static Map<ClasIoEventListenerPhase, EventNotifier<ClasIoEventNotification>> createEventNotifiers() {
		Map<ClasIoEventListenerPhase, EventNotifier<ClasIoEventNotification>> notifiers =
				new EnumMap<>(ClasIoEventListenerPhase.class);
		for (ClasIoEventListenerPhase phase : ClasIoEventListenerPhase.values()) {
			notifiers.put(phase, new EventNotifier<>());
		}
		return notifiers;
	}

	// someone who can swim all MC particles
	private ISwimAll _allMCSwimmer;

	// someone who can swim all recon particles
	private ISwimAll _allReconSwimmer;

	// the current port
	private int _currentPort;

	// the current hipo event file
	private File _currentHipoFile;

	// the current evio event file
	private File _currentEvioFile;

	//for ET
	private String _currentMachine;
	private String _currentStation;

	// current ET file
	private String _currentETFile;

	// the clas_io source of events
	private DataSource _dataSource;

	// singleton
	private static volatile ClasIoEventManager instance;

	// the current event
	private volatile DataEvent _currentEvent;

	// private constructor for singleton
	private ClasIoEventManager() {
	}

	/**
	 * Get the run data, changed every time a run bank is encountered
	 *
	 * @return the run data
	 */
	public RunData getRunData() {
		return _runData;
	}

	/**
	 * Set the displayed event to the current event
	 */
	private void setCurrentEvent() {

		try {

			if (_currentEvent != null) {

				if (isAccumulating()) {
					DataWarehouse.getInstance().newClasIoEvent(_currentEvent);
					AccumulationManager.getInstance().newClasIoEvent(_currentEvent);
				}
				else if (isScanning()) {
                    ScanManager.getInstance().newClasIoEvent(_currentEvent);
                }
				else {
					boolean wasUpdating = _updatingEventDisplay;
					_updatingEventDisplay = true;
					try {
						updateRunData();
						notifyEventListeners();
						Ced.refreshEventDisplay();
					} finally {
						_updatingEventDisplay = wasUpdating;
					}
				}
			}
		} catch (Exception e) {
			Log.getInstance().error("Could not update the current event");
			Log.getInstance().exception(e);
		}

	}

	private void updateRunData() {
		RunData next = RunData.from(RunConfig.getInstance().valuesOrNull());
		if (next == null) return;

		RunData previous = _runData;
		_runData = next;
		if (previous.run != next.run) {
			MagneticFields.getInstance().changeFieldsAndMenus(next.torus, next.solenoid);
		}
	}

	/**
	 * Get a collection of unique LundIds in the current event
	 *
	 * @return a collection of unique LundIds
	 */
	public ArrayList<LundId> uniqueLundIds() {

		List<LundId> uniqueLundIds = _uniqueLundIds;
		if (uniqueLundIds == null) {
			uniqueLundIds = List.copyOf(EventParticleIds.getInstance().uniqueLundIds());
			_uniqueLundIds = uniqueLundIds;
		}

		return new ArrayList<>(uniqueLundIds);
	}

	/**
	 * Access for the singleton
	 *
	 * @return the singleton
	 */
	public static ClasIoEventManager getInstance() {
		if (instance == null) {
			synchronized (ClasIoEventManager.class) {
				if (instance == null) {
					instance = new ClasIoEventManager();
				}
			}
		}
		return instance;
	}

	/**
	 * Get info about the current event
	 *
	 * @return
	 */
	public String currentInfoString() {
		StringBuilder sb = new StringBuilder(256);

		try {
			if (_currentEvent != null) {
				int seqNum = getSequentialEventNumber();
				int trueNum = getTrueEventNumber();

				sb.append("Event source: " + _sourceType.name() + "\n");

				switch (_sourceType) {
				case HIPOFILE:
					sb.append("File:  " + _currentHipoFile.getPath() + "\n");
					sb.append("Sequential number: " + seqNum + "\n");
					sb.append("True number: " + trueNum + "\n");
					break;

				case ET:
					sb.append("ET Machine: " + _currentMachine + "\n");
					sb.append("ET Station: " + _currentStation + "\n");
					break;

				case EVIOFILE:
					sb.append("File:  " + _currentEvioFile.getPath() + "\n");
					sb.append("Sequential number: " + seqNum + "\n");
					sb.append("True number: " + trueNum + "\n");
					break;
				}
			} else {
				sb.append("No event loaded.\n");
			}
		} catch (Exception e) {
			sb.append("Exception in currentInfoString: " + e.getMessage() + "\n");
		}

		return sb.toString();
	}

	/**
	 * Are we accumulating?
	 * @return the accumulating flag
	 */
	public boolean isAccumulating() {
		return _accumulating;
	}

	/**
	 * Set whether we are accumulating
	 * @param accumulating the accumulating to set
	 */
	public void setAccumulating(boolean accumulating) {
		_accumulating = accumulating;
	}

	/**
	 * Are we scanning?
	 *
	 * @return the scanning flag
	 */
	public boolean isScanning() {
		return _scanning;
	}

	/**
	 * Set whether we are scanning
	 * @param scanning the scanning to set
	 */
	public void setScanning(boolean scanning) {
		_scanning = scanning;
	}


	/**
	 * Get the current event
	 *
	 * @return the current event
	 */
	public DataEvent getCurrentEvent() {
		return _currentEvent;
	}
	
	/**
	 * Check whether there is a current event
	 *
	 * @return <code>true</code> if there is a current event
	 */
	public boolean hasCurrentEvent() {
		return _currentEvent != null;
	}

	/**
	 * Get a description of the current event source.
	 * @return a description of the current event source.
	 */
	public String getCurrentSourceDescription() {

		if ((_sourceType == EventSourceType.HIPOFILE) && (_currentHipoFile != null)) {
			return "Hipo " + _currentHipoFile.getName();
		} else if ((_sourceType == EventSourceType.EVIOFILE) && (_currentEvioFile != null)) {
			return "Evio " + _currentEvioFile.getName();
		}
		else if ((_sourceType == EventSourceType.ET) && (_currentMachine != null) && (_currentETFile != null)) {
			return "ET " + _currentMachine + " " + _currentETFile;
		}
		return "(none)";
	}

	/**
	 * Open an event file
	 *
	 * @param file the event file
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void openHipoEventFile(File file) throws FileNotFoundException, IOException {

		if (!file.exists()) {
			throw (new FileNotFoundException("Event event file not found"));
		}
		if (!file.canRead()) {
			throw (new FileNotFoundException("Event file cannot be read"));
		}

		HipoDataSource hipoSource = new HipoDataSource();
		_dataSource = openReplacement(_dataSource, hipoSource, file.getPath());
		_currentHipoFile = file;


		//let the data manager know
		SchemaFactory schemaFactory = hipoSource.getReader().getSchemaFactory();
		DataWarehouse.getInstance().updateSchema(schemaFactory);

		//notify the listeners
		notifyEventListeners(_currentHipoFile);
		setEventSourceType(EventSourceType.HIPOFILE);

		reset();

		// auto go to first event
		try {
			getNextEvent();
		} catch (Exception e) {
			Log.getInstance().error("Could not read the first HIPO event from " + file);
			Log.getInstance().exception(e);
		}
	}


	//partial reset for new event source
	private void reset() {
		_runData = RunData.empty();
		_currentEvent = null;
		_currentEventIndex = 0;
		_sourceExhausted = false;
	}

	/**
	 * Open an evio event file
	 *
	 * @param file the event file
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void openEvioEventFile(File file) throws FileNotFoundException, IOException {

		if (!file.exists()) {
			throw (new FileNotFoundException("Event event file not found"));
		}
		if (!file.canRead()) {
			throw (new FileNotFoundException("Event file cannot be read"));
		}

		EvioSource evioSource = new EvioSource();
		_dataSource = openReplacement(_dataSource, evioSource, file.getPath());
		_currentEvioFile = file;
		notifyEventListeners(_currentEvioFile);
		setEventSourceType(EventSourceType.EVIOFILE);

		reset();

		// TODO check if I need to skip the first event

		try {
			getNextEvent();
		} catch (Exception e) {
			Log.getInstance().error("Could not read the first EVIO event from " + file);
			Log.getInstance().exception(e);
		}
	}

	static DataSource openReplacement(DataSource current, DataSource replacement, String path) {
		try {
			replacement.open(path);
		} catch (RuntimeException exception) {
			try {
				replacement.close();
			} catch (RuntimeException closeException) {
				exception.addSuppressed(closeException);
			}
			throw exception;
		}

		if (current != null) {
			current.close();
		}
		return replacement;
	}

	/**
	 * Connect to an ET ring
	 */
	public void ConnectToETRing() {

		if (_etDialog == null) {
			_etDialog = new ConnectETDialog();
		}
		_etDialog.setVisible(true);

		if (_etDialog.reason() == DialogUtilities.OK_RESPONSE) {
			String machine = _etDialog.getMachine();
			String etFile = _etDialog.getFile();
			String station = _etDialog.getStation();
			int port = _etDialog.getPort();

			Log.getInstance().info("ET file: " + etFile);

			try {
				EvioETSource etSource = new EvioETSource(machine, port, station);
				Log.getInstance().info("Connecting to ET ring using " + etFile);
				_dataSource = openReplacement(_dataSource, etSource, etFile);
				_currentMachine = machine;
				_currentETFile = etFile;
				_currentStation = station;
				_currentPort = port;
				reset();
				setEventSourceType(EventSourceType.ET);

				//auto select events every 2 sec
				Ced.getCed().getEventMenu().autoCheckAuto();
			} catch (Exception e) {
				String message = "Could not connect to ET Ring [" + e.getMessage() + "]";
				Log.getInstance().error(message);
				Log.getInstance().exception(e);
			}

		} // end ok

	}


	/**
	 * Get the current event source type
	 *
	 * @return the current event source type
	 */
	public EventSourceType getEventSourceType() {
		return _sourceType;
	}

	/**
	 * Set the soure type
	 *
	 * @param type the new source type
	 */
	public void setEventSourceType(EventSourceType type) {
		if (_sourceType != type) {
			_sourceType = type;
			notifyEventListeners(_sourceType);
		}
		Ced.getCed().fixEventCount();
	}

	/**
	 * Check whether current event source type is a hipo file
	 *
	 * @return <code>true</code> is source type is a hipo file.
	 */
	public boolean isSourceHipoFile() {
		return getEventSourceType() == EventSourceType.HIPOFILE;
	}

	/**
	 * Check whether current event source type is an evio file
	 *
	 * @return <code>true</code> is source type is an evio file.
	 */
	public boolean isSourceEvioFile() {
		return getEventSourceType() == EventSourceType.EVIOFILE;
	}

	/**
	 * Check whether current event source type is the ET ring
	 *
	 * @return <code>true</code> is source type is the ET ring.
	 */
	public boolean isSourceET() {
		return getEventSourceType() == EventSourceType.ET;
	}

	/**
	 * Get the number of events available, 0 for ET since that is unknown.
	 *
	 * @return the number of events available
	 */
	public int getEventCount() {

		int evcount = 0;
		if (isSourceHipoFile()) {
			evcount = (_dataSource == null) ? 0 : _dataSource.getSize();
		} else if (isSourceEvioFile()) {
			evcount = (_dataSource == null) ? 0 : _dataSource.getSize();
		}
		else if (isSourceET()) {
			return Integer.MAX_VALUE;
		}
		return evcount;
	}

	/**
	 * Get the sequential number of the current event, 0 if there is none
	 *
	 * @return the sequential number of the current event.
	 */
	public int getSequentialEventNumber() {
		return _currentEventIndex;
	}

	/**
	 * Get the true event number of the current event, 1 if there is none.
	 * The value comes from the RUN::config bank
	 *
	 * @return the true number of the current event.
	 */
	public int getTrueEventNumber() {
		return (_currentEvent == null) ? -1 : RunConfig.getInstance().eventOrMinusOne();
	}


	/**
	 * Determines whether any next event control should be enabled.
	 *
	 * @return <code>true</code> if any next event control should be enabled.
	 */
	public boolean isNextOK() {
		if (_sourceExhausted && !isSourceET()) return false;

		boolean isOK = true;
		EventSourceType estype = getEventSourceType();

		switch (estype) {
		case HIPOFILE:
			isOK = (isSourceHipoFile() && (getEventCount() > 0) && (getSequentialEventNumber() < getEventCount()));
			break;
		case EVIOFILE:
			isOK = (isSourceEvioFile() && (getEventCount() > 0) && (getSequentialEventNumber() < getEventCount()));
			break;
		case ET:
			isOK = true;
			break;
		}

		return isOK;
	}

	/**
	 * Obtain the number of remaining events. For a file source it is what you
	 * expect. For an et source, it is arbitrarily set to a large number
	 *
	 * @return the number of remaining events
	 */
	public int getNumRemainingEvents() {
		if (_sourceExhausted && !isSourceET()) return 0;

		int numRemaining = 0;
		EventSourceType estype = getEventSourceType();

		switch (estype) {
		case HIPOFILE:
		case EVIOFILE:
			numRemaining = getEventCount() - getSequentialEventNumber();
			break;
		case ET:
			numRemaining = Integer.MAX_VALUE;
		}

		return numRemaining;
	}

	/**
	 * Determines whether any prev event control should be enabled.
	 *
	 * @return <code>true</code> if any prev event control should be enabled.
	 */
	public boolean isPrevOK() {
		return isGotoOK() && (_currentEventIndex > 1);
	}

	/**
	 * Determines whether any goto event control should be enabled.
	 *
	 * @return <code>true</code> if any prev event control should be enabled.
	 */
	public boolean isGotoOK() {
		return (isSourceHipoFile() || isSourceEvioFile()) && (getEventCount() > 0);
	}

	/**
	 * Set the object that can swim all MonteCarlo particles
	 *
	 * @param allSwimmer the object that can swim all MonteCarlo particles
	 */
	public void setAllMCSwimmer(ISwimAll allSwimmer) {
		_allMCSwimmer = allSwimmer;
	}

	/**
	 * Set the object that can swim all reconstructed particles
	 *
	 * @param allSwimmer the object that can swim all reconstructed particles
	 */
	public void setAllReconSwimmer(ISwimAll allSwimmer) {
		_allReconSwimmer = allSwimmer;
	}

	DataEvent readNextDecodedEvent() {
		if (_dataSource == null || (_sourceExhausted && !isSourceET())) return null;
		if (!_dataSource.hasEvent()) {
			markSourceExhausted();
			return null;
		}

		DataEvent event;
		try {
			event = _dataSource.getNextEvent();
		} catch (IndexOutOfBoundsException exception) {
			markSourceExhausted();
			Log.getInstance().warning("Event source reached an inconsistent end-of-file boundary at event "
					+ _currentEventIndex);
			return null;
		}
		if (event == null) {
			markSourceExhausted();
			return null;
		}
		_currentEventIndex++;
		return event instanceof EvioDataEvent evioEvent ? _evioDecoder.decode(evioEvent) : event;
	}

	private void markSourceExhausted() {
		if (!isSourceET()) _sourceExhausted = true;
	}

	/**
	 * Get the previous event from the current  reader
	 * @return the next event, if possible
	 */
	public DataEvent getPreviousEvent() {
		return gotoEvent(_currentEventIndex - 1);
	}


	/**
	 * Get the next event from the current  reader
	 * @return the next event, if possible
	 */
	public DataEvent getNextEvent() {

		EventSourceType estype = getEventSourceType();
		boolean done = false; //for filters

		switch (estype) {

		case HIPOFILE:
		case EVIOFILE:

			while (!done) {

				_currentEvent = readNextDecodedEvent();

				done = (_currentEvent == null) || FilterManager.getInstance().pass();
			}
			break;

		case ET:
			int maxTries = 30;
			int attempts = 0;

			_dataSource.waitForEvents();
			while ((attempts < maxTries) && !_dataSource.hasEvent()) {
				try {
					attempts++;
					Thread.sleep(50);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					Log.getInstance().warning("Interrupted while waiting for ET events");
					return null;
				}
				_dataSource.waitForEvents();
			}

			_currentEvent = null;

			if (_dataSource.hasEvent()) {

				_currentEvent = readNextDecodedEvent();

				if (!FilterManager.getInstance().pass()) {
					_currentEvent = null;
				}

				break;
			}

			break; // end case ET

		} // end switch

		setCurrentEvent();
		return _currentEvent;
	}


	/**
	 * See if another event is available
	 *
	 * @return <code>true</code> if another event is available
	 */
	public boolean hasEvent() {
		if (_sourceExhausted && !isSourceET()) return false;

		EventSourceType estype = getEventSourceType();
		switch (estype) {
		case HIPOFILE:
		case ET:
		case EVIOFILE:
			boolean hasETEvent = ((_dataSource != null) && _dataSource.hasEvent());
			return hasETEvent;
		default:
			return true;
		}
	}


	// skip a number of events
	private void skipEvents(int n) {
		if (n < 1) {
			return;
		}

		EventSourceType estype = getEventSourceType();

		switch (estype) {
		case HIPOFILE:
		case EVIOFILE:
			int numRemaining = getNumRemainingEvents();
			n = Math.min(numRemaining, n);

			int stopIndex = _currentEventIndex + n;
			boolean done = false;

			while (!done && (_currentEventIndex < stopIndex)) {
				if (hasEvent()) {
					_currentEvent = readNextDecodedEvent();
					if (_currentEvent == null) done = true;
				}
				else {
					done = true;
				}
			}

			break;

		case ET:
			break;
		}
	}


	/**
	 *
	 * @param eventNumber a 1-based number 1..num events in file
	 * @return the event at the given number (if possible).
	 */
	public DataEvent gotoEvent(int eventNumber) {

		if ((eventNumber < 1) || (eventNumber == _currentEventIndex) || (eventNumber > getEventCount())) {
			return _currentEvent;
		}

		EventSourceType estype = getEventSourceType();
		switch (estype) {

		case HIPOFILE:
			if (eventNumber > _currentEventIndex) {
				int numToSkip = (eventNumber - _currentEventIndex) - 1;
				skipEvents(numToSkip);
				getNextEvent();
			} else {
				_dataSource.close();
				_currentEvent = null;
				_currentEventIndex = 0;
				_sourceExhausted = false;
				_dataSource.open(_currentHipoFile);
				gotoEvent(eventNumber);
			}

			break;

		case EVIOFILE:
			_sourceExhausted = false;
			_currentEvent = _dataSource.gotoEvent(eventNumber);
			if ((_currentEvent != null) && (_currentEvent instanceof EvioDataEvent)) {
				_currentEvent = _evioDecoder.decode((EvioDataEvent)_currentEvent);
				_currentEventIndex = eventNumber;
			}
			break;

			default:
				break;
		}


		setCurrentEvent();
		return _currentEvent;
	}

	/**
	 * Reload the current event
	 *
	 * @return the same current event
	 */
	public DataEvent reloadCurrentEvent() {

		if (_currentEvent != null) {
			notifyEventListeners();
		}
		return _currentEvent;
	}

	/**
	 * Notify listeners we have opened a new file
	 *
	 * @param path the path to the new file
	 */
	private void notifyEventListeners(EventSourceType source) {
		if (_dataSource != null) {
			_currentEvent = null;
			_currentEventIndex = 0;
		}

		clearTrajectoriesWithoutNotification();
		notifyByPhase(new ClasIoEventNotification.SourceChanged(source));

		Ced.getCed().fixTitle();
	}

	// new event file notification
	private void notifyEventListeners(File file) {

		clearTrajectoriesWithoutNotification();
		notifyByPhase(new ClasIoEventNotification.OpenedFile(file.getAbsolutePath()));

		Ced.getCed().fixTitle();
	}


	/**
	 * Notify listeners we have a new event ready for display. All they may want is
	 * the notification that a new event has arrived. But the event itself is passed
	 * along.
	 */
	protected void notifyEventListeners() {

		if (_currentEvent == null) {
			return;
		}

		boolean displayEvent = !isAccumulating() && !isScanning();
		boolean wasUpdating = _updatingEventDisplay;
		_updatingEventDisplay = displayEvent;
		try {
			try (EventTrajectoryUpdate ignored = EventTrajectoryUpdate.begin(displayEvent)) {
				_uniqueLundIds = null;
				Ced.getCed().setEventFilteringLabel(FilterManager.getInstance().isFilteringOn());

				notifyByPhase(new ClasIoEventNotification.NewEvent(_currentEvent));
				finalSteps();
			}
		} finally {
			_updatingEventDisplay = wasUpdating;
		}

	}

	private void notifyByPhase(ClasIoEventNotification notification) {
		for (ClasIoEventListenerPhase phase : ClasIoEventListenerPhase.values()) {
			EventNotifier<ClasIoEventNotification> notifier = eventNotifiers.get(phase);
			if (phase == ClasIoEventListenerPhase.VIEW) {
				notifier.notifyListenersSafely(notification, (listener, exception) -> {
					Log.getInstance().error("CLAS IO view listener failed: "
							+ listener.getClass().getName());
					Log.getInstance().exception(exception);
				});
			} else {
				notifier.notifyListeners(notification);
			}
		}
	}

	public boolean isUpdatingEventDisplay() {
		return _updatingEventDisplay;
	}

	private static void clearTrajectoriesWithoutNotification() {
		EventTrajectoryUpdate.clearWithoutNotification();
	}



	// final steps
	private void finalSteps() {
		if (isAccumulating() || isScanning()) {
			return;
		}


		swimAllMC();
		swimAllRecon();
		Ced.setEventNumberLabel(getSequentialEventNumber(), getTrueEventNumber());

		for (JInternalFrame jif : Desktop.getInstance().getAllFrames()) {
			if (jif instanceof CedView) {
				((CedView) jif).getContainer().redoFeedback();
			}
		}
	}

	private void swimAllMC() {
		if (_allMCSwimmer != null) {
			_allMCSwimmer.swimAll();
		}
	}

	private void swimAllRecon() {
		if (_allReconSwimmer != null) {
			_allReconSwimmer.swimAll();
		}
	}

	/**
	 * Get the maximum energy deposited in the cal for the current event. Might be
	 * NaN if there are no "true" (gemc) banks
	 *
	 * @param plane (0, 1, 2) for (PCAL, EC_INNER, EC_OUTER)
	 * @return the max energy deposited in that cal plane in MeV
	 */
	public double getMaxEdepCal(int plane) {
		return maxEDepCal[plane];
	}

	/**
	 * Remove a IClasIoEventListener. IClasIoEventListener listeners listen for new
	 * events.
	 *
	 * @param listener the IClasIoEventListener listener to remove.
	 */
	public void removeClasIoEventListener(IClasIoEventListener listener) {
		for (EventNotifier<ClasIoEventNotification> notifier : eventNotifiers.values()) {
			notifier.removeListener(listener);
		}
	}

	/**
	 * Add a IClasIoEventListener. IClasIoEventListener listeners listen for new
	 * events.
	 *
	 * @param listener the IClasIoEventListener listener to add.
	 * @param phase determines notification order: data first, derived processing
	 *              second, and views last
	 */
	public void addClasIoEventListener(IClasIoEventListener listener, ClasIoEventListenerPhase phase) {
		eventNotifiers.get(phase).addListener(listener);
	}

	/**
	 * @deprecated use {@link #addClasIoEventListener(IClasIoEventListener, ClasIoEventListenerPhase)}
	 */
	@Deprecated(forRemoval = false)
	public void addClasIoEventListener(IClasIoEventListener listener, int index) {
		addClasIoEventListener(listener, ClasIoEventListenerPhase.fromIndex(index));
	}



}
