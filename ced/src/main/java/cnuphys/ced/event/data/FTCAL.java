package cnuphys.ced.event.data;

import org.jlab.io.base.DataEvent;

public class FTCAL extends DetectorData {

	AdcLRHitList _adcHits = new AdcLRHitList("FTCAL::adc");

	private static FTCAL _instance;

	/**
	 * Public access to the singleton
	 *
	 * @return the FTOF singleton
	 */
	public static FTCAL getInstance() {
		if (_instance == null) {
			_instance = new FTCAL();
		}
		return _instance;
	}

	@Override
	public void newClasIoEvent(DataEvent event) {
		_adcHits = new AdcLRHitList("FTCAL::adc");
	}

	/**
	 * Update the list. This is probably needed only during accumulation
	 *
	 * @return the updated list
	 */
	public AdcLRHitList updateAdcList() {
		_adcHits = new AdcLRHitList("FTCAL::adc");
		return _adcHits;
	}

	/**
	 * Get the adc hit list
	 *
	 * @return the adc hit list
	 */
	public AdcLRHitList getHits() {
		return _adcHits;
	}
}