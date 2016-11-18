/**
 * Simple class to handle Http communicstions
 */
package org.broadinstitute.macarthurlab.matchbox.network;

import org.broadinstitute.macarthurlab.matchbox.entities.MatchmakerResult;
import org.broadinstitute.macarthurlab.matchbox.entities.Node;
import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.broadinstitute.macarthurlab.matchbox.matchmakers.PatientRecordUtility;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * @author harindra
 *
 */
public class Communication {
	private static final Logger logger = LoggerFactory.getLogger(Communication.class);
	
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
		//TODO: this needs to be injected too
		System.setProperty("javax.net.ssl.trustStore", "/local/mme/config/java/jdk1.8.0_101/keystore");
		List<MatchmakerResult> allResults = new ArrayList<MatchmakerResult>();
		HttpsURLConnection connection = null;
		try {
			//Create connection
			URL url = new URL(null, matchmakerNode.getUrl(), new sun.net.www.protocol.https.Handler());
//			TODO: Why not just do this? URL url = new URL(matchmakerNode.getUrl());
//			URL url = new URL(matchmakerNode.getUrl());
			connection = (HttpsURLConnection) url.openConnection();

			if (matchmakerNode.isSelfSignedCertificate()) {
				CertificateAdjustment.install();
				CertificateAdjustment.relaxHostChecking(connection);
			}

			connection.setRequestMethod("POST");

			//node specific attributes
			connection.setRequestProperty("X-Auth-Token", matchmakerNode.getToken());
			connection.setRequestProperty("Content-Type", matchmakerNode.getContentTypeHeader());
			connection.setRequestProperty("Accept", matchmakerNode.getAcceptHeader());
			connection.setRequestProperty("Content-Language", matchmakerNode.getContentLanguage());

			connection.setUseCaches(false);
			connection.setDoOutput(true);

			//Send request
			Instant start = Instant.now();
			//String payload="{\"patient\":{\"id\":\"1\",\"contact\": {\"name\":\"Jane Doe\", \"href\":\"mailto:jdoe@example.edu\"},\"features\":[{\"id\":\"HP:0000522\"}],\"genomicFeatures\":[{\"gene\":{\"id\":\"NGLY1\"}}]}}";
			String payload = "{\"patient\":" + queryPatient.getEmptyFieldsRemovedJson() + "}";
			logger.info("patient being sent out to external MME node: {} {}", matchmakerNode.getName(), payload);
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.writeBytes(payload);
			wr.close();

			//Get Response
			java.io.InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			StringBuilder response = new StringBuilder(); // or StringBuffer if not Java 5+
			String line;
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			rd.close();
			JSONParser parser = new JSONParser();
			Instant end = Instant.now();

			logger.info("response back from {} ({} ms): {}", matchmakerNode.getName(), Duration.between(start, end).toMillis(), response);
			JSONObject resultJsonObject = (JSONObject) parser.parse(response.toString());
			JSONArray results = (JSONArray) resultJsonObject.get("results");

			for (int i = 0; i < results.size(); i++) {
				JSONObject result = (JSONObject) results.get(i);
				boolean inputDataValid = patientUtility.areAllRequiredFieldsPresent(result.toString());
				if (inputDataValid) {
					Patient parsedPatient = patientUtility.parsePatientInformation(result.toString());
					allResults.add(new MatchmakerResult(
							new HashMap<String, Double>(),
							parsedPatient
					));
				} else {
					logger.error("error parsing patient from external source (required fields missing): {} : {}", matchmakerNode.getName(), result.toString());
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("error connecting to: {}, moving on.. : {}", matchmakerNode.getName(), e.getMessage());
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		return allResults;
	}

}
