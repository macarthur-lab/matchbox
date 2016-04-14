/**
 * Controller for main match route
 */
package org.broadinstitute.macarthurlab.beamr.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.broadinstitute.macarthurlab.beamr.entities.MatchmakerResult;
import org.broadinstitute.macarthurlab.beamr.entities.Patient;
import org.broadinstitute.macarthurlab.beamr.matchmakers.MatchmakerSearch;
import org.broadinstitute.macarthurlab.beamr.matchmakers.PatientRecordUtility;
import org.broadinstitute.macarthurlab.beamr.matchmakers.Search;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
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
	 * Controller for /match POST end-point
	 * @param patient	A patient structure sent as JSON through the API
	 * @return	A list of result patients found in the network that match input patient
	 */
	@RequestMapping(method = RequestMethod.POST, value="/match")
    public Map<String,List<MatchmakerResult>> match(@RequestBody String requestString) {
		Map<String,List<MatchmakerResult>> results = new HashMap<String,List<MatchmakerResult>>();
		try{
			String decodedRequestString = java.net.URLDecoder.decode(requestString, "UTF-8");
			//TODO figure out why there is a = at the end of JSON string
			Patient patient = this.getPatientUtility().parsePatientInformation(decodedRequestString.substring(0,decodedRequestString.length()-1));
			List<MatchmakerResult> matches=new ArrayList<MatchmakerResult>();
			results.put("results",this.getSearcher().searchInLocalDatabaseOnly(patient));
		}
		catch(Exception e){
			System.out.println("error occurred in match controller:"+e.toString());
			e.printStackTrace();
		}
    	return results;
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
