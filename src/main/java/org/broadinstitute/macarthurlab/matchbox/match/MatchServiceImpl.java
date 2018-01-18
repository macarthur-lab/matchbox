/**
 * Represents a matchmaker match
 */
package org.broadinstitute.macarthurlab.matchbox.match;

import org.broadinstitute.macarthurlab.matchbox.entities.GenotypeSimilarityScore;
import org.broadinstitute.macarthurlab.matchbox.entities.MatchmakerResult;
import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.broadinstitute.macarthurlab.matchbox.entities.PhenotypeSimilarityScore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

/**
 * @author harindra
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Service
public class MatchServiceImpl implements MatchService {

    private static final Logger logger = LoggerFactory.getLogger(MatchServiceImpl.class);
    
    @Value("${allow.no-gene-in-common.matches}")
    private boolean ALLOW_NO_GENE_IN_COMMON_PHENOTYPE_MATCHES;
    
    private static double PHENOTYPE_MATCH_THRESHOLD=0.7;
    private static double ZERO_PHENOTYPE_SCORE_FOR_GENE_MATCH_SCORE_PENALTY=0.01;

    private final GenotypeSimilarityService genotypeSimilarityService;
    private final PhenotypeSimilarityService phenotypeSimilarityService;

    @Autowired
    public MatchServiceImpl(GenotypeSimilarityService genotypeSimilarityService, PhenotypeSimilarityService phenotypeSimilarityService) {
        this.genotypeSimilarityService = genotypeSimilarityService;
        this.phenotypeSimilarityService = phenotypeSimilarityService;
    }

    /**
     * Do a match in the local database based of this patient.
     * <p>
     * Once matches are found via genotypes, it is then ranked by genotype similarity and
     * then ranked by phenotype similarity and following that a score is assigned based on
     * those two metrics.
     *
     * @param patient a patient to match on
     * @param patients against which to score the query patient
     */
    public List<MatchmakerResult> match(Patient queryPatient, List<Patient> patients) {
        List<MatchmakerResult> results = new ArrayList<>();
        
        int numPatientsWithGoodGenotypeMatch=0;
        
        logger.info("Matching query patient {} against all {} patients in this node.", queryPatient.getId(), patients.size());
        
        //The final score should be in the range 0.0 - 1.0 where 1.0 is a self-match.
        PhenotypeSimilarityScorer phenotypeSimilarityScorer = phenotypeSimilarityService.buildPhenotypeSimilarityScorer(queryPatient);
        
        Map<Patient,Map<String,Double>> candidateNodePatientsToReturn = new HashMap<>();
        
        
        //compare every patient in matchbox pairwise with the query patient, and when a gene-match happens, put it
        //aside to return. Keep the others aside for scoring probabilities. Each patient gets a separate phenotype
        //and a base genotype (using gnomad) score
        for (Patient nodePatient : patients) {
            GenotypeSimilarityScore genotypeSimilarityScore = genotypeSimilarityService.scoreGenotypes(queryPatient, nodePatient);
            PhenotypeSimilarityScore phenotypeSimilarityScore = phenotypeSimilarityScorer.scorePhenotypes(queryPatient, nodePatient);
            
            double genotypeScore = genotypeSimilarityScore.getScore();
            double phenotypeScore = phenotypeSimilarityScore.getScore();
                   
            Map<String, Double> score = new LinkedHashMap<>();
            score.put("patient", 0d);//putting a placeholder "patient" score till we can calculate disease population probabilities after loop
            score.put("_genotypeScore", genotypeScore);
            score.put("_phenotypeScore", phenotypeScore);
            logger.info("{}:{} base scaled-genoScore: {} phenoScore: {}", queryPatient.getId(), nodePatient.getId(), genotypeScore, phenotypeScore);
            
            if (this.ALLOW_NO_GENE_IN_COMMON_PHENOTYPE_MATCHES){
            	if (genotypeSimilarityScore.hasCommonGene() || phenotypeScore >= MatchServiceImpl.PHENOTYPE_MATCH_THRESHOLD){
                	candidateNodePatientsToReturn.put(nodePatient, score);
                }
            }
            else{
            	//default logic is that we return any query-patient combination with at least one gene in common
            	if (genotypeSimilarityScore.hasCommonGene()){
                	candidateNodePatientsToReturn.put(nodePatient, score);
                }
            }
            
            //numPatientsWithGoodGenotypeMatch var is to help figure out population frequencies within matchbox
            if (genotypeSimilarityScore.hasCommonGene() 
            				  && (genotypeSimilarityScore.hasAtleastOneGeneInCommonWithSameType()
            				  ||  genotypeSimilarityScore.hasAtleastOneGeneInCommonWithSameZygosity() 
            				  ||  genotypeSimilarityScore.hasAtleastOneGeneInCommonWithSameVariantPosition())){
            	numPatientsWithGoodGenotypeMatch +=1;
            }
        }
        logger.info("number of patients with good a genotypeMatch (without phenotype-only matches) {})", numPatientsWithGoodGenotypeMatch );

        for (Patient localNodePatient : candidateNodePatientsToReturn.keySet()){
        	results.add(new MatchmakerResult(calculateMatchScore(candidateNodePatientsToReturn.get(localNodePatient),
        														 numPatientsWithGoodGenotypeMatch, 
        														 patients.size()),
        				localNodePatient));
        	}
        //sort by score
        results.sort(Comparator.comparingDouble(object -> {
            MatchmakerResult matchmakerResult = (MatchmakerResult) object;
            return matchmakerResult.getScore().get("patient");
        }).reversed());

        logger.info("Matches for patient: {}", queryPatient.getId());
        results.forEach(matchmakerResult ->
                logger.info("{}-{}: {}", queryPatient.getId(), matchmakerResult.getPatient().getId(), matchmakerResult.getScore()));

        return results;
    }

    
    
    
    /**
     * Merges the phenotype and genotype scores and uses matchbox as a disease population proxy
     * @return a new map of scores with updates merged score
     */
    private  Map<String,Double> calculateMatchScore(final Map<String,Double> resultCandidateMatchScores,
    												int numPatientsWithGoodGenotypeMatch,
    												int matchboxPatientPopSize) {
    	double baseGenotypeScore = resultCandidateMatchScores.get("_genotypeScore");
    	double phenotypeScore = resultCandidateMatchScores.get("_phenotypeScore");
        
        Map<String,Double> merged = new HashMap<>();
        merged.put("_phenotypeScore", phenotypeScore);
        
        double matchboxPopulationProbability = ((double)numPatientsWithGoodGenotypeMatch/(double)matchboxPatientPopSize);
        //genotypeScore is 1.0 if it's a perfect match genotypically, no more scoring needed
        if (baseGenotypeScore == 1d){
        	merged.put("_genotypeScore", baseGenotypeScore);
        	merged.put("patient", 1d);
        	return merged;
        }
        double genotypeScore = baseGenotypeScore * matchboxPopulationProbability;
        merged.put("_genotypeScore", genotypeScore);
        
        double mergedScore = genotypeScore;
        if (phenotypeScore !=0){
        	mergedScore *= phenotypeScore;
        }
        merged.put("patient",BigDecimal.valueOf(mergedScore).setScale(4, RoundingMode.HALF_UP).doubleValue() );
        return merged;
    }

    
	/**
	 * @return the ALLOW_NO_GENE_IN_COMMON_PHENOTYPE_MATCHES
	 */
	public boolean getALLOW_NO_GENE_IN_COMMON_PHENOTYPE_MATCHES() {
		return ALLOW_NO_GENE_IN_COMMON_PHENOTYPE_MATCHES;
	}

	/**
	 * @param ALLOW_NO_GENE_IN_COMMON_PHENOTYPE_MATCHES the aLLOW_NO_GENE_IN_COMMON_PHENOTYPE_MATCHES to set
	 */
	public void setALLOW_NO_GENE_IN_COMMON_PHENOTYPE_MATCHES(String aLLOW_NO_GENE_IN_COMMON_PHENOTYPE_MATCHES) {
		ALLOW_NO_GENE_IN_COMMON_PHENOTYPE_MATCHES = Boolean.valueOf(ALLOW_NO_GENE_IN_COMMON_PHENOTYPE_MATCHES);
	}
    
    
    

}
