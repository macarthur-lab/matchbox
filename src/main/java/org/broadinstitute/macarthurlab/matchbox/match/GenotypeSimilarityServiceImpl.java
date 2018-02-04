/**
 * To represent a genetype match
 */
package org.broadinstitute.macarthurlab.matchbox.match;

import org.broadinstitute.macarthurlab.matchbox.entities.*;
import org.broadinstitute.macarthurlab.matchbox.network.Communication;
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
    
    //Perfect genotype match at the variant level and gene level
    private static final GenotypeSimilarityScore PERFECT_GENOTYPE_MATCH = new GenotypeSimilarityScore(1.0d, Collections.emptyList());
    
    //http://www.sequenceontology.org/browser/current_svn/term/SO:0001583
    private static final String MISSENSE_VARIANT_SOCCODE = "SO:0001583";
    
    //http://www.sequenceontology.org/browser/current_svn/term/SO:0001819
    private static final String SYNNONYMOUS_VARIANT_SOCCODE = "SO:0001819";
    
    //http://www.sequenceontology.org/browser/current_svn/term/SO:0002054
    private static final String LOSS_OF_FUNCTION_VARIANT_SOCCODE = "SO:0002054";
    
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

        if (geneMatches.size() == queryPatient.getGenomicFeatures().size() && 
        				queryPatient.getGenomicFeatures().size() == nodePatient.getGenomicFeatures().size() ){
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
        }
        double gnomadFrequency=this.findNormalPopulationProbabilitiesFromGnomad(geneMatches);
        //we are getting the inverse here to get across the inverse relationship to the strength of the match
        double inverseOfnormPopulationProbabilities = Math.pow(gnomadFrequency,-1);
        
        logger.info("base genotype score (prescaled): {})", inverseOfnormPopulationProbabilities );
        //use sigmoid/logistic to scale between 0,1
        double scaled = (1/( 1 + Math.pow(Math.E,(-1*inverseOfnormPopulationProbabilities))));
        
        logger.info("final genotype score is: {}", scaled);
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
     * Checking if the query is the same as the result patient. This is complicated given the nodes do not
     * share the real name of the patients, and users sometimes submit the same patient to multiple nodes.
     * We have to be careful not to negate a genuine good match.
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
    private double findNormalPopulationProbabilitiesFromGnomad(List<GenomicFeatureMatch> geneMatches) {
    	List<Double> alleleFreqs = new ArrayList<>();
        for (GenomicFeatureMatch gFeatureMatch : geneMatches) {
        	double constraintScore = findNormPopGeneConstraintScore(gFeatureMatch.getGeneIdentifier(),gFeatureMatch.getNodeFeature().getType().get("id"));
            Double localMatchAlleleFreq = 0d;
            Variant variant = gFeatureMatch.getNodeFeature().getVariant();
            if (!variant.isUnPopulated() && !variant.isPartiallyPopulated()) {
                localMatchAlleleFreq = findAlleFreqInNormPop(
                        variant.getReferenceName(),
                        variant.getStart(),
                        variant.getReferenceBases(),
                        variant.getAlternateBases());
                alleleFreqs.add(localMatchAlleleFreq * constraintScore);
            }
            //if variant information is un-populated or for some reason gnomad search on variant failed (localMatchAlleleFreq is -1 in that case), 
            //search on only gene name or id(ENSG preferred)
            if (variant.isUnPopulated() || variant.isPartiallyPopulated() || localMatchAlleleFreq == -1) {
                logger.info("skipping variant based gnomad search due to incomplete info, searching by gene name: {}", 
                																			gFeatureMatch.getGeneIdentifier());
                //using the local/node match patients type since external nodes do not always give this.  
                //Given incomplete variant information, using constraint scores of gene instead of allele frequencies
                alleleFreqs.add(constraintScore);
            }
        }
        Double score=alleleFreqs.get(0);
        int i=1;
        while (i>alleleFreqs.size()){
        	score = score * alleleFreqs.get(i);
        	i++;
        }
        logger.info("gnomad frequencies (only) based intermediate score is: {} ",score);
        return score;
    }
    
        
    /**
     * Find the allele frequency of this variant, when complete variant information are given
     * @param chromosome chromosome of variant
     * @param variantPos position of variant
     * @param refBase reference base
     * @param altBase alternate base
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
    	payload.append("\\\", source: \\\"exome\\\"){allele_freq}}\"}");
    	logger.info("++++>normal population allele frequency variant query is: {}", payload.toString());
    	String reply = this.httpCommunication.postToNonAuthenticatedHttpUrl("http://gnomad-api.broadinstitute.org", payload.toString());
    	Map<String,Double> counts = this.parseGnomadVariantReply(reply);
    	if (counts.size()==0){
    		return -1d;
    	}
    	logger.info("normal population allele frequency based on variant is: {}",counts.get("allele_freq"));
    	GenotypeSimilarityServiceImpl.geneAlleleFreqCache.put(cacheKey, counts.get("allele_freq"));
    	return counts.get("allele_freq");
    }
    
    
    /**
     * Given a gene ID (HGNC or ENSG) return a metric based constraint scores of that gene. 
     * If no constraint score are found: 1.0 is returned so it doesn't affect any other calculations downstream TODO check this!
     * SO code information:
     * http://www.sequenceontology.org/browser/current_svn/term/SO:0001583
     * @param HGNC name or ENSG or ID (method differentiates automatically between the two)
     * @param variant type as SO code
     * @return an appropriate constraint score
     */
    private double findNormPopGeneConstraintScore(String gene, String typeAsSOCode){
        if(GenotypeSimilarityServiceImpl.geneAlleleFreqCache.containsKey(gene)){
    		double gInfo = GenotypeSimilarityServiceImpl.geneAlleleFreqCache.get(gene);
    		logger.info("using cache for gene (search by gene) allele info: {}",gInfo);
    		return  gInfo;
    	}
    	 StringBuilder payload = new StringBuilder();
    	 if (gene.indexOf("ENSG")==0){
    		 payload.append("{\"query\": \"query{gene(gene_id: \\\"");
    	 }
    	 else{
    		 payload.append("{\"query\": \"query{gene(gene_name: \\\"");
    	 }
    	 payload.append(gene);
    	 payload.append("\\\")");
    	 payload.append("{exacv1_constraint {pLI,syn_z,mis_z}}");
    	 payload.append("}\"}");
    	 logger.info("query used for getting allele frequency based on gene:  {}, for type: {} : {}",gene, typeAsSOCode,payload.toString());
    	 String reply = this.httpCommunication.postToNonAuthenticatedHttpUrl("http://gnomad-api.broadinstitute.org",payload.toString());
    	 Map<String,Double> freq = parseGnomadGeneLookupReply(reply);
    	 logger.info("normal population constraint scores based on gene ID {} are:{} , {} , {}",gene, freq.get("pLI"),freq.get("syn_z"),freq.get("mis_z"));
    	 
    	 if (freq.size()>0){
	    	 if (typeAsSOCode.equals(MISSENSE_VARIANT_SOCCODE)){
	    		 GenotypeSimilarityServiceImpl.geneAlleleFreqCache.put(gene, freq.get("mis_z"));
	    		 return freq.get("mis_z");
	    	 }
	    	 else if(typeAsSOCode.equals(SYNNONYMOUS_VARIANT_SOCCODE)){
	    		 GenotypeSimilarityServiceImpl.geneAlleleFreqCache.put(gene, freq.get("syn_z"));
	    		 return freq.get("syn_z");
	    	 }
	    	 else if(typeAsSOCode.equals(LOSS_OF_FUNCTION_VARIANT_SOCCODE)){
	    		 GenotypeSimilarityServiceImpl.geneAlleleFreqCache.put(gene, freq.get("pLI"));
	    		 return freq.get("pLI");
	    	 }
	    	 else{
	    		 //if no type is given OR other type is given, we are going to use missense, and value is not cached
	    		 return freq.get("mis_z");
	    	 }
    	 }
    	 logger.info("no constrain information was found, so simply not using it via equaling it to '1.0d'");
    	 return 1.0d; //sentinel placeholder when information is missing in gnomad
    }
    
    
 
    /**
     * Parses a reply from Gnomad gene service and returns empty map if no values are returned from Gnomad API
     * @param reply from gnomad API: A string reply in JSON format
     * @return a parsed map of constraint values from Gnomad API
     */
    private Map<String,Double> parseGnomadGeneLookupReply(String reply) {
    	Map<String,Double> constraintScore= new HashMap<>();
    	try{
	    	JSONParser parser = new JSONParser();
	    	JSONObject jsonObject = (JSONObject) parser.parse(reply);
	    	JSONObject dataObj = (JSONObject)jsonObject.get("data");
	    	JSONObject geneObj = (JSONObject)dataObj.get("gene");
	    	JSONObject exomeVariantsObj = (JSONObject)geneObj.get("exacv1_constraint");
	    	if (exomeVariantsObj != null){
	            constraintScore.put("pLI", (Double)exomeVariantsObj.get("pLI"));
	            constraintScore.put("syn_z", (Double)exomeVariantsObj.get("syn_z"));
	            constraintScore.put("mis_z", (Double)exomeVariantsObj.get("mis_z"));
	    	}
        }
    	catch(Exception e){
    		logger.error("error parsing gnomad gene based query reply: {} for reply: {}", e.getMessage(),reply);
    	}
    	return constraintScore;
    }
    
    
    
    /**
     * Parses a reply from Gnomad variant service and return a map of the results
     * @param reply A string reply in JSON format
     * @return a map of values returned back from gnomad
     */
    private Map<String,Double> parseGnomadVariantReply(String reply) {
    	logger.info("reply back from gnomad for variant based allele frequency: {}",reply);
    	Map<String,Double> parsed = new HashMap<>();
    	try{
	    	JSONParser parser = new JSONParser();
	    	JSONObject jsonObject = (JSONObject) parser.parse(reply);
	    	JSONObject dataObj = (JSONObject)jsonObject.get("data");
	    	JSONObject variantObj = (JSONObject)dataObj.get("variant");
	    	if (variantObj == null){
	    		logger.info("skipping parsing variant based gnomad query, results are unparsable: {}", reply);
	    		return parsed;
	    	}
	    	parsed.put("allele_freq",(Double)variantObj.get("allele_freq"));
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
