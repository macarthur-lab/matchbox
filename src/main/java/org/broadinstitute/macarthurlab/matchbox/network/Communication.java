/**
 * Simple class to handle HTTP communications
 */
package org.broadinstitute.macarthurlab.matchbox.network;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.broadinstitute.macarthurlab.matchbox.entities.MatchmakerResult;
import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.broadinstitute.macarthurlab.matchbox.search.PatientRecordUtility;
import org.broadinstitute.macarthurlab.matchbox.entities.Node;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



/**
 * @author harindra
 *
 */
@Service
public class Communication {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * A set of tools to parse and store patient information
	 */
	@Autowired
	private PatientRecordUtility patientUtility;

	/**
	 * Default constructor
	 */
	public Communication(){}
	
	/**
	 * Makes a call to an external node
	 * @param matchmakerNode	An external MME node
	 * @param queryPatient	A patient to query with
	 * @return	results found
	 */
	public List<MatchmakerResult> callNode(Node matchmakerNode, Patient queryPatient) {
		List<MatchmakerResult> allResults = new ArrayList<MatchmakerResult>();
		HttpsURLConnection connection = null;  
		try {
		    //Create connection
		    URL url = new URL(null,matchmakerNode.getUrl(),new sun.net.www.protocol.https.Handler());
		    connection = (HttpsURLConnection)url.openConnection();

		    if (matchmakerNode.isSelfSignedCertificate()){
		    	CertificateAdjustment.install();
		    	CertificateAdjustment.relaxHostChecking(connection);
		    }
		    
		    connection.setRequestMethod("POST");
		    
		    //node specific attributes
		    connection.setRequestProperty("X-Auth-Token",matchmakerNode.getToken());
		    connection.setRequestProperty("Content-Type",matchmakerNode.getContentTypeHeader());
		    connection.setRequestProperty("Accept",matchmakerNode.getAcceptHeader());
		    connection.setRequestProperty("Content-Language",matchmakerNode.getContentLanguage());  	 
		    		    
		    connection.setUseCaches(false);
		    connection.setDoOutput(true);	    
		    
		    StringBuilder payloadBuilder = new StringBuilder();
		    payloadBuilder.append("{\"patient\":");
		    payloadBuilder.append(queryPatient.getEmptyFieldsRemovedJson());
		    payloadBuilder.append(",");
		    payloadBuilder.append("\"_disclaimer\":");
		    payloadBuilder.append("\"" + this.getPatientUtility().getDisclaimer() + "\"");
		    payloadBuilder.append("}");
		    
		    //this.getLogger().info("patient being sent out to external MME node: "+payloadBuilder.toString());
		    this.getLogger().info("patient being sent out to external MME node: "+matchmakerNode.getName());
		    DataOutputStream wr = new DataOutputStream (connection.getOutputStream());
		    wr.writeBytes(payloadBuilder.toString());
		    wr.close();

		    //Get Response  
		    java.io.InputStream is = connection.getInputStream();
		    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		    StringBuilder response = new StringBuilder(); 
		    String line;
		    while((line = rd.readLine()) != null) {
		      response.append(line);
		      response.append('\r');
		    }
		    rd.close();
		    JSONParser parser = new JSONParser();
		    this.getLogger().info("response back from external node: "+response.toString());
		    JSONObject resultJsonObject = (JSONObject) parser.parse(response.toString());

		    JSONArray  results = (JSONArray)resultJsonObject.get("results");
		    this.getLogger().info("number of results from to external MME node "+matchmakerNode.getName() + " is: " + Integer.toString(results.size()));
		    for (int i=0; i<results.size(); i++){
				JSONObject result = (JSONObject)results.get(i);
				
				
				boolean inputDataValid=this.getPatientUtility().areAllRequiredFieldsPresent(result.toString());
				if (inputDataValid) {
					//parse out patient data
					Patient parsedPatient = this.getPatientUtility().parsePatientInformation(result.toString());
					
					//parse out score data
					HashMap<String, Double> extMathscore = new HashMap<String, Double>();
					JSONObject score = (JSONObject)result.get("score");
					extMathscore.put("patient", ((Number)score.get("patient")).doubleValue());
					
					allResults.add(new MatchmakerResult(extMathscore,parsedPatient));
				} else {
					this.getLogger().error("error parsing patient from external source (required fields missing):"+
													matchmakerNode.getName() + " : " + result.toString());
				}
				
		    }		 
		  } catch (Exception e) {
			  this.getLogger().error("error connecting to: " + matchmakerNode.getName() + ", moving on.. : "+e.getMessage());    
		  } finally {
		    if(connection != null) {
		      connection.disconnect(); 
		    }
		  }
		return allResults;
	}
	


	/**
	 * @return the patientUtility
	 */
	public PatientRecordUtility getPatientUtility() {
		return patientUtility;
	}

	/**
	 * @return the logger
	 */
	public Logger getLogger() {
		return logger;
	}
}
