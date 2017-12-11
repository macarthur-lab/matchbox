/**
 * Base class that has some commong methods for internal (priviledged info) and external
 * metrics queries
 */
package org.broadinstitute.macarthurlab.matchbox.metrics;

import org.broadinstitute.macarthurlab.matchbox.entities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

/**
 * @author harindra
 *
 */
@Component
public class BaseMetric {
	private static final Logger logger = LoggerFactory.getLogger(BaseMetric.class);
	
	@Value("${matchbox.gene-symbol-to-id-mappings}")
	private String geneSymbolToEnsemnlIdFileName;
	
    @Autowired
	MongoOperations operator;
    
	Map<String,String> geneSymbolToEnsemblId;

	public BaseMetric() {
		this.geneSymbolToEnsemblId = new HashMap<String,String>();	
		try{
			File geneSymbolToEnsemnlIdFile = new File(this.getGeneSymbolToEnsemnlIdFileName());
			BufferedReader reader = new BufferedReader(new FileReader(geneSymbolToEnsemnlIdFile));
			while (true) {
				String line = reader.readLine();
				if (line == null)
					break;
				/**
				 * Each row is expected to look like,
				 * HGNC:5  A1BG    ENSG00000121410
				 */
				StringTokenizer st=new StringTokenizer(line);
				if (st.countTokens()==3){
					st.nextToken(); 
					String geneSymbol=st.nextToken(); 
					String ensemblId=st.nextToken();
					this.geneSymbolToEnsemblId.put(geneSymbol, ensemblId);
				}
        }
        reader.close();
		}
		catch (Exception e){
			this.logger.error("Error reading gene symbol to emsembl id map:"+e.toString() + " : " + e.getMessage());
		}
	}

	/**
	 * TODO:
	 * 	- Nested for loop needs to be improved and processing moved to DB, OK for low load now)
	 *  
	 * Returns the percentage of genes that have made a match
	 * @return percentage of genes that have made a match
	 */
	protected double getPercentageOfGenesThatMatch(List<Patient> allPatients){
		Map<String,Integer> geneCounts = this.countGenesInSystem(allPatients);
		Set<String> allGenes = new HashSet<String>();
		for (String geneName: geneCounts.keySet()){
			allGenes.add(geneName);
		}
		String query = "{}";
		BasicQuery q = new BasicQuery(query);
		List<ExternalMatchQuery> extQueries = operator.find(q,ExternalMatchQuery.class);
		Set<String> matchedGenes = new HashSet<String>();
		for (ExternalMatchQuery extQry:extQueries){
			for (MatchmakerResult result : extQry.getResults()){
				for ( GenomicFeature gf: result.getPatient().getGenomicFeatures()){
					if (this.isEnsemblGeneId(gf.getGene().get("id"))){
						matchedGenes.add(gf.getGene().get("id"));
					}
					else{
						if (geneSymbolToEnsemblId.containsKey(gf.getGene().get("id"))){
							matchedGenes.add(geneSymbolToEnsemblId.get(geneSymbolToEnsemblId));
						}
					}
				}
			}
		}
		return (double)matchedGenes.size()/(double)allGenes.size();
	}
	
	
	
	/**
	 * Simple method to check if Ensembl ID
	 * @return true if Ensembl ID
	 */
	protected boolean isEnsemblGeneId(String id){
		if ("ENS".equals(id.substring(0,3))){
			return true;
		}
		return false;
	}
	
	/**
	 * TODO
	 * Returns the mean number of genes per patient
	 * @return mean number of genes per patient
	 */
	protected double getMeanNumberOfGenesPerCase(List<Patient> allPatients){
		int genomicFeatureCount=0;
		for (Patient p : allPatients){
				genomicFeatureCount += p.getGenomicFeatures().size();
		}
		return (double)genomicFeatureCount/(double)allPatients.size();
	}
	
	
	/**
	 * TODO
	 * Returns the mean number of phenotypes per patient
	 * @return mean number of phenotypes per patient
	 */
	protected double getMeanNumberOfPhenotypesPerCase(List<Patient> allPatients){
		int featureCount=0;
		for (Patient p : allPatients){
				featureCount += p.getFeatures().size();
		}
		return (double)featureCount/(double)allPatients.size();
	}
	
	
	/**
	 * TODO
	 * Returns the mean number of variants per patient
	 * @return mean number of variants per patient
	 */
	protected double getMeanNumberOfVariantsPerCase(List<Patient> allPatients){
		int variantCount=0;
		for (Patient p : allPatients){
			for (GenomicFeature gf : p.getGenomicFeatures()){
				if (!gf.getVariant().isUnPopulated()){
					variantCount += 1;
				}
			}
		}
		return (double)variantCount/(double)allPatients.size();
	}
	
	
	
	
	/**
	 * Returns the number of patients with  diagnosis listed
	 * @return number of patients with  diagnosis listed
	 */
	protected int getNumberOfCasesWithDiagnosis(){
		String query = "{$where:\"this.disorders.length > 0\"}";
		BasicQuery q = new BasicQuery(query);
		List<Patient> patients = operator.find(q,Patient.class);
		return patients.size();
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
		List<ExternalMatchQuery> extQueries = operator.find(q,ExternalMatchQuery.class);
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
		List<ExternalMatchQuery> extQueries = operator.find(q,ExternalMatchQuery.class);
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
	 * @return the geneSymbolToEnsemnlIdFileName
	 */
	String getGeneSymbolToEnsemnlIdFileName() {
		return geneSymbolToEnsemnlIdFileName;
	}

	/**
	 * @param geneSymbolToEnsemnlIdFileName the geneSymbolToEnsemnlIdFileName to set
	 */
	void setGeneSymbolToEnsemnlIdFileName(String geneSymbolToEnsemnlIdFileName) {
		this.geneSymbolToEnsemnlIdFileName = geneSymbolToEnsemnlIdFileName;
	}
	
	
	

}
