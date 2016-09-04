/**
 * Handles the insertion of a patient in Beamr data model
 */
package org.broadinstitute.macarthurlab.matchbox.controllers;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import org.broadinstitute.macarthurlab.matchbox.datamodel.mongodb.PatientMongoRepository;
import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.broadinstitute.macarthurlab.matchbox.matchmakers.MatchmakerSearch;
import org.broadinstitute.macarthurlab.matchbox.matchmakers.PatientRecordUtility;
import org.broadinstitute.macarthurlab.matchbox.matchmakers.Search;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
@CrossOrigin(origins = "*")
public class PatientController {
	
	private final PatientRecordUtility patientUtility;
	@Autowired
	private PatientMongoRepository patientMongoRepository;
	private final Search searcher;
	
	/**
	 * Constructor populates search functionality
	 */
	public PatientController(){
        this.patientUtility = new PatientRecordUtility();
        String configFile = "file:" + System.getProperty("user.dir") + "/resources/config.xml";
        ApplicationContext context = new ClassPathXmlApplicationContext(configFile);
        this.searcher = context.getBean("matchmakerSearch", MatchmakerSearch.class);
	}
	
	
	/**
	 * Controller for /individual/add end-point. This adds a patient to the local MME database
	 * 
	 * @param patient
	 *            A patient structure sent as JSON through the API
	 * @return a success message or error message
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/patient/add")
	public ResponseEntity<String> add(@RequestBody String requestString) {
		String jsonMessage = "{\"message\":\"insertion OK\",\"status_code\":200}";
		try {
			String decodedRequestString = java.net.URLDecoder.decode(requestString, "UTF-8");
			Patient patient = this.getPatientUtility()
					.parsePatientInformation(decodedRequestString.substring(0, decodedRequestString.length() - 1));
			//if the patient doesn't exist already, add them
			if (null == this.patientMongoRepository.findOne(patient.getId())) {
				System.out.println(this.patientMongoRepository.insert(patient));
			} else {
				jsonMessage = "{\"message\":\"That patient record was not inserted, it (specifically that ID) already exists in Broad system\",\"status_code\":440}";
				return new ResponseEntity<String>(jsonMessage, HttpStatus.CONFLICT);
			}

		} catch (Exception e) {
			e.printStackTrace();
			jsonMessage = "{\"message\":\"unable to insert, an unknown error occurred.\",\"status_code\":442, \"error_message\":\""
					+ e.getMessage() + "\"}";
			return new ResponseEntity<String>(jsonMessage, HttpStatus.SERVICE_UNAVAILABLE);
		}
		return new ResponseEntity<String>(jsonMessage, HttpStatus.OK);
	}
	
	
	/**
	 * Controller for /individual/view end-point
	 * @return	a list of patients found in Beamr data model
	 */
	@RequestMapping(method = RequestMethod.GET, value="/patient/view")
    public List<Patient>  view() {
		List<Patient> patients = new ArrayList<Patient>();
		try{
			patients = this.patientMongoRepository.findAll();
		}catch(Exception e){
			e.printStackTrace();
		}
        return patients;
    }
	

	/**
	 * Controller for individual/delete POST end-point. Expects a data load that looks like,
	 * {"id":"id_to_delete"}
	 * @param patientId	A patient ID to delete
	 */
	@RequestMapping(method = RequestMethod.DELETE, value="/patient/delete")
    public ResponseEntity<String> delete(@RequestBody String requestString) {
		String jsonMessage="";
		try{
			String decodedRequestString = java.net.URLDecoder.decode(requestString, "UTF-8");
			Map<String,String> delInfo = this.patientUtility.parsePatientIdFromDeleteCall(decodedRequestString);
			Long numDeleted=this.patientMongoRepository.deletePatientById(delInfo.get("id"));
			if (numDeleted==1){
				jsonMessage = "{\"message\":\"deleted " + Long.toString(numDeleted) + " patient.\",\"status_code\":200\"}";
			}
			if (numDeleted==0){
				jsonMessage = "{\"message\":\"no patients were deleted, are you sure that ID was valid?\",\"status_code\":400\"}";
				return new ResponseEntity<String>(jsonMessage, HttpStatus.BAD_REQUEST);
			}
		}
		catch(Exception e){
			jsonMessage = "{\"message\":\"unable to delete, an unknown error occurred.\",\"status_code\":442, \"error_message\":\""
					+ e.getMessage() + "\"}";
			System.out.println("error occurred in match controller DELETE endpoint:"+e);
			return new ResponseEntity<String>(jsonMessage, HttpStatus.MULTI_STATUS);
		}
		return new ResponseEntity<String>(jsonMessage, HttpStatus.OK);
    }
	
	
	/**
	 * @return the patientUtility
	 */
	public PatientRecordUtility getPatientUtility() {
		return patientUtility;
	}


    /**
	 * @return the searcher
	 */
	public Search getSearcher() {
		return this.searcher;
	}

	
	

}
