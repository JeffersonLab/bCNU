package cnuphys.ced.alldata;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;

import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.SymbolDraw;
import cnuphys.ced.frame.CedColors;
import edu.cnu.mdi.ui.colors.X11Colors;

public class DataDrawSupport {

	public static final int HB_CROSS = 0;
	public static final int TB_CROSS = 1;
	public static final int AIHB_CROSS = 2;
	public static final int AITB_CROSS = 3;
	public static final int BST_CROSS = 4;
	public static final int BMT_CROSS = 5;
	public static final int FMT_CROSS = 6;


	// for uniform feedback colors
	public static final String prelimColor = "$orange$";
	public static final String trueColor = "$Alice Blue$";


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
	public static void drawCluster(Graphics g, Point pp) {
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
	public static void drawClusterHighlight(Graphics g, Point pp) {
		g.setColor(HIGHLIGHTFILL);
		g.fillOval(pp.x - 8, pp.y - 8, 16, 16);
		drawCluster(g, pp);
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

		int opt = 2;
		if ((mode == 0) || (mode == 4)) {
			opt = 3;
		}

		drawSphere(g, transColors[mode], x, y, CROSSHALF, opt);
		SymbolDraw.drawCross(g, x, y, CROSSHALF, Color.black);
	}

	/**
	 * Draw a 3D sphere icon
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


}
