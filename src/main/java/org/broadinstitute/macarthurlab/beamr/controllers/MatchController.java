/**
 * Controller for main match route
 */
package org.broadinstitute.macarthurlab.beamr.controllers;

import java.util.HashMap;
import java.util.Map;

import org.broadinstitute.macarthurlab.beamr.entities.MatchmakerResult;
import org.broadinstitute.macarthurlab.beamr.entities.Patient;
import org.broadinstitute.macarthurlab.beamr.matchmakers.MatchmakerSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ModelAttribute;
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
	
	/**
	 * This gets injected via Spring XML using IoC
	 */
	private MatchmakerSearch searcher;
	
	/**
	 * Constructor
	 */
	public MatchController(){
        String configFile = "file:" + System.getProperty("user.dir") + "/config.xml";
        ApplicationContext context = new ClassPathXmlApplicationContext(configFile);
        this.searcher = context.getBean("matchmakerSearch", MatchmakerSearch.class);
	}
	

	@RequestMapping(method = RequestMethod.POST, value="/match")
    public Map<String,MatchmakerResult> match(@ModelAttribute Patient patient) {
		Map<String,MatchmakerResult> results = new HashMap<String,MatchmakerResult>();
		try{
			this.getSearcher().Search(new Patient());
			results.put("results", new MatchmakerResult());
		}
		catch(Exception e){
			throw e;
		}
    	return results;
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
