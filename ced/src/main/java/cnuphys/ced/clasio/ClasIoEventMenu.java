package cnuphys.ced.clasio;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Hashtable;
import java.util.OptionalInt;
import java.util.Vector;
import java.util.function.IntConsumer;

import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.MenuSelectionManager;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.component.TransparentPanel;
import cnuphys.bCNU.util.Environment;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.event.ScanManager;
import cnuphys.ced.frame.Ced;
import cnuphys.splot.plot.ImageManager;

public class ClasIoEventMenu extends JMenu implements ActionListener, IClasIoEventListener {

	private ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();

	// to find recently opened files from the preferences
	private static String _recentFileKey = "RecentEventFiles";

	// the menu items
	private JMenuItem quitItem;
	private JMenuItem nextItem;
	private JMenuItem prevItem;
	private JMenuItem accumulationItem;

	// recently opened menu
	private static JMenu _recentMenu;

	// a hash table of menu items used by recent file feature
	private static Hashtable<String, JMenuItem> _menuItems;

	// for goto sequential
	private JTextField seqEvNum;

	// for goto true
	private JTextField trueEvNum;


	// for auto next event
	private JCheckBox _periodEvent;
	private float _period = 2f; // sec
	private JTextField _periodTF;
	private Timer _nextEventTimer;

	private boolean _isReady;

	/** Last selected data file */
	private static String dataFilePath;


	private static FileFilter _hipoEventFileFilter;

	private static EvioFileFilter _evioEventFileFilter = new EvioFileFilter();

	// for both evio and hipo
	private static FileFilter _compositeFilter;

	/**
	 * The event menu used for the clasio package
	 *
	 * @param includeAccumulation include accumulation option
	 * @param includeQuit         include quite option
	 */
	public ClasIoEventMenu(boolean includeAccumulation, boolean includeQuit) {
		super("Events");

		_eventManager.addClasIoEventListener(this, ClasIoEventListenerPhase.DERIVED);

		if (_hipoEventFileFilter == null) {
			_hipoEventFileFilter = new FileFilter() {

				@Override
				public boolean accept(File f) {
					return f.getPath().endsWith(".hipo") || f.getPath().endsWith(".hippo")
							|| f.getPath().contains(".hipo.");
				}

				@Override
				public String getDescription() {
					return "Hipo Event Files";
				}
			};
		}

		// allows evio or hipo selction
		if (_compositeFilter == null) {
			_compositeFilter = new FileFilter() {

				@Override
				public boolean accept(File f) {
					return _hipoEventFileFilter.accept(f) || _evioEventFileFilter.accept(f);
				}

				@Override
				public String getDescription() {
					return "Hipo and Evio Event Files";
				}

			};
		}

		// accumulate
		if (includeAccumulation) {
			accumulationItem = addMenuItem("Accumulate Events...", KeyEvent.VK_A);
			addSeparator();
		}

		// next
		nextItem = addMenuItem("Next Event", KeyEvent.VK_N);

		// previous
		prevItem = addMenuItem("Previous Event", KeyEvent.VK_P);

		// goto sequential
		add(createGotoSequentialPanel());

		// goto true
		add(createGotoTruePanel());


		// periodic event
		add(createEventPeriodPanel());

		if (includeQuit) {
			addSeparator();
			quitItem = addMenuItem("Quit", 0);
		}

		_isReady = true;
		fixState();
	}

	/**
	 * Get the menu item to open a HIPO event file
	 *
	 * @return the menu item to open an event file
	 */
	public static JMenuItem getOpenEventFileItem() {
		final JMenuItem item = new JMenuItem("Open Hipo or Evio File...");

		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				openEventFile();
			}

		};
		item.addActionListener(al);
		return item;
	}


	public static JMenuItem getConnectETItem() {
		final JMenuItem item = new JMenuItem("Connect to ET Ring...");
		item.setIcon(ImageManager.getInstance().loadImageIcon("images/et.png"));

		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				ClasIoEventManager.getInstance().ConnectToETRing();
			}

		};
		item.addActionListener(al);
		return item;
	}

	// convenience method to add menu item
	private JMenuItem addMenuItem(String label, int accelKey) {
		JMenuItem item = new JMenuItem(label);
		if (accelKey > 0) {
			item.setAccelerator(KeyStroke.getKeyStroke(accelKey, ActionEvent.CTRL_MASK));
		}

		item.addActionListener(this);
		add(item);
		return item;
	}

	/**
	 * Fix the state of the menu items
	 */
	public void fixState() {
		if (!_isReady) {
			return;
		}

		boolean nextOK = _eventManager.isNextOK();

		// System.err.println("FIX STATE: nextOK: " + nextOK);

		nextItem.setEnabled(nextOK);
		prevItem.setEnabled(_eventManager.isPrevOK());
		seqEvNum.setEnabled(_eventManager.isGotoOK());
		trueEvNum.setEnabled(_eventManager.isGotoOK());
		_periodEvent.setEnabled(nextOK);
		_periodTF.setEnabled(nextOK);

		if (!nextOK) {
			if (_nextEventTimer != null) {
				_nextEventTimer.stop();
			}
		}

		if (accumulationItem != null) {
			accumulationItem.setEnabled(_eventManager.isNextOK());
		}
	}

	/**
	 * Set the default directory in which to look for event files.
	 *
	 * @param defaultDataDir default directory in which to look for event files
	 */
	public static void setDefaultDataDir(String defaultDataDir) {
		dataFilePath = defaultDataDir;
	}

	/**
	 * Select and open an event file.
	 *
	 */
	private static void openEventFile() {

		JFileChooser chooser = new JFileChooser(dataFilePath);
		chooser.setSelectedFile(null);
		chooser.setFileFilter(_compositeFilter);
		int returnVal = chooser.showOpenDialog(Ced.getFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			if (!canOpenEventFile(file)) {
				showOpenError(file, "The file does not exist or is not readable.");
				return;
			}
			try {
				dataFilePath = file.getPath();

				if (_hipoEventFileFilter.accept(file)) {
					ClasIoEventManager.getInstance().openHipoEventFile(file);
				} else if (_evioEventFileFilter.accept(file)) {
					ClasIoEventManager.getInstance().openEvioEventFile(file);
				}
			} catch (IOException | RuntimeException e) {
				showOpenError(file, e.getMessage());
			}
		}
	}

	/**
	 * Get the menu from which you can choose a recently opened file
	 *
	 * @return the menu from which you can choose a recently opened file
	 */
	public static JMenu getRecentEventFileMenu() {
		if (_recentMenu != null) {
			return _recentMenu;
		}
		_recentMenu = new JMenu("Recent Event Files");

		// get the recent files from the prefs
		Vector<String> recentFiles = Environment.getInstance().getPreferenceList(_recentFileKey);

		if (recentFiles != null) {
			Vector<String> validFiles = new Vector<>(recentFiles.size());
			for (String fn : recentFiles) {
				File file = new File(fn);
				if (canOpenEventFile(file)) {
					addMenu(fn, false);
					validFiles.add(fn);
				}
			}
			if (validFiles.size() != recentFiles.size()) {
				saveRecentFiles(validFiles);
			}
		}

		return _recentMenu;
	}

	// use to open recent files
	private static void addMenu(String path, boolean atTop) {

		// if it is in the hash, remove from has and from menu
		if (_menuItems != null) {
			JMenuItem item = _menuItems.remove(path);
			if (item != null) {
				_recentMenu.remove(item);
			}
		} else {
			_menuItems = new Hashtable<>(41);
		}

		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent ae) {
				String fn = ae.getActionCommand();
				File file = new File(fn);
				if (!canOpenEventFile(file)) {
					removeRecentFile(fn);
					showOpenError(file, "The file no longer exists or is not readable.");
					return;
				}

				try {
					if (_hipoEventFileFilter.accept(file)) {
						ClasIoEventManager.getInstance().openHipoEventFile(file);
					} else if (_evioEventFileFilter.accept(file)) {
						ClasIoEventManager.getInstance().openEvioEventFile(file);
					}
				} catch (IOException | RuntimeException e) {
					showOpenError(file, e.getMessage());
				}
			}

		};
		JMenuItem item = new JMenuItem(path);
		item.addActionListener(al);
		_menuItems.put(path, item);
		if (atTop) {
			_recentMenu.add(item, 0);
		} else {
			_recentMenu.add(item);
		}

	}

	static boolean canOpenEventFile(File file) {
		if (file == null || !file.isFile() || !file.canRead()) {
			return false;
		}
		try (SeekableByteChannel ignored = Files.newByteChannel(file.toPath(), StandardOpenOption.READ)) {
			return true;
		} catch (IOException | SecurityException e) {
			return false;
		}
	}

	private static void removeRecentFile(String path) {
		JMenuItem item = (_menuItems == null) ? null : _menuItems.remove(path);
		if (item != null && _recentMenu != null) {
			_recentMenu.remove(item);
		}
		Vector<String> recentFiles = Environment.getInstance().getPreferenceList(_recentFileKey);
		if (recentFiles != null && recentFiles.remove(path)) {
			saveRecentFiles(recentFiles);
		}
	}

	private static void saveRecentFiles(Vector<String> recentFiles) {
		if (recentFiles == null || recentFiles.isEmpty()) {
			Environment.getInstance().savePreference(_recentFileKey, "");
		} else {
			Environment.getInstance().savePreferenceList(_recentFileKey, recentFiles);
		}
	}

	private static void showOpenError(File file, String detail) {
		String message = "Could not open event file:\n" + file.getPath();
		if (detail != null && !detail.isBlank()) {
			message += "\n\n" + detail;
		}
		JOptionPane.showMessageDialog(Ced.getFrame(), message, "Event File Error", JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Update the recent files for the recent file menu.
	 *
	 * @param path the path to the file
	 */
	private void updateRecentFiles(String path) {
		if (path == null) {
			return;
		}
		Vector<String> recentFiles = Environment.getInstance().getPreferenceList(_recentFileKey);
		if (recentFiles == null) {
			recentFiles = new Vector<>(10);
		}

		// keep no more than 10
		recentFiles.remove(path);
		recentFiles.add(0, path);
		if (recentFiles.size() > 9) {
			recentFiles.removeElementAt(9);
		}
		Environment.getInstance().savePreferenceList(_recentFileKey, recentFiles);

		// add to menu
		addMenu(path, true);
	}

	// create the goto sequential event widget
	private JPanel createGotoSequentialPanel() {
		JPanel sp = new TransparentPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));

		JLabel label = new JLabel("Go to Sequential Event: ");

		seqEvNum = new JTextField("1", 10);

		seqEvNum.addActionListener(event -> navigateToEnteredEvent(seqEvNum, _eventManager::gotoEvent));

		sp.add(label);
		sp.add(seqEvNum);
		seqEvNum.setEnabled(false);
		return sp;
	}

	// create the goto true event widget
	private JPanel createGotoTruePanel() {
		JPanel sp = new TransparentPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));

		JLabel label = new JLabel("Go to True Event: ");

		trueEvNum = new JTextField("1", 10);

		trueEvNum.addActionListener(event ->
				navigateToEnteredEvent(trueEvNum, ScanManager.getInstance()::gotoTrue));

		sp.add(label);
		sp.add(trueEvNum);
		trueEvNum.setEnabled(false);
		return sp;
	}

	private static void navigateToEnteredEvent(JTextField field, IntConsumer navigation) {
		MenuSelectionManager.defaultManager().clearSelectedPath();
		OptionalInt eventNumber = positiveEventNumber(field.getText());
		if (eventNumber.isPresent()) {
			navigation.accept(eventNumber.getAsInt());
		} else {
			UIManager.getLookAndFeel().provideErrorFeedback(field);
			field.selectAll();
		}
	}

	static OptionalInt positiveEventNumber(String text) {
		try {
			int eventNumber = Integer.parseInt(text == null ? "" : text.trim());
			return eventNumber > 0 ? OptionalInt.of(eventNumber) : OptionalInt.empty();
		} catch (NumberFormatException exception) {
			return OptionalInt.empty();
		}
	}




	/**
	 * Auto select the auto event every two seconds. This is
	 * called after successful connection to an ET ring so that
	 * shift takers don't have to remember to do it.
	 */
	public void autoCheckAuto() {
		_periodEvent.setSelected(true);
		fixState();
		autoAction();
	}

	//action when selected the auto event radio
	private void autoAction() {
		if (_periodEvent.isSelected()) {
			if (_nextEventTimer == null) {

				_period = normalizedEventPeriod(_periodTF.getText(), _period);
				_periodTF.setText(Float.toString(_period));

				ActionListener nextAl = new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						_eventManager.getNextEvent();
					}

				};

				int delay = (int) (1000 * _period);
				_nextEventTimer = new Timer(delay, nextAl);

			}
			if (!_nextEventTimer.isRunning()) {
				_nextEventTimer.restart();
			}
		} else {
			if (_nextEventTimer != null) {
				_nextEventTimer.stop();
			}
		}
	}

	// create the event every so many seconds widget
	private JPanel createEventPeriodPanel() {
		JPanel sp = new TransparentPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));

		_periodEvent = new JCheckBox("Auto Next-Event Every ");

		_periodTF = new JTextField("" + _period, 4);

		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				autoAction();
			}

		};
		_periodEvent.addActionListener(al);

		KeyAdapter ka = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent kev) {
				if (kev.getKeyCode() == KeyEvent.VK_ENTER) {
					MenuSelectionManager.defaultManager().clearSelectedPath();
					_period = normalizedEventPeriod(_periodTF.getText(), _period);

					if (_nextEventTimer != null) {
						int delay = (int) (1000 * _period);
						_nextEventTimer.setDelay(delay);
					}
					_periodTF.setText(Float.toString(_period));
				}
			}
		};
		_periodTF.addKeyListener(ka);

		sp.add(_periodEvent);
		sp.add(_periodTF);
		sp.add(new JLabel("sec"));
		_periodEvent.setEnabled(false);
		_periodTF.setEnabled(false);
		return sp;
	}

	static float normalizedEventPeriod(String text, float fallback) {
		try {
			float period = Float.parseFloat(text);
			if (!Float.isFinite(period)) return fallback;
			return Math.max(0.001f, Math.min(60f, period));
		} catch (NumberFormatException exception) {
			return fallback;
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();

		if (source == nextItem) {
			_eventManager.getNextEvent();
		} else if (source == prevItem) {
			_eventManager.getPreviousEvent();
		} else if (source == accumulationItem) {
			ClasIoAccumulationDialog dialog = new ClasIoAccumulationDialog(AccumulationManager.getInstance());
			dialog.setVisible(true);
		} else if (source == quitItem) {
			System.exit(0);
		}
	}

	/**
	 * Part of the IClasIoEventListener interface
	 *
	 * @param event the new current event
	 */
	@Override
	public void newClasIoEvent(DataEvent event) {
		fixState();
	}

	/**
	 * Part of the IClasIoEventListener interface
	 *
	 * @param path the new path to the event file
	 */
	@Override
	public void openedNewEventFile(String path) {

		// remember which file was chosen
		setDefaultDataDir(path);
		updateRecentFiles(path);
		fixState();
	}

	/**
	 * Change the event source type
	 *
	 * @param source the new source: File, ET
	 */
	@Override
	public void changedEventSource(ClasIoEventManager.EventSourceType source) {
		fixState();
	}


}
