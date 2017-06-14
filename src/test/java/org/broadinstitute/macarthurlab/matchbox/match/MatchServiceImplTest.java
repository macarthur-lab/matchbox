package org.broadinstitute.macarthurlab.matchbox.match;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class MatchServiceImplTest {

//    @Test
//    public void testMatchWithEmptyDatabase() {
//        MatchService instance = new MatchServiceImpl(new MockGenotypeMatchService(0), new MockPhenotypeMatchService(0));
//
//        Patient patient = new Patient();
//        assertThat(instance.match(patient, Collections.emptyList()).isEmpty(), is(true));
//    }
//
//    @Test
//    public void testMatchOnlyReturnsResultsOverCutoff() {
//        Double cutoff = 0.42;
//        int maxExpected = 5;
//        MatchService instance = new MatchServiceImpl(new MockGenotypeMatchService(0.6), new MockPhenotypeMatchService(0.7));
//
//        Patient patient = TestData.getTestPatient();
//        List<Patient> patients = TestData.getTwoTestPatients();
//
//        List<MatchmakerResult> matchmakerResults = instance.match(patient, patients);
//
//        assertThat(matchmakerResults.size() <= maxExpected, is(true));
//
//        for (int i = 0; i < matchmakerResults.size() ; i++) {
//            MatchmakerResult matchmakerResult = matchmakerResults.get(i);
//            System.out.println(matchmakerResult);
//            Double score = matchmakerResult.getScore().get("patient");
//            assertThat(score >= cutoff, is(true));
//            assertThat(matchmakerResult.getPatient(), equalTo(patients.get(i)));
//        }
//    }
//
//    @Test
//    public void testMatchReturnsNothingWhenResultsAreBelowCutoff() {
//        MatchService instance = new MatchServiceImpl(new MockGenotypeMatchService(0.0), new MockPhenotypeMatchService(0.0));
//
//        Patient patient = TestData.getTestPatient();
//        List<Patient> patients = TestData.getTwoTestPatients();
//
//        List<MatchmakerResult> matchmakerResults = instance.match(patient, patients);
//
//        assertThat(matchmakerResults.isEmpty(), is(true));
//    }
//
//
//    @Test
//    public void testMatchWithNoGeneMatchNoPhenoMatch() {
//        MatchService instance = new MatchServiceImpl(new MockGenotypeMatchService(0.0), new MockPhenotypeMatchService(0.0));
//
//        Patient patient = TestData.getTestPatient();
//        List<Patient> patients = TestData.getTwoTestPatients();
//
//        List<MatchmakerResult> matchmakerResults = instance.match(patient, patients);
//
//        for (MatchmakerResult matchmakerResult : matchmakerResults) {
//            System.out.println(matchmakerResult);
//            assertThat(matchmakerResult.getScore().get("patient"), equalTo(0.0));
//        }
//    }
//
//    @Test
//    public void testMatchWithNoGeneMatchPerfectPhenoMatch() {
//        MatchService instance = new MatchServiceImpl(new MockGenotypeMatchService(0.0), new MockPhenotypeMatchService(1.0));
//
//        Patient patient = TestData.getTestPatient();
//        List<Patient> patients = TestData.getTwoTestPatients();
//
//        List<MatchmakerResult> matchmakerResults = instance.match(patient, patients);
//
//        for (MatchmakerResult matchmakerResult : matchmakerResults) {
//            System.out.println(matchmakerResult);
//            assertThat(matchmakerResult.getScore().get("patient"), equalTo(0.0));
//        }
//    }
//
//    @Test
//    public void testMatchWithPerfectGeneMatchNoPhenoMatch() {
//        MatchService instance = new MatchServiceImpl(new MockGenotypeMatchService(1.0), new MockPhenotypeMatchService(0.0));
//
//        Patient patient = TestData.getTestPatient();
//        List<Patient> patients = TestData.getTwoTestPatients();
//
//        List<MatchmakerResult> matchmakerResults = instance.match(patient, patients);
//
//        for (MatchmakerResult matchmakerResult : matchmakerResults) {
//            System.out.println(matchmakerResult);
//            assertThat(matchmakerResult.getScore().get("patient"), equalTo(0.0));
//        }
//    }
//
//    @Test
//    public void testMatchWithPerfectGeneMatchPerfectPhenoMatch() {
//        MatchService instance = new MatchServiceImpl(new MockGenotypeMatchService(1.0), new MockPhenotypeMatchService(1.0));
//
//        Patient patient = TestData.getTestPatient();
//        List<Patient> patients = TestData.getTwoTestPatients();
//
//        List<MatchmakerResult> matchmakerResults = instance.match(patient, patients);
//
//        for (MatchmakerResult matchmakerResult : matchmakerResults) {
//            System.out.println(matchmakerResult);
//            assertThat(matchmakerResult.getScore().get("patient"), equalTo(1.0));
//        }
//    }
//
//    @Test
//    public void testMatchWithHugeGeneMatchScorePerfectPhenoMatch() {
//        MatchService instance = new MatchServiceImpl(new MockGenotypeMatchService(1000000.0), new MockPhenotypeMatchService(1.0));
//
//        Patient patient = TestData.getTestPatient();
//        List<Patient> patients = TestData.getTwoTestPatients();
//
//        List<MatchmakerResult> matchmakerResults = instance.match(patient, patients);
//
//        for (MatchmakerResult matchmakerResult : matchmakerResults) {
//            System.out.println(matchmakerResult);
//            assertThat(matchmakerResult.getScore().get("patient"), equalTo(1.0));
//        }
//    }
//
//
//
//    private class MockGenotypeMatchService implements GenotypeSimilarityService {
//
//        private final Double returnScore;
//
//        public MockGenotypeMatchService(double returnScore) {
//            this.returnScore = returnScore;
//        }
//
//        @Override
//        public List<GenotypeSimilarityScore> scoreGenotypes(Patient queryPatient, List<Patient> patients) {
//            return patients.stream().map(patient -> returnScore).collect(toList());
//        }
//    }
//
//    private class MockPhenotypeMatchService implements PhenotypeSimilarityService {
//        private final Double returnScore;
//
//        public MockPhenotypeMatchService(double returnScore) {
//            this.returnScore = returnScore;
//        }
//
//        @Override
//        public List<Double> scorePhenotypes(Patient queryPatient, List<Patient> patients) {
//            return patients.stream().map(patient -> returnScore).collect(toList());
//        }
//    }
}