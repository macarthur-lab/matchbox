/**
 * Simple class to handle Http communicstions
 */
package org.broadinstitute.macarthurlab.beamr.network;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.broadinstitute.macarthurlab.beamr.entities.MatchmakerResult;
import org.broadinstitute.macarthurlab.beamr.entities.Patient;
import org.broadinstitute.macarthurlab.beamr.matchmakers.Node;
import org.broadinstitute.macarthurlab.beamr.matchmakers.PatientRecordUtility;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


/**
 * @author harindra
 *
 */
public class HttpCommunication {
	
	/**
	 * A set of tools to parse and store patient information
	 */
	private final PatientRecordUtility patientUtility;

	/**
	 * Default constructor
	 */
	public HttpCommunication(){
		this.patientUtility = new PatientRecordUtility();
	}
	
	
	
	public List<MatchmakerResult> callNodeWithHttp(Node matchmakerNode, Patient queryPatient) {
		List<MatchmakerResult> allResults = new ArrayList<MatchmakerResult>();
		//HttpCertificate.install();
		HttpsURLConnection connection = null;  
		try {
		    //Create connection
		    URL url = new URL(matchmakerNode.getUrl());
		    connection = (HttpsURLConnection)url.openConnection();
		    HttpCertificate.relaxHostChecking(connection);
		    connection.setRequestMethod("POST");
		    
		    //node specific attributes
		    connection.setRequestProperty("X-Auth-Token",matchmakerNode.getToken());
		    connection.setRequestProperty("Content-Type",matchmakerNode.getContentTypeHeader());
		    connection.setRequestProperty("Accept",matchmakerNode.getAcceptHeader());
		    connection.setRequestProperty("Content-Language",matchmakerNode.getContentLanguage());  	    
		    
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
	 * @return the patientUtility
	 */
	public PatientRecordUtility getPatientUtility() {
		return patientUtility;
	}


}
