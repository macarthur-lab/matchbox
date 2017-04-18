/**
 * To represent an extended series of metrics who audience is internal ONLY since
 * it will show a privileged set of information
 * 
 * As of now, privileged and public endpoints are identical, but this leaves room
 * for future privileged series of metrics as well
 */
package org.broadinstitute.macarthurlab.matchbox.entities;

/**
 * @author harindra
 *
 */
public class PrivilegedMetric extends PublicMetric{

	/**
	 * default constructor
	 */
	public PrivilegedMetric() {
		super();
	}

	/** 
	 * Primary constructor
	 */
	public PrivilegedMetric(int numberOfSubmitters, int numberOfUniqueGenes, int numberOfUniqueFeatures,
			int numberOfCasesWithDiagnosis, int numberOfCases, double percentageOfGenesThatMatch,
			double meanNumberOfGenesPerCase, double meanNumberOfVariantsPerCase, double meanNumberOfPhenotypesPerCase,
			int numberOfRequestsReceived, int numberOfPotentialMatchesSent) {
		super(numberOfSubmitters, numberOfUniqueGenes, numberOfUniqueFeatures, numberOfCasesWithDiagnosis, numberOfCases,
				percentageOfGenesThatMatch, meanNumberOfGenesPerCase, meanNumberOfVariantsPerCase,
				meanNumberOfPhenotypesPerCase, numberOfRequestsReceived, numberOfPotentialMatchesSent);
	}
	
	
}
