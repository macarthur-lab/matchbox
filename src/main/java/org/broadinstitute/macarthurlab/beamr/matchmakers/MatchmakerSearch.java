/**
 * Intiates a call to all match maker nodes recorded
 */
package org.broadinstitute.macarthurlab.beamr.matchmakers;

import java.util.List;

/**
 * @author harindra
 *
 */
public class MatchmakerSearch {
	private List<Node> matchmakers;
	
	
	/**
	 * Default constructor
	 */
	public MatchmakerSearch(){
	}
	
	
	/**
	 * Default constructor
	 */
	public MatchmakerSearch(List<Node> matchmakers){
		this.matchmakers=matchmakers;
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
