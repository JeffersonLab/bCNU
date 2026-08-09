package cnuphys.ced.alldata;

import java.awt.Color;


/**
 * Static methods to support ADC banks
 */
public class ADCSupport {
	
	// the ced data warehouse
	private static DataWarehouse _dataWarehouse = DataWarehouse.getInstance();
	
	//color for zero adc value
	private static final Color ADCZERO = new Color(40, 40, 120, 18);

	
	
	/**
	 * Get the ADC values for the specified bank
	 * @param bankName the name of the bank
	 * @return the ADC values or null if the bank does not exist
	 */
    public static int[] getADC(String bankName) {
		int[] adc = null;

		if (_dataWarehouse.hasBank(bankName)) {
			adc = _dataWarehouse.getInt(bankName, "ADC");
		}

		return adc;
    	
    }

    /**
     * Get the max ADC value for the specified bank
     * @param bankName the name of the bank
     * @return the max ADC value or 0 if the bank does not exist
     */
	public static int getMaxADC(String bankName) {
		int maxADC = 0;
		if (_dataWarehouse.hasBank(bankName)) {
			maxADC = DataWarehouse.getMaxIntValue(bankName, "ADC");
		}
		return maxADC;
	}
	
	/**
	 * Get the color for a given adc value
	 * @param adc the adc value
	 * @return the color
	 */
	public static Color getADCColor(String bankName, int adc) {
		
		if (adc > 0) {
			int maxADC = getMaxADC(bankName);
			if (maxADC <= 0) {
				return ADCZERO;
			}
			double fract = ((double) adc) / maxADC;
			fract = Math.max(0, Math.min(1.0, fract));

			return AdcColorScale.getInstance().getColor(fract);
		}
		return ADCZERO;
	}


	/**
	 * Get the color for a given adc value
	 * @param adc the adc value
	 * @return the color with some transparency
	 */
	public static Color getADCAlphaColor(String bankName, int adc) {
		if (adc > 0) {
			int maxADC = getMaxADC(bankName);
			if (maxADC <= 0) {
				return ADCZERO;
			}
			double fract = ((double) adc) / maxADC;
			fract = Math.max(0, Math.min(1.0, fract));
			int alpha = 128 + (int) (127 * fract);
			alpha = Math.min(255, alpha);

			return AdcColorScale.getInstance().getAlphaColor(fract, alpha);
		}
		return ADCZERO;
	}

}
