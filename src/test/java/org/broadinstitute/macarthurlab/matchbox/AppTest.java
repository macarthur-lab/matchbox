package org.broadinstitute.macarthurlab.matchbox;


import com.github.fakemongo.Fongo;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
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
    
    /**
     * This test is WIP. Need to wire in test context
     */
    public void testFakeMongo(){
    	Fongo fongo = new Fongo("mongo server 1");
    	DB db = fongo.getDB("mydb");
    	DBCollection cltcn = db.getCollection("mycollection");
    	cltcn.insert(new BasicDBObject("name", "jon"));
    	System.out.println(cltcn.findOne());
    }

}