package cnuphys.ced.component;

import java.awt.Font;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import edu.cnu.mdi.ui.fonts.Fonts;
import cnuphys.bCNU.view.BaseView;

/**
 * A panel for selecting a range of components. The range is specified as a
 * comma-separated list of ranges and/or individual components. For example:
 * "1-10, 17, 230-244". Like a printer range.
 */
public class RangePanel extends JPanel {

	//the text field for the range
    private final JTextField rangeField;

    //input text
    private String _rangeText;

    //the owner view
    private BaseView _view;
    Font font = Fonts.mediumFont;

    public RangePanel(BaseView view, String fullRangeReminder, String defaultText) {
    	_view = view;
    	_rangeText = new String(defaultText);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Top label: "Full Range: 1-1024"
        JLabel fullRangeLabel = new JLabel(fullRangeReminder, SwingConstants.CENTER);
        fullRangeLabel.setFont(font);
        fullRangeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(fullRangeLabel);

        // Next Label: "Example: 1-10, 17, 230-244"
        JLabel visibleStripsLabel = new JLabel("Disconnected ranges e.g. \"1-10, 17, 230-244\"", SwingConstants.CENTER);
        visibleStripsLabel.setFont(font);
        visibleStripsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(visibleStripsLabel);


        // Single text field for range input
        rangeField = new JTextField(30);
        rangeField.setText(_rangeText);
        rangeField.setFont(font);
        rangeField.setMaximumSize(new Dimension(200, 25));
        rangeField.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(rangeField);

        // Add listeners to the text field
        ActionListener updateCallback = e -> updateRange();
        FocusAdapter focusListener = new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                updateRange();
            }
        };

        rangeField.addActionListener(updateCallback);
        rangeField.addFocusListener(focusListener);
    }

    private void updateRange() {
        String input = rangeField.getText().trim();
        if (isValidRange(input)) {
            _rangeText = input;
			if (_view != null) {
				_view.refresh();
			}
        } else {
            JOptionPane.showMessageDialog(this, "Invalid range format. Use formats like '1-10, 17, 230-244'.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            rangeField.setText(_rangeText);
        }
    }

    private boolean isValidRange(String input) {
        String regex = "^\\s*(\\d+(-\\d+)?)(\\s*,\\s*\\d+(-\\d+)?)*\\s*$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }

    public boolean showStrip(int strip) {
        String[] parts = _rangeText.split(",");
        for (String part : parts) {
            part = part.trim();
            if (part.contains("-")) {
                String[] bounds = part.split("-");
                int min = Integer.parseInt(bounds[0].trim());
                int max = Integer.parseInt(bounds[1].trim());
                if (strip >= min && strip <= max) {
                    return true;
                }
            } else {
                int single = Integer.parseInt(part);
                if (strip == single) {
                    return true;
                }
            }
        }
        return false;
    }


}
