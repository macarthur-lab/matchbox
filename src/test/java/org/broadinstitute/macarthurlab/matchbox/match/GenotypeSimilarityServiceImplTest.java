package org.broadinstitute.macarthurlab.matchbox.match;

import org.broadinstitute.macarthurlab.matchbox.TestData;
import org.broadinstitute.macarthurlab.matchbox.entities.GenomicFeature;
import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;
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

        List<Double> matches = patients.stream()
                .map(nodePatient -> genotypeSimilarityService.scoreGenotypes(patient, nodePatient))
                .map(GenotypeSimilarityScore::getScore)
                .collect(toList());

        List<Double> expected = Arrays.asList(1.0, 1.0);
        assertThat(matches, equalTo(expected));
    }

    @Test
    public void testMatchWhenNoSoTermsSupplied() {
        List<Patient> patients = TestData.getTwoTestPatients();
        Patient patient = patients.get(0);

        GenotypeSimilarityService genotypeSimilarityService = new GenotypeSimilarityServiceImpl(TestData.geneIdentifiers());

        List<Double> matches = patients.stream()
                .map(nodePatient -> genotypeSimilarityService.scoreGenotypes(patient, nodePatient))
                .map(GenotypeSimilarityScore::getScore)
                .collect(toList());
        List<Double> expected = Arrays.asList(0.5, 0.5);
        assertThat(matches, equalTo(expected));
    }

    @Test
    public void testMatchWhenQueryPatientHasNoGenotype() {
        List<Patient> patients = TestData.getTwoTestPatients();
        Patient queryPatientWithoutGenomicFeatures = TestData.getTestPatient();
        queryPatientWithoutGenomicFeatures.getGenomicFeatures().clear();

        GenotypeSimilarityService genotypeSimilarityService = new GenotypeSimilarityServiceImpl(TestData.geneIdentifiers());

        List<Double> matches = patients.stream()
                .map(nodePatient -> genotypeSimilarityService.scoreGenotypes(queryPatientWithoutGenomicFeatures, nodePatient))
                .map(GenotypeSimilarityScore::getScore)
                .collect(toList());
        List<Double> expected = Arrays.asList(0.6, 0.6);
        assertThat(matches, equalTo(expected));
    }

    @Test
    public void testFindGenomicFeatureMatches() {
        List<Patient> testPatients = TestData.getTwoTestPatients();
        Patient testP1 = testPatients.get(0);
        Patient testP2 = testPatients.get(1);

        GenotypeSimilarityServiceImpl genotypeSimilarityService = new GenotypeSimilarityServiceImpl(TestData.geneIdentifiers());
        List<GenomicFeatureMatch> commonGenes = genotypeSimilarityService.findGenomicFeatureMatches(testP1, testP2);
        GenomicFeature genomicFeature = testP1.getGenomicFeatures().get(0);
        List<GenomicFeatureMatch> expected = Collections.singletonList(new GenomicFeatureMatch(genomicFeature, genomicFeature));
        assertThat(commonGenes, equalTo(expected));
    }
}