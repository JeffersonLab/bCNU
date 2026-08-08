package cnuphys.ced.cedwindow;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.ClasIoEventListenerPhase;
import cnuphys.ced.clasio.IClasIoEventListener;

/**
 * A window that can be dragged and resized.
 * This is an alternative to a view, which is confined to
 * the ced desktop.. This is a free floating window,
 */
public abstract class CedWindow extends JFrame implements WindowListener, IClasIoEventListener {
    // Initial mouse x and y coordinates
    private int mouseX, mouseY;


	// the clasIO event manager
	protected ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();


	/**
	 * Create a draggable and resizable window
	 * @param title
	 */
    public CedWindow(String title) {
        super(title);

        // Implement window dragging
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Record the initial positions when mouse is pressed
                mouseX = e.getX();
                mouseY = e.getY();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                // New location is current location plus the mouse movement
                int x = getLocation().x + e.getX() - mouseX;
                int y = getLocation().y + e.getY() - mouseY;
                setLocation(x, y);
            }
        });

        // Add WindowListener
        addWindowListener(this);

        //add event listener
		_eventManager.addClasIoEventListener(this, ClasIoEventListenerPhase.VIEW);
   }

    // WindowListener methods
    @Override
	public void windowActivated(WindowEvent e) {}
    @Override
	public void windowClosed(WindowEvent e) {}
    @Override
	public void windowClosing(WindowEvent e) {}
    @Override
	public void windowDeactivated(WindowEvent e) {}
    @Override
	public void windowDeiconified(WindowEvent e) {}
    @Override
	public void windowIconified(WindowEvent e) {}
    @Override
	public void windowOpened(WindowEvent e) {}

}
