package cnuphys.ced.ced3d;

import java.awt.Font;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Random;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

import cnuphys.CLAS12Swim.CLAS12SwimResult;
import cnuphys.CLAS12Swim.ICLAS12Swimmer;
import cnuphys.CLAS12Swim.geometry.Plane;
import cnuphys.bCNU.component.LabeledTextField;
import cnuphys.bCNU.component.VariableRange;
import cnuphys.bCNU.dialog.VerticalFlowLayout;
import cnuphys.bCNU.graphics.component.CommonBorder;
import edu.cnu.mdi.ui.fonts.Fonts;
import cnuphys.bCNU.util.UnicodeSupport;
import cnuphys.ced.ced3d.view.SwimmingTestView3D;
import cnuphys.ced.swim.CedSwimmerFactory;
import cnuphys.magfield.MagneticFieldChangeListener;
import cnuphys.magfield.MagneticFields;

public class SwimmerControlPanel extends JPanel implements ActionListener, MagneticFieldChangeListener {

	public enum SWIM_ALGORITHM {STANDARD, FIXEDZ, FIXEDRHO, TOPLANE, TOCYLINDER}

	public enum CHARGE {POSITIVE, NEGATIVE, RANDOM}

	public enum SHOW {SUCCESSES, FAILURES, ALL}

	//which swim algorithm (stopper)
	private SWIM_ALGORITHM _algorithm = SWIM_ALGORITHM.FIXEDRHO;

	//how charge is determined
	private CHARGE _charge = CHARGE.RANDOM;

	//which tracks do we show
	private static SHOW _show = SHOW.ALL;

	//common font
	public static Font swimFont = Fonts.mediumFont;

	//swim results
	public static ArrayList<CLAS12SwimResult> _swimResults = new ArrayList<>();

	//algorithm selection
	private JRadioButton _standardRB;
	private JRadioButton _fixedZRB;
	private JRadioButton _fixedRhoRB;
	private JRadioButton _toPlaneRB;
	private JRadioButton _toCylinderRB;

	//charge choices
	private JRadioButton _positiveChargeRB;
	private JRadioButton _negativeChargeRB;
	private JRadioButton _randomChargeRB;

	//what to show
	private JRadioButton _sucessesRB;
	private JRadioButton _failuresRB;
	private JRadioButton _allRB;

	//panel widgets for selecting a plane and cylinder
	private PlanePanel _planePanel;
	private CylinderPanel _cylinderPanel;

	//the ranges. Order = p, xo, yo, zo, theta, phi
	private VariableRange[] _ranges = new VariableRange[6];

	//labels for ranges
	private String[] _rangePrompts = {"p from",
			"x" + UnicodeSupport.SUBZERO + " from",
			"y" + UnicodeSupport.SUBZERO + " from",
			"z" + UnicodeSupport.SUBZERO + " from",
			UnicodeSupport.SMALL_THETA + " from",
			UnicodeSupport.SMALL_PHI + " from"};
	private String[] _rangeUnits = {"GeV/c", "cm", "cm", "cm", "deg", "deg"};

	//the min and max for the ranges
	private double[] _rangeMin = {0.4, 0, 0, 0, 20, -180};
	private double[] _rangeMax = {3, 0, 0, 0, 45, 180};

	//number to swim
	private LabeledTextField _swimCount;
	private int _lastGoodSwimCount = 100;

	//random seed
	private LabeledTextField _randomSeed;
	private long _lastRandomSeed = 0;

	//swim to accuracy in microns
	private LabeledTextField _accuracy;
	private double _lastAccuracy = 10;

	//max path length cm
	private LabeledTextField _sMax;
	private double _lastSmax = 800; //cm

	//z cutoff cm
	private LabeledTextField _fixedZ;
	private double _lastFixedZ = 500; //cm

	// rho cutoff cm
	private LabeledTextField _fixedRho;
	private double _lastFixedRho = 100; //cm


	//shared random number generator
	private Random _rand = new Random();

	//parent view
	private SwimmingTestView3D _view;

	//parent panel
	private SwimmerPanel3D _panel3D;

	//the swim button
	private JButton _swimButton;

	//clear trajectories button
	private JButton _clearButton;

	//mag field description label
	private JLabel _magFieldLabel;



	//for the measured string to get them to align
	private String _measureString = "XYZ from";

	private static final int MINWIDTH = 300;

	/**
	 * The control panel for the swim tester 3D view
	 */
	public SwimmerControlPanel(SwimmingTestView3D view, SwimmerPanel3D panel3D) {
		_view = view;
		_panel3D = panel3D;
		setLayout(new BorderLayout(6, 4));
		addNorth();
		addCenter();
		addSouth();
		fixState();
		MagneticFields.getInstance().addMagneticFieldChangeListener(this);
	}

	//add the north component
	private void addNorth() {
		JPanel np = new JPanel();
		np.setLayout(new VerticalFlowLayout(true, 0));

		_magFieldLabel = new JLabel("Magnetic Field Configuration");
		_magFieldLabel.setFont(Fonts.mediumBoldFont);
		_magFieldLabel.setForeground(Color.red);

		np.add(_magFieldLabel);
		setMagLabel();


		FontMetrics fm = getFontMetrics(swimFont);
		int width = fm.stringWidth(" Random number seed:");

		//how many to swim
		_swimCount = createTextField("Number of swims:", null, "" + _lastGoodSwimCount, width, np);

		//random seed
		_randomSeed = createTextField("Random number seed:", null, "" + _lastRandomSeed, width, np);

		add(np, BorderLayout.NORTH);
	}

	//convenience method to create a labeled text field
	private LabeledTextField createTextField(String prompt, String units, String defaultStr, int width, JPanel panel) {
		LabeledTextField ltf = new LabeledTextField(prompt, units, 7, swimFont);
		ltf.setText(defaultStr);
		panel.add(ltf);

		JLabel plab = ltf.getPrompt();
		plab.setHorizontalAlignment(SwingConstants.RIGHT);

		Dimension d = plab.getPreferredSize();
		d.width = width;
		plab.setPreferredSize(d);

		return ltf;
	}


	//add the center component
	private void addCenter() {
		JPanel cp = new JPanel();
		cp.setLayout(new BorderLayout(6, 4));


		//add the algorithm radio button panel
		cp.add(getAlgRBPanel(), BorderLayout.CENTER);

		//add the range panel
		cp.add(getRangePanel(), BorderLayout.NORTH);


		//add the algorithm parameter panel
		//and the plane and cylinder panels
		JPanel sp = new JPanel();
		sp.setLayout(new VerticalFlowLayout(true, 0));

		_planePanel = new PlanePanel();
		_cylinderPanel = new CylinderPanel();


		sp.add(_planePanel);
		sp.add(_cylinderPanel);
		sp.add(getAlgParamPanel());

		cp.add(sp, BorderLayout.SOUTH);


		add(cp, BorderLayout.CENTER);
	}

	//add the south component
	private void addSouth() {
		JPanel sp = new JPanel();
		sp.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 0));

		_clearButton = createButton("Clear Trajectories", sp);
		_swimButton  = createButton("Swim Trajectories", sp);

		add(sp, BorderLayout.SOUTH);
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();

		if (d.width < MINWIDTH) {
			d.width = MINWIDTH;
		}

		return d;

	}

	/**
	 * Get the cutoff accuracy
	 * @return the cutoff accuracy in microns
	 */
	public double getAccuracy() {
		_lastAccuracy = getGoodValue(_accuracy, _lastAccuracy, true);
		return _lastAccuracy;
	}

	/**
	 * Get fixed z cutoff
	 * @return the fixed z cutoff in cm
	 */
	public double getFixedZ() {
		_lastFixedZ = getGoodValue(_fixedZ, _lastFixedZ, false);
		return _lastFixedZ;
	}

	/**
	 * Get fixed rho cutoff
	 * @return the fized rho in cm
	 */
	public double getFixedRho() {
		_lastFixedRho = getGoodValue(_fixedRho, _lastFixedRho, true);
		return _lastFixedRho;
	}

	/**
	 * Get the max path length
	 * @return max path length in cm
	 */
	public double getSmax() {
		_lastSmax = getGoodValue(_sMax, _lastSmax, true);
		return _lastSmax;
	}

	//convenience method to get a good value or use the last good value
	private double getGoodValue(LabeledTextField ltf, double lastVal, boolean positive) {
		OptionalDouble value = positive ? SwimInputValues.positiveDouble(ltf.getText())
				: SwimInputValues.finiteDouble(ltf.getText());
		if (value.isPresent()) {
			return value.getAsDouble();
		}

		ltf.setText(valStr(lastVal));
		return lastVal;

	}


	//convenience method for a string rep of a double
	private String valStr(double val) {
		String s =  String.format("%-8.3f", val);
		return s.trim();
	}

	/**
	 * Get how many swims we will do
	 *
	 * @return how many swims
	 */
	public int getSwimCount() {
		OptionalInt value = SwimInputValues.positiveInt(_swimCount.getText());
		if (value.isPresent()) {
			_lastGoodSwimCount = value.getAsInt();
		} else {
			_swimCount.setText(Integer.toString(_lastGoodSwimCount));
		}

		return _lastGoodSwimCount;
	}

	/**
	 * Get the random number generator
	 *
	 * @return the random number generator
	 */
	public Random getRand() {
		OptionalLong value = SwimInputValues.randomSeed(_randomSeed.getText());
		long seed = value.orElse(_lastRandomSeed);
		if (value.isEmpty() || !Long.toString(seed).equals(_randomSeed.getText().trim())) {
			_randomSeed.setText(Long.toString(seed));
		}

		_rand = (seed == 0) ? new Random() : new Random(seed);

		_lastRandomSeed = seed;
		return _rand;
	}


	//get the variable range panel
	private JPanel getRangePanel() {

		JPanel p = new JPanel();
		p.setLayout(new VerticalFlowLayout(true, 0));

		for (int i = 0; i < 6; i++) {
			_ranges[i] = createRange(_rangePrompts[i], _rangeUnits[i], _rangeMin[i], _rangeMax[i], p);
		}

		p.setBorder(new CommonBorder("Ranges for randomized variables"));

		return p;
	}

	//get the algorithm radio button panel
	private JPanel getAlgRBPanel() {
		JPanel p = new JPanel();
		p.setLayout(new VerticalFlowLayout(true, 0));


		ButtonGroup bg = new ButtonGroup();
		_standardRB   = createRadioButton("Standard swim to sMax", _algorithm == SWIM_ALGORITHM.STANDARD, bg, p);
		_fixedZRB     = createRadioButton("Swim to a fixed z", _algorithm == SWIM_ALGORITHM.FIXEDZ, bg, p);
		_fixedRhoRB   = createRadioButton("Swim to a fixed " + UnicodeSupport.SMALL_RHO, _algorithm == SWIM_ALGORITHM.FIXEDRHO, bg, p);
		_toPlaneRB    = createRadioButton("Swim to a plane", _algorithm == SWIM_ALGORITHM.TOPLANE, bg, p);
		_toCylinderRB = createRadioButton("Swim to a cylinder", _algorithm == SWIM_ALGORITHM.TOCYLINDER, bg, p);

		p.setBorder(new CommonBorder("Swimming stopping algorithm"));
		return p;
	}

	//get the algorithm radio button panel
	private JPanel getAlgParamPanel() {
		JPanel p = new JPanel();
        p.setLayout(new BorderLayout(2, 4));

        p.add(cutoffPanel(), BorderLayout.CENTER);

        JPanel sp = new JPanel();
        sp.setLayout(new VerticalFlowLayout(true, 0));

        sp.add(getChargePanel());
        sp.add(getShowPanel());


        p.add(sp, BorderLayout.SOUTH);

		p.setBorder(new CommonBorder("Stopping algorithm parameters"));
		return p;
	}


	/**
	 * Test of whether a trajectory should be shown
	 * @param result the swimmer result
	 * @return
	 */
	public static boolean showTrajectory(CLAS12SwimResult result) {
		if (result == null) {
			return false;
		}


		if (_show == SHOW.ALL) {
			return true;
		}


		boolean swimFailed = !result.isSuccess();

		if ((!swimFailed && (_show == SHOW.SUCCESSES)) || (swimFailed && (_show == SHOW.FAILURES))) {
			return true;
		}



		return false;
	}


	//what tracks are shown
	private JPanel getShowPanel() {
		JPanel p = new JPanel();
		p.setLayout(new FlowLayout(FlowLayout.LEFT, 6, 0));

		JLabel lab = new JLabel("Show: ");
		lab.setFont(swimFont);
		p.add(lab);

		ButtonGroup bg = new ButtonGroup();
		_sucessesRB   = createRadioButton("Successes", _show == SHOW.SUCCESSES, bg, p);
		_failuresRB   = createRadioButton("Failures", _show == SHOW.FAILURES, bg, p);
		_allRB   = createRadioButton("All", _show == SHOW.ALL, bg, p);

		return p;
	}


	//how charge is selected
	private JPanel getChargePanel() {
		JPanel p = new JPanel();
		p.setLayout(new FlowLayout(FlowLayout.LEFT, 6, 0));

		JLabel lab = new JLabel("Charge: ");
		lab.setFont(swimFont);
		p.add(lab);

		ButtonGroup bg = new ButtonGroup();
		_positiveChargeRB   = createRadioButton("+ only", _charge == CHARGE.POSITIVE, bg, p);
		_negativeChargeRB   = createRadioButton("- only", _charge == CHARGE.NEGATIVE, bg, p);
		_randomChargeRB   = createRadioButton("Random", _charge == CHARGE.RANDOM, bg, p);


		return p;
	}

	//algorithm cutoffs
	private JPanel cutoffPanel() {
		JPanel p = new JPanel();
		p.setLayout(new VerticalFlowLayout(true, 0));

		FontMetrics fm = getFontMetrics(swimFont);
		int width = fm.stringWidth("\"Swim to\" accuracy:");

		//accuracy
		_accuracy = createTextField("\"swim to\" accuracy:", UnicodeSupport.SMALL_MU + "m", valStr(_lastAccuracy), width, p);

		//max path length
		_sMax = createTextField("Max path length:", "cm", valStr(_lastSmax), width, p);

		//fixed z
		_fixedZ = createTextField("Fixed z cutoff:", "cm", valStr(_lastFixedZ), width, p);

		//fixed rho
		_fixedRho = createTextField("Fixed " + UnicodeSupport.SMALL_RHO + " cutoff:", "cm", valStr(_lastFixedRho), width, p);

		return p;
	}

	//create a radio button
	private JRadioButton createRadioButton(String prompt, boolean selected, ButtonGroup bg, JPanel p) {
		JRadioButton rb;
		rb = new JRadioButton(prompt, selected);
		rb.setFont(swimFont);
		bg.add(rb);
		rb.addActionListener(this);

		p.add(rb);
		return rb;
	}

	//fix the state of the GUI
	private void fixState() {
		_accuracy.setEnabled(_algorithm != SWIM_ALGORITHM.STANDARD);
		_fixedZ.setEnabled(_algorithm == SWIM_ALGORITHM.FIXEDZ);
		_fixedRho.setEnabled(_algorithm == SWIM_ALGORITHM.FIXEDRHO);

		_planePanel.setEnabled(_algorithm == SWIM_ALGORITHM.TOPLANE);
		_cylinderPanel.setEnabled(_algorithm == SWIM_ALGORITHM.TOCYLINDER);
	}


	//create a variable range component
	private VariableRange createRange(String prompt, String units, double minVal, double maxVal, JPanel p) {

		VariableRange vr = new VariableRange(prompt, units, _measureString, swimFont, minVal, maxVal);
		p.add(vr);
		return vr;

	}

	//convenience method to create a button with "this" as the action listener
	private JButton createButton(String label, JPanel p) {
		JButton button = new JButton(label);
		button.setFont(swimFont);
		button.addActionListener(this);
		p.add(button);
		return button;
	}



	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();

		if (source == _clearButton) {
			handleClear();
		}
		else if (source == _swimButton) {
			handleSwim();
		}
		else if ((source == _standardRB) && (_algorithm != SWIM_ALGORITHM.STANDARD)) {
			handleClear();
			_algorithm = SWIM_ALGORITHM.STANDARD;
		}
		else if ((source == _fixedZRB) && (_algorithm != SWIM_ALGORITHM.FIXEDZ)) {
			handleClear();
			_algorithm = SWIM_ALGORITHM.FIXEDZ;
		}
		else if ((source == _fixedRhoRB) && (_algorithm != SWIM_ALGORITHM.FIXEDRHO)) {
			handleClear();
			_algorithm = SWIM_ALGORITHM.FIXEDRHO;
		}
		else if ((source == _toPlaneRB) && (_algorithm != SWIM_ALGORITHM.TOPLANE)) {
			handleClear();
			_algorithm = SWIM_ALGORITHM.TOPLANE;
		}
		else if ((source == _toCylinderRB) && (_algorithm != SWIM_ALGORITHM.TOCYLINDER)) {
			handleClear();
			_algorithm = SWIM_ALGORITHM.TOCYLINDER;
		}
		else if ((source == _positiveChargeRB) && (_charge != CHARGE.POSITIVE)){
			handleClear();
			_charge = CHARGE.POSITIVE;
		}
		else if (source == _negativeChargeRB) {
			handleClear();
			_charge = CHARGE.NEGATIVE;
		}
		else if ((source == _randomChargeRB) && (_charge != CHARGE.RANDOM)) {
			handleClear();
			_charge = CHARGE.RANDOM;
		}
		else if ((source == _sucessesRB) && (_show != SHOW.SUCCESSES)) {
			_show = SHOW.SUCCESSES;
			_view.refresh();
		}
		else if ((source == _failuresRB) && (_show != SHOW.FAILURES)) {
			_show = SHOW.FAILURES;
			_view.refresh();
		}
		else if ((source == _allRB) && (_show != SHOW.ALL)) {
			_show = SHOW.ALL;
			_view.refresh();
		}

		fixState();
	}

	//convenience method to get the charge for the next swim
	private int getCharge() {
		return SwimInputValues.selectCharge(_charge, _rand.nextDouble());
	}

	//handle clear trajectories
	private void handleClear() {
		_swimResults.clear();
		_view.refresh();
	}

	/**
	 * Get the current collection of swim results
	 * @return the current collection of swim results
	 */
	public static ArrayList<CLAS12SwimResult> getSwimResults() {
		return _swimResults;
	}

	//handle generation of swim trajectories
	private void handleSwim() {
		//set the visual aid display item
		setDisplayItem();

		newSwim();
		_view.refresh();
	}

	//swim using the new swimmer
	private void newSwim() {

		double sMax = getSmax(); //cm
		double accuracy = getAccuracy()*1e-4; //microns to cm
	    double h = 5e-4; //step size
		double tolerance = 1.0e-8;

		ICLAS12Swimmer swimmer = CedSwimmerFactory.create();
		getRand();


		for (int i = 0; i < getSwimCount(); i++) {

			CLAS12SwimResult result = null;

			int q = getCharge();
			double p = _ranges[0].nextRandom(_rand);
			double xo = _ranges[1].nextRandom(_rand); //cm
			double yo = _ranges[2].nextRandom(_rand); //cm
			double zo = _ranges[3].nextRandom(_rand); //cm
			double theta = _ranges[4].nextRandom(_rand);
			double phi = _ranges[5].nextRandom(_rand);

			switch(_algorithm) {
			case STANDARD:
				result = swimmer.swim(q, xo, yo, zo, p, theta, phi, sMax, h, tolerance);
				break;

			case FIXEDZ:
				double z = getFixedZ();  //rho in cm
				result = swimmer.swimZ(q, xo, yo, zo, p, theta, phi, z, accuracy, sMax, h, tolerance);
				break;

			case FIXEDRHO:
				result = swimmer.swimRho(q, xo, yo, zo, p, theta, phi, getFixedRho(), accuracy, sMax, h, tolerance);
				break;

			case TOPLANE:
				double norm[] = _planePanel.getNormal();
				double point[] = _planePanel.getPoint();

			Plane targetPlane = new Plane(norm, point);
				result = swimmer.swimPlane(q, xo, yo, zo, p, theta, phi, targetPlane, accuracy, sMax, h, tolerance);
				break;

			case TOCYLINDER:
				double p1[] = _cylinderPanel.getCenterLineP1(); //cm
				double p2[] = _cylinderPanel.getCenterLineP2(); //cm


				double radius = _cylinderPanel.getRadius(); //cm

				result = swimmer.swimCylinder(q, xo, yo, zo, p, theta, phi, p1, p2, radius, accuracy, sMax, h, tolerance);

				break;

			}

			if (result != null) {
				_swimResults.add(result);
			}

		}

	}

	//set the display item based on the type of swim
	private void setDisplayItem() {
		//set the display item
		_panel3D.removeDisplayItem();
		switch(_algorithm) {

		case STANDARD:
			break;

		case FIXEDZ:
			double z = getFixedZ();  //z in cm
			_panel3D.setDisplayItemConstantZ((float)z);
			break;

		case FIXEDRHO:
			double rho = getFixedRho();  //rho in cm
			_panel3D.setDisplayItemConstantRho((float)rho);
			break;

		case TOPLANE:
			_panel3D.setDisplayItemPlane(_planePanel.getNormal(), _planePanel.getPoint());
			break;

		case TOCYLINDER:
			_panel3D.setDisplayItemCylinder(_cylinderPanel.getCenterLineP1(), _cylinderPanel.getCenterLineP2(), _cylinderPanel.getRadius());
			break;

		}

	}

	//set the mag field label
	private void setMagLabel() {
		String s = MagneticFields.getInstance().getActiveFieldDescription();
		_magFieldLabel.setText(" Magfield: " + s);
	}

	@Override
	public void magneticFieldChanged() {
		setMagLabel();
	}


}
