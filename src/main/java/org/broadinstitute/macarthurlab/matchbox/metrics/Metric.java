/**
 * Generate metrics
 */
package org.broadinstitute.macarthurlab.matchbox.metrics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.broadinstitute.macarthurlab.matchbox.datamodel.mongodb.MongoDBConfiguration;
import org.broadinstitute.macarthurlab.matchbox.datamodel.mongodb.PatientMongoRepository;
import org.broadinstitute.macarthurlab.matchbox.entities.GenomicFeature;
import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.BasicQuery;

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

	/**
	 * Counts the number of patients for a given gene
	 * @return a map of gene name to count
	 */
	public Map<String,Integer> countGenesInSystem(){
		Map<String,Integer> counts = new HashMap<String,Integer>();
		
		StringBuilder query = new StringBuilder("{}");
		BasicQuery q = new BasicQuery(query.toString());
		List<Patient> patients = this.getOperator().find(q,Patient.class);
		for (Patient p: patients){
			for (GenomicFeature gf:p.getGenomicFeatures()){
				if (counts.containsKey(gf.getGene().get("id"))){
					int updatedCount=counts.get(gf.getGene().get("id")) + 1;
					counts.put(gf.getGene().get("id"),updatedCount);
				}
				else{
					counts.put(gf.getGene().get("id"),1);
				}
			}
		}
		return counts;
	}


	/**
	 * @param operator the operator to set
	 */
	public void setOperator(MongoOperations operator) {
		this.operator = operator;
	}



}


