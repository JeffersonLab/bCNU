package cnuphys.ced.magfield;

import java.awt.Color;

import cnuphys.CLAS12Swim.CLAS12Trajectory;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.util.UnicodeSupport;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.bCNU.view.PlotView;
import cnuphys.bCNU.view.ViewManager;
import cnuphys.ced.frame.Ced;
import cnuphys.ced.geometry.GeometryManager;
import cnuphys.magfield.FieldProbe;
import cnuphys.magfield.MagneticFields;
import cnuphys.magfield.RotatedCompositeProbe;
import cnuphys.splot.fit.FitType;
import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.pdata.DataSetException;
import cnuphys.splot.pdata.DataSetType;
import cnuphys.splot.plot.PlotCanvas;
import cnuphys.swim.SwimTrajectory;
import cnuphys.swim.SwimTrajectory2D;

/**
 * Owns the plotting boundary for trajectory magnetic-field integrals.
 * Sector views deliberately know nothing about the plotting implementation.
 */
public final class TrajectoryIntegralPlotter {

	private static final Color[] PLOT_COLORS = {
			X11Colors.getX11Color("Dark Red"), X11Colors.getX11Color("Dark Blue"),
			X11Colors.getX11Color("Dark Green"), Color.black, Color.gray,
			X11Colors.getX11Color("wheat")
	};

	private TrajectoryIntegralPlotter() {
	}

	/** Add the trajectory to the shared field-integral plot and show it. */
	public static void show(SwimTrajectory2D trajectory2D) {
		PlotView plotView = Ced.getCed().getPlotView();
		if (plotView == null || trajectory2D == null) {
			return;
		}

		PlotCanvas canvas = plotView.getPlotCanvas();
		try {
			SwimTrajectory trajectory = trajectory2D.getTrajectory3D();
			double[][] integral = fieldIntegralSamples(trajectory, FieldProbe.factory());
			boolean havePlotData = canvas.getDataSet() != null && canvas.getDataSet().dataAdded();

			if (!havePlotData) {
				initializePlot(canvas, trajectory2D, integral);
			} else {
				int curveIndex = canvas.getDataSet().getCurveCount();
				DataSet dataSet = canvas.getDataSet();
				dataSet.addCurve("X", curveName(trajectory2D));
				for (double[] sample : integral) {
					dataSet.addToCurve(curveIndex, sample[0], sample[1]);
				}
				setCurveStyle(canvas, curveIndex);
			}

			ViewManager.getInstance().setVisible(plotView, true);
			canvas.repaint();
		} catch (DataSetException | RuntimeException exception) {
			Log.getInstance().error("Could not plot the trajectory magnetic-field integral");
			Log.getInstance().exception(exception);
		}
	}

	private static void initializePlot(PlotCanvas canvas, SwimTrajectory2D trajectory2D, double[][] integral)
			throws DataSetException {
		DataSet dataSet = new DataSet(DataSetType.XYXY, "X", curveName(trajectory2D));
		canvas.getParameters().setPlotTitle("Magnetic Field Integral");
		canvas.getParameters().setXLabel("Path Length (m)");
		canvas.getParameters().setYLabel("<html>" + UnicodeSupport.INTEGRAL + "|<bold>B</bold> "
				+ UnicodeSupport.TIMES + " <bold>dL</bold>| kG-m");
		for (double[] sample : integral) {
			dataSet.add(sample[0], sample[1]);
		}
		canvas.setDataSet(dataSet);
		setCurveStyle(canvas, 0);
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

	private static void setCurveStyle(PlotCanvas canvas, int index) {
		Color color = PLOT_COLORS[index % PLOT_COLORS.length];
		canvas.getDataSet().getCurveStyle(index).setFitLineColor(color);
		canvas.getDataSet().getCurveStyle(index).setBorderColor(color);
		canvas.getDataSet().getCurveStyle(index).setFillColor(color);
		canvas.getDataSet().getCurveStyle(index).setSymbolType(cnuphys.splot.style.SymbolType.X);
		canvas.getDataSet().getCurveStyle(index).setSymbolSize(6);
		canvas.getDataSet().getCurve(index).getFit().setFitType(FitType.CUBICSPLINE);
	}
}
