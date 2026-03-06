package cnuphys.ced.ced3d.util;

public class BoundingBox3D {
	public double minX = Double.POSITIVE_INFINITY;
    public double maxX = Double.NEGATIVE_INFINITY;
    public double minY = Double.POSITIVE_INFINITY;
    public double maxY = Double.NEGATIVE_INFINITY;
    public double minZ = Double.POSITIVE_INFINITY;
    public double maxZ = Double.NEGATIVE_INFINITY;

    @Override
    public String toString() {
        return String.format("Box[X: %.2f to %.2f, Y: %.2f to %.2f, Z: %.2f to %.2f]", 
                             minX, maxX, minY, maxY, minZ, maxZ);
    }
}
