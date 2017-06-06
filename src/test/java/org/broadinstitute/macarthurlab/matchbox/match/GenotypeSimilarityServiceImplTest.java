package org.broadinstitute.macarthurlab.matchbox.match;

import org.broadinstitute.macarthurlab.matchbox.TestData;
import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class GenotypeSimilarityServiceImplTest {


    @Test
    public void testExactMatch() {
        List<Patient> patients = Arrays.asList(TestData.getTestPatient(), TestData.getTestPatient());
        Patient patient = patients.get(0);

        GenotypeSimilarityService genotypeSimilarityService = new GenotypeSimilarityServiceImpl(TestData.geneIdentifiers());

        List<Double> matches = genotypeSimilarityService.scoreGenotypes(patient, patients);
        List<Double> expected = Arrays.asList(1.0, 1.0);
        assertThat(matches, equalTo(expected));
    }

    @Test
    public void testMatchWhenNoSoTermsSupplied() {
        List<Patient> patients = TestData.getTwoTestPatients();
        Patient patient = patients.get(0);

        GenotypeSimilarityService genotypeSimilarityService = new GenotypeSimilarityServiceImpl(TestData.geneIdentifiers());

        List<Double> matches = genotypeSimilarityService.scoreGenotypes(patient, patients);
        List<Double> expected = Arrays.asList(1.0, 1.0);
        assertThat(matches, equalTo(expected));
    }

    @Test
    public void testMatchWhenQueryPatientHasNoGenotype() {
        List<Patient> patients = TestData.getTwoTestPatients();
        Patient patient = new Patient();

        GenotypeSimilarityService genotypeSimilarityService = new GenotypeSimilarityServiceImpl(TestData.geneIdentifiers());

        List<Double> matches = genotypeSimilarityService.scoreGenotypes(patient, patients);
        List<Double> expected = Arrays.asList(0.6, 0.6);
        assertThat(matches, equalTo(expected));
    }

    /**
     * TestfindCommonGenes() function
     */
    @Test
    public void testFindCommonGenes() {
        List<Patient> testPatients = TestData.getTwoTestPatients();
        Patient testP1 = testPatients.get(0);
        Patient testP2 = testPatients.get(1);

        GenotypeSimilarityServiceImpl genotypeSimilarityService = new GenotypeSimilarityServiceImpl(TestData.geneIdentifiers());
        List<String> commonGenes = genotypeSimilarityService.findCommonGenes(testP1, testP2);
        List<String> expected = Collections.singletonList("PDE4DIP");
        assertThat(commonGenes, equalTo(expected));
    }

}