/**
 * Handles the insertion of a patient in Beamr data model
 */
package org.broadinstitute.macarthurlab.beamr.controllers;


import org.broadinstitute.macarthurlab.beamr.datamodel.mongodb.PatientMongoRepository;
import org.broadinstitute.macarthurlab.beamr.entities.Patient;
import org.broadinstitute.macarthurlab.beamr.matchmakers.PatientRecordUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
@CrossOrigin(origins = "*")
public class BeamrAddController {
	
	private final PatientRecordUtility patientUtility;
	@Autowired
	private PatientMongoRepository patientMongoRepository;
	
	/**
	 * Constructor populates search functionality
	 */
	public BeamrAddController(){
        this.patientUtility = new PatientRecordUtility();
	}
	
	
	/**
	 * Controller for /beamr_add PUT end-point
	 * @param patient	A patient structure sent as JSON through the API
	 * @return	A list of result patients found in the network that match input patient
	 */
	@RequestMapping(method = RequestMethod.POST, value="/add")
    public ResponseEntity<String>  match(@RequestBody String requestString) {
		try{
		String decodedRequestString = java.net.URLDecoder.decode(requestString, "UTF-8");
		Patient patient = this.getPatientUtility().parsePatientInformation(decodedRequestString.substring(0,decodedRequestString.length()-1));
		this.patientMongoRepository.save(patient);
		}catch(Exception e){
			e.printStackTrace();
		}
        return new ResponseEntity<String>("{\"message\":\"insertion OK\"}",HttpStatus.OK);
    }

	
	
	/**
	 * @return the patientUtility
	 */
	public PatientRecordUtility getPatientUtility() {
		return patientUtility;
	}


	
	

}
