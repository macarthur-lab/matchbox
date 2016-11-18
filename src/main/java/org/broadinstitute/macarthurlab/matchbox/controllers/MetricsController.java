/**
 * An  endpoint to deliver metrics
 */
package org.broadinstitute.macarthurlab.matchbox.controllers;


import org.broadinstitute.macarthurlab.matchbox.metrics.Metric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author harindra
 *
 */

@RestController
@CrossOrigin(origins = "*")
public class MetricsController {
	private static final Logger logger = LoggerFactory.getLogger(MetricsController.class);

	private static final String CONTENT_TYPE_HEADER="application/vnd.ga4gh.matchmaker.v1.0+json ";
	private final Metric metric;

	public MetricsController(Metric metric) {
    	this.metric = metric;
	}

	/**
	 * Controller for /metrics GET end-point.Returns metric of LOCAL DATABASE ONLY
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/metrics", produces = CONTENT_TYPE_HEADER)
	public ResponseEntity<?> match() {
		Map<String,Integer> geneCounts = metric.countGenesInSystem();
		Map<String,Integer> phenotypeCounts = metric.countPhenotypesInSystem();
		StringBuilder msg = new StringBuilder();	
		
		
		msg.append("{\"metrics\":");
		msg.append("{");

		//----
		msg.append("\"totalNumberOfGenes\":");
		int totalNumGenes=0;
		for (String k : geneCounts.keySet()){
			totalNumGenes += geneCounts.get(k);
		}
		msg.append(totalNumGenes);
		msg.append(",");
		
		//----
		msg.append("\"totalNumberOfPhenotypes\":");
		msg.append(metric.getTotalNumOfPhenotypesInSystem());
		msg.append(",");
		
		//----
		msg.append("\"totalNumberOfPatients\":");
		msg.append(metric.getNumOfPatientsInSystem());
		msg.append(",");
		
		//----
		msg.append("\"geneCounts\":{");
		int i=0;
		for (String k:geneCounts.keySet()){
			msg.append("\"");
			msg.append(k);
			msg.append("\"");
			msg.append(":");
			msg.append(geneCounts.get(k));
			if (i<geneCounts.size()-1){
				msg.append(",");
			}
			i+=1;
		}
		msg.append("},");
		
		
		//----
		msg.append("\"phenotypeCounts\":{");
		int j=0;
		for (String k:phenotypeCounts.keySet()){
			msg.append("\"");
			msg.append(k);
			msg.append("\"");
			msg.append(":");
			msg.append(phenotypeCounts.get(k));
			if (j<phenotypeCounts.size()-1){
				msg.append(",");
			}
			j+=1;
		}
		msg.append("},");
		
		
		//----
		msg.append("\"matches\":{");

		int numMatches= metric.getNumOfMatches();
		int numIncomingReqs= metric.getNumOfIncomingMatchRequests();
		//----
		msg.append("\"numberOfIncomingMatchRequests\":");
		msg.append(numIncomingReqs);
		msg.append(",");
		
		//----
		msg.append("\"numberOfMatchesMade\":");
		msg.append(numMatches);
		msg.append(",");
		
		//----
		msg.append("\"matchRatio\":");
		double ratio=(double)numMatches / (double)numIncomingReqs;
		if (Double.isNaN(ratio)){
			ratio=0.0d;
		}
		msg.append(ratio);
		
		msg.append("}");
		msg.append("}}");
		
		final HttpHeaders httpHeaders= new HttpHeaders();
	    httpHeaders.setContentType(MediaType.valueOf(CONTENT_TYPE_HEADER));
		return new ResponseEntity<>(msg.toString(), httpHeaders,HttpStatus.OK);
	}

}