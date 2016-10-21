/**
 * Handles the insertion of a patient in Beamr data model
 */
package org.broadinstitute.macarthurlab.matchbox.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	
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
	 * Controller for /patient/add end-point. This adds a patient to the local MME database
	 * 
	 * @param patient
	 *            A patient structure sent as JSON through the API
	 * @return a success message or error message
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/patient/add")
	public ResponseEntity<String> add(@RequestBody String requestString) {
		String jsonMessage = "{\"message\":\"insertion OK\",\"status_code\":200}";
		Patient patient = null;
		try {
			String decodedRequestString = java.net.URLDecoder.decode(requestString, "UTF-8");
			String inputData=decodedRequestString;
			if ('=' == inputData.charAt(decodedRequestString.length() - 1)){
				inputData=decodedRequestString.substring(0, decodedRequestString.length() - 1);
			}
			boolean inputDataValid=this.getPatientUtility().areAllRequiredFieldsPresent(inputData);
			if (!inputDataValid) {
				StringBuilder msg = new StringBuilder();
				msg.append("add to MME request has invalid JSON data:");
				msg.append(decodedRequestString);
				System.out.println(msg.toString());
				return new ResponseEntity<String>(msg.toString(),HttpStatus.BAD_REQUEST);
			}
			//if the patient doesn't exist already, add them
			patient = this.getPatientUtility().parsePatientInformation(inputData);
			Patient recordInDb = this.patientMongoRepository.findOne(patient.getId());
			if (null == recordInDb) {
				this.patientMongoRepository.insert(patient);
				this.getLogger().info("inserting new patient for the first time: " + patient.toString());
			} else {
				//let's delete the existing record and add in the new one.
				//#TODO add an audit here for external users who don't use seqr to audit this
				this.patientMongoRepository.delete(recordInDb);
				this.patientMongoRepository.insert(patient);
				this.getLogger().info("deleting existing patient record and inserting new patient record: " + patient.toString());
				jsonMessage = "{\"message\":\"That patient record (specifically that ID) had already been submitted in the past, it  already exists in Broad system. We are deleting that record and updating it with this new submission\",\"status_code\":200}";
				return new ResponseEntity<String>(jsonMessage, HttpStatus.CONFLICT);
			}
		} catch (Exception e) {
			this.getLogger().error(e.getMessage());
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
			this.getLogger().error(e.getMessage());
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
			this.getLogger().error(e.getMessage());
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

	
	/**
	 * @return the logger
	 */
	public Logger getLogger() {
		return logger;
	}

}
