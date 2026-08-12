package cnuphys.ced.cedview;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import edu.cnu.mdi.ui.fonts.Fonts;

public class RollOverDCPanel extends JPanel {


	//rollover colors
	protected static final Color inactiveFG = Color.cyan;
	protected static final Color inactiveBG = Color.black;
	protected static final Color activeFG = Color.yellow;
	protected static final Color activeBG = Color.darkGray;

	//roll over labels
	protected static final String HB_ROLLOVER = "Reg Hit Based DC Clusters";
	protected static final String TB_ROLLOVER = "Reg Time Based DC Clusters";
	protected static final String AIHB_ROLLOVER = "AI Hit Based DC Clusters";
	protected static final String AITB_ROLLOVER = "AI Time Based DC Clusters";

	//rollover labels
	protected static String roLabels[] = {HB_ROLLOVER,
			TB_ROLLOVER,
			AIHB_ROLLOVER,
			AITB_ROLLOVER};

	//rollover boolean flags
	public boolean roShowHBDCClusters;
	public boolean roShowTBDCClusters;
	public boolean roShowAIHBDCClusters;
	public boolean roShowAITBDCClusters;

	//the parent view
	protected CedView view;

	/**
	 * Create a roll over panel
	 * @param view the parent view
	 * @param title the title of the panel
	 * @param numCols the number of columns
	 * @param labels the labels
	 */
	public RollOverDCPanel(CedView view, String title, int numCols) {
		this.view = view;

		int numRows = 1 + (roLabels.length - 1) / numCols;
		setLayout(new GridLayout(numRows, numCols, 4, 4));
		for (String text : roLabels) {
			addLabel(text);
		}
	}

	private void addLabel(String text) {
		JLabel label = new JLabel(text, SwingConstants.CENTER);
		label.setOpaque(true);
		label.setFont(Fonts.mediumFont);
		label.setForeground(inactiveFG);
		label.setBackground(inactiveBG);
		label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent event) {
				rollOverMouseEnter(label);
			}

			@Override
			public void mouseExited(MouseEvent event) {
				rollOverMouseExit(label, event);
			}
		});
		add(label);
	}

    //handle rollover events

	private void rollOverMouseEnter(JLabel label) {

		String text = label.getText();
		if (text.contains(HB_ROLLOVER)) {
			roShowHBDCClusters = true;
		}
		else if (text.contains(TB_ROLLOVER)) {
			roShowTBDCClusters = true;
		}
		else if (text.contains(AIHB_ROLLOVER)) {
			roShowAIHBDCClusters = true;
		}
		else if (text.contains(AITB_ROLLOVER)) {
			roShowAITBDCClusters = true;
		}

		label.setForeground(activeFG);
		label.setBackground(activeBG);

		view.refresh();
	}

	private void rollOverMouseExit(JLabel label, MouseEvent e) {

		if (e.isAltDown() || e.isControlDown() || e.isMetaDown()) {
			return;
		}

		String text = label.getText();
		if (text.contains(HB_ROLLOVER)) {
			roShowHBDCClusters = false;
		}
		else if (text.contains(TB_ROLLOVER)) {
			roShowTBDCClusters = false;
		}
		else if (text.contains(AIHB_ROLLOVER)) {
			roShowAIHBDCClusters = false;
		}
		else if (text.contains(AITB_ROLLOVER)) {
			roShowAITBDCClusters = false;
		}

		label.setForeground(inactiveFG);
		label.setBackground(inactiveBG);

		view.refresh();
	}


}
