package cnuphys.bCNU.graphics.toolbar;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import cnuphys.bCNU.component.MagnifyWindow;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.util.Bits;
import cnuphys.bCNU.util.Fonts;

/**
 * @author heddle
 *
 */
@SuppressWarnings("serial")
public class BaseToolBar extends JToolBar implements MouseListener, MouseMotionListener {

	public static final int RANGEBUTTON        = 040;
	public static final int TEXTFIELD          = 0200;
	public static final int USERCOMPONENT      = 0400; // user (app) provides drawing
	public static final int MAGNIFYBUTTON      = 010000;

	public static final int CLONEBUTTON        = 040000;

	// used to eliminate some basic buttons
	public static final int CENTERBUTTON       = 040000000;

	public static final int EVERYTHING = RANGEBUTTON | TEXTFIELD | USERCOMPONENT | MAGNIFYBUTTON | CENTERBUTTON;

	/**
	 * Text field used for messages
	 */
	private JTextField _textField;

	// default pointer tool
	private PointerButton _pointerButton;

	// rubber-band zoom
	private BoxZoomButton _boxZoomButton;

	// magnifying glass
	private MagnifyButton _magnifyButton;

	// the owner container
	private IContainer _container;

	// user component
	private UserToolBarComponent _userComponent;

	private final ButtonGroup _buttonGroup = new ButtonGroup();

	private final ActionListener _toggleActionListener = event -> activeToggleButtonChanged();

	/**
	 * Create a toolbar with all the buttons.
	 *
	 * @param container the container this toolbar controls.
	 */
	public BaseToolBar(IContainer container) {
		this(container, EVERYTHING);
	}

	/**
	 * Create a tool bar.
	 *
	 * @param container the container this toolbar controls.
	 * @param bits      controls which tools are added.
	 */
	public BaseToolBar(IContainer container, int bits) {
		// box layout needed for user component to work
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		_container = container;
		_container.setToolBar(this);
		makeButtons(bits);

		Component c = _container.getComponent();
		if (c != null) {
			c.addMouseListener(this);
			c.addMouseMotionListener(this);
		}
		setBorder(BorderFactory.createEtchedBorder());
		setFloatable(false);
	}

	/**
	 * Makes all the buttons.
	 *
	 * @param bits Bitwise test of which annotation buttons to add.
	 */
	protected void makeButtons(int bits) {

		ZoomInButton zoomInButton = new ZoomInButton(_container);
		ZoomOutButton zoomOutButton = new ZoomOutButton(_container);
		WorldButton worldButton = new WorldButton(_container);
		_boxZoomButton = new BoxZoomButton(_container);
		RefreshButton refreshButton = new RefreshButton(_container);
		CenterButton centerButton = null;
		if (Bits.checkBit(bits, CENTERBUTTON)) {
			centerButton = new CenterButton(_container);
		}

		CloneButton cloneButton = null;
		if (Bits.checkBit(bits, CLONEBUTTON)) {
			cloneButton = new CloneButton(_container);
		}

		RangeButton rangeButton = null;
		if (Bits.checkBit(bits, RANGEBUTTON)) {
			rangeButton = new RangeButton(_container);
		}

		if (Bits.checkBit(bits, MAGNIFYBUTTON)) {
			_magnifyButton = new MagnifyButton(_container);
		}

		_pointerButton = new PointerButton(_container);

		// add the pointer button and make it the default
		add(_pointerButton);

		add(_boxZoomButton);
		add(zoomInButton);
		add(zoomOutButton);
		if (_magnifyButton != null) {
			add(_magnifyButton);
		}
		if (centerButton != null) {
			add(centerButton);
		}
		add(worldButton);
		if (rangeButton != null) {
			add(rangeButton);
		}
		add(refreshButton);
		if (cloneButton != null) {
			add(Box.createHorizontalStrut(8));
			add(cloneButton);
		}

		// add the text field?

		if (Bits.checkBit(bits, TEXTFIELD)) {

			_textField = new JTextField(" ");

			_textField.setFont(Fonts.commonFont(Font.PLAIN, 11));
			_textField.setEditable(false);
			_textField.setBackground(Color.black);
			_textField.setForeground(Color.cyan);

			FontMetrics fm = getFontMetrics(_textField.getFont());
			Dimension d = _textField.getPreferredSize();
			d.width = fm.stringWidth(" ( 9999.99999 , 9999.99999 ) XXXXXXXXXXX");
			_textField.setPreferredSize(d);
			_textField.setMaximumSize(d);

			add(_textField);
		}

		// if user component, add last

		if (Bits.checkBit(bits, USERCOMPONENT)) {
			addSeparator();
			_userComponent = new UserToolBarComponent(_container);
			add(_userComponent);
		}

		resetDefaultSelection();

	}

	public void add(JToggleButton toggleButton) {
		if (toggleButton != null) {
			super.add(toggleButton);
			_buttonGroup.add(toggleButton);
			toggleButton.addActionListener(_toggleActionListener);
		}
	}

	/**
	 * Sets the text in the text field widget.
	 *
	 * @param text the new text.
	 */
	public void setText(String text) {
		if (_textField == null) {
			return;
		}

		if (text == null) {
			_textField.setText("");
		} else {
			_textField.setText(text);
		}
	}

	/**
	 * Reset the default toggle button selection
	 */
	public void resetDefaultSelection() {
		_pointerButton.doClick();
		_pointerButton.setSelected(true);
		_container.getComponent().setCursor(_pointerButton.canvasCursor());
	}

	/**
	 * Get the toolbar's point button.
	 *
	 * @return the toolbar's pointer button.
	 */
	public PointerButton getPointerButton() {
		return _pointerButton;
	}

	/**
	 * The mouse was clicked. Note that the order the events will come is PRESSED,
	 * RELEASED, CLICKED. And a CLICKED will happen only if the mouse was not moved
	 * between press and release.
	 *
	 * @param mouseEvent the causal event.
	 */
	@Override
	public void mouseClicked(MouseEvent mouseEvent) {

		if (!_container.getComponent().isEnabled()) {
			Toolkit.getDefaultToolkit().beep();
			return;
		}

		ToolBarToggleButton mtb = getActiveButton();
		if (mtb == null) {
			return;
		}

		boolean mb1 = (mouseEvent.getButton() == MouseEvent.BUTTON1) && !mouseEvent.isControlDown();
		if (mb1) {
			if (mouseEvent.getClickCount() == 1) { // single click
				mtb.mouseClicked(mouseEvent);
			} else { // double (or more) clicks
				mtb.mouseDoubleClicked(mouseEvent);
			}
		}

	}

	/**
	 * The mouse has entered the container.
	 *
	 * @param mouseEvent the causal event.
	 */
	@Override
	public void mouseEntered(MouseEvent mouseEvent) {

		ToolBarToggleButton mtb = getActiveButton();
		if (mtb != null) {
			_container.getComponent().setCursor(mtb.canvasCursor());
			mtb.mouseEntered(mouseEvent);
		}
	}

	/**
	 * The mouse has exited the container.
	 *
	 * @param mouseEvent the causal event.
	 */
	@Override
	public void mouseExited(MouseEvent mouseEvent) {
		ToolBarToggleButton mtb = getActiveButton();

		if (mtb == null) {
			return;
		}

		mtb.mouseExited(mouseEvent);
	}

	/**
	 * The mouse was pressed. Note that the order the events will come is PRESSED,
	 * RELEASED, CLICKED. And a CLICKED will happen only if the mouse was not moved
	 * between press and release.
	 *
	 * @param me the causal event.
	 */
	@Override
	public void mousePressed(MouseEvent me) {

		if (!_container.getComponent().isEnabled()) {
			return;
		}

		ToolBarToggleButton mtb = getActiveButton();

		if (mtb == null) {
			return;
		}

		switch (me.getClickCount()) {
		case 1:

			// hack, if mouse button 2
			if (mtb == _pointerButton) {
				if ((_boxZoomButton != null) && (me.getButton() == MouseEvent.BUTTON2)) {
					mtb = _boxZoomButton;
				}
			}

			mtb.mousePressed(me);
			break;
		}

	}

	/**
	 * The mouse was clicked. Note that the order the events will come is PRESSED,
	 * RELEASED, CLICKED. And a CLICKED will happen only if the mouse was not moved
	 * between press and release. Also, the RELEASED will come even if the mouse was
	 * dragged off the container.
	 *
	 * @param me the causal event.
	 */
	@Override
	public void mouseReleased(MouseEvent me) {
		if (!_container.getComponent().isEnabled()) {
			return;
		}

		ToolBarToggleButton mtb = getActiveButton();

		if (mtb == null) {
			return;
		}

		// hack, if mouse button 2 treat as box zoom
		if (mtb == _pointerButton) {
			if (me.getButton() == MouseEvent.BUTTON2) {
				mtb = _boxZoomButton;
			}
		}

		mtb.mouseReleased(me);

	}

	/**
	 *
	 * @param mouseEvent the causal event.
	 */
	@Override
	public void mouseDragged(MouseEvent mouseEvent) {
		if (!_container.getComponent().isEnabled()) {
			return;
		}

		ToolBarToggleButton mtb = getActiveButton();

		if (mtb == null) {
			return;
		}

		mtb.mouseDragged(mouseEvent);
	}

	/**
	 * The mouse has moved. Note will not come here if mouse button pressed, will go
	 * to DRAG instead.
	 *
	 * @param me the causal event.
	 */
	@Override
	public void mouseMoved(MouseEvent me) {
		ToolBarToggleButton mtb = getActiveButton();

		if (mtb == null) {
			return;
		}

		mtb.mouseMoved(me);
	}

	/**
	 * Convenience routine to get the active button.
	 *
	 * @return the active toggle button.
	 */
	public ToolBarToggleButton getActiveButton() {
		try {
			for (Enumeration<AbstractButton> buttons = _buttonGroup.getElements(); buttons.hasMoreElements();) {
				AbstractButton button = buttons.nextElement();
				if (button.isSelected()) {
					return (ToolBarToggleButton) button;
				}
			}
		} catch (RuntimeException e) {
			Log.getInstance().exception(e);
		}
		return null;
	}

	/**
	 * Get the user component, on which the app might draw stuff
	 *
	 * @return the userComponent
	 */
	public UserToolBarComponent getUserComponent() {
		return _userComponent;
	}

	/**
	 * The active toggle button has changed
	 */
	protected void activeToggleButtonChanged() {
		if (getActiveButton() != _magnifyButton) {
			MagnifyWindow.closeMagnifyWindow();
		}
		if (_container != null) {
			_container.activeToolBarButtonChanged(getActiveButton());
		}
	}

}
