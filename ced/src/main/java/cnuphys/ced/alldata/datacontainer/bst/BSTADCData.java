package cnuphys.ced.alldata.datacontainer.bst;

import java.util.List;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

import cnuphys.ced.alldata.datacontainer.ACommonADCData;

public class BSTADCData  extends ACommonADCData {

	// singleton
	private static volatile BSTADCData _instance;
	
	//adc bank name
	private static final String bankName = "BST::adc";
	
	public BSTADCData() {
		super(bankName);
	}

	/**
	 * Public access to the singleton
	 *
	 * @return the singleton
	 */
	public static BSTADCData getInstance() {
		if (_instance == null) {
			synchronized (BSTADCData.class) {
				if (_instance == null) {
					_instance = new BSTADCData();
				}
			}
		}
		return _instance;
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
	}


	/**
	 * Common feedback format for ADC values
	 * @param index the index of the data
	 * @param feedbackStrings the list of feedback strings
	 */
	public void adcFeedback(int index, List<String> feedbackStrings) {
		feedbackStrings.add(String.format("$cyan$BST strip %d adc %d time %6.3f order %d",
				component[index], adc[index], time[index], order[index]));
	}

}
