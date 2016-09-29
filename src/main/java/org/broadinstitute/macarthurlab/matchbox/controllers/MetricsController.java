/**
 * An  endpoint to deliver metrics
 */
package org.broadinstitute.macarthurlab.matchbox.controllers;

import java.io.IOException;

import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
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
	
	/**
	 * Constructor for testing
	 */
	public MetricsController() {}
	
	
	
	/**
	 * Controller for /metrics GET end-point.Returns metric of LOCAL DATABASE ONLY
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/metrics")
	public ResponseEntity<?> match() {
		StringBuilder msg = new StringBuilder();
		msg.append("matchmaker request from:");		
		String results = "{" + "\"metrics\":" + "metrics" + "}";
		
		final HttpHeaders httpHeaders= new HttpHeaders();
	    httpHeaders.setContentType(MediaType.valueOf(this.CONTENT_TYPE_HEADER));
		return new ResponseEntity<>(results, httpHeaders,HttpStatus.OK);
	}
}