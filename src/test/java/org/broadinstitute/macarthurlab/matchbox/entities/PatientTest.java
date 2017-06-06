package org.broadinstitute.macarthurlab.matchbox.entities;

//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.json.JsonTest;
//import org.springframework.boot.test.json.JacksonTester;
//import org.springframework.boot.test.json.JsonContent;
//import org.springframework.test.context.junit4.SpringRunner;

import org.broadinstitute.macarthurlab.matchbox.TestData;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
//@RunWith(SpringRunner.class)
//@JsonTest
public class PatientTest {

    //TODO add Jackson Annotations and remove all the hard-coded json printing, including PatientRecordUtility

    @Test
    public void testGenomicFeatureEmptyFieldScrubbing() {
        Patient patient = TestData.getTestPatient();
        for (GenomicFeature gf : patient.getGenomicFeatures()) {
            Variant variant = gf.getVariant();
            assertThat(variant.getEmptyFieldsRemovedJson().indexOf("start"), equalTo(42));
            assertThat(variant.getEmptyFieldsRemovedJson().indexOf("end"), equalTo(-1));
            assertThat(variant.getEmptyFieldsRemovedJson().indexOf("referenceBases"), equalTo(-1));
            assertThat(variant.getEmptyFieldsRemovedJson().indexOf("alternateBases"), equalTo(-1));
        }
    }

    @Test
    public void testPhenotypeFeatureEmptyFieldScrubbing() {
        Patient patient = TestData.getTestPatient();
        for (PhenotypeFeature ff : patient.getFeatures()) {
            System.out.println(ff);
            System.out.println(ff.getEmptyFieldsRemovedJson());
            assertThat(ff.getEmptyFieldsRemovedJson().indexOf("ageOfOnset"), equalTo(-1));
            assertThat(ff.getEmptyFieldsRemovedJson().indexOf("id"), equalTo(2));
        }
    }

    @Test
    public void testGenotypeFeatureEmptyFieldScrubbing() {
        Patient patient = TestData.getTestPatient();
        for (GenomicFeature gf : patient.getGenomicFeatures()) {
            assertThat(gf.getEmptyFieldsRemovedJson().indexOf("zygosity"), equalTo(-1));
        }
    }

    /**
     * Test if empty fields get scrubbed out in complete Patient entity
     */
    @Test
    public void testPatientEmptyFieldScrubbing() {
        Patient patient = TestData.getTestPatient();
        assertThat(patient.getEmptyFieldsRemovedJson().indexOf("species"), equalTo(-1));
        assertThat(patient.getEmptyFieldsRemovedJson().indexOf("ageOfOnset"), equalTo(-1));
        assertThat(patient.getEmptyFieldsRemovedJson().indexOf("sex"), equalTo(-1));
        assertThat(patient.getEmptyFieldsRemovedJson().indexOf("inheritanceMode"), equalTo(-1));
        assertThat(patient.getEmptyFieldsRemovedJson().indexOf("alternateBases"), equalTo(-1));
        assertThat(patient.getEmptyFieldsRemovedJson().indexOf("referenceBases"), equalTo(-1));
        assertThat(patient.getEmptyFieldsRemovedJson().indexOf("name"), equalTo(109));
    }


//    @Autowired
//    private JacksonTester<Patient> json;

//    @Test
//    public void serialize() throws IOException {
//        Patient patient = Patient.builder()
//                .id("12345")
//                .test(true)
//                .label("Patient 1")
//                .features(Arrays.asList(new PhenotypeFeature("HP:000122", "true", null)))
//                .build();
//
//        JsonContent<Patient> write = this.json.write(patient);
//
//        assertThat(write, equalTo("expected.json"));
//    }
//
//    @Test
//    public void deserialize() throws IOException {
//        String content = "{\"fact\":\"Chuck Norris knows Victoria's secret.\"}";
//        Patient patient = Patient.builder()
//                .id("12345")
//                .test(true)
//                .label("Patient 1")
//                .features(Arrays.asList(new PhenotypeFeature("HP:000122", "true", null)))
//                .build();
//
//        assertThat(this.json.parse(content), equalTo(patient));
//    }
//
//    @Test
//    public void testJson() {
//        Patient patient = Patient.builder()
//                .id("12345")
//                .test(true)
//                .label("Patient 1")
//                .features(Arrays.asList(new PhenotypeFeature("HP:000122", "true", null)))
//                .build();
//        System.out.println(patient);
//    }

}