package cnuphys.ced.alldata.datacontainer.cnd;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

import cnuphys.ced.alldata.datacontainer.ACommonADCData;

public class CNDADCData extends ACommonADCData {
	
	//adc bank name
	public static final String BANK_NAME = "CND::adc";

	// singleton
	private static volatile CNDADCData _instance;
	
	public CNDADCData() {
		super(BANK_NAME);
	}

	/**
	 * Public access to the singleton
	 *
	 * @return the singleton
	 */
	public static CNDADCData getInstance() {
		if (_instance == null) {
			synchronized (CNDADCData.class) {
				if (_instance == null) {
					_instance = new CNDADCData();
				}
			}
		}
		return _instance;
	}


	@Override
	public void update(DataEvent event) {
		DataBank bank = event.getBank(BANK_NAME);

		if (bank == null) {
			return;
		}

        sector = bank.getByte("sector");
        layer = bank.getByte("layer");
        component = bank.getShort("component");
        order = bank.getByte("order");
        adc = bank.getInt("ADC");
        time = bank.getFloat("time");
	}

}
