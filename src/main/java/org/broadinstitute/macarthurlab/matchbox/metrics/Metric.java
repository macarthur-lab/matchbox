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
import org.broadinstitute.macarthurlab.matchbox.entities.ExternalMatchQuery;
import org.broadinstitute.macarthurlab.matchbox.entities.GenomicFeature;
import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.broadinstitute.macarthurlab.matchbox.entities.PhenotypeFeature;
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
	 * Counts the number of patients for a given phenotype
	 * @return a map of phenotype name to count
	 */
	public Map<String,Integer> countPhenotypesInSystem(){
		Map<String,Integer> counts = new HashMap<String,Integer>();
		StringBuilder query = new StringBuilder("{}");
		BasicQuery q = new BasicQuery(query.toString());
		List<Patient> patients = this.getOperator().find(q,Patient.class);
		for (Patient p: patients){
			for (PhenotypeFeature pf:p.getFeatures()){
				if (counts.containsKey(pf.getId())){
					int updatedCount=counts.get(pf.getId()) + 1;
					counts.put(pf.getId(),updatedCount);
				}
				else{
					counts.put(pf.getId(),1);
				}
			}
		}
		return counts;
	}
	

	
	/**
	 * Counts the number of patients for a given gene
	 * @return a count
	 */
	public int getNumOfPatientsInSystem(){
		StringBuilder query = new StringBuilder("{}");
		BasicQuery q = new BasicQuery(query.toString());
		List<Patient> patients = this.getOperator().find(q,Patient.class);
		return patients.size();
	}
	
	
	/**
	 * Counts the number of patients for a given gene
	 * #TODO return count by HPO term to show diversity
	 * @return a count
	 */
	public int getTotalNumOfPhenotypesInSystem(){
		StringBuilder query = new StringBuilder("{}");
		BasicQuery q = new BasicQuery(query.toString());
		List<Patient> patients = this.getOperator().find(q,Patient.class);
		int counts=0;
		for (Patient p: patients){
			counts += p.getFeatures().size();
		}
		return counts;
	}

	
	/**
	 * Counts the number of incoming match requests
	 * @return a count
	 */
	public int getNumOfIncomingMatchRequests(){
		StringBuilder query = new StringBuilder("{}");
		BasicQuery q = new BasicQuery(query.toString());
		List<ExternalMatchQuery> extQueries = this.getOperator().find(q,ExternalMatchQuery.class);
		return extQueries.size();
	}
	
	/**
	 * Counts the number of incoming match requests that found a match
	 * @return a count
	 */
	public int getNumOfMatches(){
		StringBuilder query = new StringBuilder("{matchFound:true}");
		BasicQuery q = new BasicQuery(query.toString());
		List<ExternalMatchQuery> extQueries = this.getOperator().find(q,ExternalMatchQuery.class);
		return extQueries.size();
	}
	
	
	

	/**
	 * @param operator the operator to set
	 */
	public void setOperator(MongoOperations operator) {
		this.operator = operator;
	}



}


