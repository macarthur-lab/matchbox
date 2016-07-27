/**
 * To represent a genetype match
 */
package org.broadinstitute.macarthurlab.matchbox.match;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
	 * Ranks a patient list by their genotype similarity to a query patient
	 * @param patients a list of patients to rank
	 * @param queryPatient a target patient to rank against
	 * @return Sends back a list of scores for each patient based on genotype. Order matches input list
	 */
	public List<Double> rankByGenotypes(List<Patient> patients, Patient queryPatient){
		List<Double> patientGenotypeRankingScores = new ArrayList<Double>();
		for (Patient patient :patients){
			patientGenotypeRankingScores.add(this.getGenotypeSimilarity(patient, queryPatient));
		}
		return patientGenotypeRankingScores;
	}

	
	/**
	 * As a first naive step, we will simply get the number of 
	 * genes they have in common against the total number of genes
	 * @param p1	patient 1
	 * @param p2	patient 2
	 * @return	a representative number (described above)
	 */
	private double getGenotypeSimilarity(Patient p1, Patient p2){
		List<String> p1Genes = new ArrayList<String>();
		p1.getGenomicFeatures().forEach((k)->{
							p1Genes.add(k.getGene().get("id"));
						});
		List<String> p2Genes = new ArrayList<String>();
		p2.getGenomicFeatures().forEach((k)->{
							p2Genes.add(k.getGene().get("id"));
						});
		List<String> p1p2Intersect = p1Genes.stream()
                .filter(p2Genes::contains)
                .collect(Collectors.toList());
		return (double)p1p2Intersect.size() / ((double)p1.getGenomicFeatures().size() +(double)p2.getGenomicFeatures().size());

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
