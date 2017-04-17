/**
 * To represent a public facing metric end point
 */
package org.broadinstitute.macarthurlab.matchbox.metrics;

import java.util.List;

import org.broadinstitute.macarthurlab.matchbox.datamodel.mongodb.MongoDBConfiguration;
import org.broadinstitute.macarthurlab.matchbox.entities.Metric;
import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.broadinstitute.macarthurlab.matchbox.entities.PublicMetric;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.stereotype.Service;

/**
 * @author harindra
 *
 */
@Service(value="publicMetricServiceImpl")
public class PublicMetricServiceImpl extends BaseMetric implements MetricService {
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
	public Metric getMetrics() {
		StringBuilder query = new StringBuilder("{}");
		BasicQuery q = new BasicQuery(query.toString());
		List<Patient> allPatients = this.getOperator().find(q,Patient.class);
		
		return new PublicMetric(
								this.getNumberOfSubmitters(allPatients), 
								this.countGenesInSystem(allPatients).size(), 
								this.countPhenotypesInSystem(allPatients).size(),
								this.getNumberOfCasesWithDiagnosis(), 
								this.getNumOfPatientsInSystem(allPatients), 
								this.getPercentageOfGenesThatMatch(allPatients),
								this.getMeanNumberOfGenesPerCase(allPatients), 
								this.getMeanNumberOfVariantsPerCase(allPatients), 
								this.getMeanNumberOfPhenotypesPerCase(allPatients),
								this.getNumOfIncomingMatchRequests(), 
								this.getNumOfMatches());
	}


	/**
	 * @return the operator
	 */
	public MongoOperations getOperator() {
		return operator;
	}



}


















