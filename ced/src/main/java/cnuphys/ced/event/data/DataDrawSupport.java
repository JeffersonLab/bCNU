package cnuphys.ced.event.data;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;

import javax.swing.JComponent;
import javax.swing.JFrame;

import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.SymbolDraw;
import cnuphys.ced.frame.CedColors;
import cnuphys.splot.plot.X11Colors;

public class DataDrawSupport {

	public static final int HB_CROSS = 0;
	public static final int TB_CROSS = 1;
	public static final int AIHB_CROSS = 2;
	public static final int AITB_CROSS = 3;
	public static final int BST_CROSS = 4;
	public static final int BMT_CROSS = 5;
	public static final int FMT_CROSS = 6;



	private static final Color TRANSYELLOW = new Color(255, 255, 0, 240);
	private static final Color TRANSGREEN = X11Colors.getX11Color("lawn green", 250);
	private static final Color TRANSRED = X11Colors.getX11Color("red", 128);
	private static final Color HIGHLIGHTFILL = X11Colors.getX11Color("bisque", 64);


	public static Color transColors[] = { CedColors.HB_TRANS, CedColors.TB_TRANS, CedColors.AIHB_TRANS, CedColors.AITB_TRANS,
			TRANSYELLOW, TRANSGREEN, TRANSRED};



	public static String prefix[] = { "Reg HB ", "Reg TB ", "AI HB ", "AI TB ", "BST ", "BMT ", "FMT " };

	// half the size of a cross
	public static final int CROSSHALF = 6; // pixels

	// half the size of a hit
	public static final int HITHALF = 6; // pixels

	private static final Color gemc_hit_fillColor = new Color(255, 255, 0, 196);
	private static final Color gemc_hit_lineColor = X11Colors.getX11Color("dark red");

	// reconstructed hits
	private static final Color rec_hit_fillColor = Color.cyan;
	private static final Color rec_hit_lineColor = Color.red;

	// adc hits
	private static final Color adc_hit_lineColor = Color.black;

	// reconstructed cluster
	private static final Color cluster_fillColor = X11Colors.getX11Color("fuchsia");
	private static final Color cluster_lineColor = Color.green;

	public static final String[] EC_PLANE_NAMES = { "?", "Inner", "Outer" };
	public static final String[] EC_VIEW_NAMES = { "?", "U", "V", "W" };


	/**
	 * Draw the ECAL reconstruction
	 *
	 * @param g  the graphics context
	 * @param pp the screen location
	 */
	public static void drawECALRec(Graphics g, Point pp, boolean highlight) {
		SymbolDraw.drawDavid(g, pp.x, pp.y, highlight ? 5 : 4, Color.black, highlight ? Color.yellow : Color.red);
	}



	/**
	 * Draw a hit based hit at the given screen location
	 *
	 * @param g  the graphics context
	 * @param pp the screen location
	 */
	public static void drawHBHit(Graphics g, Point pp) {
		g.setColor(Color.white);
		g.drawLine(pp.x - 4, pp.y - 5, pp.x + 6, pp.y + 5);
		g.drawLine(pp.x - 4, pp.y + 5, pp.x + 6, pp.y - 5);


		g.setColor(rec_hit_lineColor);
		g.drawLine(pp.x - 5, pp.y - 5, pp.x + 5, pp.y + 5);
		g.drawLine(pp.x - 5, pp.y + 5, pp.x + 5, pp.y - 5);
		g.setColor(CedColors.HB_COLOR);
		g.fillRect(pp.x - 3, pp.y - 3, 6, 6);
		g.setColor(rec_hit_lineColor);
		g.drawRect(pp.x - 3, pp.y - 3, 6, 6);
	}

	/**
	 * Draw a highlighted HB hit at the given screen location
	 *
	 * @param g  the graphics context
	 * @param pp the screen location
	 */
	public static void drawHBHitHighlight(Graphics g, Point pp) {
		g.setColor(HIGHLIGHTFILL);
		g.fillOval(pp.x - 8, pp.y - 8, 16, 16);
		drawHBHit(g, pp);
		g.setColor(Color.black);
		g.drawOval(pp.x - 7, pp.y - 7, 16, 16);
		g.setColor(Color.white);
		g.drawOval(pp.x - 8, pp.y - 8, 16, 16);
	}


	/**
	 * Draw a time based hit at the given screen location
	 *
	 * @param g  the graphics context
	 * @param pp the screen location
	 */
	public static void drawTBHit(Graphics g, Point pp) {
		g.setColor(Color.white);
		g.drawLine(pp.x - 4, pp.y - 5, pp.x + 6, pp.y + 5);
		g.drawLine(pp.x - 4, pp.y + 5, pp.x + 6, pp.y - 5);

		g.setColor(rec_hit_lineColor);
		g.drawLine(pp.x - 5, pp.y - 5, pp.x + 5, pp.y + 5);
		g.drawLine(pp.x - 5, pp.y + 5, pp.x + 5, pp.y - 5);
		g.setColor(CedColors.TB_COLOR);
		g.fillRect(pp.x - 3, pp.y - 3, 6, 6);

		GraphicsUtilities.drawSimple3DRect(g, pp.x - 3, pp.y - 3, 6, 6, true);	//	g.drawRect(pp.x - 3, pp.y - 3, 6, 6);
	}

	/**
	 * Draw a highlighted TB hit at the given screen location
	 *
	 * @param g  the graphics context
	 * @param pp the screen location
	 */
	public static void drawTBHitHighlight(Graphics g, Point pp) {
		g.setColor(HIGHLIGHTFILL);
		g.fillOval(pp.x - 8, pp.y - 8, 16, 16);
		drawTBHit(g, pp);
		g.setColor(Color.black);
		g.drawOval(pp.x - 7, pp.y - 7, 16, 16);
		g.setColor(Color.white);
		g.drawOval(pp.x - 8, pp.y - 8, 16, 16);
	}


	//Convert opaque color to one woth alpha
	private static Color alphaColor(Color color, int alpha) {
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
	}


	/**
	 * Draw a reconstructed hit at the given screen location
	 *
	 * @param g  the graphics context
	 * @param pp the screen location
	 */
	public static void drawReconHit(Graphics g, Point pp) {

		g.setColor(Color.white);
		g.drawLine(pp.x - 4, pp.y - 5, pp.x + 6, pp.y + 5);
		g.drawLine(pp.x - 4, pp.y + 5, pp.x + 6, pp.y - 5);

		g.setColor(rec_hit_lineColor);
		g.drawLine(pp.x - 5, pp.y - 5, pp.x + 5, pp.y + 5);
		g.drawLine(pp.x - 5, pp.y + 5, pp.x + 5, pp.y - 5);
		g.setColor(rec_hit_fillColor);
		g.fillRect(pp.x - 3, pp.y - 3, 6, 6);
		GraphicsUtilities.drawSimple3DRect(g, pp.x - 3, pp.y - 3, 6, 6, true);
	}

	/**
	 * Draw a reconstructed hit at the given screen location
	 *
	 * @param g  the graphics context
	 * @param pp the screen location
	 */
	public static void drawReconHit(Graphics g, Point pp, int alpha) {

		g.setColor(alphaColor(Color.white, alpha));
		g.drawLine(pp.x - 4, pp.y - 5, pp.x + 6, pp.y + 5);
		g.drawLine(pp.x - 4, pp.y + 5, pp.x + 6, pp.y - 5);

		g.setColor(alphaColor(rec_hit_lineColor, alpha));
		g.drawLine(pp.x - 5, pp.y - 5, pp.x + 5, pp.y + 5);
		g.drawLine(pp.x - 5, pp.y + 5, pp.x + 5, pp.y - 5);
		g.setColor(alphaColor(rec_hit_fillColor, alpha));
		g.fillRect(pp.x - 3, pp.y - 3, 6, 6);
		GraphicsUtilities.drawSimple3DRect(g, pp.x - 3, pp.y - 3, 6, 6, true);
	}


	/**
	 * Draw a highlighted reconstructed hit at the given screen location
	 *
	 * @param g  the graphics context
	 * @param pp the screen location
	 */
	public static void drawReconHitHighlight(Graphics g, Point pp) {
		g.setColor(HIGHLIGHTFILL);
		g.fillOval(pp.x - 8, pp.y - 8, 16, 16);
		drawReconHit(g, pp);
		g.setColor(Color.black);
		g.drawOval(pp.x - 7, pp.y - 7, 16, 16);
		g.setColor(Color.white);
		g.drawOval(pp.x - 8, pp.y - 8, 16, 16);
	}


	/**
	 * Draw a reconstructed hit at the given screen location
	 *
	 * @param g  the graphics context
	 * @param pp the screen location
	 */
	public static void drawAdcHit(Graphics g, Point pp, Color fcolor) {		// now the cross
		g.setColor(adc_hit_lineColor);
		g.drawLine(pp.x - 4, pp.y - 4, pp.x + 4, pp.y + 4);
		g.drawLine(pp.x - 4, pp.y + 4, pp.x + 4, pp.y - 4);
		g.setColor(fcolor);
		g.fillOval(pp.x - 3, pp.y - 3, 6, 6);
		g.setColor(adc_hit_lineColor);
		g.drawOval(pp.x - 3, pp.y - 3, 6, 6);
	}

	/**
	 * Draw a reconstructed hit at the given screen location
	 *
	 * @param g  the graphics context
	 * @param pp the screen location
	 */
	public static void drawReconCluster(Graphics g, Point pp) {
		g.setColor(cluster_lineColor);
		g.drawLine(pp.x, pp.y - 5, pp.x, pp.y + 5);
		g.drawLine(pp.x - 5, pp.y, pp.x + 5, pp.y);
		g.setColor(cluster_fillColor);
		g.fillRect(pp.x - 3, pp.y - 3, 6, 6);
		GraphicsUtilities.drawSimple3DRect(g, pp.x - 3, pp.y - 3, 6, 6, false);
	}

	/**
	 * Draw a reconstructed hit at the given screen location
	 *
	 * @param g  the graphics context
	 * @param pp the screen location
	 */
	public static void drawReconClusterHighlight(Graphics g, Point pp) {
		g.setColor(HIGHLIGHTFILL);
		g.fillOval(pp.x - 8, pp.y - 8, 16, 16);
		drawReconCluster(g, pp);
		g.setColor(Color.white);
		g.drawOval(pp.x - 7, pp.y - 7, 16, 16);
		g.setColor(Color.black);
		g.drawOval(pp.x - 8, pp.y - 8, 16, 16);
	}


	/**
	 * Draw a reconstructed cross
	 *
	 * @param g    the graphics context
	 * @param x    the x location
	 * @param y    the y location
	 * @param mode the mode (HB, TB, etc)
	 */
	public static void drawCross(Graphics g, int x, int y, int mode) {
//		SymbolDraw.drawOval(g, x, y, CROSSHALF, CROSSHALF, Color.black, transColors[mode]);

		int opt = 2;
		if ((mode == 0) || (mode == 4)) {
			opt = 3;
		}

		drawSphere(g, transColors[mode], x, y, CROSSHALF, opt);
		SymbolDraw.drawCross(g, x, y, CROSSHALF, Color.black);
	}


	/**
	 * Draw a #D sphere icon
	 *
	 * @param g    the graphics context
	 * @param name
	 * @param xc    the x center location
	 * @param yc    the y center location
	 * @param radius
	 */
	public static void drawSphere(Graphics g, Color baseColor, int xc, int yc, float radius, int opt) {
		GraphicsUtilities.drawGradientCircle(g, radius, baseColor, Color.black, new Point2D.Float(xc, yc), opt);
	}






	/**
	 * Draw a bigger reconstructed cross for highlighting
	 *
	 * @param g    the graphics context
	 * @param x    the x location
	 * @param y    the y location
	 * @param mode the mode (HB, TB, etc)
	 */
	public static void drawBiggerCross(Graphics g, int x, int y, int mode) {
		int opt = 2;
		if ((mode == 0) || (mode == 4)) {
			opt = 3;
		}

		drawSphere(g, transColors[mode], x, y, CROSSHALF+3, opt);

		SymbolDraw.drawCross(g, x, y, CROSSHALF+3, Color.black);
	}


	/**
	 * Get a string representing the id array
	 *
	 * @param ids variable length ids
	 * @return a string representing the id array
	 */
	public static String getIDString(int... ids) {
		if ((ids == null) || (ids.length < 1)) {
			return "???";
		}
		StringBuilder sb = new StringBuilder(50);
		sb.append("]");
		for (int i = 0; i < ids.length; i++) {
			sb.append(ids[i]);
			if (i < (ids.length - 1)) {
				sb.append(", ");
			}
		}
		return sb.toString();
	}





	public static void main(String arg[]) {
		JFrame testFrame = new JFrame("Attributes");

		Color baseColors[] = {
				Color.black,
				Color.red,
				Color.green,
				Color.yellow,
				Color.cyan,
				Color.DARK_GRAY,
				Color.gray,
				Color.magenta,
				Color.orange,
				Color.pink,
				X11Colors.getX11Color("Dark Green"),
				X11Colors.getX11Color("Deep Sky Blue"),
				X11Colors.getX11Color("Medium Purple"),
				X11Colors.getX11Color("Wheat"),
				X11Colors.getX11Color("Gold"),
				Color.white,
		};

		final float radius = 80;


		JComponent component = new JComponent() {

			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);


				Point2D.Float center = new Point2D.Float();

				int index =  0;
				for (int col = 0; col < 4; col++) {
					float x = 10 + radius +  col * (2*radius + 20);
					for (int row = 0; row < 4; row++) {
						float y = 10 + radius +  row * (2*radius + 20);
						center.setLocation(x, y);
						Color baseColor = baseColors[index++];
						GraphicsUtilities.drawGradientCircle(g, radius, baseColor, Color.black, center, 2);
					}
				}

			}

			@Override
			public Dimension getPreferredSize() {
				return new Dimension(800, 800);
			}
		};


		testFrame.setLayout(new BorderLayout(8, 8));
		testFrame.add(component, BorderLayout.CENTER);

		// set up what to do if the window is closed
		WindowAdapter windowAdapter = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				System.err.println("Done");
				System.exit(1);
			}
		};

		testFrame.addWindowListener(windowAdapter);
		testFrame.pack();
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				testFrame.setVisible(true);
			}
		});
	}

}
