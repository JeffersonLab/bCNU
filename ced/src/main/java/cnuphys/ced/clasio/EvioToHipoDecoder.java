package cnuphys.ced.clasio;

import java.util.function.Consumer;

import org.jlab.detector.decode.CLASDecoder4;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.hipo.HipoDataEvent;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.data.SchemaFactory;
import org.jlab.utils.system.ClasUtilsFile;

import cnuphys.bCNU.log.Log;

/** Lazily decodes legacy EVIO events for CED's HIPO-based data accessors. */
final class EvioToHipoDecoder {

    private final Consumer<SchemaFactory> schemaConsumer;
    private CLASDecoder4 decoder;

    EvioToHipoDecoder(Consumer<SchemaFactory> schemaConsumer) {
        this.schemaConsumer = schemaConsumer;
    }

    HipoDataEvent decode(EvioDataEvent event) {
        if (event == null) return null;

        try {
            initialize();
            decoder.initEvent(event);
            Event decodedEvent = decoder.getDataEvent();
            return new HipoDataEvent(decodedEvent, decoder.getSchemaFactory());
        } catch (Exception exception) {
            Log.getInstance().error("Error decoding EVIO to HIPO: " + exception.getMessage());
            Log.getInstance().exception(exception);
            return null;
        }
    }

    private void initialize() {
        if (decoder != null) return;

        SchemaFactory schemaFactory = new SchemaFactory();
        String directory = ClasUtilsFile.getResourceDir("CLAS12DIR", "etc/bankdefs/hipo4");
        schemaFactory.initFromDirectory(directory);
        decoder = new CLASDecoder4();
        schemaConsumer.accept(schemaFactory);
    }
}
