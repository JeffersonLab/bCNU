package cnuphys.ced.properties;

import java.io.File;
import java.util.Properties;

import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.util.SerialIO;
import edu.cnu.mdi.util.Environment;

/**
 * For dealing with persistent user preferences
 *
 * @author heddle
 *
 */
public class PropertiesManager {

	// singleton
	private static volatile PropertiesManager _instance;

	private static Properties _userPref;

	private static File _upFile;

	// private constructor for singleton
	private PropertiesManager() {
	}

	/**
	 * Public access for the singleton
	 *
	 * @return the PropertiesManager singleton.
	 */
	public static PropertiesManager getInstance() {

		if (_instance == null) {
			synchronized (PropertiesManager.class) {
				if (_instance == null) {
					_instance = new PropertiesManager();
					_instance.getPropertiesFromDisk();
				}
			}
		}
		return _instance;
	}

	/**
	 * Put in a property, then write the preferences
	 *
	 * @param key   the key
	 * @param value the value
	 */
	public void putAndWrite(String key, String value) {
		if ((_userPref != null) && (_upFile != null) && (key != null) && (value != null)) {

			_userPref.put(key, value);
			writeProperties();
		}
	}

	/**
	 * Put in a property, don't write the preferences
	 *
	 * @param key   the key
	 * @param value the value
	 */
	public void put(String key, String value) {
		if ((_userPref != null) && (_upFile != null) && (key != null) && (value != null)) {

			_userPref.put(key, value);
		}
	}

	/**
	 * Get a property from the user preferences
	 *
	 * @param key the key
	 * @return the property, or <code>null</code> if not found
	 */
	public String get(String key) {
		if ((_userPref != null) && (_upFile != null)) {
			return _userPref.getProperty(key);
		}
		return null;
	}

	/**
	 * Write the properties file. Should be .ced.user.pref in the home dir
	 */
	public void writeProperties() {
		if ((_upFile == null) || (_userPref == null)) {
			return;
		}

		SerialIO.serialWrite(_userPref, _upFile.getPath());
	}

	/**
	 * Read the property from from the home directory.
	 */
	private void getPropertiesFromDisk() {
		String homeDir = Environment.getInstance().getHomeDirectory();
		_upFile = new File(homeDir, ".ced.user.pref");
		_userPref = loadProperties(_upFile);
	}

	static Properties loadProperties(File preferenceFile) {
		if ((preferenceFile == null) || !preferenceFile.isFile()) {
			return new Properties();
		}

		Object savedPreferences = SerialIO.serialRead(preferenceFile.getPath());
		if (savedPreferences instanceof Properties properties) {
			return properties;
		}

		Log.getInstance().warning("Ignoring invalid CED preference file: " + preferenceFile.getPath());
		return new Properties();
	}
}
