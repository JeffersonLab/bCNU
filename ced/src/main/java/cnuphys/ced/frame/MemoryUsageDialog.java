package cnuphys.ced.frame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.util.concurrent.TimeUnit;

import javax.swing.JDialog;

import edu.cnu.mdi.graphics.style.IStyled;
import edu.cnu.mdi.graphics.style.SymbolType;
import edu.cnu.mdi.splot.fit.CurveDrawingMethod;
import edu.cnu.mdi.splot.pdata.PlotData;
import edu.cnu.mdi.splot.pdata.PlotDataException;
import edu.cnu.mdi.splot.pdata.StripChartCurve;
import edu.cnu.mdi.splot.plot.LimitsMethod;
import edu.cnu.mdi.splot.plot.PlotCanvas;
import edu.cnu.mdi.splot.plot.PlotPanel;
import edu.cnu.mdi.splot.plot.PlotParameters;

/** Displays a live graph of the JVM heap allocated to CED. */
@SuppressWarnings("serial")
public final class MemoryUsageDialog extends JDialog {

	private static final int SAMPLE_CAPACITY = 25;
	private static final long SAMPLE_INTERVAL_MS = 2_000L;

	private final StripChartCurve memoryCurve;

	/**
	 * Creates the modeless memory-usage dialog.
	 *
	 * @param owner the owning CED frame
	 */
	public MemoryUsageDialog(Frame owner) {
		super(owner, "Memory Usage", false);
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		setLayout(new BorderLayout());

		memoryCurve = new StripChartCurve("Memory", SAMPLE_CAPACITY,
				time -> allocatedHeapMegabytes(), SAMPLE_INTERVAL_MS);
		memoryCurve.setTimeUnit(TimeUnit.SECONDS);
		configureCurve(memoryCurve);

		PlotData plotData = createPlotData(memoryCurve);
		PlotCanvas canvas = new PlotCanvas(plotData, "Memory Usage (MB)", "Time (s)", "Memory (MB)");
		configurePlot(canvas.getParameters());
		memoryCurve.setOnSample(canvas::repaint);

		PlotPanel plotPanel = new PlotPanel(canvas);
		plotPanel.setPreferredSize(new Dimension(500, 400));
		add(plotPanel, BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(owner);
	}

	private static PlotData createPlotData(StripChartCurve curve) {
		try {
			return new PlotData(curve);
		}
		catch (PlotDataException exception) {
			throw new IllegalStateException("Could not create the memory usage plot", exception);
		}
	}

	private static void configureCurve(StripChartCurve curve) {
		curve.setCurveDrawingMethod(CurveDrawingMethod.STAIRS);
		IStyled style = curve.getStyle();
		style.setLineColor(Color.RED);
		style.setFillColor(new Color(128, 0, 0, 48));
		style.setSymbolType(SymbolType.NOSYMBOL);
	}

	private static void configurePlot(PlotParameters parameters) {
		parameters.setMinExponentY(3);
		parameters.setNumDecimalY(0);
		parameters.setXLimitsMethod(LimitsMethod.USEDATALIMITS);
		parameters.includeYZero(true);
	}

	static double allocatedHeapMegabytes() {
		return Runtime.getRuntime().totalMemory() / 1_048_576.0;
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			memoryCurve.start();
		}
		super.setVisible(visible);
	}

	@Override
	public void dispose() {
		memoryCurve.shutdown();
		super.dispose();
	}
}
