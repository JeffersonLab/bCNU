package cnuphys.ced.ced3d.fmt;

import java.awt.Color;

import com.jogamp.opengl.GLAutoDrawable;

import bCNU3D.Support3D;
import edu.cnu.mdi.ui.colors.X11Colors;
import cnuphys.ced.alldata.FMTCrosses;
import cnuphys.ced.clasio.ClasIoEventManager;
import item3D.Item3D;

/** Draws reconstructed FMT crosses in the FMT 3D view. */
public class FMTCrossDrawer3D extends Item3D {

	static final float CROSS_LENGTH = 3f; // cm
	private static final Color CROSS_COLOR = X11Colors.getX11Color("dark orange");

	private final FMTPanel3D panel;
	private final FMTCrosses crosses;

	public FMTCrossDrawer3D(FMTPanel3D panel) {
		this(panel, FMTCrosses.getInstance());
	}

	FMTCrossDrawer3D(FMTPanel3D panel, FMTCrosses crosses) {
		super(panel);
		this.panel = panel;
		this.crosses = crosses;
	}

	@Override
	public void draw(GLAutoDrawable drawable) {
		if (ClasIoEventManager.getInstance().isAccumulating() || !panel.showReconCrosses()) {
			return;
		}

		for (int row = 0; row < crosses.count(); row++) {
			float x = crosses.x(row);
			float y = crosses.y(row);
			float z = crosses.z(row);
			float ux = crosses.ux(row);
			float uy = crosses.uy(row);
			float uz = crosses.uz(row);

			Support3D.drawLine(drawable, x, y, z, ux, uy, uz, CROSS_LENGTH, CROSS_COLOR, 3f);
			Support3D.drawLine(drawable, x, y, z, ux, uy, uz, 1.1f * CROSS_LENGTH, Color.black, 1f);
			Support3D.drawPoint(drawable, x, y, z, Color.black, 13, true);
			Support3D.drawPoint(drawable, x, y, z, CROSS_COLOR, 11, true);
		}
	}
}
