/**
 * To represent a genetype match
 */
package org.broadinstitute.macarthurlab.matchbox.match;

import org.broadinstitute.macarthurlab.matchbox.entities.GenomicFeature;
import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * @author harindra
 */
@Service
public class GenotypeSimilarityServiceImpl implements GenotypeSimilarityService {

    private static final Logger logger = LoggerFactory.getLogger(GenotypeSimilarityServiceImpl.class);

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
     * @param nodePatients     a list of patients to rank
     * @return Sends back a list of scores for each patient based on genotype. Order matches input list
     */
    public List<GenotypeSimilarityScore> scoreGenotypes(Patient queryPatient, List<Patient> nodePatients) {
        List<GenotypeSimilarityScore> patientGenotypeRankingScores = new ArrayList<>();
        for (Patient nodePatient : nodePatients) {
            GenotypeSimilarityScore genotypeSimilarity = scoreGenotypes(queryPatient, nodePatient);
            logger.debug("{}-{} genotype similarity score = {}", queryPatient.getId(), nodePatient.getId(), genotypeSimilarity);
            patientGenotypeRankingScores.add(genotypeSimilarity);
        }
        return patientGenotypeRankingScores;
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
            //don't overly penalize these - maybe the phenotype score is high which could lead to a good match.
            return new GenotypeSimilarityScore(0.6, Collections.emptyList());
        }
        List<GenomicFeatureMatch> geneMatches = findGenomicFeatureMatches(queryPatient, nodePatient);

        //TODO: each GenomicFeatureMatch should have its own score based on the variant, zygosity and SO code (as per current implementation)
        //for the final GenotypeSimilarityScore we'll return the top-ranked individual score.
        double zygosityScore = calculateZygosityScore(geneMatches);
        double typeScore = calculateVariantEffectScore(geneMatches);
        logger.debug("Zygosity: {} Variant Effect: {}", zygosityScore, typeScore);
        //return a maximum of 1.0
        double score = Math.min(zygosityScore + typeScore, 1.0);
        return new GenotypeSimilarityScore(score, geneMatches);
    }

    List<GenomicFeatureMatch> findGenomicFeatureMatches(Patient queryPatient, Patient nodePatient) {

        List<GenomicFeature> queryPatientGenomicFeatures = queryPatient.getGenomicFeatures();
        List<GenomicFeature> nodePatientGenomicFeatures = nodePatient.getGenomicFeatures();

        //not the greatest test for equality out there
        if (queryPatient.getId().equals(nodePatient.getId())) {
            return samePatientGenomicFeatureMatches(queryPatientGenomicFeatures);
        }

        List<GenomicFeatureMatch> matches = new ArrayList<>();
        for (GenomicFeature queryPatientGenomicFeature : queryPatientGenomicFeatures) {
            String queryPatientGeneSymbol = toGeneSymbol(queryPatientGenomicFeature.getGene().get("id"));
            if (!queryPatientGeneSymbol.equals("UNKNOWN")) {
                for (GenomicFeature nodePatientGenomicFeature : nodePatientGenomicFeatures) {
                    String nodePatientGeneSymbol = toGeneSymbol(nodePatientGenomicFeature.getGene().get("id"));
                    if (nodePatientGeneSymbol.equals(queryPatientGeneSymbol)) {
                        GenomicFeatureMatch match = new GenomicFeatureMatch(queryPatientGenomicFeature, nodePatientGenomicFeature);
                        matches.add(match);
                    }
                }
            }
        }
        return matches;
    }

    private List<GenomicFeatureMatch> samePatientGenomicFeatureMatches(List<GenomicFeature> genomicFeatures) {
        return genomicFeatures.stream()
                .filter(queryPatientGenomicFeature -> !toGeneSymbol(queryPatientGenomicFeature.getGene().get("id")).equals("UNKNOWN"))
                .map(queryPatientGenomicFeature ->new GenomicFeatureMatch(queryPatientGenomicFeature, queryPatientGenomicFeature))
                .collect(toList());
    }

    /**
     * Access zygosity's affect on the score. If zygosity is the same in at least
     * one of the common genes, 0.5 is returned.
     *
     * @param genomicFeatureMatches the matching genomic features
     * @return A score (0.5 is returned if there is a match in zygositys)
     */
    private double calculateZygosityScore(List<GenomicFeatureMatch> genomicFeatureMatches) {
        for (GenomicFeatureMatch match : genomicFeatureMatches) {
            if (match.hasZygosityMatch()) {
                logger.debug("Zygosity match: {}", match.hasZygosityMatch());
                return 0.5;
            }
        }
        return 0.0;
    }

    /**
     * Generates a score based on variant positions inside a common gene.
     *
     * Returns a 0.5 if a perfect match
     *
     * @param genomicFeatureMatches patient in the node
     * @return Returns a representative metric
     */
    private double calculateVariantEffectScore(List<GenomicFeatureMatch> genomicFeatureMatches) {
        double score = 0.0;
        int similarCount = 0;
        for (GenomicFeatureMatch match : genomicFeatureMatches) {
            logger.debug("Checking {} type {}", match.getGeneIdentifier(), match.getQuerySequenceOntologyId());
            if (match.hasTypeMatch()) {
                logger.debug("SO term match: {}", match.hasTypeMatch());
                similarCount++;
            }
            //UP the score IF it is a HIGH danger variant type?
            if (highlyDeleteriousSoCodes().contains(match.getQuerySequenceOntologyId())) {
                score += 0.1;
            }
        }
        logger.debug("Matching SO terms: {} Matching genes: {}", similarCount, genomicFeatureMatches.size());
        if (similarCount == genomicFeatureMatches.size()) {
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
    private List<String> highlyDeleteriousSoCodes() {
        List<String> codes = new ArrayList<>();
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
        codes.add("SO:0001619");
        codes.add("SO:0001575");
        codes.add("SO:0001619");
        return codes;
    }

}
