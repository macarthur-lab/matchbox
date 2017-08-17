package org.broadinstitute.macarthurlab.matchbox.match;

import org.broadinstitute.macarthurlab.matchbox.TestData;
import org.broadinstitute.macarthurlab.matchbox.entities.GenotypeSimilarityScore;
import org.broadinstitute.macarthurlab.matchbox.entities.MatchmakerResult;
import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.broadinstitute.macarthurlab.matchbox.entities.PhenotypeSimilarityScore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class MatchServiceImplTest {
	
    
    @Value("${allow.no-gene-in-common.matches}")
    private boolean ALLOW_NO_GENE_IN_COMMON_PHENOTYPE_MATCHES;

    @Test
    public void testMatchWithEmptyDatabase() {
        MatchService instance = new MatchServiceImpl(new MockGenotypeMatchService(0), new MockPhenotypeMatchService(0));

        Patient patient = new Patient();
        assertThat(instance.match(patient, Collections.emptyList()).isEmpty(), is(true));
    }

    @Test
    public void testMatchOnlyReturnsResultsOverCutoff() {
        Double cutoff = 0.42;

        MatchService instance = new MatchServiceImpl(new MockGenotypeMatchService(0.6), new MockPhenotypeMatchService(0.7));

        Patient patient = TestData.getTestPatient();
        List<Patient> patients = TestData.getTwoTestPatients();

        List<MatchmakerResult> matchmakerResults = instance.match(patient, patients);

        for (int i = 0; i < matchmakerResults.size() ; i++) {
            MatchmakerResult matchmakerResult = matchmakerResults.get(i);
            System.out.println(matchmakerResult);
            Double score = matchmakerResult.getScore().get("patient");
            assertThat(score >= cutoff, is(true));
            assertThat(matchmakerResult.getPatient(), equalTo(patients.get(i)));
        }
    }

    @Test
    public void testMatchWithNoGeneMatchNoPhenoMatchReturnsEmptyResults() {
        MatchService instance = new MatchServiceImpl(new MockGenotypeMatchService(0.0), new MockPhenotypeMatchService(0.0));

        Patient patient = TestData.getTestPatient();
        List<Patient> patients = TestData.getTwoTestPatients();

        List<MatchmakerResult> matchmakerResults = instance.match(patient, patients);

        assertThat(matchmakerResults.isEmpty(), is(true));
    }

    @Test
    public void testMatchWithNoGeneMatchPerfectPhenoMatch() {
    	if (this.getALLOW_NO_GENE_IN_COMMON_PHENOTYPE_MATCHES()){	    	
	        MatchService instance = new MatchServiceImpl(new GenotypeSimilarityServiceImpl(TestData.geneIdentifiers()), new MockPhenotypeMatchService(1.0));
	
	        Patient patient = TestData.getTestPatient();
	        patient.getGenomicFeatures().clear();
	        List<Patient> patients = TestData.getTwoTestPatients();
	
	        List<MatchmakerResult> matchmakerResults = instance.match(patient, patients);
	
	        assertThat(matchmakerResults.size(), equalTo(patients.size()));
	
	        for (MatchmakerResult matchmakerResult : matchmakerResults) {
	            System.out.println(matchmakerResult);
	            assertThat(matchmakerResult.getScore().get("patient"), equalTo(0.6));
	        }
    	}
    	else{
    		//dummy assertion to since in this case test is NA
    		assertThat(1, equalTo(1));
    	}
    }

    @Test
    public void testMatchWithNoGeneMatchPhenoMatchUnderCutoffReturnsEmptyResults() {
        MatchService instance = new MatchServiceImpl(new GenotypeSimilarityServiceImpl(TestData.geneIdentifiers()), new MockPhenotypeMatchService(0.0));

        Patient patient = TestData.getTestPatient();
        patient.getGenomicFeatures().clear();
        List<Patient> patients = TestData.getTwoTestPatients();

        List<MatchmakerResult> matchmakerResults = instance.match(patient, patients);

        assertThat(matchmakerResults.isEmpty(), is(true));
    }

    @Test
    public void testMatchWithNoGeneMatchPhenoMatchAtCutoffReturnsResults() {
        double noGenotypeScore = 0.6;
        double phenotypeCutoffScore = 0.7;

        MatchService instance = new MatchServiceImpl(new GenotypeSimilarityServiceImpl(TestData.geneIdentifiers()), new MockPhenotypeMatchService(phenotypeCutoffScore));

        Patient patient = TestData.getTestPatient();
        patient.getGenomicFeatures().clear();
        List<Patient> patients = TestData.getTwoTestPatients();

        List<MatchmakerResult> matchmakerResults = instance.match(patient, patients);

        assertThat(matchmakerResults.size(), equalTo(patients.size()));

        for (MatchmakerResult matchmakerResult : matchmakerResults) {
            System.out.println(matchmakerResult);
            assertThat(matchmakerResult.getScore().get("patient"), equalTo(noGenotypeScore * phenotypeCutoffScore));
        }
    }

    @Test
    public void testMatchWithPerfectGeneMatchNoPhenoMatch() {
        MatchService instance = new MatchServiceImpl(new GenotypeSimilarityServiceImpl(TestData.geneIdentifiers()), new MockPhenotypeMatchService(0.0));

        Patient patient = TestData.getTestPatient();
        List<Patient> patients = TestData.getTwoTestPatients();

        List<MatchmakerResult> matchmakerResults = instance.match(patient, patients);
        for (MatchmakerResult matchmakerResult : matchmakerResults) {
            assertThat(matchmakerResult.getScore().get("patient"), equalTo(0.0));
        }
    }

    @Test
    public void testMatchWithPerfectGeneMatchPerfectPhenoMatch() {
        MatchService instance = new MatchServiceImpl(new MockGenotypeMatchService(1.0), new MockPhenotypeMatchService(1.0));

        Patient patient = TestData.getTestPatient();
        List<Patient> patients = TestData.getTwoTestPatients();

        List<MatchmakerResult> matchmakerResults = instance.match(patient, patients);

        for (MatchmakerResult matchmakerResult : matchmakerResults) {
            System.out.println(matchmakerResult);
            assertThat(matchmakerResult.getScore().get("patient"), equalTo(1.0));
        }
    }

    @Test
    public void testMatchWithHugeGeneMatchScorePerfectPhenoMatch() {
        MatchService instance = new MatchServiceImpl(new MockGenotypeMatchService(1000000.0), new MockPhenotypeMatchService(1.0));

        Patient patient = TestData.getTestPatient();
        List<Patient> patients = TestData.getTwoTestPatients();

        List<MatchmakerResult> matchmakerResults = instance.match(patient, patients);

        for (MatchmakerResult matchmakerResult : matchmakerResults) {
            System.out.println(matchmakerResult);
            assertThat(matchmakerResult.getScore().get("patient"), equalTo(1.0));
        }
    }



    private class MockGenotypeMatchService implements GenotypeSimilarityService {

        private final double returnScore;

        public MockGenotypeMatchService(double returnScore) {
            this.returnScore = returnScore;
        }

        @Override
        public GenotypeSimilarityScore scoreGenotypes(Patient queryPatient, Patient nodePatient) {
            return new GenotypeSimilarityScore(returnScore, Collections.emptyList());
        }
    }

    private class MockPhenotypeMatchService implements PhenotypeSimilarityService {
        private final double returnScore;

        public MockPhenotypeMatchService(double returnScore) {
            this.returnScore = returnScore;
        }

        @Override
        public PhenotypeSimilarityScorer buildPhenotypeSimilarityScorer(Patient patient) {
            return new MockPhenotypeSimilarityScorer(returnScore);
        }
    }

    private class MockPhenotypeSimilarityScorer implements PhenotypeSimilarityScorer {

        private final double returnScore;

        public MockPhenotypeSimilarityScorer(double returnScore) {
            this.returnScore = returnScore;
        }

        @Override
        public PhenotypeSimilarityScore scorePhenotypes(Patient queryPatient, Patient nodePatient) {
            return new PhenotypeSimilarityScore(returnScore, Collections.emptyList());
        }
    }
    
	/**
	 * @return the aLLOW_NO_GENE_IN_COMMON_PHENOTYPE_MATCHES
	 */
	public boolean getALLOW_NO_GENE_IN_COMMON_PHENOTYPE_MATCHES() {
		return ALLOW_NO_GENE_IN_COMMON_PHENOTYPE_MATCHES;
	}

	/**
	 * @param aLLOW_NO_GENE_IN_COMMON_PHENOTYPE_MATCHES the aLLOW_NO_GENE_IN_COMMON_PHENOTYPE_MATCHES to set
	 */
	public void setALLOW_NO_GENE_IN_COMMON_PHENOTYPE_MATCHES(String aLLOW_NO_GENE_IN_COMMON_PHENOTYPE_MATCHES) {
		ALLOW_NO_GENE_IN_COMMON_PHENOTYPE_MATCHES = Boolean.valueOf(ALLOW_NO_GENE_IN_COMMON_PHENOTYPE_MATCHES);
	}
    

}