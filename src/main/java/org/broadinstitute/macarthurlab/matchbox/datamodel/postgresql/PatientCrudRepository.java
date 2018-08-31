package org.broadinstitute.macarthurlab.matchbox.datamodel.postgresql;



import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.springframework.data.repository.CrudRepository;

public interface PatientCrudRepository extends CrudRepository<Patient, Long>{
}
