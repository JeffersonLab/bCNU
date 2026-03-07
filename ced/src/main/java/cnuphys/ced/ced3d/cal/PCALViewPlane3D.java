package cnuphys.ced.ced3d.cal;

import java.awt.Color;

import com.jogamp.opengl.GLAutoDrawable;

import bCNU3D.Support3D;
import cnuphys.ced.alldata.datacontainer.cal.PCalADCData;
import cnuphys.ced.ced3d.DetectorItem3D;
import cnuphys.ced.ced3d.PlainPanel3D;
import cnuphys.ced.geometry.PCALGeometry;

public class PCALViewPlane3D extends DetectorItem3D {

	// sector is 1..6
	private final int _sector;

	// [1, 2, 3] for [u, v, w] like geometry "layer+1"
	private final int _view;

	// the triangle coordinates
	private float _coords[];

	public PCALViewPlane3D(PlainPanel3D panel3D, int sector, int view) {
		super(panel3D);
		_sector = sector;
		_view = view;
		_coords = new float[9];
		PCALGeometry.getViewTriangle(sector, view, _coords);
	}

	@Override
	public void drawShape(GLAutoDrawable drawable) {

		Color outlineColor = new Color(32, 200, 64, getVolumeAlpha());
		Support3D.drawTriangles(drawable, _coords, outlineColor, 1f, true);

		// float coords[] = new float[24];
		// for (int strip = 1; strip <= 36; strip++) {
		// ECGeometry.getStrip(_sector, _stack, _view, strip, coords);
		// drawStrip(drawable, outlineColor, coords);
		// }
	}

	// draw a single strip
	private void drawStrip(GLAutoDrawable drawable, Color color, float coords[]) {

		boolean frame = true;
		Support3D.drawQuad(drawable, coords, 0, 1, 2, 3, color, 1f, frame);
		Support3D.drawQuad(drawable, coords, 3, 7, 6, 2, color, 1f, frame);
		Support3D.drawQuad(drawable, coords, 0, 4, 7, 3, color, 1f, frame);
		Support3D.drawQuad(drawable, coords, 0, 4, 5, 1, color, 1f, frame);
		Support3D.drawQuad(drawable, coords, 1, 5, 6, 2, color, 1f, frame);
		Support3D.drawQuad(drawable, coords, 4, 5, 6, 7, color, 1f, frame);
	}

	@Override
	public void drawData(GLAutoDrawable drawable) {

		//draw adc data
		PCalADCData pcalADCData = PCalADCData.getInstance();
		int count = pcalADCData.count();
		if (count == 0) {
			return;
		}

		float coords[] = new float[24];

		for (int i = 0; i < count; i++) {
			if (pcalADCData.sector.get(i) == _sector) {
				if (pcalADCData.view.get(i) == (_view - 1)) {
					int adc = pcalADCData.adc.get(i);
					int strip = pcalADCData.strip.get(i);
					Color color = pcalADCData.getADCColor(adc);
					PCALGeometry.getStrip(_sector, _view, strip, coords);
					drawStrip(drawable, color, coords);
				}
			}
		}

	}

	// show PCALs?
	@Override
	protected boolean show() {
		boolean showpcal = _cedPanel3D.showPCAL();
		return showpcal && _cedPanel3D.showSector(_sector);
	}

}
