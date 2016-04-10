/**
 * Intiates a call to all match maker nodes recorded
 */
package org.broadinstitute.macarthurlab.beamr.matchmakers;

import java.util.ArrayList;
import java.util.List;

import org.broadinstitute.macarthurlab.beamr.entities.MatchmakerResult;
import org.broadinstitute.macarthurlab.beamr.entities.Patient;

/**
 * @author harindra
 *
 */
public class MatchmakerSearch {
	/**
	 * A list of MatchmakeNode objs that would be all
	 * available nodes in system to look for. 
	 * 
	 * This is populated via config.xml file via Spring IoC
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
	public List<MatchmakerResult> Search(Patient patient){
		List<MatchmakerResult> allResults = new ArrayList<MatchmakerResult>();
		for (Node n:this.getMatchmakers()){
			allResults.addAll(this.searchNode(n, patient));
		}
		return allResults;
	}
	

	/**
	 * Searches in this node for this patient
	 * @param matchmakerNode	A matchmaker node/center
	 * @param patient	A patient
	 * @return	The results found for this patient
	 */
	private List<MatchmakerResult> searchNode(Node matchmakerNode, Patient patient){
		System.out.println(this.callUrl(""));
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
