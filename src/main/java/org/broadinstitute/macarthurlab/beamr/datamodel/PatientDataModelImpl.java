/**
 * This is where all queries/insertions relating Patients
 * would go through. It abstracts out using MongoDB as the
 * backend datamodel from the rest of the application
 */
package org.broadinstitute.macarthurlab.beamr.datamodel;

import org.broadinstitute.macarthurlab.beamr.datamodel.mongodb.PatientMongoRepository;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author harindra
 *
 */
public class PatientDataModelImpl implements DataModelService{

	@Autowired
	private PatientMongoRepository mongoRepository;

}
