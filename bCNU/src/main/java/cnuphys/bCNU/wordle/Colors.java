package cnuphys.bCNU.wordle;

import java.awt.Color;

import cnuphys.bCNU.util.X11Colors;

public class Colors {

	public static final int NEUTRAL = 0;
	public static final int WRONG = 1;
	public static final int RIGHT = 2;

	public static final Color colors[] = {new Color(160, 160, 160),
			X11Colors.getX11Color("Goldenrod"),
			X11Colors.getX11Color("Medium Sea Green")};

}
