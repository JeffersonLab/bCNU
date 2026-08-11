package cnuphys.ced.magfield;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;

import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import bCNU3D.DoubleFormat;
import cnuphys.bCNU.graphics.ImageManager;
import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.bCNU.util.UnicodeSupport;
import cnuphys.magfield.FieldProbe;
import cnuphys.magfield.MagneticFieldInitializationException;
import cnuphys.magfield.MagneticFields;
import cnuphys.magfield.MagneticFields.FieldType;
import edu.cnu.mdi.graphics.style.IStyled;
import edu.cnu.mdi.graphics.style.SymbolType;
import edu.cnu.mdi.splot.fit.CurveDrawingMethod;
import edu.cnu.mdi.splot.pdata.Curve;
import edu.cnu.mdi.splot.pdata.PlotData;
import edu.cnu.mdi.splot.pdata.PlotDataException;
import edu.cnu.mdi.splot.pdata.PlotDataType;
import edu.cnu.mdi.splot.plot.PlotCanvas;
import edu.cnu.mdi.splot.plot.PlotPanel;
import edu.cnu.mdi.splot.plot.PlotParameters;

@SuppressWarnings("serial")
public class PlotFieldDialog extends JDialog implements ActionListener {

	private static int _numPlotPoints = 50000;

	private static final int Z = 0;
	private static final int RHO = 1;
	private static final int PHI = 2;

	// which is the variable (other two are fixed)
	private static int _whichVaries = Z;

	// the default fixed values and ranges

	private static String sPHI = UnicodeSupport.SMALL_PHI;
	private static String sRHO = UnicodeSupport.SMALL_RHO;
	private static String sDEG = UnicodeSupport.DEGREE;

	// the x axis labels
	private static String _xLabels[] = { "z (cm) ", sRHO + " (cm) ", sPHI + " (deg)" };

	// the toggle button labels
	private static String tbLabels[] = { " z ", " " + sRHO + " ", " " + sPHI + " " };

	// generate a new plot
	private JButton _plotButton;

	// clear all plots
	private JButton _clearButton;

	// hold the variable changing fields
	private VariablePanel _varPanels[];

	// the variable toggle buttons
	private JRadioButton _vButtons[];

	// plot parameters
	private PlotParameters _parameters;

	private final PlotCanvas _canvas;
	private final PlotData _plotData;

	/**
	 * Create the dialog for ploting the field
	 *
	 * @param parent the parent dialog
	 * @param modal  the usual meaning
	 */
	public PlotFieldDialog(JFrame parent, boolean modal) {
		super(parent, "Magnetic Field Plotter", modal);
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		setLayout(new BorderLayout());
		setIconImage(ImageManager.cnuIcon.getImage());

		_plotData = createPlotData();
		_canvas = new PlotCanvas(_plotData, "Magnetic Field", "z (cm)", "|B| (T)");
		setPreferences();
		addNorth();
		PlotPanel plotPanel = new PlotPanel(_canvas);
		plotPanel.setPreferredSize(new Dimension(600, 600));
		add(plotPanel, BorderLayout.CENTER);
		setJMenuBar(new JMenuBar());
		pack();
		setLocationRelativeTo(parent);
	}

	private static PlotData createPlotData() {
		try {
			PlotData data = new PlotData(PlotDataType.XYXY,
					new String[] { getInitialCurveName() }, null);
			configureCurve((Curve) data.getCurve(0));
			return data;
		}
		catch (PlotDataException exception) {
			throw new IllegalStateException("Could not create the magnetic-field plot", exception);
		}
	}

	private static String getInitialCurveName() {
		return "|B| (1) " + MagneticFields.getInstance().getCurrentConfiguration();
	}

	private void setPreferences() {
		_parameters = _canvas.getParameters();
		_parameters.setExtraDrawing(true);
		_parameters.includeYZero(true);
		_parameters.setMinExponentX(3);
	}

	/**
	 * Add a north component
	 */
	private void addNorth() {
		JPanel panel = new JPanel();

		panel.setLayout(new BorderLayout(2, 2));
		panel.add(makeVariablePanel(), BorderLayout.NORTH);
		panel.add(makeButtonPanel(), BorderLayout.SOUTH);

		panel.setBorder(new CommonBorder("Variable Selection"));

		JPanel cPanel = new JPanel() {
			@Override
			public Insets getInsets() {
				Insets def = super.getInsets();
				return new Insets(def.top + 2, def.left + 2, def.bottom + 2, def.right + 2);
			}

		};

		cPanel.setLayout(new GridLayout(3, 1, 0, 6));

		_varPanels = new VariablePanel[3];

		for (int i = 0; i < 3; i++) {
			_varPanels[i] = new VariablePanel(i);
			cPanel.add(_varPanels[i]);
		}

		panel.add(cPanel, BorderLayout.CENTER);

		add(panel, BorderLayout.NORTH);

	}

	// make a button panel
	private JPanel makeButtonPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 0));

		_clearButton = new JButton(" Clear ");
		_clearButton.addActionListener(this);

		_plotButton = new JButton(" Plot ");
		_plotButton.addActionListener(this);

		panel.add(_clearButton);
		panel.add(_plotButton);
		return panel;
	}

	private JPanel makeVariablePanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 0));
		ButtonGroup bg = new ButtonGroup();

		panel.add(new JLabel("Select what varies: "));

		_vButtons = new JRadioButton[3];
		for (int var = 0; var < 3; var++) {
			_vButtons[var] = makeRadioButton(tbLabels[var], var == _whichVaries);
			bg.add(_vButtons[var]);
			panel.add(_vButtons[var]);
		}

		return panel;
	}

	private JRadioButton makeRadioButton(String label, boolean selected) {
		JRadioButton tb = new JRadioButton(label, selected);
		tb.addActionListener(this);
		return tb;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();

		if (source == _plotButton) {
			doPlot();
		} else if (source == _clearButton) {
			doClear();
		} else {
			for (int var = 0; var < 3; var++) {
				if (source == _vButtons[var]) {

					if (var == _whichVaries) {
						return;
					}

					int oldV = _whichVaries;
					_whichVaries = var;
					_varPanels[oldV].setEnabled();
					_varPanels[_whichVaries].setEnabled();

					_parameters.setXLabel(_xLabels[var]);
					fixExtraStrings();
					// System.err.println("CHANGED VARIABLE");
					// _canvas.getDataSet().clear();
					break;
				}

			}
		}
	}

	private void fixExtraStrings() {

		String s0 = MagneticFields.getInstance().getActiveFieldDescription();
		String s4 = MagneticFields.getInstance().fileBaseNames();
		String s1 = "";
		String s2 = "";
		switch (_whichVaries) {
		case Z:
			String pV = DoubleFormat.doubleFormat(_varPanels[PHI].getFixedValue(), 1);
			String rV = DoubleFormat.doubleFormat(_varPanels[RHO].getFixedValue(), 1);
			s1 = sPHI + " = " + pV + sDEG;
			s2 = sRHO + " = " + rV + "cm";
			break;

		case RHO:
			pV = DoubleFormat.doubleFormat(_varPanels[PHI].getFixedValue(), 1);
			String zV = DoubleFormat.doubleFormat(_varPanels[Z].getFixedValue(), 1);
			s1 = sPHI + " = " + pV + sDEG;
			s2 = "z  = " + zV + "cm";
			break;

		case PHI:
			rV = DoubleFormat.doubleFormat(_varPanels[RHO].getFixedValue(), 1);
			zV = DoubleFormat.doubleFormat(_varPanels[Z].getFixedValue(), 1);
			s1 = sRHO + " = " + rV + "cm";
			s2 = "z  = " + zV + "cm";
			break;
		}

		_parameters.setExtraStrings(s0, s4, s1, s2);

	}

	// clear all the plots
	private void doClear() {
		_canvas.clearData();
		_plotData.getFirstCurve().setVisible(false);
		_parameters.setExtraStrings();
		_canvas.setWorldSystem();
		_canvas.repaint();
	}

	// create the plot
	private void doPlot() {
		// _canvas.getDataSet().clear();

		Curve curve = (Curve) _plotData.getFirstCurve();
		curve.clearData();
		curve.setName(getInitialCurveName());
		curve.setVisible(true);

		FieldProbe probe = FieldProbe.factory();

		double min = _varPanels[_whichVaries].getMinValue();
		double max = _varPanels[_whichVaries].getMaxValue();
		double del = (max - min) / (_numPlotPoints - 1);

		float x;
		float y;
		float z;
		double phiRad;

		double[] values = new double[_numPlotPoints];
		double[] magnitudes = new double[_numPlotPoints];
		for (int i = 0; i < _numPlotPoints; i++) {
			double val = min + i * del;
			double mag = 0;
			switch (_whichVaries) {
			case Z:
				phiRad = Math.toRadians(_varPanels[PHI].getFixedValue());
				x = (float) (_varPanels[RHO].getFixedValue() * Math.cos(phiRad));
				y = (float) (_varPanels[RHO].getFixedValue() * Math.sin(phiRad));
				z = (float) val;
				mag = probe.fieldMagnitude(x, y, z);
				break;

			case RHO:
				phiRad = Math.toRadians(_varPanels[PHI].getFixedValue());
				x = (float) (val * Math.cos(phiRad));
				y = (float) (val * Math.sin(phiRad));
				z = (float) _varPanels[Z].getFixedValue();
				mag = probe.fieldMagnitude(x, y, z);
				break;

			case PHI:
				phiRad = Math.toRadians(val);
				x = (float) (_varPanels[RHO].getFixedValue() * Math.cos(phiRad));
				y = (float) (_varPanels[RHO].getFixedValue() * Math.sin(phiRad));
				z = (float) _varPanels[Z].getFixedValue();
				mag = probe.fieldMagnitude(x, y, z);
				break;
			}

			mag = mag / 10; // to tesla
			values[i] = val;
			magnitudes[i] = mag;
		}
		curve.addAll(values, magnitudes);

		fixExtraStrings();
		_canvas.setWorldSystem();
		_canvas.repaint();
	}

	private static void configureCurve(Curve curve) {
		curve.setCurveDrawingMethod(CurveDrawingMethod.CONNECT);
		IStyled style = curve.getStyle();
		style.setSymbolType(SymbolType.NOSYMBOL);
		style.setLineColor(Color.BLACK);
		style.setLineWidth(2f);
	}

	public static void main(String arg[]) {

		// get the home directory
		String homeDir = System.getProperty("user.home");
		// String torusPath =
		// "/Users/heddle/magfield/Jan_clas12TorusFull_2.00.dat";
		String torusPath = homeDir + "/magfield/Symm_torus_r2501_phi16_z251_24Apr2018.dat";
		String solenoidPath = homeDir + "/magfield/Symm_solenoid_r601_phi1_z1201_13June2018.dat";
		try {
			MagneticFields.getInstance().initializeMagneticFieldsFromPath(torusPath, solenoidPath);
			MagneticFields.getInstance().setActiveField(FieldType.TORUS);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.println("Could not initialize Magnetic Fields");
			System.exit(1);
		} catch (MagneticFieldInitializationException e) {
			e.printStackTrace();
			System.err.println("Could not initialize Magnetic Fields");
			System.exit(1);
		}

		PlotFieldDialog pfd = new PlotFieldDialog(null, true);

		JMenuBar mb = pfd.getJMenuBar();
		mb.add(MagneticFields.getInstance().getMagneticFieldMenu());

		WindowAdapter wa = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				System.out.println("Exiting.");
				System.exit(0);
			}
		};
		pfd.addWindowListener(wa);

		pfd.setVisible(true);

	}

	class VariablePanel extends JPanel {

		private JTextField _fixedTF;
		private JTextField _minTF;
		private JTextField _maxTF;

		private double fixedValues[] = { 375, 50, 0 };
		private double minValues[] = { 200, 0, -20 };
		private double maxValues[] = { 500, 250, 20 };

		private int _varIndex;

		public VariablePanel(int var) {

			_varIndex = var;
			setLayout(new FlowLayout(FlowLayout.LEFT, 20, 0));
			_fixedTF = new JTextField(DoubleFormat.doubleFormat(fixedValues[var], 2), 8);

			add(new JLabel(fwString(_xLabels[var], "  X (XXX) ")));
			add(_fixedTF);

			add(new JLabel(" min "));
			_minTF = new JTextField(DoubleFormat.doubleFormat(minValues[var], 2), 8);
			add(_minTF);

			add(new JLabel(" max "));
			_maxTF = new JTextField(DoubleFormat.doubleFormat(maxValues[var], 2), 8);
			add(_maxTF);

			setEnabled();
		}

		public void setEnabled() {
			// System.err.println("INDEX: " + _varIndex + " VARIES: "+
			// _whichVaries);
			_fixedTF.setEnabled(_varIndex != _whichVaries);
			_minTF.setEnabled(_varIndex == _whichVaries);
			_maxTF.setEnabled(_varIndex == _whichVaries);
		}

		public double getMinValue() {
			try {
				return Double.parseDouble(_minTF.getText());
			} catch (Exception e) {
				return minValues[_varIndex];
			}
		}

		public double getMaxValue() {
			try {
				return Double.parseDouble(_maxTF.getText());
			} catch (Exception e) {
				return maxValues[_varIndex];
			}
		}

		public double getFixedValue() {
			try {
				return Double.parseDouble(_fixedTF.getText());
			} catch (Exception e) {
				return fixedValues[_varIndex];
			}
		}

		private String fwString(String s, String targ) {
			FontMetrics fm = getFontMetrics(getFont());
			int targSW = fm.stringWidth(targ);

			String ss = new String(s);

			while (fm.stringWidth(ss) < targSW) {
				ss = " " + ss;
			}
			return ss;
		}

		@Override
		public Insets getInsets() {
			Insets def = super.getInsets();
			return new Insets(def.top + 2, def.left + 2, def.bottom + 2, def.right + 2);
		}

	}

}
