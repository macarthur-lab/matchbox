/**
 * To represent a public facing metric end point
 */
package org.broadinstitute.macarthurlab.matchbox.metrics;

import org.broadinstitute.macarthurlab.matchbox.datamodel.mongodb.MongoDBConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Service;

/**
 * @author harindra
 *
 */
@Service(value="publicMetricServiceImpl")
public class PublicMetricServiceImpl implements MetricService {
	private MongoOperations operator;
	
	/**
	 * Default constructor
	 */
	public PublicMetricServiceImpl(){
		ApplicationContext context = new AnnotationConfigApplicationContext(MongoDBConfiguration.class);
		this.operator = context.getBean("mongoTemplate", MongoOperations.class);
	}

	
	/**
	 * @param operator the operator to set
	 */
	public void setOperator(MongoOperations operator) {
		this.operator = operator;
	}
	
	
	@Override
	/**
	 * Returns a String representing a JSON
	 * TODO should be an obj reprsenting the JSON
	 */
	public String getMetrics() {
		StringBuilder msg = new StringBuilder();	
		msg.append("{\"metrics\":\"\"}");
		return msg.toString();
	}


	/**
	 * @return the operator
	 */
	public MongoOperations getOperator() {
		return operator;
	}



}


















