/**
 * Controller for main match route.
 * This is defined via the MME specification
 */
package org.broadinstitute.macarthurlab.matchbox.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.broadinstitute.macarthurlab.matchbox.entities.MatchmakerResult;
import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.broadinstitute.macarthurlab.matchbox.matchmakers.MatchmakerSearch;
import org.broadinstitute.macarthurlab.matchbox.matchmakers.PatientRecordUtility;
import org.broadinstitute.macarthurlab.matchbox.matchmakers.Search;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.http.HttpStatus;
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
	
	/**
	 * Constructor populates search functionality
	 */
	public MatchController(){
        String configFile = "file:" + System.getProperty("user.dir") + "/config.xml";
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
		Map<String, List<MatchmakerResult>> results = new HashMap<String, List<MatchmakerResult>>();
		Patient patient = null;
		try {
			String decodedRequestString = java.net.URLDecoder.decode(requestString, "UTF-8");
			// TODO figure out why there is a = at the end of JSON string
			boolean inputDataValid=this.getPatientUtility().areAllRequiredFieldsPresent(decodedRequestString.substring(0, decodedRequestString.length() - 1));
			if (inputDataValid) {
				patient = this.getPatientUtility().parsePatientInformation(decodedRequestString.substring(0, decodedRequestString.length() - 1));
			} else {
				return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			System.out.println("error parsing patient in /match :" + e.toString());
		}
		// return results if no error
		results.put("results", this.getSearcher().searchInLocalDatabaseOnly(patient));
		return new ResponseEntity<>(results, HttpStatus.OK);
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
