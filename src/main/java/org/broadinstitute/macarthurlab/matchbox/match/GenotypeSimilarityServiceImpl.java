/**
 * To represent a genetype match
 */
package org.broadinstitute.macarthurlab.matchbox.match;

import org.broadinstitute.macarthurlab.matchbox.entities.GenomicFeature;
import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author harindra
 *
 */
@Service
public class GenotypeSimilarityServiceImpl implements GenotypeSimilarityService{

	private final MongoOperations operator;
	private final Map<String,String> geneSymbolToEnsemblId;
	private final Map<String,String> ensemblIdToGeneSymbol;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * Constructor
	 */
	@Autowired
	public GenotypeSimilarityServiceImpl(MongoOperations operator, Map<String,String> geneSymbolToEnsemblId) {
		this.operator = operator;
		this.geneSymbolToEnsemblId = geneSymbolToEnsemblId;
		this.ensemblIdToGeneSymbol = new HashMap<>();

		for (Map.Entry<String, String> entry: geneSymbolToEnsemblId.entrySet()) {
			String geneSymbol = entry.getKey();
			String ensemblId = entry.getValue();
			ensemblIdToGeneSymbol.put(ensemblId, geneSymbol);
		}

	}

	/**
	 * Search for matching patients using GenomicFeatures
	 * 1. Considers it a match if they have AT LEAST 1 gene in common
	 * 2. So far only supports gene symbol and ensembl ID for gene ID field
	 */
	public List<Patient> searchByGenomicFeatures(Patient patient){
		List<Patient> results = new ArrayList<>();
		
		StringBuilder geneSymbolQuery = new StringBuilder("{'genomicFeatures.gene.id':{$in:[");
		StringBuilder ensemblIdQuery = new StringBuilder("{'genomicFeatures.gene.id':{$in:[");
		
		int i=0;
		for (GenomicFeature genomicFeature : patient.getGenomicFeatures()){
			String id = genomicFeature.getGene().get("id");
			String ensemblId="";
			String geneId="";
			if (geneSymbolToEnsemblId.containsKey(id)){
				geneId=id;
				ensemblId = geneSymbolToEnsemblId.get(id);
			}
			if (ensemblIdToGeneSymbol.containsKey(id)){
				ensemblId=id;
				geneId= ensemblIdToGeneSymbol.get(id);
			}
			
			geneSymbolQuery.append("'"+geneId+"'"); 
			if (i<patient.getGenomicFeatures().size()-1){
				geneSymbolQuery.append(",");
			}

			ensemblIdQuery.append("'"+ensemblId+"'"); 
			if (i<patient.getGenomicFeatures().size()-1){
				ensemblIdQuery.append(",");
			}
			if(!geneSymbolToEnsemblId.containsKey(id) &&
					!ensemblIdToGeneSymbol.containsKey(id)){
				String mesg="could not identify provided gene ID as ensmbl or hgnc:"+id;
				logger.error(mesg);
			}
			i++;
		}
		geneSymbolQuery.append("]}}");
		ensemblIdQuery.append("]}}");
		
		logger.info(geneSymbolQuery.toString());
		logger.info(ensemblIdQuery.toString());
		
		BasicQuery qGeneId = new BasicQuery(geneSymbolQuery.toString());
		List<Patient> psGeneId = operator.find(qGeneId,Patient.class);
		Set<String> usedIds = new HashSet<String>();
		for (Patient p:psGeneId){
			results.add(p);
			usedIds.add(p.getId());
		}
		BasicQuery qEnsemblId = new BasicQuery(ensemblIdQuery.toString());
		List<Patient> psEnsembl = operator.find(qEnsemblId,Patient.class);
		for (Patient p:psEnsembl){
			if (!usedIds.contains(p.getId())){
				results.add(p);
			}
		}		
		logger.info(results.toString());
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
	 * Access zygosity's affect on the score. If zygosity is the same in at least
	 * one of the common genes, 0.25 is returned.
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
		List<String> p1Genes = p1.getGenomicFeatures().stream()
				.map(k -> k.getGene().get("id"))
				.collect(Collectors.toList());

		List<String> p2Genes = p2.getGenomicFeatures().stream()
				.map(k -> k.getGene().get("id"))
				.collect(Collectors.toList());

		return p1Genes.stream()
                .filter(p2Genes::contains)
                .collect(Collectors.toList());
	}

}
