/**
 * To represent a search. As of now we will be searching only
 * in Matchmaker network, but hid search features behind this
 * interface in order to leave room for searching in other
 * networks in the future
 */
package org.broadinstitute.macarthurlab.matchbox.search;

import org.broadinstitute.macarthurlab.matchbox.entities.Patient;

import java.util.List;

/**
 * @author harindra
 */
public interface SearchService {
    public List<String> searchInExternalMatchmakerNodesOnly(Patient patient);

    public List<String> searchInLocalDatabaseOnly(Patient patient, String requestOriginHostname);
}
