package cnuphys.ced.clasio;

import java.io.File;
import java.nio.file.Path;

import org.jlab.detector.decode.CLASDecoder4;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioSource;
import org.jlab.io.hipo.HipoDataEvent;
import org.jlab.jnp.hipo4.data.Event;

public class IOTest {
	
	private static CLASDecoder4 decoder;

	public static void main(String[] args) {
		String home = System.getProperty("user.home");

		Path filepath = Path.of(home, "data", "alert", "alert_021123.evio");
		File file = filepath.toFile();
				
		EvioSource dataSource = new EvioSource();
		dataSource.open(file.getPath());

		System.out.println("Size = " + dataSource.getSize());

		for (int i = 0; i < 20; i++) {
			if (dataSource.hasEvent()) {
				DataEvent event = dataSource.getNextEvent();

				if (decoder == null) {
					decoder = new CLASDecoder4();			
				}
				if (event instanceof EvioDataEvent) {
					System.out.println("Event is an EvioDataEvent");
					System.out.println("Decoder schema factory: " + decoder.getSchemaFactory());
					decoder.initEvent(event);
					Event dump = decoder.getDataEvent();
					HipoDataEvent hipoEvent = new HipoDataEvent(dump, decoder.getSchemaFactory());
					String[] bankList = hipoEvent.getBankList();
					System.out.println("Bank list size: " + bankList.length);
					for (String bankName : bankList) {
						System.out.println("bank: " + bankName);
					}
				}
			}
		}
		
		
		dataSource.close();

	}

}
