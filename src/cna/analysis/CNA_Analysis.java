package cna.analysis;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import cna.data.Alignment;
import cna.data.DataSet;
import cna.data.RepeatMasker;

/**
 * Main class for performing analyses. Define your desired analysis in the body of the main method using the functionality in the CompositionAnalysis, CoverageAnalysis and FragmentsSizeAnalysis classes.
 * Provides some functionality to store data.
 * @author Stefan Grabuschnig
 */
public class CNA_Analysis {

	public static void main(String[] args) {
		// __________________________________________________________________________________________________________________________________________________
		// Load dataset

		// DataSet dataSet = new DataSet("taskCancerCells.xml");
		// DataSet dataSet = new DataSet("taskBovine.xml");
		DataSet dataSet = new DataSet("taskSepsisMUG.xml");
		// DataSet dataSet = new DataSet("taskHealthyX1000.xml");

		// __________________________________________________________________________________________________________________________________________________
		// Define set of considered repeatfamilies
		LinkedList<String> repeatFamilies = new LinkedList<String>();
		
		// human
		//repeatFamilies.add("Alu");
		//repeatFamilies.add("L1");
		//repeatFamilies.add("SVA");
		//repeatFamilies.add("centr");
		//repeatFamilies.add("Simple_repeat");
		//repeatFamilies.add("Satellite");
		//repeatFamilies.add("telo");
		//repeatFamilies.add("acro");

		// all annotated
		repeatFamilies = RepeatMasker.getRepeatFamilies();

		// bovine
//		repeatFamilies.add("RTE-BovB");
//		repeatFamilies.add("L1");
//		repeatFamilies.add("centr");
//		repeatFamilies.add("Core-RTE");
		
		System.out.println("Repeat Families: ");
		for(String rf : repeatFamilies)
			System.out.print(rf + " ");
		System.out.println();

		// __________________________________________________________________________________________________________________________________________________
		// Define data for analysis

		LinkedHashMap<String, ArrayList<Alignment>> data = new LinkedHashMap<String, ArrayList<Alignment>>();

		// human
		ArrayList<Alignment> healthy = dataSet.getAlignmentsFromLabel("healthy");
//		ArrayList<Alignment> sepsis = dataSet.getAlignmentsFromLabel("sepsis");
//
		data.put("healthy", healthy);
//		data.put("sepsis", sepsis);

		
		//Cancer cell lines
//		ArrayList<Alignment> vcap = dataSet.getAlignmentsFromIndividual("VCAP");
//		ArrayList<Alignment> bewo1425 = dataSet.getAlignmentsFromIndividual("BEWO1425");
//		ArrayList<Alignment> bewo1426 = dataSet.getAlignmentsFromIndividual("BEWO1426");
//		
//		data.put("VCaP", vcap);
//		data.put("BeWo1425", bewo1425);
//		data.put("BeWo1426", bewo1426);
		
		//bovine
//		ArrayList<Alignment> alignmentsFemaleCows = dataSet.getAlignmentsFromLabel("N");
//		data.put("bovine", alignmentsFemaleCows);

		// __________________________________________________________________________________________________________________________________________________
		// Define analysis to perform
		LinkedHashMap<String, ArrayList<double[]>> compositions = CompositionAnalysis.calculateCompositions(data,
				repeatFamilies);
		double[] repeatAbundances = CompositionAnalysis.calculateRepeatAbundances(repeatFamilies, dataSet.getGenome());
		
		ArrayList<double[]> referenceBasedCoverageLevelsHealthy = CompositionAnalysis.calculateReferenceBasedCoverageLevels(compositions.get("healthy"), repeatAbundances, repeatAbundances.length - 1);
		//ArrayList<double[]> referenceBasedCoverageLevelsSepsis = CompositionAnalysis.calculateReferenceBasedCoverageLevels(compositions.get("sepsis"), repeatAbundances, repeatAbundances.length - 1);
		
//		ArrayList<double[]> referenceBasedCoverageLevelsVCaP = CompositionAnalysis.calculateReferenceBasedCoverageLevels(compositions.get("VCaP"), repeatAbundances, repeatAbundances.length - 1);
//		ArrayList<double[]> referenceBasedCoverageLevelsBeWo1425 = CompositionAnalysis.calculateReferenceBasedCoverageLevels(compositions.get("BeWo1425"), repeatAbundances, repeatAbundances.length - 1);
//		ArrayList<double[]> referenceBasedCoverageLevelsBeWo1426 = CompositionAnalysis.calculateReferenceBasedCoverageLevels(compositions.get("BeWo1426"), repeatAbundances, repeatAbundances.length - 1);
		
//		ArrayList<double[]> referenceBasedCoverageLevelsBovine = CompositionAnalysis.calculateReferenceBasedCoverageLevels(compositions.get("bovine"), repeatAbundances, repeatAbundances.length - 1);

		// __________________________________________________________________________________________________________________________________________________
		// store data
		
		//compositions
		saveData(compositions.get("healthy"), repeatFamilies, "Compositions_healthyRFAll");
//		saveData(compositions.get("sepsis"), repeatFamilies, "Compositions_sepsisRF3");
		
//		saveData(compositions.get("VCaP"), repeatFamilies, "Compositions_VCaPRF3");
//		saveData(compositions.get("BeWo1425"), repeatFamilies, "Compositions_BeWo1425RF3");
//		saveData(compositions.get("BeWo1426"), repeatFamilies, "Compositions_BeWo1426RF3");
		
//		saveData(compositions.get("bovine"), repeatFamilies, "Compositions_BovineRF4");
		
		//repeat abundances
		saveData(repeatAbundances, repeatFamilies, "RepeatAbundancesRF4");
		
		//reference based coverages
		saveData(referenceBasedCoverageLevelsHealthy, repeatFamilies, "ReferenceBasedCoverageLevels_healthyRFAll");
//		saveData(referenceBasedCoverageLevelsSepsis, repeatFamilies, "ReferenceBasedCoverageLevels_sepsisRF3");
		
//		saveData(referenceBasedCoverageLevelsVCaP, repeatFamilies, "ReferenceBasedCoverageLevels_VCaPRF3");
//		saveData(referenceBasedCoverageLevelsBeWo1425, repeatFamilies, "ReferenceBasedCoverageLevels_BeWo1425RF3");
//		saveData(referenceBasedCoverageLevelsBeWo1426, repeatFamilies, "ReferenceBasedCoverageLevels_BeWo1426RF3");
		
//		saveData(referenceBasedCoverageLevelsBovine, repeatFamilies, "ReferenceBasedCoverageLevels_BovineRF4");

	}

	/**
	 * Saves double array data (i.e. compositions or reference based coverage levels) to a textfile.
	 * @param data list of douable arrays containing the data
	 * @param repeatFamilies list of repeat families (header)
	 * @param fileName name of the file
	 */
	public static void saveData(ArrayList<double[]> data, LinkedList<String> repeatFamilies, String fileName) {
		try {
			BufferedWriter bwr = new BufferedWriter(new FileWriter(fileName + ".txt"));

			// data header
			for (int i = 0; i < repeatFamilies.size(); i++)
				bwr.write(repeatFamilies.get(i) + "\t");

			bwr.write("Other repeats\t");
			bwr.write("Non repetetive");
			bwr.newLine();

			// data
			for (double[] vector : data) {
				for (int i = 0; i < vector.length - 1; i++)
					bwr.write(vector[i] + "\t");
				bwr.write("" + vector[vector.length - 1]);
				bwr.newLine();
			}

			bwr.flush();
			bwr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Saves a single double array (i.e. repeat abundances) to a textfile.
	 * @param vector double array
	 * @param repeatFamilies list of repeat families (header)
	 * @param fileName name of the file
	 */
	public static void saveData(double[] vector, LinkedList<String> repeatFamilies, String fileName) {
		try {
			BufferedWriter bwr = new BufferedWriter(new FileWriter(fileName + ".txt"));

			// data header
			for (int i = 0; i < repeatFamilies.size(); i++)
				bwr.write(repeatFamilies.get(i) + "\t");

			bwr.write("Other repeats\t");
			bwr.write("Non repetetive");
			bwr.newLine();

			// data
			for (int i = 0; i < vector.length - 1; i++)
				bwr.write(vector[i] + "\t");
			bwr.write("" + vector[vector.length - 1]);
			bwr.newLine();

			bwr.flush();
			bwr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param values list of double arrays
	 * @return arithmetic mean of double arrays
	 */
	public static double[] calculateMean(ArrayList<double[]> values) {
		double[] mean = new double[values.get(0).length];

		for (double[] value : values)
			for (int i = 0; i < value.length; i++)
				mean[i] += value[i];

		for (int i = 0; i < mean.length; i++)
			mean[i] /= values.size();

		return mean;
	}
	
	/**
	 * @deprecated
	 * Creates a textfile with a table of average compositions for different labels
	 * @param data Hashmap of labeled average compositions
	 * @param repeatFams names of considered repeat families
	 * @param repeatAbundances double array with genomic abundances of repeat families
	 */
	public static void createTable(LinkedHashMap<String, double[]> data, LinkedList<String> repeatFams,
			double[] repeatAbundances) {

		LinkedList<String> repeatFamilies = new LinkedList<String>();
		repeatFamilies.addAll(repeatFams);
		repeatFamilies.add("Other Repeats");
		repeatFamilies.add("Non repetetive");

		// headline

		DecimalFormat fsc = new DecimalFormat("0.00E0");
		DecimalFormat f = new DecimalFormat("0.00");

		String output = "Repeat Family:\tGenome:";
		for (String key : data.keySet())
			output += "\t" + key + ":" + "\tOverrep:";
		output += System.lineSeparator();

		for (int i = 0; i < repeatAbundances.length; i++) {
			output += repeatFamilies.get(i) + ":";

			if (100.0d * repeatAbundances[i] < 0.01)
				output += "\t" + fsc.format(100.0d * repeatAbundances[i]) + "%";
			else
				output += "\t" + f.format(100.0d * repeatAbundances[i]) + "%";

			for (String key : data.keySet())
				if (100.0d * data.get(key)[i] < 0.01)
					output += "\t" + fsc.format(100.0d * data.get(key)[i]) + "%\t"
							+ f.format(100.0d * data.get(key)[i] / repeatAbundances[i]) + "%";
				else
					output += "\t" + f.format(100.0d * data.get(key)[i]) + "%\t"
							+ f.format(100.0d * data.get(key)[i] / repeatAbundances[i]) + "%";
			output += System.lineSeparator();
		}

		output = output.replaceAll("\\.", ",");

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("CNAcomposition.txt"));
			writer.write(output);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			System.out.println("Cant write output to textfile");
			e.printStackTrace();
		}
	}
}
