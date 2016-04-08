/**
 * Controller for main match route
 */
package org.broadinstitute.macarthurlab.beamr.controllers;

import java.util.HashMap;
import java.util.Map;

import org.broadinstitute.macarthurlab.beamr.entities.MatchmakerResult;
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
    @RequestMapping(method = RequestMethod.POST, value="/match")
    public Map<String,MatchmakerResult> greeting(@RequestParam(value="name", defaultValue="World") String name) {
    	Map<String,MatchmakerResult> results = new HashMap<String,MatchmakerResult>();
    	results.put("results", new MatchmakerResult());
    	return results;
    }

}
