package cnuphys.ced.cedview;

import java.awt.Color;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;

import cnuphys.bCNU.component.IRollOverListener;
import cnuphys.bCNU.component.RollOverPanel;
import edu.cnu.mdi.ui.fonts.Fonts;

public class RollOverDCPanel extends RollOverPanel implements IRollOverListener {


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
		super(title, numCols, Fonts.mediumFont, inactiveFG, inactiveBG, roLabels);
		this.view = view;
		addRollOverListener(this);
	}


    //handle rollover events


	@Override
	public void RollOverMouseEnter(JLabel label, MouseEvent e) {

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

	@Override
	public void RollOverMouseExit(JLabel label, MouseEvent e) {

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
