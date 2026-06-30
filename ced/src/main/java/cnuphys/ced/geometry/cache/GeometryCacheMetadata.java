package cnuphys.ced.geometry.cache;

import java.time.Instant;

/**
 * Metadata stored at the beginning of the geometry cache.
 * <p>
 * This is file-level cache metadata. It is separate from detector-specific
 * geometry version information.
 */
public class GeometryCacheMetadata {

	private final String _cacheType;
	private final int _formatVersion;
	private final long _createdEpochMillis;
	private final String _createdBy;

	/**
	 * Create geometry cache metadata.
	 *
	 * @param cacheType     the cache backend/type, e.g. "kryo"
	 * @param formatVersion the cache format version
	 * @param createdBy     optional creator/application string
	 */
	public GeometryCacheMetadata(String cacheType, int formatVersion, String createdBy) {
		_cacheType = cacheType;
		_formatVersion = formatVersion;
		_createdEpochMillis = Instant.now().toEpochMilli();
		_createdBy = createdBy;
	}

	public String getCacheType() {
		return _cacheType;
	}

	public int getFormatVersion() {
		return _formatVersion;
	}

	public long getCreatedEpochMillis() {
		return _createdEpochMillis;
	}

	public String getCreatedBy() {
		return _createdBy;
	}

	@Override
	public String toString() {
		return String.format("GeometryCacheMetadata[type=%s, format=%d, created=%d, createdBy=%s]",
				_cacheType, _formatVersion, _createdEpochMillis, _createdBy);
	}
}