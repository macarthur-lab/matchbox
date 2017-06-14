package org.broadinstitute.macarthurlab.matchbox.match;

import org.broadinstitute.macarthurlab.matchbox.entities.GenomicFeature;
import org.broadinstitute.macarthurlab.matchbox.entities.Variant;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class GenomicFeatureMatchTest {

    @Test
    public void testNoFeatureMatches() {
        GenomicFeature queryFeature = new GenomicFeature();
        GenomicFeature nodeFeature = new GenomicFeature();

        GenomicFeatureMatch match = new GenomicFeatureMatch(queryFeature, nodeFeature);
        assertThat(match.getGeneIdentifier(), equalTo(""));
        assertThat(match.hasTypeMatch(), is(false));
        assertThat(match.hasZygosityMatch(), is(false));
    }

    @Test
    public void testFeatureMatchesGeneIdOnly() {
        Map<String, String> geneIdentifier = new HashMap<>();
        geneIdentifier.put("id", "ADH1A");
        GenomicFeature queryFeature = new GenomicFeature(geneIdentifier, new Variant(), -1L, Collections.emptyMap());
        GenomicFeature nodeFeature = new GenomicFeature(geneIdentifier, new Variant(), -1L, Collections.emptyMap());

        GenomicFeatureMatch match = new GenomicFeatureMatch(queryFeature, nodeFeature);
        assertThat(match.getGeneIdentifier(), equalTo("ADH1A"));
        assertThat(match.hasTypeMatch(), is(false));
        assertThat(match.hasZygosityMatch(), is(false));
    }

    @Test
    public void testZygosityNotSetNoMatch() {
        Map<String, String> geneIdentifier = new HashMap<>();
        geneIdentifier.put("id", "ADH1A");
        GenomicFeature queryFeature = new GenomicFeature(geneIdentifier, new Variant(), -1L, Collections.emptyMap());
        GenomicFeature nodeFeature = new GenomicFeature(geneIdentifier, new Variant(), 1L, Collections.emptyMap());

        GenomicFeatureMatch match = new GenomicFeatureMatch(queryFeature, nodeFeature);
        assertThat(match.hasZygosityMatch(), is(false));
    }

    @Test
    public void testZygositySetNoMatch() {
        Map<String, String> geneIdentifier = new HashMap<>();
        geneIdentifier.put("id", "ADH1A");
        GenomicFeature queryFeature = new GenomicFeature(geneIdentifier, new Variant(), 2L, Collections.emptyMap());
        GenomicFeature nodeFeature = new GenomicFeature(geneIdentifier, new Variant(), 1L, Collections.emptyMap());

        GenomicFeatureMatch match = new GenomicFeatureMatch(queryFeature, nodeFeature);
        assertThat(match.hasZygosityMatch(), is(false));
    }

    @Test
    public void testZygositySetMatch() {
        Map<String, String> geneIdentifier = new HashMap<>();
        geneIdentifier.put("id", "ADH1A");
        GenomicFeature queryFeature = new GenomicFeature(geneIdentifier, new Variant(), 2L, Collections.emptyMap());
        GenomicFeature nodeFeature = new GenomicFeature(geneIdentifier, new Variant(), 2L, Collections.emptyMap());

        GenomicFeatureMatch match = new GenomicFeatureMatch(queryFeature, nodeFeature);
        assertThat(match.hasZygosityMatch(), is(true));
    }

    @Test
    public void testTypeNotSetNoMatch() {
        Map<String, String> geneIdentifier = new HashMap<>();
        geneIdentifier.put("id", "ADH1A");

        Map<String, String> type = new HashMap<>();
        type.put("id", "SO:0001576");


        GenomicFeature queryFeature = new GenomicFeature(geneIdentifier, new Variant(), -1L, Collections.emptyMap());
        GenomicFeature nodeFeature = new GenomicFeature(geneIdentifier, new Variant(), -1L, type);

        GenomicFeatureMatch match = new GenomicFeatureMatch(queryFeature, nodeFeature);
        assertThat(match.hasTypeMatch(), is(false));
        assertThat(match.getQuerySequenceOntologyId(), equalTo(""));
    }

    @Test
    public void testTypeSetNoMatch() {
        Map<String, String> geneIdentifier = new HashMap<>();
        geneIdentifier.put("id", "ADH1A");

        Map<String, String> querySoId = new HashMap<>();
        querySoId.put("id", "SO:0004321");

        Map<String, String> nodeSoId = new HashMap<>();
        nodeSoId.put("id", "SO:0001576");

        GenomicFeature queryFeature = new GenomicFeature(geneIdentifier, new Variant(), -1L, querySoId);
        GenomicFeature nodeFeature = new GenomicFeature(geneIdentifier, new Variant(), -1L, nodeSoId);

        GenomicFeatureMatch match = new GenomicFeatureMatch(queryFeature, nodeFeature);
        assertThat(match.hasTypeMatch(), is(false));
        assertThat(match.getQuerySequenceOntologyId(), equalTo("SO:0004321"));
    }

    @Test
    public void testTypeSetMatch() {
        Map<String, String> geneIdentifier = new HashMap<>();
        geneIdentifier.put("id", "ADH1A");

        Map<String, String> type = new HashMap<>();
        type.put("id", "SO:0001576");

        GenomicFeature queryFeature = new GenomicFeature(geneIdentifier, new Variant(), -1L, type);
        GenomicFeature nodeFeature = new GenomicFeature(geneIdentifier, new Variant(), -1L, type);

        GenomicFeatureMatch match = new GenomicFeatureMatch(queryFeature, nodeFeature);
        assertThat(match.hasTypeMatch(), is(true));
        assertThat(match.getQuerySequenceOntologyId(), equalTo("SO:0001576"));
    }

}