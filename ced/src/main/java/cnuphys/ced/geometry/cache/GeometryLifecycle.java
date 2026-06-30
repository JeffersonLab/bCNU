package cnuphys.ced.geometry.cache;

/**
 * Higher-level geometry initialization contract.
 *
 * This interßface deliberately does not expose Kryo, SQLite, or any other
 * cache implementation detail. It represents the lifecycle of a geometry
 * object: try cache, initialize from the authoritative source, then save.
 */
public interface GeometryLifecycle {

    /**
     * Initialize this geometry from the authoritative source, usually CCDB.
     */
    void initializeUsingCCDB();

    /**
     * Initialize this geometry from the configured local cache.
     *
     * @return true if the geometry was successfully initialized from cache
     */
    boolean initializeFromCache();

    /**
     * Save this geometry to the configured local cache.
     *
     * @return true if the geometry was successfully saved
     */
    boolean saveToCache();
}