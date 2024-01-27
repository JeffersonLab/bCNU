package cnuphys.ced.cedview.allec;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.jlab.geom.prim.Point3D;
import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.view.FBData;
import cnuphys.ced.alldata.datacontainer.cal.ECalReconData;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.data.DataDrawSupport;
import cnuphys.ced.frame.CedColors;
import cnuphys.ced.geometry.ECGeometry;
import cnuphys.lund.LundId;

/**
 * Rec drawer for the AllECView
 *
 * @author davidheddle
 *
 */
public class ECRecDrawer extends ECViewDrawer {
	
	//the EC data container
	private static ECalReconData _ecReconData = ECalReconData.getInstance();

	// the current event
	private DataEvent _currentEvent;

	// cached for feedback
	private ArrayList<FBData> _fbData = new ArrayList<>();

	public ECRecDrawer(ECView view) {
		super(view);
	}

	// ignore drawing or feedback
	private boolean ignore() {
		if (!_view.showRecCal() || ClasIoEventManager.getInstance().isAccumulating()|| !_view.isSingleEventMode()) {
			return true;
		}

		_currentEvent = _eventManager.getCurrentEvent();
		if ((_currentEvent == null) || !_currentEvent.hasBank("REC::Calorimeter")) {
			return true;
		}

		return false;
	}

	@Override
	public void draw(Graphics g, IContainer container) {
		_fbData.clear();

		if (ignore()) {
			return;
		}

		for (int index = 0; index < _ecReconData.count(); index++) {
			drawRecCal(g, container, index, false);
		}

	}

	/**
	 * Draw a single RecCal "cluster"
	 * @param g
	 * @param container
	 * @param index
	 * @param highlight
	 */
	public void drawRecCal(Graphics g, IContainer container, int index, boolean highlight) {
		
		Point pp = new Point();
		Rectangle2D.Double wr = new Rectangle2D.Double();
		Point2D.Double wp = new Point2D.Double();


		int plane = _ecReconData.plane.get(index);
		if (plane != _view.getDisplayPlane()) {
			return;
		}
		float x = _ecReconData.x.get(index);
		float y = _ecReconData.y.get(index);
		float z = _ecReconData.z.get(index);

		Point3D clasP = new Point3D(x, y, z);
		Point3D localP = new Point3D();
		ECGeometry.getTransformations(plane).clasToLocal(localP, clasP);

		localP.setZ(0);

		// get the right item
		_view.getHexSectorItem(_ecReconData.sector.get(index)).ijkToScreen(container, localP, pp);

		DataDrawSupport.drawECALRec(g, pp, highlight);

		float radius = _ecReconData.getRadius(_ecReconData.energy.get(index));
		if (radius > 0) {
			container.localToWorld(pp, wp);
			wr.setRect(wp.x - radius, wp.y - radius, 2 * radius, 2 * radius);
			WorldGraphicsUtilities.drawWorldOval(g, container, wr, CedColors.RECCalFill, highlight ? Color.red : null);
		}


		_fbData.add(new FBData(pp,
				String.format("$magenta$REC xyz (%-6.3f, %-6.3f, %-6.3f) cm", x, y, z),
				String.format("$magenta$REC sector: %d", _ecReconData.sector.get(index)),
				String.format("$magenta$REC plane %s", ECGeometry.PLANE_NAMES[_ecReconData.plane.get(index)]),
				String.format("$magenta$REC view %s", ECGeometry.VIEW_NAMES[_ecReconData.view.get(index)]),
				String.format("$magenta$%s", _ecReconData.getPIDStr(index)),
				String.format("$magenta$REC Energy %-7.4f", _ecReconData.energy.get(index))));

	}

	/**
	 * Use what was drawn to generate feedback strings
	 *
	 * @param container       the drawing container
	 * @param screenPoint     the mouse location
	 * @param worldPoint      the corresponding world location
	 * @param feedbackStrings add strings to this collection
	 */
	@Override
	public void feedback(IContainer container, Point screenPoint, Point2D.Double worldPoint,
			List<String> feedbackStrings) {

		if (ignore()) {
			return;
		}

		for (FBData fbdata : _fbData) {
			boolean added = fbdata.addFeedback(screenPoint, feedbackStrings);
			if (added) {
				break;
			}
		}

	}

}