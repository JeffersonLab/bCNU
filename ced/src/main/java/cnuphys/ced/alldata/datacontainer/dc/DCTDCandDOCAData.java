package cnuphys.ced.alldata.datacontainer.dc;

import java.util.List;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

import cnuphys.ced.alldata.datacontainer.ACommonTDCData;
import cnuphys.lund.DoubleFormat;

public class DCTDCandDOCAData extends ACommonTDCData {

	private static final int TOTALNUMWIRE = 24192;
	private static final int TOTALNUMWIRESECTOR = 4032;


	// for feedback strings
	private static final String _fbColor = "$Orange$";

	// singleton
	private static volatile DCTDCandDOCAData _instance;

	/**
	 * Public access to the singleton
	 *
	 * @return the singleton
	 */
	public static DCTDCandDOCAData getInstance() {
		if (_instance == null) {
			synchronized (DCTDCandDOCAData.class) {
				if (_instance == null) {
					_instance = new DCTDCandDOCAData();
				}
			}
		}
		return _instance;
	}

	/** raw data has layer 1..36. Convert to 1..6 */
	public byte layer6[];

	/** superlayer 1..6 */
	public byte superlayer[];

	/** does snr think this is noise? */
	public boolean noise[];

	/**left right value */
	public byte LR[];

	/** doca value */
	public float doca[];

	/** sdoca value */
	public float sdoca[];

	/** time value */
	public float time[];

	/** stime value */
	public float stime[];
	
	/** for dc::tot bank */
	public short tot[];

	//counts hits in each sector
	private int _sectorCounts[] = new int[6];

	//counts hits in each superlayer
	private int _superlayerCounts[][] = new int[6][6];


	@Override
	public void clear() {
		super.clear();
		layer6 = null;
		superlayer = null;
		noise = null;
		LR = null;
		doca = null;
		sdoca = null;
		time = null;
		stime = null;

		for (int i = 0; i < 6; i++) {
			_sectorCounts[i] = 0;
			for (int j = 0; j < 6; j++) {
                _superlayerCounts[i][j] = 0;
            }
		}

	}
	
	private boolean isEmpty(DataBank bank) {
		return (bank == null) || (bank.rows() == 0);
	}

	@Override
	public void update(DataEvent event) {
		DataBank bank = event.getBank("DC::tdc");
		
		boolean dctot = false;
		
		//new altbank starting with coatjava 1.3.2
		if (isEmpty(bank)) {
			bank = event.getBank("DC::tot");
			dctot = true;
		}

		if (isEmpty(bank)) {
			return;
		}

        sector = bank.getByte("sector");
        layer = bank.getByte("layer");
        component = bank.getShort("component");
        order = bank.getByte("order");
        tdc = bank.getInt("TDC");
        
		if (dctot) {
			tot = bank.getShort("ToT");
		} else {
			tot = null;
		}

        //get the layer6 and superlayer from the layer
        if (layer != null) {
			int len = layer.length;
			layer6 = new byte[len];
			superlayer = new byte[len];

			noise = new boolean[len];

			for (int i = 0; i < len; i++) {
				byte layer36 = layer[i];
				superlayer[i] = (byte) (((layer36 - 1) / 6) + 1);
				layer6[i] = (byte) (((layer36 - 1) % 6) + 1);

				noise[i] = false;

				_sectorCounts[sector[i] - 1]++;
				_superlayerCounts[sector[i] - 1][superlayer[i] - 1]++;
			}
        }

        //and from doca bank
        DataBank docaBank = event.getBank("DC::doca");
		if (docaBank == null) {
			return;
		}
		LR = docaBank.getByte("LR");
		doca = docaBank.getFloat("doca");
		sdoca = docaBank.getFloat("sdoca");
		time = docaBank.getFloat("time");
		stime = docaBank.getFloat("stime");


	}

	/**
	 * Get a string for just the tdc data
	 *
	 * @return a string for just the tdc data
	 */
	public String tdcString(int index) {
		if (tdc[index] < 0) {
			return "";
		} else {
			if (tot == null) {
				return "tdc " + tdc[index] + "  order " + order[index];
			} else {
				return "tdc " + tdc[index] + "  order " + order[index] + "  ToT: " + tot[index];
			}
		}
	}

	// make a sensible doca string
	private String docaString(float d, float t) {
		if (Float.isNaN(d) || Float.isNaN(t)) {
			return "";
		}
		String dStr = DoubleFormat.doubleFormat(d, 3);
		String tStr = DoubleFormat.doubleFormat(t, 3);
		return "DC (" + dStr + " mm, " + tStr + ")";
	}


	/**
	 * Common feedback
	 * @param index the index of the data
	 * @param feedbackStrings the list of feedback strings
	 */
	public void tdcFeedback(int index, List<String> feedbackStrings) {
		feedbackStrings.add(_fbColor + "sect " + sector[index] +
				" suplay " + superlayer[index] + " layer " + layer6[index] + " wire " + component[index]);

		feedbackStrings.add(_fbColor + "DC tdc " + tdc[index] + "  order " + order[index]);
	}


	/**
	 * Add to the feedback list
	 *
	 * @param showNoise       if <code>true<code> add string for noise status
	 * @param showDoca        if <code>true<code> add string for doca data (if
	 *                        present)
	 * @param feedbackStrings
	 */
	public void tdcFeedback(int index, boolean showNoise, boolean showDoca, List<String> feedbackStrings) {


		feedbackStrings.add(_fbColor + "DC sector " + sector[index] +
				" suplay " + superlayer[index] + " layer " + layer6[index] + " wire " + component[index]);

		String tdcStr = tdcString(index);
		if (tdcStr.length() > 3) {
			feedbackStrings.add(_fbColor + tdcStr);
		}

		if (showNoise && (noise != null) && (noise.length > index)) {
			feedbackStrings.add(_fbColor + "DC Noise guess " + (noise[index] ? "noise" : "not noise"));
		}

		if (showDoca && (doca != null) && (doca.length > index) && (time != null)) {
			String dstr = docaString(doca[index], time[index]);
			if (dstr.length() > 3) {
				feedbackStrings.add(_fbColor + "DC SIM (doca, time) " + dstr);
			}
			String sdstr = docaString(sdoca[index], stime[index]);
			if (dstr.length() > 3) {
				feedbackStrings.add(_fbColor + "DC SIM (sdoca, stime) " + sdstr);
			}
		}

	}

	/**
	 * total DC occupancy all sectors all layers
	 *
	 * @return total DC occupancy
	 */
	public double totalOccupancy() {
		return ((double) count()) / TOTALNUMWIRE;
	}

	/**
	 * total DC occupancy for a sector
	 * @param sector the 1-based sector
	 * @return total DC occupancy for a sector
	 */
	public double totalSectorOccupancy(int sector) {
		if ((sector > 0) && (sector < 7)) {
			return ((double) _sectorCounts[sector-1]) / TOTALNUMWIRESECTOR;
		} else {
			return 0.;
		}
	}

	/**
	 * total DC occupancy for a sector
	 *
	 * @return total DC occupancy for a sector
	 */
	public double totalSuperlayerOccupancy(int sector, int superlayer) {
		if ((sector > 0) && (sector < 7)) {
			return (_superlayerCounts[sector-1][superlayer-1]) / 672.;
		} else {
			return 0.;
		}
	}


}
