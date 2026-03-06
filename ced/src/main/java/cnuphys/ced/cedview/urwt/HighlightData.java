package cnuphys.ced.cedview.urwt;

/**
 * This will hold the highlighted data from uRWell banks
 * resulting from clicking on one of the bank rows
 * @author heddle
 *
 */
public class HighlightData {

	/** 0-based index of highlight hit, none if <0 */
	public int hit = -1;

	/** 0-based index of highlight cluster, none if <0 */
	public int cluster = -1;

	/** 0-based index of highlight cross, none if <0 */
	public int cross = -1;



	public HighlightData() {
	}

	/**
	 * Reset to starting values (which indicate no highlight)
	 */
	public void reset() {
		hit = -1;
		cluster = -1;
		cross = -1;
	}

}
