/**
 * To represent a search. As of now we will be searching only
 * in Matchmaker network, but hid search features behind this
 * interface in order to leave room for searching in other
 * networks in the future
 */
package org.broadinstitute.macarthurlab.matchbox.matchmakers;

import java.util.List;

import org.broadinstitute.macarthurlab.matchbox.entities.MatchmakerResult;
import org.broadinstitute.macarthurlab.matchbox.entities.Patient;

/**
 * @author harindra
 *
 */
public interface SearchService {
	public List<MatchmakerResult> searchInExternalMatchmakerNodesOnly(Patient patient);
	public List<String> searchInLocalDatabaseOnly(Patient patient, String requestOriginHostname);
}
