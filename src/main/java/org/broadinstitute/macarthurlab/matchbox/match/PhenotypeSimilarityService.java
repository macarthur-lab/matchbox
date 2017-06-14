/**
 * 
 */
package org.broadinstitute.macarthurlab.matchbox.match;

import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.broadinstitute.macarthurlab.matchbox.entities.PhenotypeFeature;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @author harindra
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public interface PhenotypeSimilarityService {

	public PhenotypeSimilarityScore scorePhenotypes(Patient queryPatient, Patient nodePatient);

	public static List<String> getObservedPhenotypeIds(Patient patient) {
		return patient.getFeatures().stream()
				.filter(phenotypeFeature -> "yes".equals(phenotypeFeature.getObserved()))
				.map(PhenotypeFeature::getId)
				.collect(toList());
	}
}
