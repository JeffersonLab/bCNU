package cnuphys.ced.cedview.alldc;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import cnuphys.bCNU.graphics.GraphicsUtilities;
import edu.cnu.mdi.component.CommonBorder;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.frame.CedColors;

public class AllDCDisplayPanel extends JPanel implements ActionListener {

	// the parent view
	private CedView _view;

	// the toggle buttons
	private JCheckBox _rawHitsButton;
	private JCheckBox _hbHitsButton;
	private JCheckBox _tbHitsButton;
	private JCheckBox _aihbHitsButton;
	private JCheckBox _aitbHitsButton;

	public AllDCDisplayPanel(CedView view) {
		_view = view;
		setup();
	}

	// create and lawout the components
	private void setup() {
		setLayout(new GridLayout(3, 2, 2, 2));
		setBorder(new CommonBorder("Hit Display Control"));

		JPanel[] panels = new JPanel[6];
		for (int i = 0; i < panels.length; i++) {
			panels[i] = new JPanel();
			panels[i].setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
		}

		_rawHitsButton = createButton(panels[0], 0);
		_hbHitsButton = createButton(panels[1], 1);
		_tbHitsButton = createButton(panels[2], 2);
		_aihbHitsButton = createButton(panels[3], 3);
		_aitbHitsButton = createButton(panels[4], 4);


		for (JPanel panel : panels) {
			add(panel);
		}
	}

	private JCheckBox createButton(JPanel panel, int opt) {
		JCheckBox button = null;

		JComponent component = null;

		int w = 8;
		int h = 12;

		switch (opt) {
		case 0:

			component =  new JComponent() {
				@Override
				public void paintComponent(Graphics g) {
					g.setColor(Color.red);
					g.fillRect(0, 0, w, h);
				}

			};


			button = new JCheckBox("Raw ", true);
			break;


		case 1:
			component =  new JComponent() {
				@Override
				public void paintComponent(Graphics g) {
					g.setColor(CedColors.HB_COLOR);
					g.fillRect(0, 0, w, h);
				}

			};

			button = new JCheckBox("Reg HB ", false);
			break;

		case 2:
			component =  new JComponent() {
				@Override
				public void paintComponent(Graphics g) {
					Rectangle b = getBounds();
					g.setColor(CedColors.TB_COLOR);
					g.fillRect(0, 0, w, h);
				}

			};

			button = new JCheckBox("Reg TB ", false);
			break;

		case 3:
			component =  new JComponent() {
				@Override
				public void paintComponent(Graphics g) {
					Rectangle b = getBounds();
					g.setColor(CedColors.AIHB_COLOR);
					g.fillRect(0, 0, w, h);
				}

			};

			button = new JCheckBox("AI HB ", false);
			break;

		case 4:
			component =  new JComponent() {
				@Override
				public void paintComponent(Graphics g) {
					Rectangle b = getBounds();
					g.setColor(CedColors.AITB_COLOR);
					g.fillRect(0, 0, w, h);
				}

			};

			button = new JCheckBox("AI TB ", false);
			break;

		}

		if ((component == null) || (button == null)) {
			return null;
		}

		Dimension dim = new Dimension(w, h);
		component.setPreferredSize(dim);
		component.setSize(dim);

		GraphicsUtilities.setSizeSmall(button);
		panel.add(component);
		panel.add(button);
		button.addActionListener(this);
		return button;
	}

	/**
	 * Display raw DC hits?
	 *
	 * @return <code> if we should display raw hits
	 */
	public boolean showRawHits() {
		return _rawHitsButton.isSelected();
	}

	/**
	 * Display regular hit based hits?
	 *
	 * @return <code> if we should display hit based hits
	 */
	public boolean showHBHits() {
		return _hbHitsButton.isSelected();
	}

	/**
	 * Display regular time based hits?
	 *
	 * @return <code> if we should display hits
	 */
	public boolean showTBHits() {
		return _tbHitsButton.isSelected();
	}

	/**
	 * Display AI hit based hits?
	 *
	 * @return <code> if we should display hit based hits
	 */
	public boolean showAIHBHits() {
		return _aihbHitsButton.isSelected();
	}

	/**
	 * Display AI time based hits?
	 *
	 * @return <code> if we should display hits
	 */
	public boolean showAITBHits() {
		return _aitbHitsButton.isSelected();
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		_view.refresh();
	}
}
