package cnuphys.ced.clasio;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.view.BaseView;
import cnuphys.ced.clasio.table.NodePanel;

public class ClasIoEventView extends BaseView {

	// holds the panel that has the table
	protected NodePanel _nodePanel;

	// singleton
	private static ClasIoEventView instance;

	/**
	 * Constructor for the Event view, which manages events from an event file.
	 */
	private ClasIoEventView() {

		super(PropertySupport.TITLE, "Current Event", PropertySupport.ICONIFIABLE, true, PropertySupport.MAXIMIZABLE,
				true, PropertySupport.CLOSABLE, true, PropertySupport.RESIZABLE, true, PropertySupport.WIDTH, 1100,
				PropertySupport.HEIGHT, 650, PropertySupport.VISIBLE, true, PropertySupport.TOOLBAR, false,
				PropertySupport.PROPNAME, "CUREVENT");

		JPanel sPanel = new JPanel();
		sPanel.setLayout(new BorderLayout(2, 2));
		_nodePanel = new NodePanel(this);

		sPanel.add(_nodePanel, BorderLayout.CENTER);
		add(sPanel);
	}


	/**
	 * Create the event view, or return the already created singleton.
	 *
	 * @return the event view singleton.
	 */
	public static ClasIoEventView createEventView() {

		if (instance == null) {
			instance = new ClasIoEventView();
		}
		return instance;
	}

	/**
	 * Access to the singleton (might be <code>null</code>
	 *
	 * @return the event view singleton.
	 */
	public static ClasIoEventView getInstance() {
		return instance;
	}

}