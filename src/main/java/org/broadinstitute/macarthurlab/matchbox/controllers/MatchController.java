/**
 * Controller for main match route.
 * This is defined via the MME specification
 */
package org.broadinstitute.macarthurlab.matchbox.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.broadinstitute.macarthurlab.matchbox.entities.MatchmakerResult;
import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.broadinstitute.macarthurlab.matchbox.matchmakers.MatchmakerSearch;
import org.broadinstitute.macarthurlab.matchbox.matchmakers.PatientRecordUtility;
import org.broadinstitute.macarthurlab.matchbox.matchmakers.Search;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author harindra
 *
 */

@RestController
@CrossOrigin(origins = "*")
public class MatchController {
	private final Search searcher;
	private final PatientRecordUtility patientUtility;
	private final String CONTENT_TYPE_HEADER="application/vnd.ga4gh.matchmaker.v1.0+json ";
	
	/**
	 * Constructor populates search functionality
	 */
	public MatchController(){
        String configFile = "file:" + System.getProperty("user.dir") + "/resources/config.xml";
        ApplicationContext context = new ClassPathXmlApplicationContext(configFile);
        this.searcher = context.getBean("matchmakerSearch", MatchmakerSearch.class);
        this.patientUtility = new PatientRecordUtility();
	}
	

	/**
	 * Controller for /match POST end-point.ONLY SEARCHES INSIDE LOCAL DATABASE
	 * 
	 * @param patient
	 *            A patient structure sent as JSON through the API
	 * @return A list of result patients found in the network that match input
	 *         patient
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/match")
	public ResponseEntity<?> match(@RequestBody String requestString) {
		//Map<String, List<String>> results = new HashMap<String, List<String>>();
		Patient patient = null;
		try {
			String decodedRequestString = java.net.URLDecoder.decode(requestString, "UTF-8");
			// TODO figure out why there is a = at the end of JSON string
			String inputData=decodedRequestString;
			if ('=' == inputData.charAt(decodedRequestString.length() - 1)){
				inputData=decodedRequestString.substring(0, decodedRequestString.length() - 1);
			}
			boolean inputDataValid=this.getPatientUtility().areAllRequiredFieldsPresent(inputData);
			if (inputDataValid) {
				patient = this.getPatientUtility().parsePatientInformation(inputData);
				StringBuilder msg = new StringBuilder();
				msg.append("matchmaker request from:");
				msg.append(patient.getContact().toString());
				System.out.println(msg.toString());
			} else {
				System.out.println("input data invalid:");
				System.out.println(inputData);
				return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("error parsing patient in /match :" + e.toString());
		}
		// return results if no error
		//results.put("results", this.getSearcher().searchInLocalDatabaseOnly(patient));
		
		String results = "{" + "\"results\":" + this.getSearcher().searchInLocalDatabaseOnly(patient).toString() + "}";
		
		final HttpHeaders httpHeaders= new HttpHeaders();
	    httpHeaders.setContentType(MediaType.valueOf(this.CONTENT_TYPE_HEADER));
		return new ResponseEntity<>(results, httpHeaders,HttpStatus.OK);
	}
	
	
	
	
	/**
	 * Controller for individual/match POST end-point (as per Matchmaker spec)
	 * ONLY SEARCHES IN EXTERNAL NODES and NOT in local node
	 * @param patient	A patient structure sent as JSON through the API
	 * @return	A list of result patients found in the network that match input patient
	 */
	@RequestMapping(method = RequestMethod.POST, value="/match/external")
    public ResponseEntity<?> individualMatch(@RequestBody String requestString) {
		Map<String,List<MatchmakerResult>> results = new HashMap<String,List<MatchmakerResult>>();
		Patient patient=null;
		try{
			String decodedRequestString = java.net.URLDecoder.decode(requestString, "UTF-8");
			String inputData=decodedRequestString;
			//TODO figure out why there is a = at the end of JSON string when non-curled
			if ('=' == inputData.charAt(decodedRequestString.length() - 1)){
				inputData=decodedRequestString.substring(0, decodedRequestString.length() - 1);
			}
			boolean inputDataValid=this.getPatientUtility().areAllRequiredFieldsPresent(inputData);
			if (inputDataValid) {
				patient = this.getPatientUtility().parsePatientInformation(inputData);
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
		
		final HttpHeaders httpHeaders= new HttpHeaders();
	    httpHeaders.setContentType(MediaType.valueOf(this.CONTENT_TYPE_HEADER));
    	return new ResponseEntity<>(results, httpHeaders, HttpStatus.OK);
    }
	
	
	
    /**
	 * @return the searcher
	 */
	public Search getSearcher() {
		return this.searcher;
	}


	/**
	 * @return the patientUtility
	 */
	public PatientRecordUtility getPatientUtility() {
		return patientUtility;
	}


	

}
