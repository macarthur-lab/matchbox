/**
 * Intiates a call to all match maker nodes recorded
 */
package org.broadinstitute.macarthurlab.matchbox.search;


import org.broadinstitute.macarthurlab.matchbox.datamodel.mongodb.PatientMongoRepository;
import org.broadinstitute.macarthurlab.matchbox.entities.ExternalMatchQuery;
import org.broadinstitute.macarthurlab.matchbox.entities.MatchmakerResult;
import org.broadinstitute.macarthurlab.matchbox.entities.Node;
import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.broadinstitute.macarthurlab.matchbox.match.MatchService;
import org.broadinstitute.macarthurlab.matchbox.network.Communication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author harindra
 */
@Service
public class MatchmakerSearchImpl implements SearchService {
    /**
     * A list of MatchmakeNode objs that would be all
     * available nodes in system to look for. Contained
     * in nodes.json file contained in the config directory
     * where JAR file lives
     */
    @Autowired
    private List<Node> matchmakerNodes;

    /**
     * Genotype matching tools
     */
    @Autowired
    private MatchService matchService;

    /**
     * A connection to MongoDB for queries
     */
    @Autowired
    private PatientMongoRepository patientMongoRepository;

    @Autowired
    private MongoOperations operator;

    /**
     * A set of tools to parse and store patient information
     */
    @Autowired
    private PatientRecordUtility patientUtility;

    /**
     * A set of tools to help with make a Http call to an external node
     */
    @Autowired
    private Communication httpCommunication;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    /**
     * Search in matchmaker node network only (not in Beamr data model)
     *
     * @param    A Patient object
     */
    public List<String> searchInExternalMatchmakerNodesOnly(Patient patient) {
        List<MatchmakerResult> allResults = new ArrayList<MatchmakerResult>();
        List<String> scrubbedResults = new ArrayList<String>();
        for (Node n : this.matchmakerNodes) {
            allResults.addAll(this.searchNode(n, patient));
        }
        for (MatchmakerResult r : allResults) {
            scrubbedResults.add(r.getEmptyFieldsRemovedJson());
            logger.info("found and scrubbed empty results off external match result: " + r.getPatient().getId());
        }
        return scrubbedResults;
    }

    /**
     * Search in local matchmaker node ONLY, not in the large matchmaker network. Log the results
     * and the query that was used for those hits
     *
     * @param    A patient
     */
    public List<String> searchInLocalDatabaseOnly(Patient queryPatient, String hostNameOfRequestOrigin) {
        List<Patient> nodePatients = patientMongoRepository.findAll();

        List<MatchmakerResult> results = matchService.match(queryPatient, nodePatients);

        List<String> scrubbedResults = new ArrayList<>();
        for (MatchmakerResult r : results) {
            if (!r.getPatient().getId().equals(queryPatient.getId())) {
                scrubbedResults.add(r.getEmptyFieldsRemovedJson());
            } else {
                logger.info("ignoring this result since it is the same as query patient (same ID)");
            }
        }
        /**
         *  persist for logging and metrics and tracking of data sent out. Persist the
         *  incoming query ONLY if a match is made, otherwise don't keep any of the
         *  information that is sent in, which is only fair.
         */
        ExternalMatchQuery externalQueryMatch;
        if (!results.isEmpty()) {
            externalQueryMatch = new ExternalMatchQuery(queryPatient,
                    results,
                    hostNameOfRequestOrigin,
                    queryPatient.getContact().get("institution"),
                    true);

        } else {
            //we don't persist query unless there is a match per MME rules
            externalQueryMatch = new ExternalMatchQuery(null,
                    results,
                    hostNameOfRequestOrigin,
                    queryPatient.getContact().get("institution"),
                    false);
        }
        operator.save(externalQueryMatch);
        return scrubbedResults;
    }


    /**
     * Searches in this external matchmaker node for this patient
     *
     * @param matchmakerNode A matchmaker node/center
     * @param patient        A patient
     * @throws MalformedURLException
     * @return The results found for this patient
     */
    private List<MatchmakerResult> searchNode(Node matchmakerNode, Patient queryPatient) {
        logger.info("searching in external node: " + matchmakerNode.getName());
        return httpCommunication.callNode(matchmakerNode, queryPatient);
    }


}
