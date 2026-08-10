package cnuphys.ced.ced3d;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.util.OptionalDouble;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import cnuphys.bCNU.dialog.VerticalFlowLayout;
import cnuphys.bCNU.graphics.component.CommonBorder;

/**
 * Used to define a plane for the swimmer
 * @author heddle
 *
 */
public class PlanePanel extends JPanel {

	//the text fields
	private JTextField normalTF[];
	private JTextField pointTF[];

	//the last good values
	private double[] normal = {0, 0.4226, 0.9063};
	private double[] point = {0, 0, 600};


	public PlanePanel() {
		setLayout(new VerticalFlowLayout(true, 0));

		add(normalPanel());
		add(pointPanel());

		setBorder(new CommonBorder("Specify the plane (normal and point)"));
	}


	//the panel for the normal vector
	private JPanel normalPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));

		panel.add(fixedLabel("Normal (xyz)"));

		normalTF = new JTextField[3];
		for (int i = 0; i < 3; i++) {
			normalTF[i] = textField(normal[i]);
			panel.add(normalTF[i]);
		}

		panel.add(unitLabel("arbitrary"));
		return panel;
	}

	//the panel for the point in the plane
	private JPanel pointPanel() {
		JPanel panel = new JPanel();

		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));

		panel.add(fixedLabel("Point (xyz)"));

		pointTF = new JTextField[3];
		for (int i = 0; i < 3; i++) {
			pointTF[i] = textField(point[i]);
			panel.add(pointTF[i]);
		}


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

	//create a label wnd fix its width
	private JLabel fixedLabel(String text) {
		JLabel label = new JLabel(text);

		label.setHorizontalAlignment(SwingConstants.RIGHT);
		label.setFont(SwimmerControlPanel.swimFont);
		FontMetrics fm = this.getFontMetrics(SwimmerControlPanel.swimFont);
		int sw = fm.stringWidth(" Normal (xyz) ");

		Dimension d = label.getPreferredSize();
		d.width = sw;
		label.setPreferredSize(d);
		return label;
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		if (normalTF != null) {
			for (JTextField tf : normalTF) {
				tf.setEnabled(enabled);
			}
		}

		if (pointTF != null) {
			for (JTextField tf : pointTF) {
				tf.setEnabled(enabled);
			}
		}

	}

	/**
	 * get the normal vector
	 * @return the normal vector
	 */
	public double[] getNormal() {

		double[] candidate = normal.clone();
		for (int i = 0; i < 3; i++) {
			OptionalDouble value = SwimInputValues.finiteDouble(normalTF[i].getText());
			if (value.isPresent()) {
				candidate[i] = value.getAsDouble();
			} else {
				normalTF[i].setText(valStr(normal[i]));
			}
		}

		if (SwimInputValues.isNonZeroVector(candidate)) {
			normal = candidate;
		} else {
			for (int i = 0; i < 3; i++) {
				normalTF[i].setText(valStr(normal[i]));
			}
		}

		return normal;
	}

	/**
	 * get the point in the plane
	 * @return the point in the plane
	 */
	public double[] getPoint() {

		for (int i = 0; i < 3; i++) {
			OptionalDouble value = SwimInputValues.finiteDouble(pointTF[i].getText());
			if (value.isPresent()) {
				point[i] = value.getAsDouble();
			} else {
				pointTF[i].setText(valStr(point[i]));
			}
		}


		return point;
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

}
