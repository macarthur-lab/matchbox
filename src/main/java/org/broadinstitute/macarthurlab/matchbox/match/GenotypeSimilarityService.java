/**
 * To represent a Genotype match service
 */
package org.broadinstitute.macarthurlab.matchbox.match;

import org.broadinstitute.macarthurlab.matchbox.entities.GenotypeSimilarityScore;
import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.broadinstitute.macarthurlab.matchbox.network.Communication;

/**
 * @author harindra
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public interface GenotypeSimilarityService {

    public GenotypeSimilarityScore scoreGenotypes(Patient queryPatient, Patient nodePatients);
    void setHttpCommunication(Communication httpCommunication);

}
