package cnuphys.ced.alldata.datacontainer.bmt;

import java.awt.Point;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

import cnuphys.ced.alldata.DataDrawSupport;
import cnuphys.ced.alldata.datacontainer.ACommonADCData;

public class BMTADCData extends ACommonADCData {

	// singleton
	private static volatile BMTADCData _instance;


	/** cached x coordinate of drawing locations */
	public int ppx[];

	/** cached y coordinate of drawing locations */
	public int ppy[];
	
	private static String bankName = "BMT::adc";


	public BMTADCData() {
		super(bankName);
	}

	/**
	 * Public access to the singleton
	 *
	 * @return the singleton
	 */
	public static BMTADCData getInstance() {
		if (_instance == null) {
			synchronized (BMTADCData.class) {
				if (_instance == null) {
					_instance = new BMTADCData();
				}
			}
		}
		return _instance;
	}

	@Override
	public void clear() {
		super.clear();
		ppx = null;
		ppy = null;
	}

	@Override
	public void update(DataEvent event) {
		DataBank bank = event.getBank(bankName);

		if (bank == null) {
			return;
		}

        sector = bank.getByte("sector");
        layer = bank.getByte("layer");
        component = bank.getShort("component");
        order = bank.getByte("order");
        adc = bank.getInt("ADC");
        time = bank.getFloat("time");

        int n = (sector != null) ? sector.length : 0;
 		if (n > 0) {
 			ppx = new int[n];
 			ppy = new int[n];
 		}
	}


	/**
	 * Used for hit detection
	 * @param index the cluster index
	 * @param pp the screen point
	 * @return true if the screen point is in the cluster
	 */
	public boolean contains(int index, Point pp) {
		return ((Math.abs(ppx[index] - pp.x) <= DataDrawSupport.HITHALF)
				&& (Math.abs(ppy[index] - pp.y) <= DataDrawSupport.HITHALF));
	}


	/**
	 * Set the location where the cluster was last drawn
	 *
	 * @param index the index of the cluster
	 * @param pp    the location
	 */
	public void setLocation(int index, Point pp) {

		int n = (sector == null) ? 0 : sector.length;
		if (n == 0) {
			return;
		}

		if ((ppx == null) || (ppy == null)) {
			ppx = new int[n];
			ppy = new int[n];
		}
		ppx[index] = pp.x;
		ppy[index] = pp.y;
	}

}