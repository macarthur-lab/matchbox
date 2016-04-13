/**
 * To represent a generic data model/database
 */
package org.broadinstitute.macarthurlab.beamr.datamodel;

import org.broadinstitute.macarthurlab.beamr.entities.Patient;

/**
 * @author harindra
 *
 */
public interface DataModelService {
	public Patient savePatient(Patient patient);
}
