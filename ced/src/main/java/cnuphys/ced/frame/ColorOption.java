package cnuphys.ced.frame;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import edu.cnu.mdi.ui.colors.ColorDialog;
import edu.cnu.mdi.ui.colors.IColorChangeListener;


public class ColorOption extends JComponent {

	// the current color
	private Color _currentColor;

	// the listener for color changes
	private IColorChangeListener _colorChangeListener;

	// the prompt label
	private String _prompt;

	// the size of the color box
	private int _rectSize = 14;

	// used for sizing
	private Dimension _size;

	private boolean _checked;


	/**
	 * Create a clickable color label.
	 *
	 * @param colorChangeListener the listener for color changes.
	 * @param intitialColor       the initial color.
	 * @param prompt              the prompt string.
	 */
	public ColorOption(IColorChangeListener colorChangeListener, Color intitialColor, String prompt) {
		_colorChangeListener = colorChangeListener;
		_currentColor = intitialColor;
		_prompt = prompt;

		final ColorOption cl = this;

		MouseAdapter ma = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {

				if (e.isControlDown() || e.isMetaDown()) {
					Color selectedColor = ColorDialog.showDialog(SwingUtilities.getWindowAncestor(cl),
							_currentColor, false, false);

					if (!selectedColor.equals(_currentColor)) {
						_currentColor = selectedColor;
						cl.repaint();
						_colorChangeListener.colorChanged(cl, getColor());
					}
					return;
				}

				if (e.getClickCount() == 1) {
					_checked = !_checked;
					cl.repaint();
				}


				_colorChangeListener.colorChanged(cl, getColor());
			}
		};

		addMouseListener(ma);
	}

	/**
	 * See if it is checked
	 * @return if checked
	 */
	public boolean isChecked() {
		return _checked;
	}

	/**
	 * Set the checked flag
	 * @param checked
	 */
	public void setChecked(boolean checked) {
		_checked = checked;
	}

	public void setCurrentColor(Color color) {
		_currentColor = color;
	}

	public Color getCurrentColor() {
		return _currentColor;
	}

	@Override
	public void paintComponent(Graphics g) {
		FontMetrics fm = getFontMetrics(getFont());
		g.setFont(getFont());


		if (_checked) {

			g.setColor(Color.lightGray);
			g.fillRect(0, 0, _rectSize, _rectSize);

			g.setColor(Color.white);
			g.drawLine(1, 1, _rectSize, _rectSize);
			g.drawLine(1, _rectSize, _rectSize, 1);

			g.setColor(Color.black);
			g.drawLine(0, 1, _rectSize-1, _rectSize);
			g.drawLine(0, _rectSize, _rectSize-1, 1);

		}
		else {
			g.setColor(_currentColor);
			g.fillRect(0, 0, _rectSize, _rectSize);
		}
		g.setColor(Color.black);
		g.drawRect(0, 0, _rectSize, _rectSize);
		g.setColor(Color.black);

		g.drawString(_prompt, _rectSize + 6, fm.getHeight() - 4);
	}

	@Override
	public Dimension getPreferredSize() {
		if (_size == null) {
			return super.getPreferredSize();
		}
		return _size;
	}

	/**
	 * Return the current color. Null if checked.
	 *
	 * @return the current color.
	 */
	public Color getColor() {
		return _checked ? OrderColors.TRANSCOLOR : _currentColor;
	}





}
