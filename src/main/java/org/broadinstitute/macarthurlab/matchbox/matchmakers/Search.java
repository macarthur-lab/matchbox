/**
 * To represent a search in a matchmaker node
 */
package org.broadinstitute.macarthurlab.matchbox.matchmakers;

import java.util.List;

import org.broadinstitute.macarthurlab.matchbox.entities.MatchmakerResult;
import org.broadinstitute.macarthurlab.matchbox.entities.Patient;

/**
 * @author harindra
 *
 */
public interface Search {
	public List<MatchmakerResult> searchInExternalMatchmakerNodesOnly(Patient patient);
	public List<String> searchInLocalDatabaseOnly(Patient patient, String requestOriginHostname);
}
