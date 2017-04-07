/**
 * Intiates a call to all match maker nodes recorded
 */
package org.broadinstitute.macarthurlab.matchbox.search;



import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import org.broadinstitute.macarthurlab.matchbox.datamodel.mongodb.PatientMongoRepository;
import org.broadinstitute.macarthurlab.matchbox.entities.ExternalMatchQuery;
import org.broadinstitute.macarthurlab.matchbox.entities.MatchmakerResult;
import org.broadinstitute.macarthurlab.matchbox.entities.Node;
import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.broadinstitute.macarthurlab.matchbox.match.MatchService;
import org.broadinstitute.macarthurlab.matchbox.network.Communication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author harindra
 *
 */
@Service
public class MatchmakerSearchImpl implements SearchService{
	/**
	 * A list of MatchmakeNode objs that would be all
	 * available nodes in system to look for. 
	 * 
	 * This is populated via config.xml file via Spring IoC
	 */
	private List<Node> matchmakers;
	
	/**
	 * Genotype matching tools
	 */
	@Autowired
	private MatchService match;
	
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
	 * Default constructor
	 */
	public MatchmakerSearchImpl(){}
	
	
	/**
	 * Search in matchmaker node network only (not in Beamr data model)
	 * @param	A Patient object
	 */
	public List<MatchmakerResult> searchInExternalMatchmakerNodesOnly(Patient patient){
		List<MatchmakerResult> allResults = new ArrayList<MatchmakerResult>();
		for (Node n:this.getMatchmakers()){
			allResults.addAll(this.searchNode(n, patient));
		}
		return allResults;
	}
	
	/**
	 * Search in local matchmaker node ONLY, not in the large matchmaker network. Log the results
	 * and the query that was used for those hits
	 * @param	A patient
	 */
	public List<String> searchInLocalDatabaseOnly(Patient queryPatient, String hostNameOfRequestOrigin){
		List<String> scrubbedResults=new ArrayList<String>();
		List<MatchmakerResult> results = this.getMatch().match(queryPatient);
		for (MatchmakerResult r:results){
			if (!r.getPatient().getId().equals(queryPatient.getId())){
				scrubbedResults.add(r.getEmptyFieldsRemovedJson());
			}
			else{
				this.getLogger().info("ignoring this result since it is the same as query patient (same ID)");
			}
		}
	   /**
		*  persist for logging and metrics and tracking of data sent out. Persist the 
		*  incoming query ONLY if a match is made, otherwise don't keep any of the
		*  information that is sent in, which is only fair.
		*/
		ExternalMatchQuery externalQueryMatch;
		if (results.size()>0){
				externalQueryMatch = new ExternalMatchQuery(queryPatient, 
															results,
															hostNameOfRequestOrigin,
															queryPatient.getContact().get("institution"),
															true);
				
		}else{
			//we don't persist query unless there is a match per MME rules
			externalQueryMatch = new ExternalMatchQuery(null, 
					   									results,
					   									hostNameOfRequestOrigin,
					   									queryPatient.getContact().get("institution"),
					   									false);
		}
		this.getOperator().save(externalQueryMatch);
		return scrubbedResults;
	}
		

	/**
	 * Searches in this external matchmaker node for this patient
	 * @param matchmakerNode	A matchmaker node/center
	 * @param patient	A patient
	 * @return	The results found for this patient
	 * @throws MalformedURLException 
	 */
	private List<MatchmakerResult> searchNode(Node matchmakerNode, Patient queryPatient) {
		this.getLogger().info("searching in external node: "+matchmakerNode.getName());
			return this.getHttpCommunication().callNode(matchmakerNode, queryPatient);	
	}

	
	

	/**
	 * @return the matchmakers
	 */
	public List<Node> getMatchmakers() {
		return matchmakers;
	}


	/**
	 * @param matchmakers the matchmakers to set
	 */
	public void setMatchmakers(List<Node> matchmakers) {
		this.matchmakers = matchmakers;
	}


	/**
	 * @return the patientMongoRepository
	 */
	public PatientMongoRepository getPatientMongoRepository() {
		return this.patientMongoRepository;
	}


	/**
	 * @return the operator
	 */
	public MongoOperations getOperator() {
		return operator;
	}


	/**
	 * @param operator the operator to set
	 */
	public void setOperator(MongoOperations operator) {
		this.operator = operator;
	}



	/**
	 * @return the patientUtility
	 */
	public PatientRecordUtility getPatientUtility() {
		return patientUtility;
	}


	/**
	 * @return the httpCommunication
	 */
	public Communication getHttpCommunication() {
		return httpCommunication;
	}


	/**
	 * @return the genotypeMatch
	 */
	public MatchService getMatch() {
		return this.match;
	}
	

	/**
	 * @param patientMongoRepository the patientMongoRepository to set
	 */
	public void setPatientMongoRepository(PatientMongoRepository patientMongoRepository) {
		this.patientMongoRepository = patientMongoRepository;
	}

	
	
	/**
	 * @return the logger
	 */
	public Logger getLogger() {
		return logger;
	}

	
	
}
