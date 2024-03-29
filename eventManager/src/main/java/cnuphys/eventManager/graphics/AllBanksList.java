package cnuphys.eventManager.graphics;

import java.awt.Dimension;

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.eventManager.namespace.NameSpaceManager;

/**
 * Puts all known banks into a scrollable list
 *
 * @author heddle
 *
 */
public class AllBanksList extends JList<String> {

	private static Dimension _size = new Dimension(220, 250);

	// the scroll pane
	private JScrollPane _scrollPane;

	public AllBanksList() {
		super(NameSpaceManager.getInstance().getKnownBanks());
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		_scrollPane = new JScrollPane(this);
		_scrollPane.setPreferredSize(_size);
		_scrollPane.setBorder(new CommonBorder("Bank Name"));
	}

	/**
	 * Get the scroll pane
	 *
	 * @return the scroll pane
	 */
	public JScrollPane getScrollPane() {
		return _scrollPane;
	}

}
