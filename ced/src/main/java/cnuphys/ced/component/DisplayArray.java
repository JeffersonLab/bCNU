package cnuphys.ced.component;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractButton;

import edu.cnu.mdi.component.checkboxarray.CheckBoxArray;
import cnuphys.bCNU.util.Bits;
import edu.cnu.mdi.ui.colors.X11Colors;
import cnuphys.ced.cedview.CedView;

/**
 * Create the display flags based on bits. This allows for a common appearance
 * across all views
 *
 * @author DHeddle
 *
 */

@SuppressWarnings("serial")
public class DisplayArray extends CheckBoxArray implements ItemListener {
	
	/** display label for urWT layer 1 */
	public static final String LAYER_1_LABEL = "Layer 1";
	
	/** display label for urWT layer 2 */
	public static final String LAYER_2_LABEL = "Layer 2";
	
	/** display label for urWT layer 3 */
	public static final String LAYER_3_LABEL = "Layer 3";
	
	/** display label for urWT layer 4 */
	public static final String LAYER_4_LABEL = "Layer 4";

	/** property for inner outer */
	public static final String SHOWINNER_PROPERTY = "DisplayInner";

	/** property for tof panel */
	public static final String TOFPANEL_PROPERTY = "DisplayInner";

	/** Label and access to the monte carlo truth checkbox */
	public static final String MCTRUTH_LABEL = "Truth";

	/** Label and access to the single event button */
	public static final String SINGLEEVENT_LABEL = "Single";

	/** Label and access to the accumulated button */
	public static final String ACCUMULATED_LABEL = "Accum.";

	/** Label and access for trkDoca label */
	public static final String NODOCA_LABEL = "No Doca";

	/** Label and access for trkDoca label */
	public static final String ALLDOCA_LABEL = "All Doca";

	/** Label and access for trkDoca label */
	public static final String TRKDOCA_LABEL = "TrkDoca";

	/** Label and access for doca label */
	public static final String DOCA_LABEL = "Doca";

	/** Tag and access to the accumulated button group */
	public static final String ACCUMULATED_BUTTONGROUP = "AccumulatedButtonGroup";

	/** Tag and access to the doca button group */
	public static final String DOCA_BUTTONGROUP = "DocaButtonGroup";

	/** Tag and access to the inner/outer button group */
	public static final String INNEROUTER_BUTTONGROUP = "InnerOuterButtonGroup";

	/** Tag and access to the tof panel button group */
	public static final String TOFPANEL_BUTTONGROUP = "TOFPanelButtonGroup";


	/** Tag and access to the BST ispoint/cross button group */
	public static final String MIDPOINTCROSS_BUTTONGROUP = "MidpointCrossButtonGroup";

	/** Label for inner plane of ec */
	public static final String INNER_LABEL = "Inner Plane";

	/** Label for outer plane of ec */
	public static final String OUTER_LABEL = "Outer Plane";

	/** Label for u strips */
	public static final String U_LABEL = "U";

	/** Label for v strips */
	public static final String V_LABEL = "V";

	/** Label for w strips */
	public static final String W_LABEL = "W";

	/** Label for TOF panel 1A strips */
	public static final String PAN1A_LABEL = "Panel 1A";

	/** Label for TOF pabel 1B strips */
	public static final String PAN1B_LABEL = "Panel 1B";

	/** Label for TOF Panel 2 strips */
	public static final String PAN2_LABEL = "Panel 2";

	/** BST Hits as crosses */
	public static final String COSMIC_LABEL = "Cosmic Tracks";

	private static final Color _buttonColor = X11Colors.getX11Color("Dark Red");

	/** Label for reconstructed crosses */
	private static final String CROSS_LABEL = "Crosses";

	/** Label for dc HB Hits button */
	private static final String DC_HIT_LABEL = "DC Recon Hits";

	/** Label for magnetic field grid */
	private static final String MAGGRID_LABEL = "Fieldmap Grids";

	/** Label for REC::Calorimeter Hits button */
	private static final String RECCAL_LABEL = "REC Cal";

	/** Label for dc reconstructed segments button */
	private static final String SEGMENT_LABEL = "Segments";

	/** Label for reconstructed hits (other than dc) button */
	private static final String RECON_HIT_LABEL = "Recon Hits";

	/** Label for reconstructed clusters button */
	public static final String CLUSTER_LABEL = "Clusters";

	/** Label for reconstructed fmt crosses button */
	public static final String FMTCROSS_LABEL = "FMT Crosses";

	/** Label for REC Particle button */
	public static final String RECPART_LABEL = "REC::Particles";

	/** Label for sector change diamonds */
	public static final String SECTORCHANGE_LABEL = "Sector Change";

	/** Global show HB */
	private static final String GLOBAL_HB_LABEL = "Reg HB";

	/** Global show TB */
	private static final String GLOBAL_TB_LABEL = "Reg TB";

	/** Global show AI HB */
	private static final String GLOBAL_AIHB_LABEL = "AI HB";

	/** Global show AITB */
	private static final String GLOBAL_AITB_LABEL = "AI TB";

	/** Global show ADC hits */
	private static final String GLOBAL_ADC_DATA_LABEL = "ADC Data";

	/** Label for reconstructed CVT Rec Tracks */
	private static final String CVTREC_TRACK_LABEL = "CVTRec Tracks";

	/** Label for reconstructed CVT Rec Trajectory */
	private static final String CVTREC_TRAJ_LABEL = "CVTRec Traj";

	/** Label for reconstructed CVT Rec KF Trajectory */
	private static final String CVTRECKF_TRAJ_LABEL = "CVTRecKF Traj";

	/** Label for reconstructed CVT Pass 1Tracks */
	private static final String CVTP1_TRACK_LABEL = "CVTP1 Tracks";

	/** Label for reconstructed CVT Pass 1Trajectory */
	private static final String CVTP1_TRAJ_LABEL = "CVTP1 Traj";


	// controls whether layer 1 of urWT is displayed
	public AbstractButton _layer1Button;
	
	// controls whether layer 2 of urWT is displayed
	public AbstractButton _layer2Button;
	
	// controls whether layer 3 of urWT is displayed
	public AbstractButton _layer3Button;
	
	// controls whether layer 4 of urWT is displayed
	public AbstractButton _layer4Button;

	// controls whether any HB data displayed
	private AbstractButton _showHBButton;

	// controls whether any TB data displayed
	private AbstractButton _showTBButton;


 // controls whether any AI HB data displayed
 	private AbstractButton _showAIHBButton;

 	// controls whether any AI TB data displayed
 	private AbstractButton _showAITBButton;

	// controls whether dc reconstructed Hits are displayed
	private AbstractButton _dcHitsButton;

	// controls whether REC::Calorimeter data are displayed
    private AbstractButton _recCalButton;

    // controls whether field map grids are displayed
    private AbstractButton _magGridButton;

	// controls whether reconstructed segments are displayed
	private AbstractButton _segmentButton;

	// controls whether reconstructed crosses are displayed
	private AbstractButton _crossButton;

	// controls whether reconstructed hits (not DC) are displayed
	private AbstractButton _reconHitButton;

	// controls whether ADC hits
	private AbstractButton _adcHitButton;

	// controls display of cvt reconstructed tracks
	private AbstractButton _cvtRecTrackButton;

	// controls display od cvt reconstructed trajectory bank data
	private AbstractButton _cvtRecTrajButton;

	// controls display of cvt kf trajectory bank data
	private AbstractButton _cvtRecKFTrajButton;

	// controls display of cvt pass 1 tracks
	private AbstractButton _cvtP1TrackButton;

	// controls display od cvt pass 1 trajectory bank data
	private AbstractButton _cvtP1TrajButton;

	// controls whether reconstructed clusters are displayed
	private AbstractButton _clusterButton;

	// controls whether reconstructed fmt crosses are displayed
	private AbstractButton _fmtCrossButton;

	// controls whether REC::Particle tracks are displayed
	private AbstractButton _recPartButton;

	// controls whether sector change diamonds
	private AbstractButton _sectorChangeButton;

	// controls mc truth is displayed (when available)
	private AbstractButton _mcTruthButton;

	// controls cosmic tracks in BST (when available)
	private AbstractButton _cosmicButton;

	// controls whether distance scale displayed
	// private AbstractButton _showScaleButton;

	// controls whether single events are displayed
	private AbstractButton _singleEventButton;

	// controls whether accumulated hits are displayed
	private AbstractButton _accumulatedButton;

	// controls whether all docas are displayed
	private AbstractButton _noDocaButton;

	// controls whether all docas are displayed
	private AbstractButton _allDocaButton;

	// controls whether track docas are displayed
	private AbstractButton _trkDocaButton;

	// controls whether doca columns are displayed
	private AbstractButton _docaButton;

	// controls whether inner plane displayed for ec
	private AbstractButton _innerButton;

	// controls whether inner plane displayed for ec
	private AbstractButton _outerButton;

	// controls whether we draw u strips
	private AbstractButton _uButton;

	// controls whether we draw v strips
	private AbstractButton _vButton;

	// controls whether we draw w strips
	private AbstractButton _wButton;

	// controls whether we draw panel 1A on TOF views
	private AbstractButton _pan1aButton;

	// controls whether we draw panel 1B on TOF views
	private AbstractButton _pan1bButton;

	// controls whether we draw panel 2 on TOF views
	private AbstractButton _pan2Button;


	// the parent view
	private CedView _view;

	/**
	 * Create a display flag array. This constructor produces a two column array.
	 *
	 * @param view the parent view
	 * @param bits controls what flags are added
	 */
	public DisplayArray(CedView view, int bits, int nc, int hgap) {
		super(nc, hgap, -2);
		_view = view;
		
		//four layers of urwt?
		if (Bits.checkBit(bits, DisplayBits.URWTLAYERS)) {
			_layer1Button = add(LAYER_1_LABEL, true, true, this, _buttonColor).getCheckBox();
			_layer2Button = add(LAYER_2_LABEL, true, true, this, _buttonColor).getCheckBox();
			_layer3Button = add(LAYER_3_LABEL, true, true, this, _buttonColor).getCheckBox();
			_layer4Button = add(LAYER_4_LABEL, true, true, this, _buttonColor).getCheckBox();
		}

		//tof panels?

		if (Bits.checkBit(bits, DisplayBits.TOFPANELS)) {
			_pan1aButton = add(PAN1A_LABEL, true, true, TOFPANEL_BUTTONGROUP, this, X11Colors.getX11Color("navy"))
					.getCheckBox();

			_pan1bButton = add(PAN1B_LABEL, false, true, TOFPANEL_BUTTONGROUP, this, X11Colors.getX11Color("navy"))
					.getCheckBox();

			_pan2Button = add(PAN2_LABEL, false, true, TOFPANEL_BUTTONGROUP, this, X11Colors.getX11Color("navy"))
					.getCheckBox();

		}


		// innerouter?
		if (Bits.checkBit(bits, DisplayBits.INNEROUTER)) {
			_innerButton = add(INNER_LABEL, true, true, INNEROUTER_BUTTONGROUP, this, X11Colors.getX11Color("navy"))
					.getCheckBox();

			_outerButton = add(OUTER_LABEL, false, true, INNEROUTER_BUTTONGROUP, this, X11Colors.getX11Color("navy"))
					.getCheckBox();
		}

		if (Bits.checkBit(bits, DisplayBits.UVWSTRIPS)) {
			_uButton = add(U_LABEL, true, true, this, Color.black).getCheckBox();
			_vButton = add(V_LABEL, true, true, this, Color.black).getCheckBox();
			_wButton = add(W_LABEL, true, true, this, Color.black).getCheckBox();
		}

		// accumulation?
		if (Bits.checkBit(bits, DisplayBits.ACCUMULATION)) {
			_singleEventButton = add(SINGLEEVENT_LABEL, view.isSingleEventMode(), true, ACCUMULATED_BUTTONGROUP, this,
					X11Colors.getX11Color("teal")).getCheckBox();

			_accumulatedButton = add(ACCUMULATED_LABEL, view.isAccumulatedMode(), true, ACCUMULATED_BUTTONGROUP, this,
					X11Colors.getX11Color("teal")).getCheckBox();

		}

		// DOCA Option?
		if (Bits.checkBit(bits, DisplayBits.DOCA)) {
			_trkDocaButton = add(NODOCA_LABEL, false, true, DOCA_BUTTONGROUP, this, X11Colors.getX11Color("dark green"))
					.getCheckBox();

			_allDocaButton = add(ALLDOCA_LABEL, true, true, DOCA_BUTTONGROUP, this, X11Colors.getX11Color("dark green"))
					.getCheckBox();

			_trkDocaButton = add(TRKDOCA_LABEL, false, true, DOCA_BUTTONGROUP, this,
					X11Colors.getX11Color("dark green")).getCheckBox();

			_docaButton = add(DOCA_LABEL, false, true, DOCA_BUTTONGROUP, this, X11Colors.getX11Color("dark green"))
					.getCheckBox();

		}

		// display mc truth?
		if (Bits.checkBit(bits, DisplayBits.MCTRUTH)) {
			_mcTruthButton = add(MCTRUTH_LABEL, true, true, this, Color.black).getCheckBox();
		}

		// cosmics?
		if (Bits.checkBit(bits, DisplayBits.COSMICS)) {
			_cosmicButton = add(COSMIC_LABEL, true, true, this, Color.black).getCheckBox();
		}

		// global hit based data
		if (Bits.checkBit(bits, DisplayBits.GLOBAL_HB)) {
			_showHBButton = add(GLOBAL_HB_LABEL, true, true, this, _buttonColor).getCheckBox();
		}

		// global time based data
		if (Bits.checkBit(bits, DisplayBits.GLOBAL_TB)) {
			_showTBButton = add(GLOBAL_TB_LABEL, true, true, this, _buttonColor).getCheckBox();
		}

		// global hit based AI data
		if (Bits.checkBit(bits, DisplayBits.GLOBAL_AIHB)) {
			_showAIHBButton = add(GLOBAL_AIHB_LABEL, true, true, this, _buttonColor).getCheckBox();
		}

		// global time based AI data
		if (Bits.checkBit(bits, DisplayBits.GLOBAL_AITB)) {
			_showAITBButton = add(GLOBAL_AITB_LABEL, true, true, this, _buttonColor).getCheckBox();
		}

		// reonstructed crosses?
		if (Bits.checkBit(bits, DisplayBits.CROSSES)) {
			_crossButton = add(CROSS_LABEL, true, true, this, _buttonColor).getCheckBox();
		}

		// dc reonstructed hits?
		if (Bits.checkBit(bits, DisplayBits.DC_HITS)) {
			_dcHitsButton = add(DC_HIT_LABEL, true, true, this, _buttonColor).getCheckBox();
		}

		// dc reonstructed hits?
		if (Bits.checkBit(bits, DisplayBits.RECCAL)) {
			_recCalButton = add(RECCAL_LABEL, true, true, this, _buttonColor).getCheckBox();
		}

		// mag field grid?
		if (Bits.checkBit(bits, DisplayBits.MAGGRID)) {
			_magGridButton = add(MAGGRID_LABEL, true, true, this, _buttonColor).getCheckBox();
		}

		// reconstructed dc segments?
		if (Bits.checkBit(bits, DisplayBits.SEGMENTS)) {
			_segmentButton = add(SEGMENT_LABEL, true, true, this, _buttonColor).getCheckBox();
		}

		// other (not DC) reconstructed hits
		if (Bits.checkBit(bits, DisplayBits.RECONHITS)) {
			_reconHitButton = add(RECON_HIT_LABEL, true, true, this, _buttonColor).getCheckBox();
		}

		if (Bits.checkBit(bits, DisplayBits.CVTRECTRACKS)) {
			_cvtRecTrackButton = add(CVTREC_TRACK_LABEL, true, true, this, _buttonColor).getCheckBox();
		}

		if (Bits.checkBit(bits, DisplayBits.CVTRECTRAJ)) {
			_cvtRecTrajButton = add(CVTREC_TRAJ_LABEL, true, true, this, _buttonColor).getCheckBox();
		}

		if (Bits.checkBit(bits, DisplayBits.CVTRECKFTRAJ)) {
			_cvtRecKFTrajButton = add(CVTRECKF_TRAJ_LABEL, true, true, this, _buttonColor).getCheckBox();
		}

		if (Bits.checkBit(bits, DisplayBits.CVTP1TRACKS)) {
			_cvtP1TrackButton = add(CVTP1_TRACK_LABEL, true, true, this, _buttonColor).getCheckBox();
		}

		if (Bits.checkBit(bits, DisplayBits.CVTP1TRAJ)) {
			_cvtP1TrajButton = add(CVTP1_TRAJ_LABEL, true, true, this, _buttonColor).getCheckBox();
		}

		// ADC hits
		if (Bits.checkBit(bits, DisplayBits.ADCDATA)) {
			_adcHitButton = add(GLOBAL_ADC_DATA_LABEL, true, true, this, _buttonColor).getCheckBox();
		}

		//sector change markers
		if (Bits.checkBit(bits, DisplayBits.SECTORCHANGE)) {
			_sectorChangeButton = add(SECTORCHANGE_LABEL, false, true, this, _buttonColor).getCheckBox();
		}

		// fmt crosses
		if (Bits.checkBit(bits, DisplayBits.FMTCROSSES)) {
			_fmtCrossButton = add(FMTCROSS_LABEL, true, true, this, _buttonColor).getCheckBox();
		}

		// REC Particles
		if (Bits.checkBit(bits, DisplayBits.RECPART)) {
			_recPartButton = add(RECPART_LABEL, true, true, this, _buttonColor).getCheckBox();
		}


		// reconstructed clusters
		if (Bits.checkBit(bits, DisplayBits.CLUSTERS)) {
			_clusterButton = add(CLUSTER_LABEL, true, true, this, _buttonColor).getCheckBox();
		}


	}

	/**
	 * A button has been clicked
	 *
	 * @param e the causal event
	 */
	@Override
	public void itemStateChanged(ItemEvent e) {
		AbstractButton button = (AbstractButton) e.getSource();
		if (button == _singleEventButton) {
			_view.setMode(CedView.Mode.SINGLE_EVENT);
		} else if (button == _accumulatedButton) {
			_view.setMode(CedView.Mode.ACCUMULATED);
		} else if (button == _innerButton) {
			_view.setBooleanProperty(SHOWINNER_PROPERTY, true);
		} else if (button == _outerButton) {
			_view.setBooleanProperty(SHOWINNER_PROPERTY, false);
		} else if (button == _pan1aButton) {
			_view.setIntProperty(TOFPANEL_PROPERTY, 0);
		} else if (button == _pan1bButton) {
			_view.setIntProperty(TOFPANEL_PROPERTY, 1);
		} else if (button == _pan2Button) {
			_view.setIntProperty(TOFPANEL_PROPERTY, 2);
		}

		// repaint the view
		if (_view != null) {
			_view.getContainer().refresh();
		}
	}

	/**
	 * Convenience method to see it we show the montecarlo truth.
	 *
	 * @return <code>true</code> if we are to show the montecarlo truth, if it is
	 *         available.
	 */
	public boolean showMcTruth() {
		return (_mcTruthButton != null) && _mcTruthButton.isSelected();
	}

	/**
	 * Convenience method to see it we show the cosmic tracks.
	 *
	 * @return <code>true</code> if we are to show the cosmic tracks, if it is
	 *         available.
	 */
	public boolean showCosmics() {
		return (_cosmicButton != null) && _cosmicButton.isSelected();
	}

	/**
	 * Convenience method to see if scale is displayed
	 *
	 * @return <code>true</code> if we are to display the distance scale
	 */
	public boolean showScale() {
		return true;
//		return _showScaleButton == null ? false : _showScaleButton.isSelected();
	}
	
	/**
	 * Convenience method to see if layer 1 of urWT is displayed
	 *
	 * @return <code>true</code> if we are to display layer 1 of urWT
	 */
	public boolean showLayer1() {
		return _layer1Button == null ? false : _layer1Button.isSelected();
	}
	
	/**
	 * Convenience method to see if layer 2 of urWT is displayed
	 *
	 * @return <code>true</code> if we are to display layer 2 of urWT
	 */
	public boolean showLayer2() {
		return _layer2Button == null ? false : _layer2Button.isSelected();
	}
	
	/**
	 * Convenience method to see if layer 3 of urWT is displayed
	 *
	 * @return <code>true</code> if we are to display layer 3 of urWT
	 */
	public boolean showLayer3() {
		return _layer3Button == null ? false : _layer3Button.isSelected();
	}
	
	/**
	 * Convenience method to see if layer 4 of urWT is displayed
	 *
	 * @return <code>true</code> if we are to display layer 4 of urWT
	 */
	public boolean showLayer4() {
		return _layer4Button == null ? false : _layer4Button.isSelected();
	}

	/**
	 * Convenience method to see if u strips displayed
	 *
	 * @return <code>true</code> if we are to display u strips
	 */
	public boolean showUStrips() {
		return _uButton == null ? false : _uButton.isSelected();
	}

	/**
	 * Convenience method to see if v strips displayed
	 *
	 * @return <code>true</code> if we are to display v strips
	 */
	public boolean showVStrips() {
		return _vButton == null ? false : _vButton.isSelected();
	}

	/**
	 * Convenience method to see if w strips displayed
	 *
	 * @return <code>true</code> if we are to display w strips
	 */
	public boolean showWStrips() {
		return _wButton == null ? false : _wButton.isSelected();
	}

	/**
	 * Convenience method to see if we show the reconstructed clusters.
	 *
	 * @return <code>true</code> if we are to show reconstructed clusters
	 */
	public boolean showClusters() {
		return (_clusterButton != null) && _clusterButton.isSelected();
	}

	/**
	 * Convenience method to see if we show the reconstructed FMT Crosses.
	 *
	 * @return <code>true</code> if we are to show reconstructed FMT Crosses
	 */
	public boolean showFMTCrosses() {
		return (_fmtCrossButton != null) && _fmtCrossButton.isSelected();
	}

	/**
	 * Convenience method to see if we show the REC::Particle tracks.
	 *
	 * @return <code>true</code> if we are to show REC::Particle tracks
	 */
	public boolean showRecPart() {
		return (_recPartButton != null) && _recPartButton.isSelected();
	}

	/**
	 * Convenience method to see if we show the sector change diamonds.
	 *
	 * @return <code>true</code> if we are to show  sector change diamonds
	 */
	public boolean showSectorChange() {
		return (_sectorChangeButton != null) && _sectorChangeButton.isSelected();
	}


	/**
	 * Convenience method to see if we show the reconstructed hits. These are
	 * reconstructed hits except DC hits
	 *
	 * @return <code>true</code> if we are to show reconstructed hits.
	 */
	public boolean showReconHits() {
		return (_reconHitButton != null) && _reconHitButton.isSelected();
	}

	/**
	 * Convenience method to see if we show the ADC hits. These are ADC hits
	 *
	 * @return <code>true</code> if we are to show ADC hits.
	 */
	public boolean showADCHits() {
		return (_adcHitButton != null) && _adcHitButton.isSelected();
	}

	/**
	 * Convenience method to see if we show CVT reconstructed tracks.
	 *
	 * @return <code>true</code> if we are to show CVT reconstructed tracks.
	 */
	public boolean showCVTRecTracks() {
		return (_cvtRecTrackButton != null) && _cvtRecTrackButton.isSelected();
	}

	/**
	 * Convenience method to see if we show CVT reconstructed trajectory data.
	 * hits except
	 *
	 * @return <code>true</code> if we are to show CVT reconstructed trajectory data.
	 */
	public boolean showCVTRecTraj() {
		return (_cvtRecTrajButton != null) && _cvtRecTrajButton.isSelected();
	}

	/**
	 * Convenience method to see if we show recon KF traj
	 *
	 * @return <code>true</code> if we are to show CVT pass 1 trajectory data.
	 */
	public boolean showRecKFTraj() {
		return (_cvtRecKFTrajButton != null) && _cvtRecKFTrajButton.isSelected();
	}

	/**
	 * Convenience method to see if we show CVT Pass 1 tracks.
	 *
	 * @return <code>true</code> if we are to show CVT Pass 1 tracks.
	 */
	public boolean showCVTP1Tracks() {
		return (_cvtP1TrackButton != null) && _cvtP1TrackButton.isSelected();
	}

	/**
	 * Convenience method to see if we show CVT pass 1 trajectory data.
	 * hits except
	 *
	 * @return <code>true</code> if we are to show CVT pass 1 trajectory data.
	 */
	public boolean showCVTP1Traj() {
		return (_cvtP1TrajButton != null) && _cvtP1TrajButton.isSelected();
	}



	/**
	 * Convenience method global hit based display
	 *
	 * @return <code>true</code> if we are to show hb globally
	 */
	public boolean showHB() {
		return (_showHBButton != null) && _showHBButton.isSelected();
	}

	/**
	 * Convenience method global time based display
	 *
	 * @return <code>true</code> if we are to show tb globally
	 */
	public boolean showTB() {
		return (_showTBButton != null) && _showTBButton.isSelected();
	}

	/**
	 * Convenience method global AI hit based display
	 *
	 * @return <code>true</code> if we are to show AI hb globally
	 */
	public boolean showAIHB() {
		return (_showAIHBButton != null) && _showAIHBButton.isSelected();
	}

	/**
	 * Convenience method global AI time based display
	 *
	 * @return <code>true</code> if we are to show AI tb globally
	 */
	public boolean showAITB() {
		return (_showAITBButton != null) && _showAITBButton.isSelected();
	}


	/**
	 * Convenience method to see if we show the dc HB reconstructed hits.
	 *
	 * @return <code>true</code> if we are to show dc HB reconstructed hits.
	 */
	public boolean showDCHBHits() {
		return showHB() && (_dcHitsButton != null) && _dcHitsButton.isSelected();
	}

	/**
	 * Convenience method to see if we show the AI dc TB reconstructed hits.
	 *
	 * @return <code>true</code> if we are to show AI dc TB reconstructed hits.
	 */
	public boolean showAIDCTBHits() {
		return showAITB() && (_dcHitsButton != null) && _dcHitsButton.isSelected();
	}

	/**
	 * Convenience method to see if we show the AI dc HB reconstructed hits.
	 *
	 * @return <code>true</code> if we are to show AI dc HB reconstructed hits.
	 */
	public boolean showAIDCHBHits() {
		return showAIHB() && (_dcHitsButton != null) && _dcHitsButton.isSelected();
	}

	/**
	 * Convenience method to see if we show the dc TB reconstructed hits.
	 *
	 * @return <code>true</code> if we are to show dc TB reconstructed hits.
	 */
	public boolean showDCTBHits() {
		return showTB() && (_dcHitsButton != null) && _dcHitsButton.isSelected();
	}

	/**
	 * Convenience method to see if we show REC::Calorimeter data
	 *
	 * @return <code>true</code> if we are to show REC::Calorimeter.
	 */
	public boolean showRecCal() {
		return (_recCalButton != null) && _recCalButton.isSelected();
	}

	/**
	 * Convenience method to see if we show field map grid
	 *
	 * @return <code>true</code> if we are to show field map grid
	 */
	public boolean showMagGrid() {
		return (_magGridButton != null) && _magGridButton.isSelected();
	}

	/**
	 * Convenience method to see if we show the dc HB reconstructed clusters.
	 *
	 * @return <code>true</code> if we are to show dc HB reconstructed clusters.
	 */
	public boolean showDCHBClusters() {
		return showHB() && showClusters();
	}

	/**
	 * Convenience method to see if we show the dc TB reconstructed clusters.
	 *
	 * @return <code>true</code> if we are to show dc TB reconstructed clusters.
	 */
	public boolean showDCTBClusters() {
		return showTB() && showClusters();
	}

	/**
	 * Convenience method to see if we show the reconstructed segments.
	 *
	 * @return <code>true</code> if we are to showreconstructed crosses.
	 */
	public boolean showSegments() {
		return (_segmentButton != null) && _segmentButton.isSelected();
	}

	/**
	 * Convenience method to see if we show the dc hb reconstructed segments.
	 *
	 * @return <code>true</code> if we are to show dc hb reconstructed segments.
	 */
	public boolean showDCHBSegments() {
		return showHB() && showSegments();
	}

	/**
	 * Convenience method to see if we show the dc tb reconstructed segments.
	 *
	 * @return <code>true</code> if we are to show dc tb reconstructed segments.
	 */
	public boolean showDCTBSegments() {
		return showTB() && showSegments();
	}


	/**
	 * Convenience method to see if we show the AI dc hb reconstructed segments.
	 *
	 * @return <code>true</code> if we are to show AI dc hb reconstructed segments.
	 */
	public boolean showAIDCHBSegments() {
		return showAIHB() && showSegments();
	}

	/**
	 * Convenience method to see if we show the AI dc tb reconstructed segments.
	 *
	 * @return <code>true</code> if we are to show AI dc tb reconstructed segments.
	 */
	public boolean showAIDCTBSegments() {
		return showAITB() && showSegments();
	}

	/**
	 * Convenience method to see if we show the reconstructed crosses.
	 *
	 * @return <code>true</code> if we are to show reconstructed crosses.
	 */
	public boolean showCrosses() {
		return (_crossButton != null) && _crossButton.isSelected();
	}

	/**
	 * Convenience method to see if we show the dc HB reconstructed crosses.
	 *
	 * @return <code>true</code> if we are to show dc HB reconstructed crosses.
	 */
	public boolean showDCHBCrosses() {
		return showHB() && showCrosses();
	}

	/**
	 * Convenience method to see if we show the dc TB reconstructed crosses.
	 *
	 * @return <code>true</code> if we are to show dc TB reconstructed crosses.
	 */
	public boolean showDCTBCrosses() {
		return showTB() && showCrosses();
	}

	/**
	 * Convenience method to see if we show the AI dc HB reconstructed crosses.
	 *
	 * @return <code>true</code> if we are to show AI dc HB reconstructed crosses.
	 */
	public boolean showAIDCHBCrosses() {
		return showAIHB() && showCrosses();
	}

	/**
	 * Convenience method to see if we show the AI dc TB reconstructed crosses.
	 *
	 * @return <code>true</code> if we are to show AI dc TB reconstructed crosses.
	 */
	public boolean showAIDCTBCrosses() {
		return showAITB() && showCrosses();
	}


	/**
	 * Convenience method to see if we show no doca at all.
	 *
	 * @return <code>true</code> if we are to show no doca.
	 */
	public boolean showNoDoca() {
		return (_noDocaButton != null) && _noDocaButton.isSelected();
	}

	/**
	 * Convenience method to see if we show all doca at all.
	 *
	 * @return <code>true</code> if we are to show all doca.
	 */
	public boolean showAllDoca() {
		return (_allDocaButton != null) && _allDocaButton.isSelected();
	}

	/**
	 * Convenience method to see if we show the track doca column.
	 *
	 * @return <code>true</code> if we are to show track doca column.
	 */
	public boolean showTrkDoca() {
		return showAllDoca() || ((_trkDocaButton != null) && _trkDocaButton.isSelected());
	}

	/**
	 * Convenience method to see if we show the doca column.
	 *
	 * @return <code>true</code> if we are to show the doca column.
	 */
	public boolean showDoca() {
		return showAllDoca() || ((_docaButton != null) && _docaButton.isSelected());
	}

	/**
	 * Convenience method to see if we show the HB trkDoca column.
	 *
	 * @return <code>true</code> if we are to show HB trkDoca column.
	 */
	public boolean showHBTrkDoca() {
		return showHB() && showTrkDoca();
	}

	/**
	 * Convenience method to see if we show the TB trkDoca column.
	 *
	 * @return <code>true</code> if we are to show TB trkDoca column.
	 */
	public boolean showTBTrkDoca() {
		return showTB() && showTrkDoca();
	}

	/**
	 * Convenience method to see if we show the HB doca column.
	 *
	 * @return <code>true</code> if we are to show HB doca column.
	 */
	public boolean showHBDoca() {
		return showHB() && showDoca();
	}

	/**
	 * Convenience method to see if we show the TB doca column.
	 *
	 * @return <code>true</code> if we are to show TB doca column.
	 */
	public boolean showTBDoca() {
		return showTB() && showDoca();
	}

}
