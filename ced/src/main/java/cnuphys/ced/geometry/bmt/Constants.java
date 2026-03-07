package cnuphys.ced.geometry.bmt;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class Constants {

	private Constants() {

	}

	/*
	 * The algorithm to describe the geometry of the BMT is provided
	 * by Franck Sabatie and implemented into the Java framework. This version is
	 * for the last region of the BMT only. CRC and CRZ characteristics localize
	 * strips in the cylindrical coordinate system. The target center is at the
	 * origin. The Z-axis is along the beam axis. The angles are defined with
	 * theZ-axis oriented from the accelerator to the beam dump.
	 */
	// THE GEOMETRY CONSTANTS
	public static final int NREGIONS = 3; // 3 regions of MM
	// public static final int STARTINGLAYR = 5; // current configuration is 3 BST +
	// 3BMT (outermost BST ring)

	// Z detector characteristics
	private static double[] CRZRADIUS = new double[NREGIONS]; // the radius of the Z detector in mm
	private static int[] CRZNSTRIPS = new int[NREGIONS]; // the number of strips
	private static double[] CRZSPACING = new double[NREGIONS]; // the strip spacing in mm
	private static double[] CRZWIDTH = new double[NREGIONS]; // the strip width in mm
	private static double[] CRZLENGTH = new double[NREGIONS]; // the strip length in mm
	private static double[] CRZZMIN = new double[NREGIONS]; // PCB upstream extremity mm
	private static double[] CRZZMAX = new double[NREGIONS]; // PCB downstream extremity mm
	private static double[] CRZOFFSET = new double[NREGIONS]; // Beginning of strips in mm
	private static double[][] CRZEDGE1 = new double[NREGIONS][3]; // the angle of the first edge of each PCB detector A,
																	// B, C
	private static double[][] CRZEDGE2 = new double[NREGIONS][3]; // the angle of the second edge of each PCB detector
																	// A, B, C
	private static double[] CRZXPOS = new double[NREGIONS]; // Distance on the PCB between the PCB first edge and the
															// edge of the first strip in mm

	// C detector characteristics
	private static double[] CRCRADIUS = new double[NREGIONS]; // the radius of the Z detector in mm
	private static int[] CRCNSTRIPS = new int[NREGIONS]; // the number of strips
	private static double[] CRCSPACING = new double[NREGIONS]; // the strip spacing in mm
	private static double[] CRCLENGTH = new double[NREGIONS]; // the strip length in mm
	private static double[] CRCZMIN = new double[NREGIONS]; // PCB upstream extremity mm
	private static double[] CRCZMAX = new double[NREGIONS]; // PCB downstream extremity mm
	private static double[] CRCOFFSET = new double[NREGIONS]; // Beginning of strips in mm
	private static int[][] CRCGROUP = new int[NREGIONS][]; // Number of strips with same width
	private static double[][] CRCWIDTH = new double[NREGIONS][]; // the width of the corresponding group of strips
	private static double[][] CRCEDGE1 = new double[NREGIONS][3]; // the angle of the first edge of each PCB detector A,
																	// B, C
	private static double[][] CRCEDGE2 = new double[NREGIONS][3]; // the angle of the second edge of each PCB detector
																	// A, B, C
	private static double[] CRCXPOS = new double[NREGIONS]; // Distance on the PCB between the PCB first edge and the
															// edge of the first strip in mm

	// THE RECONSTRUCTION CONSTANTS
	public static final double SigmaMax = 0.4; // Max transverse diffusion value (GEMC value)
	public static final double hDrift = 3.0; // Size of the drift gap
	public static final double hStrip2Det = hDrift / 2.; // distance between strips and the middle of the conversion gap
															// (~half the drift gap)

	private static double ThetaL = 0; // the Lorentz angle for 5-T B-field

	// private static double w_i =25.0;

	public static boolean areConstantsLoaded = false;

	// ----- cut based cand select
	public static final double phi12cut = 35.;
	public static final double phi13cut = 35.;
	public static final double phi14cut = 35.;
	public static final double radcut = 100.;
	public static final double drdzcut = 150.;
	public static final double LYRTHICKN = 4.;

	public static final int STARTINGLAYR = 1;

	public static synchronized void Load() {
		if (areConstantsLoaded) {
			return;
		}

		areConstantsLoaded = true;
	}

	public static double getThetaL() {
		return ThetaL;
	}

	public static synchronized void setThetaL(double thetaL) {
		ThetaL = thetaL;
	}

	public static synchronized double[] getCRZRADIUS() {
		return CRZRADIUS;
	}

	public static synchronized void setCRZRADIUS(double[] cRZRADIUS) {
		CRZRADIUS = cRZRADIUS;
	}

	public static synchronized int[] getCRZNSTRIPS() {
		return CRZNSTRIPS;
	}

	public static synchronized void setCRZNSTRIPS(int[] cRZNSTRIPS) {
		CRZNSTRIPS = cRZNSTRIPS;
	}

	public static synchronized double[] getCRZSPACING() {
		return CRZSPACING;
	}

	public static synchronized void setCRZSPACING(double[] cRZSPACING) {
		CRZSPACING = cRZSPACING;
	}

	public static synchronized double[] getCRZWIDTH() {
		return CRZWIDTH;
	}

	public static synchronized void setCRZWIDTH(double[] cRZWIDTH) {
		CRZWIDTH = cRZWIDTH;
	}

	public static synchronized double[] getCRZLENGTH() {
		return CRZLENGTH;
	}

	public static synchronized void setCRZLENGTH(double[] cRZLENGTH) {
		CRZLENGTH = cRZLENGTH;
	}

	public static synchronized double[] getCRZZMIN() {
		return CRZZMIN;
	}

	public static synchronized void setCRZZMIN(double[] cRZZMIN) {
		CRZZMIN = cRZZMIN;
	}

	public static synchronized double[] getCRZZMAX() {
		return CRZZMAX;
	}

	public static synchronized void setCRZZMAX(double[] cRZZMAX) {
		CRZZMAX = cRZZMAX;
	}

	public static synchronized double[] getCRZOFFSET() {
		return CRZOFFSET;
	}

	public static synchronized void setCRZOFFSET(double[] cRZOFFSET) {
		CRZOFFSET = cRZOFFSET;
	}

	public static synchronized double[][] getCRZEDGE1() {
		return CRZEDGE1;
	}

	public static synchronized void setCRZEDGE1(double[][] cRZEDGE1) {
		CRZEDGE1 = cRZEDGE1;
	}

	public static synchronized double[][] getCRZEDGE2() {
		return CRZEDGE2;
	}

	public static synchronized void setCRZEDGE2(double[][] cRZEDGE2) {
		CRZEDGE2 = cRZEDGE2;
	}

	public static synchronized double[] getCRZXPOS() {
		return CRZXPOS;
	}

	public static synchronized void setCRZXPOS(double[] cRZXPOS) {
		CRZXPOS = cRZXPOS;
	}

	public static synchronized double[] getCRCRADIUS() {
		return CRCRADIUS;
	}

	public static synchronized void setCRCRADIUS(double[] cRCRADIUS) {
		CRCRADIUS = cRCRADIUS;
	}

	public static synchronized int[] getCRCNSTRIPS() {
		return CRCNSTRIPS;
	}

	public static synchronized void setCRCNSTRIPS(int[] cRCNSTRIPS) {
		CRCNSTRIPS = cRCNSTRIPS;
	}

	public static synchronized double[] getCRCSPACING() {
		return CRCSPACING;
	}

	public static synchronized void setCRCSPACING(double[] cRCSPACING) {
		CRCSPACING = cRCSPACING;
	}

	public static synchronized double[] getCRCLENGTH() {
		return CRCLENGTH;
	}

	public static synchronized void setCRCLENGTH(double[] cRCLENGTH) {
		CRCLENGTH = cRCLENGTH;
	}

	public static synchronized double[] getCRCZMIN() {
		return CRCZMIN;
	}

	public static synchronized void setCRCZMIN(double[] cRCZMIN) {
		CRCZMIN = cRCZMIN;
	}

	public static synchronized double[] getCRCZMAX() {
		return CRCZMAX;
	}

	public static synchronized void setCRCZMAX(double[] cRCZMAX) {
		CRCZMAX = cRCZMAX;
	}

	public static synchronized double[] getCRCOFFSET() {
		return CRCOFFSET;
	}

	public static synchronized void setCRCOFFSET(double[] cRCOFFSET) {
		CRCOFFSET = cRCOFFSET;
	}

	public static synchronized int[][] getCRCGROUP() {
		return CRCGROUP;
	}

	public static synchronized void setCRCGROUP(int[][] cRCGROUP) {
		CRCGROUP = cRCGROUP;
	}

	public static synchronized double[][] getCRCWIDTH() {
		return CRCWIDTH;
	}

	public static synchronized void setCRCWIDTH(double[][] cRCWIDTH) {
		CRCWIDTH = cRCWIDTH;
	}

	public static synchronized double[][] getCRCEDGE1() {
		return CRCEDGE1;
	}

	public static synchronized void setCRCEDGE1(double[][] cRCEDGE1) {
		CRCEDGE1 = cRCEDGE1;
	}

	public static synchronized double[][] getCRCEDGE2() {
		return CRCEDGE2;
	}

	public static synchronized void setCRCEDGE2(double[][] cRCEDGE2) {
		CRCEDGE2 = cRCEDGE2;
	}

	public static synchronized double[] getCRCXPOS() {
		return CRCXPOS;
	}

	public static synchronized void setCRCXPOS(double[] cRCXPOS) {
		CRCXPOS = cRCXPOS;
	}
	
	public static void writeConstants(Kryo kryo, Output output) {
	    // Write the arrays for the Z detector characteristics
	    kryo.writeObject(output, CRZRADIUS);
	    kryo.writeObject(output, CRZNSTRIPS);
	    kryo.writeObject(output, CRZSPACING);
	    kryo.writeObject(output, CRZWIDTH);
	    kryo.writeObject(output, CRZLENGTH);
	    kryo.writeObject(output, CRZZMIN);
	    kryo.writeObject(output, CRZZMAX);
	    kryo.writeObject(output, CRZOFFSET);
	    kryo.writeObject(output, CRZEDGE1);
	    kryo.writeObject(output, CRZEDGE2);
	    kryo.writeObject(output, CRZXPOS);
	    
	    // Write the arrays for the C detector characteristics
	    kryo.writeObject(output, CRCRADIUS);
	    kryo.writeObject(output, CRCNSTRIPS);
	    kryo.writeObject(output, CRCSPACING);
	    kryo.writeObject(output, CRCLENGTH);
	    kryo.writeObject(output, CRCZMIN);
	    kryo.writeObject(output, CRCZMAX);
	    kryo.writeObject(output, CRCOFFSET);
	    kryo.writeObject(output, CRCGROUP);
	    kryo.writeObject(output, CRCWIDTH);
	    kryo.writeObject(output, CRCEDGE1);
	    kryo.writeObject(output, CRCEDGE2);
	    kryo.writeObject(output, CRCXPOS);
	    
	    // Write the additional reconstruction constants
	    output.writeDouble(ThetaL);
	    output.writeBoolean(areConstantsLoaded);
	}

	public static void readConstants(Kryo kryo, Input input) {
	    CRZRADIUS = kryo.readObject(input, double[].class);
	    CRZNSTRIPS = kryo.readObject(input, int[].class);
	    CRZSPACING = kryo.readObject(input, double[].class);
	    CRZWIDTH = kryo.readObject(input, double[].class);
	    CRZLENGTH = kryo.readObject(input, double[].class);
	    CRZZMIN = kryo.readObject(input, double[].class);
	    CRZZMAX = kryo.readObject(input, double[].class);
	    CRZOFFSET = kryo.readObject(input, double[].class);
	    CRZEDGE1 = kryo.readObject(input, double[][].class);
	    CRZEDGE2 = kryo.readObject(input, double[][].class);
	    CRZXPOS = kryo.readObject(input, double[].class);
	    
	    CRCRADIUS = kryo.readObject(input, double[].class);
	    CRCNSTRIPS = kryo.readObject(input, int[].class);
	    CRCSPACING = kryo.readObject(input, double[].class);
	    CRCLENGTH = kryo.readObject(input, double[].class);
	    CRCZMIN = kryo.readObject(input, double[].class);
	    CRCZMAX = kryo.readObject(input, double[].class);
	    CRCOFFSET = kryo.readObject(input, double[].class);
	    CRCGROUP = kryo.readObject(input, int[][].class);
	    CRCWIDTH = kryo.readObject(input, double[][].class);
	    CRCEDGE1 = kryo.readObject(input, double[][].class);
	    CRCEDGE2 = kryo.readObject(input, double[][].class);
	    CRCXPOS = kryo.readObject(input, double[].class);
	    
	    ThetaL = input.readDouble();
	    areConstantsLoaded = input.readBoolean();
	}

}
