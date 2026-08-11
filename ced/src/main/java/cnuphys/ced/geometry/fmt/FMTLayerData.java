package cnuphys.ced.geometry.fmt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.jlab.geom.component.TrackerStrip;
import org.jlab.geom.detector.fmt.FMTLayer;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;

/** Primitive FMT strip geometry retained by CED and stored in SQLite. */
final class FMTLayerData {

	final int sector;
	final int superlayer;
	final int layer;
	private final double[][] stripVertices;

	FMTLayerData(FMTLayer source) {
		sector = source.getSectorId();
		superlayer = source.getSuperlayerId();
		layer = source.getLayerId();
		stripVertices = new double[source.getNumComponents()][24];
		for (int strip = 0; strip < stripVertices.length; strip++) {
			TrackerStrip component = source.getComponent(strip);
			for (int corner = 0; corner < 8; corner++) {
				Point3D point = component.getVolumePoint(corner);
				int offset = 3 * corner;
				stripVertices[strip][offset] = point.x();
				stripVertices[strip][offset + 1] = point.y();
				stripVertices[strip][offset + 2] = point.z();
			}
		}
	}

	private FMTLayerData(int sector, int superlayer, int layer, double[][] stripVertices) {
		this.sector = sector;
		this.superlayer = superlayer;
		this.layer = layer;
		this.stripVertices = stripVertices;
	}

	int stripCount() {
		return stripVertices.length;
	}

	Line3D stripLine(int strip) {
		double[] vertices = stripVertices[strip];
		return new Line3D(faceCenter(vertices, 0), faceCenter(vertices, 4));
	}

	void copyStripVertices(int strip, float[] coordinates) {
		double[] vertices = stripVertices[strip];
		for (int index = 0; index < vertices.length; index++) {
			coordinates[index] = (float) vertices[index];
		}
	}

	void writeToCache(DataOutput output) throws IOException {
		output.writeInt(sector);
		output.writeInt(superlayer);
		output.writeInt(layer);
		output.writeInt(stripVertices.length);
		for (double[] vertices : stripVertices) {
			for (double coordinate : vertices) {
				output.writeDouble(coordinate);
			}
		}
	}

	static FMTLayerData readFromCache(DataInput input) throws IOException {
		int sector = input.readInt();
		int superlayer = input.readInt();
		int layer = input.readInt();
		int stripCount = input.readInt();
		if (stripCount < 1 || stripCount > 10_000) {
			throw new IOException("Invalid FMT strip count: " + stripCount);
		}
		double[][] vertices = new double[stripCount][24];
		for (double[] strip : vertices) {
			for (int coordinate = 0; coordinate < strip.length; coordinate++) {
				strip[coordinate] = input.readDouble();
			}
		}
		return new FMTLayerData(sector, superlayer, layer, vertices);
	}

	private static Point3D faceCenter(double[] vertices, int firstCorner) {
		double x = 0.0;
		double y = 0.0;
		double z = 0.0;
		for (int corner = firstCorner; corner < firstCorner + 4; corner++) {
			int offset = 3 * corner;
			x += vertices[offset];
			y += vertices[offset + 1];
			z += vertices[offset + 2];
		}
		return new Point3D(x / 4.0, y / 4.0, z / 4.0);
	}
}
