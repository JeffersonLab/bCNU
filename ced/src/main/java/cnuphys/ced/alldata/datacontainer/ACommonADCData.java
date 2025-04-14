package cnuphys.ced.alldata.datacontainer;

import java.awt.Color;
import java.util.List;

import cnuphys.ced.alldata.ADCSupport;
import cnuphys.ced.alldata.DataWarehouse;
import cnuphys.ced.clasio.ClasIoEventManager;

public abstract class ACommonADCData implements IDataContainer {


	// the data warehouse
	protected static DataWarehouse _dataWarehouse = DataWarehouse.getInstance();

	// event manager
	protected static ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();


	/** 1-based sectors */
	public byte[] sector;

	/** 1-based layer*/
	public byte layer[];

	/** 1-based component*/
	public short component[];

	/** Used for subdivisions like left/right */
	public byte order[];

	/** adc value */
	public int adc[];

	/** time value */
	public float time[];
	
	//the adc bank name
	private String bankName;

	/**
	 * Create a data container and notify the data warehouse that it wants to be
	 * notified of data events.
     */
	public ACommonADCData(String bankName) {
		this.bankName = bankName;
		_dataWarehouse.addDataContainerListener(this);
	}


	@Override
	public void clear() {
		sector = null;
		layer = null;
		component = null;
		order = null;
		adc = null;
		time = null;
	}

	@Override
	public int count() {
		return (sector == null) ? 0 : sector.length;
	}


	/**
	 * Get the color for a given adc value
	 * @param adc the adc value
	 * @return the color
	 */
	public Color getADCColor(int adc) {
		return ADCSupport.getADCColor(bankName, adc);
	}



	/**
	 * Common feedback format for ADC values
	 * @param detectorName the name of the detector
	 * @param index the index of the data
	 * @param feedbackStrings the list of feedback strings
	 */
	public void adcFeedback(String detectorName, int index, List<String> feedbackStrings) {
		feedbackStrings.add(String.format("$cyan$%s adc %d time %6.3f order %d", detectorName, adc[index], time[index], order[index]));
	}

}
