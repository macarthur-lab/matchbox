/**
 * Handles the insertion of a patient in Beamr data model
 */
package org.broadinstitute.macarthurlab.beamr.controllers;


import java.util.ArrayList;
import java.util.List;

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
public class IndividualController {
	
	private final PatientRecordUtility patientUtility;
	@Autowired
	private PatientMongoRepository patientMongoRepository;
	
	/**
	 * Constructor populates search functionality
	 */
	public IndividualController(){
        this.patientUtility = new PatientRecordUtility();
	}
	
	
	/**
	 * Controller for /individual/add end-point
	 * @param patient	A patient structure sent as JSON through the API
	 * @return	a success message
	 */
	@RequestMapping(method = RequestMethod.POST, value="/individual/add")
    public ResponseEntity<String>  individualMatch(@RequestBody String requestString) {
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
	 * Controller for /individual/view end-point
	 * @return	a list of patients found in Beamr data model
	 */
	@RequestMapping(method = RequestMethod.GET, value="/individual/view")
    public List<Patient>  IndividualView() {
		List<Patient> patients = new ArrayList<Patient>();
		try{
			patients = this.patientMongoRepository.findAll();
		}catch(Exception e){
			e.printStackTrace();
		}
        return patients;
    }

	
	
	/**
	 * @return the patientUtility
	 */
	public PatientRecordUtility getPatientUtility() {
		return patientUtility;
	}


	
	

}
