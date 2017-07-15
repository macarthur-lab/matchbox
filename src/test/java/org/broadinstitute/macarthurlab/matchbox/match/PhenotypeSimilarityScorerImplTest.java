package org.broadinstitute.macarthurlab.matchbox.match;

import org.broadinstitute.macarthurlab.matchbox.TestData;
import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.broadinstitute.macarthurlab.matchbox.entities.PhenotypeSimilarityScore;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.phenotype.Model;
import org.monarchinitiative.exomiser.core.phenotype.ModelPhenotypeMatch;
import org.monarchinitiative.exomiser.core.phenotype.ModelScorer;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class PhenotypeSimilarityScorerImplTest {

    private final double defaultScore = 0.6;

    @Test
    public void testQueryPatientWithNoPhenotypesReturnsDefaultScore() {

        PhenotypeSimilarityScorer instance = new PhenotypeSimilarityScorerImpl(null);

        Patient patientWithoutPhenotypes = TestData.getTestPatient();
        patientWithoutPhenotypes.getFeatures().clear();

        List<Patient> patients = TestData.getTwoTestPatients();

        assertThat(instance.scorePhenotypes(patientWithoutPhenotypes, patients.get(0)), equalTo(new PhenotypeSimilarityScore(defaultScore, Collections.emptyList())));
    }

    @Test
    public void testNodePatientWithNoPhenotypesReturnsDefaultScore() {

        PhenotypeSimilarityScorer instance = new PhenotypeSimilarityScorerImpl(null);

        Patient queryPatient = TestData.getTestPatient();
        List<Patient> nodePatients = TestData.getTwoTestPatients();

        Patient nodePatientWithoutPhenotypes = nodePatients.get(0);
        nodePatientWithoutPhenotypes.getFeatures().clear();

        assertThat(instance.scorePhenotypes(queryPatient, nodePatientWithoutPhenotypes), equalTo(new PhenotypeSimilarityScore(defaultScore, Collections.emptyList())));
    }

    @Test
    public void testGoodPhenotypeMatchReturnsPhenotypeSimilarityScore() {

        PhenotypeSimilarityScorer instance = new PhenotypeSimilarityScorerImpl(new MockPhenotypeModelScorer(0.7));

        Patient queryPatient = TestData.getTestPatient();
        List<Patient> nodePatients = TestData.getTwoTestPatients();

        assertThat(instance.scorePhenotypes(queryPatient, nodePatients.get(0)), equalTo(new PhenotypeSimilarityScore(0.7, Collections.emptyList())));
    }

    private class MockPhenotypeModelScorer implements ModelScorer {

        private final double returnScore;

        MockPhenotypeModelScorer(double returnScore) {
            this.returnScore = returnScore;
        }

        @Override
        public ModelPhenotypeMatch scoreModel(Model model) {
            System.out.println("Scoring model " + model);
            return ModelPhenotypeMatch.of(returnScore, model, Collections.emptyList());
        }
    }
}