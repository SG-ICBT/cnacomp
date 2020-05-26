# cnacomp
This repository contains java source code that was used for the analysis of circulating nuceic acids sequencing data. It does not intend to comprise a tool.

# Usage: 

The following steps describe how to use the provides source code

1. Data
The first step to run an analysis is to prepare the target data, which is a set of sam/bam alignment files. The dataset has to be defined in an xml-file, which also contains information on the target genome of the alignment files.
An exampe for a dataset definition is given in exampleTask.xml (Note that the task.dtd file is also required).

2. Annotation database
An annotation database is required for running an analysis. This database needs to contain the refGene gene annotation (refGene, refLink) and the RepeatMasker annotation (rmsk) for the respective genome (e.g. hg38). The UCSC mySQL database access is preconfigured in the cna.config.Config.java file. Note that UCSC will blacklist you for excessive use of this database, therefore it is recommended to run a private instance with the required database schema and tables (can be downloaded from UCSC goldenpath).

3. Source code
Download the source code from the repository (git-clone) and create a project in the IDE of your choice.
Link the required libraries (picard, mysql-connector, jfreechart, jfreesvg, lz4-java, jcommon) to your project.
There are basically two files that need to be edited in order to define a custom analysis.
The cna.config.Config.java contains the paramaters for the database connection and a very small set of other configuration paramates (e.g. the number of threads used for parallel computations).
The cna.CNA_Analysis.java contains the definition of the desired analysis, where the dataset is loaded, data subgroups are defined followed by the analysis steps and saving of the result (An example analysis with comments is provided in this file). There are three additional classes containing the functionality to define an analysis run (cna.CompositionAnalysis, cna.CoverageAnalysis, cna.FragmentSizeAnalysis). See the provided javadoc for information on how to use the functionaities provided by these classes.

4. Run the analyis
After an analysis procedure is implemented, build an executable jar-file. This jar file need to be in the same directory as the dataset definition (xml + dtd file). It may be required to set the memory parameters (-Xms, -Xmx) for the java virtual machine in order to execute the analysis. 

# Related publication: 
Circulating cell-free DNA is predominantly composed of retrotransposable elements and non-telomeric satellite DNA.

PMID: 32165241 
DOI: 10.1016/j.jbiotec.2020.03.002

https://doi.org/10.1016/j.jbiotec.2020.03.002

# Data:
The data used for this proejct is accessible at the NCBI bioproject PRJNA596372.

https://www.ncbi.nlm.nih.gov/sra/PRJNA596372
