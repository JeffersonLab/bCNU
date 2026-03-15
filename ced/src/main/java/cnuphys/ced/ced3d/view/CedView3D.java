package cnuphys.ced.ced3d.view;

import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.util.TextUtilities;
import cnuphys.ced.ced3d.CedPanel3D;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.IClasIoEventListener;
import cnuphys.ced.component.IBankMatching;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.event.IAccumulationListener;
import cnuphys.ced.properties.PropertiesManager;
import cnuphys.lund.SwimTrajectoryListener;
import cnuphys.swim.Swimming;

@SuppressWarnings("serial")
public abstract class CedView3D extends PlainView3D
		implements IClasIoEventListener, SwimTrajectoryListener, IAccumulationListener, IBankMatching {

	// the event manager
	private final ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();

	//for appending event number to the titile
	private static final String evnumAppend = "  (Event# ";

	//for bank matching
	protected static final String[] _noMatches = { CedView.NOMATCHES };
	protected String _matches[] = _noMatches;

	/**
	 * Create a 3D view
	 *
	 * @param title
	 * @param angleX
	 * @param angleY
	 * @param angleZ
	 * @param xDist
	 * @param yDist
	 * @param zDist
	 */
	public CedView3D(String title, float angleX, float angleY, float angleZ, float xDist, float yDist, float zDist) {
		super(title, angleX, angleY, angleZ, xDist, yDist, zDist);

		_eventManager.addClasIoEventListener(this, 2);

		// listen for trajectory changes
		Swimming.addSwimTrajectoryListener(this);

		AccumulationManager.getInstance().addAccumulationListener(this);
	}

	// make the 3d panel
	@Override
	protected abstract CedPanel3D make3DPanel(float angleX, float angleY, float angleZ, float xDist, float yDist,
			float zDist);

	/**
	 * Check if the bank matching array is the no matches array
	 *
	 * @return true if no matches
	 */
	public boolean hasNoBankMatches() {
		return ((_matches != null) && (_matches.length == 1) && CedView.NOMATCHES.equals(_matches[0]));
	}

	@Override
	public void newClasIoEvent(DataEvent event) {
		if (!_eventManager.isAccumulating()) {
			fixTitle(event);
			_panel3D.refreshQueued();
		}
	}

	@Override
	public void openedNewEventFile(String path) {
		_panel3D.refreshQueued();
	}

	/**
	 * Change the event source type
	 *
	 * @param source the new source: File, ET
	 */
	@Override
	public void changedEventSource(ClasIoEventManager.EventSourceType source) {
	}

	@Override
	public void accumulationEvent(int reason) {
		switch (reason) {
		case AccumulationManager.ACCUMULATION_STARTED:
			break;

		case AccumulationManager.ACCUMULATION_CANCELLED:
			fixTitle(_eventManager.getCurrentEvent());
			_panel3D.refresh();
			break;

		case AccumulationManager.ACCUMULATION_FINISHED:
			fixTitle(_eventManager.getCurrentEvent());
			_panel3D.refresh();
			break;
		}
	}

	@Override
	public void trajectoriesChanged() {
		if (!_eventManager.isAccumulating()) {
			_panel3D.refreshQueued();
		}
	}


	/**
	 * Fix the title of the view after an event arrives. The default is to append
	 * the event number.
	 *
	 * @param event the new event
	 */
	protected void fixTitle(DataEvent event) {
		String title = getTitle();
		int index = title.indexOf(evnumAppend);
		if (index > 0) {
			title = title.substring(0, index);
		}

		int seqNum = _eventManager.getSequentialEventNumber();
		int trueNum = _eventManager.getTrueEventNumber();
		if (seqNum > 0) {
			if (trueNum > 0) {
				setTitle(title + evnumAppend + seqNum + "  True event# " + trueNum + ")");
			}
			else {
			    setTitle(title + evnumAppend + seqNum + ")");
			}
		}
	}

	/**
	 * Write some properties for persitance
	 */
	@Override
	public void writeCommonProperties() {
		// bank match
		String propName = getPropertyName() + "_" + CedView.BANKMATCHPROP;
		String cssStr = TextUtilities.stringArrayToString(_matches);

		System.err.println();
		if (cssStr == null) {
			cssStr = CedView.NOMATCHES;
		}

		PropertiesManager.getInstance().putAndWrite(propName, cssStr);

	}

	/**
	 * Get banks of interest for matching banks panel on tabbed pane on control
	 * panel. If null, all banks are of interest
	 *
	 * @return banks of interest for matching banks
	 */
	@Override
	public String[] getBanksMatches() {
		return _matches;
	}

	/**
	 * Get banks of interest for matching banks panel on tabbed pane on control
	 * panel. If null, all banks are of interest (not advisable); Default does
	 * nothing
	 */
	@Override
	public void setBankMatches(String[] matches) {

		if ((matches == null) || (matches.length == 0)) {
			_matches = _noMatches;
			return;
		}

		_matches = new String[matches.length];

		for (int i = 0; i < matches.length; i++) {
			String s = matches[i];
			_matches[i] = new String(s);
		}
	}

}
