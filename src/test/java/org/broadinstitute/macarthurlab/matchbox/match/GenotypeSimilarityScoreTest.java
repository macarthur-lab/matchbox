package org.broadinstitute.macarthurlab.matchbox.match;

import org.broadinstitute.macarthurlab.matchbox.entities.GenomicFeature;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class GenotypeSimilarityScoreTest {

    @Test
    public void testNoMatch() {
        GenotypeSimilarityScore instance = new GenotypeSimilarityScore(0.0, Collections.emptyList());
        assertThat(instance.getScore(), equalTo(0.0  ));
        assertThat(instance.hasCommonGene(), is(false));
        assertThat(instance.getGenomicFeatureMatches().isEmpty(), is(true));
    }

    @Test
    public void testSingleGenotypeMatch() {
        GenomicFeature queryFeature = new GenomicFeature();
        GenomicFeature nodeFeature = new GenomicFeature();

        GenomicFeatureMatch match = new GenomicFeatureMatch(queryFeature, nodeFeature);

        GenotypeSimilarityScore instance = new GenotypeSimilarityScore(1.0, Collections.singletonList(match));
        assertThat(instance.getScore(), equalTo(1.0  ));
        assertThat(instance.hasCommonGene(), is(true));
        assertThat(instance.getGenomicFeatureMatches().isEmpty(), is(false));
        System.out.println(instance);
    }

}