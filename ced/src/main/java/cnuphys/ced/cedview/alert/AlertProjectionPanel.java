package cnuphys.ced.cedview.alert;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Dictionary;
import java.util.Enumeration;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cnuphys.bCNU.dialog.VerticalFlowLayout;
import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.bCNU.util.Fonts;

public class AlertProjectionPanel extends JPanel {

    private final JSlider zSlider;
    private final JLabel zSliderLabel;

    private final AlertXYView alertXYView;

    private static final Font FONT = Fonts.tweenFont;

    public static final int DEFAULT_Z = 0;

    //TOF view radio buttons
    JRadioButton showAllTOF;
    JRadioButton showIntersectingTOF ;


    //constructor
    public AlertProjectionPanel(AlertXYView alertXYView) {
        this.alertXYView = alertXYView;
        setLayout(new VerticalFlowLayout());
        add(Box.createRigidArea(new Dimension(0, 2))); // Spacer
       add(createTOFPanel());

        add(Box.createRigidArea(new Dimension(0, 5))); // Spacer

        // Z Slider
        zSliderLabel = createLabel("  ");
        add(zSliderLabel);

        zSlider = createSlider(-150, 150, DEFAULT_Z, 50, 10, this::updateZLabel, true);
        this.alertXYView.setProjectionPlane(DEFAULT_Z);
        add(zSlider);

        setBorder(new CommonBorder("ALERT Z Projection Plane"));
        updateZLabel(null);
    }

    //panel controlling TOF display
    private JPanel createTOFPanel() {
        JPanel panel = new JPanel();

        // Use a BoxLayout with vertical alignment
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        ButtonGroup projectionGroup = new ButtonGroup();
        
        JPanel bPanel = new JPanel();
        bPanel.setLayout(new BoxLayout(bPanel, BoxLayout.X_AXIS));
        showAllTOF = createRadioButton("All ATOF", projectionGroup, this::handleTOFViewChange);
        showIntersectingTOF = createRadioButton("    Intersecting ATOF", projectionGroup, this::handleTOFViewChange);
        showAllTOF.setSelected(true);

        // Add radio buttons to the panel
        bPanel.add(showAllTOF);
        bPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Spacer
        bPanel.add(showIntersectingTOF);
        showIntersectingTOF.setEnabled(false);
        panel.add(bPanel);
 
        // Set default selection
        showAllTOF.setSelected(true);

        return panel;
    }

	private JRadioButton createRadioButton(String text, ButtonGroup bg, ChangeListener changeListener) {
		JRadioButton radioButton = new JRadioButton(text);
		radioButton.setFont(FONT);
		radioButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		radioButton.addChangeListener(changeListener);
		bg.add(radioButton);
		return radioButton;
	}

    /**
     * Show all the TOF or just the intersecting TOF
     * @return true if all TOF should be shown
     */
	public boolean showAllTOF() {
		return showAllTOF.isSelected();
	}

    //helper method to create a label
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    //helper method to create a slider
    private JSlider createSlider(int min, int max, int value, int major, int minor, ChangeListener changeListener, boolean enabled) {
        JSlider slider = new JSlider(min, max, value) {
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.width = 220;
                return d;
            }
        };

        slider.setMajorTickSpacing(major);
        slider.setMinorTickSpacing(minor);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setEnabled(enabled);
        customizeSliderLabels(slider);
        slider.addChangeListener(changeListener);
        return slider;
    }

    //helper method to customize the slider labels
    private void customizeSliderLabels(JSlider slider) {
        Dictionary<?, ?> labelTable = slider.createStandardLabels(50);
        slider.setLabelTable(labelTable);

        if (labelTable != null) {
            Enumeration<?> keys = labelTable.keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object val = labelTable.get(key);
                if (val instanceof JLabel) {
                    ((JLabel) val).setFont(FONT);
                }
            }
        }
    }


    // Callback for TOF view change
    private void handleTOFViewChange(ChangeEvent e) {
        alertXYView.refresh();
    }

	// update the z label
	private void updateZLabel(ChangeEvent e) {
		zSliderLabel.setText(" z = " + zSlider.getValue() + " mm");
		alertXYView.setProjectionPlane(zSlider.getValue());
		alertXYView.refresh();
	}


	/**
	 * Get the fixed Z value
	 *
	 * @return the fixed Z value
	 */
    public double getFixedZ() {
        return zSlider.getValue();
    }
}
