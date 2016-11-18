/**
 * Intiates a call to all matchService maker nodes recorded
 */
package org.broadinstitute.macarthurlab.matchbox.matchmakers;


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
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * @author harindra
 *
 */
@Component
public class MatchmakerSearch implements Search {

	private static final Logger logger = LoggerFactory.getLogger(MatchmakerSearch.class);

	/**
	 * A list of MatchmakeNode objs that would be all
	 * available nodes in system to look for. 
	 * 
	 * This is populated via config.xml file via Spring IoC
	 */
	private final List<Node> matchmakerNodes;
	
	/**
	 * Genotype matching tools
	 */
	private final MatchService matchService;
	
	/**
	 * A connection to MongoDB for queries
	 */
	private PatientMongoRepository patientMongoRepository;

	
	private MongoOperations operator;
	
	/**
	 * A set of tools to parse and store patient information
	 */
	private final PatientRecordUtility patientUtility;

	/**
	 * A set of tools to help with make a Http call to an external node
	 */
	private final Communication httpCommunication;
	
	/**
	 * Default constructor
	 */
	@Autowired
	public MatchmakerSearch(PatientMongoRepository patientMongoRepository, MongoOperations operator, MatchService matchService, List<Node> matchmakerNodes){
		this.patientMongoRepository = patientMongoRepository;
		this.operator = operator;
		this.matchService = matchService;
		this.matchmakerNodes = matchmakerNodes;
		this.patientUtility = new PatientRecordUtility();
		this.httpCommunication = new Communication();
	}
	
	
	/**
	 * Search in matchmaker node network only (not in Beamr data model)
	 * @param	A Patient object
	 */
	public List<MatchmakerResult> searchInExternalMatchmakerNodesOnly(Patient patient){
		//ideally this should be able to run in parallel, however searchNodeFor cannot, so don't try.
		return matchmakerNodes.stream().flatMap(searchNodeFor(patient)).collect(toList());
	}
	
	/**
	 * Search in local matchmaker node ONLY, not in the large matchmaker network. Log the results
	 * and the query that was used for those hits
	 * @param	A patient
	 */
	public List<String> searchInLocalDatabaseOnly(Patient queryPatient, String hostNameOfRequestOrigin){
		logger.info("Searching in local database for {} from {}", queryPatient.getId(), hostNameOfRequestOrigin);
		List<String> scrubbedResults = new ArrayList<>();
		List<MatchmakerResult> results = matchService.match(queryPatient);
		for (MatchmakerResult r : results) {
			if (r.getPatient().getId().equals(queryPatient.getId())) {
				logger.info("Removing patient hit with same id {}", r.getPatient().getId());
			} else {
				scrubbedResults.add(r.getEmptyFieldsRemovedJson());
			}
		}
		/**persist for logging and metrics and tracking of data sent out. Persist the
		 *  incoming query ONLY if a matchService is made, otherwise don't keep any of the
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

	//TODO: Logically, shouldn't the MatchMakerNode have this method?
	//public Stream<MatchMakerResult> search(Patient queryPatient)
	private Function<Node, Stream<MatchmakerResult>> searchNodeFor(Patient queryPatient) {
		return matchmakerNode -> {
			logger.info("searching in external node: " + matchmakerNode.getName());
			return httpCommunication.callNode(matchmakerNode, queryPatient).stream();
		};
	}

	/**
	 * Searches in this external matchmaker node for this patient
	 * @param matchmakerNode	A matchmaker node/center
	 * @param patient	A patient
	 * @return	The results found for this patient
	 * @throws MalformedURLException 
	 */
	private List<MatchmakerResult> searchNodeFor(Node matchmakerNode, Patient queryPatient) {
		logger.info("searching in external node: " + matchmakerNode.getName());
		return httpCommunication.callNode(matchmakerNode, queryPatient);
	}

}
