package cnuphys.ced.component;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractButton;

import edu.cnu.mdi.component.checkboxarray.CheckBoxArray;
import edu.cnu.mdi.component.CommonBorder;
import cnuphys.bCNU.util.Bits;
import edu.cnu.mdi.ui.colors.X11Colors;
import cnuphys.ced.cedview.CedView;

public class GeoDisplayArray extends CheckBoxArray implements ItemListener {

	public static final String REGION_1 = "Region 1";

	public static final String REGION_2 = "Region 2";

	public static final String REGION_3 = "Region 3";

	public static final String REGION_4 = "Region 4";

	public static final String LAYER_1 = "Layer 1";

	public static final String LAYER_2 = "Layer 2";

	public static final String LAYER_3 = "Layer 3";

	public static final String LAYER_4 = "Layer 4";

	public static final String LAYER_5 = "Layer 5";

	public static final String LAYER_6 = "Layer 6";

	// controls whether region1 displayed
		private AbstractButton _showRegion1;

		// controls whether region2 displayed
		private AbstractButton _showRegion2;

		// controls whether region3 displayed
		private AbstractButton _showRegion3;

		// controls whether region4 displayed
		private AbstractButton _showRegion4;

		// controls whether layer1 displayed
		private AbstractButton _showLayer1;

		// controls whether layer2 displayed
		private AbstractButton _showLayer2;

		// controls whether layer3 displayed
		private AbstractButton _showLayer3;

		// controls whether layer4 displayed
		private AbstractButton _showLayer4;

		// controls whether layer5 displayed
		private AbstractButton _showLayer5;

		// controls whether layer6 displayed
		private AbstractButton _showLayer6;

		private static final Color _buttonColor = X11Colors.getX11Color("Dark Red");

		// the parent view
		private CedView _view;


		/**
		 * Create a display flag array. This constructor produces a two column array.
		 *
		 * @param view the parent view
		 * @param bits controls what flags are added
		 */
		public GeoDisplayArray(CedView view, int bits, int nc, int hgap) {
			super(nc, hgap, -2);
			_view = view;

			if (Bits.checkBit(bits, GeoDisplayBits.REGION_1)) {
				_showRegion1 = add(REGION_1, true, true, this, _buttonColor).getCheckBox();
			}

			if (Bits.checkBit(bits, GeoDisplayBits.REGION_2)) {
				_showRegion2 = add(REGION_2, true, true, this, _buttonColor).getCheckBox();
			}

			if (Bits.checkBit(bits, GeoDisplayBits.REGION_3)) {
				_showRegion3 = add(REGION_3, true, true, this, _buttonColor).getCheckBox();
			}

			if (Bits.checkBit(bits, GeoDisplayBits.REGION_4)) {
				_showRegion4 = add(REGION_4, true, true, this, _buttonColor).getCheckBox();
			}

			if (Bits.checkBit(bits, GeoDisplayBits.LAYER_1)) {
				_showLayer1 = add(LAYER_1, true, true, this, _buttonColor).getCheckBox();
			}

			if (Bits.checkBit(bits, GeoDisplayBits.LAYER_2)) {
				_showLayer2 = add(LAYER_2, true, true, this, _buttonColor).getCheckBox();
			}

			if (Bits.checkBit(bits, GeoDisplayBits.LAYER_3)) {
				_showLayer3 = add(LAYER_3, true, true, this, _buttonColor).getCheckBox();
			}

			if (Bits.checkBit(bits, GeoDisplayBits.LAYER_4)) {
				_showLayer4 = add(LAYER_4, true, true, this, _buttonColor).getCheckBox();
			}

			if (Bits.checkBit(bits, GeoDisplayBits.LAYER_5)) {
				_showLayer5 = add(LAYER_5, true, true, this, _buttonColor).getCheckBox();
			}

			if (Bits.checkBit(bits, GeoDisplayBits.LAYER_6)) {
				_showLayer6 = add(LAYER_6, true, true, this, _buttonColor).getCheckBox();
			}

			setBorder(new CommonBorder("Geometry Display"));
		}


		@Override
		public void itemStateChanged(ItemEvent arg0) {
			// repaint the view
			if (_view != null) {
				_view.getContainer().refresh();
			}
		}

		public boolean showRegion1() {
			return (_showRegion1 != null) && _showRegion1.isSelected();
		}

		public boolean showRegion2() {
			return (_showRegion2 != null) && _showRegion2.isSelected();
		}

		public boolean showRegion3() {
			return (_showRegion3 != null) && _showRegion3.isSelected();
		}

		public boolean showRegion4() {
			return (_showRegion4 != null) && _showRegion4.isSelected();
		}

		public boolean showLayer1() {
			return (_showLayer1 != null) && _showLayer1.isSelected();
		}

		public boolean showLayer2() {
			return (_showLayer2 != null) && _showLayer2.isSelected();
		}

		public boolean showLayer3() {
			return (_showLayer3 != null) && _showLayer3.isSelected();
		}

		public boolean showLayer4() {
			return (_showLayer4 != null) && _showLayer4.isSelected();
		}

		public boolean showLayer5() {
			return (_showLayer5 != null) && _showLayer5.isSelected();
		}

		public boolean showLayer6() {
			return (_showLayer6 != null) && _showLayer6.isSelected();
		}



}
