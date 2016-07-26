/**
 * Intiates a call to all match maker nodes recorded
 */
package org.broadinstitute.macarthurlab.matchbox.matchmakers;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.broadinstitute.macarthurlab.matchbox.datamodel.mongodb.MongoDBConfiguration;
import org.broadinstitute.macarthurlab.matchbox.datamodel.mongodb.PatientMongoRepository;
import org.broadinstitute.macarthurlab.matchbox.entities.GenomicFeature;
import org.broadinstitute.macarthurlab.matchbox.entities.MatchmakerResult;
import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.broadinstitute.macarthurlab.matchbox.network.HttpCommunication;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.omg.CORBA.portable.InputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.Index.Duplicates;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


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
	 * A set of tools to parse and store patient information
	 */
	private final PatientRecordUtility patientUtility;

	/**
	 * A set of tools to help with make a Http call to an external node
	 */
	private final HttpCommunication httpCommunication;
	
	
	/**
	 * Default constructor
	 */
	public MatchmakerSearch(){
		ApplicationContext context = new AnnotationConfigApplicationContext(MongoDBConfiguration.class);
		this.operator = context.getBean("mongoTemplate", MongoOperations.class);
		this.patientUtility = new PatientRecordUtility();
		this.httpCommunication = new HttpCommunication();
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
		query.append("]}}");
		BasicQuery q = new BasicQuery(query.toString());
		List<Patient> ps = this.getOperator().find(q,Patient.class);
		for (Patient p:ps){
			results.add(p);
		}
		return results;
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
		return this.getHttpCommunication().callNodeWithHttp(matchmakerNode, queryPatient);		
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
	public HttpCommunication getHttpCommunication() {
		return httpCommunication;
	}
	
	
	
	
	
}
