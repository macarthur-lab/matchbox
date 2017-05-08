/**
 * To represent an extended series of metrics who audience is internal ONLY since
 * it will show a privileged set of information
 * 
 * As of now, privileged and public endpoints are identical, but this leaves room
 * for future privileged series of metrics as well
 */
package org.broadinstitute.macarthurlab.matchbox.entities;


import java.util.Map;

/**
 * @author harindra
 *
 */
public class PrivilegedMetric extends PublicMetric{
	
	private Map<String,Integer> geneCounts;
	private Map<String,Integer> phenotypeCounts;

	/**
	 * default constructor
	 */
	public PrivilegedMetric() {
		super();
	}

	
	/**
	 * @param numberOfSubmitters
	 * @param numberOfUniqueGenes
	 * @param numberOfUniqueFeatures
	 * @param numberOfCasesWithDiagnosis
	 * @param numberOfCases
	 * @param percentageOfGenesThatMatch
	 * @param meanNumberOfGenesPerCase
	 * @param meanNumberOfVariantsPerCase
	 * @param meanNumberOfPhenotypesPerCase
	 * @param numberOfRequestsReceived
	 * @param numberOfPotentialMatchesSent
	 * @param geneCounts
	 */
	public PrivilegedMetric(int numberOfSubmitters, int numberOfUniqueGenes, int numberOfUniqueFeatures,
			int numberOfCasesWithDiagnosis, int numberOfCases, double percentageOfGenesThatMatch,
			double meanNumberOfGenesPerCase, double meanNumberOfVariantsPerCase, double meanNumberOfPhenotypesPerCase,
			int numberOfRequestsReceived, int numberOfPotentialMatchesSent,
			Map<String,Integer> geneCounts,
			Map<String,Integer> phenotypeCounts) {
				
		super(numberOfSubmitters, numberOfUniqueGenes, numberOfUniqueFeatures, numberOfCasesWithDiagnosis, numberOfCases,
				percentageOfGenesThatMatch, meanNumberOfGenesPerCase, meanNumberOfVariantsPerCase,
				meanNumberOfPhenotypesPerCase, numberOfRequestsReceived, numberOfPotentialMatchesSent);
		
		this.geneCounts = geneCounts;
		this.phenotypeCounts = phenotypeCounts;
	}






	/**
	 * @return the geneCounts
	 */
	public Map<String, Integer> getGeneCounts() {
		return geneCounts;
	}

	/**
	 * @param geneCounts the geneCounts to set
	 */
	public void setGeneCounts(Map<String, Integer> geneCounts) {
		this.geneCounts = geneCounts;
	}


	/**
	 * @return the phenotypeCounts
	 */
	public Map<String, Integer> getPhenotypeCounts() {
		return phenotypeCounts;
	}


	/**
	 * @param phenotypeCounts the phenotypeCounts to set
	 */
	public void setPhenotypeCounts(Map<String, Integer> phenotypeCounts) {
		this.phenotypeCounts = phenotypeCounts;
	}
	
	
	


	
	
	
}
