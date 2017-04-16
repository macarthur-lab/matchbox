/**
 * Generate metrics
 */
package org.broadinstitute.macarthurlab.matchbox.metrics;


import java.util.Map;
import org.broadinstitute.macarthurlab.matchbox.datamodel.mongodb.MongoDBConfiguration;
import org.broadinstitute.macarthurlab.matchbox.entities.PrivilegedMetric;
import org.broadinstitute.macarthurlab.matchbox.entities.Metric;
import org.broadinstitute.macarthurlab.matchbox.entities.PublicMetric;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Service;

/**
 * @author harindra
 *
 */
@Service(value="privilegedMetricServiceImpl")
public class PrivilegedMetricServiceImpl extends BaseMetric implements MetricService{
	private MongoOperations operator;

	
	/**
	 * Constructor
	 */
	public PrivilegedMetricServiceImpl() {
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
	 * @param operator the operator to set
	 */
	public void setOperator(MongoOperations operator) {
		this.operator = operator;
	}


	/**
	 * Returns a String representing a JSON
	 * TODO should be an obj reprsenting the JSON
	 */
	public PublicMetric getMetricsTEMP() {
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
		
		//return msg.toString();
		return new PublicMetric();
	}


	@Override
	/**
	 * Returns a seiries of metric as JSON
	 */
	public Metric getMetrics() {	
		return new PrivilegedMetric();
	}

	

}


