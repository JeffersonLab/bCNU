package cnuphys.ced.clasio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;

import org.jlab.detector.decode.CLASDecoder4;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataSource;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioETSource;
import org.jlab.io.evio.EvioSource;
import org.jlab.io.hipo.HipoDataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.data.SchemaFactory;
import org.jlab.utils.system.ClasUtilsFile;

import cnuphys.bCNU.application.Desktop;
import cnuphys.bCNU.dialog.DialogUtilities;
import cnuphys.bCNU.graphics.ImageManager;
import cnuphys.bCNU.graphics.component.IpField;
import cnuphys.bCNU.magneticfield.swim.ISwimAll;
import cnuphys.bCNU.threading.EventNotifier;
import cnuphys.ced.alldata.DataWarehouse;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.clasio.et.ConnectETDialog;
import cnuphys.ced.clasio.filter.FilterManager;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.event.ScanManager;
import cnuphys.ced.frame.Ced;
import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;
import cnuphys.swim.Swimming;

public class ClasIoEventManager {

	// Unique lund ids in the event (if any)
	private ArrayList<LundId> _uniqueLundIds = new ArrayList<>();

	// used in pcal and ec hex gradient displays
	private double maxEDepCal[] = { Double.NaN, Double.NaN, Double.NaN };

	// Data from the special run bank
	private RunData _runData = new RunData();

	// for HIPO ring
	public IpField _ipField;

	// connect to ring
	public JButton _connectButton;

	// decode evio to hipo
	private CLASDecoder4 _decoder;
	private SchemaFactory _schemaFactory;

	// reset everytime hipo or evio file is opened
	private int _currentEventIndex;

	// sources of events (the type, not the actual source)
	public enum EventSourceType {
		HIPOFILE, ET, EVIOFILE
	}

	// the current source type
	private EventSourceType _sourceType = EventSourceType.HIPOFILE;

	// ET dialog
	private ConnectETDialog _etDialog;

	// flag that set set to <code>true</code> if we are accumulating events
	private boolean _accumulating = false;

	// flag that set set to <code>true</code> if we are quickly scanning events events
	private boolean _scanning = false;




	// list of event listeners. There are actually three lists. Those in index 0
	// are notified first. Then those in index 1. Finally those in index 2. The
	// Data containers should be in index 0. The trajectory and noise in index 1, and
	// the regular views in index 2 (they are notified last)
	private final List<EventNotifier<Object>> eventNotifiers = List.of(
			new EventNotifier<>(), new EventNotifier<>(), new EventNotifier<>());

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
	private DataEvent _currentEvent;

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
					_runData.set();
					notifyEventListeners();
					Ced.refresh();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Get a collection of unique LundIds in the current event
	 *
	 * @return a collection of unique LundIds
	 */
	public ArrayList<LundId> uniqueLundIds() {

		if (_uniqueLundIds != null) {
			return _uniqueLundIds;
		}

		_uniqueLundIds = new ArrayList<>();

		if (_currentEvent != null) {
			// use any bank with a true pid column

			String[] cbanks = _currentEvent.getBankList();
			if (cbanks != null) {
				for (String bankName : cbanks) {

					if (bankName.contains("::Particle") || bankName.contains("::Lund")) {

						if (!DataWarehouse.getInstance().bankContainsColumn(bankName, "pid")) {
							continue;
						}

						//get the pid column
						int pid[] = DataWarehouse.getInstance().getInt(bankName, "pid");
						if ((pid != null) && (pid.length > 0)) {
							for (int pdgid : pid) {
								LundId lid = LundSupport.getInstance().get(pdgid);
								if (lid != null) {
									_uniqueLundIds.remove(lid);
									_uniqueLundIds.add(lid);
								}
							}
						}
					}
				}
			}
		} // currentevent != null

		return _uniqueLundIds;
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
		StringBuffer sb = new StringBuffer(256);

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
				sb.append("No event loaded./n");
			}
		} catch (Exception e) {
			sb.append("Exception in currentInfoString: " + e.getMessage() + "/n");
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

		_currentHipoFile = file;
		
		if (_dataSource != null) {
			_dataSource.close();
		}

		_dataSource = new HipoDataSource();
		_dataSource.open(file.getPath());


		//let the data manager know
		_schemaFactory = ((HipoDataSource)_dataSource).getReader().getSchemaFactory();
		DataWarehouse.getInstance().updateSchema(_schemaFactory);

		//notify the listeners
		notifyEventListeners(_currentHipoFile);
		setEventSourceType(EventSourceType.HIPOFILE);

		reset();

		// auto go to first event
		try {
			getNextEvent();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	//partial reset for new event source
	private void reset() {
		_runData.reset();
		_currentEvent = null;
		_currentEventIndex = 0;
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

		_currentEvioFile = file;
		
		if (_dataSource != null) {
			_dataSource.close();
		}

		_dataSource = new EvioSource();
		_dataSource.open(file.getPath());
		notifyEventListeners(_currentEvioFile);
		setEventSourceType(EventSourceType.EVIOFILE);

		reset();

		// TODO check if I need to skip the first event

		try {
			getNextEvent();
		} catch (Exception e) {
			e.printStackTrace();
		}
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

			reset();
			
			if (_dataSource != null) {
				_dataSource.close();
			}

			_dataSource = null;
			_currentMachine = _etDialog.getMachine();
			_currentETFile = _etDialog.getFile();
			_currentStation = _etDialog.getStation();
			_currentPort = _etDialog.getPort();

			// does the file exist?

			System.err.println("ET File Name:_currentETFile [" + _currentETFile + "]");

			try {

				_dataSource = new EvioETSource(_currentMachine, _currentPort, _currentStation);

				if (_dataSource == null) {
					JOptionPane.showMessageDialog(null, "The ET Data Source is null, used Machine: " + _currentMachine,
							"ET null Data Source", JOptionPane.INFORMATION_MESSAGE, ImageManager.cnuIcon);
					return;
				}

				System.err.println("trying to connect using et file: " + _currentETFile);
				setEventSourceType(EventSourceType.ET);
				_dataSource.open(_currentETFile);

				//auto select events every 2 sec
				Ced.getCed().getEventMenu().autoCheckAuto();
			} catch (Exception e) {
				String message = "Could not connect to ET Ring [" + e.getMessage() + "]";
				System.err.println(message);
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
		if (_currentEvent != null) {
			DataBank db = _currentEvent.getBank("RUN::config");
			if (db != null) {
				int[] ia = db.getInt("event");
				if ((ia != null) && (ia.length > 0)) {
					return ia[0];
				}
			}
		}
		return -1;
	}


	/**
	 * Determines whether any next event control should be enabled.
	 *
	 * @return <code>true</code> if any next event control should be enabled.
	 */
	public boolean isNextOK() {

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

	// decode an evio event to hipo
	private HipoDataEvent decodeEvioToHipo(EvioDataEvent event) {

		try {
			if (_decoder == null) {
				_schemaFactory = new SchemaFactory();

				String dir = ClasUtilsFile.getResourceDir("CLAS12DIR", "etc/bankdefs/hipo4");
				_schemaFactory.initFromDirectory(dir);
				_decoder = new CLASDecoder4();
				DataWarehouse.getInstance().updateSchema(_schemaFactory);
			}
			_decoder.initEvent(event);
			Event dump = _decoder.getDataEvent();
			HipoDataEvent hipoEvent = new HipoDataEvent(dump, _decoder.getSchemaFactory());
			return hipoEvent;
		}

		catch (Exception e) {
			System.err.println("Error decoding evio to hipo: " + e.getMessage());
			e.printStackTrace();
			return null;
		}

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

				if (_dataSource.hasEvent()) {
					_currentEvent = _dataSource.getNextEvent();
				} else {
					_currentEvent = null;
				}

				if ((_currentEvent != null) && (_currentEvent instanceof EvioDataEvent)) {
					_currentEvent = decodeEvioToHipo((EvioDataEvent) _currentEvent);
				}

				done = (_currentEvent == null) || FilterManager.getInstance().pass();
			}
			if (_currentEvent != null) {
				_currentEventIndex++;
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
					e.printStackTrace();
				}
				_dataSource.waitForEvents();
			}

			_currentEvent = null;

			if (_dataSource.hasEvent()) {

				_currentEvent = _dataSource.getNextEvent();

				if ((_currentEvent != null) && (_currentEvent instanceof EvioDataEvent)) {
					_currentEvent = decodeEvioToHipo((EvioDataEvent) _currentEvent);
				}

				if (FilterManager.getInstance().pass()) {
					_currentEventIndex++;
				} else {
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
				if (_dataSource.hasEvent()) {
					DataEvent event = _dataSource.getNextEvent();
					if (FilterManager.getInstance().pass()) {
						_currentEventIndex++;
					}
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
				_dataSource.open(_currentHipoFile);
				gotoEvent(eventNumber);
			}

			break;

		case EVIOFILE:
			_currentEvent = _dataSource.gotoEvent(eventNumber);
			if ((_currentEvent != null) && (_currentEvent instanceof EvioDataEvent)) {
				_currentEvent = decodeEvioToHipo((EvioDataEvent)_currentEvent);
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

	//	Swimming.clearAllTrajectories();

		if (_dataSource != null) {
			_currentEvent = null;
			_currentEventIndex = 0;
		}

		Swimming.setNotifyOn(false); // prevent refreshes
		Swimming.clearAllTrajectories();
		Swimming.setNotifyOn(true); // prevent refreshes
		for (int index = 0; index < eventNotifiers.size(); index++) {
			try {
				eventNotifiers.get(index).nonThreadedTriggerEvent(source);
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}

		Ced.getCed().fixTitle();
	}

	// new event file notification
	private void notifyEventListeners(File file) {

		Swimming.setNotifyOn(false); // prevent refreshes
		Swimming.clearAllTrajectories();
		Swimming.setNotifyOn(true); // prevent refreshes
		for (int index = 0; index < eventNotifiers.size(); index++) {
			try {
				eventNotifiers.get(index).nonThreadedTriggerEvent(file.getAbsolutePath());
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}

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

		Swimming.setNotifyOn(false); // prevent refreshes
		Swimming.clearAllTrajectories();
		Swimming.setNotifyOn(true); // prevent refreshes

		_uniqueLundIds = null;
		Ced.getCed().setEventFilteringLabel(FilterManager.getInstance().isFilteringOn());

		Swimming.clearAllTrajectories();
		for (int index = 0; index < eventNotifiers.size(); index++) {
			try {
				eventNotifiers.get(index).nonThreadedTriggerEvent(_currentEvent);
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		finalSteps();

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
		for (int i = 0; i < eventNotifiers.size(); i++) {
			eventNotifiers.get(i).removeListener(listener);
		}
	}

	/**
	 * Add a IClasIoEventListener. IClasIoEventListener listeners listen for new
	 * events.
	 *
	 * @param listener the IClasIoEventListener listener to add.
	 * @param index    Determines gross notification order. Those in index 0 are
	 *                 notified first. Then those in index 1. Finally those in index
	 *                 2. The Data containers should be in index 0. The trajectory
	 *                 and noise in index 1, and the regular views in index 2 (they
	 *                 are notified last)
	 */
	public void addClasIoEventListener(IClasIoEventListener listener, int index) {
		eventNotifiers.get(index).addListener(listener);
	}



}
