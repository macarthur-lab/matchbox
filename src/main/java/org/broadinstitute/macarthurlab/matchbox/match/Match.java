/**
 * Represents a matchmaker match
 */
package org.broadinstitute.macarthurlab.matchbox.match;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.broadinstitute.macarthurlab.matchbox.entities.MatchmakerResult;
import org.broadinstitute.macarthurlab.matchbox.entities.Patient;

/**
 * @author harindra
 *
 */
public class Match implements MatchService{
	
	/**
	 * Genotype matching tools
	 */
	private final GenotypeMatch genotypeMatch;
	/**
	 * Phenotype matching tools
	 */
	private final PhenotypeMatch phenotypeMatch;

	/**
	 * Does a MME match
	 */
	public Match() {
		this.genotypeMatch = new GenotypeMatch();
		this.phenotypeMatch = new PhenotypeMatch();
	}
	
	
	/**
	 * Do a match in the local database based of this patient
	 * @param patient a patient to match on
	 */
	public List<MatchmakerResult> match(Patient patient){
		List<MatchmakerResult> allResults = new ArrayList<MatchmakerResult>();
		List<Patient> genomicFeatMatches = this.getGenotypeMatch().searchByGenomicFeatures(patient);
		List<Double> patientPhenotypeRankingScores = this.getPhenotypeMatch().rankByPhenotypes(genomicFeatMatches, patient);
		for (Patient p: genomicFeatMatches){
			allResults.add(new MatchmakerResult(
												new HashMap<String, Double>(),
												p
												));
		}
		return allResults;
	}


	/**
	 * @return the genotypeMatch
	 */
	public GenotypeMatch getGenotypeMatch() {
		return genotypeMatch;
	}


	/**
	 * @return the phenotypeMatch
	 */
	public PhenotypeMatch getPhenotypeMatch() {
		return phenotypeMatch;
	}
	
	
	
	

}
