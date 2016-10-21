/**
 * To represent a genetype match
 */
package org.broadinstitute.macarthurlab.matchbox.match;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import org.broadinstitute.macarthurlab.matchbox.datamodel.mongodb.MongoDBConfiguration;
import org.broadinstitute.macarthurlab.matchbox.entities.GenomicFeature;
import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author harindra
 *
 */



public class GenotypeMatch {
	private MongoOperations operator;
	private final Map<String,String> geneSymbolToEnsemblId;
	private final Map<String,String> ensemblIdToGeneSymbol;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
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
			this.getLogger().error("Error reading gene symbol to emsembl id map:"+e.toString() + " : " + e.getMessage());
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
		Set<String> usedIds = new HashSet<String>();
		for (Patient p:psGeneId){
			results.add(p);
			usedIds.add(p.getId());
		}
		BasicQuery qEnsemblId = new BasicQuery(ensemblIdQuery.toString());
		List<Patient> psEnsembl = this.getOperator().find(qEnsemblId,Patient.class);
		for (Patient p:psEnsembl){
			if (!usedIds.contains(p.getId())){
				results.add(p);
			}
		}		
		return results;
	}
	

	
	
	
	/**
	 * Ranks a patient list by their genotype similarity to a query patient. Since Genotype
	 * is half the score (other half is phenotype rank), this section can given a 0.5 as a perfect hit.
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
	 * Calculates a metric on similarity. Returns 0.5 for a perfect match.
	 * @param p1	patient 1
	 * @param queryP	patient 2
	 * @return	a representative number (described above)
	 */
	public double getGenotypeSimilarity(Patient p1, Patient queryP){
		double simScore=0.0;
		List<String> commonGenes = findCommonGenes(p1, queryP);
		//total possible addition of 0.25 (if perfect match)
		simScore += getZygosityScore(p1, queryP,commonGenes);
		//total possible addition of 0.25 (if perfect match)
		simScore += getTypeScore(p1, queryP,commonGenes);
		return simScore;
	}

	
	
	/**
	 * Access zygosity's affect on the score. If zygosity is the same 0.25 is returned.
	 * @param p1	The patient in question
	 * @param queryP	The query patient
	 * @return A score (0.25 is returned if there is a match in zygositys)
	 */
	public double getZygosityScore(Patient p1, Patient queryP,List<String> commonGenes){
		double score=0.0d;
		for (GenomicFeature gf: p1.getGenomicFeatures()){
			if (commonGenes.contains(gf.getGene().get("id"))){
				long queryPZygosity=-1L;
				for (GenomicFeature queryPgf: queryP.getGenomicFeatures()){
					if (queryPgf.getGene().get("id").equals(gf.getGene().get("id"))){
						queryPZygosity=queryPgf.getZygosity();
					}
				}
				if (gf.getZygosity() == queryPZygosity){
					score=0.25;
				}
			}
		}
		return score;
	}
	
	
	/**
	 * Generates a score based on variant positions inside a common gene. Returns a 
	 * 0.25 if a perfect match
	 * @param p1 patient
	 * @param queryP another patient
	 * @return	Returns a representative metric
	 */
	public double getTypeScore(Patient p1, Patient queryP, List<String> p1p2Intersect){
		double score=0.0;
		/**
		 * make map of query relevant gene-name/symbol:variant-type (SO code) 
		 * TODO:translate all to ensembl before comparison
		 */
		Map<String,String> queryGenomicFeatures = new HashMap<String,String>();
		queryP.getGenomicFeatures().forEach((k)->{
			if (p1p2Intersect.contains(k.getGene().get("id"))){
				queryGenomicFeatures.put(k.getGene().get("id"), 
										 k.getType().get("id"));
			}
		});
		//now see if the match has these SO codes in common
		int similarCount=0;
		for(GenomicFeature p1GenomicFeature: p1.getGenomicFeatures()){
			if (queryGenomicFeatures.containsKey(p1GenomicFeature.getGene().get("id"))){
				if ( queryGenomicFeatures.get(p1GenomicFeature.getGene().get("id")).equals(p1GenomicFeature.getType().get("id")) )
				{
					similarCount+=1;
				}
				//UP the score IF it is a HIGH danger variant type?
				if (this.getSOCodes().contains(p1GenomicFeature.getType().get("id"))){
					score += 0.1;
				}
			}
		}
		if (similarCount == queryGenomicFeatures.size()){
			score += 0.25;
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
	
	
	/**
	 * @return the logger
	 */
	public Logger getLogger() {
		return logger;
	}	
	
	

	
}
