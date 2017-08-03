/**
 * Represents a matchmaker match
 */
package org.broadinstitute.macarthurlab.matchbox.match;

import org.apache.commons.math3.stat.inference.TTest;
import org.broadinstitute.macarthurlab.matchbox.entities.GenotypeSimilarityScore;
import org.broadinstitute.macarthurlab.matchbox.entities.MatchmakerResult;
import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.broadinstitute.macarthurlab.matchbox.entities.PhenotypeSimilarityScore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author harindra
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Service
public class MatchServiceImpl implements MatchService {

    private static final Logger logger = LoggerFactory.getLogger(MatchServiceImpl.class);

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
    public List<MatchmakerResult> match(Patient patient, List<Patient> patients) {
        logger.info("Matching query patient {} against all {} patients in this node.", patient.getId(), patients.size());
        //The final score should be in the range 0.0 - 1.0 where 1.0 is a self-match.
        PhenotypeSimilarityScorer phenotypeSimilarityScorer = phenotypeSimilarityService.buildPhenotypeSimilarityScorer(patient);

        List<MatchmakerResult> results = new ArrayList<>();
        
        List<MatchmakerResult> nodePatientsWithVariantInSameGeneAsQuery = new LinkedList<MatchmakerResult>();
        List<MatchmakerResult> nodePatientsWithNoVariantInSameGeneAsQuery = new LinkedList<MatchmakerResult>();
        
        for (Patient nodePatient : patients) {
            GenotypeSimilarityScore genotypeSimilarityScore = genotypeSimilarityService.scoreGenotypes(patient, nodePatient);
            PhenotypeSimilarityScore phenotypeSimilarityScore = phenotypeSimilarityScorer.scorePhenotypes(patient, nodePatient);
            
            double genotypeScore = genotypeSimilarityScore.getScore();
            double phenotypeScore = phenotypeSimilarityScore.getScore();
            
            double matchScore = calculateMatchScore(genotypeScore, phenotypeScore);
            logger.info("{}-{}: {} genoScore: {} phenoScore: {}", patient.getId(), nodePatient.getId(), matchScore, genotypeScore, phenotypeScore);
            Map<String, Double> score = new LinkedHashMap<>();
            score.put("patient", matchScore);
            score.put("_genotypeScore", genotypeScore);
            score.put("_phenotypeScore", phenotypeScore);
            if (genotypeSimilarityScore.hasCommonGene()){
            	nodePatientsWithVariantInSameGeneAsQuery.add(new MatchmakerResult(score, nodePatient));
            }
            else{
            	nodePatientsWithNoVariantInSameGeneAsQuery.add(new MatchmakerResult(score, nodePatient));
            }
    
        }
        //now get list<Double> of only phenotype scores
        List<Double> phenoScoresOfNodePatientsWithVariant = new LinkedList<Double>();
        List<Double> phenoScoresOfNodePatientsWithoutVariant = new LinkedList<Double>();
        for (MatchmakerResult r: nodePatientsWithVariantInSameGeneAsQuery){
        	phenoScoresOfNodePatientsWithVariant.add(r.getScore().get("_phenotypeScore"));
        }
        for (MatchmakerResult r: nodePatientsWithNoVariantInSameGeneAsQuery){
        	phenoScoresOfNodePatientsWithoutVariant.add(r.getScore().get("_phenotypeScore"));
        }
        
        //temp, need to wire in the P-values
         //TODO add in the _phentypeMatches and _genotypeMatches etc here.
        for (MatchmakerResult result : nodePatientsWithVariantInSameGeneAsQuery){
        	results.add(new MatchmakerResult(result.getScore(), result.getPatient()));
        }
        
        

        results.sort(Comparator.comparingDouble(object -> {
            //yuk
            MatchmakerResult matchmakerResult = (MatchmakerResult) object;
            return matchmakerResult.getScore().get("patient");
        }).reversed());

        logger.info("Matches for patient: {}", patient.getId());
        results.forEach(matchmakerResult ->
                logger.info("{}-{}: {}", patient.getId(), matchmakerResult.getPatient().getId(), matchmakerResult.getScore()));

        return results;
    }

    private double calculateMatchScore(double genotypeScore, double phenotypeScore) {
        double mergedScore = genotypeScore * phenotypeScore;
        if (mergedScore > 1) {
            mergedScore = 1;
        }
        if (mergedScore < 0) {
            mergedScore = 0;
        }
        return mergedScore;
    }

}
