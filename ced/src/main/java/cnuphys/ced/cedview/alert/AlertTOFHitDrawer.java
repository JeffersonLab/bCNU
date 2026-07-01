package cnuphys.ced.cedview.alert;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;

import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.ced.alldata.DataWarehouse;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.geometry.alert.AlertGeometry;
import cnuphys.ced.geometry.alert.TOFLayer;

/**
 * Draws ALERT TOF event and accumulated hits in the ALERT XY view.
 * <p>
 * This class uses paddle indices rather than live geometry-service
 * {@code ScintillatorPaddle} objects so it works both after CCDB initialization
 * and after geometry-cache reads.
 */
public class AlertTOFHitDrawer {

	/** Data warehouse. */
	private DataWarehouse _dataWarehouse = DataWarehouse.getInstance();

	/** The ALERT XY view. */
	private AlertXYView _view;

	/**
	 * Create a TOF hit drawer for the ALERT view.
	 *
	 * @param view the ALERT XY view
	 */
	public AlertTOFHitDrawer(AlertXYView view) {
		_view = view;
	}

	/**
	 * Draw the hits for the current event.
	 *
	 * @param g         the graphics context
	 * @param container the drawing container
	 */
	public void drawHits(Graphics g, IContainer container) {
		if (ClasIoEventManager.getInstance().isAccumulating()) {
			return;
		}

		DataEvent dataEvent = ClasIoEventManager.getInstance().getCurrentEvent();
		if (dataEvent == null) {
			return;
		}

		drawTOFHits(g, container, dataEvent);
	}

	/**
	 * Draw the TOF hits.
	 *
	 * @param g         the graphics context
	 * @param container the drawing container
	 * @param dataEvent the data event
	 */
	private void drawTOFHits(Graphics g, IContainer container, DataEvent dataEvent) {
		if (dataEvent.hasBank("ATOF::tdc") && _view.showADCHits()) {
			short component[] = _dataWarehouse.getShort("ATOF::tdc", "component");

			if (component != null) {
				int count = component.length;

				if (count > 0) {
					byte sector[] = _dataWarehouse.getByte("ATOF::tdc", "sector");
					byte layer[] = _dataWarehouse.getByte("ATOF::tdc", "layer");
					byte order[] = _dataWarehouse.getByte("ATOF::tdc", "order");

					AlertTOFGeometryNumbering tdcGeom = new AlertTOFGeometryNumbering();

					for (int i = 0; i < count; i++) {
						tdcGeom.fromHipoNumbering(sector[i], layer[i], component[i], order[i]);

						TOFLayer tofl = AlertGeometry.getTOFLayer(tdcGeom.sector, tdcGeom.superlayer, tdcGeom.layer);

						if (tofl == null) {
							System.err.println("TOF layer not found for sector " + tdcGeom.sector + ", superlayer "
									+ tdcGeom.superlayer + ", layer " + tdcGeom.layer);
							continue;
						}

						tofl.drawPaddle(g, container, tdcGeom.paddleIndex, Color.red, Color.black);
					}
				}
			}
		}
	}

	/**
	 * Draw the highlighted hit from the TDC bank.
	 *
	 * @param g         the graphics context
	 * @param container the drawing container
	 * @param dataEvent the data event
	 * @param index     the 0-based hit index
	 */
	public void drawHighlightHit(Graphics g, IContainer container, DataEvent dataEvent, int index) {
		if (dataEvent.hasBank("ATOF::tdc") && _view.showADCHits()) {
			short component[] = _dataWarehouse.getShort("ATOF::tdc", "component");

			if (component != null) {
				int count = component.length;

				if (count > index) {
					byte sector[] = _dataWarehouse.getByte("ATOF::tdc", "sector");
					byte compLayer[] = _dataWarehouse.getByte("ATOF::tdc", "layer");
					byte order[] = _dataWarehouse.getByte("ATOF::tdc", "order");

					AlertTOFGeometryNumbering tdcGeom = new AlertTOFGeometryNumbering();
					tdcGeom.fromHipoNumbering(sector[index], compLayer[index], component[index], order[index]);

					TOFLayer tofl = AlertGeometry.getTOFLayer(tdcGeom.sector, tdcGeom.superlayer, tdcGeom.layer);

					if (tofl != null) {
						tofl.drawPaddle(g, container, tdcGeom.paddleIndex, Color.orange, Color.black);
					}
				}
			}
		}
	}

	/**
	 * Draw accumulated TOF hits.
	 *
	 * @param g         the graphics context
	 * @param container the drawing container
	 */
	public void drawAccumulatedHits(Graphics g, IContainer container) {
		drawAccumulatedHitsSL(g, container, 0);
		drawAccumulatedHitsSL(g, container, 1);
	}

	/**
	 * Draw accumulated hits for one superlayer.
	 *
	 * @param g          the graphics context
	 * @param container  the drawing container
	 * @param superlayer the 0-based superlayer
	 */
	private void drawAccumulatedHitsSL(Graphics g, IContainer container, int superlayer) {
		int maxHit;
		int counts[][][];

		if (superlayer == 0) {
			maxHit = AccumulationManager.getInstance().getMaxAlertTOFSL0Count();
			counts = AccumulationManager.getInstance().getAccumulatedAlertTOFSL0Data();
		} else {
			maxHit = AccumulationManager.getInstance().getMaxAlertTOFSL1Count();
			counts = AccumulationManager.getInstance().getAccumulatedAlertTOFSL1Data();
		}

		if (counts == null) {
			return;
		}

		for (int sector = 0; sector < counts.length; sector++) {
			for (int layer = 0; layer < counts[sector].length; layer++) {
				TOFLayer tofl = AlertGeometry.getTOFLayer(sector, superlayer, layer);

				if (tofl == null) {
					continue;
				}

				for (int paddle = 0; paddle < counts[sector][layer].length; paddle++) {
					double count = counts[sector][layer][paddle];
					double fract = (maxHit == 0) ? 0 : (count / maxHit);
					Color color = AccumulationManager.getInstance().getColor(_view.getColorScaleModel(), fract);

					tofl.drawPaddle(g, container, paddle, color, Color.black);
				}
			}
		}
	}

	/**
	 * Get feedback strings for a hit.
	 *
	 * @param container       the drawing container
	 * @param pp              the pixel point
	 * @param wp              the world point
	 * @param tofl            the TOF layer
	 * @param feedbackStrings the feedback list
	 */
	public void getHitFeedbackStrings(IContainer container, Point pp, Point2D.Double wp, TOFLayer tofl,
			List<String> feedbackStrings) {
		if (ClasIoEventManager.getInstance().isAccumulating() || _view.isAccumulatedMode()) {
			return;
		}

		DataEvent dataEvent = ClasIoEventManager.getInstance().getCurrentEvent();

		if ((dataEvent == null) || !dataEvent.hasBank("ATOF::tdc")) {
			return;
		}

		short component[] = _dataWarehouse.getShort("ATOF::tdc", "component");

		if (component != null) {
			int count = component.length;

			if (count > 0) {
				byte sector[] = _dataWarehouse.getByte("ATOF::tdc", "sector");
				byte compLayer[] = _dataWarehouse.getByte("ATOF::tdc", "layer");
				byte order[] = _dataWarehouse.getByte("ATOF::tdc", "order");

				AlertTOFGeometryNumbering tdcGeom = new AlertTOFGeometryNumbering();

				for (int i = 0; i < count; i++) {
					tdcGeom.fromHipoNumbering(sector[i], compLayer[i], component[i], order[i]);

					if (tdcGeom.match(tofl)) {
						boolean contains = tofl.paddleContains(tdcGeom.paddleIndex, pp);

						if (contains) {
							String bankName = "ATOF::tdc";
							AlertFeedbackSupport.handleInt(bankName, "TDC", i, "$orange$", feedbackStrings);
							AlertFeedbackSupport.handleByte(bankName, "order", i, "$orange$", feedbackStrings);
							AlertFeedbackSupport.handleInt(bankName, "ToT", i, "$orange$", feedbackStrings);
							return;
						}
					}
				}
			}
		}
	}
}