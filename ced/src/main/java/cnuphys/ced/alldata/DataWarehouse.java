package cnuphys.ced.alldata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.jnp.hipo4.data.Schema;
import org.jlab.jnp.hipo4.data.SchemaFactory;

import cnuphys.bCNU.threading.EventNotifier;
import cnuphys.ced.alldata.datacontainer.IDataContainer;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.ClasIoEventListenerPhase;
import cnuphys.ced.clasio.ClasIoEventManager.EventSourceType;
import cnuphys.ced.clasio.IClasIoEventListener;

public class DataWarehouse implements IClasIoEventListener {

	//the singleton
	private static volatile DataWarehouse _instance;

	// all the known banks in the current event
	private ArrayList<String> _knownBanks = new ArrayList<>();
	
	private HashMap<String, Long> _seenBanks = new HashMap<>();

	//the current schema factory (dictionary)
	private SchemaFactory _schemaFactory;

	// private constructor for singleton
	private DataWarehouse() {
		ClasIoEventManager.getInstance().addClasIoEventListener(this, ClasIoEventListenerPhase.DATA);
	}

	/** type is unknown */
	public static final int UNKNOWN = 0;

	/** type is a byte */
	public static final int INT8 = 1;

	/** type is a short */
	public static final int INT16 = 2;

	/** type is an int */
	public static final int INT32 = 3;

	/** type is a float */
	public static final int FLOAT32 = 4;

	/** type is a double */
	public static final int FLOAT64 = 5;

	/** type is a string */
	public static final int STRING = 6;

	/** type is a group */
	public static final int GROUP = 7;

	/** type is a long int */
	public static final int INT64 = 8;

	/** type is a vector3f */
	public static final int VECTOR3F = 9;

	/** type is a composite */
	public static final int COMPOSITE = 10;

	/** type is a table */
	public static final int TABLE = 11;

	/** type is a branch */
	public static final int BRANCH = 12;

	/** type names */
	private static final String[] TYPE_NAMES = { "Unknown", "byte", "short", "int", "float", "double", "string", "group", "long", "vector3f", "composite", "table", "branch"};

	/** the column data used by the node panel */
	private ArrayList<ColumnData> _columnData = new ArrayList<>();

	//for notifying about new data
	private final EventNotifier<DataContainerNotification> eventNotifier = new EventNotifier<>();

	/**
	 * Public access to the singleton
	 *
	 * @return the singleton
	 */
	public static DataWarehouse getInstance() {
		if (_instance == null) {
			synchronized (DataWarehouse.class) {
                if (_instance == null) {
                    _instance = new DataWarehouse();
                }
            }
        }
		return _instance;
	}

	/**
	 * Get the banks in the current event
	 *
	 * @return the known banks in a String array
	 */
	public String[] getKnownBanks() {
		return _knownBanks.toArray(String[]::new);
	}

	/**
	 * Checks if a bank, identified by a string such as "XXXX::hits", is in the
	 * current event.
	 *
	 * @param bankName the bank name
	 * @return <code>true</code> if the bank is in the curent event.
	 */
	public boolean isBankInCurrentEvent(String bankName) {
		if ((bankName == null) || (_knownBanks == null)) {
			return false;
		}

		int index = Collections.binarySearch(_knownBanks, bankName);
		return index >= 0;
	}


	/**
	 * Does the current event have a bank with the given name?
	 *
	 * @param bankName the bank name
	 * @return <code>true</code> if the current event has the bank
	 */
	public boolean hasBank(String bankName) {
		return getBank(bankName) != null;
	}

	/**
	 * Update the schema factory
	 * @param schemaFactory
	 */
	public void updateSchema(SchemaFactory schemaFactory) {

		_schemaFactory = schemaFactory;
		_knownBanks.clear();
		if (schemaFactory == null) {
			return;
		}

		//schemas are banks
		List<Schema> schemas = schemaFactory.getSchemaList();

		// a schema is a bank
		if (schemas == null || schemas.isEmpty()) {
			return;
		}

		for (Schema schema : schemas) {
			_knownBanks.add(schema.getName());
		}

        // sort the banks
		_knownBanks.sort(null);

	}


	/**
	 * Get the type of a column
	 * @param bankName the bank name
	 * @param columnName the column name
	 * @return the data type of the column, or UNKNOWN if not found
	 */
	public int getType(String bankName, String columnName) {
		return columnType(_schemaFactory, bankName, columnName);
	}

	static int columnType(SchemaFactory schemaFactory, String bankName, String columnName) {
		Schema schema = findSchema(schemaFactory, bankName);
		return (schema == null || columnName == null || !schema.hasEntry(columnName))
				? UNKNOWN : schema.getType(columnName);
	}

	static Schema findSchema(SchemaFactory schemaFactory, String bankName) {
		if (schemaFactory == null || bankName == null || !schemaFactory.hasSchema(bankName)) {
			return null;
		}
		return schemaFactory.getSchema(bankName);
	}

	/**
	 * Get the data type name of a column
	 * @param bankName the bank name
	 * @param columnName the column name
	 * @return the data type name of the column, or "Unknown" if not found
	 */
	public String getTypeName(String bankName, String columnName) {
		return typeName(getType(bankName, columnName));
	}

	static String typeName(int type) {
		return (type < 0 || type >= TYPE_NAMES.length) ? TYPE_NAMES[UNKNOWN] : TYPE_NAMES[type];
	}

	/**
	 * Get the sorted list of column names for a bank name
	 *
	 * @param bankName the bank name
	 * @return the list of column names
	 */
	public String[] getColumnNames(String bankName) {
		Schema schema = findSchema(_schemaFactory, bankName);
		if (schema != null) {
			List<String> list = schema.getEntryList();
			String[] array = new String[list.size()];
			list.toArray(array);
			Arrays.sort(array);
			return array;
		}
		return null;
	}

	//get the current event from the IO manager
	public DataEvent getCurrentEvent() {
		return ClasIoEventManager.getInstance().getCurrentEvent();
	}

	/**
	 * Get a list of the banks in the current event
	 * @return banks in current event
	 */
	public String[] banks() {
		DataEvent event = getCurrentEvent();
		return (event == null) ? null : sortedCopy(event.getBankList());
	}

	static String[] sortedCopy(String[] values) {
		if (values == null) {
			return null;
		}
		String[] copy = values.clone();
		Arrays.sort(copy);
		return copy;
    }
	
	/**
	 * Get a bank in the current event
	 * @param bankName the bank name
	 * @return the bank or null
	 */
	public DataBank getBank(String bankName) {
		return findBank(getCurrentEvent(), bankName);
	}

	static DataBank findBank(DataEvent event, String bankName) {
		if (event == null || bankName == null || !event.hasBank(bankName)) {
			return null;
		}
		return event.getBank(bankName);
	}

	/**
	 * Get a byte array for the bank and column names in the current event
	 *
	 * @param bankName   the bank name
	 * @param columnName the column name
	 * @return a byte array or null.
	 */
	public byte[] getByte(String bankName, String columnName) {
		DataBank bank = getBank(bankName);
		if (hasColumn(bank, columnName)) {
			return bank.getByte(columnName);
		}
		return null;
	}
	
	/**
	 * Get a float array for the bank and column names in the current event
	 *
	 * @param bankName   the bank name
	 * @param columnName the column name
	 * @return a float array or null.
	 */
	public float[] getFloat(String bankName, String columnName) {
		DataBank bank = getBank(bankName);
		if (hasColumn(bank, columnName)) {
			return bank.getFloat(columnName);
		}
		return null;
	}

	/**
	 * Get a short array for the bank and column names in the current event
	 * @param bankName the bank name
	 * @param columnName the column name
	 * @return a short array or null.
	 */
	public short[] getShort(String bankName, String columnName) {
		DataBank bank = getBank(bankName);
		if (hasColumn(bank, columnName)) {
			return bank.getShort(columnName);
		}
		return null;
	}

	/**
	 * Get an int array for the bank and column names in the current event
	 * @param bankName the bank name
	 * @param columnName the column name
	 * @return a short array or null.
	 */
	public int[] getInt(String bankName, String columnName) {
		DataBank bank = getBank(bankName);
		if (hasColumn(bank, columnName)) {
			return bank.getInt(columnName);
		}
		return null;
	}

	/**
	 * Get a long array for the bank and column names in the current event
	 *
	 * @param bankName   the bank name
	 * @param columnName the column name
	 * @return a short array or null.
	 */
	public long[] getLong(String bankName, String columnName) {
		DataBank bank = getBank(bankName);
		if (hasColumn(bank, columnName)) {
			return bank.getLong(columnName);
		}
		return null;
   }

	/**
	 * Get a double array for the bank and column names in the current event
	 *
	 * @param bankName   the bank name
	 * @param columnName the column name
	 * @return a short array or null.
	 */
	public double[] getDouble(String bankName, String columnName) {
		DataBank bank = getBank(bankName);
		if (hasColumn(bank, columnName)) {
			return bank.getDouble(columnName);
		}
		return null;
	}

	/**
	 * Notify the data containers of a new event
	 *
	 * @param event the new event they should use to update themselves.
	 *
	 */
	public void notifyListeners(DataEvent event) {
		eventNotifier.nonThreadedTriggerEvent(new DataContainerNotification.Update(event));
	}

	/**
	 * Notify the data containers to clear their data
	 *
	 */
	public void notifyListeners() {
		eventNotifier.nonThreadedTriggerEvent(DataContainerNotification.Clear.INSTANCE);
	}

	/**
	 * Does the current event have a bank with the given name?
	 * @param bankName the bank name
	 * @param columnName the column name
	 * @return <code>true</code> if the current event has the bank and column
     */
	public boolean bankContainsColumn(String bankName, String columnName) {
		return hasColumn(getBank(bankName), columnName);
	}

	static boolean hasColumn(DataBank bank, String columnName) {
		if (bank == null || columnName == null) {
			return false;
		}
		String[] columnNames = bank.getColumnList();
		if (columnNames != null) {
			for (String name : columnNames) {
				if (columnName.equals(name)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Get the number of rows in a bank for the current event
	 *
	 * @param bankName the bank name
	 * @return the number of rows in the bank, or 0 if the bank is not found.
	 */
	public int rows(String bankName) {
		DataBank bank = getBank(bankName);
		return (bank == null) ? 0 : bank.rows();
	}


	/**
	 * Add an data container listener.
	 *
	 * @param listener the data container listener to add.
	 */
	public void addDataContainerListener(IDataContainer listener) {
		eventNotifier.addListener(new DataListener(listener));
	}


	@Override
	public void newClasIoEvent(DataEvent event) {


		// create the column data
		_columnData.clear();

		int bankIndex = 0;
		for (String bankName : _knownBanks) {
			DataBank bank = event.getBank(bankName);
			if ((bank != null) && (event.hasBank(bankName))) {
		  	    String columnNames[] = bank.getColumnList();
		  	    Arrays.sort(columnNames);
				for (String columnName : columnNames) {
					_columnData.add(new ColumnData(bankName, columnName, getType(bankName, columnName), bankIndex));
				}
				bankIndex++;
			}
		}
		
		//update seen banks
		String[] banks = event.getBankList();
		if (banks != null) {
			for (String bank : banks) {
				if (_seenBanks.get(bank) == null) {
					_seenBanks.put(bank, 1L);
				}
				else {
                    _seenBanks.put(bank, _seenBanks.get(bank) + 1L);
				}
			}
		}

		notifyListeners(); //clear previous data
		notifyListeners(event);
	}
	
	/**
	 * Get the sorted list of seen banks and their counts.
	 * IS reset if new file opened or event source changed
	 * @return the list of seen banks and their counts
	 */
	public List<String> getSortedSeenBanks() {
		ArrayList<String> bankCounts = new ArrayList<>();
		List<String> keys = new ArrayList<>(_seenBanks.keySet());
		Collections.sort(keys);		
		for (String key : keys) {
			String s = "[" + key + "]  (" + _seenBanks.get(key) + ")";
			bankCounts.add(s);
		}
		return bankCounts;
	}
	
	/**
	 * Clear the seen banks
	 */
	public void clearSeenBanks() {
		_seenBanks.clear();
	}
	

	@Override
	public void openedNewEventFile(String path) {
		_seenBanks.clear();
		notifyListeners();
	}

	@Override
	public void changedEventSource(EventSourceType source) {
		_seenBanks.clear();
		notifyListeners();
	}

	/**
	 * Get the column data
	 *
	 * @return the column data
	 */
	public ArrayList<ColumnData> getColumnData() {
		return _columnData;
	}
	
	/**
	 * Get the maximum int value for the given column in the given bank
	 * 
	 * @param bankName the bank name
	 * @param column   the column name
	 * @return the maximum int value (-1 on failure)
	 */
	public static int getMaxIntValue(String bankName, String column) {
		int max = -1;
		int[] values = DataWarehouse.getInstance().getInt(bankName, column);
		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				max = Math.max(max, values[i]);
			}
		}
		return max;
	}

}
