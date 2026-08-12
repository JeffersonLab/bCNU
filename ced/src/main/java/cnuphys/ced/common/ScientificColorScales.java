package cnuphys.ced.common;

import java.awt.Color;

import edu.cnu.mdi.ui.colors.ScientificColorMap;

/** Utilities for adapting MDI scientific color maps to CED color models. */
public final class ScientificColorScales {

	public static final int DEFAULT_SAMPLE_COUNT = 256;

	private ScientificColorScales() {
	}

	public static Color[] sample(ScientificColorMap colorMap) {
		return sample(colorMap, DEFAULT_SAMPLE_COUNT);
	}

	static Color[] sample(ScientificColorMap colorMap, int sampleCount) {
		if (colorMap == null) {
			throw new IllegalArgumentException("A scientific color map is required");
		}
		if (sampleCount < 2) {
			throw new IllegalArgumentException("At least two color samples are required");
		}

		Color[] colors = new Color[sampleCount];
		for (int i = 0; i < sampleCount; i++) {
			colors[i] = colorMap.colorAt((double) i / (sampleCount - 1));
		}
		return colors;
	}
}
