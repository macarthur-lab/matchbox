/**
 * Intiates a call to all match maker nodes recorded
 */
package org.broadinstitute.macarthurlab.matchbox.matchmakers;



import java.net.MalformedURLException;



import java.util.ArrayList;
import java.util.List;
import org.broadinstitute.macarthurlab.matchbox.datamodel.mongodb.MongoDBConfiguration;
import org.broadinstitute.macarthurlab.matchbox.datamodel.mongodb.PatientMongoRepository;
import org.broadinstitute.macarthurlab.matchbox.entities.MatchmakerResult;
import org.broadinstitute.macarthurlab.matchbox.entities.Node;
import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.broadinstitute.macarthurlab.matchbox.match.Match;
import org.broadinstitute.macarthurlab.matchbox.match.MatchService;
import org.broadinstitute.macarthurlab.matchbox.network.Communication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;


/**
 * @author harindra
 *
 */
public class MatchmakerSearch implements Search{
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
	private final MatchService match;
	
	/**
	 * A connection to MongoDB for queries
	 */
	@Autowired
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
	public MatchmakerSearch(){
		ApplicationContext context = new AnnotationConfigApplicationContext(MongoDBConfiguration.class);
		this.operator = context.getBean("mongoTemplate", MongoOperations.class);
		this.patientUtility = new PatientRecordUtility();
		this.httpCommunication = new Communication();
		this.match = new Match();
	}
	
	
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
	 * Search in local matchmaker node ONLY, not in the large matchmaker network
	 * @param	A patient
	 */
	public List<String> searchInLocalDatabaseOnly(Patient patient){
		List<String> scrubbedResults=new ArrayList<String>();
		for (MatchmakerResult r:this.getMatch().match(patient)){
			scrubbedResults.add(r.getEmptyFieldsRemovedJson());
		}
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
		System.out.println("searching in external node: "+matchmakerNode.getName());
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



	
	
	
	
	
}
