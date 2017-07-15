package org.broadinstitute.macarthurlab.matchbox.match;

import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.broadinstitute.macarthurlab.matchbox.entities.PhenotypeSimilarityScore;
import org.monarchinitiative.exomiser.core.phenotype.Model;
import org.monarchinitiative.exomiser.core.phenotype.ModelPhenotypeMatch;
import org.monarchinitiative.exomiser.core.phenotype.ModelScorer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class PhenotypeSimilarityScorerImpl implements PhenotypeSimilarityScorer {

    private static final Logger logger = LoggerFactory.getLogger(PhenotypeSimilarityScorerImpl.class);

    private final ModelScorer phenotypeModelScorer;

    public PhenotypeSimilarityScorerImpl(ModelScorer phenotypeModelScorer) {
        this.phenotypeModelScorer = phenotypeModelScorer;
    }

    @Override
    public PhenotypeSimilarityScore scorePhenotypes(Patient queryPatient, Patient nodePatient) {
        if (queryPatient.getFeatures().isEmpty() || nodePatient.getFeatures().isEmpty()) {
            //don't overly penalize these - maybe the genotype score is high which could lead to a good match.
            return new PhenotypeSimilarityScore(0.6, Collections.emptyList());
        }

        PatientModel nodePatientModel = toModel(nodePatient);
        ModelPhenotypeMatch modelPhenotypeMatch = phenotypeModelScorer.scoreModel(nodePatientModel);
        logger.debug("{}-{} phenotype similarity score = {}", queryPatient.getId(), nodePatient.getId(), modelPhenotypeMatch.getScore());
        return new PhenotypeSimilarityScore(modelPhenotypeMatch.getScore(), Collections.emptyList());
    }

    private PatientModel toModel(Patient patient) {
        List<String> phenotypes = PhenotypeSimilarityService.getObservedPhenotypeIds(patient);
        return new PatientModel(patient.getId(), phenotypes);
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
