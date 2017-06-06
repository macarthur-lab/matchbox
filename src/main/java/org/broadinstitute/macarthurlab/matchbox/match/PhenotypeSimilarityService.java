/**
 * 
 */
package org.broadinstitute.macarthurlab.matchbox.match;

import org.broadinstitute.macarthurlab.matchbox.entities.Patient;

import java.util.List;

/**
 * @author harindra
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public interface PhenotypeSimilarityService {

	public List<Double> scorePhenotypes(Patient queryPatient, List<Patient> patients);
}
