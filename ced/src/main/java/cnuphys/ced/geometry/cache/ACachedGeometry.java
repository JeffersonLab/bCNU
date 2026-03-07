package cnuphys.ced.geometry.cache;

public abstract class ACachedGeometry implements IGeometryCache {

	// the name of this geometry
	private String _name;

	/**
	 * Constructor
	 * 
	 * @param name the name of this geometry
	 */
	public ACachedGeometry(String name) {
		_name = name;
		GeometryCache.addGeometry(this);
	}

	@Override
	public String getName() {
		return _name;
	}
	


}
