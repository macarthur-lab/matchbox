/**
 * An interface to MongoDb datamodel
 */
package org.broadinstitute.macarthurlab.beamr.datamodel.mongodb;

import org.broadinstitute.macarthurlab.beamr.entities.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author harindra
 *
 */
public interface PatientMongoRepository extends MongoRepository<Patient, String> {
}
