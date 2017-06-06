/**
 * To represent a Genotype match service
 */
package org.broadinstitute.macarthurlab.matchbox.match;

import org.broadinstitute.macarthurlab.matchbox.entities.Patient;

import java.util.List;

/**
 * @author harindra
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public interface GenotypeSimilarityService {

    public List<Double> scoreGenotypes(Patient queryPatient, List<Patient> patients);

}
