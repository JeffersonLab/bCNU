package cnuphys.ced.clasio.table;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.MenuSelectionManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jlab.io.base.DataEvent;

import edu.cnu.mdi.format.DoubleFormat;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.view.BaseView;
import cnuphys.ced.alldata.ColumnData;
import cnuphys.ced.alldata.DataWarehouse;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.ClasIoEventListenerPhase;
import cnuphys.ced.clasio.ClasIoPresentBankPanel;
import cnuphys.ced.clasio.IClasIoEventListener;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.event.IAccumulationListener;
import cnuphys.ced.event.ScanManager;

public class NodePanel extends JPanel
		implements ActionListener, ListSelectionListener, IClasIoEventListener, IAccumulationListener {

	// Text area shows data values for selected nodes.
	private JTextArea _dataTextArea;
	
	private SeenBankPanel _seenBankPanel;

	// the event info panel
	private EventInfoPanel _eventInfoPanel;

	/** A button for selecting "next" event. */
	protected JButton nextButton;

	/** A button for selecting "previous" event. */
	protected JButton prevButton;

	/** show ints as hex */
	protected JCheckBox intsInHexButton;

	/** Used for "goto" event */
	protected JTextField seqEventNumberInput;

	/** Used for "goto" event */
	protected JTextField trueEventNumberInput;

	// the table
	protected NodeTable _nodeTable;

	// the event manager
	ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();

	// set true when constructor finished
	private boolean _isReady;

	// current selected node
	private ColumnData _currentColumnData;

	// present banks
	private ClasIoPresentBankPanel _presentPanel;

	//view owner
	private BaseView _view;

	/**
	 * Create a node panel for displaying events
	 */
	public NodePanel(BaseView view) {
		_view = view;
		_eventManager.addClasIoEventListener(this, ClasIoEventListenerPhase.DERIVED);

		setLayout(new BorderLayout());
		addCenter();
		addEast();
		addWest();

		_isReady = true;
		fixButtons();

		AccumulationManager.getInstance().addAccumulationListener(this);
	}

	// add the east components
	private void addEast() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		// shows which banks are present
		_presentPanel = ClasIoPresentBankPanel.createPresentBankPanel(_view, _nodeTable, 45);

		panel.add(_presentPanel.getScrollPane());
		panel.add(Box.createVerticalGlue());

		add(panel, BorderLayout.EAST);
	}

	/**
	 * Create the text area that will display structure data. What is actually
	 * returned is the scroll pane that contains the text area.
	 *
	 * @return the scroll pane holding the text area.
	 */
	private JScrollPane createDataTextArea() {

		_dataTextArea = new JTextArea(3, 40);
		_dataTextArea.setMinimumSize(new Dimension(180, 200));
		_dataTextArea.setFont(Fonts.mediumFont);
		// _dataTextArea.setBorder(BorderFactory.createTitledBorder(null,
		// "Data",
		// TitledBorder.LEADING, TitledBorder.TOP, null, Color.blue));
		_dataTextArea.setEditable(false);

		JScrollPane scrollPane = new JScrollPane() {
			@Override
			public Dimension getMinimumSize() {
				return new Dimension(180, 200);
			}
		};
		scrollPane.getViewport().setView(_dataTextArea);
		// Borderlayout respects preferred width in east/west,
		// but ignores height -- so use this to set width only.
		// Don't use "setPreferredSize" on textArea or it messes up the
		// scrolling.
		scrollPane.setPreferredSize(new Dimension(200, 600));

		return scrollPane;
	}

	// add the center components
	private void addCenter() {
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BorderLayout(0, 0));

		// event info
		_eventInfoPanel = new EventInfoPanel();
		addEventControls();

		JPanel npanel = new JPanel();
		npanel.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 4));
		npanel.add(_eventInfoPanel);
		centerPanel.add(npanel, BorderLayout.NORTH);

		_nodeTable = new NodeTable();
		_nodeTable.getSelectionModel().addListSelectionListener(this);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, createDataTextArea(),
				_nodeTable.getScrollPane());
		splitPane.setResizeWeight(0.1);
		centerPanel.add(splitPane, BorderLayout.CENTER);

		add(centerPanel, BorderLayout.CENTER);
	}
	
	//add the seen bank panel on the west
	public void addWest() {
		_seenBankPanel = new SeenBankPanel();
		add(_seenBankPanel, BorderLayout.WEST);
	}

	/**
	 * Set the model data based on a clasIO DataEvent
	 *
	 * @param event the event
	 */
	public void setData(DataEvent event) {
		_nodeTable.setData(event);
	}

	// list of ignored tags
	/**
	 * Create a panel to change events in viewer.
	 */
	private void addEventControls() {

		JPanel eventSourcePanel = _eventInfoPanel.getEventSourcePanel();
		JPanel numPanel = _eventInfoPanel.getNumberPanel();

		nextButton = new JButton("next");
		nextButton.setFont(Fonts.smallFont);
		nextButton.addActionListener(this);

		prevButton = new JButton("prev");
		prevButton.setFont(Fonts.smallFont);
		prevButton.addActionListener(this);

		JLabel seqLabel = new JLabel("goto event   seq: ");
		GraphicsUtilities.setSizeSmall(seqLabel);

		JLabel trueLabel = new JLabel(" true: ");
		GraphicsUtilities.setSizeSmall(trueLabel);


		seqEventNumberInput = new JTextField(7);
		trueEventNumberInput = new JTextField(7);

		KeyAdapter ka = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent kev) {

				if (kev.getSource() == seqEventNumberInput) {
					if (kev.getKeyCode() == KeyEvent.VK_ENTER) {
						MenuSelectionManager.defaultManager().clearSelectedPath();
						try {
							int enumber = Integer.parseInt(seqEventNumberInput.getText());
							_eventManager.gotoEvent(enumber + 1);
						} catch (Exception e) {
							seqEventNumberInput.setText("");
						}
					}
				}
				else if (kev.getSource() == trueEventNumberInput) {
					if (kev.getKeyCode() == KeyEvent.VK_ENTER) {
						MenuSelectionManager.defaultManager().clearSelectedPath();
						try {
							int enumber = Integer.parseInt(trueEventNumberInput.getText());
							ScanManager.getInstance().gotoTrue(enumber);						} catch (Exception e) {
							trueEventNumberInput.setText("");
						}
					}
				}


			}
		};
		seqEventNumberInput.addKeyListener(ka);
		trueEventNumberInput.addKeyListener(ka);


		intsInHexButton = new JCheckBox("Show ints in hex", false);
		intsInHexButton.setFont(Fonts.defaultFont);
		GraphicsUtilities.setSizeSmall(intsInHexButton);

		ItemListener il = new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent arg0) {
				updateDataArea(_currentColumnData);
			}

		};
		intsInHexButton.addItemListener(il);

		eventSourcePanel.add(Box.createHorizontalStrut(4));
		eventSourcePanel.add(intsInHexButton);


		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
		panel.add(Box.createHorizontalStrut(2));
		panel.add(prevButton);
		panel.add(Box.createHorizontalStrut(2));
		panel.add(nextButton);
		panel.add(Box.createHorizontalStrut(2));

		numPanel.add(panel, BorderLayout.NORTH);


		panel.add(seqLabel);
		panel.add(seqEventNumberInput);
		panel.add(trueLabel);
		panel.add(trueEventNumberInput);
		panel.add(Box.createHorizontalStrut(6));

		numPanel.add(panel, 0);
	}

	/**
	 * Set the selectability of the buttons
	 */
	public void fixButtons() {
		if (!_isReady) {
			return;
		}
		nextButton.setEnabled(_eventManager.isNextOK());
		prevButton.setEnabled(_eventManager.isPrevOK());
		seqEventNumberInput.setEnabled(_eventManager.isGotoOK());
        trueEventNumberInput.setEnabled(_eventManager.isGotoOK());
	}

	/**
	 * /** Set the displayed event source value.
	 *
	 * @param source event source.
	 */
	public void setSource(String source) {
		_eventInfoPanel.setSource(source);
	}

	/**
	 * Get the displayed event source value.
	 *
	 * @return the displayed event source value.
	 */
	public String getSource() {
		return _eventInfoPanel.getSource();
	}

	/**
	 * Set the displayed sequential event number value.
	 * That is just the event number in the file.
	 * @param seqEventNumber event number.
	 */
	public void setSeqEventNumber(int seqEventNumber) {
		_eventInfoPanel.setSeqEventNumber(seqEventNumber);
	}

	/**
	 * Set the displayed true event number value.
	 * That is the number in the RUN::config bank
	 * @param trueEventNumber event number.
	 */
	public void setTrueEventNumber(int trueEventNumber) {
		_eventInfoPanel.setTrueEventNumber(trueEventNumber);
	}

	/**
	 * Set the displayed run number value.
	 *
	 * @param runNumber run number.
	 */
	public void setRunNumber(int runNumber) {
		_eventInfoPanel.setRunNumber(runNumber);
	}

	/**
	 * Get the displayed event number value.
	 *
	 * @return the displayed event number value.
	 */
	public int getEventNumber() {
		return _eventInfoPanel.getEventNumber();
	}

	/**
	 * Set the displayed number-of-events value.
	 *
	 * @param numberOfEvents number of events.
	 */
	public void setNumberOfEvents(int numberOfEvents) {
		_eventInfoPanel.setNumberOfEvents(numberOfEvents);
	}

	/**
	 * Get the displayed number-of-events value.
	 *
	 * @return the displayed number-of-events value.
	 */
	public int getNumberOfEvents() {
		return _eventInfoPanel.getNumberOfEvents();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();

		if (source == nextButton) {
			_eventManager.getNextEvent();
		} else if (source == prevButton) {
			_eventManager.getPreviousEvent();
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
			return;
		}
		int row = _nodeTable.getSelectedRow();

		_currentColumnData = _nodeTable.getColumnData(row);
		updateDataArea(_currentColumnData);
	}

	/**
	 * Update the data text area
	 *
	 * @param treeSelectionEvent the causal event.
	 */
	protected void updateDataArea(ColumnData cd) {

		_dataTextArea.setText("");
		int blankLineEveryNth = 5; // put in a blank line after every Nth

		if (cd == null) {
			return;
		}

		DataEvent event = DataWarehouse.getInstance().getCurrentEvent();
		if (event == null) {
			return;
		}

		DataWarehouse dw = DataWarehouse.getInstance();
		String bankName = cd.bankName;
        String columnName = cd.columnName;

		int lineCounter = 1;
		int index = 0;

		switch (cd.type) {

		case DataWarehouse.INT8: // byte
			byte bytes[] = dw.getByte(bankName, columnName);
			if (bytes != null) {
				for (byte i : bytes) {
					String s;
					if (intsInHexButton.isSelected()) {
						s = String.format("[%02d]  %#04X", index++, i);
					} else {
						s = String.format("[%02d]  %d", index++, i);
					}
					_dataTextArea.append(s);
					if (lineCounter < bytes.length) {
						if (lineCounter % blankLineEveryNth == 0) {
							_dataTextArea.append("\n\n");
						} else {
							_dataTextArea.append("\n");
						}
						lineCounter++;
					}
				}
			} else {
				_dataTextArea.append("null data\n");
			}
			break;

		case DataWarehouse.INT16:
			short shorts[] = dw.getShort(bankName, columnName);
			if (shorts != null) {
				for (short i : shorts) {
					String s;
					if (intsInHexButton.isSelected()) {
						s = String.format("[%02d]  %#05X", index++, i);
					} else {
						s = String.format("[%02d]  %d", index++, i);
					}
					_dataTextArea.append(s);
					if (lineCounter < shorts.length) {
						if (lineCounter % blankLineEveryNth == 0) {
							_dataTextArea.append("\n\n");
						} else {
							_dataTextArea.append("\n");
						}
						lineCounter++;
					}
				}
			} else {
				_dataTextArea.append("null data\n");
			}
			break;

		case DataWarehouse.INT32:
			int ints[] = dw.getInt(bankName, columnName);
			if (ints != null) {
				for (int i : ints) {
					String s;
					if (intsInHexButton.isSelected()) {
						s = String.format("[%02d]  %#010X", index++, i);
					} else {
						s = String.format("[%02d]  %d", index++, i);
					}
					_dataTextArea.append(s);
					if (lineCounter < ints.length) {
						if (lineCounter % blankLineEveryNth == 0) {
							_dataTextArea.append("\n\n");
						} else {
							_dataTextArea.append("\n");
						}
						lineCounter++;
					}
				}
			} else {
				_dataTextArea.append("null data\n");
			}
			break;

		case DataWarehouse.INT64:
			long longs[] = dw.getLong(bankName, columnName);
			if (longs != null) {
				for (long i : longs) {
					String s;
					if (intsInHexButton.isSelected()) {
						s = String.format("[%02d]  %#010X", index++, i);
					} else {
						s = String.format("[%02d]  %d", index++, i);
					}
					_dataTextArea.append(s);
					if (lineCounter < longs.length) {
						if (lineCounter % blankLineEveryNth == 0) {
							_dataTextArea.append("\n\n");
						} else {
							_dataTextArea.append("\n");
						}
						lineCounter++;
					}
				}
			} else {
				_dataTextArea.append("null data\n");
			}
			break;


		case DataWarehouse.FLOAT32:
			float floats[] = dw.getFloat(bankName, columnName);
			if (floats != null) {
				for (float f : floats) {
					String doubStr = DoubleFormat.doubleFormat(f, 6, 4);
					String s = String.format("[%02d]  %s", index++, doubStr);
					_dataTextArea.append(s);
					if (lineCounter < floats.length) {
						if (lineCounter % blankLineEveryNth == 0) {
							_dataTextArea.append("\n\n");
						} else {
							_dataTextArea.append("\n");
						}
						lineCounter++;
					}
				}
			} else {
				_dataTextArea.append("null data\n");
			}
			break;

		case DataWarehouse.FLOAT64:
			double doubles[] = dw.getDouble(bankName, columnName);
			if (doubles != null) {
				for (double d : doubles) {
					String doubStr = DoubleFormat.doubleFormat(d, 6, 4);
					String s = String.format("[%02d]  %s", index++, doubStr);
					_dataTextArea.append(s);
					if (lineCounter < doubles.length) {
						if (lineCounter % blankLineEveryNth == 0) {
							_dataTextArea.append("\n\n");
						} else {
							_dataTextArea.append("\n");
						}
						lineCounter++;
					}
				}

			} else {
				_dataTextArea.append("null data\n");
			}
			break;

		default:
			_dataTextArea.append("null data\n");

		}

	}

	/**
	 * Part of the IClasIoEventListener interface
	 *
	 * @param event the new current event
	 */
	@Override
	public void newClasIoEvent(DataEvent event) {

		if (!_eventManager.isAccumulating()) {
			setData(event);
			setSeqEventNumber(_eventManager.getSequentialEventNumber());
			setTrueEventNumber(_eventManager.getTrueEventNumber());
			setRunNumber(_eventManager.getRunData().run);
			fixButtons();
			_seenBankPanel.updateSeenBanks();
		}
	}

	/**
	 * Part of the IClasIoEventListener interface
	 *
	 * @param path the new path to the event file
	 */
	@Override
	public void openedNewEventFile(String path) {
		setSeqEventNumber(0);
		setTrueEventNumber(-1);
		setRunNumber(-1);

		// set the text field
		setSource(path);
		setNumberOfEvents(_eventManager.getEventCount());
		fixButtons();
		_seenBankPanel.updateSeenBanks();
	}

	/**
	 * Change the event source type
	 *
	 * @param source the new source: File, ET
	 */
	@Override
	public void changedEventSource(ClasIoEventManager.EventSourceType source) {
		fixButtons();
	}

	@Override
	public void accumulationEvent(int reason) {
		switch (reason) {
		case AccumulationManager.ACCUMULATION_STARTED:
			break;

		case AccumulationManager.ACCUMULATION_CANCELLED:
			break;

		case AccumulationManager.ACCUMULATION_FINISHED:
			setData(_eventManager.getCurrentEvent());
			setSeqEventNumber(_eventManager.getSequentialEventNumber());
			setTrueEventNumber(_eventManager.getTrueEventNumber());
			setRunNumber(_eventManager.getRunData().run);
			fixButtons();
			break;
		}
	}


}
