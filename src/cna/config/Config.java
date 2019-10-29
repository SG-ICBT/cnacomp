package cna.config;

/**
 * Stores configuration elements
 * @author Stefan Grabuschnig
 *
 */
public class Config {
	
	//performance
	public static final int numThreads = 15; //number of threads used for parallel computation
	
	//marker detection
	public static boolean fragmentsOnly = true; //ignore non concordantly mapped reads
	
	//coverage plots
	public static boolean plotDNAseClusters = false; //plot DNAse sensitive sites in coverage charts
	public static boolean showLegend = false; //show legends in coverage charts
		
	// Annotation lookup
	//Define paramers for annotation database lookup
	public static final String dbDriver = "com.mysql.jdbc.Driver";
	public static final String dbURL =  "jdbc:mysql://genome-mysql.soe.ucsc.edu:3306/";
	public static final String dbUser = "genomep";
	public static final String dbPassword = "password";
	public static final String dbSchema = "hg38"; //"bosTau8";
}
