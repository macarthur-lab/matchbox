/**
 * Represents a phenotype based match
 */
package org.broadinstitute.macarthurlab.matchbox.match;

import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.broadinstitute.macarthurlab.matchbox.entities.PhenotypeFeature;
import org.monarchinitiative.exomiser.core.phenotype.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

/**
 * @author harindra
 */
@Service
public class PhenotypeSimilarityServiceImpl implements PhenotypeSimilarityService {

    private static final Logger logger = LoggerFactory.getLogger(PhenotypeSimilarityServiceImpl.class);

    @Autowired
    private PhenotypeMatchService phenotypeMatchService;

    /**
     * Constructor sets up everything
     */
    public PhenotypeSimilarityServiceImpl() {
    }

    /**
     * Ranks a patient list by their phenotype similarity to a query patient
     *
     * @param patients     a list of patients to rank
     * @param queryPatient a target patient to rank against
     * @return Sends back a list of scores for each patient based on phenotype. Order matches input list
     */
    public List<Double> rankByPhenotypes(List<Patient> patients, Patient queryPatient) {
        ModelScorer modelScorer = setUpModelScorer(queryPatient);

//        return patients.stream()
//                .map(this::toModel)
//                .map(modelScorer::scoreModel)
//                //this is not good - we're throwing away all the useful match information
//                .map(ModelPhenotypeMatch::getScore)
//                .collect(toList());

        List<Double> patientPhenotypeRankingScores = new ArrayList<>();
        for (Patient patient : patients) {
            PatientModel patientModel = toModel(patient);
            ModelPhenotypeMatch matchScore = modelScorer.scoreModel(patientModel);
            logger.info("{}", matchScore);
//            double phenotypeSimilarityScore = this.getPhenotypeSimilarity(patient, queryPatient);
            patientPhenotypeRankingScores.add(matchScore.getScore());
        }
        return patientPhenotypeRankingScores;
    }

    private ModelScorer setUpModelScorer(Patient queryPatient) {
        List<String> queryPatientPhenotypes = getObservedPhenotypes(queryPatient);
        List<PhenotypeTerm> queryPhenotypeTerms = phenotypeMatchService.makePhenotypeTermsFromHpoIds(queryPatientPhenotypes);
        PhenotypeMatcher hpHpQueryMatcher = phenotypeMatchService.getHumanPhenotypeMatcherForTerms(queryPhenotypeTerms);
        return PhenodigmModelScorer.forSameSpecies(hpHpQueryMatcher);
    }

    private PatientModel toModel(Patient patient) {
        List<String> phenotypes = getObservedPhenotypes(patient);
        return new PatientModel(patient.getId(), phenotypes);
    }

    private List<String> getObservedPhenotypes(Patient patient) {
        return patient.getFeatures().stream()
                .filter(phenotypeFeature -> "yes".equals(phenotypeFeature.getObserved()))
                .map(PhenotypeFeature::getId)
                .collect(toList());
    }


    /**
     * As a first VERY naive step, we will simply get the number of
     * HPO terms they have in common against the total number of HPO terms.
     * NOTE: in a perfect match, returns 0.5 as per weight allowed to phenotypes
     *
     * @param p1 patient 1
     * @param p2 patient 2
     * @return a representative number (described above)
     */
    private double getPhenotypeSimilarity(Patient p1, Patient queryPatient) {
        List<String> p1Features = new ArrayList<String>();
        p1.getFeatures().forEach((k) -> {
            p1Features.add(k.getId());
        });
        List<String> queryFeatures = new ArrayList<String>();
        queryPatient.getFeatures().forEach((k) -> {
            queryFeatures.add(k.getId());
        });
        List<String> p1p2Intersect = p1Features.stream()
                .filter(queryFeatures::contains)
                .collect(toList());
        /**
         * If ALL of the query is a subset of the match, still return
         * a high score of 0.4. Then it is assumed that the query just
         * didn't have/send all the information, but a good match anyway.
         */
        if (p1p2Intersect.size() == queryFeatures.size() && p1p2Intersect.size() < p1Features.size()) {
            return 0.4d;
        }
        /**
         * If a PERFECT match return 0.5
         */
        if (p1p2Intersect.size() == p1Features.size()) {
            return 0.5d;
        }
        /**
         * Otherwise return a metric of inclusion
         */
        return (double) p1p2Intersect.size() / ((double) p1.getFeatures().size() + (double) queryPatient.getFeatures()
                .size());
    }


    private class PatientModel implements Model {

        private final String id;
        private final List<String> phenotypes;

        public PatientModel(String id, List<String> phenotypes) {
            this.id = id;
            this.phenotypes = phenotypes;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public List<String> getPhenotypeIds() {
            return phenotypes;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PatientModel that = (PatientModel) o;
            return Objects.equals(id, that.id) &&
                    Objects.equals(phenotypes, that.phenotypes);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, phenotypes);
        }

        @Override
        public String toString() {
            return "PatientModel{" +
                    "id='" + id + '\'' +
                    ", phenotypes=" + phenotypes +
                    '}';
        }
    }
}
