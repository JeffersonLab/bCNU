package cnuphys.ced.component;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.SymbolDraw;
import cnuphys.bCNU.graphics.style.LineStyle;
import edu.cnu.mdi.ui.colors.X11Colors;
import cnuphys.ced.alldata.DataDrawSupport;
import cnuphys.ced.frame.CedColors;

/**
 * Shows some of the symbology on a legend.
 * @author heddle
 *
 */
public class DrawingLegend extends JPanel {

	private static final int TRAJSIZE = 10;

	private static final Font labelFont = new Font("SansSerif", Font.PLAIN, 9);

	private static final Color bgColor = new Color(120, 120, 120);

	private static final int X = 10;
	private static final int Y = 12;

	private static final int numRow = 17;
	private static final int numCol = 2;

	Dimension size = new Dimension(numCol * 150, numRow * 30);

	public DrawingLegend() {
		setLayout(new GridLayout(numRow, numCol));
		addLegendComponents();
	}

	@Override
	public void paintComponent(Graphics g) {
		Rectangle b = getBounds();
		g.setColor(Color.black);
		g.fillRect(0, 0, b.width, b.height);
	}

	@Override
	public Dimension getPreferredSize() {
		return size;
	}


	private void addLegendComponents() {
		add(hitRecon());
		add(hitReconHighlight());
		add(hbHit());
		add(hbHitHighlight());
		add(tbHit());
		add(tbHitHighlight());
		add(clusterRecon());
		add(clusterReconHighlight());
		add(ecalRec());
		add(ecalRecHighlight());

		add(crossHB());
		add(crossTB());
		add(crossHBAI());
		add(crossTBAI());
		add(crossFMT());
		add(crossBST());
		add(crossBMT());
		add(docaTB());
		add(trajPointRec());
		add(kfPointRec());
		add(p1PointRec());
		add(hitStripMidpoint());
		add(trackTB());
		add(trackHB());
		add(trackTBAI());
		add(trackHBAI());
		add(trackCVT());
		add(segmentTB());
		add(segmentHB());
		add(segmentTBAI());
		add(segmentHBAI());
	}

	//PCAL/ECAL recon normal
	private LComp ecalRec() {
		LComp comp = new LComp() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				Point pp = new Point();
				pp.setLocation(X, Y);
				DataDrawSupport.drawECALRec(g, pp, false);
				quickString(g, X+8, Y, "ECAL Recon");
			}
		};
		return comp;
	}

	//PCAL/ECAL recon highlight
	private LComp ecalRecHighlight() {
		LComp comp = new LComp() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				Point pp = new Point();
				pp.setLocation(X, Y);
				DataDrawSupport.drawECALRec(g, pp, true);
				quickString(g, X+8, Y, "ECAL Recon Highlight ");
			}
		};
		return comp;
	}


	//Hit based hit
	private LComp hbHit() {
		LComp comp = new LComp() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				Point pp = new Point();
				pp.setLocation(X, Y);
				DataDrawSupport.drawHBHit(g, pp);
				quickString(g, X+8, Y, "HB Hit ");
			}
		};
		return comp;
	}

	//Recon hb Highlight
	private LComp hbHitHighlight() {
		LComp comp = new LComp() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				Point pp = new Point();
				pp.setLocation(X+3, Y);
				DataDrawSupport.drawHBHitHighlight(g, pp);
				quickString(g, X+15, Y, "HB Highlight Hit ");
			}
		};
		return comp;
	}

	//Recon tb Highlight
	private LComp tbHitHighlight() {
		LComp comp = new LComp() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				Point pp = new Point();
				pp.setLocation(X+3, Y);
				DataDrawSupport.drawTBHitHighlight(g, pp);
				quickString(g, X+15, Y, "TB Highlight Hit ");
			}
		};
		return comp;
	}

	//Hit based hit
	private LComp tbHit() {
		LComp comp = new LComp() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				Point pp = new Point();
				pp.setLocation(X, Y);
				DataDrawSupport.drawTBHit(g, pp);
				quickString(g, X+8, Y, "HB Hit ");
			}
		};
		return comp;
	}



	//Recon hit
	private LComp hitRecon() {
		LComp comp = new LComp() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				Point pp = new Point();
				pp.setLocation(X, Y);
				DataDrawSupport.drawReconHit(g, pp);
				quickString(g, X+8, Y, "Recon Hit ");
			}
		};
		return comp;
	}

	//Recon hit Highlight
	private LComp hitReconHighlight() {
		LComp comp = new LComp() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				Point pp = new Point();
				pp.setLocation(X+3, Y);
				DataDrawSupport.drawReconHitHighlight(g, pp);
				quickString(g, X+15, Y, "Highlight Hit ");
			}
		};
		return comp;
	}


	//Recon cluster
	private LComp clusterRecon() {
		LComp comp = new LComp() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				Point pp = new Point();
				pp.setLocation(X, Y);
				DataDrawSupport.drawCluster(g, pp);
				quickString(g, X+8, Y, "Recon Cluster ");
			}
		};
		return comp;
	}

	//Recon cluster
	private LComp clusterReconHighlight() {
		LComp comp = new LComp() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				Point pp = new Point();
				pp.setLocation(X+3, Y);
				DataDrawSupport.drawClusterHighlight(g, pp);
				quickString(g, X+15, Y, "Highlight Cluster ");
			}
		};
		return comp;
	}


	//Hit based cross (regular)
	private LComp crossHB() {
		LComp comp = new LComp() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				drawCross(g, X, Y, DataDrawSupport.HB_CROSS);
			}
		};
		return comp;
	}

	//Time based cross (regular)
	private LComp crossTB() {
		LComp comp = new LComp() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				drawCross(g, X, Y, DataDrawSupport.TB_CROSS);
			}
		};
		return comp;
	}

	//Hit based cross (AI)
	private LComp crossHBAI() {
		LComp comp = new LComp() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				drawCross(g, X, Y, DataDrawSupport.AIHB_CROSS);
			}
		};
		return comp;
	}

	//Time based cross (AI)
	private LComp crossTBAI() {
		LComp comp = new LComp() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				drawCross(g, X, Y, DataDrawSupport.AITB_CROSS);
			}
		};
		return comp;
	}

	//FMT cross
	private LComp crossFMT() {
		LComp comp = new LComp() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				drawCross(g, X, Y, DataDrawSupport.FMT_CROSS);
			}
		};
		return comp;
	}

	private LComp crossBST() {
		LComp comp = new LComp() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				drawCross(g, X, Y, DataDrawSupport.BST_CROSS);
			}
		};
		return comp;
	}

	private LComp crossBMT() {
		LComp comp = new LComp() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				drawCross(g, X, Y, DataDrawSupport.BMT_CROSS);
			}
		};
		return comp;
	}


	//Time based doca
	private LComp docaTB() {
		LComp comp = new LComp() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				drawCircle(g, X, Y, CedColors.docaTruthLine, "TB Doca");
			}
		};
		return comp;
	}

	//Recon trajectory point
	private LComp trajPointRec() {
		LComp comp = new LComp() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				paintRecTrajPoint(g, X, Y);
			}
		};
		return comp;
	}


	//p1 trajectory point
	private LComp p1PointRec() {
		LComp comp = new LComp() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				paintP1TrajPoint(g, X, Y);
			}
		};
		return comp;
	}

	//kf trajectory point
	private LComp kfPointRec() {
		LComp comp = new LComp() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				paintKFTrajPoint(g, X, Y);
			}
		};
		return comp;
	}


	//hit strip midpoint
	private LComp hitStripMidpoint() {
		LComp comp = new LComp() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				SymbolDraw.drawUpTriangle(g, X, Y, 4, X11Colors.getX11Color("Dark Green"),
						X11Colors.getX11Color("Aquamarine"));

				quickString(g, X + 12, Y, "Hit Strip Midpoint");
			}
		};
		return comp;
	}

	//hit based track
	private LComp trackHB() {
		LComp comp = new LComp() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				drawLine((Graphics2D)g, X, Y, CedColors.HB_COLOR, "Reg HB Track");
			}
		};
		return comp;
	}

	//time based track
	private LComp trackTB() {
		LComp comp = new LComp() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				drawLine((Graphics2D)g, X, Y, CedColors.TB_COLOR, "Reg TB Track");
			}
		};
		return comp;
	}

	//hit based AI track
	private LComp trackHBAI() {
		LComp comp = new LComp() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				drawLine((Graphics2D)g, X, Y, CedColors.AIHB_COLOR, "AI HB Track");
			}
		};
		return comp;
	}

	//time based AI track
	private LComp trackTBAI() {
		LComp comp = new LComp() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				drawLine((Graphics2D)g, X, Y, CedColors.AITB_COLOR, "AI TB Track");
			}
		};
		return comp;
	}


	//cvt track
	private LComp trackCVT() {
		LComp comp = new LComp() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				drawLine((Graphics2D)g, X, Y, CedColors.cvtTrackColor, "CVT Track");
			}
		};
		return comp;
	}

	//hit based segment
	private LComp segmentHB() {
		LComp comp = new LComp() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				drawSegLine((Graphics2D)g, X, Y, CedColors.hbSegmentLine, CedColors.HB_COLOR, "Reg HB Segment  ");
			}
		};
		return comp;
	}

	//time based segment
	private LComp segmentTB() {
		LComp comp = new LComp() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				drawSegLine((Graphics2D)g, X, Y, CedColors.hbSegmentLine, CedColors.TB_COLOR, "Reg HB Segment  ");
			}
		};
		return comp;
	}

	//hit based AI segment
	private LComp segmentHBAI() {
		LComp comp = new LComp() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				drawSegLine((Graphics2D)g, X, Y, CedColors.hbSegmentLine, CedColors.AIHB_COLOR, "Reg HB Segment  ");
			}
		};
		return comp;
	}

	//time based AI segment
	private LComp segmentTBAI() {
		LComp comp = new LComp() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				drawSegLine((Graphics2D)g, X, Y, CedColors.hbSegmentLine, CedColors.AITB_COLOR, "Reg HB Segment  ");
			}
		};
		return comp;
	}


	private void paintRecTrajPoint(Graphics g, int x, int y) {
		int s2 = TRAJSIZE/2;
		SymbolDraw.drawStar(g, x, y, s2, Color.black);
		x += (TRAJSIZE + 4);
		quickString(g, x, y, "Rec Traj Pnt");
	}

	private void paintKFTrajPoint(Graphics g, int x, int y) {
		int s2 = TRAJSIZE/2;
		SymbolDraw.drawStar(g, x, y, s2, Color.green);
		x += (TRAJSIZE + 4);
		quickString(g, x, y, "KF Traj Pnt");
	}

	private void paintP1TrajPoint(Graphics g, int x, int y) {
		int s2 = TRAJSIZE/2;
		SymbolDraw.drawStar(g, x, y, s2, Color.blue);
		x += (TRAJSIZE + 4);
		quickString(g, x, y, "P1 Traj Pnt");
	}

	private void drawLine(Graphics2D g2, int x, int yc, Color lineColor, String str) {
		g2.setColor(CedColors.docaTruthFill);
		g2.setStroke(GraphicsUtilities.getStroke(6f, LineStyle.SOLID));
		g2.drawLine(x, yc, x + 26, yc);
		g2.setColor(lineColor);
		g2.setStroke(GraphicsUtilities.getStroke(2f, LineStyle.SOLID));
		g2.drawLine(x, yc, x + 26, yc);
		x += 36;
		quickString(g2, x, yc - 2, str);
	}

	//draw a segment
	private void drawSegLine(Graphics2D g2, int x, int yc, Color lineColor, Color endColor, String str) {
		g2.setColor(CedColors.docaTruthFill);
		g2.setStroke(GraphicsUtilities.getStroke(6f, LineStyle.SOLID));
		g2.drawLine(x, yc, x + 30, yc);
		g2.setColor(lineColor);
		g2.setStroke(GraphicsUtilities.getStroke(2f, LineStyle.SOLID));
		g2.drawLine(x, yc, x + 30, yc);

		SymbolDraw.drawOval(g2, x, yc, 2, 2, endColor, endColor);
		SymbolDraw.drawOval(g2, x + 30, yc, 2, 2, endColor, endColor);
		x += 40;
		quickString(g2, x, yc - 2, str);
	}

	private void drawCross(Graphics g, int x, int y, int mode) {
		DataDrawSupport.drawCross(g, x, y, mode);

		x += (2 * DataDrawSupport.CROSSHALF);
		String s = DataDrawSupport.prefix[mode] + "cross";
		quickString(g, x, y, s);
	}

	private void drawCircle(Graphics g, int x, int y, Color color, String s) {
		SymbolDraw.drawOval(g, x, y, DataDrawSupport.CROSSHALF, DataDrawSupport.CROSSHALF, color, Color.black);
		x += (2 * DataDrawSupport.CROSSHALF);
		quickString(g, x, y, s);
	}

	// draw a string
	private void quickString(Graphics g, int x, int yc, String s) {
		FontMetrics fm = getFontMetrics(labelFont);
		g.setColor(Color.black);
		g.setFont(labelFont);
		g.setColor(Color.white);
		g.drawString(s, x, yc + fm.getAscent() / 2);
	}



	private class LComp extends JComponent {

		public LComp() {
			setBackground(bgColor);
			setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			setOpaque(true);
		}

		@Override
		public void paintComponent(Graphics g) {
			Rectangle b = getBounds();
			g.setColor(bgColor);
			g.fillRect(0, 0, b.width, b.height);
		}
	}
}
