/**
 * Controller for main match route
 */
package org.broadinstitute.macarthurlab.beamr.controllers;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.broadinstitute.macarthurlab.beamr.entities.MatchmakerResult;
import org.broadinstitute.macarthurlab.beamr.entities.Patient;
import org.broadinstitute.macarthurlab.beamr.matchmakers.MatchmakerSearch;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ModelAttribute;
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
	private MatchmakerSearch searcher;
	
	/**
	 * Constructor populates search functionality
	 */
	public MatchController(){
        String configFile = "file:" + System.getProperty("user.dir") + "/config.xml";
        ApplicationContext context = new ClassPathXmlApplicationContext(configFile);
        this.searcher = context.getBean("matchmakerSearch", MatchmakerSearch.class);
	}
	

	/**
	 * Controller for /match POST end-point
	 * @param patient	A patient structure sent as JSON through the API
	 * @return	A list of result patients found in the network that match input patient
	 */
	@RequestMapping(method = RequestMethod.POST, value="/match")
    public Map<String,MatchmakerResult> match(@RequestBody String requestString) {
		Map<String,MatchmakerResult> results = new HashMap<String,MatchmakerResult>();
		try{
			String decodedRequestString = java.net.URLDecoder.decode(requestString, "UTF-8");
			Patient patient = interpretRequestBody(decodedRequestString);
			this.getSearcher().Search(new Patient());
			results.put("results", new MatchmakerResult());
		}
		catch(Exception e){
			System.out.println("error occurred in match controller:"+e.toString());
		}
    	return results;
    }
	
	
	
	private Patient interpretRequestBody(String requestString){
		JSONParser parser = new JSONParser();
		try{
			//#TODO figure out why there is a = at the end of JSON string
			JSONObject jsonObject = (JSONObject) parser.parse(requestString.substring(0,requestString.length()-1));
			JSONObject patient = (JSONObject)jsonObject.get("patient");
			System.out.println(patient.get("disorders"));
		}
		catch(Exception e){
			System.out.println(e);
		}
		
		return new Patient();
	}
	
    /**
	 * @return the searcher
	 */
	public MatchmakerSearch getSearcher() {
		return searcher;
	}

	/**
	 * @param searcher the searcher to set
	 */
	public void setSearcher(MatchmakerSearch searcher) {
		this.searcher = searcher;
	}

}
