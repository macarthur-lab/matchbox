package org.broadinstitute.macarthurlab.matchbox.search;

import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.junit.Test;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class PatientRecordUtilityTest {


    /**
     * Test parsing patient structure
     */
    @Test
    public void testPatientStructureParsing(){
        String patientString="{\"patient\":{\"id\":\"1\", \"contact\": {\"name\":\"Jane Doe\", \"href\":\"mailto:jdoe@example.edu\"},\"features\":[{\"id\":\"HP:0000522\"}],\"genomicFeatures\":[{\"gene\":{\"id\":\"NGLY1\"}}]}}";
        Patient patient = new PatientRecordUtility().parsePatientInformation(patientString);
        //TODO: use Jackson and use (UN)WRAP_ROOT_ELEMENT
//        Use SerializationConfig.Feature.WRAP_ROOT_ELEMENT and DeserializationConfig.Feature.UNWRAP_ROOT_ELEMENT. Change the wrapper name with `@JsonRootName' annotation
        System.out.println(patient.getEmptyFieldsRemovedJson());
    }
}