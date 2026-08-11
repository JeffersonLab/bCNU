package cnuphys.ced.ced3d;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.JPanel;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.awt.GLJPanel;

import bCNU3D.Panel3D;
import cnuphys.bCNU.component.checkboxarray.CheckBoxArray;
import cnuphys.bCNU.dialog.VerticalFlowLayout;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.util.UnicodeSupport;
import cnuphys.bCNU.view.VirtualView;
import cnuphys.ced.ced3d.view.PlainView3D;

public abstract class PlainPanel3D extends Panel3D {

	// cm
	private final float zmax = 600f;
	private final float zmin = -100f;

	// labels for the check box
	public static final String SHOW_VOLUMES = "Volumes";
	public static final String SHOW_TRUTH = "Truth";
	public static final String SHOW_DC = "DC";
	public static final String SHOW_ECAL = "ECAL";
	public static final String SHOW_PCAL = "PCAL";
	public static final String SHOW_FTOF = "FTOF";
//	public static final String SHOW_SIM_SDOCA = "SDOCA";
//	public static final String SHOW_TB_DOCA = "TB DOCA";
	public static final String SHOW_RECON_FTOF = "Rec-FTOF";
	public static final String SHOW_RECON_CROSSES = "Crosses";
	public static final String SHOW_BST = "BST";
	public static final String SHOW_BST_LAYER_1 = "BST Layer 1";
	public static final String SHOW_BST_LAYER_2 = "BST Layer 2";
	public static final String SHOW_BST_LAYER_3 = "BST Layer 3";
	public static final String SHOW_BST_LAYER_4 = "BST Layer 4";
	public static final String SHOW_BST_LAYER_5 = "BST Layer 5";
	public static final String SHOW_BST_LAYER_6 = "BST Layer 6";
	public static final String SHOW_BST_LAYER_7 = "BST Layer 7";
	public static final String SHOW_BST_LAYER_8 = "BST Layer 8";
	public static final String SHOW_BST_HITS = "BST Hits";

	public static final String SHOW_BMT = "BMT";
	public static final String SHOW_BMT_LAYER_1 = "BMT Layer 1";
	public static final String SHOW_BMT_LAYER_2 = "BMT Layer 2";
	public static final String SHOW_BMT_LAYER_3 = "BMT Layer 3";
	public static final String SHOW_BMT_LAYER_4 = "BMT Layer 4";
	public static final String SHOW_BMT_LAYER_5 = "BMT Layer 5";
	public static final String SHOW_BMT_LAYER_6 = "BMT Layer 6";
	public static final String SHOW_BMT_HITS = "BMT Hits";

	public static final String SHOW_SECTOR_1 = "Sector 1";
	public static final String SHOW_SECTOR_2 = "Sector 2";
	public static final String SHOW_SECTOR_3 = "Sector 3";
	public static final String SHOW_SECTOR_4 = "Sector 4";
	public static final String SHOW_SECTOR_5 = "Sector 5";
	public static final String SHOW_SECTOR_6 = "Sector 6";

	public static final String SHOW_CTOF = "CTOF";

	public static final String SHOW_CND = "CND";
	public static final String SHOW_CND_LAYER_1 = "CND Layer 1";
	public static final String SHOW_CND_LAYER_2 = "CND Layer 2";
	public static final String SHOW_CND_LAYER_3 = "CND Layer 3";
	public static final String SHOW_CND_HITS = "CND Hits";

	public static final String SHOW_TB_CROSS = "TB Cross";
	public static final String SHOW_HB_CROSS = "HB Cross";

	public static final String SHOW_AITB_CROSS = "AITB Cross";
	public static final String SHOW_AIHB_CROSS = "AIHB Cross";

	public static final String SHOW_TB_TRACK = "Reg TB Track";
	public static final String SHOW_HB_TRACK = "Reg HB Track";

	public static final String SHOW_AITB_TRACK = "AI TB Track";
	public static final String SHOW_AIHB_TRACK = "AI HB Track";

	public static final String SHOW_CVTREC_TRACK = "CVT Rec Track";
	public static final String SHOW_CVTP1_TRACK = "CVT P1 Track";

	public static final String SHOW_REC_TRACK = "REC Track";

	public static final String SHOW_MAP_EXTENTS = "Map Extents";

	public static final String SHOW_COSMIC = "Cosmics";

	public static final String SHOW_REC_CAL = "REC Cal";

	// for ALert TOF
	public static final String TOF_LAY1 = "TOF Lay 1";
	public static final String TOF_LAY2 = "TOF Layer 2";
	public static final String TOF_LAY3 = "TOF Layer 3";
	public static final String TOF_LAY4 = "TOF Layer 4";

	public static final String TOF_SUPLAY1 = "TOF Superlayer 1";
	public static final String TOF_SUPLAY2 = "TOF Superlayer2 ";

	public static final String SHOW_SECT1 = "TOF Sector 1";
	public static final String SHOW_SECT2 = "TOF Sector 2";
	public static final String SHOW_SECT3 = "TOF Sector 3";
	public static final String SHOW_SECT4 = "TOF Sector 4";
	public static final String SHOW_SECT5 = "TOF Sector 5";
	public static final String SHOW_SECT6 = "TOF Sector 6";
	public static final String SHOW_SECT7 = "TOF Sector 7";
	public static final String SHOW_SECT8 = "TOF Sector 8";
	public static final String SHOW_SECT9 = "TOF Sector 9";
	public static final String SHOW_SECT10 = "TOF Sector 10";
	public static final String SHOW_SECT11 = "TOF Sector 11";
	public static final String SHOW_SECT12 = "TOF Sector 12";
	public static final String SHOW_SECT13 = "TOF Sector 13";
	public static final String SHOW_SECT14 = "TOF Sector 14";
    public static final String SHOW_SECT15 = "TOF Sector 15";


	public static final String SHOW_TOF = "TOF";

	// for Alert DC
	public static final String DC_SUPLAY1_LAY1 = "DC_Suplay1_Lay1";
//	public static final String DC_SUPLAY1_LAY2 = "DC_Suplay1_Lay2";
	public static final String DC_SUPLAY2_LAY1 = "DC_Suplay2_Lay1";
	public static final String DC_SUPLAY2_LAY2 = "DC_Suplay2_Lay2";
	public static final String DC_SUPLAY3_LAY1 = "DC_Suplay3_Lay1";
	public static final String DC_SUPLAY3_LAY2 = "DC_Suplay3_Lay2";
	public static final String DC_SUPLAY4_LAY1 = "DC_Suplay4_Lay1";
	public static final String DC_SUPLAY4_LAY2 = "DC_Suplay4_Lay2";
	public static final String DC_SUPLAY5_LAY1 = "DC_Suplay5_Lay1";
//	public static final String DC_SUPLAY5_LAY2 = "DC_Suplay5_Lay2";

	public static final String SHOW_FMT_LAYER_1 = "FMT Layer 1";
	public static final String SHOW_FMT_LAYER_2 = "FMT Layer 2";
	public static final String SHOW_FMT_LAYER_3 = "FMT Layer 3";
	public static final String SHOW_FMT_LAYER_4 = "FMT Layer 4";
	public static final String SHOW_FMT_LAYER_5 = "FMT Layer 5";
	public static final String SHOW_FMT_LAYER_6 = "FMT Layer 6";

	public static final String SHOW_FMT_REGION_1 = "FMT Region 1";
	public static final String SHOW_FMT_REGION_2 = "FMT Region 2";
	public static final String SHOW_FMT_REGION_3 = "FMT Region 3";
	public static final String SHOW_FMT_REGION_4 = "FMT Region 4";

	public static final String SHOW_URWT_LAYER_1 = UnicodeSupport.SMALL_MU +  "RWT Layer 1";
	public static final String SHOW_URWT_LAYER_2 = UnicodeSupport.SMALL_MU +  "RWT Layer 2";
	public static final String SHOW_URWT_LAYER_3 = UnicodeSupport.SMALL_MU +  "RWT Layer 3";
	public static final String SHOW_URWT_LAYER_4 = UnicodeSupport.SMALL_MU +  "RWT Layer 4";

	// Check box array
	protected CheckBoxArray _checkBoxArray;

	// alpha slider for volume alphas
	protected AlphaSlider _volumeAlphaSlider;

	// display array labels
	protected String _cbaLabels[];

	protected PlainView3D _view;

	protected JPanel _eastPanel;

	/*
	 * The panel that holds the 3D objects
	 *
	 * @param angleX the initial x rotation angle in degrees
	 *
	 * @param angleY the initial y rotation angle in degrees
	 *
	 * @param angleZ the initial z rotation angle in degrees
	 *
	 * @param xdist move viewpoint left/right
	 *
	 * @param ydist move viewpoint up/down
	 *
	 * @param zdist the initial viewer z distance should be negative
	 */
	public PlainPanel3D(PlainView3D view, float angleX, float angleY, float angleZ, float xDist, float yDist,
			float zDist, float bgRed, float bgGreen, float bgBlue, String... cbaLabels) {

		super(angleX, angleY, angleZ, xDist, yDist, zDist, bgRed, bgGreen, bgBlue, true);

		_view = view;
		_cbaLabels = cbaLabels;
		_volumeAlphaSlider = new AlphaSlider(this, "Volume alpha");

		gljpanel.setBorder(new CommonBorder());
		final GLJPanel gljp = gljpanel;

		addEast();
		addNorth();
		addWest();
		addSouth();

		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				refresh();
				gljp.requestFocus();
			}

		};
		for (String s : _cbaLabels) {
			AbstractButton ab = _checkBoxArray.getButton(s);
			ab.setFont(Fonts.smallFont);
			ab.setSelected(!SHOW_MAP_EXTENTS.equals(s));
			ab.addActionListener(al);
		}
		fixSize();
	}


	@Override
	public void display(GLAutoDrawable drawable) {
		if (VirtualView.getInstance().isViewVisible(_view)) {
			super.display(drawable);
		} 
	}

	// add north panel
	protected abstract void addNorth();

	// add west panel
	protected abstract void addWest();

	// add south panel
	protected abstract void addSouth();

	// add eastern panel
	private void addEast() {
		_eastPanel = new JPanel();
		_eastPanel.setLayout(new VerticalFlowLayout());

		_eastPanel.add(new KeyboardLegend(this));
		_checkBoxArray = new CheckBoxArray(3, 4, 4, _cbaLabels);

		AbstractButton ab = _checkBoxArray.getButton(SHOW_MAP_EXTENTS);
		if (ab != null) {
			ab.setSelected(false);
		}

		_checkBoxArray.setBorder(new CommonBorder());
		_eastPanel.add(_checkBoxArray);

		add(_eastPanel, BorderLayout.EAST);
	}

	/**
	 * Get the east panel for adding custom controls
	 *
	 * @return the east panel for adding custom controls
	 */
	public JPanel getEastPanel() {
		return _eastPanel;
	}

	/**
	 * Snapshot of the panel.
	 */
	public void snapshot() {
		GraphicsUtilities.saveAsPng(this);
	}

	// a fixed fraction of the screen
	private void fixSize() {

		double size[] = _view.getSizeFromScreenFraction(0.7);

		Dimension d = new Dimension((int) size[1], (int) size[1]);
		gljpanel.setPreferredSize(d);
	}

	/**
	 * Get the alpha for volume drawing
	 *
	 * @return the alpha for volume drawing
	 */
	public int getVolumeAlpha() {
		return _volumeAlphaSlider.getAlpha();
	}

	/**
	 * Get one of the display buttons
	 *
	 * @param label the button label
	 * @return the button or null on failure
	 */
	public AbstractButton getDisplayButton(String label) {
		if (_checkBoxArray == null) {
			return null;
		}
		return _checkBoxArray.getButton(label);
	}

	/**
	 * Get all the display buttons in an array
	 *
	 * @return all the display buttons
	 */
	public AbstractButton[] gatAllDisplayButtons() {
		if ((_checkBoxArray == null) || (_cbaLabels == null) || (_cbaLabels.length < 1)) {
			return null;
		}

		AbstractButton buttons[] = new AbstractButton[_cbaLabels.length];

		for (int i = 0; i < _cbaLabels.length; i++) {
			buttons[i] = _checkBoxArray.getButton(_cbaLabels[i]);
		}

		return buttons;
	}

	/**
	 * Check if a feature should be drawn
	 *
	 * @param label the label for the check box on the option array
	 * @return <code>true</code> if the feature should be drawn
	 */
	private boolean show(String label) {
		AbstractButton ab = _checkBoxArray.getButton(label);
		return (ab == null) ? false : ab.isSelected();
	}

	public void enableLabel(String label, boolean enabled) {
		AbstractButton ab = _checkBoxArray.getButton(label);
		if (ab != null) {
			ab.setEnabled(enabled);
		}
	}

	/**
	 * Show ECAL?
	 *
	 * @return <code>true</code> if we are to show ECAL
	 */
	public boolean showECAL() {
		return show(PlainPanel3D.SHOW_ECAL);
	}

	/**
	 * Show PCAL?
	 *
	 * @return <code>true</code> if we are to show PCAL
	 */
	public boolean showPCAL() {
		return show(PlainPanel3D.SHOW_PCAL);
	}

	/**
	 * Show forward TOF?
	 *
	 * @return <code>true</code> if we are to show FTOF
	 */
	public boolean showFTOF() {
		return show(PlainPanel3D.SHOW_FTOF);
	}

	/**
	 * Show CTOF?
	 *
	 * @return <code>true</code> if we are to show CTOF
	 */
	public boolean showCTOF() {
		return show(PlainPanel3D.SHOW_CTOF);
	}

	/**
	 * Show BST?
	 *
	 * @return <code>true</code> if we are to show BST
	 */
	public boolean showBST() {
		return show(PlainPanel3D.SHOW_BST);
	}

	/**
	 * Show BMT?
	 *
	 * @return <code>true</code> if we are to show BMT
	 */
	public boolean showBMT() {
		return show(PlainPanel3D.SHOW_BMT);
	}

	/**
	 * Show CND?
	 *
	 * @return <code>true</code> if we are to show CND
	 */
	public boolean showCND() {
		return show(PlainPanel3D.SHOW_CND);
	}

	/**
	 * Show CND Layer 1?
	 *
	 * @return <code>true</code> if we are to show CND Layer 1
	 */
	public boolean showCNDLayer1() {
		return showCND() && show(PlainPanel3D.SHOW_CND_LAYER_1);
	}

	/**
	 * Show CND Layer 2?
	 *
	 * @return <code>true</code> if we are to show CND Layer 2
	 */
	public boolean showCNDLayer2() {
		return showCND() && show(PlainPanel3D.SHOW_CND_LAYER_2);
	}

	/**
	 * Show CND Layer 3?
	 *
	 * @return <code>true</code> if we are to show CND Layer 3
	 */
	public boolean showCNDLayer3() {
		return showCND() && show(PlainPanel3D.SHOW_CND_LAYER_3);
	}

	/**
	 * Show CND hits?
	 *
	 * @return {@code true} if CND hits should be drawn
	 */
	public boolean showCNDHits() {
		return showCND() && show(PlainPanel3D.SHOW_CND_HITS);
	}

	/**
	 * Show reconstructed Crosses?
	 *
	 * @return <code>true</code> if we are to show reconstructed crosses
	 */
	public boolean showReconCrosses() {
		return show(PlainPanel3D.SHOW_RECON_CROSSES);
	}

	/**
	 * Show reconstructed FTOF?
	 *
	 * @return <code>true</code> if we are to show reconstructed ftof
	 */
	public boolean showReconFTOF() {
		return show(PlainPanel3D.SHOW_RECON_FTOF);
	}

	/**
	 * Show time based track?
	 *
	 * @return <code>true</code> if we are to show time based track
	 */
	public boolean showTBTrack() {
		return show(PlainPanel3D.SHOW_TB_TRACK);
	}

	/**
	 * Show hit based track?
	 *
	 * @return <code>true</code> if we are to show hit based track
	 */
	public boolean showHBTrack() {
		return show(PlainPanel3D.SHOW_HB_TRACK);
	}

	/**
	 * Show AI time based track?
	 *
	 * @return <code>true</code> if we are to show time based tracks
	 */
	public boolean showAITBTrack() {
		return show(PlainPanel3D.SHOW_AITB_TRACK);
	}

	/**
	 * Show AI hit based track?
	 *
	 * @return <code>true</code> if we are to show hit based tracks
	 */
	public boolean showAIHBTrack() {
		return show(PlainPanel3D.SHOW_AIHB_TRACK);
	}

	/**
	 * Show REC::Particle tracks?
	 *
	 * @return <code>true</code> if we are to show recon tracks
	 */
	public boolean showRecTrack() {
		return show(PlainPanel3D.SHOW_REC_TRACK);
	}

	/**
	 * Show REC::Calorimiter data?
	 *
	 * @return <code>true</code> if we are to REC::Calorimeter data
	 */
	public boolean showRecCal() {
		return show(PlainPanel3D.SHOW_REC_CAL);
	}

	/**
	 * Show field map extents
	 *
	 * @return <code>true</code> if we are to show torus and solenoid extent
	 */
	public boolean showMapExtents() {
		return show(PlainPanel3D.SHOW_MAP_EXTENTS);
	}

	/**
	 * Show cvt rec based track?
	 *
	 * @return <code>true</code> if we are to show cvt based track
	 */
	public boolean showCVTRecTrack() {
		return show(PlainPanel3D.SHOW_CVTREC_TRACK);
	}

	/**
	 * Show cvt pass 1based track?
	 *
	 * @return <code>true</code> if we are to show cvt based track
	 */
	public boolean showCVTP1Track() {
		return show(PlainPanel3D.SHOW_CVTP1_TRACK);
	}

	/**
	 * Show hit based cross?
	 *
	 * @return <code>true</code> if we are to show hit based cross
	 */
	public boolean showHBCross() {
		return show(PlainPanel3D.SHOW_HB_CROSS);
	}

	/**
	 * Show time based cross?
	 *
	 * @return <code>true</code> if we are to show time based cross
	 */
	public boolean showTBCross() {
		return show(PlainPanel3D.SHOW_TB_CROSS);
	}

	/**
	 * Show AI hit based cross?
	 *
	 * @return <code>true</code> if we are to show AI hit based cross
	 */
	public boolean showAIHBCross() {
		return show(PlainPanel3D.SHOW_AIHB_CROSS);
	}

	/**
	 * Show AI time based cross?
	 *
	 * @return <code>true</code> if we are to show AI time based cross
	 */
	public boolean showAITBCross() {
		return show(PlainPanel3D.SHOW_AITB_CROSS);
	}

	/**
	 * Show BST Hits?
	 *
	 * @return <code>true</code> if we are to show BST Hits
	 */
	public boolean showBSTHits() {
		return show(PlainPanel3D.SHOW_BST_HITS);
	}

	/**
	 * Show BMT Hits?
	 *
	 * @return <code>true</code> if we are to show BMT Hits
	 */
	public boolean showBMTHits() {
		return show(PlainPanel3D.SHOW_BMT_HITS);
	}

	/**
	 * Show DC?
	 *
	 * @return <code>true</code> if we are to show DC
	 */
	public boolean showDC() {
		return show(PlainPanel3D.SHOW_DC);
	}

	/**
	 * Show BST Layer 1?
	 *
	 * @return <code>true</code> if we are to show BST Layer 1
	 */
	public boolean showBSTLayer1() {
		return showBST() && show(PlainPanel3D.SHOW_BST_LAYER_1);
	}

	/**
	 * Show BST Layer 2?
	 *
	 * @return <code>true</code> if we are to show BST Layer 2
	 */
	public boolean showBSTLayer2() {
		return showBST() && show(PlainPanel3D.SHOW_BST_LAYER_2);
	}

	/**
	 * Show BST Layer 3?
	 *
	 * @return <code>true</code> if we are to show BST Layer 3
	 */
	public boolean showBSTLayer3() {
		return showBST() && show(PlainPanel3D.SHOW_BST_LAYER_3);
	}

	/**
	 * Show BST Layer 4?
	 *
	 * @return <code>true</code> if we are to show BST Layer 4
	 */
	public boolean showBSTLayer4() {
		return showBST() && show(PlainPanel3D.SHOW_BST_LAYER_4);
	}

	/**
	 * Show BST Layer 5?
	 *
	 * @return <code>true</code> if we are to show BST Layer 5
	 */
	public boolean showBSTLayer5() {
		return showBST() && show(PlainPanel3D.SHOW_BST_LAYER_5);
	}

	/**
	 * Show BST Layer 6?
	 *
	 * @return <code>true</code> if we are to show BST Layer 6
	 */
	public boolean showBSTLayer6() {
		return showBST() && show(PlainPanel3D.SHOW_BST_LAYER_6);
	}

	/**
	 * Show BMT Layer 1?
	 *
	 * @return <code>true</code> if we are to show BMT Layer 1
	 */
	public boolean showBMTLayer1() {
		return showBMT() && show(PlainPanel3D.SHOW_BMT_LAYER_1);
	}

	/**
	 * Show BMT Layer 2?
	 *
	 * @return <code>true</code> if we are to show BMT Layer 2
	 */
	public boolean showBMTLayer2() {
		return showBMT() && show(PlainPanel3D.SHOW_BMT_LAYER_2);
	}

	/**
	 * Show BMT Layer 3?
	 *
	 * @return <code>true</code> if we are to show BMT Layer 3
	 */
	public boolean showBMTLayer3() {
		return showBMT() && show(PlainPanel3D.SHOW_BMT_LAYER_3);
	}

	/**
	 * Show BMT Layer 4?
	 *
	 * @return <code>true</code> if we are to show BMT Layer 4
	 */
	public boolean showBMTLayer4() {
		return showBMT() && show(PlainPanel3D.SHOW_BMT_LAYER_4);
	}

	/**
	 * Show BMT Layer 5?
	 *
	 * @return <code>true</code> if we are to show BMT Layer 5
	 */
	public boolean showBMTLayer5() {
		return showBMT() && show(PlainPanel3D.SHOW_BMT_LAYER_5);
	}

	/**
	 * Show BMT Layer 6?
	 *
	 * @return <code>true</code> if we are to show BMT Layer 6
	 */
	public boolean showBMTLayer6() {
		return showBMT() && show(PlainPanel3D.SHOW_BMT_LAYER_6);
	}

	/**
	 * Show sector 1?
	 *
	 * @return <code>true</code> if we are to show sector 1
	 */
	public boolean showSector1() {
		return show(PlainPanel3D.SHOW_SECTOR_1);
	}

	/**
	 * Show sector 2?
	 *
	 * @return <code>true</code> if we are to show sector 2
	 */
	public boolean showSector2() {
		return show(PlainPanel3D.SHOW_SECTOR_2);
	}

	/**
	 * Show sector 3?
	 *
	 * @return <code>true</code> if we are to show sector 3
	 */
	public boolean showSector3() {
		return show(PlainPanel3D.SHOW_SECTOR_3);
	}

	/**
	 * Show sector 4?
	 *
	 * @return <code>true</code> if we are to show sector 4
	 */
	public boolean showSector4() {
		return show(PlainPanel3D.SHOW_SECTOR_4);
	}

	/**
	 * Show sector 5?
	 *
	 * @return <code>true</code> if we are to show sector 5
	 */
	public boolean showSector5() {
		return show(PlainPanel3D.SHOW_SECTOR_5);
	}

	/**
	 * Show sector 6?
	 *
	 * @return <code>true</code> if we are to show sector 6
	 */
	public boolean showSector6() {
		return show(PlainPanel3D.SHOW_SECTOR_6);
	}

	/**
	 * Show Cosmics?
	 *
	 * @return <code>true</code> if we are to show Cosmics
	 */
	public boolean showCosmics() {
		return show(PlainPanel3D.SHOW_COSMIC);
	}

	/**
	 * Show we show the 1-based sector?
	 *
	 * @param sector the sector [1..6]
	 * @return <code>true</code> if we are to show the sector
	 */
	public boolean showSector(int sector) {
		switch (sector) {
		case 1:
			return showSector1();
		case 2:
			return showSector2();
		case 3:
			return showSector3();
		case 4:
			return showSector4();
		case 5:
			return showSector5();
		case 6:
			return showSector6();
		}
		return false;
	}

	/**
	 * Show FMT Region 1?
	 *
	 * @return <code>true</code> if we are to show FMT Region 1
	 */
	public boolean showFMTRegion1() {
		return show(PlainPanel3D.SHOW_FMT_REGION_1);
	}

	/**
	 * Show FMT Region 2?
	 *
	 * @return <code>true</code> if we are to show FMT Region 2
	 */
	public boolean showFMTRegion2() {
		return show(PlainPanel3D.SHOW_FMT_REGION_2);
	}

	/**
	 * Show FMT Region 3?
	 *
	 * @return <code>true</code> if we are to show FMT Region 3
	 */
	public boolean showFMTRegion3() {
		return show(PlainPanel3D.SHOW_FMT_REGION_3);
	}

	/**
	 * Show FMT Region 4?
	 *
	 * @return <code>true</code> if we are to show FMT Region 4
	 */
	public boolean showFMTRegion4() {
		return show(PlainPanel3D.SHOW_FMT_REGION_4);
	}

	/**
	 * Show FMT Layer 1?
	 *
	 * @return <code>true</code> if we are to show FMT Layer 1
	 */
	public boolean showFMTLayer1() {
		return show(PlainPanel3D.SHOW_FMT_LAYER_1);
	}

	/**
	 * Show FMT Layer 2?
	 *
	 * @return <code>true</code> if we are to show FMT Layer 2
	 */
	public boolean showFMTLayer2() {
		return show(PlainPanel3D.SHOW_FMT_LAYER_2);
	}

	/**
	 * Show FMT Layer 3?
	 *
	 * @return <code>true</code> if we are to show FMT Layer 3
	 */
	public boolean showFMTLayer3() {
		return show(PlainPanel3D.SHOW_FMT_LAYER_3);
	}

	/**
	 * Show FMT Layer 4?
	 *
	 * @return <code>true</code> if we are to show FMT Layer 4
	 */
	public boolean showFMTLayer4() {
		return show(PlainPanel3D.SHOW_FMT_LAYER_4);
	}

	/**
	 * Show FMT Layer 5?
	 *
	 * @return <code>true</code> if we are to show FMT Layer 5
	 */
	public boolean showFMTLayer5() {
		return show(PlainPanel3D.SHOW_FMT_LAYER_5);
	}

	/**
	 * Show FMT Layer 6?
	 *
	 * @return <code>true</code> if we are to show FMT Layer 6
	 */
	public boolean showFMTLayer6() {
		return show(PlainPanel3D.SHOW_FMT_LAYER_6);
	}

	/**
	 * Show FMT Layer?
	 *
	 * @param layer the 1-based layer to show
	 * @return <code>true</code> if we are to show the layer
	 */
	public boolean showFMTLayer(int layer) {
		switch (layer) {
		case 1:
			return showFMTLayer1();
		case 2:
			return showFMTLayer2();
		case 3:
			return showFMTLayer3();
		case 4:
			return showFMTLayer4();
		case 5:
			return showFMTLayer5();
		case 6:
			return showFMTLayer6();
		}
		return false;
	}

	/**
	 * Show FMT Region?
	 *
	 * @param region the 1-based region to show
	 * @return <code>true</code> if we are to show the region
	 */
	public boolean showFMTRegion(int region) {
		switch (region) {
		case 1:
			return showFMTRegion1();
		case 2:
			return showFMTRegion2();
		case 3:
			return showFMTRegion3();
		case 4:
			return showFMTRegion4();
		}
		return false;
	}

	public boolean showSector_1() {
		return show(PlainPanel3D.SHOW_SECT1);
	}

	public boolean showSector_2() {
		return show(PlainPanel3D.SHOW_SECT2);
	}

	public boolean showSector_3() {
		return show(PlainPanel3D.SHOW_SECT3);
	}


	public boolean showSector_4() {
		return show(PlainPanel3D.SHOW_SECT4);
	}

	public boolean showSector_5() {
		return show(PlainPanel3D.SHOW_SECT5);
	}


	public boolean showSector_6() {
		return show(PlainPanel3D.SHOW_SECT6);
	}

	public boolean showSector_7() {
		return show(PlainPanel3D.SHOW_SECT7);
	}

	public boolean showSector_8() {
		return show(PlainPanel3D.SHOW_SECT8);
	}

	public boolean showSector_9() {
		return show(PlainPanel3D.SHOW_SECT9);
	}

	public boolean showSector_10() {
		return show(PlainPanel3D.SHOW_SECT10);
	}

	public boolean showSector_11() {
		return show(PlainPanel3D.SHOW_SECT11);
	}

	public boolean showSector_12() {
		return show(PlainPanel3D.SHOW_SECT12);
	}

	public boolean showSector_13() {
		return show(PlainPanel3D.SHOW_SECT13);
	}

	public boolean showSector_14() {
		return show(PlainPanel3D.SHOW_SECT14);
	}

	public boolean showSector_15() {
		return show(PlainPanel3D.SHOW_SECT15);
	}

	/**
	 * Show ALERT TOF superlayer 1?
	 *
	 * @return <code>true</code> if we are to show ALERT TOF superlayer 1
	 */
	public boolean showTOF_SUPLAY1() {
		return show(PlainPanel3D.TOF_SUPLAY1);
	}


	/**
	 * Show ALERT TOF superlayer 2?
	 *
	 * @return <code>true</code> if we are to show ALERT TOF superlayer 2
	 */
	public boolean showTOF_SUPLLAY2() {
		return show(PlainPanel3D.TOF_SUPLAY2);
	}


	/**
	 * Show ALERT TOF Layer 1?
	 *
	 * @return <code>true</code> if we are to show ALERT TOF Layer 1
	 */
	public boolean showTOF_LAY1() {
		return show(PlainPanel3D.TOF_LAY1);
	}


	/**
	 * Show ALERT TOF Layer 2?
	 *
	 * @return <code>true</code> if we are to show ALERT TOF Layer 2
	 */
	public boolean showTOF_LAY2() {
		return show(PlainPanel3D.TOF_LAY2);
	}

	/**
	 * Show ALERT TOF Layer 3?
	 *
	 * @return <code>true</code> if we are to show ALERT TOF Layer 3
	 */
	public boolean showTOF_LAY3() {
		return show(PlainPanel3D.TOF_LAY3);
	}

	/**
	 * Show ALERT TOF Layer 4?
	 *
	 * @return <code>true</code> if we are to show ALERT TOF Layer 4
	 */
	public boolean showTOF_LAY4() {
		return show(PlainPanel3D.TOF_LAY4);
	}


	/**
	 * Show TOF?
	 *
	 * @return <code>true</code> if we are to show TOF
	 */
	public boolean showTOF() {
		return show(PlainPanel3D.SHOW_TOF);
	}


	// Generalized method to call appropriate show methods
	/**
	 * Show TOF for the given superlayer and layer.
	 *
	 * @param superlayer 1-based superlayer number
	 * @param layer      1-based layer number
	 * @return <code>true</code> if the appropriate layer is shown
	 */
	public boolean showTOF(int sector, int superlayer, int layer) {

		if (!showTOF()) {
			return false;
		}

		boolean showSector = false;
		boolean showSuperLayer = false;
		boolean showLayer = false;

		switch (sector) {
		case 1:
			showSector = showSector_1();
			break;
		case 2:
			showSector = showSector_2();
			break;
		case 3:
			showSector = showSector_3();
			break;
		case 4:
			showSector = showSector_4();
			break;
		case 5:
			showSector = showSector_5();
			break;
		case 6:
			showSector = showSector_6();
			break;
		case 7:
			showSector = showSector_7();
			break;
		case 8:
			showSector = showSector_8();
			break;
		case 9:
			showSector = showSector_9();
			break;
		case 10:
			showSector = showSector_10();
			break;
		case 11:
			showSector = showSector_11();
			break;
		case 12:
			showSector = showSector_12();
			break;
		case 13:
			showSector = showSector_13();
			break;
		case 14:
			showSector = showSector_14();
			break;
		case 15:
			showSector = showSector_15();
			break;
		default:
			throw new IllegalArgumentException("in ShowTOF Invalid  superlayer: " + superlayer);

		}

		switch (superlayer) {
		case 1:
			showSuperLayer = showTOF_SUPLAY1();
			break;
		case 2:
			showSuperLayer = showTOF_SUPLLAY2();
			break;
		default:
			throw new IllegalArgumentException("in ShowTOF Invalid  superlayer: " + superlayer);
		}

		switch (layer) {
		case 1:
			showLayer = showTOF_LAY1();
			break;
		case 2:
			showLayer = showTOF_LAY2();
			break;
		case 3:
			showLayer = showTOF_LAY3();
			break;
		case 4:
			showLayer = showTOF_LAY4();
			break;
		default:
			throw new IllegalArgumentException("in ShowTOF Invalid  layer: " + layer);
		}


		return showSector && showSuperLayer && showLayer;

	}

	/**
	 * Show DC_Suplay1_Lay1?
	 *
	 * @return <code>true</code> if we are to show DC_Suplay2_Lay1
	 */
	public boolean showDC_SUPLAY1_LAY11() {
		return show(PlainPanel3D.DC_SUPLAY1_LAY1);
	}

	/**
	 * Show DC_Suplay2_Lay1?
	 *
	 * @return <code>true</code> if we are to show DC_Suplay2_Lay1
	 */
	public boolean showDC_SUPLAY2_LAY1() {
		return show(PlainPanel3D.DC_SUPLAY2_LAY1);
	}

	/**
	 * Show DC_Suplay2_Lay2?
	 *
	 * @return <code>true</code> if we are to show DC_Suplay2_Lay2
	 */
	public boolean showDC_SUPLAY2_LAY2() {
		return show(PlainPanel3D.DC_SUPLAY2_LAY2);
	}

	/**
	 * Show DC_Suplay3_Lay1?
	 *
	 * @return <code>true</code> if we are to show DC_Suplay3_Lay1
	 */
	public boolean showDC_SUPLAY3_LAY1() {
		return show(PlainPanel3D.DC_SUPLAY3_LAY1);
	}

	/**
	 * Show DC_Suplay3_Lay2?
	 *
	 * @return <code>true</code> if we are to show DC_Suplay3_Lay2
	 */
	public boolean showDC_SUPLAY3_LAY2() {
		return show(PlainPanel3D.DC_SUPLAY3_LAY2);
	}

	/**
	 * Show DC_Suplay4_Lay1?
	 *
	 * @return <code>true</code> if we are to show DC_Suplay4_Lay1
	 */
	public boolean showDC_SUPLAY4_LAY1() {
		return show(PlainPanel3D.DC_SUPLAY4_LAY1);
	}

	/**
	 * Show DC_Suplay4_Lay2?
	 *
	 * @return <code>true</code> if we are to show DC_Suplay4_Lay2
	 */
	public boolean showDC_SUPLAY4_LAY2() {
		return show(PlainPanel3D.DC_SUPLAY4_LAY2);
	}

	/**
	 * Show DC_Suplay5_Lay1?
	 *
	 * @return <code>true</code> if we are to show DC_Suplay5_Lay1
	 */
	public boolean showDC_SUPLAY5_LAY1() {
		return show(PlainPanel3D.DC_SUPLAY5_LAY1);
	}


	// Generalized method to call appropriate show methods
	/**
	 * Show DC for the given superlayer and layer.
	 *
	 * @param superlayer Superlayer number
	 * @param layer      Layer number
	 * @return <code>true</code> if the appropriate layer is shown
	 */
	public boolean showDC(int superlayer, int layer) {

		if (!showDC()) {
			return false;
		}

		switch (superlayer) {
		case 1:
			if (layer == 1) {
				return showDC_SUPLAY1_LAY11();
			}
			break;
		case 2:
			switch (layer) {
			case 1:
				return showDC_SUPLAY2_LAY1();
			case 2:
				return showDC_SUPLAY2_LAY2();
			}
			break;
		case 3:
			switch (layer) {
			case 1:
				return showDC_SUPLAY3_LAY1();
			case 2:
				return showDC_SUPLAY3_LAY2();
			}
			break;
		case 4:
			switch (layer) {
			case 1:
				return showDC_SUPLAY4_LAY1();
			case 2:
				return showDC_SUPLAY4_LAY2();
			}
			break;
		case 5:
			switch (layer) {
			case 1:
				return showDC_SUPLAY5_LAY1();
			}
			break;
		default:
			throw new IllegalArgumentException("Invalid superlayer or layer.");
		}
		return false;
	}
	
	/**
	 * Show URWT Layer 1?
	 *
	 * @return <code>true</code> if we are to show URWT Layer 1
	 */
	public boolean showUrwtLayer1() {
		return show(PlainPanel3D.SHOW_URWT_LAYER_1);
	}
	
	/**
	 * Show URWT Layer 2?
	 *
	 * @return <code>true</code> if we are to show URWT Layer 2
	 */
	public boolean showUrwtLayer2() {
		return show(PlainPanel3D.SHOW_URWT_LAYER_2);
	}
	
	/**
	 * Show URWT Layer 3?
	 *
	 * @return <code>true</code> if we are to show URWT Layer 3
	 */
	public boolean showUrwtLayer3() {
		return show(PlainPanel3D.SHOW_URWT_LAYER_3);
	}

	/**
	 * Show URWT Layer 4?
	 *
	 * @return <code>true</code> if we are to show URWT Layer 3
	 */
	public boolean showUrwtLayer4() {
		return show(PlainPanel3D.SHOW_URWT_LAYER_4);
	}
	
	
	/**
	 * Show MC truth?
	 *
	 * @return <code>true</code> if we are to show simulation truth
	 */
	public boolean showMCTruth() {
		return show(PlainPanel3D.SHOW_TRUTH);
	}

	/**
	 * Show Volumes?
	 *
	 * @return <code>true</code> if we are to show volumes
	 */
	public boolean showVolumes() {
		return show(PlainPanel3D.SHOW_VOLUMES);
	}

	/**
	 * This gets the z step used by the mouse and key adapters, to see how fast we
	 * move in or in in response to mouse wheel or up/down arrows. It should be
	 * overridden to give something sensible. like the scale/100;
	 *
	 * @return the z step (changes to zDist) for moving in and out
	 */
	@Override
	public float getZStep() {
		return (zmax - zmin) / 50f;
	}
}
