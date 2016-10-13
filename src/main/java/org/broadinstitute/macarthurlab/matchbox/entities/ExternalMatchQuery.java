/**
 * To persist a query from an external center and the results that were sent back to them.
 * Objective: to help gather metrics on performance as well as function as a record of all 
 * data communicated
 */
package org.broadinstitute.macarthurlab.matchbox.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author harindra
 */
public class ExternalMatchQuery {
	private final Date timeStamp;
	private final String requestOriginHostname;
	private final String institution;
	private final Patient incomingQuery;
	private final boolean matchFound;
	private final List<MatchmakerResult> results;

	/**
	 * Constructor for testing
	 */
	public ExternalMatchQuery() {
		this.requestOriginHostname="";
		this.incomingQuery=new Patient();
		this.results= new ArrayList<MatchmakerResult>();
		this.timeStamp = new Date();
		this.institution="";
		this.matchFound=false;
	}
	
	/**
	 * Constructor
	 */
	public ExternalMatchQuery(Patient queryPatient, List<MatchmakerResult> results, String requestOriginHostname, String institution, boolean matchFound) {
		this.incomingQuery=queryPatient;
		this.results= results;
		this.timeStamp = new Date();
		this.requestOriginHostname = requestOriginHostname;
		this.institution=institution;
		this.matchFound=matchFound;
	}

	/**
	 * @return the timeStamp
	 */
	public Date getTimeStamp() {
		return timeStamp;
	}

	/**
	 * @return the incomingQuery
	 */
	public Patient getIncomingQuery() {
		return incomingQuery;
	}

	/**
	 * @return the results
	 */
	public List<MatchmakerResult> getResults() {
		return results;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ExternalMatchQuery [timeStamp=" + timeStamp + ", incomingQuery=" + incomingQuery + ", results="
				+ results + "]";
	}

	/**
	 * @return the requestOriginHostname
	 */
	public String getRequestOriginHostname() {
		return requestOriginHostname;
	}

	/**
	 * @return the institution
	 */
	public String getInstitution() {
		return institution;
	}

	/**
	 * @return the matchFound
	 */
	public boolean isMatchFound() {
		return matchFound;
	}
	
	
	
	

}
