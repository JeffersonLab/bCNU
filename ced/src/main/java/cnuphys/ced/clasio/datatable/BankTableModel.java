package cnuphys.ced.clasio.datatable;

import javax.swing.table.DefaultTableModel;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

import edu.cnu.mdi.format.DoubleFormat;
import cnuphys.bCNU.util.ArrayIndexSorter;
import cnuphys.bCNU.util.PrimitiveArrayToWrapper;
import cnuphys.ced.alldata.DataWarehouse;

public class BankTableModel extends DefaultTableModel {

	// the data warehouse
	private static DataWarehouse _dataWarehouse = DataWarehouse.getInstance();

	private String _bankName;
	private String _columnNames[];

	//index array for first column
	private int _index[];

	// the data
	private DataEvent _event;

	//used in sorting
	private boolean _ascending = true;
	private int _lastSortColumn = -1;

	public BankTableModel(String bankName) {
		super(getColumnNames(bankName), 2);
		_bankName = bankName;
		_columnNames = getColumnNames(bankName);
	}

	/**
	 * Get the number of columns
	 *
	 * @return the number of columns
	 */
	@Override
	public int getColumnCount() {
		return _columnNames.length;
	}

	/**
	 * Set the data event
	 *
	 * @param event the data event
	 */
	public void setData(DataEvent event) {
		_event = event;
		_index = null;
		_ascending = true;
		_lastSortColumn = -1;

		if (event != null) {
			DataBank bank = event.getBank(_bankName);
			if (bank != null) {
				_index = new int[bank.rows()];
				for (int i = 0; i < _index.length; i++) {
					_index[i] = i;
				}
			}
		}


		fireTableDataChanged();
	}

	/**
	 * Get the number of rows
	 *
	 * @return the number of rows
	 */
	@Override
	public int getRowCount() {
		if (_event == null) {
			return 0;
		}

		DataBank bank = _event.getBank(_bankName);
		if (bank == null) {
			return 0;
		}

		return bank.rows();
	}


	/**
	 * Sort the table by a given column. Uses an index array to keep track of the
	 * original row order.
	 * @param column the column
	 */
	public void sort(int column) {
		int n = getRowCount();
		if (n < 2) {
			return;
		}

		if (column == _lastSortColumn) {
			_ascending = !_ascending;
		} else {
			_ascending = true;
			_lastSortColumn = column;
		}

		//1st column is easy
		if (column == 0) {
			if (_ascending) {
				for (int i = 0; i < n; i++) {
					_index[i] = i;
				}
			} else {
				for (int i = 0; i < n; i++) {
					_index[i] = n - 1 - i;
				}
			}
		} else {
			String columnName = _columnNames[column];
			int type = _dataWarehouse.getType(_bankName, columnName);

			int newIndex[] = null;
			switch (type) {
			case DataWarehouse.INT8:
				byte ba[] = _dataWarehouse.getByte(_bankName, columnName);
				newIndex = ArrayIndexSorter.sortIndices(PrimitiveArrayToWrapper.toWrapper(ba), _ascending);
				break;
			case DataWarehouse.INT16:
				short sa[] = _dataWarehouse.getShort(_bankName, columnName);
				newIndex = ArrayIndexSorter.sortIndices(PrimitiveArrayToWrapper.toWrapper(sa), _ascending);
				break;
			case DataWarehouse.INT32:
				int ia[] = _dataWarehouse.getInt(_bankName, columnName);
				newIndex = ArrayIndexSorter.sortIndices(PrimitiveArrayToWrapper.toWrapper(ia), _ascending);
				break;
			case DataWarehouse.INT64:
				long la[] = _dataWarehouse.getLong(_bankName, columnName);
				newIndex = ArrayIndexSorter.sortIndices(PrimitiveArrayToWrapper.toWrapper(la), _ascending);
				break;
			case DataWarehouse.FLOAT32:
		        float fa[] = _dataWarehouse.getFloat(_bankName, columnName);
				newIndex = ArrayIndexSorter.sortIndices(PrimitiveArrayToWrapper.toWrapper(fa), _ascending);
				break;
			case DataWarehouse.FLOAT64:
				double da[] = _dataWarehouse.getDouble(_bankName, columnName);
				newIndex = ArrayIndexSorter.sortIndices(PrimitiveArrayToWrapper.toWrapper(da), _ascending);
				break;
			default:
				break;
			}

			if (newIndex != null) {
				_index = newIndex;
			}

		}
		fireTableDataChanged();
	}

	/**
	 * Get the value at a given row and column
	 *
	 * @param row the 0-based row
	 * @param col the 0-based column
	 * @return the value at a given row and column
	 */
	@Override
	public Object getValueAt(int row, int col) {
		if ((row < 0) || (col < 0)) {
			return null;
		}

		//to account for sorting
		row = _index[row];

		if (col == 0) {
			return row;   //0-based index
		}

		DataBank bank = _event.getBank(_bankName);
		if (bank == null) {
			return null;
		}

		String columnName = _columnNames[col];
		int type = _dataWarehouse.getType(_bankName, columnName);

		switch (type) {
		case DataWarehouse.INT8:
			return "" + _dataWarehouse.getByte(_bankName, columnName)[row];
		case DataWarehouse.INT16:
			return "" + _dataWarehouse.getShort(_bankName, columnName)[row];
		case DataWarehouse.INT32:
			return "" + _dataWarehouse.getInt(_bankName, columnName)[row];
		case DataWarehouse.INT64:
			return "" + _dataWarehouse.getLong(_bankName, columnName)[row];
		case DataWarehouse.FLOAT32:
			float f = _dataWarehouse.getFloat(_bankName, columnName)[row];
			return DoubleFormat.doubleFormat(f, 5, 4);
		case DataWarehouse.FLOAT64:
			double d = _dataWarehouse.getDouble(_bankName, columnName)[row];
			return DoubleFormat.doubleFormat(d, 5, 4);
		default:
			return "???";
		}
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}

	/**
	 * Get the bank name
	 *
	 * @return the bank name
	 */
	public String getBankName() {
		return _bankName;
	}

	/**
	 * Get the table column names, which are the names of all the known banks for
	 * this bank
	 *
	 * @return the tames of all known data columns for this bank
	 */
	public String[] columnNames() {
		return _columnNames;
	}

	// add an extra column name for index
	private static String[] getColumnNames(String bankName) {
		String cnames[] = DataWarehouse.getInstance().getColumnNames(bankName);

		String expNames[] = new String[cnames.length + 1];
		expNames[0] = "";
		for (int i = 0; i < cnames.length; i++) {
			expNames[i + 1] = cnames[i];
		}

		return expNames;
	}
}
