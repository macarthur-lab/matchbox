/**
 * Intiates a call to all match maker nodes recorded
 */
package org.broadinstitute.macarthurlab.beamr.matchmakers;

import java.util.ArrayList;
import java.util.List;

import org.broadinstitute.macarthurlab.beamr.entities.MatchmakerResult;

/**
 * @author harindra
 *
 */
public class MatchmakerSearch {
	/**
	 * A list of MatchmakeNode objs that would be all
	 * available nodes in system to look for. 
	 * 
	 * This would be populated via Spring XML file and Spring IoC
	 */
	private List<Node> matchmakers;

	
	
	/**
	 * Default constructor
	 */
	public MatchmakerSearch(){
	}
	
	
	/**
	 * Search in this matchmaker node
	 * @param	A matchmaker node
	 */
	public List<MatchmakerResult> Search(Node matchmakerNode){
		String callResult = matchmakerNode.getUrl();
		
		return new ArrayList<MatchmakerResult>();
	}
	
	
	/**
	 * Call this URL and fetch result
	 * @param url	A URL to call
	 */
	private String callUrl(String url){
		return "";
	}
	
	
	


	/**
	 * @return the matchmakers
	 */
	public List<Node> getMatchmakers() {
		return matchmakers;
	}


	/**
	 * @param matchmakers the matchmakers to set
	 */
	public void setMatchmakers(List<Node> matchmakers) {
		this.matchmakers = matchmakers;
	}
	
	
}
