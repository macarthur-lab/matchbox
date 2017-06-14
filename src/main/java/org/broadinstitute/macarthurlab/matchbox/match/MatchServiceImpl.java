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
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * @author Harindra Arachchi <harindra@broadinstitute.org>
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
        
        /**
         * IF genotypes are given:
         * we are interested in patients with a variant in the same gene as the query, and only
         * those will be scored and returned
         */
        List<Double> patientGenotypeRankingScores = new ArrayList<Double>();
        List<Patient> patientsPickedByGenotype  = new ArrayList<Patient>();
        if (patient.getGenomicFeatures().size()>0){
        	patientsPickedByGenotype = this.getGenotypeMatch().searchByGenomicFeatures(patient);
        	patientGenotypeRankingScores = genotypeMatch.scoreGenotypes(patient, patientsPickedByGenotype);
        }
        /**
         * IF phenotypes are given:
         * another set of matches are found based ONLY phenotype matching.
         * 
         * Phenotype matching system as of now returns all patients scored, but better would be it determines
         * a best subset on some magic criteria so that component remains a black box and easily switchable
         */
        List<Double> patientPhenotypeRankingScores = new ArrayList<Double>();
        List<Patient> patientsPickedByPhenotype = new ArrayList<Patient>();
        if (patient.getFeatures().size()>0){
        	patientPhenotypeRankingScores = phenotypeMatch.scorePhenotypes(patient, patients);
        	patientsPickedByPhenotype = pickTopSubsetOfPhenotypeMatchesToReturn(patientPhenotypeRankingScores,patients);
        }
        
        /**
         * find the subset of results that we will send back as results
         * 1. for genotypes: All results that have a variant in a same gene as the query(this is already reflected in the result)
         * 2. for phenotypes: Jules will decide a proper cutoff
         * 
         * This function will make a combined final result set that takes into account 1,2
         */
        Map<String,Patient> resultsToSendBack = ascertainCombinedResultsToSendBack(patientsPickedByPhenotype,patientsPickedByGenotype);
        
        /**
         * Generate a merged score for all results for now (later we could only do this for patients we return)
         */
        Map<Patient,MatchScore> scores = generateMergedScore(patient, 
        											  resultsToSendBack, 
        											  patientsPickedByGenotype,
        											  patientGenotypeRankingScores, 
        											  patientsPickedByPhenotype,
        											  patientPhenotypeRankingScores);
        //need to adjust this func a bit
        //logTopNScores(8, patient.getId(), scores);

        List<MatchmakerResult> allResults = new ArrayList<>();
        for (Patient p:scores.keySet()) {
            MatchScore matchScore = scores.get(p);
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
    
    
    

    /**
     * Given a list of patients, and their phenotype ranking scores, sends back a list of them that are deemed
     * worthy to send back to querier (as we cannot send back all patients in the db to a request)
     * @param patientPhenotypeRankingScores a series of score representing similarity to query
     * @param patients	a series of patients
     * @return a subset of patients that are deemed a close match, and will be sent back as results
     */
	private List<Patient> pickTopSubsetOfPhenotypeMatchesToReturn(List<Double> patientPhenotypeRankingScores, List<Patient> patients) {
		// TODO Auto-generated method stub
		/**
		 * To get started, just sending back all patients, Jules, you will do the magic here as the subject expert
		 * and pick a good way to decide a cutoff and pick a subset of patients?
		 */
		return patients;
	}

	/**
     * find the subset of results between those found by genotypes and phenotypes 
     * that we will send back as results.
     * 
     * Inputs:
     * Results found via genotypes (all results that have a variant in a same gene as the query)
     * Results found via phenotypes: (Jules will decide a proper cutoff)
	 *
     * Output:
     * A map of ID of patient, to the full patient object  
     *  So in essence will return a UNION of the two lists
     */
    private Map<String, Patient> ascertainCombinedResultsToSendBack(List<Patient>patientsPickedByPhenotype, List<Patient>patientsPickedByGenotype)
    {
    	List<Patient> intersect = patientsPickedByGenotype.stream().filter(patientsPickedByPhenotype::contains).collect(Collectors.toList());
    	Map<String,Patient> sendBackToQuerier = new HashMap<String,Patient>();
    	for (Patient p:intersect){
    		sendBackToQuerier.put(p.getId(), p);
    	}
    	return sendBackToQuerier;
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
    private Map<Patient,MatchScore> generateMergedScore(Patient queryPatient, 
    											Map<String,Patient> resultsToSendBack,
    											List<Patient> patientsPickedByGenotype,
    											List<Double> patientGenotypeRankingScores,
    											List<Patient> patientsPickedByPhenotype,
    											List<Double> patientPhenotypeRankingScores) {    
        Map<String,List<Double>> scores=new HashMap<String,List<Double>>();
        //genotypes
        for (int i=0;i<patientsPickedByGenotype.size();i++){
        	if (resultsToSendBack.keySet().contains(patientsPickedByGenotype.get(i))){
        		if (scores.containsKey(patientsPickedByGenotype.get(i))){
        			scores.get(patientsPickedByGenotype.get(i)).add(patientGenotypeRankingScores.get(i));
        		}
        		else{
        			List<Double> patientScores = new ArrayList<Double>();
        			patientScores.add(patientGenotypeRankingScores.get(i));
        			scores.put(patientsPickedByGenotype.get(i).getId(), patientScores);
        		}
        	}
        }
        
        //phenotypes
        for (int i=0;i<patientsPickedByPhenotype.size();i++){
        	if (resultsToSendBack.keySet().contains(patientsPickedByPhenotype.get(i))){
        		if (scores.containsKey(patientsPickedByPhenotype.get(i))){
        			scores.get(patientsPickedByPhenotype.get(i)).add(patientPhenotypeRankingScores.get(i));
        		}
        		else{
        			List<Double> patientScores = new ArrayList<Double>();
        			patientScores.add(patientPhenotypeRankingScores.get(i));
        			scores.put(patientsPickedByPhenotype.get(i).getId(), patientScores);
        		}
        	}
        }
        
        //now merge the scores
        Map<Patient,MatchScore> merged = new HashMap<Patient,MatchScore>();
        for (String id:scores.keySet()){
        	Patient matchPatient = resultsToSendBack.get(id);
            Double genotypeScore = scores.get(id).get(0);   //are there cases when there won't be this score? 
            Double phenotypeScore = scores.get(id).get(1);  //are there cases when there won't be this score?
            merged.put(matchPatient,new MatchScore(queryPatient.getId(), matchPatient.getId(), genotypeScore, phenotypeScore));
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
