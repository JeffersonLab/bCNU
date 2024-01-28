package cnuphys.ced.alldata.datacontainer.cal;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

public class PCalReconData extends ACalReconDataContainer {

	// singleton
	private static volatile  PCalReconData _instance;
	
	/**
	 * Public access to the singleton
	 *
	 * @return the singleton
	 */
	public static PCalReconData getInstance() {
		if (_instance == null) {
			synchronized (PCalReconData.class) {
				if (_instance == null) {
					_instance = new PCalReconData();
				}
			}
		}
		return _instance;
	}
	
	@Override
	public void update(DataEvent event) {
		
		//don't need recon data if accumulating
		if (_eventManager.isAccumulating()) {
			return;
		}
		
		DataBank bank = event.getBank("REC::Calorimeter");
		if (bank == null) {
			return;
		}
		
		byte[] sectorArray = bank.getByte("sector");
		if (sectorArray != null) {
			// layers are 1..3 for PCAL and 4..9 for EC
			byte layerArray[] = bank.getByte("layer");
			float timeArray[] = bank.getFloat("time");
			float energyArray[] = bank.getFloat("energy");
			float xArray[] = bank.getFloat("x");
			float yArray[] = bank.getFloat("y");
			float zArray[] = bank.getFloat("z");
			short pindexArray[] = bank.getShort("pindex");

			for (int i = 0; i < sectorArray.length; i++) {

				if (layerArray[i] < 4) { // means it is PCAL, not ECAL
					byte layer = (byte) (layerArray[i] - 1); // 0..2
					byte view0 = (byte) (layer % 3); // 0, 1, 2 for U, V, W

					sector.add(sectorArray[i]);
					view.add(view0);
					
					time.add(timeArray[i]);
					energy.add(energyArray[i]);
					x.add(xArray[i]);
					y.add(yArray[i]);
					z.add(zArray[i]);
					
					pIndex.add(pindexArray[i]);
						
				}
			}
			
			//get the pids from the REC::Particle bank
			getPIDArray(event);
		} // end sectorArray not null	
	} // end update

}
