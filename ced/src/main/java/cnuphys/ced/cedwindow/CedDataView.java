package cnuphys.ced.cedwindow;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.table.TableColumn;

import org.jlab.io.base.DataEvent;

import edu.cnu.mdi.component.CommonBorder;
import cnuphys.bCNU.util.FileUtilities;
import edu.cnu.mdi.ui.fonts.Fonts;
import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.view.BaseView;
import cnuphys.ced.alldata.DataWarehouse;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.ClasIoEventListenerPhase;
import cnuphys.ced.clasio.ClasIoEventManager.EventSourceType;
import cnuphys.ced.clasio.IClasIoEventListener;
import cnuphys.ced.clasio.datatable.BankDataTable;
import cnuphys.ced.clasio.table.NamedLabel;
import cnuphys.ced.properties.PropertiesManager;

public class CedDataView extends BaseView
		implements ItemListener, ActionListener, WindowListener, IClasIoEventListener {

	// the clasIO event manager
	protected ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();

	// counter to offset windows
	private static int count = 0;

	// check boxes
	private JPanel _checkboxPanel;
	private JCheckBox _cbarray[];

	// set true when constructor finished
	private boolean _isReady;

	/**
	 * A label for displaying the ordinal number of the event from an event file.
	 */
	private NamedLabel seqEventNumberLabel;

	/**
	 * A label for displaying the true number of the event from the RUN::config
	 * bank.
	 */
	private NamedLabel trueEventNumberLabel;

	/** A button for selecting "next" event. */
	protected JButton nextButton;

	/** A button for selecting "previous" event. */
	protected JButton prevButton;

	// table to hold the data
	private BankDataTable _table;

	// bank name
	private String _bankName;

	// cache all created bank data windows
	private static HashMap<String, CedDataView> _dataBanks = new HashMap<>(193);

	/**
	 * View that shows a bank and all its columns
	 *
	 * @param bankName the bank name
	 */
	private CedDataView(String bankName) {

		super(PropertySupport.TITLE, bankName, PropertySupport.ICONIFIABLE, true, PropertySupport.MAXIMIZABLE, true,
				PropertySupport.CLOSABLE, true, PropertySupport.RESIZABLE, true, PropertySupport.LEFT,
				40 + (count % 5) * 10 + (count / 5) * 40, PropertySupport.TOP, 40 + (count % 5) * 30,
				PropertySupport.VISIBLE, true, PropertySupport.TOOLBAR, false);

		count++;

		_bankName = bankName;

		getContentPane().removeAll();
		setLayout(new BorderLayout(2, 2));

		addNorth();
		addCenter();
		addSouth();

		readVisibility();

		pack();

        //add event listener
		_eventManager.addClasIoEventListener(this, ClasIoEventListenerPhase.VIEW);

		_isReady = true;
	}

	// add the north component
	private void addNorth() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 4));

		nextButton = new JButton("next");
		nextButton.setFont(Fonts.smallFont);
		nextButton.addActionListener(this);

		prevButton = new JButton("prev");
		prevButton.setFont(Fonts.smallFont);
		prevButton.addActionListener(this);

		seqEventNumberLabel = new NamedLabel("seq #", "true #", 65);
		trueEventNumberLabel = new NamedLabel("true #", "true #", 65);

		panel.add(nextButton);
		panel.add(prevButton);
		panel.add(seqEventNumberLabel);
		panel.add(trueEventNumberLabel);
		add(panel, BorderLayout.NORTH);
	}

	// add the center component
	private void addCenter() {
		// add the table
		_table = new BankDataTable(_bankName);
		add(_table.getScrollPane(), BorderLayout.CENTER);
	}

	// add the south component
	private void addSouth() {
		// add the visibility checkbox panel
		_checkboxPanel = new JPanel();
		_checkboxPanel.setBorder(new CommonBorder("Visibility"));
		_checkboxPanel.setLayout(new GridLayout(5, 10, 4, 4));

		// now the checkboxes
		String columns[] = colNames(_bankName);

		if ((columns != null) && (columns.length > 0)) {

			_cbarray = new JCheckBox[columns.length];

			// get the mask (if there is one) from the user preferences (persistance)
			String maskName = PropertiesManager.getInstance().get(_bankName);
			String tokens[] = null;
			if (maskName != null) {
				tokens = FileUtilities.tokens(maskName, ":");
			}

			for (int i = 0; i < columns.length; i++) {

				// the checkbox is selected if there is no mask or, if there is a mask,
				// if the column name is one of the tokens generated from the mask
				boolean selected = false;
				if ((tokens == null) || (tokens.length < 1)) {
					selected = true;
				} else {
					for (String token : tokens) {
						if (columns[i].equals(token)) {
							selected = true;
							break;
						}
					}
				}

				_cbarray[i] = new JCheckBox(columns[i], selected);
				_cbarray[i].setFont(Fonts.tweenFont);
				_cbarray[i].addItemListener(this);
				_checkboxPanel.add(_cbarray[i]);
			}

		}
		add(_checkboxPanel, BorderLayout.SOUTH);
	}

	/**
	 * Get a bank view. If it does not exist, create it.
	 *
	 * @param bankName the name of the bank
	 * @return the bank window
	 */
	public static CedDataView getBankView(String bankName) {
		CedDataView dataView = _dataBanks.get(bankName);

		if (dataView == null) {
			dataView = new CedDataView(bankName);
			// set the location
			int x = 40 + (count % 5) * 10 + (count / 5) * 40;
			int y = 40 + (count % 5) * 30;
			dataView.setLocation(x, y);
			count++;

			_dataBanks.put(bankName, dataView);
		}

		dataView.setVisible(true);
		dataView.toFront();
		dataView.requestFocus();
		return dataView;
	}


	/**
	 * Set the list to the columns of the given bank
	 *
	 * @param bankName the name of the bank
	 */
	public String[] colNames(String bankName) {
		if (bankName != null) {
			return DataWarehouse.getInstance().getColumnNames(bankName);
		}
		return null;
	}

	// persist the visibility selections
	private void writeVisibility() {
		PropertiesManager.getInstance().putAndWrite(_bankName,
				Long.toString(ColumnVisibility.selectedMask(_cbarray)));
	}

	// get visibility from properties
	private void readVisibility() {
		String vs = PropertiesManager.getInstance().get(_bankName);
		ColumnVisibility.parseMask(vs)
				.ifPresent(mask -> ColumnVisibility.applyMask(mask, _cbarray, _table.getColumnModel()));
	} // readvis

	/**
	 * Update the table with the new event
	 */
	public void update() {
		DataEvent event = _eventManager.getCurrentEvent();
		if (event == null) {
			return;
		}

		fixButtons();
		_table.setEvent(event);
		revalidate();
	}

	/**
	 * Empty table
	 */
	public void clear() {
		_table.setEvent(null);
	}

	@Override
	public void windowClosing(WindowEvent e) {
		_dataBanks.remove(_bankName);
		_eventManager.removeClasIoEventListener(this);
	}

	// WindowListener methods
	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowOpened(WindowEvent e) {
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
	public void itemStateChanged(ItemEvent e) {
		JCheckBox cb = (JCheckBox) (e.getSource());
		int index = -1;

		for (int i = 0; i < _cbarray.length; i++) {
			if (cb == _cbarray[i]) {
				index = i;
				break;
			}
		}

		if (index >= 0) {
			// plus 1 for row column
			TableColumn column = _table.getColumnModel().getColumn(index + 1);
			if (cb.isSelected()) {
				column.setMinWidth(20);
				column.setMaxWidth(500);
				column.setPreferredWidth(BankDataTable.COLWIDTH);
				column.setResizable(true);
			} else {
				column.setMinWidth(0);
				column.setMaxWidth(0);
				column.setPreferredWidth(0);
				column.setResizable(false);
			}
			_table.revalidate();
			writeVisibility();
		}
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
		setSeqEventNumber();
		setTrueEventNumber();
	}

	/**
	 * Set the displayed sequential event number value. The sequential event number
	 * is just the number in the file.
	 *
	 * @param seqEventNum event number.
	 */
	public void setSeqEventNumber() {
		int seqEventNum = _eventManager.getSequentialEventNumber();
		if (seqEventNum > -1) {
			seqEventNumberLabel.setText("" + seqEventNum);
		}
	}

	/**
	 * Set the displayed true event number value. The true event number comes from
	 * the RUN::config bank.
	 *
	 * @param trueEventNum event number.
	 */
	public void setTrueEventNumber() {
		int trueEventNum = _eventManager.getTrueEventNumber();

		if (trueEventNum > -1) {
			trueEventNumberLabel.setText("" + trueEventNum);
		} else {
			trueEventNumberLabel.setText("n/a");
		}
	}

	@Override
	public void openedNewEventFile(String path) {
	}

	@Override
	public void changedEventSource(EventSourceType source) {
	}

	@Override
	public void newClasIoEvent(DataEvent event) {
		if (!_eventManager.isAccumulating()) {
			update();
		}
	}



}
