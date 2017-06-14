package org.broadinstitute.macarthurlab.matchbox.match;

import org.broadinstitute.macarthurlab.matchbox.entities.GenomicFeature;

import java.util.Objects;

/**
 * Simple container for storing matched GenomicFeature objects.
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class GenomicFeatureMatch {

    private final GenomicFeature queryFeature;
    private final GenomicFeature nodeFeature;

    public GenomicFeatureMatch(GenomicFeature queryFeature, GenomicFeature nodeFeature) {
        this.queryFeature = queryFeature;
        this.nodeFeature = nodeFeature;
    }

    public String getGeneIdentifier() {
        return queryFeature.getGene().getOrDefault("id", "");
    }

    public boolean hasZygosityMatch() {
        //zygosity: <number> (1 for heterozygous or hemizygous, 2 for homozygous) (optional)
        //GenomicFeature has -1L as the default instead of 0L
        return !queryFeature.getZygosity().equals(-1L) && queryFeature.getZygosity().equals(nodeFeature.getZygosity());
    }

    public boolean hasTypeMatch() {
//        type: the effect of the mutation. This enables describing the broad category of cDNA effect predicted to result from a mutation to improve matchmaking, without necessarily disclosing the actual mutation. (optional)
//                id: a Sequence Ontology term identifier ("SO:#######"). This will usually (but not necessarily) be a descendant of SO:0001576 [transcript variant]. (mandatory, if type is provided)
        String querySoTermId = queryFeature.getType().getOrDefault("id", "");
        String nodeSoTermId = nodeFeature.getType().getOrDefault("id", "");

        return !querySoTermId.isEmpty() && querySoTermId.equals(nodeSoTermId);
    }

    public String getQuerySequenceOntologyId() {
        return queryFeature.getType().getOrDefault("id", "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenomicFeatureMatch match = (GenomicFeatureMatch) o;
        return Objects.equals(queryFeature, match.queryFeature) &&
                Objects.equals(nodeFeature, match.nodeFeature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(queryFeature, nodeFeature);
    }

    @Override
    public String toString() {
        return "GenomicFeatureMatch{" +
                "queryFeature=" + queryFeature +
                ", nodeFeature=" + nodeFeature +
                '}';
    }

}
