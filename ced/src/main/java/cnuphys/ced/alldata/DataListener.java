package cnuphys.ced.alldata;

import cnuphys.bCNU.threading.IEventListener;
import cnuphys.ced.alldata.datacontainer.IDataContainer;

final class DataListener implements IEventListener<DataContainerNotification> {

	private final IDataContainer container;

	public DataListener(IDataContainer container) {
		this.container = container;
	}

	@Override
	public void newEvent(DataContainerNotification notification) {
		notification.dispatchTo(container);
	}

}
