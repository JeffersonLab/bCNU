package cnuphys.ced.magfield;

import java.awt.Color;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import cnuphys.bCNU.util.PropertySupport;
import edu.cnu.mdi.ui.colors.X11Colors;
import cnuphys.bCNU.view.BaseView;
import edu.cnu.mdi.graphics.style.IStyled;
import edu.cnu.mdi.graphics.style.SymbolType;
import edu.cnu.mdi.splot.fit.CurveDrawingMethod;
import edu.cnu.mdi.splot.pdata.Curve;
import edu.cnu.mdi.splot.pdata.DataColumn;
import edu.cnu.mdi.splot.pdata.PlotData;
import edu.cnu.mdi.splot.pdata.PlotDataException;
import edu.cnu.mdi.splot.pdata.PlotDataType;
import edu.cnu.mdi.splot.plot.PlotCanvas;
import edu.cnu.mdi.splot.plot.PlotPanel;

/** Shared MDI plot view for magnetic-field integrals along trajectories. */
@SuppressWarnings("serial")
public final class TrajectoryIntegralPlotView extends BaseView {

	private static final Color[] PLOT_COLORS = {
			X11Colors.getX11Color("Dark Red"), X11Colors.getX11Color("Dark Blue"),
			X11Colors.getX11Color("Dark Green"), Color.BLACK, Color.GRAY,
			X11Colors.getX11Color("wheat")
	};

	private final PlotData plotData;
	private final PlotCanvas plotCanvas;

	public TrajectoryIntegralPlotView() {
		super(PropertySupport.TITLE, "Magnetic Field Integral",
				PropertySupport.ICONIFIABLE, true,
				PropertySupport.MAXIMIZABLE, true,
				PropertySupport.CLOSABLE, true,
				PropertySupport.RESIZABLE, true,
				PropertySupport.WIDTH, 700,
				PropertySupport.HEIGHT, 700,
				PropertySupport.TOOLBAR, false,
				PropertySupport.PROPNAME, "TRAJECTORYINTEGRALPLOTVIEW",
				PropertySupport.VISIBLE, false);

		plotData = createPlotData();
		plotCanvas = new PlotCanvas(plotData, "Magnetic Field Integral", "Path Length (m)",
				"∫|B × dL| kG-m");
		add(new PlotPanel(plotCanvas));
		addMenu();
	}

	private static PlotData createPlotData() {
		try {
			PlotData data = new PlotData(PlotDataType.XYXY, new String[] { "Trajectory" }, null);
			data.getFirstCurve().setVisible(false);
			return data;
		}
		catch (PlotDataException exception) {
			throw new IllegalStateException("Could not create the trajectory-integral plot", exception);
		}
	}

	private void addMenu() {
		JMenuItem clearItem = new JMenuItem("Clear");
		clearItem.addActionListener(event -> clearPlot());
		JMenu plotMenu = new JMenu("Plot");
		plotMenu.add(clearItem);
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(plotMenu);
		setJMenuBar(menuBar);
	}

	/** Add one trajectory curve to the plot. */
	public void addCurve(String name, double[][] samples) {
		Curve curve = findAvailableCurve(name);
		double[] path = new double[samples.length];
		double[] integral = new double[samples.length];
		for (int i = 0; i < samples.length; i++) {
			path[i] = samples[i][0];
			integral[i] = samples[i][1];
		}
		curve.addAll(path, integral);
		plotCanvas.setWorldSystem();
		plotCanvas.repaint();
	}

	private Curve findAvailableCurve(String name) {
		for (int i = 0; i < plotData.size(); i++) {
			Curve curve = (Curve) plotData.getCurve(i);
			if (curve.length() == 0) {
				curve.setName(name);
				curve.setVisible(true);
				configureCurve(curve, i);
				return curve;
			}
		}

		try {
			Curve curve = new Curve(name, new DataColumn(), new DataColumn(), null);
			configureCurve(curve, plotData.size());
			plotData.addCurve(curve);
			return curve;
		}
		catch (PlotDataException exception) {
			throw new IllegalStateException("Could not add a trajectory-integral curve", exception);
		}
	}

	private static void configureCurve(Curve curve, int index) {
		curve.setCurveDrawingMethod(CurveDrawingMethod.CUBICSPLINE);
		IStyled style = curve.getStyle();
		Color color = PLOT_COLORS[index % PLOT_COLORS.length];
		style.setLineColor(color);
		style.setBorderColor(color);
		style.setFillColor(color);
		style.setSymbolType(SymbolType.X);
		style.setSymbolSize(6);
	}

	private void clearPlot() {
		for (int i = 0; i < plotData.size(); i++) {
			plotData.getCurve(i).clearData();
			plotData.getCurve(i).setVisible(false);
		}
		plotCanvas.setWorldSystem();
		plotCanvas.repaint();
	}
}
