package cnuphys.ced.trigger;

import java.awt.Dimension;

import org.jlab.io.base.DataEvent;

import cnuphys.ced.alldata.RunTriggers;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.ClasIoEventListenerPhase;
import cnuphys.ced.clasio.ClasIoEventManager.EventSourceType;
import cnuphys.ced.clasio.IClasIoEventListener;

public class TriggerMenuPanel extends TriggerPanel implements IClasIoEventListener {

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

			RunTriggers triggers = RunTriggers.getInstance();
			if (triggers.hasCompleteRow(0)) {
				setBits(triggers.id(0), triggers.trigger(0));
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
