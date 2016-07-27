/**
 * Represents a phenotype based match
 */
package org.broadinstitute.macarthurlab.matchbox.match;

import java.util.ArrayList;
import java.util.List;

import org.broadinstitute.macarthurlab.matchbox.entities.Patient;

/**
 * @author harindra
 *
 */
public class PhenotypeMatch {

	/**
	 * Constructor sets up everything
	 */
	public PhenotypeMatch() {

	}

	
	
	/**
	 * Ranks a patient list by their phenotype similarity to a query patient
	 * @param patients a list of patients to rank
	 * @param queryPatient a target patient to rank against
	 * @return Sends back a list of scores for each patient based on phenotype. Order matches input list
	 */
	public List<Double> rankByPhenotypes(List<Patient> patients, Patient queryPatient){
		List<Double> patientPhenotypeRankingScores = new ArrayList<Double>();
		return patientPhenotypeRankingScores;
	}
}
