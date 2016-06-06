/**
 * Intiates a call to all match maker nodes recorded
 */
package org.broadinstitute.macarthurlab.beamr.matchmakers;

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
import org.broadinstitute.macarthurlab.beamr.datamodel.mongodb.MongoDBConfiguration;
import org.broadinstitute.macarthurlab.beamr.datamodel.mongodb.PatientMongoRepository;
import org.broadinstitute.macarthurlab.beamr.entities.GenomicFeature;
import org.broadinstitute.macarthurlab.beamr.entities.MatchmakerResult;
import org.broadinstitute.macarthurlab.beamr.entities.Patient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.omg.CORBA.portable.InputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.BasicQuery;
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
	 * Default constructor
	 */
	public MatchmakerSearch(){
		ApplicationContext context = new AnnotationConfigApplicationContext(MongoDBConfiguration.class);
		this.operator = context.getBean("mongoTemplate", MongoOperations.class);
		this.patientUtility = new PatientRecordUtility();
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
		System.out.println(matchmakerNode.getName());
		System.out.println(matchmakerNode.getToken());
		System.out.println(matchmakerNode.getUrl());
		List<MatchmakerResult> allResults = new ArrayList<MatchmakerResult>();
		  HttpURLConnection connection = null;  
		  try {
		    //Create connection
		    URL url = new URL(matchmakerNode.getUrl());
		    connection = (HttpURLConnection)url.openConnection();
		    connection.setRequestMethod("POST");
		    connection.setRequestProperty("Content-Type","application/vnd.ga4gh.matchmaker.v1.0+json");
		    connection.setRequestProperty("Content-Language", "en-US");  
		    connection.setUseCaches(false);
		    connection.setDoOutput(true);
		    

		    //Send request
		    String payload="{\"patient\":{\"id\":\"1\",\"contact\": {\"name\":\"Jane Doe\", \"href\":\"mailto:jdoe@example.edu\"},\"features\":[{\"id\":\"HP:0000522\"}],\"genomicFeatures\":[{\"gene\":{\"id\":\"NGLY1\"}}]}}";
		    DataOutputStream wr = new DataOutputStream (connection.getOutputStream());
		    wr.writeBytes(payload);
		    wr.close();

		    //Get Response  
		    java.io.InputStream is = connection.getInputStream();
		    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		    StringBuilder response = new StringBuilder(); // or StringBuffer if not Java 5+ 
		    String line;
		    while((line = rd.readLine()) != null) {
		      response.append(line);
		      response.append('\r');
		    }
		    rd.close();
		    JSONParser parser = new JSONParser();
		    JSONObject resultJsonObject = (JSONObject) parser.parse(response.toString());
		    JSONArray  results = (JSONArray)resultJsonObject.get("results");
		    
		    for (int i=0; i<results.size(); i++){
				JSONObject result = (JSONObject)results.get(i);
				boolean inputDataValid=this.getPatientUtility().areAllRequiredFieldsPresent(result.toString());
				if (inputDataValid) {
					Patient parsedPatient = this.getPatientUtility().parsePatientInformation(result.toString());
					allResults.add(new MatchmakerResult(
							new HashMap<String, Double>(),
							parsedPatient
							));
				} else {
					//TODO what to do here?
				}
				
		    }		    
		  } catch (Exception e) {
		    e.printStackTrace();
		    return null;
		  } finally {
		    if(connection != null) {
		      connection.disconnect(); 
		    }
		  }
		return allResults;
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

	
	
	
	
}
