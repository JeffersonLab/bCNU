package cnuphys.ced.noise;

import java.awt.Color;

import org.jlab.io.base.DataEvent;

import cnuphys.ced.alldata.DCRawHits;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.ClasIoEventListenerPhase;
import cnuphys.ced.clasio.IClasIoEventListener;
import cnuphys.snr.NoiseReductionParameters;
import cnuphys.snr.SNRAnalysisLevel;
import cnuphys.snr.clas12.Clas12NoiseAnalysis;
import cnuphys.snr.clas12.Clas12NoiseResult;

public class NoiseManager implements IClasIoEventListener {

	/** noise mask color for left benders */
	public static final Color maskFillLeft = new Color(255, 128, 0, 48);

	/** noise mask color for right benders */
	public static final Color maskFillRight = new Color(0, 128, 255, 48);

	// singleton
	private static volatile NoiseManager instance;

	// The analysis package
	private Clas12NoiseAnalysis noisePackage = new Clas12NoiseAnalysis(SNRAnalysisLevel.TWOSTAGE);

	// result container
	private Clas12NoiseResult _noiseResults = new Clas12NoiseResult();

	// event manager
	private ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();

	// data containers
	private static DCRawHits _dcData = DCRawHits.getInstance();


	// private constructor
	private NoiseManager() {
		// I need to be notified before the views
		_eventManager.addClasIoEventListener(this, ClasIoEventListenerPhase.DERIVED);
	}

	/**
	 * Public access to the singleton
	 *
	 * @return the NoiseManager singleton
	 */
	public static NoiseManager getInstance() {
		if (instance == null) {
			synchronized (NoiseManager.class) {
				if (instance == null) {
					instance = new NoiseManager();
				}
			}
		}
		return instance;
	}

	/**
	 * Get the parameters for a given 0-based superlayer
	 *
	 * @param sect0 the 0-based sector
	 * @param supl0 the 0-based superlayer in question
	 * @return the parameters for that superlayer
	 */
	public NoiseReductionParameters getParameters(int sect0, int supl0) {
		return noisePackage.getParameters(sect0, supl0);
	}



	@Override
	public void newClasIoEvent(DataEvent event) {
		noisePackage.clear();
		_noiseResults.clear();

		int count = _dcData.count();
		if (count > 0) {
			int sector[] = new int[count];
			int superlayer[] = new int[count];
			int layer[] = new int[count];
			int wire[] = new int[count];
			for (int i = 0; i < count; i++) {
				sector[i] = _dcData.sector(i);
				superlayer[i] = _dcData.superlayer(i);
				layer[i] = _dcData.layerInSuperlayer(i);
				wire[i] = _dcData.wire(i);
			}

			noisePackage.findNoise(sector, superlayer, layer, wire, _noiseResults);
			_dcData.setNoiseFlags(_noiseResults.noise);

		}

	}

	@Override
	public void openedNewEventFile(String path) {
	}

	/**
	 * Change the event source type
	 *
	 * @param source the new source: File, ET
	 */
	@Override
	public void changedEventSource(ClasIoEventManager.EventSourceType source) {
	}


}
