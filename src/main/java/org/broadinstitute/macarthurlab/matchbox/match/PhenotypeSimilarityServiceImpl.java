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
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Service
public class PhenotypeSimilarityServiceImpl implements PhenotypeSimilarityService {

    private static final Logger logger = LoggerFactory.getLogger(PhenotypeSimilarityServiceImpl.class);

    private final PhenotypeMatchService phenotypeMatchService;

    @Autowired
    public PhenotypeSimilarityServiceImpl(PhenotypeMatchService phenotypeMatchService) {
        this.phenotypeMatchService = phenotypeMatchService;
    }

    /**
     * Ranks a patient list by their phenotype similarity to a query patient
     *
     * @param queryPatient a target patient to rank against
     * @param patients     a list of patients to rank
     * @return Sends back a list of scores for each patient based on phenotype. Order matches input list
     */
    public List<Double> scorePhenotypes(Patient queryPatient, List<Patient> patients) {
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
            ModelPhenotypeMatch modelPhenotypeMatch = modelScorer.scoreModel(patientModel);
//            logger.info("{}", modelPhenotypeMatch);
            if (queryPatient.getFeatures().isEmpty() || patient.getFeatures().isEmpty()) {
                //don't overly penalize these - maybe the genotype score is high which could lead to a good match.
                patientPhenotypeRankingScores.add(0.6);
            } else {
                logger.debug("{}-{} phenotype similarity score = {}", queryPatient.getId(), patient.getId(), modelPhenotypeMatch.getScore());
                patientPhenotypeRankingScores.add(modelPhenotypeMatch.getScore());
            }
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

    private class PatientModel implements Model {

        private final String id;
        private final List<String> phenotypes;

        PatientModel(String id, List<String> phenotypes) {
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
