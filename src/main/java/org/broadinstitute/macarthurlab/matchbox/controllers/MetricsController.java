/**
 * An  endpoint to deliver metrics
 */
package org.broadinstitute.macarthurlab.matchbox.controllers;


import java.util.Map;
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

/**
 * @author harindra
 *
 */

@RestController
@CrossOrigin(origins = "*")
public class MetricsController {
	private final String CONTENT_TYPE_HEADER="application/vnd.ga4gh.matchmaker.v1.0+json ";
	private final Metric metric;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * Constructor for testing
	 */
	public MetricsController() {
    	this.metric = new Metric();
	}
	
	
	
	/**
	 * Controller for /metrics GET end-point.Returns metric of LOCAL DATABASE ONLY
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/metrics")
	public ResponseEntity<?> match() {
		Map<String,Integer> geneCounts = this.getMetric().countGenesInSystem();
		Map<String,Integer> phenotypeCounts = this.getMetric().countPhenotypesInSystem();
		StringBuilder msg = new StringBuilder();	
		
		
		msg.append("{\"metrics\":");
		msg.append("{");

		//----
		msg.append("\"totalNumberOfGenes\":");
		int totalNumGenes=0;
		for (String k:geneCounts.keySet()){
			totalNumGenes += geneCounts.get(k);
		}
		msg.append(totalNumGenes);
		msg.append(",");
		
		//----
		msg.append("\"totalNumberOfPhenotypes\":");
		msg.append(this.getMetric().getTotalNumOfPhenotypesInSystem());
		msg.append(",");
		
		//----
		msg.append("\"totalNumberOfPatients\":");
		msg.append(this.getMetric().getNumOfPatientsInSystem());
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
				
		int numMatches=this.getMetric().getNumOfMatches();
		int numIncomingReqs=this.getMetric().getNumOfIncomingMatchRequests();
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
	    httpHeaders.setContentType(MediaType.valueOf(this.CONTENT_TYPE_HEADER));
		return new ResponseEntity<>(msg.toString(), httpHeaders,HttpStatus.OK);
	}



	/**
	 * @return the cONTENT_TYPE_HEADER
	 */
	public String getCONTENT_TYPE_HEADER() {
		return CONTENT_TYPE_HEADER;
	}



	/**
	 * @return the metric
	 */
	public Metric getMetric() {
		return metric;
	}



	/**
	 * @return the logger
	 */
	public Logger getLogger() {
		return logger;
	}
	
	
	
}