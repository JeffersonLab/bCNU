package cnuphys.ced.clasio.datatable;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import org.jlab.io.base.DataEvent;

import edu.cnu.mdi.ui.fonts.Fonts;
import cnuphys.ced.clasio.table.HeaderRenderer;
import cnuphys.ced.clasio.table.SimpleRenderer;

public class BankDataTable extends JTable {

	private JScrollPane _scrollPane;

	public static final int COLWIDTH = 100;


	//the bank name
	private String _bankName;

	/**
	 * Create a bank table
	 *
	 * @param bankName the name of the bank, e.g. "DC::tdc"
	 */
	public BankDataTable(String bankName) {
		super(new BankTableModel(bankName));
		_bankName = bankName;

		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);


		HeaderRenderer hrender = new HeaderRenderer();
		SimpleRenderer renderer = new SimpleRenderer();
		setFont(Fonts.tweenFont);

		// set preferred widths
		for (int i = 0; i < getColumnCount(); i++) {
			TableColumn column = getColumnModel().getColumn(i);
			column.setCellRenderer(renderer);
			column.setHeaderRenderer(hrender);
			if (i == 0) {
				FontMetrics fm = getFontMetrics(getFont());
				int sw = fm.stringWidth("999999");
				column.setPreferredWidth(sw);
				column.setMaxWidth(sw);
				column.setResizable(false);
			} else {
				column.setPreferredWidth(COLWIDTH);
			}
		}

//		this.setAutoResizeMode(AUTO_RESIZE_OFF);
		getTableHeader().setResizingAllowed(true);
		setShowGrid(true);
		setGridColor(Color.gray);

		//detect clicks on header for sorting
		MouseAdapter mouseAdapter = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Point point = e.getPoint();
				int column = columnAtPoint(point);
				getBankTableModel().sort(column);
			}

		};
		JTableHeader header = getTableHeader();
		header.addMouseListener(mouseAdapter);
	}


	@Override
	public void valueChanged(ListSelectionEvent e) {
		   if (e.getValueIsAdjusting()) {
			return;
		}

		super.valueChanged(e);

		int row = getSelectedRow();
		if (row < 0) {
			SelectedDataManager.notifyListeners(_bankName, -1);
			return;
		}

		Object val = getBankTableModel().getValueAt(row, 0);
		if (val == null) {
			SelectedDataManager.notifyListeners(_bankName, -1);
			return;
		}

		int index = (int) val;
		SelectedDataManager.notifyListeners(_bankName, index);
	}


	/**
	 * Get all the TableColumns in an array
	 *
	 * @return all the table columns
	 */
	public TableColumn[] getTableColumns() {
		int size = getColumnCount();
		if (size < 1) {
			return null;
		}

		TableColumn cols[] = new TableColumn[size];
		for (int i = 0; i < size; i++) {
			cols[i] = getColumnModel().getColumn(i);
		}

		return cols;
	}

	/**
	 * Get the underlying model
	 *
	 * @return the data model
	 */
	public BankTableModel getBankTableModel() {
		return (BankTableModel) getModel();
	}

	/**
	 * Set the event for table display
	 *
	 * @param event the event
	 */
	public void setEvent(DataEvent event) {
		getBankTableModel().setData(event);
	}

	/**
	 * Get the table's scroll pane
	 *
	 * @return te table's scroll pane
	 */
	public JScrollPane getScrollPane() {
		if (_scrollPane == null) {
			_scrollPane = new JScrollPane(this) {
				@Override
				public Dimension getPreferredSize() {
					int width = getModel().getColumnCount() * COLWIDTH + 20;
					return new Dimension(Math.min(1000, width), 550);
				}

			};
		}
		return _scrollPane;
	}


}
