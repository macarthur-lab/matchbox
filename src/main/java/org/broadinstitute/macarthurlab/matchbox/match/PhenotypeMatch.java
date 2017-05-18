/**
 * Represents a phenotype based match
 */
package org.broadinstitute.macarthurlab.matchbox.match;

import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author harindra
 *
 */
@Component
public class PhenotypeMatch {

	
	
	/**
	 * Ranks a patient list by their phenotype similarity to a query patient
	 * @param patients a list of patients to rank
	 * @param queryPatient a target patient to rank against
	 * @return Sends back a list of scores for each patient based on phenotype. Order matches input list
	 */
	public List<Double> rankByPhenotypes(List<Patient> patients, Patient queryPatient){
		List<Double> patientPhenotypeRankingScores = new ArrayList<Double>();
		for (Patient patient :patients){
			double phenotypeSimilarityScore=this.getPhenotypeSimilarity(patient, queryPatient);
			patientPhenotypeRankingScores.add(phenotypeSimilarityScore);
		}
		return patientPhenotypeRankingScores;
	}
	
	
	/**
	 * As a first VERY naive step, we will simply get the number of 
	 * HPO terms they have in common against the total number of HPO terms.
	 * NOTE: in a perfect match, returns 0.5 as per weight allowed to phenotypes
	 * @param p1	patient 1
	 * @param p2	patient 2
	 * @return	a representative number (described above)
	 */
	private double getPhenotypeSimilarity(Patient p1, Patient queryPatient){
		List<String> p1Features = new ArrayList<String>();
		p1.getFeatures().forEach((k)->{
							p1Features.add(k.getId());
						});
		List<String> queryFeatures = new ArrayList<String>();
		queryPatient.getFeatures().forEach((k)->{
							queryFeatures.add(k.getId());
						});
		List<String> p1p2Intersect = p1Features.stream()
                .filter(queryFeatures::contains)
                .collect(Collectors.toList());		
		/**
		 * If ALL of the query is a subset of the match, still return
		 * a high score of 0.4. Then it is assumed that the query just
		 * didn't have/send all the information, but a good match anyway.
		 */
		if (p1p2Intersect.size() == queryFeatures.size() && p1p2Intersect.size() < p1Features.size()){
			return 0.4d;
		}
		/**
		 * If a PERFECT match return 0.5
		 */
		if (p1p2Intersect.size() == p1Features.size()){
			return 0.5d;
		}
		/**
		 * Otherwise return a metric of inclusion
		 */
		return (double)p1p2Intersect.size() / ((double)p1.getFeatures().size() + (double)queryPatient.getFeatures().size());
	}
	
}
