/**
 * Represents a phenotype feature
 */
package org.broadinstitute.macarthurlab.beamr.entities;

/**
 * @author harindra
 *
 */
public class PhenotypeFeature {
	/**
	 * HPO code
	 */
	private final String id;
	/**
	 * yes/no
	 */
	private final String observed;
	/**
	 * ageOfOnset 
	 */
	private final String ageOfOnset;

	
	/**
	 * Default constructor creates empty object
	 */
	public PhenotypeFeature() {
		this.id = "";
		this.observed = "";
		this.ageOfOnset = "";
	}
	
	

	/**
	 * @param id	HPO code
	 * @param observed	yes/no
	 * @param ageOfOnset	simple string
	 */
	public PhenotypeFeature(String id, String observed, String ageOfOnset) {
		this.id = id;
		this.observed = observed;
		this.ageOfOnset = ageOfOnset;
	}
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @return the observed
	 */
	public String getObserved() {
		return observed;
	}
	/**
	 * @return the ageOfOnset
	 */
	public String getAgeOfOnset() {
		return ageOfOnset;
	}



	/* 
	 * To string method
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PhenotypeFeature [id=" + id + ", observed=" + observed + ", ageOfOnset=" + ageOfOnset + "]";
	}
	
	
}
