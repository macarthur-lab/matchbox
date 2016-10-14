/**
 * Simple class to handle Http communicstions
 */
package org.broadinstitute.macarthurlab.matchbox.network;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.broadinstitute.macarthurlab.matchbox.entities.MatchmakerResult;
import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.broadinstitute.macarthurlab.matchbox.entities.Node;
import org.broadinstitute.macarthurlab.matchbox.matchmakers.PatientRecordUtility;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


/**
 * @author harindra
 *
 */
public class Communication {
	
	/**
	 * A set of tools to parse and store patient information
	 */
	private final PatientRecordUtility patientUtility;

	/**
	 * Default constructor
	 */
	public Communication(){
		this.patientUtility = new PatientRecordUtility();
	}
	
	
	
	public List<MatchmakerResult> callNode(Node matchmakerNode, Patient queryPatient) {
		System.setProperty("javax.net.ssl.trustStore","/local/mme/config/java/jdk1.8.0_101/keystore");
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

		    //Send request
		    //String payload="{\"patient\":{\"id\":\"1\",\"contact\": {\"name\":\"Jane Doe\", \"href\":\"mailto:jdoe@example.edu\"},\"features\":[{\"id\":\"HP:0000522\"}],\"genomicFeatures\":[{\"gene\":{\"id\":\"NGLY1\"}}]}}";
		    String payload = "{\"patient\":" + queryPatient.getEmptyFieldsRemovedJson() + "}";
		    System.out.println(payload);
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
		    System.out.println(response);
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
					System.out.println("error parsing patient from external source (required fields missing):"+matchmakerNode.getName());
				}
				
		    }		 
		  } catch (Exception e) {
			  e.printStackTrace();
			  System.out.println("error connecting to: " + matchmakerNode.getName() + ", moving on.. : "+e);    
		  } finally {
		    if(connection != null) {
		      connection.disconnect(); 
		    }
		  }
		return allResults;
	}
	
	
	/**
	 * DEPRACATED--, need a better solution
	 * Check if this host is live
	 * @param hostName name of host
	 * @param portNum port number
	 * @return	true if live
	 */
	public boolean isHostLive(String hostName, int portNum) {
	    try (Socket socket = new Socket()) {
	        socket.connect(new InetSocketAddress(hostName, portNum));
	        return true;
	    } catch (IOException e) {
	        return false; 
	    }
	}


	/**
	 * @return the patientUtility
	 */
	public PatientRecordUtility getPatientUtility() {
		return patientUtility;
	}


}
