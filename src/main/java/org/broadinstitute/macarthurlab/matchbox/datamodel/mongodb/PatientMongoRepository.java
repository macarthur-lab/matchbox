/**
 * An interface to MongoDb datamodel
 */
package org.broadinstitute.macarthurlab.matchbox.datamodel.mongodb;

import java.util.List;

import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author harindra
 *
 */
public interface PatientMongoRepository extends MongoRepository<Patient, String> {
	Long deletePatientById(String id);
	List<Patient> findAll();
}
