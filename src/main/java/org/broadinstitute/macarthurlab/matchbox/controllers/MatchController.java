/**
 * Controller for main match route.
 * This is defined via the MME specification
 */
package org.broadinstitute.macarthurlab.matchbox.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.broadinstitute.macarthurlab.matchbox.entities.MatchmakerResult;
import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.broadinstitute.macarthurlab.matchbox.search.PatientRecordUtility;
import org.broadinstitute.macarthurlab.matchbox.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author harindra
 *
 */

@RestController
@CrossOrigin(origins = "*")
public class MatchController {
	
	@Autowired
	private SearchService searcher;
	
	private final PatientRecordUtility patientUtility;
	private final String CONTENT_TYPE_HEADER="application/vnd.ga4gh.matchmaker.v1.0+json ";
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * Constructor populates search functionality
	 */
	public MatchController(){
        this.patientUtility = new PatientRecordUtility();
	}
	

	/**
	 * Controller for /match POST end-point. ONLY SEARCHES INSIDE LOCAL DATABASE
	 * 
	 * @param patient
	 *            A patient structure sent as JSON through the API
	 * @return A list of result patients found in the local database that match input
	 *         patient
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/match")
	public ResponseEntity<?> match(@RequestBody String requestString, HttpServletRequest request) {
		String originMatchmakerNodeName = request.getAttribute("originMatchmakerNodeName").toString();
		Patient queryPatient = null;
		
		try {
			String decodedRequestString = java.net.URLDecoder.decode(requestString, "UTF-8");
			// TODO figure out why there is a = at the end of JSON string
			String inputData=decodedRequestString;
			if ('=' == inputData.charAt(decodedRequestString.length() - 1)){
				inputData=decodedRequestString.substring(0, decodedRequestString.length() - 1);
			}
			boolean inputDataValid=this.getPatientUtility().areAllRequiredFieldsPresent(inputData);
			if (inputDataValid) {
				queryPatient = this.getPatientUtility().parsePatientInformation(inputData);
				StringBuilder msg = new StringBuilder();
				msg.append("matchmaker request from:");
				msg.append(queryPatient.getContact().toString());
				msg.append(", orignating from matchmaker node:");
				msg.append(originMatchmakerNodeName);
				this.getLogger().warn(msg.toString());
			} else {
				this.getLogger().warn("input data invalid:" + inputData.toString());
				return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			this.getLogger().error("error parsing patient in /match :" + e.toString() + " : " + e.getMessage());
		}
		String matches = this.getSearcher().searchInLocalDatabaseOnly(queryPatient,originMatchmakerNodeName).toString();
		StringBuilder resultsBuilder = new StringBuilder();
		resultsBuilder.append("{");
		resultsBuilder.append("\"results\":");
		resultsBuilder.append(matches);
		resultsBuilder.append(",");
		resultsBuilder.append("\"_disclaimer\":");
		resultsBuilder.append("\"" + this.getPatientUtility().getDisclaimer() + "\"");
		resultsBuilder.append("}");
		
		final HttpHeaders httpHeaders= new HttpHeaders();
	    httpHeaders.setContentType(MediaType.valueOf(this.CONTENT_TYPE_HEADER));
		return new ResponseEntity<>(resultsBuilder.toString(), httpHeaders,HttpStatus.OK);
	}
	
	
	
	
	/**
	 * Controller for individual/match POST end-point (as per Matchmaker spec)
	 * ONLY SEARCHES IN EXTERNAL NODES and NOT in local node
	 * @param patient	A patient structure sent as JSON through the API
	 * @return	A list of result patients found in the other MME nodes in the 
	 * 			network that match input patient
	 */
	@RequestMapping(method = RequestMethod.POST, value="/match/external")
    public ResponseEntity<?> individualMatch(@RequestBody String requestString) {
		final HttpHeaders httpHeaders= new HttpHeaders();
	    httpHeaders.setContentType(MediaType.valueOf(this.CONTENT_TYPE_HEADER));
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
			List<String> matchmakerResults = this.getSearcher().searchInExternalMatchmakerNodesOnly(patient);			
			StringBuilder resultsBuilder = new StringBuilder();
			resultsBuilder.append("{");
			resultsBuilder.append("\"results\":");
			resultsBuilder.append(matchmakerResults);
			resultsBuilder.append(",");
			resultsBuilder.append("\"_disclaimer\":");
			resultsBuilder.append("\"" + this.getPatientUtility().getDisclaimer() + "\"");
			resultsBuilder.append("}");
			return new ResponseEntity<>(resultsBuilder.toString(), httpHeaders, HttpStatus.OK);
		}
		catch(Exception e){
			e.printStackTrace();
			this.getLogger().error("error occurred in match controller :"+e.toString() + " : " + e.toString());
			return new ResponseEntity<>("{\"message\":\"error occurred searching external nodes\"}", httpHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
		}
    	
    }
	
	
	
    /**
	 * @return the searcher
	 */
	public SearchService getSearcher() {
		return this.searcher;
	}
	

	/**
	 * @param searcher the searcher to set
	 */
	public void setSearcher(SearchService searcher) {
		this.searcher = searcher;
	}


	/**
	 * @return the patientUtility
	 */
	public PatientRecordUtility getPatientUtility() {
		return patientUtility;
	}


	/**
	 * @return the logger
	 */
	public Logger getLogger() {
		return logger;
	}

	

}
