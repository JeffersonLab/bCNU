package cnuphys.ced.geometry.urwt;

import org.jlab.detector.geant4.v2.MPGD.URWT.URWTStripFactory;
import org.jlab.geom.prim.Line3D;

public class UrWTDetectorData {

	// sector is stored as 1-based [1..6]
	public final int sector;

	// layer stored as [1-base] [1..4]
	public final int layer;

	// the strip count
	public final int count;

	// the strips
	public Line3D[] strips;

	/**
	 * Some useful chamber data
	 *
	 * @param sector  [1..6]
	 * @param layer   [1..4]
	 */
	public UrWTDetectorData(URWTStripFactory factory, int sector, int layer) {

		if ((sector < 1) || (sector > 6)) {
			System.err.println("Bad sector in UrWELL data: " + sector);
			System.exit(0);
		} else if ((layer < 1) || (layer > 4)) {
			System.err.println("Bad layer in UrWELL data: " + layer);
			System.exit(0);
		}

		// these are 1-based, just like in the database
		this.sector = sector;
		this.layer = layer;

		// the strip count
		count = factory.getNComponents(sector, layer);

		// the strips
		strips = new Line3D[count];

		for (int strip = 1; strip <= count; strip++) {

			strips[strip - 1] = factory.getStrip(sector, layer, strip);

			if (strips[strip - 1] == null) {
				System.err.println(
						"strip factory returned null for sector " + sector + " layer " + layer + " strip " + strip);
				System.exit(1);
			}

		}

	}
}
