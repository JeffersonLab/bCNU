package cnuphys.bCNU.component;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.util.Random;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class VariableRange extends JPanel {

	//shared random number generator
	private static Random _rand = new Random();

	//to make the prompts uniform width
	private int _measureWidth;

	//the limit value text fields
	private JTextField _minValue;
	private JTextField _maxValue;

	//cache last good values for bad entry recovery
	private double _lastGoodMin;
	private double _lastGoodMax;



	public VariableRange(String prompt, String units, String measureString, Font font, double minVal, double maxVal) {
		setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));

		_lastGoodMin = minVal;
		_lastGoodMax = maxVal;


		FontMetrics fm = this.getFontMetrics(font);
		_measureWidth = Math.max(20, fm.stringWidth(measureString));

		add(createPrompt(prompt, font));

		_minValue = new JTextField(valStr(minVal), 7);
		_maxValue = new JTextField(valStr(maxVal), 7);

		_minValue.setFont(font);
		_maxValue.setFont(font);


		add (_minValue);
		add (makeLabel(" to ", font));
		add (_maxValue);
		add (makeLabel(units, font));

	}

	//create the prompt as wide as the measure width
	private JLabel createPrompt(String prompt, Font font) {
		JLabel label;

		label = new JLabel(prompt, SwingConstants.RIGHT) {

			@Override
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				d.width = _measureWidth;
				return d;
			}
		};

		label.setFont(font);

		return label;
	}

	private JLabel makeLabel(String s, Font font) {
		JLabel label = new JLabel(s);
		label.setFont(font);
		return label;
	}


	private String valStr(double v) {
		String s = String.format("%-9.3f", v);
		return s.trim();
	}

	//get the min value, watch for bad input
	private double getMinValue() {
		double value = finiteOrDefault(_minValue.getText(), _lastGoodMin);
		if (value == _lastGoodMin && !isFiniteDouble(_minValue.getText())) {
			_minValue.setText(valStr(_lastGoodMin));
		}
		_lastGoodMin = value;
		return value;
	}

	//get the max value, watch for bad input
	private double getMaxValue() {
		double value = finiteOrDefault(_maxValue.getText(), _lastGoodMax);
		if (value == _lastGoodMax && !isFiniteDouble(_maxValue.getText())) {
			_maxValue.setText(valStr(_lastGoodMax));
		}
		_lastGoodMax = value;
		return value;
	}

	/**
	 * Get a random number corresponding to the range
	 * @return a random number corresponding to the range
	 */
	public double nextRandom() {
		return nextRandom(_rand);
	}

	/**
	 * Get a random number corresponding to the range using the supplied generator.
	 *
	 * @param random random number generator
	 * @return a value in the configured range
	 */
	public double nextRandom(Random random) {
		double minV = getMinValue();
		double maxV = getMaxValue();
		return randomBetween(minV, maxV, random);
	}

	static double randomBetween(double minimum, double maximum, Random random) {
		if (Math.abs(minimum - maximum) < 1.0e-16) {
			return minimum;
		}

		return minimum + (maximum - minimum) * random.nextDouble();
	}

	static double finiteOrDefault(String text, double defaultValue) {
		if (text == null) {
			return defaultValue;
		}
		try {
			double value = Double.parseDouble(text.trim());
			return Double.isFinite(value) ? value : defaultValue;
		} catch (NumberFormatException exception) {
			return defaultValue;
		}
	}

	private static boolean isFiniteDouble(String text) {
		if (text == null) {
			return false;
		}
		try {
			return Double.isFinite(Double.parseDouble(text.trim()));
		} catch (NumberFormatException exception) {
			return false;
		}
	}


}
