package org.broadinstitute.macarthurlab.matchbox.match;

import org.broadinstitute.macarthurlab.matchbox.TestData;
import org.broadinstitute.macarthurlab.matchbox.entities.GenomicFeature;
import org.broadinstitute.macarthurlab.matchbox.entities.GenomicFeatureMatch;
import org.broadinstitute.macarthurlab.matchbox.entities.GenotypeSimilarityScore;
import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.broadinstitute.macarthurlab.matchbox.entities.Variant;
import org.broadinstitute.macarthurlab.matchbox.network.Communication;
import org.junit.Test;

import java.text.DecimalFormat;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class GenotypeSimilarityServiceImplTest {

    @Test
    public void testNoMatchingGeneIdentifierInCache() {
        List<Patient> patients = Arrays.asList(TestData.getTestPatient(), TestData.getTestPatient());
        Patient patient = patients.get(0);

        GenotypeSimilarityService genotypeSimilarityService = new GenotypeSimilarityServiceImpl(Collections.emptyMap());

        List<Double> matches = patients.stream()
                .map(nodePatient -> genotypeSimilarityService.scoreGenotypes(patient, nodePatient))
                .map(GenotypeSimilarityScore::getScore)
                .collect(toList());

        List<Double> expected = Arrays.asList(0.6, 0.6);
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
    public void testMatchWhenNodePatientHasNoGenotype() {
        List<Patient> patients = TestData.getTwoTestPatients();
        Patient queryPatientWithoutGenomicFeatures = TestData.getTestPatient();

        GenotypeSimilarityService genotypeSimilarityService = new GenotypeSimilarityServiceImpl(TestData.geneIdentifiers());

        List<Double> matches = patients.stream()
                .map(nodePatient -> {nodePatient.getGenomicFeatures().clear(); return nodePatient;})
                .map(nodePatient -> genotypeSimilarityService.scoreGenotypes(queryPatientWithoutGenomicFeatures, nodePatient))
                .map(GenotypeSimilarityScore::getScore)
                .collect(toList());
        List<Double> expected = Arrays.asList(0.6, 0.6);
        assertThat(matches, equalTo(expected));
    }

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
    public void testMatchingGeneSymbolOnlyNodeMatchHigerSpecified() {
        GenomicFeature geneOneSymbolOnly = new GenomicFeature(Collections.singletonMap("id", "ENSG00000152926"), new Variant(), -1L, Collections.emptyMap());
        Patient queryPatient = new Patient("queryPatient", "queryPatient", Collections.emptyMap(), "9606", "M", "", "", Collections.emptyList(), Collections.emptyList(), Arrays.asList(geneOneSymbolOnly));

        Variant variantOne = new Variant("GRCh37","7", 64438667L, 64438667L, "G", "A");
        GenomicFeature geneOne = new GenomicFeature(Collections.singletonMap("id", "ENSG00000152926"), variantOne, 2L, Collections.emptyMap());

        Variant variantTwo = new Variant("GRCh37","18", 25616451L, 25616451L, "A", "T");
        GenomicFeature geneTwo = new GenomicFeature(Collections.singletonMap("id", "ENSG00000170558"), variantTwo, 2L, Collections.emptyMap());
        Patient nodePatient = new Patient("nodePatient", "nodePatient", Collections.emptyMap(), "9606", "M", "", "", Collections.emptyList(), Collections.emptyList(), Arrays.asList(geneTwo, geneOne));

        Map<String, String> geneIdentifiers = new HashMap<>();
        geneIdentifiers.put("GENE1", "ENSG00000152926");
        geneIdentifiers.put("GENE2", "ENSG00000170558");

        GenotypeSimilarityService genotypeSimilarityService = new GenotypeSimilarityServiceImpl(geneIdentifiers);
        genotypeSimilarityService.setHttpCommunication(new Communication());
        
        GenotypeSimilarityScore genotypeSimilarityScore = genotypeSimilarityService.scoreGenotypes(queryPatient, nodePatient);
        DecimalFormat df = new DecimalFormat("#.##");
        
        assertThat(df.format(genotypeSimilarityScore.getScore()), equalTo("0.74"));
    }

    @Test
    public void testGeneSymbolMatchOnly() {
        long unintialisedZygosityValue = -1L;
        Variant variantOne = new Variant("GRCh37","7", 64438667L, 64438667L, "G", "A");
        Map<String, String> soTerm = new HashMap<>();
        soTerm.put("id", "SO:0001587");
        soTerm.put("label", "STOPGAIN");
        GenomicFeature geneOne = new GenomicFeature(Collections.singletonMap("id", "ENSG00000152926"), variantOne, unintialisedZygosityValue, soTerm);

        Variant variantTwo = new Variant("18", "GRCh37", 25616451L, 25616451L, "A", "T");
        GenomicFeature geneTwo = new GenomicFeature(Collections.singletonMap("id", "ENSG00000170558"), variantTwo, unintialisedZygosityValue, soTerm);

        Patient patient1 = new Patient("patient1", "patient1", Collections.emptyMap(), "9606", "M", "", "", Collections.emptyList(), Collections.emptyList(), Arrays.asList(geneOne));
        Patient patient2 = new Patient("patient2", "patient2", Collections.emptyMap(), "9606", "M", "", "", Collections.emptyList(), Collections.emptyList(), Arrays.asList(geneTwo, geneOne));

        Map<String, String> geneIdentifiers = new HashMap<>();
        geneIdentifiers.put("GENE1", "ENSG00000152926");
        geneIdentifiers.put("GENE2", "ENSG00000170558");

        GenotypeSimilarityService genotypeSimilarityService = new GenotypeSimilarityServiceImpl(geneIdentifiers);
        genotypeSimilarityService.setHttpCommunication(new Communication());
        
        GenotypeSimilarityScore genotypeSimilarityScore = genotypeSimilarityService.scoreGenotypes(patient1, patient2);

        DecimalFormat df = new DecimalFormat("#.##");
        assertThat(df.format(genotypeSimilarityScore.getScore()), equalTo("0.74"));
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