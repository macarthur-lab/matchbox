/**
 * Intiates a call to all match maker nodes recorded
 */
package org.broadinstitute.macarthurlab.matchbox.matchmakers;


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
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

//import org.broadinstitute.macarthurlab.matchbox.datamodel.mongodb.MongoDBConfiguration;

/**
 * @author harindra
 */
@Component
public class MatchmakerSearch implements Search {

    private static final Logger logger = LoggerFactory.getLogger(MatchmakerSearch.class);

    /**
     * A list of MatchmakeNode objs that would be all
     * available nodes in system to look for.
     * <p>
     * This is populated via config.xml file via Spring IoC
     */
    private List<Node> matchmakers;
    private final MatchService matchService;

    private MongoOperations operator;
    private final Communication httpCommunication;

    @Autowired
    public MatchmakerSearch(MongoOperations operator, MatchService matchService, List<Node> matchmakerNodes) {
        this.operator = operator;
        this.matchService = matchService;
        this.matchmakers = matchmakerNodes;
        this.httpCommunication = new Communication();
    }

    /**
     * Search in matchmaker node network only (not in Beamr data model)
     *
     * @param patient object
     */
    public List<MatchmakerResult> searchInExternalMatchmakerNodesOnly(Patient patient) {
        List<MatchmakerResult> allResults = new ArrayList<MatchmakerResult>();
        for (Node n : matchmakers) {
            allResults.addAll(this.searchNode(n, patient));
        }
        return allResults;
    }

    /**
     * Search in local matchmaker node ONLY, not in the large matchmaker network. Log the results
     * and the query that was used for those hits
     *
     * @param queryPatient
     */
    public List<String> searchInLocalDatabaseOnly(Patient queryPatient, String hostNameOfRequestOrigin) {
        List<String> scrubbedResults = new ArrayList<String>();
        List<MatchmakerResult> results = matchService.match(queryPatient);
        for (MatchmakerResult r : results) {
            if (!r.getPatient().getId().equals(queryPatient.getId())) {
                scrubbedResults.add(r.getEmptyFieldsRemovedJson());
            }
        }
        /**persist for logging and metrics and tracking of data sent out. Persist the
         *  incoming query ONLY if a match is made, otherwise don't keep any of the
         *  information that is sent in, which is only fair.
         */
        ExternalMatchQuery externalQueryMatch;
        if (results.size() > 0) {
            externalQueryMatch = new ExternalMatchQuery(queryPatient,
                    results,
                    hostNameOfRequestOrigin,
                    queryPatient.getContact().get("institution"),
                    true);

        } else {
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
     * @param queryPatient        A patient
     * @throws MalformedURLException
     * @return The results found for this patient
     */
    private List<MatchmakerResult> searchNode(Node matchmakerNode, Patient queryPatient) {
        logger.info("searching in external node: " + matchmakerNode.getName());
        return httpCommunication.callNode(matchmakerNode, queryPatient);
    }

}
