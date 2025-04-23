package cnuphys.ced.cedview.central;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.magneticfield.swim.ASwimTrajectoryDrawer;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.lund.LundId;
import cnuphys.swim.SwimTrajectory;
import cnuphys.swim.SwimTrajectory2D;

public class SwimTrajectoryDrawerZ extends ASwimTrajectoryDrawer {

	private CentralZView _view;

	public SwimTrajectoryDrawerZ(CentralZView bsTzView) {
		_view = bsTzView;
	}

	/**
	 * Actual drawing method
	 *
	 * @param g         the graphics context
	 * @param container the base container
	 */
	@Override
	public void draw(Graphics g, IContainer container) {
		if (!ClasIoEventManager.getInstance().isAccumulating()) {

			Rectangle sr = container.getInsetRectangle();
			Graphics2D g2 = (Graphics2D) g;

			Shape oldClip = g2.getClip();

			g2.clipRect(sr.x, sr.y, sr.width, sr.height);
			super.draw(g, container);
			g2.setClip(oldClip);

		}
	}


	/**
	 * From detector xyz get the projected world point.
	 *
	 * @param v3d the 3D vector (meters)
	 * @param wp  the projected world point.
	 */
	@Override
	public void project(double[] v3d, Point2D.Double wp) {

		// the path in the 3D traj is in meters. We want mm.
		_view.labToWorld(10 * v3d[0], 10 * v3d[1], 10 * v3d[2], wp);
	}

	@Override
	public void prepareForRemoval() {
	}

	@Override
	public void setDirty(boolean dirty) {
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	public void setVisible(boolean visible) {
	}

	@Override
	public boolean acceptSimpleTrack(SwimTrajectory2D trajectory) {
		// this is a fugly hack. Check to see if it is hit based ot time based
		// then check the display flags
		LundId lid = trajectory.getTrajectory3D().getLundId();
		int id = lid.getId();

		// FUGLY hack
		if ((id == -99) || (id == -100) || (id == -101)) { // time based
			return _view.showTB();
		} else if ((id == -199) || (id == -200) || (id == -201)) { // hitbased based
			return _view.showHB();
		}

		return true;
	}

}
