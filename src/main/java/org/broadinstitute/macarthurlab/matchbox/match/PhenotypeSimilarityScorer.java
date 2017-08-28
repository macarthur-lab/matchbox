package org.broadinstitute.macarthurlab.matchbox.match;

import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.broadinstitute.macarthurlab.matchbox.entities.PhenotypeSimilarityScore;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public interface PhenotypeSimilarityScorer {

    public PhenotypeSimilarityScore scorePhenotypes(Patient queryPatient, Patient nodePatient);

}
