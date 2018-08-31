package org.broadinstitute.macarthurlab.matchbox;


import org.broadinstitute.macarthurlab.matchbox.metrics.PrivilegedMetricServiceImpl;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Unit test for simple App.
 */

public class AppTest {

    /**
     * Test counting distinct genes for metrics
     */
    @Test
    public void testMetricsDistinctGenes(){
    	PrivilegedMetricServiceImpl metric = new PrivilegedMetricServiceImpl();  
    	Map<String,Integer> counts = metric.countGenesInSystem(TestData.getTwoTestPatients());
    	assertThat(counts.size(), equalTo(2));
    }
}