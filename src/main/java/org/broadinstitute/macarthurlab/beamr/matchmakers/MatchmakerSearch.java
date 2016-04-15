/**
 * Intiates a call to all match maker nodes recorded
 */
package org.broadinstitute.macarthurlab.beamr.matchmakers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.broadinstitute.macarthurlab.beamr.datamodel.mongodb.MongoDBConfiguration;
import org.broadinstitute.macarthurlab.beamr.datamodel.mongodb.PatientMongoRepository;
import org.broadinstitute.macarthurlab.beamr.entities.GenomicFeature;
import org.broadinstitute.macarthurlab.beamr.entities.MatchmakerResult;
import org.broadinstitute.macarthurlab.beamr.entities.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.BasicQuery;


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
	 * A connection to MongoDB for queries
	 */
	@Autowired
	private PatientMongoRepository patientMongoRepository;
	
	private MongoOperations operator;

	
	
	/**
	 * Default constructor
	 */
	public MatchmakerSearch(){
		ApplicationContext context = new AnnotationConfigApplicationContext(MongoDBConfiguration.class);
		this.operator = context.getBean("mongoTemplate", MongoOperations.class);
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
	public List<MatchmakerResult> searchInLocalDatabaseOnly(Patient patient){
		List<MatchmakerResult> allResults = new ArrayList<MatchmakerResult>();
		List<Patient> genomicFeatMatches = searchByGenomicFeatures(patient);
		for (Patient p: genomicFeatMatches){
			allResults.add(new MatchmakerResult(
												new HashMap<String, Double>(),
												p
												));
		}
		return allResults;
	}
	
	
	/**
	 * Search for matching patients using GenomicFeatures
	 * 1. Considers it a match if they have AT LEAST 1 gene in common
	 */
	private List<Patient> searchByGenomicFeatures(Patient patient){
		List<Patient> results = new ArrayList<Patient>();		
		StringBuilder query = new StringBuilder("{'genomicFeatures.gene.id':{$in:[");
		int i=0;
		for (GenomicFeature genomicFeature : patient.getGenomicFeatures()){
			String geneId = genomicFeature.getGene().get("id");
			query.append("'"+geneId+"'"); 
			if (i<patient.getGenomicFeatures().size()-1){
				query.append(",");
			}
			i++;
		}
		query.append("]}})");
		BasicQuery q = new BasicQuery(query.toString());
		List<Patient> ps = this.getOperator().find(q,Patient.class);
		for (Patient p:ps){
			System.out.println(p);
			results.add(p);
		}
		return results;
	}
	

	/**
	 * Searches in this external matchmaker node for this patient
	 * @param matchmakerNode	A matchmaker node/center
	 * @param patient	A patient
	 * @return	The results found for this patient
	 */
	private List<MatchmakerResult> searchNode(Node matchmakerNode, Patient patient){
		System.out.println(this.callUrl(""));
		System.out.println("--");
		return new ArrayList<MatchmakerResult>();
	}
	
	/**
	 * Call this URL and fetch result
	 * @param url	A URL to call
	 */
	private String callUrl(String url){
		return "";
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





	
	
	
	
}
