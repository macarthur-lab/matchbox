/**
 * Base class that has some commong methods for internal (priviledged info) and external
 * metrics queries
 */
package org.broadinstitute.macarthurlab.matchbox.metrics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.broadinstitute.macarthurlab.matchbox.datamodel.mongodb.MongoDBConfiguration;
import org.broadinstitute.macarthurlab.matchbox.entities.ExternalMatchQuery;
import org.broadinstitute.macarthurlab.matchbox.entities.GenomicFeature;
import org.broadinstitute.macarthurlab.matchbox.entities.MatchmakerResult;
import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.broadinstitute.macarthurlab.matchbox.entities.PhenotypeFeature;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;

/**
 * @author harindra
 *
 */

public abstract class BaseMetric {
	private MongoOperations operator;

	
	/**
	 * Constructor
	 */
	public BaseMetric() {
		ApplicationContext context = new AnnotationConfigApplicationContext(MongoDBConfiguration.class);
		this.operator = context.getBean("mongoTemplate", MongoOperations.class);
	}
	
	
	/**
	 * TODO
	 * Returns the percentage of genes that have made a match
	 * @return percentage of genes that have made a match
	 */
	protected double getPercentageOfGenesThatMatch(){
		return -1;
	}
	
	
	/**
	 * TODO
	 * Returns the mean number of genes per patient
	 * @return mean number of genes per patient
	 */
	protected double getMeanNumberOfGenesPerCase(List<Patient> allPatients){
		
		
		return -1;
	}
	
	
	/**
	 * TODO
	 * Returns the mean number of phenotypes per patient
	 * @return mean number of phenotypes per patient
	 */
	protected double getMeanNumberOfPhenotypesPerCase(List<Patient> allPatients){
		return -1d;
	}
	
	
	/**
	 * TODO
	 * Returns the mean number of variants per patient
	 * @return mean number of variants per patient
	 */
	protected double getMeanNumberOfVariantsPerCase(List<Patient> allPatients){
		return -1;
	}
	
	
	
	
	/**
	 * Returns the number of patients with  diagnosis listed
	 * @return number of patients with  diagnosis listed
	 */
	protected int getNumberOfCasesWithDiagnosis(){
		String query = "{$where:\"this.disorders.length > 0\"}";
		BasicQuery q = new BasicQuery(query);
		List<Patient> patients = this.getOperator().find(q,Patient.class);
		return patients.size();
	}


	/**
	 * @param operator the operator to set
	 */
	public void setOperator(MongoOperations operator) {
		this.operator = operator;
	}
	
	
	/**
	 * Counts the number of unique phenotypes in the system
	 * #TODO return count by HPO term to show diversity
	 * @return a count
	 */
	public int getTotalNumOfPhenotypesInSystem(List<Patient> allPatients){
		return this.countPhenotypesInSystem(allPatients).size();
	}
	
	/**
	 * Counts the number of patients for a given phenotype
	 * @return a map of phenotype name to count
	 */
	public Map<String,Integer> countPhenotypesInSystem(List<Patient> allPatients){
		Map<String,Integer> counts = new HashMap<String,Integer>();
		for (Patient p: allPatients){
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
	 * TODO: change query into using aggregation framework so the DB does the work. 
	 * 		OK for now with the small number of patients
	 * Returns the number of unique submitters of patients
	 * @return number of unique submitters
	 */
	protected int getNumberOfSubmitters(List<Patient> allPatients){
		Set<String> submitters=new HashSet<String>();
		for (Patient p:allPatients){
			submitters.add(p.getContact().get("name"));
		}
		return submitters.size();
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
		Map<String, Set<String>> matchedIdPairs = new HashMap<String,Set<String>>();
		StringBuilder query = new StringBuilder("{matchFound:true}");
		BasicQuery q = new BasicQuery(query.toString());
		List<ExternalMatchQuery> extQueries = this.getOperator().find(q,ExternalMatchQuery.class);
		for (ExternalMatchQuery matchedQuery:extQueries){
			String queryId = matchedQuery.getIncomingQuery().getId();
			for (MatchmakerResult result: matchedQuery.getResults()){
				if (matchedIdPairs.containsKey(queryId)){
					if (!queryId.equals(result.getPatient().getId())){
						matchedIdPairs.get(queryId).add(result.getPatient().getId());
					}
				}
				else{
					Set<String> s = new HashSet<String>();
					s.add(result.getPatient().getId());
					matchedIdPairs.put(queryId,s);
				}
			}
		}
		return matchedIdPairs.size();
	}
	
	

	
	/**
	 * Counts the number of patients for a given gene
	 * @return a count
	 */
	public int getNumOfPatientsInSystem(List<Patient> allPatients){
		return allPatients.size();
	}
	
	

	/**
	 * Counts the number of patients for a given gene
	 * @return a map of gene name to count
	 */
	public Map<String,Integer> countGenesInSystem(List<Patient> allPatients){
		Map<String,Integer> counts = new HashMap<String,Integer>();	
		for (Patient p: allPatients){
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
	 * @return the operator
	 */
	public MongoOperations getOperator() {
		return operator;
	}
	

}
