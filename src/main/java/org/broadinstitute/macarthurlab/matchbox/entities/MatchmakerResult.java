/**
 * To represent a match result
 */
package org.broadinstitute.macarthurlab.matchbox.entities;

import java.util.HashMap;
import java.util.Map;

/**
 * @author harindra
 *
 */
public class MatchmakerResult {
	/**
	 * Has the match-score value between 0-1. Has a single key called "patient" to hold it.
	 * Best hit being 1 and least being 0.
	 */
	private final Map<String,Double> score;
	/**
	 * A Patient object representing the object
	 */
	private final Patient patient;

	/**
	 * Default constructor makes empty result object
	 */
	public MatchmakerResult() {
		this.score = new HashMap<String,Double>();
		this.patient = new Patient();
	}	
	
	
	/**
	 * @param score
	 * @param patient
	 */
	public MatchmakerResult(Map<String, Double> score, Patient patient) {
		this.score = score;
		this.patient = patient;
	}
	/**
	 * @return the score
	 */
	public Map<String, Double> getScore() {
		return score;
	}
	/**
	 * @return the patient
	 */
	public Patient getPatient() {
		return patient;
	}
	
	

}
