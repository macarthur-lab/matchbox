/**
 * Generate metrics
 */
package org.broadinstitute.macarthurlab.matchbox.metrics;

import java.util.HashMap;
import java.util.Map;

import org.broadinstitute.macarthurlab.matchbox.datamodel.mongodb.MongoDBConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;


/**
 * @author harindra
 *
 */
public class Metric {
	private MongoOperations operator;
	
	/**
	 * Constructor
	 */
	public Metric() {
		ApplicationContext context = new AnnotationConfigApplicationContext(MongoDBConfiguration.class);
		this.operator = context.getBean("mongoTemplate", MongoOperations.class);
	}

	/**
	 * @return the operator
	 */
	public MongoOperations getOperator() {
		return operator;
	}

	
	public Map<String,Integer> countGenesInSystem(){
		Map<String,Integer> counts = new HashMap<String,Integer>();
		return counts;
	}
}
