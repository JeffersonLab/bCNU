package cnuphys.ced.cedview.allpcal;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.jlab.geom.prim.Point3D;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.view.FBData;
import cnuphys.ced.alldata.DataDrawSupport;
import cnuphys.ced.alldata.RecCalorimeter;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.frame.CedColors;
import cnuphys.ced.geometry.ECGeometry;
import cnuphys.ced.geometry.PCALGeometry;

/**
 * Rec drawer for the AllPCALView
 * @author davidheddle
 *
 */
public class PCALRecDrawer extends PCALViewDrawer {

	//the EC data container
	private static RecCalorimeter _pcReconData = RecCalorimeter.getInstance();

	// cached for feedback
	private ArrayList<FBData> _fbData = new ArrayList<>();

	public PCALRecDrawer(PCALView view) {
		super(view);
	}

	//ignore drawing or feedback
	private boolean ignore() {
		if (!_view.showRecCal() || ClasIoEventManager.getInstance().isAccumulating() || !_view.isSingleEventMode()) {
			return true;
		}

		return _pcReconData.count() == 0;
	}

	@Override
	public void draw(Graphics g, IContainer container) {
		_fbData.clear();

		if (ignore()) {
			return;
		}

		for (int index = 0; index < _pcReconData.count(); index++) {
			if (_pcReconData.isPCal(index)) drawRecCal(g, container, index, false);
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

		float x = _pcReconData.x(index);
		float y = _pcReconData.y(index);
		float z = _pcReconData.z(index);

		Point3D clasP = new Point3D(x, y, z);
		Point3D localP = new Point3D();
		PCALGeometry.getTransformations().clasToLocal(localP, clasP);

		localP.setZ(0);

		// get the right item
		_view.getHexSectorItem(_pcReconData.sector(index)).ijkToScreen(container, localP, pp);

		DataDrawSupport.drawECALRec(g, pp, highlight);

		float radius = _pcReconData.radius(index);
		if (radius > 0) {
			container.localToWorld(pp, wp);
			wr.setRect(wp.x - radius, wp.y - radius, 2 * radius, 2 * radius);
			WorldGraphicsUtilities.drawWorldOval(g, container, wr, CedColors.RECCalFill, highlight ? Color.red : null);
		}


		_fbData.add(new FBData(pp,
				String.format("$magenta$REC xyz (%-6.3f, %-6.3f, %-6.3f) cm", x, y, z),
				String.format("$magenta$REC sector: %d", _pcReconData.sector(index)),
				String.format("$magenta$REC view %s", ECGeometry.VIEW_NAMES[_pcReconData.view(index)]),
				String.format("$magenta$%s", _pcReconData.pidString(index)),
				String.format("$magenta$REC Energy %-7.4f", _pcReconData.energy(index))));

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
