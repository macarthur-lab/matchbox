package org.broadinstitute.macarthurlab.matchbox;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.broadinstitute.macarthurlab.matchbox.entities.GenomicFeature;
import org.broadinstitute.macarthurlab.matchbox.entities.MatchmakerResult;
import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.broadinstitute.macarthurlab.matchbox.entities.PhenotypeFeature;
import org.broadinstitute.macarthurlab.matchbox.entities.Variant;
import org.broadinstitute.macarthurlab.matchbox.match.GenotypeSimilarityServiceImpl;
import org.broadinstitute.macarthurlab.matchbox.match.MatchServiceImpl;
import org.broadinstitute.macarthurlab.matchbox.match.MatchService;
import org.broadinstitute.macarthurlab.matchbox.match.PhenotypeSimilarityServiceImpl;
import org.broadinstitute.macarthurlab.matchbox.metrics.Metric;
import org.broadinstitute.macarthurlab.matchbox.search.PatientRecordUtility;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase
{
	private GenotypeSimilarityServiceImpl genotypeSimilarity = new GenotypeSimilarityServiceImpl();
	private PhenotypeSimilarityServiceImpl phenotypeSimilarity = new PhenotypeSimilarityServiceImpl();
	
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
     * test GeneTypeMatch Constructor
     */
    public void testGeneTypeMatchConstructor()
    {
        Assert.assertNotNull(this.genotypeSimilarity);
    }

    /**
     * TestfindCommonGenes() function
     */
    public void testFindCommonGenes()
    {
    	List<Patient>testPatients = getTwoTestPatients();
    	Patient testP1 = testPatients.get(0);   	
    	Patient testP2 = testPatients.get(1);
  
    	List<String> commonGenes=this.genotypeSimilarity.findCommonGenes(testP1, testP2);
    	assertTrue(1==commonGenes.size());
        assertTrue("ENSG00000178104"==commonGenes.get(0));
    }

    
    /**
     * helper: returns 2 test patients
     */
    private List<Patient> getTwoTestPatients()
    {
    	List<Patient> patients = new ArrayList<Patient>();
    	
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
    	
    	patients.add(testP1);
    	patients.add(testP2);
    	return patients;
    }
    
    
    /**
     * Returns a complete test patient
     * @return a test patient
     */
    private Patient getTestPatient(){

       	    //contact
        	Map<String,String> contact = new HashMap<String,String>();
        	contact.put("href", "http://www.ncbi.nlm.nih.gov/pubmed/23542699");
        	contact.put("institution","Children's Hospital of Eastern Ontario");
        	contact.put("name","Lijia Huang");

        	//disorders
        	List<Map<String,String>> disorders = new ArrayList<Map<String,String>>();
        	Map<String,String> disorder1=new HashMap<String,String>();
        	disorder1.put("id", "MIM:614261");
        	disorders.add(disorder1);
        	Map<String,String> disorder2=new HashMap<String,String>();
        	disorder2.put("label","#614261 MICROCEPHALY-CAPILLARY MALFORMATION SYNDROME; MICCAP");
        	disorders.add(disorder2);

        	//features
        	List<PhenotypeFeature> features =  new ArrayList<PhenotypeFeature>();
        	PhenotypeFeature pf1 = new PhenotypeFeature("HP:0100026", "yes", "");
        	PhenotypeFeature pf2 = new PhenotypeFeature("HP:0003561", "yes", "");
        	PhenotypeFeature pf3 = new PhenotypeFeature("HP:0011451", "yes", "");
        	PhenotypeFeature pf4 = new PhenotypeFeature("HP:0009879", "yes", "");
        	PhenotypeFeature pf5 = new PhenotypeFeature("HP:0011097", "yes", "");
        	PhenotypeFeature pf6 = new PhenotypeFeature("HP:0001263", "yes", "");
        	features.add(pf1);
        	features.add(pf2);
        	features.add(pf3);
        	features.add(pf4);
        	features.add(pf5);
        	features.add(pf6);
        	
        	//genomic features
        	
        	//GF1-gene
        	Map<String,String> gene1 = new HashMap<String,String>();
        	gene1.put("id", "STAMBP");
        	//GF1-type
        	Map<String,String> type1 = new HashMap<String,String>();
        	type1.put("id","SO:0001583");
        	type1.put("label", "MISSENSE");
        	//GF1-variant
        	Variant variant1 = new Variant("GRCh37","2",74058108L,0L,"","");    	
        	GenomicFeature genomicFeature1 = new GenomicFeature( gene1, variant1,0L,type1);

        	//GF2-gene
        	Map<String,String> gene2 = new HashMap<String,String>();
        	gene2.put("id", "STAMBP");
        	//GF2-type
        	Map<String,String> type2 = new HashMap<String,String>();
        	type2.put("id","SO:0001587");
        	type2.put("label", "STOPGAIN");
        	//GF2-variant
        	Variant variant2 = new Variant("GRCh37","2",74074670L,0L,"","");    	
        	GenomicFeature genomicFeature2 = new GenomicFeature( gene2, variant2,0L,type2);
        	
        	List<GenomicFeature> genomicFeatures = new ArrayList<GenomicFeature>();
        	genomicFeatures.add(genomicFeature1);
        	genomicFeatures.add(genomicFeature2);

        	String id= "P0000083";
    	    String label = "206_LR07-155a1";
			String species="";
			String sex="";
			String ageOfOnset="";
			String inheritanceMode="";
        	Patient p = new Patient(
        							id,
        							label,
        							contact,
        							species,
        							sex,
        							ageOfOnset,
        							inheritanceMode,
        							disorders,
        							features,
        							genomicFeatures
       							);
        	return p; 
    }
        
    
    
    /**
     * Test parsing patient structure
     */
    public void testPatientStructureParsing(){
    	String patientString="{\"patient\":{\"id\":\"1\", \"contact\": {\"name\":\"Jane Doe\", \"href\":\"mailto:jdoe@example.edu\"},\"features\":[{\"id\":\"HP:0000522\"}],\"genomicFeatures\":[{\"gene\":{\"id\":\"NGLY1\"}}]}}";
    	Patient patient = new PatientRecordUtility().parsePatientInformation(patientString);
    	System.out.println(patient.getEmptyFieldsRemovedJson());
    }
 
    
    /**
     * Test if empty fields get scrubbed out in Variant class
     */
    public void testVariantEmptyFieldScrubbing(){
    	Patient testP1 =  getTestPatient();
    	for (GenomicFeature gf:testP1.getGenomicFeatures()){
    		Assert.assertEquals(-1, gf.getVariant().getEmptyFieldsRemovedJson().indexOf("end"));
    		Assert.assertEquals(-1, gf.getVariant().getEmptyFieldsRemovedJson().indexOf("referenceBases"));
    		Assert.assertEquals(-1, gf.getVariant().getEmptyFieldsRemovedJson().indexOf("alternateBases"));
    		Assert.assertEquals(42, gf.getVariant().getEmptyFieldsRemovedJson().indexOf("start"));
    	}	
    }
    
    
    /**
     * Test if empty fields get scrubbed out in PhenoTypeFeature class
     */
    public void testPhenotypeFeatureEmptyFieldScrubbing(){
    	Patient testP1 =  getTestPatient();
    	for (PhenotypeFeature ff:testP1.getFeatures()){
    		Assert.assertEquals(-1, ff.getEmptyFieldsRemovedJson().indexOf("ageOfOnset"));
    		Assert.assertEquals(2, ff.getEmptyFieldsRemovedJson().indexOf("id"));
    	}	
    }
    
    
    
    
    /**
     * Test if empty fields get scrubbed out in GenoTypeFeature class
     */
    public void testGenotypeFeatureEmptyFieldScrubbing(){
    	Patient testP1 =  getTestPatient();
    	for (GenomicFeature gf:testP1.getGenomicFeatures()){
    		Assert.assertEquals(-1, gf.getEmptyFieldsRemovedJson().indexOf("zygosity"));
    	}	
    }
    
    
    /**
     * Test if empty fields get scrubbed out in complete Patient entity
     */
    public void testPatientEmptyFieldScrubbing(){
    	Patient testP1 =  getTestPatient();
    	Assert.assertEquals(-1, testP1.getEmptyFieldsRemovedJson().indexOf("species"));
    	Assert.assertEquals(-1, testP1.getEmptyFieldsRemovedJson().indexOf("ageOfOnset"));
    	Assert.assertEquals(-1, testP1.getEmptyFieldsRemovedJson().indexOf("sex"));
    	Assert.assertEquals(-1, testP1.getEmptyFieldsRemovedJson().indexOf("inheritanceMode"));
    	Assert.assertEquals(-1, testP1.getEmptyFieldsRemovedJson().indexOf("alternateBases"));
    	Assert.assertEquals(-1, testP1.getEmptyFieldsRemovedJson().indexOf("referenceBases"));
    	Assert.assertEquals(109, testP1.getEmptyFieldsRemovedJson().indexOf("name"));
    }
    
    
    
    /**
     * Test counting distinct genes for metrics
     */
    public void testMetricsDistinctGenes(){
    	Metric metric = new Metric();
    	metric.countGenesInSystem();
    }
    
    
    
    /**
     * Test if a perfect match gives back a 1 score
     */
    public void testPerfectMatchScore()
    {
    	MatchService match = new MatchServiceImpl();
    	
    	Patient testP1 =  getTestPatient(); 
 
    	//mocking an exact match by adding the same patient in
    	List<Patient> matches = new ArrayList<Patient>();
    	matches.add(testP1);
    	
    	List<Double> patientGenotypeRankingScores = this.genotypeSimilarity.rankByGenotypes(matches, testP1);
    	List<Double> patientPhenotypeRankingScores = this.phenotypeSimilarity.rankByPhenotypes(matches, testP1);
    	List<Double> scores = match.generateMergedScore(patientGenotypeRankingScores,patientPhenotypeRankingScores);
    	Assert.assertEquals(1.0, scores.get(0));
    }

}