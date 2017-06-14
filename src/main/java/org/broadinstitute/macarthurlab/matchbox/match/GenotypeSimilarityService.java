/**
 * To represent a Genotype match service
 */
package org.broadinstitute.macarthurlab.matchbox.match;

import org.broadinstitute.macarthurlab.matchbox.entities.Patient;

/**
 * @author harindra
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public interface GenotypeSimilarityService {

    public GenotypeSimilarityScore scoreGenotypes(Patient queryPatient, Patient nodePatients);

}
