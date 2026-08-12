package cnuphys.bCNU.graphics.colorscale;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import cnuphys.bCNU.graphics.GraphicsUtilities;
import edu.cnu.mdi.component.CommonBorder;
import cnuphys.bCNU.view.BaseView;

public class ColorModelPanel extends JPanel implements ActionListener {

    private ColorModelLegend _legend;
    private ColorScaleModel _model;
    private ColorScaleModel _monoModel;
    private JRadioButton _colorRB;
    private JRadioButton _monoRB;
    private BaseView _view;

    public ColorModelPanel(ColorScaleModel model, int desiredWidth, String name, int gap, double initRelVal) {
        this(null, model, desiredWidth, name, gap, false, false);
    }

    public ColorModelPanel(BaseView view, ColorScaleModel model, int desiredWidth, String name, int gap,
                           boolean includeRadioButtons, boolean colorDefault) {

        _view = view;
        _model = model;

        setLayout(new BorderLayout(2, 1)); // Reduced vertical gap
        _legend = new ColorModelLegend(_model, desiredWidth, null, gap);
        _legend.setBorder(null);

        if (includeRadioButtons) {
            addNorth(colorDefault);
        }

        add(_legend, BorderLayout.CENTER); // Center instead of SOUTH to reduce extra space
        setBorder(new CommonBorder(name));
    }

    private void addNorth(boolean colorDefault) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0)); // Smaller gaps

        ButtonGroup bg = new ButtonGroup();

        _colorRB = new JRadioButton("Color", colorDefault);
        _monoRB = new JRadioButton("Monochrome", !colorDefault);

        _colorRB.addActionListener(this);
        _monoRB.addActionListener(this);

        bg.add(_colorRB);
        bg.add(_monoRB);

        GraphicsUtilities.setSizeSmall(_colorRB);
        GraphicsUtilities.setSizeSmall(_monoRB);

        panel.add(_colorRB);
        panel.add(_monoRB);

        add(panel, BorderLayout.NORTH);
    }

    @Override
    public Insets getInsets() {
        Insets def = super.getInsets();
        return new Insets(def.top + 1, def.left + 1, def.bottom + 1, def.right + 1); // Smaller insets
    }

    public boolean isMonochrome() {
        return (_monoRB != null) && _monoRB.isSelected();
    }

    public ColorScaleModel getColorScaleModel() {
        return (_legend != null) ? _legend.getColorScaleModel() : null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == _colorRB) {
            _legend.setColorScaleModel(_model);
        } else if (e.getSource() == _monoRB) {
            if (_monoModel == null) {
                _monoModel = ColorScaleModel.getMonochromeModel(_model);
            }
            _legend.setColorScaleModel(_monoModel);
        }

        if (_view != null) {
            _view.refresh();
        }
    }
}
