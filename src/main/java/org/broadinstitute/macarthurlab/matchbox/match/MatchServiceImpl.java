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

import static java.util.stream.Collectors.toList;

/**
 * @author harindra
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Service
public class MatchServiceImpl implements MatchService {

    private static final Logger logger = LoggerFactory.getLogger(MatchServiceImpl.class);

    private final GenotypeSimilarityService genotypeMatch;
    private final PhenotypeSimilarityService phenotypeMatch;

    @Autowired
    public MatchServiceImpl(GenotypeSimilarityService genotypeMatch, PhenotypeSimilarityService phenotypeMatch) {
        this.genotypeMatch = genotypeMatch;
        this.phenotypeMatch = phenotypeMatch;
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
        List<Double> patientGenotypeRankingScores = genotypeMatch.scoreGenotypes(patient, patients);
        List<Double> patientPhenotypeRankingScores = phenotypeMatch.scorePhenotypes(patient, patients);

        List<MatchScore> scores = generateMergedScore(patient, patients, patientGenotypeRankingScores, patientPhenotypeRankingScores);

        logTopNScores(8, patient.getId(), scores);

        List<MatchmakerResult> allResults = new ArrayList<>();
        for (int i = 0; i < patients.size(); i++) {
            Patient p = patients.get(i);
            MatchScore matchScore = scores.get(i);
            logger.debug("{}", matchScore);
            Map<String, Double> score = new HashMap<>();
            score.put("patient", matchScore.getScore());
            //TODO add in the _phentypeMatches and _genotypeMatches etc here. This will require more informative return types from the geno/phenoMatchers
            allResults.add(new MatchmakerResult(score, p));
        }

        //TODO: still need to finalise these cutoffs.
        //return the top five highest scores over 0.42
        return allResults.stream()
                .filter(matchmakerResult -> matchmakerResult.getScore().get("patient") >= 0.42)
                .sorted(Comparator.comparingDouble(object -> {
                    //yuk
                    MatchmakerResult matchmakerResult = (MatchmakerResult) object;
                    return matchmakerResult.getScore().get("patient");
                }).reversed())
                .limit(5)
                .collect(toList());
    }

    private void logTopNScores(int num, String patientId, List<MatchScore> scores) {
        logger.info("Top {} matches for {}:", num, patientId);
        scores.stream().sorted(Comparator.comparingDouble(MatchScore::getScore).reversed()).limit(num).forEach(matchScore -> logger.info("{}", matchScore));
    }


    /**
     * Merge phenotype and genotype scores into a single score,
     * <p>
     * Algorithm: the absolute value of the weighted average of
     * genotype (weight:1) and phenotype(weight:1)
     *
     * @param patientGenotypeRankingScores  scores based on genotypes
     * @param patientPhenotypeRankingScores scores based on phenotypes
     * @return A merged score
     */
    private List<MatchScore> generateMergedScore(Patient queryPatient, List<Patient> patients, List<Double> patientGenotypeRankingScores, List<Double> patientPhenotypeRankingScores) {
        List<MatchScore> merged = new ArrayList<>();
        for (int i = 0; i < patients.size(); i++) {
            Patient matchPatient = patients.get(i);
            Double genotypeScore = patientGenotypeRankingScores.get(i);
            Double phenotypeScore = patientPhenotypeRankingScores.get(i);
            merged.add(new MatchScore(queryPatient.getId(), matchPatient.getId(), genotypeScore, phenotypeScore));
        }
        return merged;
    }

    private class MatchScore {

        private final String queryPatientId;
        private final String matchPatientId;

        private final double score;

        private final double genotypeScore;
        private final double phenotypeScore;


        MatchScore(String queryPatientId, String matchPatientId, double genotypeScore, double phenotypeScore) {
            this.queryPatientId = queryPatientId;
            this.matchPatientId = matchPatientId;
            this.genotypeScore = genotypeScore;
            this.phenotypeScore = phenotypeScore;
            this.score = calcualateScore(genotypeScore, phenotypeScore);
        }

        private double calcualateScore(double genotypeScore, double phenotypeScore) {
            double mergedScore = genotypeScore * phenotypeScore;
            if (mergedScore > 1) {
                mergedScore = 1;
            }
            if (mergedScore < 0) {
                mergedScore = 0;
            }
            return mergedScore;
        }

        public double getScore() {
            return score;
        }

        public double getGenotypeScore() {
            return genotypeScore;
        }

        public double getPhenotypeScore() {
            return phenotypeScore;
        }

        @Override
        public String toString() {
            return "MatchScore{" +
                    "queryPatientId='" + queryPatientId + '\'' +
                    ", matchPatientId='" + matchPatientId + '\'' +
                    ", score=" + score +
                    ", genotypeScore=" + genotypeScore +
                    ", phenotypeScore=" + phenotypeScore +
                    '}';
        }
    }

}
