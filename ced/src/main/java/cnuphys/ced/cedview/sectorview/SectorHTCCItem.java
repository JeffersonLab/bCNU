package cnuphys.ced.cedview.sectorview;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.item.ItemList;
import cnuphys.bCNU.item.PolygonItem;
import cnuphys.ced.alldata.CherenkovAdc;
import cnuphys.ced.alldata.CherenkovTdc;
import cnuphys.ced.alldata.DataDrawSupport;
import cnuphys.ced.alldata.HTCCRecHits;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.common.SuperLayerDrawing;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.geometry.GeometryManager;
import cnuphys.ced.geometry.HTCCGeometry;

public class SectorHTCCItem extends PolygonItem {

	// convenient access to the event manager
	private ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();

	//data containers
	private CherenkovAdc adcData = CherenkovAdc.htcc();
	private CherenkovTdc tdcData = CherenkovTdc.htcc();
	private HTCCRecHits recData = HTCCRecHits.getInstance();

	// sector 1-based 1..6
	private byte _sector;

	// 1-based half, 1..2 (hipo: layer)
	private byte _half;

	// 1-based ring 1..4 (hipo: component)
	private short _ring;

	// cache the outline
	private Point2D.Double[] _cachedWorldPolygon = GeometryManager.allocate(4);

	// the view this item lives on.
	private SectorView _view;

	/**
	 * Create a HTCC item for the sector view. Note, no points are added in
	 * the constructor. The points will always be supplied by the setPoints method,
	 * which will send projected wire positions (with a border of guard wires)
	 *
	 * @param itemList   the list this item is on.
	 * @param view       the view this item lives on.
	 * @param sector     the 1-based sector [1..6]
	 * @param half       the 1-based layer [1..2]
	 * @param ring       the 1-based component [1..4]
	 */
	public SectorHTCCItem(ItemList itemList, SectorView view, byte sector, byte half, short ring) {
		super(itemList);
		_view = view;
		_sector = sector;
		_half = half;
		_ring = ring;
		setPath(getWorldPolygon());
	}

	/**
	 * Custom drawer for the item.
	 *
	 * @param g         the graphics context.
	 * @param container the graphical container being rendered.
	 */
	@Override
	public void drawItem(Graphics g, IContainer container) {

		if (_eventManager.isAccumulating()) {
			return;
		}

		setPath(getWorldPolygon());

		getStyle().setFillColor(Color.white);
		super.drawItem(g, container); // draws shell

		// hits
		drawHits(g, container);
	}

	// draw any hits
	private void drawHits(Graphics g, IContainer container) {

		if (_view.isSingleEventMode()) {
			drawSingleEventHits(g, container);
		} else {
			drawAccumulatedHits(g, container);
		}
	}

	// single event drawer using adc bank
	private void drawSingleEventHits(Graphics g, IContainer container) {
		
		for (int i = 0; i < adcData.count(); i++) {
			if ((adcData.sector(i) == _sector) && (adcData.layer(i) == _half)
					&& (adcData.component(i) == _ring)) {
				g.setColor(adcData.alphaColor(i));
				g.fillPolygon(_lastDrawnPolygon);
				g.setColor(Color.black);
				g.drawPolygon(_lastDrawnPolygon);
			}
		}

		//the HTCC.rec data
		if (_view.showReconHits()) {

			Point.Double wp = new Point.Double();
			Point pp = new Point();

			for (int i = 0; i < recData.count(); i++) {
				float x = recData.x(i);
				float y = recData.y(i);
				float z = recData.z(i);

				int sect = GeometryManager.getSector(x, y);
				if (sect == _sector) {
					_view.projectClasToWorld(x, y, z, _view.getProjectionPlane(), wp);
					container.worldToLocal(pp, wp);

					DataDrawSupport.drawReconHit(g, pp);
					recData.setLocation(i, pp);

				}

			} // end loop over rec data
		} // end show recon hits
	}


	// accumulated drawer
	private void drawAccumulatedHits(Graphics g, IContainer container) {

		int maxHit = AccumulationManager.getInstance().getMaxHTCCCount();

		int hits[][][] = AccumulationManager.getInstance().getAccumulatedHTCCData();

		int hitCount = hits[_sector - 1][_half - 1][_ring - 1];

		double fract = (maxHit == 0) ? 0 : (((double) hitCount) / maxHit);
		Color color = AccumulationManager.getInstance().getColor(_view.getColorScaleModel(), fract);

		g.setColor(color);
		g.fillPolygon(_lastDrawnPolygon);
		g.setColor(Color.black);
		g.drawPolygon(_lastDrawnPolygon);

	}

	/**
	 * Add any appropriate feedback strings panel.
	 *
	 * @param container       the Base container.
	 * @param screenPoint     the mouse location.
	 * @param worldPoint      the corresponding world point.
	 * @param feedbackStrings the List of feedback strings to add to.
	 */
	@Override
	public void getFeedbackStrings(IContainer container, Point screenPoint, Point2D.Double worldPoint,
			List<String> feedbackStrings) {
		
		
		if (contains(container, screenPoint)) {

			feedbackStrings.add(DataDrawSupport.prelimColor + "HTCC sect " + _sector + " ring " + _ring + " half " + _half);
			
			for (int i = 0; i < adcData.count(); i++) {
				if ((adcData.sector(i) == _sector) && (adcData.layer(i) == _half)
						&& (adcData.component(i) == _ring)) {
					adcData.addFeedback(i, feedbackStrings);
					break;
				}
			}


			for (int i = 0; i < tdcData.count(); i++) {
				if ((tdcData.sector(i) == _sector) && (tdcData.layer(i) == _half)
						&& (tdcData.component(i) == _ring)) {
					tdcData.addFeedback(i, feedbackStrings);
					break;
				}
			} // end has data
		}

		// hit feedback
		if (_view.showReconHits()) {

			for (int i = 0; i < recData.count(); i++) {

				float x = recData.x(i);
				float y = recData.y(i);

				int sect = GeometryManager.getSector(x, y);
				if (sect == _sector) {

					if (recData.contains(i, screenPoint)) {
						recData.addFeedback(i, feedbackStrings);
						break;
					}
				}

			} // end loop over rec data
		} // end show recon hits

	}

	// get the world polygon corresponding to the boundary of the superlayer
	private Point2D.Double[] getWorldPolygon() {

		if (_dirty) {
			HTCCGeometry.getSimpleWorldPoly(_ring, _half, _view.getSliderPhi(), _cachedWorldPolygon);
			if (isLowerSector()) {
				SuperLayerDrawing.flipPolyToLowerSector(_cachedWorldPolygon);
			}
		} // end dirty
		return _cachedWorldPolygon;
	}

	/**
	 * Test whether this is a lower sector
	 *
	 * @return <code>true</code> if this is a lower sector
	 */
	public boolean isLowerSector() {
		return (_sector > 3);
	}

	/**
	 * Get the 1-based sector
	 *
	 * @return the 1-based sector
	 */
	public int sector() {
		return _sector;
	}

	/**
	 * Get the 1-based ring
	 *
	 * @return the 1-based ring
	 */
	public int ring() {
		return _ring;
	}

	/**
	 * Get the 1-based half
	 *
	 * @return the 1-based half
	 */
	public int half() {
		return _half;
	}

}
