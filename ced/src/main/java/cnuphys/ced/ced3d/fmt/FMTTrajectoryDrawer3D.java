package cnuphys.ced.ced3d.fmt;

import java.awt.Color;

import com.jogamp.opengl.GLAutoDrawable;

import bCNU3D.Support3D;
import cnuphys.ced.alldata.FMTTrajectories;
import cnuphys.ced.alldata.FMTTracks;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.geometry.fmt.FMTGeometry;
import item3D.Item3D;

/** Draws FMT trajectory positions at detector layers. */
public class FMTTrajectoryDrawer3D extends Item3D {

	private static final Color TRAJECTORY_COLOR = Color.cyan;
	private static final Color ORIGINAL_DC_COLOR = Color.lightGray;
	private final FMTPanel3D panel;
	private final FMTTrajectories trajectories;
	private final FMTTracks tracks;

	public FMTTrajectoryDrawer3D(FMTPanel3D panel) {
		super(panel);
		this.panel = panel;
		trajectories = FMTTrajectories.getInstance();
		tracks = FMTTracks.getInstance();
	}

	@Override
	public void draw(GLAutoDrawable drawable) {
		if (ClasIoEventManager.getInstance().isAccumulating() || !panel.showFMTTrajectories()) {
			return;
		}

		for (int row = 0; row < trajectories.count(); row++) {
			float x = trajectories.x(row);
			float y = trajectories.y(row);
			float z = trajectories.z(row);
			if (x == 0f && y == 0f && z == 0f) {
				float[] global = new float[3];
				FMTGeometry.localToGlobal(trajectories.layer(row) - 1,
						trajectories.dcLocalX(row), trajectories.dcLocalY(row),
						trajectories.dcLocalZ(row), global);
				x = global[0];
				y = global[1];
				z = global[2];
			}
			if (!Float.isFinite(x) || !Float.isFinite(y) || !Float.isFinite(z)) {
				continue;
			}
			Color color = tracks.statusForIndex(trajectories.trackIndex(row)) == 1
					? ORIGINAL_DC_COLOR : TRAJECTORY_COLOR;
			Support3D.drawPoint(drawable, x, y, z, Color.black, 9, true);
			Support3D.drawPoint(drawable, x, y, z, color, 7, true);
		}
	}
}
