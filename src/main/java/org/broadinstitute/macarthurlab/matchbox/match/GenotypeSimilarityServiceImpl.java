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
 */
@Service
public class GenotypeSimilarityServiceImpl implements GenotypeSimilarityService {

    private static final Logger logger = LoggerFactory.getLogger(GenotypeSimilarityServiceImpl.class);

    //TODO - this needs removing
    @Autowired
    private MongoOperations operator;

    private final Map<String, String> geneSymbolToEnsemblId;
    private final Map<String, String> ensemblIdToGeneSymbol;

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
     * Ranks a patient list by their genotype similarity to a query patient. Since Genotype
     * is half the score (other half is phenotype rank), this section can given a 0.5 as a perfect hit.
     *
     * @param queryPatient a target patient to rank against
     * @param patients     a list of patients to rank
     * @return Sends back a list of scores for each patient based on genotype. Order matches input list
     */
    public List<Double> scoreGenotypes(Patient queryPatient, List<Patient> patients) {
        List<Double> patientGenotypeRankingScores = new ArrayList<>();
        for (Patient patient : patients) {
            double genotypeSimilarity = getGenotypeSimilarity(patient, queryPatient);
            logger.debug("{}-{} genotype similarity score = {}", queryPatient.getId(), patient.getId(), genotypeSimilarity);
            patientGenotypeRankingScores.add(genotypeSimilarity);
        }
        return patientGenotypeRankingScores;
    }


    /**
     * Calculates a metric on similarity. Returns 0.5 for a perfect match.
     *
     * @param nodePatient patient in the node
     * @param queryPatient external query patient
     * @return a representative number (described above)
     */
    private double getGenotypeSimilarity(Patient nodePatient, Patient queryPatient) {
        if (nodePatient.getGenomicFeatures().isEmpty() || queryPatient.getGenomicFeatures().isEmpty()) {
            //don't overly penalize these - maybe the phenotype score is high which could lead to a good match.
            return 0.6;
        }

        List<String> commonGenes = findCommonGenes(nodePatient, queryPatient);
        double zygosityScore = getZygosityScore(nodePatient, queryPatient, commonGenes);
        double typeScore = getTypeScore(nodePatient, queryPatient, commonGenes);
        //return a maximum of 1.0
        return Math.min(zygosityScore + typeScore, 1.0);
    }

    /**
     * Returns a list of common genes
     *
     * @param p1 patient
     * @param p2 patient
     */
    List<String> findCommonGenes(Patient p1, Patient p2) {
        List<String> p1Genes = p1.getGenomicFeatures().stream()
                .map(k -> toGeneSymbol(k.getGene().get("id")))
                .filter(geneSymbol -> !geneSymbol.equals("UNKNOWN"))
                .collect(Collectors.toList());

        List<String> p2Genes = p2.getGenomicFeatures().stream()
                .map(k -> toGeneSymbol(k.getGene().get("id")))
                .filter(geneSymbol -> !geneSymbol.equals("UNKNOWN"))
                .collect(Collectors.toList());

        return p1Genes.stream()
                .filter(p2Genes::contains)
                .collect(Collectors.toList());
    }

    /**
     * Access zygosity's affect on the score. If zygosity is the same in at least
     * one of the common genes, 0.5 is returned.
     *
     * @param nodePatient patient in the node
     * @param queryPatient external query patient
     * @return A score (0.5 is returned if there is a match in zygositys)
     */
    private double getZygosityScore(Patient nodePatient, Patient queryPatient, List<String> commonGenes) {
        for (GenomicFeature gf : nodePatient.getGenomicFeatures()) {
            String geneId = toGeneSymbol(gf.getGene().get("id"));
            if (commonGenes.contains(geneId)) {
                for (GenomicFeature queryPgf : queryPatient.getGenomicFeatures()) {
                    if (toGeneSymbol(queryPgf.getGene().get("id")).equals(geneId)) {
                        //basically if the patients both have a gene in common with the same zygosity, this is a good match
                        //make the code read accordingly.
                        if (gf.getZygosity() == (long) queryPgf.getZygosity()) {
                            return 0.5;
                        }
                    }
                }
            }
        }
        return 0.005;
    }


    /**
     * Generates a score based on variant positions inside a common gene.
     *
     * Returns a 0.5 if a perfect match
     *
     * @param nodePatient patient in the node
     * @param queryPatient external query patient
     * @return Returns a representative metric
     */
    private double getTypeScore(Patient nodePatient, Patient queryPatient, List<String> commonGenes) {
        double score = 0.005;
        /**
         * make map of query relevant gene-name/symbol:variant-type (SO code)
         * TODO:translate all to ensembl before comparison
         */
        Map<String, String> queryGenomicFeatures = new HashMap<>();
        queryPatient.getGenomicFeatures().forEach(k -> {
            String geneSymbol = toGeneSymbol(k.getGene().get("id"));
            if (commonGenes.contains(geneSymbol)) {
                //'type' is optional so check its there to avoid a null pointer later on
                if (k.getType().containsKey("id")) {
                    String type = k.getType().get("id");
                    queryGenomicFeatures.put(geneSymbol, type);
                }
            }
        });
        //now see if the match has these SO codes in common
        int similarCount = 0;
        for (GenomicFeature nodeGenomicFeature : nodePatient.getGenomicFeatures()) {
            String nodePatientGeneSymbol = toGeneSymbol(nodeGenomicFeature.getGene().get("id"));
            if (queryGenomicFeatures.containsKey(nodePatientGeneSymbol)) {
                String soTermId = nodeGenomicFeature.getType().getOrDefault("id", "");
                if (queryGenomicFeatures.get(nodePatientGeneSymbol).equals(soTermId)) {
                    similarCount += 1;
                }
                //UP the score IF it is a HIGH danger variant type?
                if (getSOCodes().contains(soTermId)) {
                    score += 0.1;
                }
            }
        }
        if (similarCount == queryGenomicFeatures.size()) {
            score += 0.5;
        }
        return score;
    }

    private String toGeneSymbol(String identifier) {
//        id: A gene symbol or identifier (mandatory): gene symbol from the HGNC database OR ensembl gene ID OR entrez gene ID
        if (geneSymbolToEnsemblId.containsKey(identifier)) {
            return identifier;
        }
        if (ensemblIdToGeneSymbol.containsKey(identifier)) {
            return ensemblIdToGeneSymbol.get(identifier);
        }
        //Entrez gene id? This is missing. TODO: make a GeneIdentifier class where these can be stored and compared.
        return "UNKNOWN";
    }

    /**
     * TODO: abstract this to config file
     * Get a list of SO codes of mutations. gotten from
     * //http://doc-openbio.readthedocs.io/projects/jannovar/en/master/var_effects.html
     *
     * @return a list of SO codes
     */
    private List<String> getSOCodes() {
        List<String> codes = new ArrayList<String>();
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
     * Search for matching patients using GenomicFeatures
     * 1. Considers it a match if they have AT LEAST 1 gene in common
     * 2. So far only supports gene symbol and ensembl ID for gene ID field
     */
    //TODO: Check this really isn't required and remove
    private List<Patient> searchByGenomicFeatures(Patient patient) {
        List<Patient> results = new ArrayList<>();

        StringBuilder geneSymbolQuery = new StringBuilder("{'genomicFeatures.gene.id':{$in:[");
        StringBuilder ensemblIdQuery = new StringBuilder("{'genomicFeatures.gene.id':{$in:[");

        int i = 0;
        for (GenomicFeature genomicFeature : patient.getGenomicFeatures()) {
            String id = genomicFeature.getGene().get("id");
            String ensemblId = "";
            String geneId = "";
            if (geneSymbolToEnsemblId.containsKey(id)) {
                geneId = id;
                ensemblId = geneSymbolToEnsemblId.get(id);
            }
            if (ensemblIdToGeneSymbol.containsKey(id)) {
                ensemblId = id;
                geneId = ensemblIdToGeneSymbol.get(id);
            }

            geneSymbolQuery.append("'" + geneId + "'");
            if (i < patient.getGenomicFeatures().size() - 1) {
                geneSymbolQuery.append(",");
            }

            ensemblIdQuery.append("'" + ensemblId + "'");
            if (i < patient.getGenomicFeatures().size() - 1) {
                ensemblIdQuery.append(",");
            }
            if (!geneSymbolToEnsemblId.containsKey(id) &&
                    !ensemblIdToGeneSymbol.containsKey(id)) {
                String mesg = "could not identify provided gene ID as ensmbl or hgnc:" + id;
                logger.error(mesg);
            }
            i++;
        }
        geneSymbolQuery.append("]}}");
        ensemblIdQuery.append("]}}");

        logger.info("{}", geneSymbolQuery);
        logger.info("{}", ensemblIdQuery);

        BasicQuery qGeneId = new BasicQuery(geneSymbolQuery.toString());
        List<Patient> psGeneId = operator.find(qGeneId, Patient.class);
        Set<String> usedIds = new HashSet<String>();
        for (Patient p : psGeneId) {
            results.add(p);
            usedIds.add(p.getId());
        }
        BasicQuery qEnsemblId = new BasicQuery(ensemblIdQuery.toString());
        List<Patient> psEnsembl = operator.find(qEnsemblId, Patient.class);
        for (Patient p : psEnsembl) {
            if (!usedIds.contains(p.getId())) {
                results.add(p);
            }
        }
        logger.info("{}", results);
        return results;
    }


}
