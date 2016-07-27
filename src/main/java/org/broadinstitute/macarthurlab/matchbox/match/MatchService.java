/**
 * Represents a matchmaker match strategy
 */
package org.broadinstitute.macarthurlab.matchbox.match;

import java.util.List;

import org.broadinstitute.macarthurlab.matchbox.entities.MatchmakerResult;
import org.broadinstitute.macarthurlab.matchbox.entities.Patient;

/**
 * @author harindra
 *
 */
public interface MatchService {

	public List<MatchmakerResult> match(Patient patient);
}
