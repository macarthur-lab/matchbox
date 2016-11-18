/**
 * Controller for main match route.
 * This is defined via the MME specification
 */
package org.broadinstitute.macarthurlab.matchbox.controllers;

import org.broadinstitute.macarthurlab.matchbox.entities.MatchmakerResult;
import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.broadinstitute.macarthurlab.matchbox.matchmakers.PatientRecordUtility;
import org.broadinstitute.macarthurlab.matchbox.matchmakers.Search;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author harindra
 *
 */

@RestController
@CrossOrigin(origins = "*")
public class MatchController {

	private static final Logger logger = LoggerFactory.getLogger(MatchController.class);
	private static final String CONTENT_TYPE_HEADER = "application/vnd.ga4gh.matchmaker.v1.0+json";

	private final Search searcher;
	private final PatientRecordUtility patientUtility;

	/**
	 * Constructor populates search functionality
	 */
	@Autowired
	public MatchController(Search matchMakerSearch){
		this.searcher = matchMakerSearch;
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
	public ResponseEntity<?> match(@RequestBody String requestString, HttpServletRequest request) {
		logger.info("match request");
		Patient queryPatient = null;
		try {
			String decodedRequestString = java.net.URLDecoder.decode(requestString, "UTF-8");
			// TODO figure out why there is a = at the end of JSON string
			String inputData = decodedRequestString;
			if ('=' == inputData.charAt(decodedRequestString.length() - 1)) {
				inputData = decodedRequestString.substring(0, decodedRequestString.length() - 1);
			}
			boolean inputDataValid = patientUtility.areAllRequiredFieldsPresent(inputData);
			if (inputDataValid) {
				queryPatient = patientUtility.parsePatientInformation(inputData);
				logger.warn("matchmaker request from: {}", queryPatient.getContact());
			} else {
				logger.warn("input data invalid: {}", inputData);
				return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			logger.error("error parsing patient in /match", e);
		}
		String matches = searcher.searchInLocalDatabaseOnly(queryPatient, request.getRemoteHost()).toString();
		String results = "{" + "\"results\":" + matches + "}";
		final HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.valueOf(CONTENT_TYPE_HEADER));
		return new ResponseEntity<>(results, httpHeaders, HttpStatus.OK);
	}
	
	/**
	 * Controller for individual/match POST end-point (as per Matchmaker spec)
	 * ONLY SEARCHES IN EXTERNAL NODES and NOT in local node
	 * @param patient	A patient structure sent as JSON through the API
	 * @return	A list of result patients found in the network that match input patient
	 */
	@RequestMapping(method = RequestMethod.POST, value="/match/external")
    public ResponseEntity<?> individualMatch(@RequestBody String requestString) {
		logger.info("match request to external nodes");
		Map<String, List<MatchmakerResult>> results = new HashMap<String, List<MatchmakerResult>>();
		Patient patient = null;
		try {
			String decodedRequestString = java.net.URLDecoder.decode(requestString, "UTF-8");
			String inputData = decodedRequestString;
			//TODO figure out why there is a = at the end of JSON string when non-curled
			if ('=' == inputData.charAt(decodedRequestString.length() - 1)) {
				inputData = decodedRequestString.substring(0, decodedRequestString.length() - 1);
			}
			boolean inputDataValid = patientUtility.areAllRequiredFieldsPresent(inputData);
			if (inputDataValid) {
				patient = patientUtility.parsePatientInformation(inputData);
			} else {
				return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
			}
			List<MatchmakerResult> matchmakerResults = searcher.searchInExternalMatchmakerNodesOnly(patient);
			results.put("results", matchmakerResults);
		} catch (Exception e) {
			logger.error("error occurred in match controller:", e);
		}

		final HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.valueOf(CONTENT_TYPE_HEADER));
		return new ResponseEntity<>(results, httpHeaders, HttpStatus.OK);
	}

}
