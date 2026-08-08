package cnuphys.ced.clasio;

import java.util.EventListener;

import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.threading.IEventListener;

public interface IClasIoEventListener extends EventListener, IEventListener<Object> {
	/**
	 * Notifies listeners that a new event has arrived.
	 *
	 * @param event the new event.
	 */
	public void newClasIoEvent(final DataEvent event);

	/**
	 * Opened a new event file
	 *
	 * @param path the path to the new file
	 */
	public void openedNewEventFile(final String path);

	/**
	 * Change the event source type
	 *
	 * @param source
	 */
	public void changedEventSource(ClasIoEventManager.EventSourceType source);


	@Override
	default public void newEvent(Object data) {
		if (data instanceof DataEvent) {
			newClasIoEvent((DataEvent) data);
		} else if (data instanceof String) {
			openedNewEventFile((String) data);
		} else if (data instanceof ClasIoEventManager.EventSourceType) {
			changedEventSource((ClasIoEventManager.EventSourceType) data);
		} else {
			Log.getInstance().warning("Unknown event notification type: " + data);
		}
	}


}
