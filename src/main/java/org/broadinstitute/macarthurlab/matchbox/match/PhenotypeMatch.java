/**
 * Represents a phenotype based match
 */
package org.broadinstitute.macarthurlab.matchbox.match;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
		for (Patient patient :patients){
			double similarityScore=this.getPhenotypeSimilarity(patient, queryPatient);
			patientPhenotypeRankingScores.add(similarityScore);
		}
		return patientPhenotypeRankingScores;
	}
	
	
	/**
	 * As a first VERY naive step, we will simply get the number of 
	 * HPO terms they have in common against the total number of HPO terms
	 * @param p1	patient 1
	 * @param p2	patient 2
	 * @return	a representative number (described above)
	 */
	private double getPhenotypeSimilarity(Patient p1, Patient p2){
		List<String> p1Features = new ArrayList<String>();
		p1.getFeatures().forEach((k)->{
							p1Features.add(k.getId());
						});
		List<String> p2Features = new ArrayList<String>();
		p2.getFeatures().forEach((k)->{
							p2Features.add(k.getId());
						});
		List<String> p1p2Intersect = p1Features.stream()
                .filter(p2Features::contains)
                .collect(Collectors.toList());
		return (double)p1p2Intersect.size() / ((double)p1.getFeatures().size() + (double)p2.getFeatures().size());
	}
	
}
