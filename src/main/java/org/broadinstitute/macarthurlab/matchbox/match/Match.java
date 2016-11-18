/**
 * Represents a matchmaker match
 */
package org.broadinstitute.macarthurlab.matchbox.match;

import org.broadinstitute.macarthurlab.matchbox.entities.MatchmakerResult;
import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author harindra
 *
 */
@Component
public class Match implements MatchService {

	private static final Logger logger = LoggerFactory.getLogger(Match.class);
	
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
	public Match(GenotypeMatch genotypeMatch, PhenotypeMatch phenotypeMatch) {
		this.genotypeMatch = genotypeMatch;
		this.phenotypeMatch = phenotypeMatch;
	}
	
	
	/**
	 * Do a match in the local database based of this patient.
	 * 
	 * Once matches are found via genotypes, it is then ranked by genotype similarity and
	 * then ranked by phenotype similarity and following that a score is assigned based on
	 * those two metrics.
	 * 
	 * @param patient a patient to match on
	 */
	public List<MatchmakerResult> match(Patient patient){
		logger.info("Searching for patient matches to {}", patient.getId());
		List<MatchmakerResult> allResults = new ArrayList<>();
		List<Patient> genomicFeatMatches = genotypeMatch.searchByGenomicFeatures(patient);

		List<Double> patientGenotypeRankingScores = genotypeMatch.rankByGenotypes(genomicFeatMatches, patient);
		List<Double> patientPhenotypeRankingScores = phenotypeMatch.rankByPhenotypes(genomicFeatMatches, patient);
		List<Double> scores = generateMergedScore(patientGenotypeRankingScores, patientPhenotypeRankingScores);

		int i = 0;
		for (Patient p : genomicFeatMatches) {
			Map<String, Double> score = new HashMap<String, Double>();
			score.put("patient", scores.get(i));
			allResults.add(new MatchmakerResult(score, p));
			i++;
		}
		return allResults;
	}


	/**
	 * Merge phenotype and genotype scores into a single score,
	 * 
	 * Algorithm: the absolute value of the weighted average of 
	 * genotype (wieght:1) and phenotype(weight:1)
	 * @param patientGenotypeRankingScores	scores based on genotypes
	 * @param patientPhenotypeRankingScores scores based on phenotypes
	 * @return	A merged score
	 */
	private List<Double> generateMergedScore(List<Double> patientGenotypeRankingScores, List<Double> patientPhenotypeRankingScores){
		List<Double> merged = new ArrayList<>();
		//let's give them equal weight for now
		double genotypeWeight = 1;
		double phenotypeWeight = 1;
		//TODO: Check!! mergedScore is being overwritten and added to the list - shouldn't this be locally scoped to the loop below?
		double mergedScore = 0;
		for (int i = 0; i < patientGenotypeRankingScores.size(); i++) {
			mergedScore = patientGenotypeRankingScores.get(i) * genotypeWeight + patientPhenotypeRankingScores.get(i) * phenotypeWeight;
			if (mergedScore > 1) {
				mergedScore = 1;
			}
			if (mergedScore < 0) {
				mergedScore = 0;
			}
			merged.add(mergedScore);
		}
		return merged;
	}

}
