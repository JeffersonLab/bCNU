package cnuphys.ced.cedwindow;

import java.util.OptionalLong;

import javax.swing.JCheckBox;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import cnuphys.bCNU.util.Bits;

final class ColumnVisibility {

	private ColumnVisibility() {}

	static long selectedMask(JCheckBox[] checkBoxes) {
		boolean[] selected = new boolean[Math.min(checkBoxes.length, 63)];
		for (int i = 0; i < selected.length; i++) selected[i] = checkBoxes[i].isSelected();
		return selectedMask(selected);
	}

	static long selectedMask(boolean[] selected) {
		long mask = 1;
		for (int i = 0; i < Math.min(selected.length, 63); i++) {
			if (selected[i]) mask = Bits.setBitAtLocation(mask, i + 1);
		}
		return mask;
	}

	static OptionalLong parseMask(String value) {
		if (value == null) return OptionalLong.empty();
		try {
			return OptionalLong.of(Long.parseLong(value));
		} catch (NumberFormatException exception) {
			return OptionalLong.empty();
		}
	}

	static void applyMask(long mask, JCheckBox[] checkBoxes, TableColumnModel columns) {
		for (int i = 0; i < Math.min(checkBoxes.length, 63); i++) {
			int columnIndex = i + 1; // column zero contains the row index
			boolean visible = isVisible(mask, i);
			checkBoxes[i].setSelected(visible);
			if (!visible) hide(columns.getColumn(columnIndex));
		}
	}

	static boolean isVisible(long mask, int checkBoxIndex) {
		return Bits.checkBitAtLocation(mask, checkBoxIndex + 1);
	}

	private static void hide(TableColumn column) {
		column.setMinWidth(0);
		column.setMaxWidth(0);
		column.setResizable(false);
		column.setPreferredWidth(0);
	}
}
