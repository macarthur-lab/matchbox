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
import org.broadinstitute.macarthurlab.matchbox.entities.ExternalMatchQuery;
import org.broadinstitute.macarthurlab.matchbox.entities.GenomicFeature;
import org.broadinstitute.macarthurlab.matchbox.entities.MatchmakerResult;
import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.broadinstitute.macarthurlab.matchbox.entities.PhenotypeFeature;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.stereotype.Service;

/**
 * @author harindra
 *
 */
@Service(value="internalMetricServiceImpl")
public class InternalMetricServiceImpl implements MetricService{
	private MongoOperations operator;

	
	/**
	 * Constructor
	 */
	public InternalMetricServiceImpl() {
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
	 * Counts the number of unique phenotypes in the system
	 * #TODO return count by HPO term to show diversity
	 * @return a count
	 */
	public int getTotalNumOfPhenotypesInSystem(){
		return this.countPhenotypesInSystem().size();
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
		Map<String,Integer> geneCounts = this.countGenesInSystem();
		Map<String,Integer> phenotypeCounts = this.countPhenotypesInSystem();
		StringBuilder msg = new StringBuilder();	
		
		
		msg.append("{\"metrics\":");
		msg.append("{");

		//----
		msg.append("\"totalNumberOfGenes\":");
		msg.append(geneCounts.size());
		msg.append(",");
		
		//----
		msg.append("\"totalNumberOfPhenotypes\":");
		msg.append(this.getTotalNumOfPhenotypesInSystem());
		msg.append(",");
		
		//----
		msg.append("\"totalNumberOfPatients\":");
		msg.append(this.getNumOfPatientsInSystem());
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
				
		int numMatches=this.getNumOfMatches();
		int numIncomingReqs=this.getNumOfIncomingMatchRequests();
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
		
		return msg.toString();
	}



}


