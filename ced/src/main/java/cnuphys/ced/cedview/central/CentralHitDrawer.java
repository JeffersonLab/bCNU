package cnuphys.ced.cedview.central;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.List;

import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.graphics.SymbolDraw;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.ced.alldata.CVTTrajectories;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.cedview.ILabCoordinates;
import cnuphys.ced.clasio.ClasIoEventManager;

public abstract class CentralHitDrawer implements IDrawable {

	private boolean _visible = true;

	// the event manager
	protected final ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();


	protected CedView _view;

	private ILabCoordinates labCoord;

	//data containers
	CVTTrajectories cvtRecTrajData = CVTTrajectories.reconstructed();
	CVTTrajectories cvtRecKFTrajData = CVTTrajectories.kalmanFilter();
	CVTTrajectories cvtP1TrajData = CVTTrajectories.pass1();

	/**
	 * Create a central hit drawer
	 * @param view the CedView
	 */
	public CentralHitDrawer(CedView view) {
		_view = view;
		labCoord = (ILabCoordinates)view;
	}

	@Override
	public boolean isVisible() {
		return _visible;
	}

	@Override
	public void setVisible(boolean visible) {
		_visible = visible;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void setEnabled(boolean enabled) {
	}

	@Override
	public void setDirty(boolean dirty) {
	}

	@Override
	public void prepareForRemoval() {
	}

	@Override
	public void draw(Graphics g, IContainer container) {

		if (_eventManager.isAccumulating()) {
			return;
		}

		Graphics2D g2 = (Graphics2D) g;
		Shape oldClip = g2.getClip();
		// clip the active area
		Rectangle sr = container.getInsetRectangle();
		g2.clipRect(sr.x, sr.y, sr.width, sr.height);

		if (_view.isSingleEventMode()) {
			if (_view.showMcTruth()) {
				drawMCTruth(g, container);
			}

			drawHitsSingleMode(g, container);
		} else {
			drawAccumulatedHits(g, container);
		}

		g2.setClip(oldClip);

	}

	//single event mode
	protected void drawHitsSingleMode(Graphics g, IContainer container) {
		drawBSTHitsSingleMode(g, container);
		drawBMTHitsSingleMode(g, container);
		drawCTOFSingleHitsMode(g, container);
		drawCNDSingleHitsMode(g, container);
		drawCVTP1Traj(g, container);
		drawCVTRecKFTraj(g, container);
		drawCVTRecTraj(g, container);
	}

	protected void drawBSTHitsSingleMode(Graphics g, IContainer container) {
	}

	protected void drawBMTHitsSingleMode(Graphics g, IContainer container) {
	}

	protected void drawCTOFSingleHitsMode(Graphics g, IContainer container) {
	}

	protected void drawCNDSingleHitsMode(Graphics g, IContainer container) {
	}

	// Pass 1 Rec
	protected void drawCVTP1Traj(Graphics g, IContainer container) {

		if (!(_view.showCVTP1Traj())) {
			return;
		}

		int count = cvtP1TrajData.count();
		if (count > 0) {
			Point pp = new Point();
			for (int i = 0; i < count; i++) {

				if (Double.isNaN(cvtP1TrajData.x(i)) || Double.isNaN(cvtP1TrajData.y(i))
						|| Double.isNaN(cvtP1TrajData.z(i))) {
					continue;
				}

				// cm to mm
				labCoord.labToLocal(container, 10 * cvtP1TrajData.x(i), 10 * cvtP1TrajData.y(i),
						10 * cvtP1TrajData.z(i), pp);
				SymbolDraw.drawStar(g, pp.x, pp.y, 6, Color.blue);
				cvtP1TrajData.setLocation(i, pp);
			}
		}
	}

	//CVT KF Traj
	protected void drawCVTRecKFTraj(Graphics g, IContainer container) {

		if (!(_view.showRecKFTraj())) {
			return;
		}

		int count = cvtRecKFTrajData.count();
		if (count > 0) {
			Point pp = new Point();
			for (int i = 0; i < count; i++) {

				if (Double.isNaN(cvtRecKFTrajData.x(i)) || Double.isNaN(cvtRecKFTrajData.y(i))
						|| Double.isNaN(cvtRecKFTrajData.z(i))) {
					continue;
				}


				// cm to mm
				labCoord.labToLocal(container, 10 * cvtRecKFTrajData.x(i), 10 * cvtRecKFTrajData.y(i),
						10 * cvtRecKFTrajData.z(i), pp);

				SymbolDraw.drawStar(g, pp.x, pp.y, 6, Color.green);
				cvtRecKFTrajData.setLocation(i, pp);
			}
		}

	}

	//CVT Rec
	protected void drawCVTRecTraj(Graphics g, IContainer container) {

		if (!(_view.showCVTRecTraj())) {
			return;
		}

		int count = cvtRecTrajData.count();
		if (count > 0) {
			Point pp = new Point();
			for (int i = 0; i < count; i++) {

				if (Double.isNaN(cvtRecTrajData.x(i)) || Double.isNaN(cvtRecTrajData.y(i))
						|| Double.isNaN(cvtRecTrajData.z(i))) {
					continue;
				}

				// cm to mm
				labCoord.labToLocal(container, 10 * cvtRecTrajData.x(i), 10 * cvtRecTrajData.y(i),
						10 * cvtRecTrajData.z(i), pp);
				SymbolDraw.drawStar(g, pp.x, pp.y, 6, Color.black);
				cvtRecTrajData.setLocation(i, pp);
			}
		}

	}



	protected void drawAccumulatedHits(Graphics g, IContainer container) {
		drawBSTAccumulatedHits(g, container);
		drawBMTAccumulateHitsHits(g, container);
		drawCTOFAccumulatedHits(g, container);
		drawCNDAccumulatedHits(g, container);
	}

	protected void drawBSTAccumulatedHits(Graphics g, IContainer container) {
	}

	protected void drawBMTAccumulateHitsHits(Graphics g, IContainer container) {
	}

	protected void drawCTOFAccumulatedHits(Graphics g, IContainer container) {
	}

	protected void drawCNDAccumulatedHits(Graphics g, IContainer container) {
	}


	protected void drawMCTruth(Graphics g, IContainer container) {
	}

	/**
	 * Mouse over feedback
	 *
	 * @param container
	 * @param screenPoint
	 * @param worldPoint
	 * @param feedbackStrings
	 */
	public void feedback(IContainer container, Point screenPoint, Point2D.Double worldPoint,
			List<String> feedbackStrings) {

		if (_view.showCVTRecTraj()) {
			for (int i = 0; i < cvtRecTrajData.count(); i++) {
				if (cvtRecTrajData.contains(i, screenPoint)) {
					cvtRecTrajData.addFeedback("CVTRecTraj", i, feedbackStrings);
					break;
				}
			}

		}

		if (_view.showRecKFTraj()) {
			for (int i = 0; i < cvtRecKFTrajData.count(); i++) {
				if (cvtRecKFTrajData.contains(i, screenPoint)) {
					cvtRecKFTrajData.addFeedback("CVTRecKFTraj", i, feedbackStrings);
					break;
				}
			}

		}




	}


}
