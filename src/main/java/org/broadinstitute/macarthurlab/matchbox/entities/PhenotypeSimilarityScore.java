package org.broadinstitute.macarthurlab.matchbox.entities;

import java.util.List;
import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class PhenotypeSimilarityScore {

    private final double score;
    private final List<String> phenotypeFeatureMatches;

    public PhenotypeSimilarityScore(double score, List<String> phenotypeFeatureMatches) {
        this.score = score;
        this.phenotypeFeatureMatches = phenotypeFeatureMatches;
    }

    public double getScore() {
        return score;
    }

    public List<String> getPhenotypeFeatureMatches() {
        return phenotypeFeatureMatches;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhenotypeSimilarityScore that = (PhenotypeSimilarityScore) o;
        return Double.compare(that.score, score) == 0 &&
                Objects.equals(phenotypeFeatureMatches, that.phenotypeFeatureMatches);
    }

    @Override
    public int hashCode() {
        return Objects.hash(score, phenotypeFeatureMatches);
    }

    @Override
    public String toString() {
        return "PhenotypeSimilarityScore{" +
                "score=" + score +
                ", phenotypeFeatureMatches=" + phenotypeFeatureMatches +
                '}';
    }
}
