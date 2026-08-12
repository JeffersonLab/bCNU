package cnuphys.ced.magfield;

import cnuphys.CLAS12Swim.CLAS12Trajectory;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.view.ViewManager;
import cnuphys.ced.frame.Ced;
import cnuphys.ced.geometry.GeometryManager;
import cnuphys.magfield.FieldProbe;
import cnuphys.magfield.MagneticFields;
import cnuphys.magfield.RotatedCompositeProbe;
import cnuphys.swim.SwimTrajectory;
import cnuphys.swim.SwimTrajectory2D;

/**
 * Owns the plotting boundary for trajectory magnetic-field integrals.
 * Sector views deliberately know nothing about the plotting implementation.
 */
public final class TrajectoryIntegralPlotter {

	private TrajectoryIntegralPlotter() {
	}

	/** Add the trajectory to the shared field-integral plot and show it. */
	public static void show(SwimTrajectory2D trajectory2D) {
		TrajectoryIntegralPlotView plotView = Ced.getCed().getPlotView();
		if (plotView == null || trajectory2D == null) {
			return;
		}

		try {
			SwimTrajectory trajectory = trajectory2D.getTrajectory3D();
			double[][] integral = fieldIntegralSamples(trajectory, FieldProbe.factory());
			plotView.addCurve(curveName(trajectory2D), integral);
			ViewManager.getInstance().setVisible(plotView, true);
		} catch (RuntimeException exception) {
			Log.getInstance().error("Could not plot the trajectory magnetic-field integral");
			Log.getInstance().exception(exception);
		}
	}

	private static String curveName(SwimTrajectory2D trajectory2D) {
		return trajectory2D.summaryString() + " ["
				+ MagneticFields.getInstance().getActiveFieldDescription() + "]";
	}

	static double[][] fieldIntegralSamples(SwimTrajectory trajectory, FieldProbe probe) {
		if (trajectory instanceof CLAS12Trajectory clas12Trajectory) {
			return clas12FieldIntegralSamples(clas12Trajectory, probe);
		}

		trajectory.computeBDL(probe);
		double[][] samples = new double[trajectory.size()][2];
		for (int i = 0; i < trajectory.size(); i++) {
			double[] state = trajectory.get(i);
			samples[i][0] = state[SwimTrajectory.PATHLEN_IDX];
			samples[i][1] = state[SwimTrajectory.BXDL_IDX];
		}
		return samples;
	}

	private static double[][] clas12FieldIntegralSamples(CLAS12Trajectory trajectory, FieldProbe probe) {
		int count = Math.min(trajectory.size(), trajectory.getSSize());
		double[][] samples = new double[count][2];
		if (count == 0) {
			return samples;
		}

		double integral = 0;
		samples[0][0] = trajectory.getS(0);
		for (int i = 1; i < count; i++) {
			double[] previous = trajectory.get(i - 1);
			double[] current = trajectory.get(i);
			double dx = current[0] - previous[0];
			double dy = current[1] - previous[1];
			double dz = current[2] - previous[2];
			float[] field = new float[3];
			float x = (float) ((previous[0] + current[0]) * 0.5);
			float y = (float) ((previous[1] + current[1]) * 0.5);
			float z = (float) ((previous[2] + current[2]) * 0.5);
			if (probe instanceof RotatedCompositeProbe rotatedProbe) {
				rotatedProbe.field(GeometryManager.getSector(x, y), x, y, z, field);
			} else {
				probe.field(x, y, z, field);
			}

			double bx = field[1] * dz - field[2] * dy;
			double by = field[2] * dx - field[0] * dz;
			double bz = field[0] * dy - field[1] * dx;
			integral += Math.sqrt(bx * bx + by * by + bz * bz);
			samples[i][0] = trajectory.getS(i);
			samples[i][1] = integral;
		}
		return samples;
	}

}
