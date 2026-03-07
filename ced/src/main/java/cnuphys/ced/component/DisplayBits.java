package cnuphys.ced.component;

public class DisplayBits {

	/** FTOF panels */
	public static final int MCTRUTH = 01;

	/** A flag for accumulation */
	public static final int ACCUMULATION = 02;

	/** reconstructed segments */
	public static final int SEGMENTS = 04;

	/** A bit for uvw strips */
	public static final int UVWSTRIPS = 010;

	/** A bit for inner/outer selection for ec */
	public static final int INNEROUTER = 020;

	/** dc reconstructed hits */
	public static final int DC_HITS = 040;

	/** reconstructed crosses */
	public static final int CROSSES = 0100;

	/** reconstructed clusters */
	public static final int CLUSTERS = 0200;

	/** mag field */
	public static final int MAGFIELD = 0400;

	/** hits Recons */
	public static final int RECONHITS = 01000;

	/** Cosmic tracks */
	public static final int COSMICS = 02000;

	/** global display of hb data */
	public static final int GLOBAL_HB = 04000;

	/** global display of hb data */
	public static final int GLOBAL_TB = 010000;

	/** adc data */
	public static final int ADCDATA = 020000;

	/** cvt recon tracks */
	public static final int CVTRECTRACKS = 040000;

	/** reconstructed clusters */
	public static final int FMTCROSSES = 0100000;

	/** A flag for trkDoca v. doca */
	public static final int DOCA = 0200000;

	/** cvt recon traj */
	public static final int CVTRECTRAJ = 0400000;

	/** sector change diamonds */
    public static final int SECTORCHANGE = 01000000;

    /** TOF panels */
	public static final int TOFPANELS = 02000000;

    /** global nn data */
    public static final int GLOBAL_AIHB = 04000000;

    /** global nn data */
    public static final int GLOBAL_AITB = 010000000;

    /** data from REC::Calorimeter */
    public static final int RECCAL = 020000000;

    /** mag field grid */
    public static final int MAGGRID = 040000000;

    /** REC::Particles */
    public static final int RECPART = 0100000000;

	/** cvt rec KF traj */
	public static final int CVTRECKFTRAJ = 0200000000;

	/** cvt pass 1 tracks */
	public static final int CVTP1TRACKS = 0400000000;

	/** cvt pass 1 traj */
	public static final int CVTP1TRAJ = 01000000000;
	
	/** four layers for urWT */
	public static final int URWTLAYERS = 02000000000;



	// max octal for ints 20000000000

}
