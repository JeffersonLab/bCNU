package cnuphys.ced.ced3d;

import java.awt.Color;

import com.jogamp.opengl.GLAutoDrawable;

import bCNU3D.Support3D;
import cnuphys.ced.alldata.datacontainer.bmt.BMTADCData;
import cnuphys.ced.event.data.BMTCrosses;
import cnuphys.ced.event.data.Cross2;
import cnuphys.ced.event.data.lists.CrossList2;
import cnuphys.ced.geometry.BMTGeometry;
import cnuphys.ced.geometry.bmt.Constants;
import cnuphys.lund.X11Colors;

public class BMTLayer3D extends DetectorItem3D {

	protected static final Color outlineHitColor = new Color(0, 255, 64, 24);

	protected static final float CROSS_LEN = 3f; // in cm
	protected static final Color crossColor = X11Colors.getX11Color("dark orange");

	// the 1=based sect
	private int _sector;

	// the 1-based layer
	private int _layer;
	
	//bmt adc data
	private BMTADCData _bmtADCData = BMTADCData.getInstance();


	public BMTLayer3D(CedPanel3D panel3D, int sector, int layer) {
		super(panel3D);
		_sector = sector;
		_layer = layer;
	}

	@Override
	public void drawShape(GLAutoDrawable drawable) {
		float coords6[] = new float[6];
		int region = (_layer + 1) / 2 - 1; // region index (0...2) 0=layers 1&2, 1=layers 3&4, 2=layers 5&6

		Color color = (BMTGeometry.getGeometry().isZLayer(_layer) ? X11Colors.getX11Color("gray", getVolumeAlpha())
				: X11Colors.getX11Color("light green", getVolumeAlpha()));

		if (BMTGeometry.getGeometry().isZLayer(_layer)) {
			int numStrips = Constants.getCRZNSTRIPS()[region];
			for (int strip = 1; strip <= numStrips; strip++) {
				BMTGeometry.getGeometry().getCRZEndPoints(_sector, _layer, strip, coords6);
				if (!Float.isNaN(coords6[0])) {
					Support3D.drawLine(drawable, coords6, color, 2f);
				}
			}
		}
	}

	@Override
	public void drawData(GLAutoDrawable drawable) {

		float coords6[] = new float[6];
		
		for (int i = 0; i < _bmtADCData.count(); i++) {
			if (_bmtADCData.sector[i] == _sector && _bmtADCData.layer[i] == _layer) {
				int strip = _bmtADCData.component[i];
				BMTGeometry.getGeometry().getCRZEndPoints(_sector, _layer, strip, coords6);
				if (!Float.isNaN(coords6[0])) {
					Support3D.drawLine(drawable, coords6, _bmtADCData.adc[i] > 0 ? Color.red : Color.blue,
							STRIPLINEWIDTH);
				}
			}
		}

		// reconstructed crosses?
		if (_cedPanel3D.showReconCrosses()) {

			// BMT
			CrossList2 crosses = BMTCrosses.getInstance().getCrosses();
			int len = (crosses == null) ? 0 : crosses.size();
			for (int i = 0; i < len; i++) {
				Cross2 cross = crosses.elementAt(i);
				if (cross != null) {
					// no longer convert (already in cm)
					float x1 = cross.x;
					float y1 = cross.y;
					float z1 = cross.z;

					Support3D.drawLine(drawable, x1, y1, z1, cross.ux, cross.uy, cross.uz, CROSS_LEN, crossColor, 3f);
					Support3D.drawLine(drawable, x1, y1, z1, cross.ux, cross.uy, cross.uz, (float) (1.1 * CROSS_LEN),
							Color.black, 1f);

					drawCrossPoint(drawable, x1, y1, z1, crossColor);
				}
			} // bmt

		}
	}

	// show BMT?
	@Override
	protected boolean show() {
		switch (_layer) {
		case 1:
			return _cedPanel3D.showBMTLayer1();
		case 2:
			return _cedPanel3D.showBMTLayer2();
		case 3:
			return _cedPanel3D.showBMTLayer3();
		case 4:
			return _cedPanel3D.showBMTLayer4();
		case 5:
			return _cedPanel3D.showBMTLayer5();
		case 6:
			return _cedPanel3D.showBMTLayer6();
		}
		return false;
	}

	// show strip hits?
	protected boolean showHits() {
		return show() && _cedPanel3D.showBMTHits();
	}

}
