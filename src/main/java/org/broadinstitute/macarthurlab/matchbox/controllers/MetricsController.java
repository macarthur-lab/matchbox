/**
 * An  endpoint to deliver metrics
 */
package org.broadinstitute.macarthurlab.matchbox.controllers;

import java.util.HashMap;
import java.util.Map;

import org.broadinstitute.macarthurlab.matchbox.entities.Metric;
import org.broadinstitute.macarthurlab.matchbox.metrics.MetricService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
	
	@Autowired
	@Qualifier("privilegedMetricServiceImpl")
	private MetricService privilegedMetric;
	
	@Autowired
	@Qualifier("publicMetricServiceImpl")
	private MetricService publicMetric;
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * Constructor for testing
	 */
	public MetricsController() {}
	
	
	
	/**
	 * Controller for /metrics GET end-point.Returns metric of LOCAL DATABASE ONLY
	 * and is meant for INTERNAL VIEWING ONLY
	 * TODO: add a "privileged" phrase to URL so it looks like /metrics/privileged
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/metrics")
	public Map<String,Metric> internalMatch() {		
		final HttpHeaders httpHeaders= new HttpHeaders();
	    httpHeaders.setContentType(MediaType.valueOf(this.CONTENT_TYPE_HEADER));
	    Map<String,Metric> rslt = new HashMap<String,Metric>();
	    Metric metrics=this.getPrivilegedMetric().getMetrics();
	    rslt.put("metrics", metrics);
	    this.getLogger().info("returned public metrics:"+metrics);
	    return rslt;
	}

	
	/**
	 * Controller for /metrics/public GET end-point.Returns metric of LOCAL DATABASE ONLY
	 * and is meant for EXTERNAL VIEWING, so won't have gene level information
	 * but only counts.
	 * Follows common metrics API spec
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/metrics/public")
	public Map<String,Metric> publicMatch() {		
		final HttpHeaders httpHeaders= new HttpHeaders();
	    httpHeaders.setContentType(MediaType.valueOf(this.CONTENT_TYPE_HEADER));
	    Map<String,Metric> rslt = new HashMap<String,Metric>();
	    Metric metrics=this.getPublicMetric().getMetrics();
	    rslt.put("metrics", metrics);
	    this.getLogger().info("returned public metrics:"+metrics);
	    return rslt;
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
	public MetricService getPrivilegedMetric() {
		return privilegedMetric;
	}


	/**
	 * @param metric the metric to set
	 */
	public void setPrivilegedMetric(MetricService metric) {
		this.privilegedMetric = metric;
	}



	/**
	 * @return the logger
	 */
	public Logger getLogger() {
		return logger;
	}



	/**
	 * @return the publicMetric
	 */
	public MetricService getPublicMetric() {
		return publicMetric;
	}



	/**
	 * @param publicMetric the publicMetric to set
	 */
	public void setPublicMetric(MetricService publicMetric) {
		this.publicMetric = publicMetric;
	}
	
	
}