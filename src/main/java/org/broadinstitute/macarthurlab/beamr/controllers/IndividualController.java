/**
 * Handles the insertion of a patient in Beamr data model
 */
package org.broadinstitute.macarthurlab.beamr.controllers;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.broadinstitute.macarthurlab.beamr.datamodel.mongodb.PatientMongoRepository;
import org.broadinstitute.macarthurlab.beamr.entities.MatchmakerResult;
import org.broadinstitute.macarthurlab.beamr.entities.Patient;
import org.broadinstitute.macarthurlab.beamr.matchmakers.MatchmakerSearch;
import org.broadinstitute.macarthurlab.beamr.matchmakers.PatientRecordUtility;
import org.broadinstitute.macarthurlab.beamr.matchmakers.Search;
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
	 * Controller for /individual/add end-point
	 * @param patient	A patient structure sent as JSON through the API
	 * @return	a success message
	 */
	@RequestMapping(method = RequestMethod.POST, value="/individual/add")
    public ResponseEntity<String>  individualAdd(@RequestBody String requestString) {
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
	 * Controller for individual/match POST end-point (as per Matchmaker spec)
	 * @param patient	A patient structure sent as JSON through the API
	 * @return	A list of result patients found in the network that match input patient
	 */
	@RequestMapping(method = RequestMethod.POST, value="/individual/match")
    public Map<String,MatchmakerResult> individualMatch(@RequestBody String requestString,final HttpServletResponse res) {
		Map<String,MatchmakerResult> results = new HashMap<String,MatchmakerResult>();
		HttpServletResponse response = (HttpServletResponse)res;
		Patient patient=null;
		try{
			String decodedRequestString = java.net.URLDecoder.decode(requestString, "UTF-8");
			//TODO figure out why there is a = at the end of JSON string
			try{
				patient = this.getPatientUtility().parsePatientInformation(decodedRequestString.substring(0,decodedRequestString.length()-1));
			}
			catch (Exception e){
				response.sendError(401);
			}
			this.getSearcher().searchInExternalMatchmakerNodesOnly(patient);
			results.put("results", new MatchmakerResult());
		}
		catch(Exception e){
			System.out.println("error occurred in match controller:"+e.toString());
			e.printStackTrace();
		}
    	return results;
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
