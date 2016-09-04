/**
 * To represent a genetype match
 */
package org.broadinstitute.macarthurlab.matchbox.match;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
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
	private final Map<String,String> geneSymbolToEnsemblId;
	private final Map<String,String> ensemblIdToGeneSymbol;
	
	/**
	 * Constructor
	 */
	public GenotypeMatch() {
		ApplicationContext context = new AnnotationConfigApplicationContext(MongoDBConfiguration.class);
		this.operator = context.getBean("mongoTemplate", MongoOperations.class);
		
		this.geneSymbolToEnsemblId = new HashMap<String,String>();	
		this.ensemblIdToGeneSymbol = new HashMap<String,String>();	
		try{
			String geneSymbolToEnsemnlId = System.getProperty("user.dir") + "/resources/gene_symbol_to_ensembl_id_map.txt";
			
			File geneSymbolToEnsemnlIdFile = new File(geneSymbolToEnsemnlId);
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
					this.ensemblIdToGeneSymbol.put(ensemblId,geneSymbol);
				}
        }
        reader.close();
		}
		catch (Exception e){
			e.printStackTrace();
			System.out.println("Error in reading in gene symbol to emsembl id map:"+e.toString());
		}	
	}

	/**
	 * Search for matching patients using GenomicFeatures
	 * 1. Considers it a match if they have AT LEAST 1 gene in common
	 * 2. So far only supports gene symbol and ensembl ID for gene ID field
	 */
	public List<Patient> searchByGenomicFeatures(Patient patient){
		List<Patient> results = new ArrayList<Patient>();		
		
		StringBuilder geneSymbolQuery = new StringBuilder("{'genomicFeatures.gene.id':{$in:[");
		StringBuilder ensemblIdQuery = new StringBuilder("{'genomicFeatures.gene.id':{$in:[");
		
		int i=0;
		for (GenomicFeature genomicFeature : patient.getGenomicFeatures()){
			String id = genomicFeature.getGene().get("id");
			String ensemblId="";
			String geneId="";
			if (this.getGeneSymbolToEnsemblId().containsKey(id)){
				geneId=id;
				ensemblId=this.geneSymbolToEnsemblId.get(id);
			}
			if (this.getEnsemblIdToGeneSymbol().containsKey(id)){
				ensemblId=id;
				geneId=this.getEnsemblIdToGeneSymbol().get(id);
			}
			

			geneSymbolQuery.append("'"+geneId+"'"); 
			if (i<patient.getGenomicFeatures().size()-1){
				geneSymbolQuery.append(",");
			}
			

			ensemblIdQuery.append("'"+ensemblId+"'"); 
			if (i<patient.getGenomicFeatures().size()-1){
				ensemblIdQuery.append(",");
			}
								
			i++;
		}
		geneSymbolQuery.append("]}}");
		ensemblIdQuery.append("]}}");
		
		BasicQuery qGeneId = new BasicQuery(geneSymbolQuery.toString());
		List<Patient> psGeneId = this.getOperator().find(qGeneId,Patient.class);
		for (Patient p:psGeneId){
			results.add(p);
		}
		
		BasicQuery qEnsemblId = new BasicQuery(ensemblIdQuery.toString());
		List<Patient> psEnsembl = this.getOperator().find(qEnsemblId,Patient.class);
		for (Patient p:psEnsembl){
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
	 * Calculates a metric on similarity
	 * @param p1	patient 1
	 * @param queryP	patient 2
	 * @return	a representative number (described above)
	 */
	public double getGenotypeSimilarity(Patient p1, Patient queryP){
		double simScore=0.0;
		List<String> commonGenes = findCommonGenes(p1, queryP);
		simScore += getCommonGenesScore(p1, queryP,commonGenes);
		simScore += getVariantPositionScore(p1, queryP,commonGenes);
		return simScore;
	}
	
	
	/**
	 * As a first naive step, we will simply get the number of 
	 * genes they have in common against the total number of genes
	 * @param p1 patient
	 * @param queryP another patient
	 * @param p1p2Intersect genes both patients have in common
	 * @return	A genes in common metric
	 */
	public double getCommonGenesScore(Patient p1, Patient queryP, List<String> p1p2Intersect){
		return (double)p1p2Intersect.size() / 
				((double)p1.getGenomicFeatures().size() +(double)queryP.getGenomicFeatures().size());
	}
	
	
	/**
	 * Generates a score based on variant positions inside a common gene
	 * @param p1 patient
	 * @param queryP another patient
	 * @return	Returns a representative metric
	 */
	public double getVariantPositionScore(Patient p1, Patient queryP, List<String> p1p2Intersect){
		double score=0.0;
		//make map of relevant genes/gene-info
		Map<String,GenomicFeature> commonQueryGenes = new HashMap<String,GenomicFeature>();
		queryP.getGenomicFeatures().forEach((k)->{
			if (p1p2Intersect.contains(k.getGene().get("id"))){
				commonQueryGenes.put(k.getGene().get("id"), k);
			}
		});
		for(GenomicFeature genomicFeature: p1.getGenomicFeatures()){
			if (p1p2Intersect.contains(genomicFeature.getGene().get("id"))){
				//if it is a HIGH danger variant type
				if (this.getSOCodes().contains(genomicFeature.getType().get("id"))){
					score += 0.1;
				}
			}
		}
		return score;
	}
	
	
	
	/**
	 * TODO: abstract this to config file
	 * Get a list of SO codes of mutations. gotten from
	 * //http://doc-openbio.readthedocs.io/projects/jannovar/en/master/var_effects.html
	 * @return a list of SO codes
	 */
	private List<String> getSOCodes(){
		List<String> codes= new ArrayList<String>();
		codes.add("SO:1000182");
		codes.add("SO:0001624");
		codes.add("SO:0001572");
		codes.add("SO:0001909");
		codes.add("SO:0001910");
		codes.add("SO:0001589");
		codes.add("SO:0001908");
		codes.add("SO:0001906");
		codes.add("SO:0001583");
		codes.add("SO:1000005");
		codes.add("SO:0002012");
		codes.add("SO:0002012");
		codes.add("SO:0002012");
		codes.add("SO:0001619");
		codes.add("SO:0001575");
		codes.add("SO:0001619");	
		return codes;
	}
	
	
	
	/**
	 * Returns a list of common genes
	 * @param p1 patient
	 * @param p2 patient
	 */
	public List<String> findCommonGenes(Patient p1, Patient p2){
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
		
		return p1p2Intersect;
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
	 * @return the geneSymbolToEnsemblId
	 */
	public Map<String, String> getGeneSymbolToEnsemblId() {
		return geneSymbolToEnsemblId;
	}

	/**
	 * @return the ensemblIdToGeneSymbol
	 */
	public Map<String, String> getEnsemblIdToGeneSymbol() {
		return ensemblIdToGeneSymbol;
	}
	
	
	
	
	

	
}
