/**
 * 
 */
package org.broadinstitute.macarthurlab.matchbox.match;

import java.util.List;

import org.broadinstitute.macarthurlab.matchbox.entities.Patient;

/**
 * @author harindra
 *
 */
public interface PhenotypeSimilarityService {
	public List<Double> rankByPhenotypes(List<Patient> patients, Patient queryPatient);
}
