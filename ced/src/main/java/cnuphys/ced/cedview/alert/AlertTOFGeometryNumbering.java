package cnuphys.ced.cedview.alert;

import cnuphys.ced.geometry.alert.TOFLayer;

public class AlertTOFGeometryNumbering {

	public int sector;  //0-based
	public int superlayer; //0-based
	public int layer; //0-based
	public int paddleIndex; //0-based (paddle index)


	/**
	 * Create an ALert TOF geometry numbering
	 */
	public AlertTOFGeometryNumbering() {		// TODO Auto-generated constructor stub
	}

	/**
	 * Set the geometry numbering from a data (HIPO) numbering
	 * @param sect the 0-based sector
	 * @param layer the 0-based layer
	 * @param comp the 10-based componentID (10, 0-9)
	 * @param order
	 */
	public boolean fromHipoNumbering(int sect, int lay, int comp, int order) {

		//ALERT TOF hipo data is 0 -based

		if (sect < 0 || sect > 14) {
			return false;
        }

		if (lay < 0 || lay > 3) {
			return false;
        }

		if (comp < 0 || comp > 10) {
			return false;
		}

		sector = sect;
		layer = lay;
		paddleIndex = comp % 10; //[0, 0-9]
		superlayer = (comp == 10) ? 0 : 1;
		return true;
	}

	/**
     * Match a DC layer
     * @param tofl the DC layer
     * @return <code>true</code> if the DC layer matches
     */
	public boolean match(TOFLayer tofl) {
		return (tofl.sector == sector) && (tofl.superlayer == superlayer) && (tofl.layer == layer);
	}

}
