/**
 * To represent a genetype match
 */
package org.broadinstitute.macarthurlab.matchbox.match;

import org.broadinstitute.macarthurlab.matchbox.entities.*;
import org.broadinstitute.macarthurlab.matchbox.network.Communication;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toList;



/**
 * @author harindra
 */
@Service
public class GenotypeSimilarityServiceImpl implements GenotypeSimilarityService {

    private static final Logger logger = LoggerFactory.getLogger(GenotypeSimilarityServiceImpl.class);
    
    //Returning 0.0 here. If there are phenotypes, ans only-phenotype matches are allowed, that score will be the only/final used; otherwise no match anyway
    private static final GenotypeSimilarityScore NO_GENOTYPE_MATCH = new GenotypeSimilarityScore(0.0, Collections.emptyList());
    
    //Same gene matches, but variants not matching
    //private static final GenotypeSimilarityScore GENOTYPE_MATCH_WITH_DISSIMILAR_VARIANTS = new GenotypeSimilarityScore(0.9, Collections.emptyList());

    //Perfect genotype match at the variant level and gene level
    private static final GenotypeSimilarityScore PERFECT_GENOTYPE_MATCH = new GenotypeSimilarityScore(1.0d, Collections.emptyList());
     
    private final Map<String, String> geneSymbolToEnsemblId;
    private final Map<String, String> ensemblIdToGeneSymbol;
    
    private static Map<String,Double> geneAlleleFreqCache = new ConcurrentHashMap<>();
    
    /**
     * A set of tools to help with make a Http call to an external node
     */
    @Autowired
    private Communication httpCommunication;

    /**
     * Constructor
     */
    @Autowired
    public GenotypeSimilarityServiceImpl(Map<String, String> geneSymbolToEnsemblId) {
        this.geneSymbolToEnsemblId = geneSymbolToEnsemblId;
        this.ensemblIdToGeneSymbol = new HashMap<>();

        for (Map.Entry<String, String> entry : geneSymbolToEnsemblId.entrySet()) {
            String geneSymbol = entry.getKey();
            String ensemblId = entry.getValue();
            ensemblIdToGeneSymbol.put(ensemblId, geneSymbol);
        }
    }


    
    /**
     * Calculates a metric on similarity. Returns 0.5 for a perfect match.
     *
     * @param queryPatient external query patient
     * @param nodePatient patient in the node
     * @return a representative number (described above)
     */
    public GenotypeSimilarityScore scoreGenotypes(Patient queryPatient, Patient nodePatient) {
        if (nodePatient.getGenomicFeatures().isEmpty() || queryPatient.getGenomicFeatures().isEmpty()) {
            return NO_GENOTYPE_MATCH;
        }
        //here is list of gene-matches the query and this specific node patient had in common
        List<GenomicFeatureMatch> geneMatches = findGenomicFeatureMatches(queryPatient, nodePatient);
        
        if (geneMatches.isEmpty()){
            return NO_GENOTYPE_MATCH;
        }

        if (geneMatches.size() == queryPatient.getGenomicFeatures().size() && queryPatient.getGenomicFeatures().size() == nodePatient.getGenomicFeatures().size() ){
        	boolean allVariantsSame=true;
        	for (GenomicFeatureMatch gfMatch : geneMatches){
        		if(!gfMatch.hasZygosityMatch() || 
        		   !gfMatch.hasTypeMatch()  || 
        		   !gfMatch.hasSameVariantPosition() ||
        		   !gfMatch.hasVariantMatch()
        		   ){
        			allVariantsSame=false;
        		}	
        	}
        	if (allVariantsSame){
        		return PERFECT_GENOTYPE_MATCH;
        	}
        	
        	//DEPRACETD: should return a score here based on gnomad
        	//genes are in common, but at the variant level, there is a mismatch
        	//return GENOTYPE_MATCH_WITH_DISSIMILAR_VARIANTS;
        }

        double inverseOfnormPopulationProbabilities = 1.0d / this.findNormalPopulationProbabilities(geneMatches);
        
        logger.info("base genotype score (prescaled): {})", inverseOfnormPopulationProbabilities );
        //use sigmoid/logistic to scale between 0,1
        double scaled = (1/( 1 + Math.pow(Math.E,(-1*inverseOfnormPopulationProbabilities))));
        return new GenotypeSimilarityScore(scaled, geneMatches);
    }

    
    /**
     * Find matches arrising from genotype similarity
     * @param queryPatient patient queried with
     * @param nodePatient a patient from the local database
     * @return matches based on genomic features
     */
    protected List<GenomicFeatureMatch> findGenomicFeatureMatches(Patient queryPatient, Patient nodePatient) {

        List<GenomicFeature> queryPatientGenomicFeatures = queryPatient.getGenomicFeatures();
        List<GenomicFeature> nodePatientGenomicFeatures = nodePatient.getGenomicFeatures();

        //not the greatest test for equality out there
        if (queryPatient.getId().equals(nodePatient.getId())) {
            return samePatientGenomicFeatureMatches(queryPatientGenomicFeatures);
        }

        List<GenomicFeatureMatch> matches = new ArrayList<>();
        for (GenomicFeature queryPatientGenomicFeature : queryPatientGenomicFeatures) {
            String queryPatientEnsemblId = toEnsemblId(queryPatientGenomicFeature.getGene().get("id"));
            if (!queryPatientEnsemblId.equals("UNKNOWN")) {
                for (GenomicFeature nodePatientGenomicFeature : nodePatientGenomicFeatures) {
                    String nodePatientEnsemblId = toEnsemblId(nodePatientGenomicFeature.getGene().get("id"));
                    if (nodePatientEnsemblId.equals(queryPatientEnsemblId)) {
                        GenomicFeatureMatch match = new GenomicFeatureMatch(queryPatientGenomicFeature, nodePatientGenomicFeature);
                        matches.add(match);
                    }
                }
            }
        }
        return matches;
    }

    
    /**
     * Checking if the query is the same as the result patient
     */
    private List<GenomicFeatureMatch> samePatientGenomicFeatureMatches(List<GenomicFeature> genomicFeatures) {
        return genomicFeatures.stream()
                .filter(queryPatientGenomicFeature -> !toGeneSymbol(queryPatientGenomicFeature.getGene().get("id")).equals("UNKNOWN"))
                .map(queryPatientGenomicFeature -> new GenomicFeatureMatch(queryPatientGenomicFeature, queryPatientGenomicFeature))
                .collect(toList());
    }
    
    
    /**
     * Given a list of genomic feature matches, finds population frequencies of those combinations using gnomad
     * @param geneMatches a list of gene matches between a query patient and local-db patient
     * @return An average of allele frequencies between query and local 
     */
    private double findNormalPopulationProbabilities(List<GenomicFeatureMatch> geneMatches) {
        List<Double> alleleFredAvgs = new ArrayList<>();
        for (GenomicFeatureMatch gFeatureMatch : geneMatches) {
            Double localMatchAlleleFreq = 0d;
            Variant variant = gFeatureMatch.getNodeFeature().getVariant();
            if (!variant.isUnPopulated() && !variant.isPartiallyPopulated()) {
                localMatchAlleleFreq = findAlleFreqInNormPop(
                        variant.getReferenceName(),
                        variant.getStart(),
                        variant.getReferenceBases(),
                        variant.getAlternateBases());
                alleleFredAvgs.add(localMatchAlleleFreq);
            }
            //if unpopulated or for some reason gnomad search on variant went wrong, search on gene)
            if (variant.isUnPopulated() || variant.isPartiallyPopulated() || localMatchAlleleFreq == -1) {
                logger.info("skipping variant based gnomad search due to incomplete info, searching by gene name: {}", gFeatureMatch
                        .getGeneIdentifier());
                alleleFredAvgs.add(findAlleFreqInNormPop(gFeatureMatch.getGeneIdentifier()));
            }
        }

        return alleleFredAvgs.stream().mapToDouble(Double::valueOf).average().orElse(0d);
    }
    
        
    /**
     * Find the allele frequency of this variant, when complete variant information are given
     * @param variant
     * @return An allele frequency of this variant in a normal population
     */
    //TODO use @Cacheable
    private double findAlleFreqInNormPop(String chromosome, Long variantPos, String refBase, String altBase){
    	String cacheKey = chromosome + "." + variantPos.toString() + "." + refBase + "." + altBase;
    	if(GenotypeSimilarityServiceImpl.geneAlleleFreqCache.containsKey(cacheKey)){
    		double gInfo = (double)GenotypeSimilarityServiceImpl.geneAlleleFreqCache.get(cacheKey); 
    		logger.info("using cache for variant allele info: {}",gInfo);
    		return  gInfo;
    	}
    	StringBuilder payload = new StringBuilder();
    	payload.append("{\"query\": \"query{variant(id:\\\"");
    	payload.append(chromosome);
    	payload.append("-");
    	payload.append(Long.toString(variantPos));
    	payload.append("-");
    	payload.append(refBase);
    	payload.append("-");
    	payload.append(altBase);
    	payload.append("\\\", source: \\\"exome\\\"){allele_count,allele_num}}\"}");
    	logger.info("normal population allele frequency variant query is: {}", payload.toString());
    	String reply = this.httpCommunication.postToNonAuthenticatedHttpUrl("http://gnomad-api.broadinstitute.org", payload.toString());
    	Map<String,String> counts = this.parseGnomadVariantReply(reply);
    	if (counts.size()==0){
    		return -1d;
    	}
    	double normPopFreq = (Double.parseDouble(counts.get("allele_count")) / Double.parseDouble(counts.get("allele_num")));
    	logger.info("normal population allele frequency based on variant is: {}",normPopFreq);
    	GenotypeSimilarityServiceImpl.geneAlleleFreqCache.put(cacheKey, normPopFreq);
    	return normPopFreq;
    }
    
    
    /**
     * TODO: use ENSG here instead of HGNC due to ambigeouity of HGNC
     * Given a gene ID (HGNC) return a metric based on allele frequencies of all variants present
     * that represents the probability that variants in this gene may cause disease
     * @param hgncGeneId
     * @return a probability representing disease causality
     */
    private double findAlleFreqInNormPop(String gene){
        if(GenotypeSimilarityServiceImpl.geneAlleleFreqCache.containsKey(gene)){
    		double gInfo = GenotypeSimilarityServiceImpl.geneAlleleFreqCache.get(gene);
    		logger.info("using cache for gene (search by gene) allele info: {}",gInfo);
    		return  gInfo;
    	}
    	 StringBuilder payload = new StringBuilder();
    	 //payload.append("{\"query\": \"query{gene(gene_name: \\\"");  
    	 //payload.append(hgncGeneId);
    	 //payload.append("\\\") {gene_name,exome_variants {allele_freq}}}\"}");

    	 if (gene.indexOf("ENSG")==0){
    		 payload.append("{\"query\": \"query{gene(gene_id: \\\"");
    	 }
    	 else{
    		 payload.append("{\"query\": \"query{gene(gene_name: \\\"");
    	 }
    	   
    	 //payload.append("PCSK9");
    	 payload.append(gene);
    	 payload.append("\\\")");
    	 payload.append("{exacv1_constraint {pLI,syn_z,mis_z}}");
    	 payload.append("}\"}");
    	 
    	 System.out.println(">>>>>>>>"+payload.toString());
    	 logger.info("query used for getting allele frequency based on gene:  {}",payload.toString());
    	 String reply = this.httpCommunication.postToNonAuthenticatedHttpUrl("http://gnomad-api.broadinstitute.org",payload.toString());
    	 System.out.println(">>>>>>>>"+ reply);
    	 double freq = parseGnomadGeneLookupReply(reply);
    	 logger.info("normal population allele frequency based on gene ID {} is: {}",gene,freq);
    	 GenotypeSimilarityServiceImpl.geneAlleleFreqCache.put(gene, freq);
    	 return freq;
    }
    
    
 
    /**
     * Parses a reply from Gnomad gene service
     * @param reply A string reply in JSON format
     * @return a average of allele frequencies across the gene of various variants
     */
    private double parseGnomadGeneLookupReply(String reply) {
    	double avrgAlleleFreqForGene= 0d;
    	try{
	    	JSONParser parser = new JSONParser();
	    	JSONObject jsonObject = (JSONObject) parser.parse(reply);
	    	JSONObject dataObj = (JSONObject)jsonObject.get("data");
	    	JSONObject geneObj = (JSONObject)dataObj.get("gene");
	    	JSONArray exomeVariantsObj = (JSONArray)geneObj.get("exome_variants");
            double totalAlleleFreq = 0d;
            for (int i = 0; i < exomeVariantsObj.size(); i++) {
                JSONObject exomeVarObj = (JSONObject) exomeVariantsObj.get(i);
                try {
                    totalAlleleFreq += ((Double) exomeVarObj.get("allele_freq"));
                } catch (Exception e) {
                    e.getMessage();
                }
            }
            avrgAlleleFreqForGene = totalAlleleFreq / exomeVariantsObj.size();
        }
    	catch(Exception e){
    		logger.error("error parsing gnomad gene based query reply: {} for reply: {}", e.getMessage(),reply);
    	}
    	return  avrgAlleleFreqForGene; 
    }
    
    
    
    /**
     * Parses a reply from Gnomad variant service
     * @param reply A string reply in JSON format
     * @return a map of values returned back from gnomad
     */
    private Map<String,String> parseGnomadVariantReply(String reply) {
    	Map<String,String> parsed = new HashMap<>();
    	try{
	    	JSONParser parser = new JSONParser();
	    	JSONObject jsonObject = (JSONObject) parser.parse(reply);
	    	JSONObject dataObj = (JSONObject)jsonObject.get("data");
	    	JSONObject variantObj = (JSONObject)dataObj.get("variant");
	    	if (variantObj == null){
	    		logger.info("skipping parsing variant based gnomad query, results are unparsable: {}", reply);
	    		return parsed;
	    	}
	    	parsed.put("allele_count",Long.toString((Long)variantObj.get("allele_count")));
	    	parsed.put("allele_num",Long.toString((Long)variantObj.get("allele_num")));
    	}
    	catch(Exception e){
    		logger.error("error parsing gnomad variant based reply: {}, the reply was: {}, skipping to gene based query", e.getMessage(),reply);
    	}
    	return parsed;
    }

    
    private String toGeneSymbol(String identifier) {
    	//id: A gene symbol or identifier (mandatory): gene symbol from the HGNC database OR ensembl gene ID OR entrez gene ID
        if (geneSymbolToEnsemblId.containsKey(identifier)) {
            return identifier;
        }
        if (ensemblIdToGeneSymbol.containsKey(identifier)) {
            return ensemblIdToGeneSymbol.get(identifier);
        }
        //Entrez gene id? This is missing. TODO: make a GeneIdentifier class where these can be stored and compared.
        return "UNKNOWN";
    }

    private String toEnsemblId(String identifier) {
    	//id: A gene symbol or identifier (mandatory): gene symbol from the HGNC database OR ensembl gene ID OR entrez gene ID
      if (geneSymbolToEnsemblId.containsKey(identifier)) {
    	  return geneSymbolToEnsemblId.get(identifier);
      }
      if (ensemblIdToGeneSymbol.containsKey(identifier)) {
    	  return identifier;
      }
      	//Entrez gene id? This is missing. TODO: make a GeneIdentifier class where these can be stored and compared.
      return "UNKNOWN";
  }

	/**
	 * @param httpCommunication the httpCommunication to set
	 */
	public void setHttpCommunication(Communication httpCommunication) {
		this.httpCommunication = httpCommunication;
	}

	
    
}
