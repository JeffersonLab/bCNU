package cnuphys.ced.ced3d.cal;

import java.awt.Color;

import com.jogamp.opengl.GLAutoDrawable;

import bCNU3D.Support3D;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.ced.alldata.ECalAdc;
import cnuphys.ced.ced3d.DetectorItem3D;
import cnuphys.ced.ced3d.PlainPanel3D;
import cnuphys.ced.geometry.ECGeometry;
import cnuphys.ced.geometry.PCALGeometry;

public class ECViewPlane3D extends DetectorItem3D {

	// sector is 1..6
	private final int _sector;

	// [1,2] for inner/outer
	private final int _plane;

	// [1, 2, 3] for [u, v, w] like geometry "layer+1"
	private final int _view;

	// the triangle coordinates
	private float _coords[];

	//data container
	private ECalAdc ecADCData = ECalAdc.getInstance();


	public ECViewPlane3D(PlainPanel3D panel3D, int sector, int stack, int view) {
		super(panel3D);
		_sector = sector;
		_plane = stack;
		_view = view;
		_coords = new float[9];
		ECGeometry.getViewTriangle(sector, stack, view, _coords);
	}

	@Override
	public void drawShape(GLAutoDrawable drawable) {

		Color color = null;
		if (_plane == 1) {
			color = X11Colors.getX11Color("tan", getVolumeAlpha());
		} else {
			color = X11Colors.getX11Color("Light Green", getVolumeAlpha());
		}

		Support3D.drawTriangles(drawable, _coords, color, 1f, true);

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

		//use ADC data
		int count = ecADCData.count();
		if (count == 0) {
			return;
		}

		float coords[] = new float[24];

		for (int i = 0; i < count; i++) {
			if (ecADCData.isECal(i) && ecADCData.sector(i) == _sector) {
				if (ecADCData.plane(i) == (_plane - 1)) {
					if (ecADCData.view(i) == (_view - 1)) {
						int adc = ecADCData.adc(i);
						int strip = ecADCData.strip(i);
						Color color = ecADCData.adcColor(i);
						PCALGeometry.getStrip(_sector, _view, strip, coords);
						drawStrip(drawable, color, coords);
					}
				}
			}
		}
	}

	// show ECs?
	@Override
	protected boolean show() {
		boolean showec = _cedPanel3D.showECAL();
		return showec && _cedPanel3D.showSector(_sector);
	}

}
