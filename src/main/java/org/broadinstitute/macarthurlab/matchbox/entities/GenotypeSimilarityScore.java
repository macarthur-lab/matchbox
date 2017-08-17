package org.broadinstitute.macarthurlab.matchbox.entities;

import java.util.List;
import java.util.Objects;

/**
 * Provides information on how a query patient genotype matches that of a node patient.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class GenotypeSimilarityScore {

    private final double score;
    private final List<GenomicFeatureMatch> genomicFeatureMatches;

    public GenotypeSimilarityScore(double score, List<GenomicFeatureMatch> genomicFeatureMatches) {
        this.score = score;
        this.genomicFeatureMatches = genomicFeatureMatches;
    }

    public double getScore() {
        return score;
    }

    public List<GenomicFeatureMatch> getGenomicFeatureMatches() {
        return genomicFeatureMatches;
    }

    public boolean hasCommonGene() {
        return !genomicFeatureMatches.isEmpty();
    }
    
    /**
     * Checks if at least one match is the same zygosity
     */
    public boolean hasAtleastOneGeneInCommonWithSameZygosity(){
    	for (GenomicFeatureMatch gMatch : this.getGenomicFeatureMatches() ){
    		if (gMatch.hasZygosityMatch()){
    			return true;
    		}
    	}
    	return false;
    }
    
    
    /**
     * Checks if at least one match is the same type
     */
    public boolean hasAtleastOneGeneInCommonWithSameType(){
    	for (GenomicFeatureMatch gMatch : this.getGenomicFeatureMatches() ){
    		if (gMatch.hasTypeMatch()){
    			return true;
    		}
    	}
    	return false;
    }
    
    
    /**
     * Checks if at least one match is the same variant position
     */
    public boolean hasAtleastOneGeneInCommonWithSameVariantPosition(){
    	for (GenomicFeatureMatch gMatch : this.getGenomicFeatureMatches() ){
    		if (gMatch.hasSameVariantPosition()){
    			return true;
    		}
    	}
    	return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenotypeSimilarityScore that = (GenotypeSimilarityScore) o;
        return Double.compare(that.score, score) == 0 &&
                Objects.equals(genomicFeatureMatches, that.genomicFeatureMatches);
    }

    @Override
    public int hashCode() {
        return Objects.hash(score, genomicFeatureMatches);
    }

    @Override
    public String toString() {
        return "GenotypeSimilarityScore{" +
                "score=" + score +
                ", genomicFeatureMatches=" + genomicFeatureMatches +
                '}';
    }
}
