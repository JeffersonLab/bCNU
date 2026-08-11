package cnuphys.ced.geometry.cache;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/** A detector geometry that can initialize itself from CCDB or an explicit cache record. */
public interface IGeometryCache {

	String getName();

	void initializeUsingCCDB();

	/**
	 * Whether this geometry currently has an explicit, stable cache representation.
	 * Detector implementations opt in as they are migrated.
	 */
	default boolean supportsCache() {
		return false;
	}

	/** Version of this detector's payload format. */
	default int getCacheFormatVersion() {
		return 1;
	}

	/** Restore this geometry from its detector-specific primitive payload. */
	default void readGeometry(DataInput input) throws IOException {
		throw new UnsupportedOperationException(getName() + " does not support caching");
	}

	/** Write this geometry as a detector-specific primitive payload. */
	default void writeGeometry(DataOutput output) throws IOException {
		throw new UnsupportedOperationException(getName() + " does not support caching");
	}
}
