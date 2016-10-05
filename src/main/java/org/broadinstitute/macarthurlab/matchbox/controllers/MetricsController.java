/**
 * An  endpoint to deliver metrics
 */
package org.broadinstitute.macarthurlab.matchbox.controllers;

import java.io.IOException;
import java.util.Map;

import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.broadinstitute.macarthurlab.matchbox.metrics.Metric;
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
		Map<String,Integer> metrics = this.getMetric().countGenesInSystem();
		StringBuilder msg = new StringBuilder();	
		
		
		msg.append("{\"metrics\":");
		msg.append("{");
		msg.append("\"numberOfGenes\":{");
		int i=0;
		for (String k:metrics.keySet()){
			msg.append("\"");
			msg.append(k);
			msg.append("\"");
			
			msg.append(":");
			
			msg.append(metrics.get(k));
			
			if (i<metrics.size()-1){
				msg.append(",");
			}
			
			i+=1;
		}
		msg.append("},");
		
		msg.append("\"matches\":{");
		msg.append("\"numberOfMatches\":");
		msg.append("10");
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
	
	
	
}