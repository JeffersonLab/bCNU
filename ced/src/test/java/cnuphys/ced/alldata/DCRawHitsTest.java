package cnuphys.ced.alldata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DCRawHitsTest {
    @Test
    void convertsGlobalLayerNumber() {
        for (int layer = 1; layer <= 36; layer++) {
            assertEquals(((layer - 1) / 6) + 1, DCRawHits.superlayerForLayer(layer));
            assertEquals(((layer - 1) % 6) + 1, DCRawHits.layerInSuperlayerForLayer(layer));
        }
    }
}
