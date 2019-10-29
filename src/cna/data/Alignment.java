package cna.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SamInputResource;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;
import net.jpountz.lz4.LZ4FrameInputStream;
import net.jpountz.lz4.LZ4FrameOutputStream;

/**
 * Represents an alignment in sam/bam format. Provides functionallity for efficient access to count data
 * @author Stefan Grabuschnig
 *
 */
public class Alignment {
	// alignment data
	private Individual individual;
	private Genome genome;
	private String alignmentID;
	private String bamFile;
	private String time;
	private String infectionState;

	// Coverage data
	private HashMap<String, File> covAFiles = null; // absolute coverages
	private HashMap<String, File> covAfoFiles = null; // absolute coverage fragments only
	private HashMap<String, File> covNFiles = null; // normalized coverage
	private HashMap<String, File> covNfoFiles = null; // normalized coverage fragments only

	private HashMap<String, File> fragmentsFiles = null; // fragments derived from concordant read pairs

	/**
	 * @param alignmentID name of the alignment
	 * @param individual individual to which the alignment belongs
	 * @param bamFile path to the sam/bam file
	 * @param time name of the time assigned to the alignment
	 * @param infectionState name of the infection state
	 * @param genome the respective genome
	 */
	public Alignment(String alignmentID, Individual individual, String bamFile, String time, String infectionState,
			Genome genome) {
		this.alignmentID = alignmentID;
		this.individual = individual;
		this.infectionState = infectionState;
		this.bamFile = bamFile;
		this.time = time;
		this.genome = genome;
		this.init();
	}

	private void init() {

		// initialize coverage data information
		String coverageDataPath = this.individual.getLabel().getName() + File.separator + this.individual.getID()
				+ File.separator + this.alignmentID + File.separator;
		this.covAFiles = new HashMap<String, File>(this.genome.getNumberOfChromosomes());
		this.covAfoFiles = new HashMap<String, File>(this.genome.getNumberOfChromosomes());
		this.covNFiles = new HashMap<String, File>(this.genome.getNumberOfChromosomes());
		this.covNfoFiles = new HashMap<String, File>(this.genome.getNumberOfChromosomes());
		this.fragmentsFiles = new HashMap<String, File>(this.genome.getNumberOfChromosomes());

		for (String chromosome : this.genome.getChromosomeNames()) {
			this.covAFiles.put(chromosome,
					new File(coverageDataPath + File.separator + chromosome + File.separator + chromosome + ".covA"));
			this.covAfoFiles.put(chromosome,
					new File(coverageDataPath + File.separator + chromosome + File.separator + chromosome + ".covAfo"));
			this.covNFiles.put(chromosome,
					new File(coverageDataPath + File.separator + chromosome + File.separator + chromosome + ".covN"));
			this.covNfoFiles.put(chromosome,
					new File(coverageDataPath + File.separator + chromosome + File.separator + chromosome + ".covNfo"));
			this.fragmentsFiles.put(chromosome,
					new File(coverageDataPath + File.separator + chromosome + File.separator + chromosome + ".frags"));
		}

		// check if coverage data already exist
		boolean filesExist = true;
		for (File f : covAFiles.values())
			filesExist &= f.exists();
		for (File f : covAfoFiles.values())
			filesExist &= f.exists();
		for (File f : covNFiles.values())
			filesExist &= f.exists();
		for (File f : covNfoFiles.values())
			filesExist &= f.exists();
		for (File f : fragmentsFiles.values())
			filesExist &= f.exists();

		// calculate coverage from bam-File
		if (!filesExist) {
			try {
				this.calculateCoverage();
			} catch (IOException e) {
				System.out.println(this.alignmentID + ": Error while parsing bam File!");
				e.printStackTrace();
				System.exit(0);
			}
		}
	}

	private void calculateCoverage() throws IOException {
		File bam = new File(this.bamFile);
		double genomeLength = 0;

		// check if bamFile exists
		if (!bam.exists()) {
			System.out.println("Error: bam-File " + this.bamFile + " could not be found!");
			System.exit(0);
		}

		// initialize coverage data structure
		HashMap<String, HashMap<String, ReadPair>> readPairs = new HashMap<String, HashMap<String, ReadPair>>(
				this.genome.getNumberOfChromosomes());
		HashMap<String, ArrayList<int[]>> singleReads = new HashMap<String, ArrayList<int[]>>(
				this.genome.getNumberOfChromosomes());

		for (String chromosome : this.genome.getChromosomeNames()) {
			readPairs.put(chromosome, new HashMap<String, ReadPair>(100000));
			singleReads.put(chromosome, new ArrayList<int[]>(300000));
			genomeLength += this.genome.getChromosomeSize(chromosome);
		}

		// read bam
		System.out.println(this.alignmentID + ": Calculating coverage...");

		// InputStream is = new
		// ByteArrayInputStream(Files.readAllBytes(Paths.get(bam.getAbsolutePath())));

		SamReaderFactory samReaderFactory = SamReaderFactory.makeDefault()
				.enable(SamReaderFactory.Option.VALIDATE_CRC_CHECKSUMS)
				.validationStringency(ValidationStringency.LENIENT);
		SamInputResource resource = SamInputResource.of(bam);
		SamReader samReader = samReaderFactory.open(resource);

		double mappedBasesFragments = 0.0d;
		double mappedBasesSingleReads = 0.0d;

		// Sort records according to chromosomes
		for (final SAMRecord rec : samReader)
			if (this.genome.contains(rec.getContig())) {
				if (rec.getReadPairedFlag() && rec.getProperPairFlag()) {
					// Identify and store fragments
					String readName = rec.getReadName();
					if (rec.getFirstOfPairFlag()) {
						if (readPairs.get(rec.getContig()).containsKey(readName)) {
							readPairs.get(rec.getContig()).get(readName).setFirst(rec.getAlignmentStart(),
									rec.getAlignmentEnd(), rec.getMateAlignmentStart());
						} else {
							ReadPair pair = new ReadPair();
							pair.setFirst(rec.getAlignmentStart(), rec.getAlignmentEnd(), rec.getMateAlignmentStart());
							readPairs.get(rec.getContig()).put(readName, pair);
						}
					} else if (rec.getSecondOfPairFlag()) {
						if (readPairs.get(rec.getContig()).containsKey(readName)) {
							readPairs.get(rec.getContig()).get(readName).setSecond(rec.getAlignmentStart(),
									rec.getAlignmentEnd(), rec.getMateAlignmentStart());
						} else {
							ReadPair pair = new ReadPair();
							pair.setSecond(rec.getAlignmentStart(), rec.getAlignmentEnd(), rec.getMateAlignmentStart());
							readPairs.get(rec.getContig()).put(readName, pair);
						}
					} else {
						System.out.println(
								"ERROR: No first or second of pair flag set: " + this.alignmentID + ":" + readName);
					}
				} else { // non properly paired read
					singleReads.get(rec.getContig()).add(new int[] { rec.getAlignmentStart(), rec.getAlignmentEnd() });
					mappedBasesSingleReads += rec.getAlignmentEnd() - rec.getAlignmentStart() + 1;
				}
			}
		samReader.close();

		// Calculate total fragments coverage
		for (String chromosome : this.genome.getChromosomeNames())
			for (ReadPair rp : readPairs.get(chromosome).values())
				mappedBasesFragments += rp.getLength();

		for (String chromosome : this.genome.getChromosomeNames()) {
			int[] absoluteCoverage = new int[this.genome.getChromosomeSize(chromosome)];
			float[] normalizedCoverage = new float[this.genome.getChromosomeSize(chromosome)];
			int[] fragments = new int[2 * readPairs.get(chromosome).size()];
			int fragmentIndex = 0;

			// calculate coverage from fragments
			for (ReadPair rp : readPairs.get(chromosome).values()) {
				if (rp.isComplete()) {
					fragments[fragmentIndex] = rp.getFragmentStart() - 1;
					fragments[fragmentIndex + 1] = rp.getFragmentEnd() - 1;
					fragmentIndex += 2;

					for (int i = rp.getFragmentStart(); i <= rp.getFragmentEnd(); i++)
						absoluteCoverage[i - 1]++;
				} else {
					System.out.println("Incomplete ReadPair at " + this.alignmentID);
					System.exit(0);
				}
			}

			// normalize
			double averageCoverage = mappedBasesFragments / genomeLength;
			for (int i = 0; i < normalizedCoverage.length; i++)
				normalizedCoverage[i] = (float) ((double) absoluteCoverage[i] / averageCoverage);

			// write fragments file
			this.fragmentsFiles.get(chromosome).getParentFile().mkdirs();
			Files.write(Paths.get(this.fragmentsFiles.get(chromosome).getAbsolutePath()),
					Alignment.compress(fragments));

			// write absolute coverage fragments only file
			this.covAfoFiles.get(chromosome).getParentFile().mkdirs();
			Files.write(Paths.get(this.covAfoFiles.get(chromosome).getAbsolutePath()),
					Alignment.compress(absoluteCoverage));

			// write normalized coverage fragments only file
			this.covNfoFiles.get(chromosome).getParentFile().mkdirs();
			Files.write(Paths.get(this.covNfoFiles.get(chromosome).getAbsolutePath()),
					Alignment.compress(normalizedCoverage));

			// add coverage from single reads
			for (int[] sr : singleReads.get(chromosome))
				for (int i = sr[0]; i <= sr[1]; i++)
					absoluteCoverage[i - 1]++;

			// normalize
			averageCoverage = (mappedBasesFragments + mappedBasesSingleReads) / genomeLength;
			for (int i = 0; i < normalizedCoverage.length; i++)
				normalizedCoverage[i] = (float) ((double) absoluteCoverage[i] / averageCoverage);

			// write absolute coverage
			this.covAFiles.get(chromosome).getParentFile().mkdirs();
			Files.write(Paths.get(this.covAFiles.get(chromosome).getAbsolutePath()),
					Alignment.compress(absoluteCoverage));

			// write normalized coverage
			this.covNFiles.get(chromosome).getParentFile().mkdirs();
			Files.write(Paths.get(this.covNFiles.get(chromosome).getAbsolutePath()),
					Alignment.compress(normalizedCoverage));
		}
	}

	private static byte[] compress(int[] intArray) {
		byte[] bytesOut = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			LZ4FrameOutputStream lz4os = new LZ4FrameOutputStream(baos);
			BufferedOutputStream bos = new BufferedOutputStream(lz4os);
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(intArray);
			oos.flush();
			oos.close();
			lz4os.close();
			bytesOut = baos.toByteArray();
			baos.close();
		} catch (IOException e) {
			System.out.println("Error while compressing int array");
			e.printStackTrace();
		}
		return bytesOut;
	}

	private static byte[] compress(float[] floatArray) {
		byte[] bytesOut = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			LZ4FrameOutputStream lz4os = new LZ4FrameOutputStream(baos);
			BufferedOutputStream bos = new BufferedOutputStream(lz4os);
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(floatArray);
			oos.flush();
			oos.close();
			lz4os.close();
			bytesOut = baos.toByteArray();
			baos.close();
		} catch (IOException e) {
			System.out.println("Error while compressing int array");
			e.printStackTrace();
		}
		return bytesOut;
	}

	private static int[] uncompressIntArray(byte[] bytes) {
		int[] intArray = null;
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			BufferedInputStream bis = new BufferedInputStream(bais);
			LZ4FrameInputStream lz4is = new LZ4FrameInputStream(bis);
			ObjectInputStream ois = new ObjectInputStream(lz4is);
			intArray = (int[]) ois.readObject();
			ois.close();
		} catch (IOException e) {
			System.out.println("Error while uncompressing int array");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.out.println("Error while uncompressing int array");
			e.printStackTrace();
		}
		return intArray;
	}

	private static float[] uncompressFloatArray(byte[] bytes) {
		float[] floatArray = null;
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			BufferedInputStream bis = new BufferedInputStream(bais);
			LZ4FrameInputStream lz4is = new LZ4FrameInputStream(bis);
			ObjectInputStream ois = new ObjectInputStream(lz4is);
			floatArray = (float[]) ois.readObject();
			ois.close();
		} catch (IOException e) {
			System.out.println("Error while uncompressing int array");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.out.println("Error while uncompressing int array");
			e.printStackTrace();
		}
		return floatArray;
	}

	/**
	 * @return th ename of the alignment
	 */
	public String getID() {
		return this.alignmentID;
	}

	/**
	 * @return the individual owning the alignment
	 */
	public Individual getIndividual() {
		return this.individual;
	}

	/**
	 * @return the name of the time assigned to the alignment
	 */
	public String getTime() {
		return this.time;
	}

	/**
	 * @return the name of the infection state
	 */
	public String getInfectionState() {
		return this.infectionState;
	}

	/**
	 * @return the respective genome
	 */
	public Genome getGenome() {
		return this.genome;
	}

	/**
	 * @param chromosome the name of the chromosome
	 * @return an integer array containing the count data for the specified chromosome
	 */
	public int[] getAbsoluteCoverage(String chromosome) {
		try {
			if (cna.config.Config.fragmentsOnly)
				return Alignment.uncompressIntArray(
						Files.readAllBytes(Paths.get(this.covAfoFiles.get(chromosome).getAbsolutePath())));
			else
				return Alignment.uncompressIntArray(
						Files.readAllBytes(Paths.get(this.covAFiles.get(chromosome).getAbsolutePath())));

		} catch (IOException e) {
			System.out.println("ERROR reading covA filfe for " + this.alignmentID + ":" + chromosome);
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * @param chromosome the name of the chromosome
	 * @return a float array containing the normalized count data for the specified chromosome
	 */
	public float[] getNormalizedCoverage(String chromosome) {
		try {

			if (cna.config.Config.fragmentsOnly)
				return Alignment.uncompressFloatArray(
						Files.readAllBytes(Paths.get(this.covNfoFiles.get(chromosome).getAbsolutePath())));
			else
				return Alignment.uncompressFloatArray(
						Files.readAllBytes(Paths.get(this.covNFiles.get(chromosome).getAbsolutePath())));

		} catch (IOException e) {
			System.out.println("ERROR reading covN filfe for " + this.alignmentID + ":" + chromosome);
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param chromosome the name of the chromosome
	 * @return int array containing start and stop coordinates of fragments reconstructed from concordanly mapped read pairs
	 */
	public int[] getFragments(String chromosome) {
		try {
			return Alignment.uncompressIntArray(
					Files.readAllBytes(Paths.get(this.fragmentsFiles.get(chromosome).getAbsolutePath())));
		} catch (IOException e) {
			System.out.println("ERROR reading fragments filfe for " + this.alignmentID + ":" + chromosome);
			e.printStackTrace();
		}
		return null;
	}
}
