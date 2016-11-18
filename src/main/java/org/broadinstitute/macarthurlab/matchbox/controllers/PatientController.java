/**
 * Handles the insertion of a patient in Beamr data model
 */
package org.broadinstitute.macarthurlab.matchbox.controllers;

import org.broadinstitute.macarthurlab.matchbox.datamodel.mongodb.PatientMongoRepository;
import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.broadinstitute.macarthurlab.matchbox.matchmakers.PatientRecordUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;


@RestController
@CrossOrigin(origins = "*")
public class PatientController {

	private static final Logger logger = LoggerFactory.getLogger(PatientController.class);

	private final PatientMongoRepository patientMongoRepository;
	private final PatientRecordUtility patientUtility;

	/**
	 * Constructor populates search functionality
	 */
	@Autowired
	public PatientController(PatientMongoRepository patientMongoRepository){
		this.patientMongoRepository = patientMongoRepository;
        this.patientUtility = new PatientRecordUtility();
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
		logger.info("patient/add {}", requestString);
		String jsonMessage = "{\"message\":\"insertion OK\",\"status_code\":200}";
		try {
			String decodedRequestString = java.net.URLDecoder.decode(requestString, "UTF-8");
			String inputData = decodedRequestString;
			if ('=' == inputData.charAt(decodedRequestString.length() - 1)) {
				inputData = decodedRequestString.substring(0, decodedRequestString.length() - 1);
			}
			boolean inputDataValid = patientUtility.areAllRequiredFieldsPresent(inputData);
			if (!inputDataValid) {
				StringBuilder msg = new StringBuilder();
				msg.append("patient/add request has invalid JSON data:");
				msg.append(decodedRequestString);
				logger.warn("{}", msg);
				return new ResponseEntity<>(msg.toString(), HttpStatus.BAD_REQUEST);
			}
			//if the patient doesn't exist already, add them
			Patient patient = patientUtility.parsePatientInformation(inputData);
			Patient recordInDb = patientMongoRepository.findOne(patient.getId());
			if (null == recordInDb) {
				patientMongoRepository.insert(patient);
				logger.info("inserting new patient: {}", patient);
			} else {
				//let's delete the existing record and add in the new one.
				//#TODO add an audit here for external users who don't use seqr to audit this
				patientMongoRepository.delete(recordInDb);
				patientMongoRepository.insert(patient);
				logger.info("deleting existing patient record and inserting new patient record: {}", patient);
				jsonMessage = "{\"message\":\"That patient record (specifically that ID) had already been submitted in the past, it  already exists in Broad system. We are deleting that record and updating it with this new submission\",\"status_code\":200}";
				return new ResponseEntity<>(jsonMessage, HttpStatus.CONFLICT);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			jsonMessage = "{\"message\":\"unable to insert, an unknown error occurred.\",\"status_code\":442, \"error_message\":\""
					+ e.getMessage() + "\"}";
			return new ResponseEntity<>(jsonMessage, HttpStatus.SERVICE_UNAVAILABLE);
		}
		return new ResponseEntity<>(jsonMessage, HttpStatus.OK);
	}
	
	
	/**
	 * Controller for /individual/view end-point
	 * @return	a list of patients found in Beamr data model
	 */
	@RequestMapping(method = RequestMethod.GET, value="/patient/view")
    public List<Patient> view() {
		try{
			return patientMongoRepository.findAll();
		}catch(Exception e){
			logger.error(e.getMessage());
		}
        return Collections.emptyList();
    }
	

	/**
	 * Controller for individual/delete POST end-point. Expects a data load that looks like,
	 * {"id":"id_to_delete"}
	 * @param patientId	A patient ID to delete
	 */
	@RequestMapping(method = RequestMethod.DELETE, value="/patient/delete")
    public ResponseEntity<String> delete(@RequestBody String requestString) {
		String jsonMessage = "";
		try {
			String decodedRequestString = java.net.URLDecoder.decode(requestString, "UTF-8");
			Map<String, String> delInfo = patientUtility.parsePatientIdFromDeleteCall(decodedRequestString);
			Long numDeleted = patientMongoRepository.deletePatientById(delInfo.get("id"));
			if (numDeleted == 1) {
				jsonMessage = "{\"message\":\"deleted " + Long.toString(numDeleted) + " patient.\",\"status_code\":200\"}";
			}
			if (numDeleted == 0) {
				jsonMessage = "{\"message\":\"no patients were deleted, are you sure that ID was valid?\",\"status_code\":400\"}";
				return new ResponseEntity<>(jsonMessage, HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			jsonMessage = "{\"message\":\"unable to delete, an unknown error occurred.\",\"status_code\":442, \"error_message\":\""
					+ e.getMessage() + "\"}";
			logger.error(e.getMessage());
			return new ResponseEntity<>(jsonMessage, HttpStatus.MULTI_STATUS);
		}
		return new ResponseEntity<>(jsonMessage, HttpStatus.OK);
	}

}
