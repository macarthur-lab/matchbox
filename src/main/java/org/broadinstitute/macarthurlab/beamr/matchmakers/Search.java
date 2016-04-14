/**
 * To represent a search in a matchmaker node
 */
package org.broadinstitute.macarthurlab.beamr.matchmakers;

import java.util.List;

import org.broadinstitute.macarthurlab.beamr.entities.MatchmakerResult;
import org.broadinstitute.macarthurlab.beamr.entities.Patient;

/**
 * @author harindra
 *
 */
public interface Search {
	public List<MatchmakerResult> searchInExternalMatchmakerNodesOnly(Patient patient);
	public List<MatchmakerResult> searchInLocalDatabaseOnly(Patient patient);
}
