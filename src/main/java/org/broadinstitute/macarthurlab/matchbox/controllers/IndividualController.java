/**
 * Handles the insertion of a patient in Beamr data model
 */
package org.broadinstitute.macarthurlab.matchbox.controllers;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.broadinstitute.macarthurlab.matchbox.datamodel.mongodb.PatientMongoRepository;
import org.broadinstitute.macarthurlab.matchbox.entities.MatchmakerResult;
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
public class IndividualController {
	
	private final PatientRecordUtility patientUtility;
	@Autowired
	private PatientMongoRepository patientMongoRepository;
	private final Search searcher;
	
	/**
	 * Constructor populates search functionality
	 */
	public IndividualController(){
        this.patientUtility = new PatientRecordUtility();
        String configFile = "file:" + System.getProperty("user.dir") + "/config.xml";
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
	@RequestMapping(method = RequestMethod.POST, value = "/individual/add")
	public ResponseEntity<String> individualAdd(@RequestBody String requestString) {
		String jsonMessage = "{\"message\":\"insertion OK\",\"status_code\":200}";
		try {
			String decodedRequestString = java.net.URLDecoder.decode(requestString, "UTF-8");
			Patient patient = this.getPatientUtility()
					.parsePatientInformation(decodedRequestString.substring(0, decodedRequestString.length() - 1));
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
	 * Controller for individual/match POST end-point (as per Matchmaker spec)
	 * ONLY SEARCHES IN EXTERNAL NODES and NOT in local node
	 * @param patient	A patient structure sent as JSON through the API
	 * @return	A list of result patients found in the network that match input patient
	 */
	@RequestMapping(method = RequestMethod.POST, value="/individual/match")
    public ResponseEntity<?> individualMatch(@RequestBody String requestString) {
		Map<String,List<MatchmakerResult>> results = new HashMap<String,List<MatchmakerResult>>();
		Patient patient=null;
		try{
			String decodedRequestString = java.net.URLDecoder.decode(requestString, "UTF-8");
			boolean inputDataValid=this.getPatientUtility().areAllRequiredFieldsPresent(decodedRequestString.substring(0, decodedRequestString.length() - 1));
			if (inputDataValid) {
				//TODO figure out why there is a = at the end of JSON string
				patient = this.getPatientUtility().parsePatientInformation(decodedRequestString.substring(0,decodedRequestString.length()-1));
			}
			else{
				return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
			}
			List<MatchmakerResult> matchmakerResults = this.getSearcher().searchInExternalMatchmakerNodesOnly(patient);
			results.put("results", matchmakerResults);
		}
		catch(Exception e){
			System.out.println("error occurred in match controller:"+e.toString());
			e.printStackTrace();
		}
    	return new ResponseEntity<>(results, HttpStatus.OK);
    }
	

	/**
	 * Controller for individual/delete POST end-point 
	 * @param patientId	A patient ID to delete
	 * @return	True/False on success
	 */
	@RequestMapping(method = RequestMethod.POST, value="/individual/delete")
    public Map<String,String> individualDelete(@RequestBody String requestString) {
		Map<String,String> results = new HashMap<String,String>();
		try{
			throw new Exception("not implemented yet");
		}
		catch(Exception e){
			System.out.println("error occurred in match controller:"+e.toString());
			e.printStackTrace();
		}
    	return results;
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
