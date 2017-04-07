/**
 * To represent a Genotype match service
 */
package org.broadinstitute.macarthurlab.matchbox.match;

import java.util.List;

import org.broadinstitute.macarthurlab.matchbox.entities.Patient;

/**
 * @author harindra
 *
 */
public interface GenotypeSimilarityService {
	public List<Patient> searchByGenomicFeatures(Patient patient);
	public List<Double> rankByGenotypes(List<Patient> patients, Patient queryPatient);
	public double getGenotypeSimilarity(Patient p1, Patient queryP);
	public double getZygosityScore(Patient p1, Patient queryP,List<String> commonGenes);
	public double getTypeScore(Patient p1, Patient queryP, List<String> p1p2Intersect);
}
