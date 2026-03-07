package cnuphys.ced.geometry.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class UnmodifiableCollectionsSerializer extends Serializer<List<?>> {

    @Override
    public void write(Kryo kryo, Output output, List<?> list) {
        // Serialize as a modifiable ArrayList.
        ArrayList<?> modList = new ArrayList<>(list);
        kryo.writeObject(output, modList);
    }

    @Override
    public List<?> read(Kryo kryo, Input input, Class<? extends List<?>> type) {
        ArrayList<?> modList = kryo.readObject(input, ArrayList.class);
        return Collections.unmodifiableList(modList);
    }
}
