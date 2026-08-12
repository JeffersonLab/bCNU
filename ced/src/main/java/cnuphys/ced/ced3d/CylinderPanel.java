package cnuphys.ced.ced3d;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.util.OptionalDouble;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import edu.cnu.mdi.component.VerticalFlowLayout;
import edu.cnu.mdi.component.CommonBorder;

/**
 * Used to define a cylinder for the swimmer
 * @author heddle
 *
 */
public class CylinderPanel extends JPanel {

	//the text fields
	private JTextField centerLineP1TF[];
	private JTextField centerLineP2TF[];
	private JTextField radiusTF;

	//the last good values
	private double[] centerLineP1 = {0.5, 0.3, -125};
	private double[] centerLineP2 = {0.5, 0.3, 250};
	private double radius = 168; //cm

	public CylinderPanel() {
		setLayout(new VerticalFlowLayout(true, 0));

		add(p1Panel());
		add(p2Panel());
		add(radiusPanel());

		setBorder(new CommonBorder("Specify the cylinder (center line and radius)"));

	}

	//the panel for the center line P1
	private JPanel p1Panel() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));

		panel.add(fixedLabel("CL P1 (xyz)"));

		centerLineP1TF = new JTextField[3];
		for (int i = 0; i < 3; i++) {
			centerLineP1TF[i] = textField(centerLineP1[i]);
			panel.add(centerLineP1TF[i]);
		}

		panel.add(unitLabel("cm"));
		return panel;
	}

	//the panel for the center line P2
	private JPanel p2Panel() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));

		panel.add(fixedLabel("CL P2 (xyz)"));

		centerLineP2TF = new JTextField[3];
		for (int i = 0; i < 3; i++) {
			centerLineP2TF[i] = textField(centerLineP2[i]);
			panel.add(centerLineP2TF[i]);
		}

		panel.add(unitLabel("cm"));
		return panel;
	}


	//the panel for the radius
	private JPanel radiusPanel() {
		JPanel panel = new JPanel();

		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));

		panel.add(fixedLabel("radius"));

		radiusTF = textField(radius);
		panel.add(radiusTF);

		panel.add(unitLabel("cm"));
		return panel;
	}

	//create a text field
	private JTextField textField(double defVal) {
		JTextField tf = new JTextField(6);
		tf.setFont(SwimmerControlPanel.swimFont);
		tf.setText(valStr(defVal));
		return tf;
	}


	private JLabel fixedLabel(String text) {
		JLabel label = new JLabel(text);

		label.setHorizontalAlignment(SwingConstants.RIGHT);
		label.setFont(SwimmerControlPanel.swimFont);
		FontMetrics fm = this.getFontMetrics(SwimmerControlPanel.swimFont);
		int sw = fm.stringWidth(" CL P1 (xyz) ");

		Dimension d = label.getPreferredSize();
		d.width = sw;
		label.setPreferredSize(d);
		return label;
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		if (centerLineP1TF != null) {
			for (JTextField tf : centerLineP1TF) {
				tf.setEnabled(enabled);
			}
		}

		if (centerLineP2TF != null) {
			for (JTextField tf : centerLineP2TF) {
				tf.setEnabled(enabled);
			}
		}

		if (radiusTF != null) {
			radiusTF.setEnabled(enabled);
		}

	}

	//simple unit label
	private JLabel unitLabel(String us) {
		JLabel label = new JLabel(us);
		label.setFont(SwimmerControlPanel.swimFont);
		return label;
	}

	//convenience method for a string rep of a double
	private String valStr(double val) {
		String s =  String.format("%-8.3f", val);
		return s.trim();
	}


	/**
	 * get the center line p1
	 * @return the center line p1
	 */
	public double[] getCenterLineP1() {

		for (int i = 0; i < 3; i++) {
			OptionalDouble value = SwimInputValues.finiteDouble(centerLineP1TF[i].getText());
			if (value.isPresent()) {
				centerLineP1[i] = value.getAsDouble();
			} else {
				centerLineP1TF[i].setText(valStr(centerLineP1[i]));
			}
		}


		return centerLineP1;
	}

	/**
	 * get the center line p2
	 * @return the center line p2
	 */
	public double[] getCenterLineP2() {

		for (int i = 0; i < 3; i++) {
			OptionalDouble value = SwimInputValues.finiteDouble(centerLineP2TF[i].getText());
			if (value.isPresent()) {
				centerLineP2[i] = value.getAsDouble();
			} else {
				centerLineP2TF[i].setText(valStr(centerLineP2[i]));
			}
		}


		return centerLineP2;
	}

	/**
	 * Get the radius of the cylinder
	 * @returnthe radius of the cylinder
	 */
	public double getRadius() {
		OptionalDouble value = SwimInputValues.positiveDouble(radiusTF.getText());
		if (value.isPresent()) {
			radius = value.getAsDouble();
		} else {
			radiusTF.setText(valStr(radius));
		}
		return radius;
	}


}
