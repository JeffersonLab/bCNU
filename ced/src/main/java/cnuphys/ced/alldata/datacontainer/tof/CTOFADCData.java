package cnuphys.ced.alldata.datacontainer.tof;

import java.awt.Color;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

import cnuphys.ced.alldata.ADCSupport;
import cnuphys.ced.alldata.datacontainer.ACommonADCData;
import cnuphys.ced.alldata.datacontainer.AdcColorScale;

public class CTOFADCData extends ACommonADCData {
	// singleton
	private static volatile CTOFADCData _instance;
	
	// adc bank name
	public static final String BANK_NAME = "CTOF::adc";
	
	public CTOFADCData() {
		super(BANK_NAME);
	}

	/**
	 * Public access to the singleton
	 *
	 * @return the singleton
	 */
	public static CTOFADCData getInstance() {
		if (_instance == null) {
			synchronized (CTOFADCData.class) {
				if (_instance == null) {
					_instance = new CTOFADCData();
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

	/**
	 * Get the adc color for a given sector, layer, component, and order
	 * @param sector the 1-based sector
	 * @param layer the 1-based layer
	 * @param component the 1-based component
	 * @param order the order, 0 or 1 for left/right
	 * @return the color, or null if no data
	 */
	public Color getColor(byte sector, byte layer, short component, byte order) {

		int adc = getComponentAverageADC(sector, layer, component, order);
		if (adc > 0) {
			int maxADC = ADCSupport.getMaxADC(BANK_NAME);
			double fract = ((double) adc) / maxADC;
			fract = Math.max(0, Math.min(1.0, fract));
			return AdcColorScale.getInstance().getAlphaColor(fract, 255);
		}
		return ADCZERO;
	}

	//gets the average ADC for a given sector, layer, component, and order
	private int getComponentAverageADC(byte sector, byte layer, short component, byte order) {
		int count = 0;
		int sum = 0;
		for (int i = 0; i < this.sector.length; i++) {
			if ((this.sector[i] == sector) && (this.layer[i] == layer)
					&& (this.component[i] == component) && (this.order[i] == order)) {
				sum += adc[i];
				count++;
			}
		}

		return (count > 0) ? sum / count : 0;
	}


}