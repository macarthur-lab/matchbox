/**
 * Represents a matchmaker match strategy
 */
package org.broadinstitute.macarthurlab.matchbox.match;

import org.broadinstitute.macarthurlab.matchbox.entities.MatchmakerResult;
import org.broadinstitute.macarthurlab.matchbox.entities.Patient;

import java.util.List;

/**
 * @author harindra
 *
 */
public interface MatchService {

	public List<MatchmakerResult> match(Patient patient, List<Patient> patients);

}
