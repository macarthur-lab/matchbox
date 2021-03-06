/**
 * A set of fields for a public metric
 */
package org.broadinstitute.macarthurlab.matchbox.entities;

/**
 * @author harindra
 *
 */
public class PublicMetric extends Metric {
	private int numberOfSubmitters;
	private int numberOfUniqueGenes;
	private int numberOfUniqueFeatures;
	private int numberOfCasesWithDiagnosis;
	private int numberOfCases;
	private double percentageOfGenesThatMatch;
	private double meanNumberOfGenesPerCase;
	private double meanNumberOfVariantsPerCase;
	private double meanNumberOfPhenotypesPerCase;
	private int numberOfRequestsReceived;
	private int numberOfPotentialMatchesSent;
	
	/**
	 * Deafult constructor
	 */
	public PublicMetric(){}

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
	 */
	public PublicMetric(int numberOfSubmitters, 
						int numberOfUniqueGenes, 
						int numberOfUniqueFeatures,
						int numberOfCasesWithDiagnosis, 
						int numberOfCases, 
						double percentageOfGenesThatMatch,
						double meanNumberOfGenesPerCase, 
						double	meanNumberOfVariantsPerCase, 
						double meanNumberOfPhenotypesPerCase,
						int numberOfRequestsReceived, 
						int numberOfPotentialMatchesSent) {
		
		this.numberOfSubmitters = numberOfSubmitters;
		this.numberOfUniqueGenes = numberOfUniqueGenes;
		this.numberOfUniqueFeatures = numberOfUniqueFeatures;
		this.numberOfCasesWithDiagnosis = numberOfCasesWithDiagnosis;
		this.numberOfCases = numberOfCases;
		this.percentageOfGenesThatMatch = percentageOfGenesThatMatch;
		this.meanNumberOfGenesPerCase = meanNumberOfGenesPerCase;
		this.meanNumberOfVariantsPerCase = meanNumberOfVariantsPerCase;
		this.meanNumberOfPhenotypesPerCase = meanNumberOfPhenotypesPerCase;
		this.numberOfRequestsReceived = numberOfRequestsReceived;
		this.numberOfPotentialMatchesSent = numberOfPotentialMatchesSent;
	}

	/**
	 * @return the numberOfSubmitters
	 */
	public int getNumberOfSubmitters() {
		return numberOfSubmitters;
	}

	/**
	 * @param numberOfSubmitters the numberOfSubmitters to set
	 */
	public void setNumberOfSubmitters(int numberOfSubmitters) {
		this.numberOfSubmitters = numberOfSubmitters;
	}

	/**
	 * @return the numberOfUniqueGenes
	 */
	public int getNumberOfUniqueGenes() {
		return numberOfUniqueGenes;
	}

	/**
	 * @param numberOfUniqueGenes the numberOfUniqueGenes to set
	 */
	public void setNumberOfUniqueGenes(int numberOfUniqueGenes) {
		this.numberOfUniqueGenes = numberOfUniqueGenes;
	}

	/**
	 * @return the numberOfUniqueFeatures
	 */
	public int getNumberOfUniqueFeatures() {
		return numberOfUniqueFeatures;
	}

	/**
	 * @param numberOfUniqueFeatures the numberOfUniqueFeatures to set
	 */
	public void setNumberOfUniqueFeatures(int numberOfUniqueFeatures) {
		this.numberOfUniqueFeatures = numberOfUniqueFeatures;
	}

	/**
	 * @return the numberOfCasesWithDiagnosis
	 */
	public int getNumberOfCasesWithDiagnosis() {
		return numberOfCasesWithDiagnosis;
	}

	/**
	 * @param numberOfCasesWithDiagnosis the numberOfCasesWithDiagnosis to set
	 */
	public void setNumberOfCasesWithDiagnosis(int numberOfCasesWithDiagnosis) {
		this.numberOfCasesWithDiagnosis = numberOfCasesWithDiagnosis;
	}

	/**
	 * @return the numberOfCases
	 */
	public int getNumberOfCases() {
		return numberOfCases;
	}

	/**
	 * @param numberOfCases the numberOfCases to set
	 */
	public void setNumberOfCases(int numberOfCases) {
		this.numberOfCases = numberOfCases;
	}

	/**
	 * @return the percentageOfGenesThatMatch
	 */
	public double getPercentageOfGenesThatMatch() {
		return percentageOfGenesThatMatch;
	}

	/**
	 * @param percentageOfGenesThatMatch the percentageOfGenesThatMatch to set
	 */
	public void setPercentageOfGenesThatMatch(int percentageOfGenesThatMatch) {
		this.percentageOfGenesThatMatch = percentageOfGenesThatMatch;
	}

	/**
	 * @return the meanNumberOfGenesPerCase
	 */
	public double getMeanNumberOfGenesPerCase() {
		return meanNumberOfGenesPerCase;
	}

	/**
	 * @param meanNumberOfGenesPerCase the meanmedianNumberOfGenesPerCase to set
	 */
	public void setMeanNumberOfGenesPerCase(int meanmedianNumberOfGenesPerCase) {
		this.meanNumberOfGenesPerCase = meanmedianNumberOfGenesPerCase;
	}

	/**
	 * @return the meanNumberOfVariantsPerCase
	 */
	public double getMeanNumberOfVariantsPerCase() {
		return meanNumberOfVariantsPerCase;
	}

	/**
	 * @param meanNumberOfVariantsPerCase the meanNumberOfVariantsPerCase to set
	 */
	public void setMeanNumberOfVariantsPerCase(int meanNumberOfVariantsPerCase) {
		this.meanNumberOfVariantsPerCase = meanNumberOfVariantsPerCase;
	}

	/**
	 * @return the meanNumberOfPhenotypesPerCase
	 */
	public double getMeanNumberOfPhenotypesPerCase() {
		return meanNumberOfPhenotypesPerCase;
	}

	/**
	 * @param meanNumberOfPhenotypesPerCase the meanNumberOfPhenotypesPerCase to set
	 */
	public void setMeanNumberOfPhenotypesPerCase(int meanNumberOfPhenotypesPerCase) {
		this.meanNumberOfPhenotypesPerCase = meanNumberOfPhenotypesPerCase;
	}

	/**
	 * @return the numberOfRequestsReceived
	 */
	public int getNumberOfRequestsReceived() {
		return numberOfRequestsReceived;
	}

	/**
	 * @param numberOfRequestsReceived the numberOfRequestsReceived to set
	 */
	public void setNumberOfRequestsReceived(int numberOfRequestsReceived) {
		this.numberOfRequestsReceived = numberOfRequestsReceived;
	}

	/**
	 * @return the numberOfPotentialMatchesSent
	 */
	public int getNumberOfPotentialMatchesSent() {
		return numberOfPotentialMatchesSent;
	}

	/**
	 * @param numberOfPotentialMatchesSent the numberOfPotentialMatchesSent to set
	 */
	public void setNumberOfPotentialMatchesSent(int numberOfPotentialMatchesSent) {
		this.numberOfPotentialMatchesSent = numberOfPotentialMatchesSent;
	}

}
