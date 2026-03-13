package cnuphys.bCNU.view;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * A class to hold a configuration for a view primarily so that it can be
 * created lazily. This is used by the view manager to create views lazily.
 */
public class ViewConfiguration<T extends BaseView> {
	
	/** The class of the view to be created. This is needed to use reflection to call the static construct method. */
	private final Class<T> clazz;

	/** If true, the view will be created lazily. If false, the view will be created immediately. */
	public final boolean lazily;
	
	/** The virtual column in which the view should be placed. */
	public final int column; 
	
	/** the horizontal offset from the constraint placement (often 0) */
	public final int dh;
	
	/** the vertical offset from the constraint placement (often 0) */
	public final int dv;
	
	/** The constraint for the view placement, such as VirtualView.CENTER. */
	public final int constraint;
	
	/** The key-value pairs used to create the view. */
	public final Object[] keyVals;
	
	/** The view instance, which may be null if lazily is true and the view has not been created yet. */
	private T  view;
	
	public int menuIndex = -1; // index in the lazily created menu, if applicable
	
	
	
	public ViewConfiguration(Class<T> clazz, boolean lazily, int column, int dh, int dv, int constraint,
			Object... keyVals) {
		this.clazz = clazz;
		this.lazily = lazily;
		this.column = column;
		this.dh = dh;
		this.dv = dv;
		this.constraint = constraint;
		this.keyVals = keyVals;
	}
	
	/** * Create the view instance based on the keyVals using reflection to call the 
	 * static construct(Object... keyVals) method.
	 */
	private T realizeView() {
	    if (view != null) return view;

	    try {
	        // T is the specific subclass of BaseView
	        // We expect a static method: public static T construct(Object... keyVals)
	        java.lang.reflect.Method method = null;
	        
	        // Search for the construct method in the class hierarchy
	        // This assumes the class 'T' has been provided or inferred
	        // Assuming you add a 'Class<T> clazz' field to ViewConfiguration:
	        method = clazz.getDeclaredMethod("construct", Object[].class);
	        view = (T) method.invoke(null, (Object) keyVals);
	        
	        JMenu viewMenu = ViewManager.getInstance().getViewMenu();
	        int index = viewMenu.getItemCount()-1; // add to end of menu
	        JMenuItem menuItem = viewMenu.getItem(index);
	        viewMenu.remove(index);
	        viewMenu.insert(menuItem, menuIndex+1); // put it back in the correct place
	        
	        // If you don't have the Class object, you'll need to pass the view class 
	        // type to the ViewConfiguration constructor.
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    if (view != null) {
	        VirtualView vv = VirtualView.getInstance();
	        if (vv != null) {
	        	vv.moveTo(view, column, dh, column, constraint);
	        }
	    }
	    return view;
	}
	
	/** Get the view instance, creating it if necessary. */
	public T getView() {
		if (lazily && view == null) {
			view = realizeView();
		}
		return view;
	}
	
	
}
