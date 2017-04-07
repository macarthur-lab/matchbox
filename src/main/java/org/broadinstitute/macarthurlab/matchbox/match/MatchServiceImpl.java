/**
 * Represents a matchmaker match
 */
package org.broadinstitute.macarthurlab.matchbox.match;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.broadinstitute.macarthurlab.matchbox.entities.MatchmakerResult;
import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author harindra
 *
 */
@Service
public class MatchServiceImpl implements MatchService{
	
	/**
	 * Genotype matching tools
	 */
	@Autowired
	private GenotypeSimilarityService genotypeMatch;
	
	/**
	 * Phenotype matching tools
	 */
	@Autowired
	private PhenotypeSimilarityService phenotypeMatch;

	/**
	 * Does a MME match
	 */
	public MatchServiceImpl() {}
	
	
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
		List<MatchmakerResult> allResults = new ArrayList<MatchmakerResult>();
		
		//#TODO must add code here to search by phenotypes if genotypes are not given
		List<Patient> genomicFeatMatches = this.getGenotypeMatch().searchByGenomicFeatures(patient);

		List<Double> patientGenotypeRankingScores = this.getGenotypeMatch().rankByGenotypes(genomicFeatMatches, patient);
		List<Double> patientPhenotypeRankingScores = this.getPhenotypeMatch().rankByPhenotypes(genomicFeatMatches, patient);
		List<Double> scores = generateMergedScore(patientGenotypeRankingScores,patientPhenotypeRankingScores);
		
		int i=0;
		for (Patient p: genomicFeatMatches){
			Map<String, Double> score = new HashMap<String, Double>();
			score.put("patient", scores.get(i));
			allResults.add(new MatchmakerResult(
												score,
												p
												));
			i++;
		}
		return allResults;
	}


	/**
	 * Merge phenotype and genotype scores into a single score,
	 * 
	 * Algorithm: the absolute value of the weighted average of 
	 * genotype (weight:1) and phenotype(weight:1)
	 * @param patientGenotypeRankingScores	scores based on genotypes
	 * @param patientPhenotypeRankingScores scores based on phenotypes
	 * @return	A merged score
	 */
	public List<Double> generateMergedScore(List<Double> patientGenotypeRankingScores,List<Double> patientPhenotypeRankingScores){
		List<Double> merged = new ArrayList<Double>();
		//let's give them equal weight for now
		double genotypeWeight=1;
		double phenotypeWeight=1;
		double mergedScore=0.0d;
		for (int i=0;i<patientGenotypeRankingScores.size();i++){
			mergedScore = patientGenotypeRankingScores.get(i)*genotypeWeight + patientPhenotypeRankingScores.get(i)*phenotypeWeight ;
			if (mergedScore>1){
				mergedScore=1;
			}
			if (mergedScore<0){
				mergedScore=0;
			}			
			merged.add(mergedScore);
		}
		return merged;
	}
	
	
	
	/**
	 * @return the genotypeMatch
	 */
	public GenotypeSimilarityService getGenotypeMatch() {
		return genotypeMatch;
	}


	/**
	 * @return the phenotypeMatch
	 */
	public PhenotypeSimilarityService getPhenotypeMatch() {
		return phenotypeMatch;
	}
	
	
	
	

}
