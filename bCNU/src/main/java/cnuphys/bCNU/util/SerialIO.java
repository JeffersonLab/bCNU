package cnuphys.bCNU.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import cnuphys.bCNU.log.Log;

public class SerialIO {

	/**
	 * Reads a serializable object from a file.
	 *
	 * @param fullfn the full path.
	 * @return the deserialized object.
	 */
	public static Object serialRead(String fullfn) {
		try (FileInputStream fileInput = new FileInputStream(fullfn);
				ObjectInputStream objectInput = new ObjectInputStream(fileInput)) {
			return objectInput.readObject();
		} catch (Exception e) {
			logFailure("read serialized data from " + fullfn, e);
			return null;
		}
	}

	/**
	 * serialRead reads a serializable object from a byte array
	 *
	 * @param bytes the byte array
	 * @return the deserialized object
	 */
	public static Object serialRead(byte[] bytes) {
		try (ByteArrayInputStream byteInput = new ByteArrayInputStream(bytes);
				ObjectInputStream objectInput = new ObjectInputStream(byteInput)) {
			return objectInput.readObject();
		} catch (Exception e) {
			logFailure("read serialized data from a byte array", e);
			return null;
		}
	}

	/**
	 * serialWrite writes out a serializable object to a file.
	 *
	 * @param obj    the serializable object.
	 *
	 * @param fullfn the full path.
	 */
	public static void serialWrite(Serializable obj, String fullfn) {
		try (FileOutputStream fileOutput = new FileOutputStream(fullfn);
				ObjectOutputStream objectOutput = new ObjectOutputStream(fileOutput)) {
			objectOutput.writeObject(obj);
			objectOutput.flush();
		} catch (Exception e) {
			logFailure("write serialized data to " + fullfn, e);
		}
	}

	/**
	 * serialWrite writes out a serializable object to a byte array.
	 *
	 * @param obj the serializable object.
	 * @return the array of bytes.
	 */
	public static byte[] serialWrite(Serializable obj) {
		try (ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
				ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput)) {
			objectOutput.writeObject(obj);
			objectOutput.flush();
			return byteOutput.toByteArray();
		} catch (Exception e) {
			logFailure("write serialized data to a byte array", e);
			return null;
		}
	}

	private static void logFailure(String operation, Exception exception) {
		Log.getInstance().error("Could not " + operation);
		Log.getInstance().exception(exception);
	}
}
