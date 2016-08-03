/**
 * To handle Https communication
 */
package org.broadinstitute.macarthurlab.matchbox.network;

import java.util.ArrayList;
import java.util.List;

import org.broadinstitute.macarthurlab.matchbox.entities.MatchmakerResult;
import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.broadinstitute.macarthurlab.matchbox.entities.Node;
import org.broadinstitute.macarthurlab.matchbox.matchmakers.PatientRecordUtility;

/**
 * @author harindra
 *
 */
public class HttpsCommunication {
	
	/**
	 * A set of tools to parse and store patient information
	 */
	private final PatientRecordUtility patientUtility;

	/**
	 * Default constructor
	 */
	public HttpsCommunication(){
		this.patientUtility = new PatientRecordUtility();
	}
	
	public List<MatchmakerResult> callNodeWithHttp(Node matchmakerNode, Patient queryPatient) {
		List<MatchmakerResult> allResults = new ArrayList<MatchmakerResult>();
		return allResults;
	}
	
	
	
	/**
	 * @return the patientUtility
	 */
	public PatientRecordUtility getPatientUtility() {
		return patientUtility;
	}

	

}
