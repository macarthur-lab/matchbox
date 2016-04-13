/**
 * This is where all queries/insertions relating Patients
 * would go through. It abstracts out using MongoDB as the
 * backend datamodel from the rest of the application
 */
package org.broadinstitute.macarthurlab.beamr.datamodel;

import org.broadinstitute.macarthurlab.beamr.datamodel.mongodb.PatientMongoRepository;
import org.broadinstitute.macarthurlab.beamr.entities.Patient;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A sieries of methods get implemented by Spring data here
 *
 */
public class PatientDataModelImpl implements DataModelService{
	@Autowired
	private PatientMongoRepository patientMongoRepository;
	
	/**
	 * Deafault constructor
	 */
	public PatientDataModelImpl(){}

	/**
	 * Save a patient in model
	 * @param patient
	 * Return true if saved ok
	 */
	public Patient savePatient(Patient patient){
		return this.getPatientMongoRepository().save(patient);
	}

	/**
	 * @return the mongoRepository
	 */
	public PatientMongoRepository getPatientMongoRepository() {
		return patientMongoRepository;
	}

	



}
