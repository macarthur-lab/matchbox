/**
 * Controller for main match route
 */
package org.broadinstitute.macarthurlab.beamr.controllers;

import java.util.HashMap;
import java.util.Map;

import org.broadinstitute.macarthurlab.beamr.entities.MatchmakerResult;
import org.broadinstitute.macarthurlab.beamr.matchmakers.MatchmakerSearch;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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
	


	@RequestMapping(method = RequestMethod.POST, value="/match")
    public Map<String,MatchmakerResult> greeting(@RequestParam(value="name", defaultValue="World") String name) {
    	Map<String,MatchmakerResult> results = new HashMap<String,MatchmakerResult>();
    	results.put("results", new MatchmakerResult());
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
