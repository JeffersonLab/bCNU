package cnuphys.ced.trigger;

import java.awt.Dimension;

import org.jlab.io.base.DataEvent;

import cnuphys.ced.alldata.DataWarehouse;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.ClasIoEventListenerPhase;
import cnuphys.ced.clasio.ClasIoEventManager.EventSourceType;
import cnuphys.ced.clasio.IClasIoEventListener;

public class TriggerMenuPanel extends TriggerPanel implements IClasIoEventListener {

	// the data warehouse
	private DataWarehouse _dataWarehouse = DataWarehouse.getInstance();

	// the bank name
	private static String _bankName = "RUN::trigger";

	public TriggerMenuPanel() {
		super(true);
		ClasIoEventManager.getInstance().addClasIoEventListener(this, ClasIoEventListenerPhase.VIEW);
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		d.width = _preferredWidth;
		return d;
	}

	@Override
	public Dimension getMaximumSize() {
		return getPreferredSize();
	}

	@Override
	public void newClasIoEvent(DataEvent event) {
		if (ClasIoEventManager.getInstance().isAccumulating()) {
		} else { // single event
			setBits(0, 0);

			int idData[] = null;
			int triggerData[] = null;

			idData = _dataWarehouse.getInt(_bankName, "id");
			if ((idData != null) && (idData.length > 0)) {
				triggerData = _dataWarehouse.getInt(_bankName, "trigger");
				setBits(idData[0], triggerData[0]);
			}

		}
	}

	@Override
	public void openedNewEventFile(String path) {
	}

	@Override
	public void changedEventSource(EventSourceType source) {
	}


}
