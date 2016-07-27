/**
 * To represent a genetype match
 */
package org.broadinstitute.macarthurlab.matchbox.match;

import java.util.ArrayList;
import java.util.List;

import org.broadinstitute.macarthurlab.matchbox.datamodel.mongodb.MongoDBConfiguration;
import org.broadinstitute.macarthurlab.matchbox.entities.GenomicFeature;
import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.BasicQuery;

/**
 * @author harindra
 *
 */
public class GenotypeMatch {
	private MongoOperations operator;

	/**
	 * 
	 */
	public GenotypeMatch() {
		ApplicationContext context = new AnnotationConfigApplicationContext(MongoDBConfiguration.class);
		this.operator = context.getBean("mongoTemplate", MongoOperations.class);
	}

	/**
	 * Search for matching patients using GenomicFeatures
	 * 1. Considers it a match if they have AT LEAST 1 gene in common
	 */
	public List<Patient> searchByGenomicFeatures(Patient patient){
		List<Patient> results = new ArrayList<Patient>();		
		StringBuilder query = new StringBuilder("{'genomicFeatures.gene.id':{$in:[");
		int i=0;
		for (GenomicFeature genomicFeature : patient.getGenomicFeatures()){
			String geneId = genomicFeature.getGene().get("id");
			query.append("'"+geneId+"'"); 
			if (i<patient.getGenomicFeatures().size()-1){
				query.append(",");
			}
			i++;
		}
		query.append("]}}");
		BasicQuery q = new BasicQuery(query.toString());
		List<Patient> ps = this.getOperator().find(q,Patient.class);
		for (Patient p:ps){
			results.add(p);
		}
		return results;
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

	
}
