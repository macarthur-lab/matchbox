package org.broadinstitute.macarthurlab.matchbox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.broadinstitute.macarthurlab.matchbox.entities.GenomicFeature;
import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.broadinstitute.macarthurlab.matchbox.entities.PhenotypeFeature;
import org.broadinstitute.macarthurlab.matchbox.entities.Variant;
import org.broadinstitute.macarthurlab.matchbox.match.GenotypeMatch;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase
{
	private GenotypeMatch genotypeMatch = new GenotypeMatch();
	
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
    	
    	Map<String, String> gene1 = new HashMap<String,String>();
    	gene1.put("id","ENSG00000178104");

    	Map<String, String> gene2 = new HashMap<String,String>();
    	gene2.put("id","ENSG00000178102");
    	
    	GenomicFeature gFeature1 = new GenomicFeature(gene1, 
    												new Variant(),
    												1l,
    												new HashMap<String, String>());
    	List<GenomicFeature>gFeatures1=new ArrayList<GenomicFeature>();
    	gFeatures1.add(gFeature1);
    	
    	
    	GenomicFeature gFeature2 = new GenomicFeature(gene2, 
													new Variant(),
													1l,
													new HashMap<String, String>()); 
    	List<GenomicFeature>gFeatures2=new ArrayList<GenomicFeature>();
    	gFeatures2.add(gFeature1);
    	gFeatures2.add(gFeature2);
    	
    	
    	Patient testP1 = new Patient("testPatient1Id", 
				 "testPatient1Label", 
				 new HashMap<String, String>(), 
				 "testSpecies",
				 "testSex1", 
				 "testAgeOfOnset1", 
				 "inheritanceMode1", 
				 new ArrayList<Map<String, String>> (), 
				 new ArrayList<PhenotypeFeature>(),
				 gFeatures1
				 );
    	
    	Patient testP2 = new Patient("testPatient1Id", 
				 "testPatient1Label", 
				 new HashMap<String, String>(), 
				 "testSpecies",
				 "testSex1", 
				 "testAgeOfOnset1", 
				 "inheritanceMode1", 
				 new ArrayList<Map<String, String>> (), 
				 new ArrayList<PhenotypeFeature>(),
				 gFeatures2
				 );
    	List<String> commonGenes=this.genotypeMatch.findCommonGenes(testP1, testP2);
    	assertTrue(1==commonGenes.size());
        assertTrue("ENSG00000178104"==commonGenes.get(0));
    }
    
    
    /**
     * Rigourous Test :-)
     */
    public void testApp2()
    {

        assertTrue( true );
    }
}
