/**
 * Represents a matchmaker match
 */
package org.broadinstitute.macarthurlab.matchbox.match;

import org.broadinstitute.macarthurlab.matchbox.entities.MatchmakerResult;
import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
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
        for (Patient nodePatient : patients) {
            GenotypeSimilarityScore genotypeSimilarityScore = genotypeSimilarityService.scoreGenotypes(patient, nodePatient);
            PhenotypeSimilarityScore phenotypeSimilarityScore = phenotypeSimilarityScorer.scorePhenotypes(patient, nodePatient);
            double genotypeScore = genotypeSimilarityScore.getScore();
            double phenotypeScore = phenotypeSimilarityScore.getScore();
            if (genotypeSimilarityScore.hasCommonGene() || phenotypeScore >= 0.7) {
                double matchScore = calculateMatchScore(genotypeScore, phenotypeScore);
//                logger.info("{}-{}: {} genoScore: {} phenoScore: {}", patient.getId(), nodePatient.getId(), matchScore, genotypeScore, phenotypeScore);
                Map<String, Double> score = new HashMap<>();
                score.put("patient", matchScore);
                score.put("_genotypeScore", genotypeScore);
                score.put("_phenotypeScore", phenotypeScore);
                //TODO add in the _phentypeMatches and _genotypeMatches etc here.
                results.add(new MatchmakerResult(score, nodePatient));
            }
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
