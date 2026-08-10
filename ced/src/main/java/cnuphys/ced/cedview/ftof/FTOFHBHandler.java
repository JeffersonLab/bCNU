package cnuphys.ced.cedview.ftof;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.List;

import cnuphys.ced.alldata.DataDrawSupport;
import cnuphys.ced.alldata.FTOFHBHits;
import cnuphys.bCNU.graphics.container.IContainer;

/** Draws and supplies feedback for {@code FTOF::hbhits}. */
public class FTOFHBHandler {

	private static final int FEEDBACK_SIZE = 16;
	private final Rectangle feedbackBounds = new Rectangle();
	private final Point localPoint = new Point();
	private final Point2D.Double worldPoint = new Point2D.Double();
	private final FTOFView view;

	public FTOFHBHandler(FTOFView view) {
		this.view = view;
	}

	public void draw(Graphics g, IContainer container) {
		if (!view.isSingleEventMode() || !view.showHB()) return;

		FTOFHBHits hits = FTOFHBHits.getInstance();
		for (int i = 0; i < hits.count(); i++) {
			if (hits.hasValidGeometry(i) && hits.layer(i) - 1 == view.displayPanel()) {
				worldPoint.setLocation(hits.x(i), hits.y(i));
				container.worldToLocal(localPoint, worldPoint);
				DataDrawSupport.drawHBHit(g, localPoint);
			}
		}
	}

	public void getFeedbackStrings(IContainer container, int sector, int panel, int paddleId, Point pp,
			Point2D.Double wp, List<String> feedbackStrings) {
		if (!view.isSingleEventMode() || !view.showHB()) return;

		FTOFHBHits hits = FTOFHBHits.getInstance();
		for (int i = 0; i < hits.count(); i++) {
			if (hits.hasValidGeometry(i) && hits.sector(i) == sector && hits.layer(i) - 1 == panel) {
				worldPoint.setLocation(hits.x(i), hits.y(i));
				container.worldToLocal(localPoint, worldPoint);
				feedbackBounds.setBounds(localPoint.x - FEEDBACK_SIZE / 2,
						localPoint.y - FEEDBACK_SIZE / 2, FEEDBACK_SIZE, FEEDBACK_SIZE);
				if (feedbackBounds.contains(pp)) {
					hits.addFeedback(i, feedbackStrings);
					return;
				}
			}
		}
	}
}
